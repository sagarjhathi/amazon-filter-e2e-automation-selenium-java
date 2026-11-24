# CLAUDE.md: read this first in every session

This file is loaded into every Claude Code session for this repo. It tells you what the project is, how to run it, the rules you must follow, and where the history lives. Facts here were checked on 2026-11-22 unless marked UNVERIFIED.

## 0. Start-of-session habit (do this before anything else)

1. Read this file.
2. Read `docs/STATUS.md` (current state, next step, backlog, known issues) and `docs/LESSONS.md` (mistakes not to repeat). Read `docs/DECISIONS.md` when you are about to change how something works.
3. **`docs/` is git-ignored on purpose (owner decision). It exists on the owner's machine, not in a fresh clone.** If `docs/STATUS.md` is missing, say so and ask the owner. Do not invent project history. (`.claude/` is git-ignored too, so the skills, agents and settings are local as well.)
4. Run `git status -sb` and `git log --oneline -5` to see where the repo is now. Do not trust a status snapshot given at the start of a conversation: it can be stale (LESSONS L7).

## 1. Project purpose

End-to-end tests for the product-list filters on Amazon India (amazon.in), written with Selenium, TestNG and Maven (Java). A test searches for a term (Laptop, Mobile, Headphones, Tablet, Speaker), applies filter options in the left navigation, opens products or reads the result cards, and compares what the page shows with what the filter promised. They run against the live site, locally or on GitHub Actions, and publish reports to GitHub Pages.

- **Who it is for:** the owner (GitHub user `sagarjhathi`), as a portfolio and learning project in end-to-end test design. It is **educational only**: low-volume demo runs, not a product, not for commercial use, not for building datasets, not continuous scraping. It is unofficial and not affiliated with Amazon. The README "Disclaimer" carries the full text.
- **End goal:** a readable, trustworthy suite whose results mean something: a filter that Amazon does not show is a **skip with a reason**, a filter that is shown but could not be checked is a **fail**, and a green run is never taken as proof of testing without the skipped count.
- **Sister project:** the same work in TypeScript (Playwright) at `C:\Sagar\amazon-e2e-filter-automation` (GitHub `sagarjhathi/amazon-e2e-filter-automation-playwright-ts`; checked 2026-11-22: `git remote -v` of that folder shows this repo, its default branch is `master`, and its Pages site has "Main suite" and "Card checks" sections). It is a separate repo and stack. This repo's session never edits it; see rule 11 and the skill `port-to-ts-prompt`.

Repo: `sagarjhathi/amazon-filter-e2e-automation-selenium-java` (renamed from `amazon-filter-e2e-automation`; the local clone folder still has the old name). Live reports: https://sagarjhathi.github.io/amazon-filter-e2e-automation-selenium-java/ (returned 200 on 2026-11-22; the old URL without `-selenium-java` returns 404).

## 2. Tech stack (versions read from `pom.xml` and the machine, 2026-11-22)

| Part | Version / note |
|---|---|
| Java | `maven.compiler.release` 21; local JDK 24.0.2; CI uses JDK 24 (Temurin) |
| Maven | 3.9.11 locally; compiler plugin 3.13.0; surefire 3.0.0-M5 (`<groups>${groups}</groups>`, one profile per runner) |
| TestNG | 7.10.2 |
| Selenium | 4.40.0 (`selenium-java`, `selenium-devtools-v144`) |
| WebDriverManager | 6.1.0 |
| Reporting | ExtentReports 5.1.2 (per run and per shard); Allure `allure-testng` 2.29.0 (results in `target/allure-results`, merged across shards in CI) |
| Logging | Log4j2 2.20.0, one log file per test |
| Data and helpers | Gson 2.10.1 (JSON test data), Apache POI 5.4.1 (`Products.xlsx`), Failsafe 3.3.2, thumbnailator 0.4.21 (screenshots) |
| Unused | `cucumber-java` and `cucumber-testng` 7.33.0 are declared in the pom but not used (confirmed by the owner, not removed yet) |
| Browser | Chrome by default (`browser=chrome`); Firefox and Edge are configured; optional Selenium Grid |
| CI | GitHub Actions on `windows-latest` (card `report` job on `ubuntu-latest`), Node 20/22 only for the Allure CLI |
| Report hosting | GitHub Pages from the `gh-pages` branch (root) |
| Alerts | Discord webhook (secret `DISCORD_WEBHOOK`), used by `main-suite.yml` only |

