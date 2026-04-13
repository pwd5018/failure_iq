package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.DashboardPage;
import com.failureiq.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

// These tests cover the login behavior of the fake app.
public class LoginTests extends BaseTest {

    @Test(description = "Valid credentials should allow the user to reach the dashboard")
    public void validLoginTest() {
        LoginPage loginPage = openLoginPage();
        DashboardPage dashboardPage = loginPage.login("admin", "password123");

        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be visible after login.");
        Assert.assertEquals(dashboardPage.getWelcomeMessage(), "Welcome back, Admin");
    }

    @Test(description = "Invalid credentials should show an error message")
    public void invalidLoginTest() {
        LoginPage loginPage = openLoginPage();
        loginPage.login("admin", "wrong-password");

        Assert.assertTrue(loginPage.isLoginErrorDisplayed(), "Invalid login message should be shown.");
        Assert.assertEquals(loginPage.getInvalidLoginMessage(), "Invalid username or password.");
    }
}
