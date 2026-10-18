# Engineering Notes

A candid internal record of bugs found/fixed, open issues, and one-time cleanup done on this
project — written so it's possible to come back later and remember *why* things are the way they
are, without re-deriving it from scratch. Kept local-only (see `.gitignore`), not part of the
public-facing project.

For CI sharding/reporting/deployment specifically, see `CI-Sharding-And-Reporting.md`.

---

## 1. Bugs found and fixed

### `SafeActions.safeClickBooleanWithScreenShot` never actually retried
`while (attempts < 1)` capped it to a single pass, and the `catch` block additionally `return
false`d immediately — so on any transient failure (e.g. `StaleElementReferenceException`, which
the surrounding code elsewhere explicitly anticipates), the filter click gave up instantly instead
of retrying, despite log messages claiming "after 3 attempts." Fixed to `while (attempts < 3)`
with the early `return false` removed so it genuinely loops back and retries. Compare
`safeClickBoolean` (the sibling method, now unused/removed — see below), which always retried
correctly; this one just had the bug.

### Several `SafeActions` methods retried 2 times but logged "after 3 attempts"
`safeClick`, `safeFindElements`, both `safeFindElement` overloads used `while (attempts < 2)` (2
total attempts) but their log/print messages said "3 attempts" — misleading when reading logs to
diagnose a failure. Messages corrected to say "2 attempts," matching actual behavior.

### `safeFindElement(By)` / `safeFindElement(WebElement)` caught `Exception`, not specific types
Unlike `safeClick`/`safeFindElements` (which catch `TimeoutException | ElementClickInterceptedException
| StaleElementReferenceException`), these two caught a blanket `Exception` — meaning a genuine
programming error (NPE from a bad locator, etc.) would be silently swallowed and retried with a
page refresh instead of surfacing. Narrowed to `TimeoutException | StaleElementReferenceException`.

### Dead code: `SafeActions.safeClickBoolean(By)`
Never called anywhere (`safeClickBooleanWithScreenShot` is what every flow actually uses). Also
had a leftover bug inside it (`getfilterByTypeAndName(testName, testName)` — passing the test name
as both the filter type *and* filter value, clearly a copy-paste leftover). Removed entirely since
nothing referenced it.

### `safeFindElements` returning `null` crashed three flow methods
`safeFindElements` legitimately returns `null` (not an empty list) once it exhausts its retries —
documented, intentional behavior. But `SharedFilterFlows`, `OperatingSystemFilterFlows`, and
`BrandFilterFlows` all called `.size()` on the result without a null check in several places,
causing `NullPointerException` crashes (this is what actually prompted the investigation — a real
CI failure: `Cannot invoke "java.util.List.size()" because "inloopParent" is null`). Added null
checks at every call site; null is now treated the same as "index no longer present" /
"nothing found," returning the results collected so far instead of crashing.

### `SharedFilterFlows` didn't check whether the product page actually loaded before scraping
If a product click didn't open a new tab (same-tab navigation is a legitimate outcome, not just a
failure case — see below) *and also* didn't actually navigate anywhere, the code would still run
the full scrape against whatever page it was still on, wasting a full set of retry-heavy
`fetchTextWithRetries` calls that were guaranteed to fail. Added a `productPageLoaded` check
(`isElementInViewport` on the product-detail title element) that gates the scrape and the
"see more details" click behind it — skips straight to cleanup when navigation clearly didn't
happen, instead of burning through doomed retries.

### `closeCurrentWindowAndSwitchBack` could close every open window if the handle was stale
It loops over all open window handles and closes anything that isn't `originalWindow` — if
`originalWindow` itself were ever missing from the live handle set, every remaining window
(including what might be the only one left) would get closed. Guarded the call site in
`SharedFilterFlows`'s `finally` block: only calls it when the *active* window differs from
`currentWindow` in the first place (if we're already back on it, there's nothing to clean up).

### `CaptchaHandler.handleCaptcha()` never verified the *last* retry attempt actually worked
The loop checks `isCaptchaPage()` at the *top* of each iteration — which doubles as "did the
previous attempt clear it?" for every pass except the very last one. After the final click/refresh,
the loop just exits and throws `SkipException` unconditionally, even if that last attempt actually
succeeded. Added one more `isCaptchaPage()` check right before the throw.

### `DriverManager` had ~15 browser-arg config keys that silently did nothing
Code read keys like `chrome.arg.disable_gpu`, `chrome.arg.start_maximized` (dotted, `arg.`-prefixed,
snake_case); the properties file actually has `chrome.disableGpu`, `chrome.startMaximized`
(camelCase, no `arg.`). Since a missing key just falls back to the hardcoded default, every one of
these settings was unconfigurable from `UtilData.properties` — it happened to *look* like it worked
only because the hardcoded defaults matched what the file also said. Renamed every key in
`DriverManager.java` to match what's actually in the properties file, for all three browsers.

Also fixed in the same pass:
- Edge headless mode was reading `chrome.headless` instead of its own `edge.headless`.
- The `disable.image.loading` key didn't exist at all (real keys are `firefox.image.loading` /
  `edge.image.loading` / `chrome.image.loading`, and they mean "is image loading **enabled**," the
  opposite sense of the old code) — fixed the key names *and* inverted the condition.
- Edge's grid hub URL lookup reused the `edge.runOnGrid` boolean key instead of the actual
  `edge.gridHubUrl` key (would have connected to a URL literally named `"true"` if grid mode were
  ever enabled). Fixed to read `edge.gridHubUrl`.