## 3. Folder map

Real project root: `eclipse-workspace/Intro/` (Maven module, `pom.xml` there). Because `<sourceDirectory>` is `src`, Java packages are `main.java.amazonfilterapplicatione2e.*` (main code) and `test.java.*` / `test.testDataProvider` (tests). That looks odd but is intentional-looking; keep it (renaming is a backlog item the owner postponed).

```
.github/workflows/main-suite.yml     CI of the 14 older tests: check, notify-check-failed, test (3 shards), publish
.github/workflows/card-checks.yml    CI of the card checks: check, card-checks (4 shards), report (publishes card-checks/)
.github/pages/index.html             landing page template, copied by BOTH workflows (see do-not list)
eclipse-workspace/Intro/
  pom.xml                            dependencies, surefire, profiles named after the runners
  src/main/java/amazonfilterapplicatione2e/
    base/           BaseTest (suite/run folder, driver per test, log context), BasePage
    pages/          AmazonLandingPage, ProductListingPage (locators, single-page actions)
    flows/          SharedFilterFlows, Brand/Delivery/OperatingSystem/PriceSlider flows, CardFilterFlows, ProductPageFlows, Card
    safeActions/    SafeActions (retrying click/find wrappers)
    captcha/        BlockerHandler ("Continue shopping", 503 -> reload, then skip), CaptchaHandler
    utilities/      GenericUtility, WaitUtility, FilterMatcher, BrandMatcher, CardChecks (rules, thresholds, verdicts), image helpers
    driverManager/  DriverManager (chrome/firefox/edge, ThreadLocal driver, optional grid)
    configManager/  ConfigManager (system property, then env var, then UtilData.properties)
    fileReader/     TestInputs (coverage JSON loader, FILTER_OF_TEST), ExcelReader, FileReader
    reporting/      ReportManager (Extent), TestListenerUpdated, Attachers
    pathManager/    PathManager (run_<timestamp>/reports|logs|screenshots)
    logger/ SeleniumGrid/                        small helpers (the unused constants/ and database/ stubs were removed 2026-11-24)
  src/main/resources/
    configs/UtilData.properties      all run settings (see section 4)
    log4j2.xml
    data/searchTerms.json, filterCoverage.json, filterHeadings.json, enabledTests.json (true/false per filter: false = no runs, not a skip; missing = on), Products.xlsx
  src/test/java/
    tests/AmazonTests.java           14 older filter tests (3 TestNG groups)
    tests/CardFilterChecks.java      10 card / product-page check kinds (4 TestNG groups, each also tagged CardChecks)
    unit/                            FilterMatcherTest, CoverageConsistencyTest, CardChecksTest (no browser)
    retry/                           RetryFailedTest, RetryListener (retry once, not after an AssertionError)
    testDataProvider/                TestDataProvider ("TestRuns", "SearchTerms", "ExcelData")
  src/test/runners/                  TestNG suite XMLs: AllTestsGlobal (pom default), BatteryProcessorStorage, BrandsScreenSizeTypeCamEtc,
                                     DeliveryPriceOs, CardFilterChecks, UnitTests
README.md, LICENSE, CLAUDE.md                        tracked at the repo root
```

How many tests (from `filterCoverage.json`, and matching the published Allure totals of 2026-11-20): the 14 older methods expand to **47 runs** (one per search term listed for the filter); the 10 card methods expand to **50 runs** (10 filters x 5 terms). These counts are with every test switched on in `enabledTests.json`; a filter set to `false` there has no runs. Since 2026-11-24 six main-suite filters are `false` (Battery, Camera Resolution, the old strict Discount and Brands tests, Operating System Version, Display Size), so a run has 24 main + 50 card runs (reasons in README and DECISIONS D36). The unit suite is **41 tests** (verified by running it on 2026-11-24).

