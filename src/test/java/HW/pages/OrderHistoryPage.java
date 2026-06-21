package HW.pages;

import HW.components.HeaderComponent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class OrderHistoryPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private HeaderComponent header;

    @FindBy(xpath = "//h1[text()='Order History']")
    private WebElement pageTitle;

    @FindBy(xpath = "//h1[text()='Order History']/following-sibling::p")
    private WebElement ordersCounterText;

    @FindBy(id = "order-history-list")
    private WebElement orderHistoryList;

    @FindBy(xpath = "//div[starts-with(@id,'order-')]")
    private List<WebElement> orders;

    @FindBy(xpath = "//div[starts-with(@id,'order-status-')]")
    private List<WebElement> orderStatuses;

    @FindBy(id = "btn-export-csv")
    private WebElement exportCsvButton;

    @FindBy(xpath = "//p[contains(text(),'No orders yet')]")
    private WebElement noOrdersMessage;

    @FindBy(tagName = "body")
    private WebElement body;

    public OrderHistoryPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);

        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.header = new HeaderComponent(driver);
    }

    public HeaderComponent header() {
        return this.header;
    }

    public String getPageTitle() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return pageTitle.getText();
    }

    public String getOrdersCounterText() {
        wait.until(ExpectedConditions.visibilityOf(ordersCounterText));
        return ordersCounterText.getText();
    }

    public boolean isOrderHistoryListDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(orderHistoryList));
        return orderHistoryList.isDisplayed();
    }

    public int getOrdersCount() {
        wait.until(ExpectedConditions.visibilityOf(orderHistoryList));
        return orders.size();
    }

    public boolean isOrderDisplayed(String productName) {
        wait.until(ExpectedConditions.visibilityOf(body));

        String pageText = body.getText().toLowerCase();
        return pageText.contains(productName.toLowerCase());
    }

    public boolean isStatusDisplayed(String expectedStatus) {
        wait.until(ExpectedConditions.visibilityOf(orderHistoryList));

        for (WebElement status : orderStatuses) {
            if (status.getText().equalsIgnoreCase(expectedStatus)) {
                return true;
            }
        }

        return false;
    }

    public boolean isExportCsvButtonDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(exportCsvButton));
        return exportCsvButton.isDisplayed();
    }

    public boolean isNoOrdersMessageDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(noOrdersMessage));
        return noOrdersMessage.isDisplayed();
    }
}
