package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.config.FrameworkConfig;
import com.failureiq.automation.pages.DashboardPage;
import com.failureiq.automation.pages.LoginPage;
import com.failureiq.automation.pages.UsersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

// These tests fail on purpose so the framework produces mixed results.
// That is useful later when building reporting features.
public class FailureDemoTests extends BaseTest {

    @Test(description = "Intentional failure: wrong dashboard welcome message")
    public void wrongExpectedWelcomeMessageTest() {
        LoginPage loginPage = openLoginPage();
        DashboardPage dashboardPage = loginPage.login("admin", "password123");

        Assert.assertEquals(
                dashboardPage.getWelcomeMessage(),
                "Welcome back, Super Admin",
                "This assertion is intentionally wrong."
        );
    }

    @Test(description = "Intentional failure: assert a user exists when it should not")
    public void nonExistingUserAssertionTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        usersPage.searchForUser("Non Existing Person");
        List<String> visibleRows = usersPage.isEmptyStateDisplayed()
                ? List.of()
                : usersPage.getVisibleUserRowsText();

        Assert.assertTrue(
                visibleRows.stream().anyMatch(row -> row.contains("Non Existing Person")),
                "This assertion is intentionally wrong because the user does not exist."
        );
    }

    @Test(description = "Intentional timing failure: use a very short wait after clicking login")
    public void shortWaitTimingFailureTest() {
        LoginPage loginPage = openLoginPage();
        loginPage.enterUsername("admin")
                .enterPassword("password123")
                .clickLogin();

        WebDriverWait shortWait = new WebDriverWait(
                driver,
                Duration.ofSeconds(FrameworkConfig.getShortTimeoutSeconds())
        );

        shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Welcome back, Admin')]")
        ));

        Assert.assertTrue(true, "This line is unlikely to be reached if the short wait is too small.");
    }
}
