package com.failureiq.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

// Page object for the fake app orders page.
public class OrdersPage extends BasePage {

    private final By ordersPage = By.cssSelector("[data-testid='orders-page']");
    private final By firstViewButton = By.cssSelector("[data-testid='view-order-101']");
    private final By detailsModal = By.cssSelector("[data-testid='order-details-modal']");
    private final By detailsContent = By.cssSelector("[data-testid='order-details-content']");

    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOrdersPageLoaded() {
        return isDisplayed(ordersPage);
    }

    public OrdersPage openFirstOrderDetails() {
        click(firstViewButton);
        return this;
    }

    public boolean isOrderDetailsModalDisplayed() {
        return isDisplayed(detailsModal);
    }

    public String getOrderDetailsText() {
        return getText(detailsContent);
    }
}
