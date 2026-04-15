package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.DashboardPage;
import com.failureiq.automation.pages.LoginPage;
import com.failureiq.automation.pages.OrdersPage;
import com.failureiq.automation.pages.SettingsPage;
import com.failureiq.automation.pages.UsersPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

// These tests intentionally shift their failure pattern based on the selected profile.
// That gives FailureIQ better history for trends, recurring failures, clustering, and dashboards.
public class ProfileDrivenQualityTests extends BaseTest {

    @Test(description = "Timing profile checks whether login finishes inside a strict redirect SLA")
    public void loginRedirectWithinStrictSlaTest() {
        LoginPage loginPage = openLoginPage();
        loginPage.enterUsername("admin")
                .enterPassword("password123")
                .clickLogin();

        if (getScenarioProfile().isTimingStress()) {
            loginPage.waitForDashboardWithin(Duration.ofMillis(450));
        } else {
            DashboardPage dashboardPage = loginPage.waitForDashboardWithin(Duration.ofSeconds(3));
            Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should load inside the normal wait window.");
        }
    }

    @Test(description = "Timing profile checks whether the save toast appears inside a strict UI responsiveness window")
    public void settingsToastAppearsWithinStrictSlaTest() {
        loginAsValidUser();
        SettingsPage settingsPage = new DashboardPage(driver).goToSettingsPage();
        settingsPage.toggleDarkMode().clickSave();

        if (getScenarioProfile().isTimingStress()) {
            settingsPage.waitForSuccessToastOrThrow(Duration.ofMillis(250));
        } else {
            Assert.assertTrue(settingsPage.waitForSuccessToastWithin(Duration.ofSeconds(2)));
        }
    }

    @Test(description = "Timing profile checks whether the save toast disappears inside a very short window")
    public void settingsToastDismissesWithinShortWindowTest() {
        loginAsValidUser();
        SettingsPage settingsPage = new DashboardPage(driver).goToSettingsPage();
        settingsPage.toggleEmailNotifications().clickSave();
        Assert.assertTrue(settingsPage.waitForSuccessToastWithin(Duration.ofSeconds(2)));

        if (getScenarioProfile().isTimingStress()) {
            Assert.assertTrue(
                    settingsPage.waitForToastToDisappearWithin(Duration.ofSeconds(1)),
                    "The timing profile expects the toast to disappear inside a much shorter window."
            );
        } else {
            Assert.assertTrue(settingsPage.waitForToastToDisappearWithin(Duration.ofSeconds(5)));
        }
    }

    @Test(description = "UI regression profile checks whether a legacy users delete locator still works")
    public void legacyUsersDeleteLocatorCompatibilityTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        if (getScenarioProfile().isUiRegression()) {
            driver.findElement(By.cssSelector("[data-test='delete-user-1']")).click();
        } else {
            usersPage.openDeleteModalForUser(1);
            Assert.assertTrue(usersPage.isDeleteModalDisplayed());
        }
    }

    @Test(description = "UI regression profile checks whether a legacy order view locator still works")
    public void legacyOrderViewLocatorCompatibilityTest() {
        loginAsValidUser();
        OrdersPage ordersPage = new DashboardPage(driver).goToOrdersPage();

        if (getScenarioProfile().isUiRegression()) {
            driver.findElement(By.cssSelector("[data-test='view-order-101']")).click();
        } else {
            ordersPage.openOrderDetailsForOrder(101);
            Assert.assertTrue(ordersPage.isOrderDetailsModalDisplayed());
        }
    }

    @Test(description = "UI regression profile checks whether a legacy settings save button locator still works")
    public void legacySettingsSaveButtonLocatorCompatibilityTest() {
        loginAsValidUser();
        SettingsPage settingsPage = new DashboardPage(driver).goToSettingsPage();
        settingsPage.toggleAutoRefresh();

        if (getScenarioProfile().isUiRegression()) {
            driver.findElement(By.cssSelector("[data-test='save-settings-button']")).click();
        } else {
            settingsPage.clickSave();
            Assert.assertTrue(settingsPage.waitForSuccessToastWithin(Duration.ofSeconds(2)));
        }
    }

    @Test(description = "Release candidate profile uses a stricter welcome copy contract")
    public void dashboardWelcomeCopyContractTest() {
        LoginPage loginPage = openLoginPage();
        DashboardPage dashboardPage = loginPage.login("admin", "password123");

        if (getScenarioProfile().isReleaseCandidate()) {
            Assert.assertEquals(
                    dashboardPage.getWelcomeMessage(),
                    "Welcome back, Quality Lead",
                    "Release candidate profile expects the newer welcome copy."
            );
        } else {
            Assert.assertEquals(dashboardPage.getWelcomeMessage(), "Welcome back, Admin");
        }
    }

    @Test(description = "Release candidate profile uses a stricter contract for the number of viewer rows")
    public void viewerDirectoryContractTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();
        usersPage.filterByRole("Viewer");

        if (getScenarioProfile().isReleaseCandidate()) {
            Assert.assertEquals(
                    usersPage.getVisibleUserCount(),
                    3,
                    "Release candidate profile expects one more viewer to be present."
            );
        } else {
            Assert.assertEquals(usersPage.getVisibleUserCount(), 2);
        }
    }

    @Test(description = "Release candidate profile uses a stricter contract for the highest order amount")
    public void highestOrderAmountContractTest() {
        loginAsValidUser();
        OrdersPage ordersPage = new DashboardPage(driver).goToOrdersPage();
        ordersPage.sortByAmount("High to low");

        if (getScenarioProfile().isReleaseCandidate()) {
            Assert.assertEquals(
                    ordersPage.getFirstVisibleAmountText(),
                    "$1999.00",
                    "Release candidate profile expects a different highest-value order."
            );
        } else {
            Assert.assertEquals(ordersPage.getFirstVisibleAmountText(), "$2040.00");
        }
    }
}
