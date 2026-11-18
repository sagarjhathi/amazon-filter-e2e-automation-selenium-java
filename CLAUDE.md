# CLAUDE.md

Stable project memory. Everything here was read from the repo; items marked UNVERIFIED were not confirmed by running them.

## Project
Selenium + TestNG framework that checks the product-list filters of amazon.in (brands, battery, storage, discount, price, delivery, OS, card/seller/rating checks, ...) against live Amazon data.
Each test searches a term, applies each filter option, opens products or reads cards, and compares what the page shows with what the filter promised.
Portfolio/learning project (README Disclaimer): low-volume demo runs, not continuous scraping.

## Tech stack
- Java 21 (`maven.compiler.release=21`; CI runs JDK 24 Temurin), Maven, TestNG 7.10.2, surefire 3.0.0-M5
- Selenium 4.40.0 (+ devtools-v144), WebDriverManager 6.1.0, Gson 2.10.1, Apache POI 5.4.1, Failsafe 3.3.2, thumbnailator
- Reporting: ExtentReports 5.1.2 (Spark, per run/shard) + Allure 2.29.0 (merged across shards); logging Log4j2 2.20.0 (per-test log files)
- CI: GitHub Actions on `windows-latest`, GitHub Pages (`gh-pages` branch) for reports, Discord webhook notifications
- `cucumber-java` / `cucumber-testng` are in pom.xml but unused (confirmed by the user; not removed yet)

## Where the code lives
Real project root: `eclipse-workspace/Intro/` (Maven module, `pom.xml` there). Repo root only has README, LICENSE, `dependency-map.html`, `.github/`, `docs/`.
Because `<sourceDirectory>` is `src`, Java packages are `main.java.amazonfilterapplicatione2e.*` (main code) and `test.java.*` / `test.testDataProvider` (tests). This is intentional-looking but odd; keep it.

```
eclipse-workspace/Intro/src/
  main/java/amazonfilterapplicatione2e/
    base/            BaseTest (suite/run folder, driver per test, log context), BasePage
    pages/           AmazonLandingPage, ProductListingPage (locators and small page actions)
    flows/           SharedFilterFlows, Brand/Delivery/OperatingSystem/PriceSlider flows, CardFilterFlows, ProductPageFlows, Card
    safeActions/     SafeActions (retrying click/find wrappers)
    captcha/         BlockerHandler (captcha / "Continue shopping" / 503 -> skip), CaptchaHandler
    utilities/       GenericUtility, WaitUtility, FilterMatcher, BrandMatcher, CardChecks (judging rules), screenshot/image helpers
    driverManager/   DriverManager (chrome/firefox/edge, ThreadLocal driver, optional grid)
    configManager/   ConfigManager (system property -> env var -> UtilData.properties)
    fileReader/      TestInputs (coverage JSON loader), ExcelReader, FileReader
    reporting/       ReportManager (Extent), TestListenerUpdated, Attachers (logs/screenshots into reports)
    pathManager/     PathManager (run_<timestamp>/reports|logs|screenshots layout)
    logger/ constants/ SeleniumGrid/ database/   small/unused-looking helpers
  main/resources/    configs/UtilData.properties, log4j2.xml, data/{searchTerms,filterCoverage,filterHeadings}.json, data/Products.xlsx
  test/java/tests/   AmazonTests (14 older filter tests), CardFilterChecks (10 card/product-page check kinds x search terms)
  test/java/unit/    FilterMatcherTest, CoverageConsistencyTest, CardChecksTest (no browser)
  test/java/retry/   RetryFailedTest, RetryListener (retry once, not on AssertionError)
  test/testDataProvider/TestDataProvider.java   "TestRuns" and "ExcelData" providers
  test/runners/      TestNG suite XMLs: AllTestsGlobal (default), CardFilterChecks, UnitTests, LocalExecution (brands only), one per group (BatteryProcessorStorage, BrandsScreenSizeTypeCamEtc, DeliveryPriceOs; pom profiles of the same names)
.github/workflows/   main-suite.yml, card-checks.yml     .github/pages/index.html (Pages landing page)
```
`docs/` is in `.gitignore` on purpose (decided by the user: STATUS.md and DECISIONS.md stay local). Only `CI-Sharding-And-Reporting.md` and `Engineering-Notes.md` are tracked there. Generated and ignored: `run_*`, `target/`, `collected/`, `logs/`, `screenshots/`.

## Architecture
Top to bottom; a layer may call only the layer(s) below it:
1. **Tests** (`AmazonTests`, `CardFilterChecks`): decide what to open/read/compare and assert. Search terms come from the `TestRuns` / `SearchTerms` data providers.
2. **Flows** (`flows/`): multi-step procedures (apply option, read cards, open product tabs). Read and act; tests do the asserting.
3. **Pages** (`pages/`): locators and single-page actions.
4. **SafeActions**: retry/wait wrappers around Selenium calls.
5. **Driver / utilities**: DriverManager, WaitUtility, GenericUtility, matchers (`FilterMatcher`, `BrandMatcher`, `CardChecks`), ConfigManager.
6. **Reporting / logging**: Extent, Allure, Log4j2, via listener and ThreadContext (`testName`).

