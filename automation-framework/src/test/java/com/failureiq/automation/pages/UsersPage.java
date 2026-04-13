package com.failureiq.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

// Page object for the fake app users page.
public class UsersPage extends BasePage {

    private final By usersPage = By.cssSelector("[data-testid='users-page']");
    private final By searchInput = By.cssSelector("[data-testid='users-search-input']");
    private final By roleFilter = By.cssSelector("[data-testid='users-role-filter']");
    private final By userRows = By.cssSelector("[data-testid^='user-row-']");
    private final By emptyState = By.cssSelector("[data-testid='users-empty-state']");
    private final By deleteFirstAdminButton = By.cssSelector("[data-testid='delete-user-1']");
    private final By deleteModal = By.cssSelector("[data-testid='delete-user-modal']");
    private final By cancelDeleteButton = By.cssSelector("[data-testid='cancel-delete-button']");

    public UsersPage(WebDriver driver) {
        super(driver);
    }

    public boolean isUsersPageLoaded() {
        return isDisplayed(usersPage);
    }

    public UsersPage searchForUser(String text) {
        type(searchInput, text);
        return this;
    }

    public UsersPage filterByRole(String role) {
        Select select = new Select(waitForVisible(roleFilter));
        select.selectByVisibleText(role);
        return this;
    }

    public List<String> getVisibleUserRowsText() {
        return waitForAllVisible(userRows)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public int getVisibleUserCount() {
        return driver.findElements(userRows).size();
    }

    public boolean isEmptyStateDisplayed() {
        return isDisplayed(emptyState);
    }

    public UsersPage openDeleteModalForFirstUser() {
        click(deleteFirstAdminButton);
        return this;
    }

    public boolean isDeleteModalDisplayed() {
        return isDisplayed(deleteModal);
    }

    public UsersPage cancelDelete() {
        click(cancelDeleteButton);
        return this;
    }
}