- Firefox's grid hub URL had the same class of bug, plus the properties file itself had the wrong
  *value* (`firefox.gridHubUrl=false`, a boolean-looking string instead of a URL) — fixed both the
  code's key name and the properties file's value.

---

## 2. Known issues — flagged, not fixed

### Filter-value matching is unreliable for several tests
Most filter-validation tests do a plain, case-lowered `String.contains(filterValue)` check against
scraped product text. This has real, observed failure modes:
- **Abbreviations**: Amazon renders "Windows" as "Win" and "Motorola" as "Moto" on some listings —
  a real Motorola phone title not containing the literal word "Motorola" fails the brand check even
  though the product is correct. Reproduced multiple times in actual CI runs, not hypothetical.
- **Unit mismatches**: a display-size filter like `"up to 12.9 cm"` checked against a product
  description that only states `"4.05 inches"` can never match, even when the actual size does
  satisfy the filter, because there's no unit conversion.
- **The Discount tests may be structurally unwinnable**: a product's title/description describes
  the product itself, not the storefront's current promotional discount percentage — that number
  generally doesn't appear anywhere in product copy at all.

The alternative discussed (`GenericUtility.containsCheck`, word-split AND-matching) isn't strictly
better — it trades false negatives for false positives, since it matches each word as a bare
substring with no word-boundary check (e.g. a `"4 GB"` filter would match text containing
`"64GB"`). A proper fix needs word-boundary-aware, unit-normalizing matching — not yet built.

### A test method without a `groups=` tag silently runs in no CI shard
Covered in `CI-Sharding-And-Reporting.md` — flagging again here since it's a correctness trap for
future contributors, not just a CI detail.

### `verifyingTheBrandsFilterFunctionality` had one unexplained, suspicious failure
One real CI run filtering for Brand = **Apple** returned 16 **Lenovo** tablets as mismatches (plus
one genuine Apple accessory). This doesn't look like the matching-brittleness issue above — it
looks like the "Apple" filter checkbox either wasn't actually applied before the scrape ran, or the
page hadn't finished refreshing its results. Not reproduced again in later runs (different brands
got sharded into that slot), so root cause is still unconfirmed. Worth a closer look at
`BrandFilterFlows`'s click-to-scrape timing if it recurs.

### `pom.xml` has a dead `testng` profile
Points at `src/test/runners/testng.xml`, which doesn't exist in the repo (the actual file was
renamed to `AllTestsGlobal.xml` at some point, per an old commit message — the profile pointing at
the old name was never updated or removed).

---

## 3. Public-repo hygiene (one-time cleanup)

This repo is public (`github.com/sagarjhathi/amazon-filter-e2e-automation`). A due-diligence pass
found and addressed:

- **No `LICENSE` file existed** — added MIT, with a README disclaimer that this project is for
  learning/portfolio purposes, not affiliated with Amazon, and not intended for high-volume/scraping
  use (Amazon's ToS restricts automated access).
- **Secrets scan**: clean. Checked all currently-tracked files for hardcoded credentials/tokens/API
  keys, and all ~11,600 unique filenames ever committed across the entire git history for
  secret-looking names (`.env`, `*credential*`, `*.pem`, `id_rsa`, etc.). Nothing found either way.
- **`gh-pages` branch had accumulated ~9GB of history** — root cause and fix are the main subject of
  `CI-Sharding-And-Reporting.md` §3e. Summary: the old deploy step did `git add . / git commit /
  git push --force` every run; `--force` only moves the branch pointer, it doesn't delete the
  previous commit, so ~1,400 CI runs' worth of screenshots and reports silently piled up forever,
  even though only the latest commit was ever served. Fixed in two layers: squashed the existing
  history down to one commit (verified via tree-hash comparison that the live site's content didn't
  change at all), and changed the deploy step itself to reset history via an orphan commit every
  run, so it can't silently regrow.
- **`master`'s own history (825 commits)** still carries a handful of early (Sept–Oct 2025)
  screenshot/log commits from before `.gitignore` was tuned — `git rm --cached` was used at the
  time to stop tracking them going forward, but that doesn't remove them from the commits where
  they were already recorded. Left untouched (explicit decision: only `gh-pages` was to be
  touched, never `master` — see below). Would need `git filter-repo` to actually remove, if ever
  wanted.
- **A full clone pulls all 8 branches by default**, not just `master` — measured at 511MB / ~15
  minutes for a fresh clone after the `gh-pages` cleanup (down from an effectively multi-GB clone
  before it). The remaining size is spread across the other 7 branches
  (`Improv`, `Improvements`, `feature-allure-integration`, `feature/fixes`,
  `fix/config-resource-path`, `main`, `google-aggregator-pom-updated`) plus `master` itself — not
  yet individually audited.

**Standing rule honored throughout this cleanup**: `master` was never fetched, read, or written at
any point — all `gh-pages` work was done in an isolated git worktree specifically to guarantee
this, per explicit instruction.

---

## 4. Config system — quick reference

- **`ConfigManager.get(key)` resolution order**: system property (`-Dkey=value`) → environment
  variable (key uppercased, `.` replaced with `_`) → `UtilData.properties`. First non-blank match
  wins.
- **`DriverManager` browser selection**: `-Dbrowser=<x>` on the command line wins over
  `ConfigManager.get("browser", "chrome")`; defaults to Chrome if neither is set.
- **`-DUtilData.file=<path>`** points `ConfigManager` at a completely different properties file
  instead of the one bundled on the classpath — useful for an environment-specific config without
  editing the checked-in file.
