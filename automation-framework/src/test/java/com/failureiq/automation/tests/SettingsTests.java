package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.DashboardPage;
import com.failureiq.automation.pages.SettingsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

// These tests cover the fake app settings page.
public class SettingsTests extends BaseTest {

    @Test(description = "Changing a setting and saving should show a success toast")
    public void settingsSaveTest() {
        loginAsValidUser();
        SettingsPage settingsPage = new DashboardPage(driver).goToSettingsPage();

        Assert.assertFalse(settingsPage.isSaveButtonEnabled(), "Save button should start disabled.");

        settingsPage.toggleDarkMode();
        Assert.assertTrue(settingsPage.isSaveButtonEnabled(), "Save button should become enabled after a change.");

        settingsPage.clickSave();
        Assert.assertTrue(settingsPage.isSuccessToastDisplayed(), "Success toast should appear after saving.");
        Assert.assertEquals(settingsPage.getSuccessToastMessage(), "Settings saved successfully.");
    }

    @Test(description = "Save button should start disabled before any setting is changed")
    public void saveButtonStartsDisabledTest() {
        loginAsValidUser();
        SettingsPage settingsPage = new DashboardPage(driver).goToSettingsPage();

        Assert.assertFalse(settingsPage.isSaveButtonEnabled());
    }

    @Test(description = "The success toast should disappear after the delayed save flow completes")
    public void settingsToastEventuallyDisappearsTest() {
        loginAsValidUser();
        SettingsPage settingsPage = new DashboardPage(driver).goToSettingsPage();

        settingsPage.toggleAutoRefresh().clickSave();
        Assert.assertTrue(settingsPage.waitForSuccessToastWithin(Duration.ofSeconds(2)));
        Assert.assertTrue(settingsPage.waitForToastToDisappearWithin(Duration.ofSeconds(5)));
    }
}
