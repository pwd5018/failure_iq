package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.SettingsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

// These tests cover the fake app settings page.
public class SettingsTests extends BaseTest {

    @Test(description = "Changing a setting and saving should show a success toast")
    public void settingsSaveTest() {
        loginAsValidUser();
        SettingsPage settingsPage = new com.failureiq.automation.pages.DashboardPage(driver).goToSettingsPage();

        Assert.assertFalse(settingsPage.isSaveButtonEnabled(), "Save button should start disabled.");

        settingsPage.toggleDarkMode();
        Assert.assertTrue(settingsPage.isSaveButtonEnabled(), "Save button should become enabled after a change.");

        settingsPage.clickSave();
        Assert.assertTrue(settingsPage.isSuccessToastDisplayed(), "Success toast should appear after saving.");
        Assert.assertEquals(settingsPage.getSuccessToastMessage(), "Settings saved successfully.");
    }
}
