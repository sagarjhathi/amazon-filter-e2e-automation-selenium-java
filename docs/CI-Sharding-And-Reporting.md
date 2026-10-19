# CI Sharding, Reporting & GitHub Pages Deployment

This document explains how the CI pipeline splits tests into parallel shards, collects their
reports, merges them, and publishes the result to GitHub Pages. It covers *what* each piece
does and *why* it's built that way, since several of the design choices here exist specifically
to work around real failures hit while building this.

Source of truth: `.github/workflows/main.yml`, `pom.xml`, `src/test/java/tests/AmazonTests.java`.

---

## 1. Test grouping (how a test knows which shard it belongs to)

Every `@Test` method in `AmazonTests.java` carries a `groups = { "..." }` tag, matching one of
three groups:

| Group | Methods |
|---|---|
| `BatteryProcessorStorage` | Storage Capacity, Battery Capacity, Processor Speed, Discount |
| `BrandsScreenSizeTypeCamEtc` | Camera Resolution, Display Type, Display Size, Brands |
| `DeliveryPriceOs` | Get It Tomorrow, Get It In 2 Days, Get It Today, Free Delivery, Price Slider, OS Version |

All 14 test methods are covered — no method is in more than one group, and none is ungrouped.

**Why OS Version sits in `DeliveryPriceOs` rather than the battery/processor/storage group it was
originally in:** the three shards weren't just topical groupings — runtime was wildly uneven (one
shard took ~19 minutes, another ~4), because OS Version alone is the single heaviest test of all
14 (~7.4 minutes by itself, from real measured CI timing data). Moving just that one test rebalanced
all three shards to within about a minute of each other (~11-12 min each), while keeping every
group's name still accurate to what it actually contains — unlike an earlier, rejected attempt at a
fully time-balanced split, which would have required meaningless numbered group names
(`Shard1`/`Shard2`/`Shard3`) that tell a future contributor nothing about what's inside.

### Why groups instead of per-shard XML files

Earlier, shards were defined by three separate TestNG suite XML files (`BatteryProcessorOsStorage.xml`,
etc.), each with a hand-maintained `<include name="...">` list of method names. That has a real
correctness risk: if a new test method is added and someone forgets to add it to one of those XML
files, it silently never runs in CI — no error, no warning, it just disappears from sharded runs
while still working fine locally.

Groups solve this differently: the test method declares its own group inline, right next to its
`@Test` annotation, so there's one less place to remember to update. (Note: this doesn't eliminate
the risk entirely — a method still needs *some* `groups=` tag, or it won't run in any shard. See
[Section 6](#6-known-limitations).)

### How filtering actually works — `pom.xml`

```xml
<properties>
  <groups></groups>  <!-- empty by default = no filter = run everything -->
</properties>
```
```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <suiteXmlFiles>
      <suiteXmlFile>src/test/runners/AllTestsGlobal.xml</suiteXmlFile>
    </suiteXmlFiles>
    <groups>${groups}</groups>
  </configuration>
</plugin>
```

There is still only **one** suite file (`AllTestsGlobal.xml`, unchanged, contains the whole
`AmazonTests` class, no group filtering inside it). The `groups` property is what narrows
execution down:

- `mvn test` — `groups` is empty, runs everything. This is what a plain local run does.
- `mvn test -Dgroups=BatteryProcessorStorage` — runs only the 4 methods tagged with that group.

This is the mechanism CI uses for sharding (see Section 2). The old per-shard XML files and
`pom.xml` profiles (`BatteryProcessorOsStorage`, `BrandsScreenSizeTypeCamEtc`, `DeliveryAndPrice`,
`testng`, `LocalExecution`) still exist in the repo but are no longer what CI uses — they're
effectively superseded, not deleted.

---

## 2. CI job 1 — `test` (the actual sharding)

```yaml
strategy:
  fail-fast: false
  matrix:
    group: [BatteryProcessorStorage, BrandsScreenSizeTypeCamEtc, DeliveryPriceOs]
```

This turns one job definition into **3 parallel jobs**, one per group, each running:
```yaml
run: mvn test -Dgroups=${{ matrix.group }}
```

`fail-fast: false` is deliberate — without it, GitHub Actions cancels the other two shards the
moment one of them fails, which defeats the entire point of sharding for fast, complete feedback.

Each shard job, after running its slice of tests:
1. Finds its own freshly-created `run_<timestamp>/` folder (produced by `PathManager` /
   `ReportManager` inside the Java app — each JVM run gets its own timestamp).
2. Uploads that whole folder as a build artifact named **`extent-<group>`** (e.g.
   `extent-BatteryProcessorStorage`).
3. Uploads its raw Allure results (`target/allure-results/`) as **`allure-results-<group>`**.

Each shard job does **not** touch `gh-pages` at all anymore. That's the `publish` job's work —
see why in Section 4.

---

## 3. CI job 2 — `publish` (collect, merge, deploy)

```yaml
publish:
  needs: test
  if: always()
```

`needs: test` means this job waits for **all 3** matrix shards to finish (not just one).
`if: always()` means it still runs even if a shard's tests failed (shard *test failures* are
already tolerated via `continue-on-error: true` on the Maven step — this `if: always()` is extra
insurance so the dashboard still gets published even in a partial-failure run).

