package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.DashboardPage;
import com.failureiq.automation.pages.OrdersPage;
import com.failureiq.automation.pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

// These tests cover the stable dashboard behaviors that should usually stay green.
public class DashboardTests extends BaseTest {

    @Test(description = "Dashboard metric cards should be visible after a successful login")
    public void dashboardMetricCardsVisibleTest() {
        loginAsValidUser();
        DashboardPage dashboardPage = new DashboardPage(driver);

        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be visible.");
        Assert.assertTrue(dashboardPage.areMetricCardsVisible(), "All summary metric cards should be visible.");
        Assert.assertEquals(dashboardPage.getVisibleMetricCardCount(), 3, "Three metric cards should be shown.");
    }

    @Test(description = "Dashboard activity panel should provide stable text for basic smoke coverage")
    public void dashboardActivityPanelTextTest() {
        loginAsValidUser();
        DashboardPage dashboardPage = new DashboardPage(driver);

        Assert.assertTrue(
                dashboardPage.getActivityPanelText().contains("stable place to assert content"),
                "Activity panel text should stay stable for UI automation."
        );
    }

    @Test(description = "Dashboard navigation should open the users page")
    public void navigationMenuOpensUsersPageTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        Assert.assertTrue(usersPage.isUsersPageLoaded(), "Users page should open from the dashboard navigation.");
    }
}
