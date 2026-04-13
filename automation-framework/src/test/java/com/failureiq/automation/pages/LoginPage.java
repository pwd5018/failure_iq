package com.failureiq.automation.pages;

import com.failureiq.automation.config.FrameworkConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Page object for the fake app login page.
public class LoginPage extends BasePage {

    private final By usernameInput = By.cssSelector("[data-testid='username-input']");
    private final By passwordInput = By.cssSelector("[data-testid='password-input']");
    private final By loginButton = By.cssSelector("[data-testid='login-button']");
    private final By loginError = By.cssSelector("[data-testid='login-error']");
    private final By usernameError = By.cssSelector("[data-testid='username-error']");
    private final By passwordError = By.cssSelector("[data-testid='password-error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(FrameworkConfig.getBaseUrl() + "/login");
        return this;
    }

    public LoginPage enterUsername(String username) {
        type(usernameInput, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public void clickLogin() {
        click(loginButton);
    }

    public DashboardPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        return new DashboardPage(driver);
    }

    public String getInvalidLoginMessage() {
        return getText(loginError);
    }

    public String getUsernameRequiredMessage() {
        return getText(usernameError);
    }

    public String getPasswordRequiredMessage() {
        return getText(passwordError);
    }

    public boolean isLoginErrorDisplayed() {
        return isDisplayed(loginError);
    }
}
