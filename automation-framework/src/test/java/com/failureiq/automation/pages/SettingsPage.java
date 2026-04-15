package com.failureiq.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// Page object for the fake app settings page.
public class SettingsPage extends BasePage {

    private final By settingsPage = By.cssSelector("[data-testid='settings-page']");
    private final By emailNotificationsToggle = By.cssSelector("[data-testid='email-notifications-toggle-input']");
    private final By darkModeToggle = By.cssSelector("[data-testid='dark-mode-toggle-input']");
    private final By autoRefreshToggle = By.cssSelector("[data-testid='auto-refresh-toggle-input']");
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

    public SettingsPage toggleEmailNotifications() {
        click(emailNotificationsToggle);
        return this;
    }

    public SettingsPage toggleAutoRefresh() {
        click(autoRefreshToggle);
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

    public boolean waitForSuccessToastWithin(Duration duration) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, duration);
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public SettingsPage waitForSuccessToastOrThrow(Duration duration) {
        WebDriverWait shortWait = new WebDriverWait(driver, duration);
        shortWait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
        return this;
    }

    public boolean waitForToastToDisappearWithin(Duration duration) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, duration);
            return shortWait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
        } catch (Exception exception) {
            return false;
        }
    }
}
