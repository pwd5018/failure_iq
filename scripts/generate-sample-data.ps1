param(
    [string]$OutputDirectory = "sample-data"
)

$ErrorActionPreference = "Stop"

# This script generates fake Selenium/TestNG-style run payloads
# that match the backend POST /api/test-runs request format.

$random = [System.Random]::new(20260412)

$testCatalog = @(
    "LoginTest.shouldLoginWithValidCredentials",
    "LoginTest.shouldShowErrorForInvalidPassword",
    "CheckoutTest.shouldAddItemToCart",
    "CheckoutTest.shouldCompleteOrder",
    "ProfileTest.shouldUpdateUserDetails",
    "OrdersTest.shouldFilterOrdersByStatus",
    "UsersTest.shouldDeleteInactiveUser",
    "UsersTest.shouldSearchByEmail",
    "SettingsTest.shouldSaveNotificationPreferences",
    "DashboardTest.shouldRenderSummaryCards",
    "ReportsTest.shouldExportCsv",
    "NavigationTest.shouldOpenOrdersPage"
)

$failureTemplates = @(
    @{
        Type = "TimeoutException"
        Message = "org.openqa.selenium.TimeoutException: Expected condition failed: waiting for visibility of element located by By.cssSelector: .loading-spinner"
    },
    @{
        Type = "NoSuchElementException"
        Message = 'org.openqa.selenium.NoSuchElementException: Unable to locate element: {"method":"css selector","selector":"[data-testid=''submit-order-button'']"}'
    },
    @{
        Type = "StaleElementReferenceException"
        Message = "org.openqa.selenium.StaleElementReferenceException: stale element reference: element is not attached to the page document"
    },
    @{
        Type = "AssertionError"
        Message = "java.lang.AssertionError: expected [Order placed successfully] but found [Order failed]"
    },
    @{
        Type = "ElementClickInterceptedException"
        Message = "org.openqa.selenium.ElementClickInterceptedException: element click intercepted by overlapping modal backdrop"
    }
)

$runDefinitions = @(
    @{
        FileName = "run-001-nightly-smoke.json"
        RunName = "Nightly Smoke Run - 2026-04-12"
        TriggeredBy = "GitHub Actions"
        PassedCount = 9
        FailedCount = 2
        SkippedCount = 1
    },
    @{
        FileName = "run-002-regression.json"
        RunName = "Regression Run - Build 248"
        TriggeredBy = "Jenkins"
        PassedCount = 7
        FailedCount = 4
        SkippedCount = 1
    },
    @{
        FileName = "run-003-hotfix-validation.json"
        RunName = "Hotfix Validation - Checkout"
        TriggeredBy = "Azure DevOps"
        PassedCount = 10
        FailedCount = 1
        SkippedCount = 1
    },
    @{
        FileName = "run-004-cross-browser.json"
        RunName = "Cross Browser Run - Chrome"
        TriggeredBy = "Local QA Machine"
        PassedCount = 6
        FailedCount = 5
        SkippedCount = 1
    },
    @{
        FileName = "run-005-release-candidate.json"
        RunName = "Release Candidate Validation"
        TriggeredBy = "TeamCity"
        PassedCount = 8
        FailedCount = 3
        SkippedCount = 1
    }
)

function Get-RandomDuration {
    $value = 0.5 + ($random.NextDouble() * 5.5)
    return [Math]::Round($value, 2)
}

function New-PassedResult($testName) {
    return [ordered]@{
        testName = $testName
        status = "PASSED"
        errorMessage = $null
        durationSeconds = Get-RandomDuration
    }
}

function New-FailedResult($testName, $failureTemplate) {
    return [ordered]@{
        testName = $testName
        status = "FAILED"
        errorMessage = $failureTemplate.Message
        durationSeconds = Get-RandomDuration
    }
}

function New-SkippedResult($testName) {
    return [ordered]@{
        testName = $testName
        status = "SKIPPED"
        errorMessage = "org.testng.SkipException: Test skipped because a dependent setup step failed"
        durationSeconds = [Math]::Round((0.1 + ($random.NextDouble() * 0.7)), 2)
    }
}

function New-RunPayload($definition) {
    $shuffledTests = $testCatalog | Sort-Object { $random.Next() }
    $results = New-Object System.Collections.ArrayList
    $testIndex = 0
    $failureOffset = $random.Next(0, $failureTemplates.Count)

    for ($i = 0; $i -lt $definition.PassedCount; $i++) {
        [void]$results.Add((New-PassedResult $shuffledTests[$testIndex]))
        $testIndex++
    }

    for ($i = 0; $i -lt $definition.FailedCount; $i++) {
        $failureTemplate = $failureTemplates[($failureOffset + $i) % $failureTemplates.Count]
        [void]$results.Add((New-FailedResult $shuffledTests[$testIndex] $failureTemplate))
        $testIndex++
    }

    for ($i = 0; $i -lt $definition.SkippedCount; $i++) {
        [void]$results.Add((New-SkippedResult $shuffledTests[$testIndex]))
        $testIndex++
    }

    $results = $results | Sort-Object { $random.Next() }

    return [ordered]@{
        runName = $definition.RunName
        triggeredBy = $definition.TriggeredBy
        testCaseResults = $results
    }
}

if (-not (Test-Path $OutputDirectory)) {
    New-Item -ItemType Directory -Path $OutputDirectory | Out-Null
}

foreach ($definition in $runDefinitions) {
    $payload = New-RunPayload $definition
    $json = $payload | ConvertTo-Json -Depth 5
    $filePath = Join-Path $OutputDirectory $definition.FileName
    Set-Content -Path $filePath -Value $json -Encoding UTF8
    Write-Host "Generated $filePath"
}