Rules (from code comments and README):
- `FilterMatcher`, `BrandMatcher`, `CardChecks` touch no browser: keep them pure so they stay unit-testable.
- Flows must not swallow `SkipException`: blocked pages are skipped by `BlockerHandler`; keep catches narrow.
- A filter/option that Amazon does not show must `throw new SkipException(reason)`, never `return` (TestNG counts a bare return as a pass).
- Search terms per test come from `data/filterCoverage.json`; every new test method must be added to `TestInputs.FILTER_OF_TEST` (enforced by `CoverageConsistencyTest`).
- Tests must not call the WebDriver directly (no `driver.`, `findElement`, `By.` in test bodies); go through flows/pages/SafeActions. Checked: AmazonTests has no such calls, only unused `WebDriver`/`By` imports. Locators live in pages, Selenium calls in SafeActions/flows.
- Every new main-suite test needs a `groups` tag matching a CI matrix shard, else CI skips it (README Troubleshooting).

## How to run
Run from `eclipse-workspace/Intro/`. Needs a real browser and live amazon.in. None of these were run by me (UNVERIFIED unless stated).
- Compile: `mvn clean compile` / `mvn -q test-compile` (used by CI)
- Unit tests, no browser (what CI `check` job runs): `mvn -q test "-Dsurefire.suiteXmlFiles=src/test/runners/UnitTests.xml"`
- All older tests: `mvn clean test` (pom default suite = `AllTestsGlobal.xml`, `groups` property empty)
- One older shard/group: `mvn test -Dgroups=DeliveryPriceOs` (others: `BatteryProcessorStorage`, `BrandsScreenSizeTypeCamEtc`)
- Smoke size: `mvn clean test -DrunForAllFilterOptions=false -DoverideFilteOptionsCount=2` (README)
- Card checks: `mvn test "-Dsurefire.suiteXmlFiles=src/test/runners/CardFilterChecks.xml"`; one shard `"-Dgroups=CardSeller"` (also `CardBrandsReviewsDelivery`, `CardTopBrandsPayOnDelivery`, `CardRatingPriceDiscountSelfCheck`; `CardChecks` = all); one term `"-DcardChecks.terms=Headphones"`
- Single method: `mvn test -Dtest=...` is UNVERIFIED (pom pins `suiteXmlFiles`, which may override `-Dtest`). `LocalExecution.xml` runs only the brands test.
- Reports: Extent at `run_<timestamp>/reports/ExtentReport.html` (logs and screenshots next to it); Allure raw results in `target/allure-results`; CI publishes both to GitHub Pages `https://sagarjhathi.github.io/amazon-filter-e2e-automation/` and uploads artifact `allure-report-merged`.
- Config overrides: any key in `UtilData.properties` can be set by `-Dkey=value` or env var `KEY_UPPER` (e.g. `chrome.headless` -> `CHROME_HEADLESS`); `-DUtilData.file=<path>` swaps the whole file.
- CI triggers: `main-suite.yml` = push/PR to master, manual, nightly cron `50 17 * * *` UTC; `card-checks.yml` = push/PR to master and manual only (no cron). Both set `IS_CRON`.
- Branch `arch-improvement` is old and must not be used for anything (user decision).

## Conventions
- Code style (from the user's saved preferences): simple, clear code; short plain-language comments; keep replaced code as `// OLD:` comments (this pattern is used throughout the repo). Never commit unless asked.
- Naming: tests `verifying<Filter>FilterFunctionality` (older) and `<thing>FilterCheckedOn<Cards|TheProductPages>` (card checks); page locators are `By` fields ending in `By`.
- New test: add method with `groups`, add its name to `TestInputs.FILTER_OF_TEST`, add its filter + search terms to `filterCoverage.json`, run `UnitTests.xml`.
- Waits: explicit waits via WaitUtility/SafeActions (`safeActionsWaitTime`, `globalWaitTimeExtendingBasePage`); `Thread.sleep` only via `BasePage.pause` for waiting on Amazon.
- Retries: `RetryListener` retries a test once on non-assertion errors; on in both `AllTestsGlobal.xml` and `CardFilterChecks.xml`.
- Result rules (README "How Results Are Decided"): missing filter = skip with reason; filter present but nothing validated = fail; products with no captured details are not mismatches; soft asserts inside loops, hard asserts for preconditions.
- Logging: Log4j2 via `LoggerUtility.getLogger(...)`, lines prefixed `[testName]`. ~97 `System.out` calls still exist in the code.
- Test data: JSON under `src/main/resources/data/`; limits (how many options/products) via `UtilData.properties` keys and `ConfigManager.getLimit`. CI cron is detected with env `IS_CRON` (set in both workflows as `github.event_name == 'schedule'`).

## Session protocol
1. At the start of every session, read `CLAUDE.md`, `docs/STATUS.md` and `docs/DECISIONS.md`, then reply with a 5-line summary: what the project is, what is done, what is in progress, what is next, and any blockers. Wait for my instruction before changing code.
2. During the session, if we make a non-obvious technical decision, note it for DECISIONS.md.
3. At the end of the session, or whenever I say "wrap up", update `docs/STATUS.md` and `docs/DECISIONS.md` as described in those files, and show me the diff.
4. Never claim something is done unless it was actually run or verified. If not verified, mark it "unverified".
