package com.failureiq.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Page object for the fake app dashboard page.
public class DashboardPage extends BasePage {

    private final By dashboardPage = By.cssSelector("[data-testid='dashboard-page']");
    private final By welcomeHeading = By.xpath("//h2[contains(text(),'Welcome back, Admin')]");
    private final By usersNavLink = By.cssSelector("[data-testid='nav-users']");
    private final By ordersNavLink = By.cssSelector("[data-testid='nav-orders']");
    private final By settingsNavLink = By.cssSelector("[data-testid='nav-settings']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDashboardLoaded() {
        return isDisplayed(dashboardPage);
    }

    public String getWelcomeMessage() {
        return getText(welcomeHeading);
    }

    public UsersPage goToUsersPage() {
        click(usersNavLink);
        return new UsersPage(driver);
    }

    public OrdersPage goToOrdersPage() {
        click(ordersNavLink);
        return new OrdersPage(driver);
    }

    public SettingsPage goToSettingsPage() {
        click(settingsNavLink);
        return new SettingsPage(driver);
    }
}
