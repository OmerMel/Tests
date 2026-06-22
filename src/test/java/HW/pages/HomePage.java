package HW.pages;

import HW.components.HeaderComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HomePage {

    private static final Logger logger = LogManager.getLogger(HomePage.class);

    private final HeaderComponent header;

    // ==================== WEB ELEMENTS ====================

    // System Overview stats
    @FindBy(xpath = "//div[@data-testid='stat-total-products']//span[contains(@class, 'text-2xl')]")
    private WebElement statTotalProductsValue;

    @FindBy(xpath = "//div[@data-testid='stat-orders-processed']//span[contains(@class, 'text-2xl')]")
    private WebElement statOrdersProcessedValue;

    // ==================== CONSTRUCTOR ====================

    public HomePage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.header = new HeaderComponent(driver);
    }

    // ==================== COMPONENTS ====================

    public HeaderComponent header() {
        return this.header;
    }

    // ==================== GETTERS & VALIDATIONS ====================

    public String getTotalProductsCount() {
        String count = statTotalProductsValue.getText();
        logger.debug("Retrieved total products count: {}", count);
        return count;
    }

    public String getOrdersProcessedCount() {
        String count = statOrdersProcessedValue.getText();
        logger.debug("Retrieved orders processed count: {}", count);
        return count;
    }
}