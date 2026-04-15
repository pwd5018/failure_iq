package com.failureiq.automation.base;

import com.failureiq.automation.config.FrameworkConfig;
import com.failureiq.automation.config.ScenarioProfile;
import com.failureiq.automation.pages.LoginPage;
import com.failureiq.automation.utils.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

// BaseTest creates and closes the browser for each test method.
// This keeps tests isolated and easier to understand.
public class BaseTest {

    protected WebDriver driver;

    @BeforeMethod
    @Parameters("scenario.profile")
    public void setUp(@Optional("") String scenarioProfile) {
        if (scenarioProfile != null && !scenarioProfile.isBlank()) {
            FrameworkConfig.setTestNgScenarioProfileOverride(scenarioProfile);
        }

        driver = DriverFactory.createDriver();
        driver.get(FrameworkConfig.getBaseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected LoginPage openLoginPage() {
        return new LoginPage(driver).open();
    }

    protected void loginAsValidUser() {
        openLoginPage().login("admin", "password123");
    }

    protected ScenarioProfile getScenarioProfile() {
        return FrameworkConfig.getScenarioProfile();
    }

    public WebDriver getDriver() {
        return driver;
    }
}