`CLAUDE.md` is the one tracked Claude file, so a fresh clone has it (but not the ignored `docs/` or `.claude/`). Tracked in `docs/`: only `CI-Sharding-And-Reporting.md` and `Engineering-Notes.md`. Git-ignored (local, do not expect them in a clone): `docs/` (the rest), `.claude/` (skills, agents, settings), `run_*/`, `eclipse-workspace/Intro/target/`, `.../collected/`, `logs/`, `screenshots/`, `.../test-output/`, `.../bin`, Eclipse files (`.classpath`, `.project`, `.settings/`, `.metadata/`).

## 4. Commands

Run from `eclipse-workspace/Intro/`. "Verified" means it was run in this session on 2026-11-22. Anything that opens amazon.in is a **live run**: it is not pre-approved (rule 3 and the do-not list).

| Purpose | Command | Status |
|---|---|---|
| Compile main and test code (CI `check`) | `mvn -q test-compile` | **verified, exit 0** |
| Unit tests, no browser (CI `check`) | `mvn -q test "-Dsurefire.suiteXmlFiles=src/test/runners/UnitTests.xml"` | **verified** (the same suite without `-q`: 41 run, 0 failures, BUILD SUCCESS, 2026-11-24) |
| Clean compile (CI test jobs) | `mvn clean compile` | UNVERIFIED here, used by CI |
| Whole older suite (live) | `mvn clean test` (default suite `AllTestsGlobal.xml`, `groups` empty) | UNVERIFIED here, CI runs it |
| One older shard (live) | `mvn test -Dgroups=DeliveryPriceOs` (others: `BatteryProcessorStorage`, `BrandsScreenSizeTypeCamEtc`) | UNVERIFIED here, CI runs it |
| Smoke size (live) | `mvn clean test -DrunForAllFilterOptions=false -DoverideFilteOptionsCount=2` | UNVERIFIED (README) |
| Card checks, all (live) | `mvn test "-Dsurefire.suiteXmlFiles=src/test/runners/CardFilterChecks.xml"` | UNVERIFIED here, CI runs it |
| One card shard (live) | same, plus `"-Dgroups=CardSeller"` (also `CardBrandsReviewsDelivery`, `CardTopBrandsPayOnDelivery`, `CardRatingPriceDiscountSelfCheck`; `CardChecks` = all four) | UNVERIFIED here, CI runs it |
| One card search term (live) | same, plus `"-DcardChecks.terms=Headphones"` | UNVERIFIED here |
| One method with `-Dtest=...` | UNVERIFIED: the pom pins `suiteXmlFiles`, which may override `-Dtest` | UNVERIFIED |
| Lint / format | **none exists** (the pom has no lint or style tool) | verified by reading the pom |

PowerShell and bash both accept the quoted `"-D..."` form above. Reports: Extent at `run_<timestamp>/reports/ExtentReport.html` (with `logs/` and `screenshots/` next to it, in `eclipse-workspace/Intro`); Allure raw results in `target/allure-results`. On CI the shard reports are merged and published (section 5, CI).

Settings (system properties). Any key in `UtilData.properties` can be set with `-Dkey=value`, or with an environment variable named like the key in upper case with `.` replaced by `_` (for example `chrome.headless` -> `CHROME_HEADLESS`). Order: system property, then env var, then the file. `-DUtilData.file=<path>` swaps the whole file (read in `ConfigManager.java`, not run).

