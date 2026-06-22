package HW.pages;

import HW.components.HeaderComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class OrderHistoryPage {

    private static final Logger logger = LogManager.getLogger(OrderHistoryPage.class);

    private final WebDriverWait wait;
    private final HeaderComponent header;

    // ==================== WEB ELEMENTS ====================

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

    // ==================== CONSTRUCTOR ====================

    public OrderHistoryPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.header = new HeaderComponent(driver);
    }

    // ==================== COMPONENTS ====================

    public HeaderComponent header() {
        return this.header;
    }

    // ==================== ACTIONS & INTERACTIONS ====================

    public void clickExportToCsv() {
        logger.debug("Clicking on Export to CSV button");
        wait.until(ExpectedConditions.elementToBeClickable(exportCsvButton));
        exportCsvButton.click();
    }

    // ==================== GETTERS & VALIDATIONS ====================

    public String getPageTitle() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        String title = pageTitle.getText();
        logger.debug("Retrieved page title: '{}'", title);
        return title;
    }

    public String getOrdersCounterText() {
        wait.until(ExpectedConditions.visibilityOf(ordersCounterText));
        String counterText = ordersCounterText.getText();
        logger.debug("Retrieved orders counter text: '{}'", counterText);
        return counterText;
    }

    public boolean isOrderHistoryListDisplayed() {
        logger.debug("Checking if order history list is displayed");
        wait.until(ExpectedConditions.visibilityOf(orderHistoryList));
        return orderHistoryList.isDisplayed();
    }

    public int getOrdersCount() {
        wait.until(ExpectedConditions.visibilityOf(orderHistoryList));
        int count = orders.size();
        logger.debug("Retrieved orders count: {}", count);
        return count;
    }

    public boolean isOrderDisplayed(String productName) {
        logger.debug("Checking if product '{}' is displayed in order history", productName);
        wait.until(ExpectedConditions.visibilityOf(body));
        String pageText = body.getText().toLowerCase();
        return pageText.contains(productName.toLowerCase());
    }

    public boolean isStatusDisplayed(String expectedStatus) {
        logger.debug("Checking if status '{}' is displayed in order history", expectedStatus);
        wait.until(ExpectedConditions.visibilityOf(orderHistoryList));

        for (WebElement status : orderStatuses) {
            if (status.getText().equalsIgnoreCase(expectedStatus)) {
                return true;
            }
        }
        return false;
    }

    public boolean isExportCsvButtonDisplayed() {
        logger.debug("Checking if Export CSV button is displayed");
        wait.until(ExpectedConditions.visibilityOf(exportCsvButton));
        return exportCsvButton.isDisplayed();
    }

    public boolean isNoOrdersMessageDisplayed() {
        logger.debug("Checking if 'No orders yet' message is displayed");
        wait.until(ExpectedConditions.visibilityOf(noOrdersMessage));
        return noOrdersMessage.isDisplayed();
    }
}