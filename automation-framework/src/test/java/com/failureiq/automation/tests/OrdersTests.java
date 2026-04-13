package com.failureiq.automation.tests;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.pages.OrdersPage;
import org.testng.Assert;
import org.testng.annotations.Test;

// These tests cover the fake app orders page.
public class OrdersTests extends BaseTest {

    @Test(description = "Opening an order should show the order details modal")
    public void ordersDetailsModalTest() {
        loginAsValidUser();
        OrdersPage ordersPage = new com.failureiq.automation.pages.DashboardPage(driver).goToOrdersPage();

        ordersPage.openFirstOrderDetails();

        Assert.assertTrue(ordersPage.isOrderDetailsModalDisplayed(), "Order details modal should be visible.");
        Assert.assertTrue(ordersPage.getOrderDetailsText().contains("Order Number"), "Modal content should contain order details.");
    }
}