| Key | Default | Effect |
|---|---|---|
| `groups` | empty | TestNG group to run (surefire `<groups>`); empty = all |
| `surefire.suiteXmlFiles` | `AllTestsGlobal.xml` (pom) | which runner XML to run |
| `runForAllFilterOptions`, `overideFilteOptionsCount` | `true`, `3` | main suite: all filter options, or only N |
| `runForAllProductsUnderListing`, `overideProductsListingCount` | `false`, `3` | main suite: all products under a listing, or only N |
| `runForAllFilterOptionsCI`, `runForAllProductsUnderListingCI` | `true`, `true` | used on cron runs |
| `brandsFilterOptions`, `discountFilterOptions`, `discountCardsPerOption` | `all` | Brands and Discount cover every option, in local and CI runs |
| `sellerProductsPerOption` | `5` | card suite: product pages opened per Seller option |
| `badgeProducts` | `all` | card suite: pages opened per Top Brands / Pay On Delivery option (`all` or N) |
| `ratingProducts` | `8` | card suite: pages opened for the rating check |
| `pageDelayMs` | `2000` | pause in ms between product-page opens (not an action slow motion) |
| `collectDir` | `collected` | where the card checks save what they read |
| `cardChecks.terms` | blank | comma list of search terms for the card checks; blank = all of `searchTerms.json` |
| `browser`, `chrome.headless` | `chrome`, `false` | browser choice and headless mode (each browser has its own `*.` keys) |
| `safeActionsWaitTime`, `globalWaitTimeExtendingBasePage` | `5`, `20` | explicit wait times in seconds |
| `handleCaptcha.retries` | `2` (3 in the code if the key is missing) | attempts of `CaptchaHandler` on a captcha page; the 503 page is reloaded once by `BlockerHandler` |

Environment variable read directly (not through the file): `IS_CRON` (`true` only on the nightly schedule; set by both workflows from `github.event_name == 'schedule'`). It makes `SharedFilterFlows`, `OperatingSystemFilterFlows` and `CardFilterFlows` use the full-depth settings.

## 5. Conventions

**Layers.** Tests -> flows -> pages -> SafeActions -> driver/utilities -> reporting/logging; a layer calls only the layer(s) below it. Tests decide what to open, read and compare, and assert. Flows run multi-step procedures and return plain results. Pages hold locators (`By` fields ending in `By`) and single-page actions. Tests never call the WebDriver directly (no `driver.`, `findElement`, `By.` in test bodies). `FilterMatcher`, `BrandMatcher` and `CardChecks` touch no browser, so they stay unit-testable.

**Naming.** Older tests are `verifying<Filter>FilterFunctionality`; card tests are `<thing>FilterCheckedOn<Cards|TheProductPages>` (plus `cardSelfCheck` and `ratingOnTheCardEqualsRatingOnTheProductPage`). Every new test method needs: a `groups` tag matching a CI shard (else CI skips it), an entry in `TestInputs.FILTER_OF_TEST`, and its filter and search terms in `filterCoverage.json` (enforced by `CoverageConsistencyTest`). To switch a test off, set its filter to `false` in `enabledTests.json` (the only file to edit); never use a skip for that, and a key there that matches no filter fails the unit test.

**Code style.** Simple, clear code with short plain-language comments. Replaced code is kept as `// OLD:` comments (used throughout the repo). No formatter is configured: match the file you edit. About 97 `System.out` calls still exist; new code logs through `LoggerUtility.getLogger(...)` (lines are prefixed `[testName]`).

**Test patterns.**
- A filter or option Amazon does not show is `throw new SkipException(reason)`, never a bare `return` (TestNG counts a return as a pass). Flows must not swallow `SkipException`.
- A filter that is shown but nothing could be validated is a fail. Products with no captured details are not mismatches. Soft asserts inside loops, hard asserts for preconditions.
- The card checks judge each item `FITS`, `DOES_NOT_FIT` or `NO_VALUE` (`NO_VALUE` is listed, not judged) and need a share of fits per rule: Brands 0.9, Customer Reviews 0.95, Free Delivery 0.9, Discount 0.4 (within 5 points of the level), Price range 0.9, card self-check 0.95, card rating vs product page 0.9 (within 0.1), Pay On Delivery 1.0, Top Brands 1.0. Seller is reported (`seller-mismatch` warning), not failed. Rules live in `utilities/CardChecks.java`.
- Retries: `RetryListener` retries a test once on a non-assertion error; on in `AllTestsGlobal.xml` and `CardFilterChecks.xml`.
- Waits: explicit waits through WaitUtility/SafeActions; `Thread.sleep` only through `BasePage.pause`.
- Brand aliases are for real alternative names (iPhone for Apple), never for Amazon typos or reseller brand names: that would hide wrong data (DECISIONS D5).