### 3a. Collecting everything

Six explicit download steps (three Extent report folders, three Allure result folders) —
kept as separate, explicitly-named steps rather than a wildcard pattern download, so each
step is self-evident at a glance rather than relying on pattern-matching logic.

### 3b. Merging the Allure results

```yaml
allure generate allure-results/BatteryProcessorStorage allure-results/BrandsScreenSizeTypeCamEtc allure-results/DeliveryPriceOs --clean -o allure-report
```

The Allure CLI accepts **multiple** results directories as positional arguments and merges them
natively into one combined report — no manual file copying or merging logic needed. This works
cleanly because Allure attachments (screenshots, logs — see `ReportAttachments.java`, which already calls
`Allure.addAttachment(...)` for both) are stored with UUID-based filenames inside each
`allure-results/` folder, so there's no name-collision risk when three shards' results sit
side by side.

### 3c. NOT merging the Extent reports — relocating them as whole units instead

This is the one place where "just merge everything" would have silently broken things, and is
worth understanding in detail.

Each Extent report (`ExtentReport.html`) links to its logs and screenshots using **relative
paths**, generated by `PathManager`:
```html
<a href='../logs/verifyingDiscountFilterFunctionality_Mobile_.../....log'>...</a>
<img src="../screenshots/verifyingDiscountFilterFunctionality_Mobile_.../....png">
```
These only resolve correctly because of one fixed, assumed folder layout:
```
run_<timestamp>/
  reports/ExtentReport.html   <- "../" from here lands at run_<timestamp>/
  logs/<test-name>/<test-name>.log
  screenshots/<test-name>/<image>.png
```
If the publish job tried to flatten or interleave the three shards' `logs/` and `screenshots/`
folders into one shared top-level folder, every link inside every report would point at the wrong
place (or nowhere). So instead, each shard's entire downloaded folder — `reports/`, `logs/`,
`screenshots/` together, completely untouched internally — is copied as **one intact unit** into
its own named subfolder:

```
run_<common-timestamp>/
  BatteryProcessorStorage/
    reports/ExtentReport.html   <- internal ../logs, ../screenshots still resolve correctly
    logs/...
    screenshots/...
  BrandsScreenSizeTypeCamEtc/
    reports/ExtentReport.html
    logs/...
    screenshots/...
  DeliveryPriceOs/
    reports/ExtentReport.html
    logs/...
    screenshots/...
  allure-report/
    index.html
```
Because nothing inside each shard's folder was touched — only the folder itself was moved and
renamed — none of the HTML's internal relative links need rewriting. Three separate, fully
working Extent reports, plus one merged Allure dashboard.

### 3d. The index page

A plain, self-contained `index.html` is generated (inline `<style>`, no external
fonts/CDN/JS — loads instantly, never depends on a third party being reachable) with 4 cards:
the merged Allure report (marked "MERGED") and the three individual Extent reports.

Built as a PowerShell string array piped through `Set-Content`, not a here-string
(`@"..."@`). Here-strings require their closing `"@` at column 0 with zero leading
whitespace — indented inside a YAML `run: |` block, that would fail to parse. An array of
quoted strings avoids the whole problem.

