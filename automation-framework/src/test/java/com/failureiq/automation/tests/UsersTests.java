package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

// These tests cover the fake app users page.
public class UsersTests extends BaseTest {

    @Test(description = "Searching for a user should narrow the visible table rows")
    public void usersSearchTest() {
        loginAsValidUser();
        UsersPage usersPage = new com.failureiq.automation.pages.DashboardPage(driver).goToUsersPage();

        usersPage.searchForUser("Olivia");
        List<String> visibleRows = usersPage.getVisibleUserRowsText();

        Assert.assertTrue(usersPage.isUsersPageLoaded(), "Users page should be loaded.");
        Assert.assertEquals(usersPage.getVisibleUserCount(), 1, "Only one user should match the search.");
        Assert.assertTrue(visibleRows.get(0).contains("Olivia Carter"));
    }

    @Test(description = "Filtering by role should keep only rows for that role")
    public void usersRoleFilterTest() {
        loginAsValidUser();
        UsersPage usersPage = new com.failureiq.automation.pages.DashboardPage(driver).goToUsersPage();

        usersPage.filterByRole("Admin");
        List<String> visibleRows = usersPage.getVisibleUserRowsText();

        Assert.assertTrue(visibleRows.size() > 0, "At least one admin row should remain.");
        for (String rowText : visibleRows) {
            Assert.assertTrue(rowText.contains("Admin"), "Each visible row should belong to an Admin user.");
        }
    }

    @Test(description = "Opening the delete user modal and canceling should close the modal")
    public void deleteUserModalOpenCancelTest() {
        loginAsValidUser();
        UsersPage usersPage = new com.failureiq.automation.pages.DashboardPage(driver).goToUsersPage();

        usersPage.openDeleteModalForFirstUser();
        Assert.assertTrue(usersPage.isDeleteModalDisplayed(), "Delete confirmation modal should open.");

        usersPage.cancelDelete();
        Assert.assertFalse(usersPage.isDeleteModalDisplayed(), "Delete confirmation modal should close after cancel.");
    }
}
