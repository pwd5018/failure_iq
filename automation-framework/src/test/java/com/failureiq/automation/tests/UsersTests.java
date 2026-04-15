package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.DashboardPage;
import com.failureiq.automation.pages.UsersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

// These tests cover the fake app users page.
public class UsersTests extends BaseTest {

    @Test(description = "Searching for a user should narrow the visible table rows")
    public void usersSearchTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        usersPage.searchForUser("Olivia");
        List<String> visibleRows = usersPage.getVisibleUserRowsText();

        Assert.assertTrue(usersPage.isUsersPageLoaded(), "Users page should be loaded.");
        Assert.assertEquals(usersPage.getVisibleUserCount(), 1, "Only one user should match the search.");
        Assert.assertTrue(visibleRows.get(0).contains("Olivia Carter"));
    }

    @Test(description = "Filtering by role should keep only rows for that role")
    public void usersRoleFilterTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        usersPage.filterByRole("Admin");
        List<String> visibleRows = usersPage.getVisibleUserRowsText();

        Assert.assertTrue(visibleRows.size() > 0, "At least one admin row should remain.");
        for (String rowText : visibleRows) {
            Assert.assertTrue(rowText.contains("Admin"), "Each visible row should belong to an Admin user.");
        }
    }

    @Test(description = "Searching by email should return the matching user row")
    public void usersSearchByEmailTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        usersPage.searchForUser("mia.turner@demoapp.test");
        List<String> visibleRows = usersPage.getVisibleUserRowsText();

        Assert.assertEquals(usersPage.getVisibleUserCount(), 1);
        Assert.assertTrue(visibleRows.get(0).contains("Mia Turner"));
    }

    @Test(description = "Filtering by Support should show only support users")
    public void usersRoleFilterSupportTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        usersPage.filterByRole("Support");
        List<String> visibleRows = usersPage.getVisibleUserRowsText();

        Assert.assertEquals(usersPage.getVisibleUserCount(), 2);
        for (String rowText : visibleRows) {
            Assert.assertTrue(rowText.contains("Support"));
        }
    }

    @Test(description = "Confirming delete should remove the selected user row from the table")
    public void deleteUserConfirmRemovesRowTest() {
        loginAsValidUser();
        UsersPage usersPage = new DashboardPage(driver).goToUsersPage();

        Assert.assertTrue(usersPage.isUserRowDisplayed(1), "The user row should exist before deletion.");

        usersPage.openDeleteModalForUser(1);
        Assert.assertTrue(usersPage.getDeleteModalText().contains("Olivia Carter"));
        usersPage.confirmDelete();

        Assert.assertFalse(usersPage.isUserRowDisplayed(1), "The user row should be removed after confirming delete.");
    }
}