### 3e. Deploying — the orphan-commit reset

```powershell
git checkout --orphan gh-pages-deploy
git add -A
git commit -m "Update report"
git push origin gh-pages-deploy:gh-pages --force
```

This replaced an earlier version that did a plain `git add . / git commit / git push --force` on
top of whatever was already checked out on `gh-pages`. That pattern is a trap: `--force` only
moves where the branch *pointer* points — it does not delete the commit that used to be there.
Every CI run was silently *stacking* a new commit (screenshots, reports, the lot) on a pile
nothing was ever cleaning out. After ~1,400 runs, `gh-pages` alone had accumulated roughly 9GB of
dead history, even though only the single latest commit is ever actually served live.

`git checkout --orphan` starts a brand-new commit with **no parent** — so when it's force-pushed,
the entire branch becomes just that one commit, every single run, forever. No accumulation is
possible because there's nothing to accumulate onto.

### 3f. Windows long-path support

```yaml
- name: Enable long paths (Windows)
  run: git config --global core.longpaths true
```

Some test method names are long (`verifyingMobilePhonePrimaryCameraResolutionFilterFunctionality`),
and combined with the `run_<timestamp>/logs/<test-name>/<test-name>.log` nesting plus checking
`gh-pages` out into its own subfolder, some paths exceed Windows' legacy 260-character limit.
GitHub's hosted Windows runners already support long paths at the OS level; `git.exe` just needs
this one explicit opt-in, since it defaults to the conservative/off setting regardless of what the
OS allows.

---

## 4. Why deploying moved out of the shard jobs entirely

The original (pre-sharding) workflow ran everything — test, then checkout `gh-pages`, then
deploy — in one single job. With 3 parallel shard jobs, if each one kept doing its own
checkout-and-deploy independently, they'd race to force-push `gh-pages` within seconds of each
other — the exact same accumulation problem as Section 3e, just reintroduced through concurrency
instead of history. Each push would still be a single orphan commit, but three nearly-simultaneous
pushes means two of them get rejected/overwritten, and there's no guarantee the final published
site actually contains all three shards' results.

Solution: shard jobs only test and upload artifacts. Exactly **one** job (`publish`), which waits
for all three shards via `needs: test`, does the one and only deploy per CI run.

---

## 5. End-to-end flow, summarized

```
push/PR/schedule triggers CI
        │
        ▼
┌─────────────────────────── test (matrix × 3, parallel) ───────────────────────────┐
│  BatteryProcessorStorage     BrandsScreenSizeTypeCamEtc   DeliveryPriceOs          │
│  mvn test -Dgroups=...       mvn test -Dgroups=...        mvn test -Dgroups=...    │
│  → extent-<group> artifact   → extent-<group> artifact    → extent-<group> artifact│
│  → allure-results-<group>    → allure-results-<group>     → allure-results-<group> │
└──────────────────────────────────────┬──────────────────────────────────────────┘
                                        │ needs: test (waits for all 3)
                                        ▼
                                    publish (1 job)
                              download all 6 artifacts
                              allure generate (merges 3 → 1)
                              relocate each shard's report as a whole unit
                              build index.html
                              git checkout --orphan + force-push → gh-pages
                                        │
                                        ▼
                      https://sagarjhathi.github.io/amazon-filter-e2e-automation/
                      (GitHub Pages — serves the branch statically, no server to run)
```

---

## 6. Known limitations

- **A test method without a `groups=` tag silently runs in no shard.** It would still run in a
  plain local `mvn test` (no filter applied), but would never appear in sharded CI output. No
  build failure, no warning. Worth adding a guard (a small test or CI check asserting every
  `@Test` in `AmazonTests` has a group) before relying on this for real coverage guarantees.
- **If a shard fails before uploading its artifacts** (e.g. `Build project` or driver setup fails
  outright, not just a test assertion), the `publish` job's download step for that shard's
  artifact will fail outright and block the whole publish — rather than publishing the other two
  shards' reports anyway. Accepted as a simplification; a more resilient version would treat a
  missing artifact as "skip this shard's card" instead of failing the whole job.
- **The old per-shard XML files and Maven profiles are still in the repo**, unused by CI now but
  not removed, in case they're still wanted for local/manual runs.
