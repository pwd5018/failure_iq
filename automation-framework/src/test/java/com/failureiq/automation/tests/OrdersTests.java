package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.DashboardPage;
import com.failureiq.automation.pages.OrdersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

// These tests cover the fake app orders page.
public class OrdersTests extends BaseTest {

    @Test(description = "Opening an order should show the order details modal")
    public void ordersDetailsModalTest() {
        loginAsValidUser();
        OrdersPage ordersPage = new DashboardPage(driver).goToOrdersPage();

        ordersPage.openFirstOrderDetails();

        Assert.assertTrue(ordersPage.isOrderDetailsModalDisplayed(), "Order details modal should be visible.");
        Assert.assertTrue(ordersPage.getOrderDetailsText().contains("Order Number"), "Modal content should contain order details.");
    }

    @Test(description = "Filtering orders by Delivered should leave only delivered rows visible")
    public void ordersStatusFilterDeliveredTest() {
        loginAsValidUser();
        OrdersPage ordersPage = new DashboardPage(driver).goToOrdersPage();

        ordersPage.filterByStatus("Delivered");
        List<String> rows = ordersPage.getVisibleOrderRowsText();

        Assert.assertTrue(rows.size() > 0, "Delivered orders should be present.");
        for (String rowText : rows) {
            Assert.assertTrue(rowText.contains("Delivered"));
        }
    }

    @Test(description = "Sorting orders high to low should place the highest amount first")
    public void ordersSortHighToLowTest() {
        loginAsValidUser();
        OrdersPage ordersPage = new DashboardPage(driver).goToOrdersPage();

        ordersPage.sortByAmount("High to low");
        Assert.assertEquals(ordersPage.getFirstVisibleAmountText(), "$2040.00");
    }

    @Test(description = "Clicking an order row should also open the order details modal")
    public void orderRowClickOpensDetailsTest() {
        loginAsValidUser();
        OrdersPage ordersPage = new DashboardPage(driver).goToOrdersPage();

        ordersPage.openOrderDetailsFromRow(103);

        Assert.assertTrue(ordersPage.isOrderDetailsModalDisplayed());
        Assert.assertTrue(ordersPage.getOrderDetailsText().contains("ORD-1003"));
    }
}