**Git.** `master` is the default branch. `gh-pages` holds published reports only and is rewritten as one fresh parentless commit each time (retried compare-and-swap push, see CI): never work on it. Branch `arch-improvement` is old and retired: do not use it. **The owner makes every commit and push.** Never commit files that may hold secrets.

**CI.**
- `main-suite.yml`: triggers push and pull request to `master`, manual, and a nightly cron `50 17 * * *` UTC (11:20 PM IST). Jobs: `check` (compile + unit tests) -> `notify-check-failed` (Discord, only if `check` fails) -> `test` (matrix of the 3 groups, `fail-fast: false`, 45 min limit) -> `publish` (merged Allure report, Extent per group, deploy to `gh-pages`, not on pull requests).
- `card-checks.yml`: triggers push and pull request to `master`, manual, and a nightly cron `37 14 * * *` UTC (8:07 PM IST, about 3 hours before the main suite; schedules run only from `master`). Push, manual and nightly runs publish to `gh-pages`; pull requests do not. Jobs: `check` -> `card-checks` (matrix of the 4 groups) -> `report` (merged Allure report, publishes `card-checks/` and `index.html`, not on pull requests). It has no Discord messages.
- The test steps use `continue-on-error: true` so reports still upload. Because of this a shard job is green even when its tests failed (UNVERIFIED by a run: the end message text then reads "tests success").
- Each workflow has its own publish lock: `pages-publish-main` (main suite) and `pages-publish-card` (card checks), so they never queue behind or cancel each other (GitHub keeps one pending job per group). Both copy `.github/pages/index.html`. The main suite replaces only `allure/`, `extent/`, `run-info.txt` and `index.html`; the card suite replaces only `card-checks/` and `index.html`. Each also publishes a copy of `enabledTests.json` (main: root `enabledTests.json`; card: `card-checks/enabledTests.json`) that the landing page lists as "Switched off on purpose"; the page tells card tests by name (D38). The push to `gh-pages` is a retried compare-and-swap (up to 6 attempts): fetch the current `gh-pages`, replace only that workflow's folders, make a fresh parentless commit with `git commit-tree`, push with `--force-with-lease=gh-pages:<fetched sha>`; if the other workflow pushed first, the push is rejected and the next attempt starts from the new state. After 6 failures the step exits 1 and the shard artifacts stay. Same design as the TypeScript project. NOT yet run on GitHub Actions (written 2026-11-24).
- Discord (main suite, secret `DISCORD_WEBHOOK`): a start message per shard, "checks failed", the end message (with a `[Report hub]` link only on non-pull-request runs where no step failed), and "report publish failed". All Discord steps have `continue-on-error: true`.

## 6. Rules for agents

