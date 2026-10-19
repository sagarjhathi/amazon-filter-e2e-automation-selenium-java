# Amazon Filter E2E Automation

A fully engineered end to end automation framework designed to validate Amazon product listing filters with precision, reliability, and deep observability. This project demonstrates how a well structured test automation system behaves when it is treated as a software product rather than a simple collection of scripts. Everything from the execution engine to the CI notifications has been built with clarity, scalability, and maintainability in mind.

The framework performs real world validation of Amazon filter flows such as battery capacity, discount ranges, operating system, brand, delivery options, price sliders, display attributes, and more. It searches for a product category, navigates to the listing page, applies filter combinations based on configuration rules, extracts product sets, validates attributes on the product detail pages, and produces a complete visual and textual report. Every step is logged and every important action is captured through screenshots.

This project is designed to feel polished, easy to understand, and impressive to anyone reading its README. The structure below leads the reader through an intuitive narrative that clearly communicates what the project does, how it is engineered, and why it stands out.

---


# Table of Contents

Use this index to quickly navigate the README. Click any entry to jump to that section.

- [Technology Stack](#technology-stack)
- [Key Highlights](#key-highlights)
- [Architecture](#architecture)
- [Execution Flow](#execution-flow)
- [CI and Reporting Intelligence](#ci-and-reporting-intelligence)
- [Discord Notifications](#discord-notifications)
- [Folder Structure](#folder-structure)
- [Running Tests](#running-tests)
- [Engineering Decisions Explained](#engineering-decisions-explained)
- [Extending the Framework](#extending-the-framework)
- [Troubleshooting](#troubleshooting)



# Technology Stack

This framework is built using a rich, modern, and production oriented technology stack. The goal of this section is to clearly showcase every major technology, tool, subsystem, and engineering concept powering the project. This is one of the strongest aspects of the framework, so this version highlights it with the visibility it deserves.

## Programming Language and Ecosystem
- **Java 21** as the core language, providing strong typing, modern syntax, and long term support.
- **Maven** as the build and dependency management system.
- **JUnit-style annotations and TestNG structure** for clean test orchestration.

## Automation and Browser Tooling
- **Selenium 4.27 (W3C-compliant)** for browser automation.
- **WebDriverManager 6.1.0** for automatic driver provisioning, eliminating manual driver downloads.
- **Multi-browser compatibility** built in (Chrome, Firefox, Edge).
- **Customizable browser configurations** including user agents, headless mode, window sizing, GPU control, and sandbox settings.

## Test Execution Engine
- **TestNG 7.10** used for:
  - Parallel test execution (method-level parallelism)
  - Data providers for parameterization
  - Listeners for lifecycle hooks
  - Retry analyzers for flaky test handling

## Data and Input Systems
- **Apache POI 5.4.1** for reading structured Excel files.
- **ExcelReader and ExcelFileReader** for loading Excel test data.
- **ConfigManager** for centralized configuration control.

## Stability, Reliability, and Resilience Systems
- **SafeActions (custom-built reliability abstraction)** providing:
  - Wrapped interactions for click, type, scroll, and element lookup.
  - Intelligent retries for stale or temporarily unavailable elements.
  - Centralized waiting logic.
  - Automatic screenshot capture.
  - Standardized exceptions and logs.
- **WaitUtility** for explicit waits with reusable conditions.
- **GenericUtility** for parsing, URL handling, string normalization, and product detail extraction.
- **Failsafe-inspired retry patterns** built into the workflows.

## Logging and Diagnostics
- **Log4j2** with routing appenders that dynamically create per-run folders.
- **Per-test logs** generated automatically and independent of each other.
- **Timestamped artifact organization** for intuitive debugging.

## Reporting System
- **ExtentReports (Spark HTML)**, one per CI shard, for:
  - Visual dashboards
  - Screenshots embedded per step
  - Linked test logs
  - Sequential and chronological execution views
- **Allure**, merged across all CI shards into a single unified dashboard.

## CI/CD and DevOps Toolchain
- **GitHub Actions** running automated workflows including:
  - Push based runs
  - Pull request validations
  - Scheduled nightly CRON jobs
  - On demand manual dispatches
- **Parallel test sharding** using a matrix build: tests are split into three meaningfully named TestNG groups, each running on its own runner.
- **CI aware configuration toggling** that adjusts execution scale based on the trigger source.
- **Artifact storage** for preserving logs, screenshots, and Allure results.
- **GitHub Pages deployment** of a report hub page linking the merged Allure report and each shard's Extent report.

## Team Communication and Alerting
- **Discord Webhooks** integrated into the CI for:
  - Execution start alerts
  - Execution completion summaries
  - Status highlighting
  - Direct links to reports, logs, and artifacts

## Code Quality and Maintainability
- **Well-structured object oriented design** with separation of flows, pages, actions, utilities, listeners, and managers.
- **Reusable components** across filters and test flows.
- **Consistent naming conventions**, making navigation predictable.
- **Layered abstraction approach** improving adaptability and reducing coupling.

---

# Key Highlights

## Fully Isolated Per Run Artifacts
Every test run generates its own uniquely timestamped folder. Reports, logs and screenshots live side by side inside this folder, which is what lets the Extent report link to them with simple relative paths. This separation ensures predictable debugging and keeps historical artifacts cleanly organized.

```
run_<timestamp>/
   reports/ExtentReport.html
   logs/<testName>.log
   screenshots/<testName>/<step>.png
```

## Configuration Driven Execution
The framework reads from a central properties file to decide how extensively the tests should run. This allows wide flexibility. For example it can be configured to use only a few filter options for quick feedback during code pushes, or it can be configured to exhaustively test all available filters and all products during nightly regressions.

## Smart CI Decision Making
The CI pipeline identifies whether the run originated from a code push or from the nightly CRON job. Based on this, the framework automatically adjusts configuration values. Push based runs stay fast while nightly runs are thorough. This removes manual toggling and keeps the pipeline efficient.

## Real Time Discord Notifications
Whenever CI starts, a clear and informative message is sent to Discord. It includes the triggering user, branch name, commit reference, start time, and a direct link to the run. When execution finishes, another message appears showing the final status, completion time, artifact links, and a button to open the latest hosted report on GitHub Pages. This provides immediate visibility and eliminates the need to hunt through CI logs.

## Parallel CI Shards
Tests are tagged with TestNG groups (`BatteryProcessorStorage`, `BrandsScreenSizeTypeCamEtc`, `DeliveryPriceOs`). CI runs one job per group in parallel, so the full suite finishes in roughly the time of its slowest shard instead of the sum of all tests. Group names describe what is being tested, so anyone can also run a single group locally.

## GitHub Pages Hosted Reports
After all shards finish, a separate publish job gathers their output and deploys a report hub to GitHub Pages: one merged Allure report covering every shard, plus each shard's own interactive Extent report with its screenshots and logs. Anyone can open it in a browser and navigate through test steps. Each deploy replaces the previous history with a single commit, so the Pages branch never grows.

---

# Architecture

```text
Test Layer
   AmazonTests and Data Providers
         |
Flow Layer
   SharedFilterFlows and specialized flows for brand, discount, OS, delivery, and pricing
         |
Page Layer
   AmazonLandingPage and ProductListingPage
         |
SafeActions Layer
   Stable interaction layer improving reliability across all flows
         |
Driver and Utility Layer
   DriverManager, WaitUtility, GenericUtility, ExcelReader
         |
Reporting and Logging Layer
   ExtentReports, Allure and Log4j2 managing artifacts and diagnostics
```

---

# Execution Flow

```text
Trigger (push or CRON)
        |
CI starts one parallel job per test group (3 shards)
        |
   Each shard:
      Read configuration file
      Driver initialization
      Navigate to listing page
      Identify available filters
      Apply filter one by one based on configuration
      Collect updated product list for each filter
      Open product pages and validate attributes
      Capture logs and screenshots for each stage
      Generate Extent Report + Allure results
      Upload both as artifacts
        |
Publish job (runs after all shards, even if some failed)
      Merge all Allure results into one report
      Assemble hub page + 3 Extent reports + merged Allure report
      Deploy to GitHub Pages
        |
Discord webhook publishes notification with links
```

---

# CI and Reporting Intelligence

```text
Push Event
   Uses reduced filter and product count for quick runs

Scheduled CRON Event
   Uses full filter depth and all product validations

Manual CI Dispatch
   Uses developer provided inputs
```

Each run publishes logs, screenshots, one Extent Report per shard, and a single merged Allure report. Everything is deployed to GitHub Pages behind a simple index page for immediate access.

The three shards are defined by TestNG groups on the tests in `AmazonTests.java`:

```text
BatteryProcessorStorage      Storage, Battery, Processor, Discount
BrandsScreenSizeTypeCamEtc   Brands, Display Size, Display Type, Camera
DeliveryPriceOs              Delivery options, Price slider, OS Version
```

Extent reports are kept whole per shard because they link to their logs and screenshots with relative paths. Allure stores attachments by unique ID, so its results from all shards merge safely into one report.

---

# Discord Notifications

```text
Start Notification
   Branch
   Triggered by
   Commit reference
   Start time
   Link to CI run

Completion Notification
   Status
   Duration
   Commit reference
   Links to logs and artifacts
   Button to open latest GitHub Pages report
```

This allows anyone on the team to know test results instantly.

---

# Folder Structure

Below is a broad and expanded folder visualization that clearly illustrates how each layer of the framework connects. This diagram is intentionally detailed so the reader can grasp the complete structure in one view.

```text
project-root/
│
├── eclipse-workspace/Intro/            (Maven module)
│   ├── pom.xml
│   │
│   └── src/
│       ├── main/java/amazonfilterapplicatione2e/
│       │   ├── base/              BaseTest, BasePage
│       │   ├── captcha/           CaptchaHandler
│       │   ├── configManager/     ConfigManager
│       │   ├── constants/         GlobalConstants
│       │   ├── driverManager/     DriverManager
│       │   ├── fileReader/        ExcelReader, ExcelFileReader
│       │   ├── flows/             SharedFilterFlows, BrandFilterFlows,
│       │   │                      PriceSliderFlows, DeliveryFilterFlows,
│       │   │                      OperatingSystemFilterFlows
│       │   ├── logger/            LoggerUtility
│       │   ├── pages/             AmazonLandingPage, ProductListingPage
│       │   ├── pathManager/       PathManager (per-run folder layout)
│       │   ├── reporting/         ReportManager, TestListener, ReportAttachments
│       │   ├── safeActions/       SafeActions
│       │   ├── SeleniumGrid/      GlobalGridUtility
│       │   ├── database/          JDBCConnection
│       │   └── utilities/         GenericUtility, WaitUtility,
│       │                          ScreenshotUtil, ImageCompressor
│       │
│       ├── main/resources/
│       │   ├── configs/           UtilData.properties
│       │   ├── data/              Products.xlsx
│       │   └── log4j2.xml
│       │
│       └── test/
│           ├── java/tests/            AmazonTests (tests tagged with groups)
│           ├── java/retry/            RetryFailedTest, RetryListener
│           ├── testDataProvider/      TestDataProvider
│           └── runners/               AllTestsGlobal.xml (default suite)
│                                      plus older per-area suite files
│
├── .github/workflows/
│     main.yml (matrix-sharded tests, publish job, Discord, Pages)
│
├── LICENSE
├── dependency-map.html
└── README.md
```

Each test run also creates a `run_<timestamp>/` folder at execution time (see Key Highlights); it is generated output and not committed.

This structure emphasizes how every file and module participates in the broader framework. Each folder contains logically grouped responsibilities, making the entire system easy to navigate and easy to extend.

---

# Running Tests

## Quick Smoke Run
Useful during development when fast feedback is preferred.

```bash
mvn clean test -DrunForAllFilterOptions=false -DoverideFilteOptionsCount=2
```

## Full Regression Run
Executes all filter combinations and validates all products.

```bash
mvn clean test -DrunForAllFilterOptions=true -DrunForAllProductsUnderListing=true
```

## Run One Test Group (Shard)
Runs only the tests tagged with a given TestNG group, exactly as a CI shard does. Omit `-Dgroups` to run everything.

```bash
mvn clean test -Dgroups=DeliveryPriceOs
```

Available groups: `BatteryProcessorStorage`, `BrandsScreenSizeTypeCamEtc`, `DeliveryPriceOs`. Every new test should carry a `groups` tag, otherwise CI shards will skip it.

---

# Engineering Decisions Explained

This section highlights the reasoning behind the key architectural and technical decisions made in the project. These explanations give readers, teams, and reviewers insight into how the framework was intentionally shaped to be reliable, scalable, and easy to maintain.

## SafeActions Abstraction
Direct Selenium calls are prone to flakiness, inconsistent behavior, and complex error handling. SafeActions centralizes all interactions with the browser so the framework behaves consistently across pages and flows. It introduces controlled retry logic, timeout management, standardized screenshot capture, and structured logging. This dramatically improves reliability and reduces duplicated logic across tests.

## TestNG Over JUnit
TestNG was selected due to its stronger support for parallel execution, data driven testing, custom listeners, retry analyzers, dependency hierarchies, and flexible suite configurations. These capabilities are essential for a UI automation framework that needs to scale efficiently and report results in a structured manner.

## ExtentReports and Allure Together
Each tool covers a different need. ExtentReports (Spark HTML) gives an instant, self contained dashboard per shard with screenshots and logs embedded, but it links to those files with relative paths, so each shard's report has to stay intact as a unit. Allure stores attachments by unique ID, which makes it safe to merge results from every shard into one combined dashboard. The published site offers both: the merged Allure report for the whole-run picture, and the Extent reports for per-shard debugging.

## Sharding by Meaningful TestNG Groups
Tests are split across CI jobs using TestNG groups rather than hand-maintained method lists in XML files. Group names describe the features they cover, so anyone can tell what a shard contains and run it locally with `-Dgroups=`. Shards are kept roughly balanced in runtime, but meaningful names were chosen over perfectly numbered, time-optimized buckets.

## Log4j2 Routing
Log4j2 was chosen for its performance, flexibility, and routing appenders that allow logs to be automatically directed into per run folders. This enables excellent traceability and prevents log mixing between different runs. Each test receives its own log file, which is then linked directly inside the Extent report.

## CI Mode Switching (Push vs CRON)
Automated tests for pull requests and code pushes need fast feedback. Full regressions are better suited for scheduled executions. The CI mode switching mechanism allows the framework to detect the trigger source and adapt execution depth accordingly. This balances speed, thoroughness, and resource usage without requiring any manual intervention.

## GitHub Pages for Report Hosting
Hosting test reports publicly accessible through GitHub Pages allows team members to view results with a single click from Discord or CI logs. It removes the need to download artifacts and improves visibility. GitHub Pages is stable, fast, and integrates naturally with GitHub Actions. Each deploy is pushed as a single fresh commit, so old report history never accumulates and the branch stays small.

## Discord Notifications
Email notifications are slower and often ignored. Discord provides real time notifications directly where teams communicate. The webhook integration allows the CI to send clear, actionable messages that include links to build logs, artifacts, and the latest hosted report. This increases collaboration and makes the testing system feel alive and responsive.

---

# Extending the Framework
- Add new filters by updating flow classes and page layers
- Add new UI checks by extending GenericUtility and PDP validation methods
- Add new supported browsers by modifying DriverManager
- Add new tests by tagging them with an existing TestNG group (or a new, meaningfully named one, then add it to the matrix in `main.yml`)

---

# Troubleshooting
- Captcha can appear during Amazon navigation. Retrying the test usually resolves it
- If certain report links do not open, confirm that GitHub Pages deployed all screenshots and logs
- If a test never appears in CI results, check that it has a `groups` tag matching one of the matrix shards in `main.yml`
- On Windows, very long screenshot file names can exceed the path limit during git operations; run `git config core.longpaths true`
- SafeActions already mitigates stale element exceptions. Increase wait durations only when necessary

---

# Dependency Map

A visual reference generated directly from the codebase: the actual class-to-class import wiring across every layer, the TestNG lifecycle for a single filter test, and the full CI/CD journey from a git push to the published Extent report.

**[Preview dependency-map.html →](https://htmlpreview.github.io/?https://github.com/sagarjhathi/amazon-filter-e2e-automation/blob/master/dependency-map.html)**

---

# Disclaimer

This project is for learning and portfolio purposes only. It is not affiliated with, endorsed by, or sponsored by Amazon in any way. Amazon's Terms of Service restrict automated access to its site; this framework is intended for occasional, low-volume demonstration runs, not for scraping or continuous/high-frequency execution against the live site.

---

# License

This project is licensed under the [MIT License](LICENSE).

---



