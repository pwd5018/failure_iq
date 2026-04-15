package com.failureiq.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

// Page object for the fake app orders page.
public class OrdersPage extends BasePage {

    private final By ordersPage = By.cssSelector("[data-testid='orders-page']");
    private final By statusFilter = By.cssSelector("[data-testid='orders-status-filter']");
    private final By amountSort = By.cssSelector("[data-testid='orders-amount-sort']");
    private final By orderRows = By.cssSelector("[data-testid^='order-row-']");
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

    public OrdersPage openOrderDetailsForOrder(long orderId) {
        click(By.cssSelector("[data-testid='view-order-" + orderId + "']"));
        return this;
    }

    public OrdersPage openOrderDetailsFromRow(long orderId) {
        click(By.cssSelector("[data-testid='order-row-" + orderId + "']"));
        return this;
    }

    public OrdersPage filterByStatus(String status) {
        Select select = new Select(waitForVisible(statusFilter));
        select.selectByVisibleText(status);
        return this;
    }

    public OrdersPage sortByAmount(String sortLabel) {
        Select select = new Select(waitForVisible(amountSort));
        select.selectByVisibleText(sortLabel);
        return this;
    }

    public boolean isOrderDetailsModalDisplayed() {
        return isDisplayed(detailsModal);
    }

    public String getOrderDetailsText() {
        return getText(detailsContent);
    }

    public List<String> getVisibleOrderRowsText() {
        return waitForAllVisible(orderRows)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public String getFirstVisibleAmountText() {
        return waitForVisible(By.cssSelector("[data-testid^='order-row-'] td:nth-child(4)")).getText();
    }
}
