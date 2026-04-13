package com.failureiq.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Page object for the fake app settings page.
public class SettingsPage extends BasePage {

    private final By settingsPage = By.cssSelector("[data-testid='settings-page']");
    private final By darkModeToggle = By.cssSelector("[data-testid='dark-mode-toggle-input']");
    private final By saveSettingsButton = By.cssSelector("[data-testid='save-settings-button']");
    private final By successToast = By.cssSelector("[data-testid='settings-success-toast']");

    public SettingsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isSettingsPageLoaded() {
        return isDisplayed(settingsPage);
    }

    public boolean isSaveButtonEnabled() {
        return waitForVisible(saveSettingsButton).isEnabled();
    }

    public SettingsPage toggleDarkMode() {
        click(darkModeToggle);
        return this;
    }

    public SettingsPage clickSave() {
        click(saveSettingsButton);
        return this;
    }

    public boolean isSuccessToastDisplayed() {
        return isDisplayed(successToast);
    }

    public String getSuccessToastMessage() {
        return getText(successToast);
    }
}