1. **Read `docs/STATUS.md` and `docs/LESSONS.md` at the start of every session** (section 0).
2. **Never guess.** If unsure, search the code, run it, or ask. A command, path, number, version or date must come from something you read or ran.
3. **Run code and tests before claiming something works, and report the real output.** `mvn -q test-compile` and the unit suite are cheap and do not touch Amazon. A live run loads amazon.in: ask the owner first, then run one search term and one TestNG group only.
4. **Verify version-sensitive facts against official docs and cite them.** Selenium, TestNG, surefire, Allure, GitHub Actions syntax and JDK behaviour change. If you did not look it up in this session, mark it UNVERIFIED.
5. **Make focused changes.** Do not touch unrelated files, do not reformat, do not refactor on the side.
6. **Ask before deleting files, changing configs, changing workflows, or anything hard to reverse or outward-facing** (publishing, force-pushing, secrets, notifications). Approval for one thing does not extend to the next.
7. **Never run `git commit` or `git push`** (settings deny them). Make the edit, show the diff or `git status`, and let the owner commit. Read-only git is fine.
8. **Report what a CI run really did.** A green run with skipped tests checked nothing. Always give passed / failed / skipped / broken counts and the skip reasons (skill `assess-ci-run`).
9. **Do not name a cause you did not see.** Say "UNVERIFIED" or "likely", and say what you could not read (GitHub job logs need a login; `gh` is not installed on the owner's machine).
10. **Deferred work goes in `docs/STATUS.md`, Backlog section, with the date.**
11. **Porting to TypeScript:** the TypeScript project is a separate repo and stack. Do not edit it from here (read-only is fine). Write a prompt for its own session in TypeScript terms (skill `port-to-ts-prompt`); never call TS files by Java names, never assume a TS file exists.
12. **At the end of a session:** update `docs/STATUS.md` (done, next step, known issues), add decisions to `docs/DECISIONS.md` and new lessons to `docs/LESSONS.md`, then tell the owner the changes are ready to review and commit.

## 7. Do-not list

- **Never build or tune anything to evade Amazon's bot detection.** The code clicks "Continue shopping", reloads a 503 page once, and otherwise skips with "Blocked by Amazon" (`BlockerHandler`; reloading again and again makes Amazon throttle harder). No stealth settings, no user-agent or fingerprint changes, no proxies, no challenge solving, no human-like input tricks. Known undecided items (STATUS.md): `DriverManager` adds `--disable-blink-features=AutomationControlled` for Chrome (default on, `chrome.disableBlinkAutomationControl`) and `UtilData.properties` has a fixed `userAgent` key. Do not add to them and do not copy them anywhere.
- **Do not run tests in a loop, in big parallel batches, or to collect data.** One search term and one group at a time, and only when the owner approves.
- **Do not count a skip as a pass, and do not report a green workflow as a good result without the skipped count.**
- **Never use a bare `return` for a missing filter;** throw `SkipException` (LESSONS L2).
- **Do not use `${{ github.* }}` inside a workflow `run:` script;** pass it through `env:` (a branch name can contain `$(...)`). The current workflows still do this in their Discord steps: it is a known backlog item, do not copy it into new steps.
- **Do not put secrets or the webhook URL in any file or log.** The webhook is the GitHub secret `DISCORD_WEBHOOK` (the TypeScript project uses `AMAZON_PLAYWRIGHT`).
- **Do not leave "TEMPORARY" branch triggers or temp files in a workflow or on `master`.**
- **Do not publish to `gh-pages` by hand,** and do not let a branch with an old `.github/pages/index.html` publish: both workflows copy that one file when they publish and the last publisher wins. Keep the file identical on every publishing branch.
- **Do not add aliases for Amazon typos or reseller brand names** to make a brand test pass (DECISIONS D5).
- **Do not make the card Discount threshold depend on the option level.** The rule in `CardChecks.DISCOUNT`: a card fits when the discount it prints is at least the option's level minus 5 points, a card that prints none is not judged, the share that must fit is 0.4 (DECISIONS D9). If it flaps, the fallback agreed in the sister project is 0.3; it is not in this code yet. (The older `verifyingDiscountFilterFunctionality` test is a separate, strict test.)
- **Do not decide a filter was applied by comparing the card list before and after a click.** The filter group's own "Clear" link is the signal: shown means applied, gone means cleared (`CardFilterFlows`, `ProductListingPage.clearLinkBy`, DECISIONS D28).
- **Do not rely on `.gitignore` to untrack a file:** a tracked file stays tracked (LESSONS L11).
- **Do not rename packages, remove the cucumber dependencies or edit `pom.xml`, runners or workflows** as a side effect of another task; they are separate, owner-approved changes.
- **Do not use the branch `arch-improvement`,** and do not run `git push --force`, `git reset --hard` or delete branches without being asked.
- **Do not let the README drift.** When you change test counts, workflows, triggers, the Pages layout or the URL, update the README in the same session.

## 8. Glossary

| Term | Meaning |
|---|---|
| Main suite / older tests | `AmazonTests.java` (14 methods, 47 runs): apply a filter, open products, compare their details; workflow `main-suite.yml` |
| Card checks | `CardFilterChecks.java` (10 methods, 50 runs): filters judged on the result cards or product pages; workflow `card-checks.yml` |
| Card | one product tile on the results page (`Card`: title, price, M.R.P., discount, rating, delivery) |
| Option | one value of a filter ("4 Stars & Up", "Samsung", "60% Off or more") |
| Shard | one parallel CI job. Here a shard is a TestNG **group**, not a numbered slice |
| Group | a TestNG group tag: `BatteryProcessorStorage`, `BrandsScreenSizeTypeCamEtc`, `DeliveryPriceOs` (main); `CardSeller`, `CardBrandsReviewsDelivery`, `CardTopBrandsPayOnDelivery`, `CardRatingPriceDiscountSelfCheck` (card; all also tagged `CardChecks`) |
| Runner | a TestNG suite XML in `src/test/runners/`; a pom profile of the same name selects it |
| Data provider | `TestRuns` (older tests) and `SearchTerms` (card checks) in `TestDataProvider`; they read the terms for the test method from `filterCoverage.json` through `TestInputs.FILTER_OF_TEST` |
| Coverage | `filterCoverage.json`: which search terms each filter runs for (97 runs in all) |
| Verdict | `FITS`, `DOES_NOT_FIT` or `NO_VALUE` for one item; `NO_VALUE` is listed, not judged |
| Threshold | the share of judged items that must fit for a check to pass (`CardChecks`) |
| Blocker | a captcha, "Continue shopping" or 503 page; handled by `BlockerHandler` / `CaptchaHandler`: a 503 page is reloaded once, then the test skips with "Blocked by Amazon" |
| Run folder | `run_<timestamp>/` with `reports/`, `logs/`, `screenshots/` (one per run) |
| Collected | JSON written to `collected/` before any card check, so a failed check keeps its data |
| CHECK line | a `CardChecks` log line that states a result in numbers |
| IS_CRON | env var, `true` only on the nightly schedule: full filter depth and all products |
| Landing page | `.github/pages/index.html`, the root `index.html` on `gh-pages`, with a Main suite and a Card checks section |
| run-info | `run-info.txt` (main) and `card-checks/run-info.txt`: the "Run #N - commit - time" line the landing page shows |
| `pages-publish-main` / `pages-publish-card` | the concurrency group of each workflow's publish job (one publish at a time per workflow; the two workflows are kept apart by the retried push) |
| Report hub | the Pages site root, linked in the Discord end message |
| Sister project | the TypeScript/Playwright repo, kept in step by hand through prompts |

## 9. Pointers

- `docs/STATUS.md`: current goal, done, in progress, backlog, known issues. **Read first.**
- `docs/LESSONS.md`: mistakes not to repeat. **Read first.**
- `docs/DECISIONS.md`: why things are the way they are.
- Older notes, not copied here: `docs/CI-Sharding-And-Reporting.md` and `docs/Engineering-Notes.md` (tracked; bugs found and fixed, known issues, repo hygiene, config reference) and the local-only `Assertion-Review.md`, `Cleanup-Change-Log.md`, `Improvement-Checklist.md`, `Improvements-Summary.md`, `Session-Handoff.md` (describes the retired `arch-improvement` branch, not master), `Session-Log-2026-10-22-to-24.md`, `Sibling-Port.md`, `input-handling-backup/`. They may be stale: check against the code. STATUS.md is the one backlog.
- Skills (`.claude/skills/`): `assess-ci-run` (read a published run honestly), `merge-readiness` (check a branch before merging to `master`), `port-to-ts-prompt` (write the prompt for the TypeScript session).
- Subagents (`.claude/agents/`): `code-reviewer` (reads and reports, never edits), `test-runner` (compile, unit suite, and one live check only when named and approved; summarises failures).
- Settings: `.claude/settings.json` (allow safe commands, ask for live runs and risky ones, deny `git commit` and `git push`). No hooks: the repo has no formatter.
- External: Selenium docs (https://www.selenium.dev/documentation/), TestNG docs (https://testng.org/), Maven Surefire (https://maven.apache.org/surefire/maven-surefire-plugin/), GitHub Actions docs (https://docs.github.com/actions), Allure TestNG (https://allurereport.org/docs/testng/).

## 10. Ongoing habit

- **Start of each session:** read CLAUDE.md, `docs/STATUS.md`, `docs/LESSONS.md`; run `git status -sb`.
- **End of each session:** update STATUS.md (done, next exact step, known issues), add decisions to DECISIONS.md and lessons to LESSONS.md, then tell the owner the changes are ready so they can review and commit. Never commit yourself.
