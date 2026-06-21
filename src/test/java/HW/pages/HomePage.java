package HW.pages;

import HW.components.HeaderComponent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HomePage {

    private WebDriver driver;
    private HeaderComponent header;

    // כותרת ההודעה הראשית
    @FindBy(xpath = "//section[@data-testid='welcome-section']//h1")
    private WebElement welcomeHeading;

    // כרטיסיות הניווט המהיר (Quick Navigation)
    @FindBy(id = "nav-card-new-order")
    private WebElement quickNavNewOrderCard;

    @FindBy(id = "nav-card-order-history")
    private WebElement quickNavOrderHistoryCard;

    @FindBy(id = "nav-card-returns")
    private WebElement quickNavReturnsCard;

    // נתוני מערכת (System Overview)
    @FindBy(xpath = "//div[@data-testid='stat-total-products']//span[contains(@class, 'text-2xl')]")
    private WebElement statTotalProductsValue;

    @FindBy(xpath = "//div[@data-testid='stat-orders-processed']//span[contains(@class, 'text-2xl')]")
    private WebElement statOrdersProcessedValue;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);

        header = new HeaderComponent(driver);
    }

    // To be able to access header component methods from test classes
    public HeaderComponent header() {
        return this.header;
    }

    public String getWelcomeMessageText() {
        return welcomeHeading.getText();
    }

    public void clickQuickNavNewOrder() {
        quickNavNewOrderCard.click();
    }

    public void clickQuickNavOrderHistory() {
        quickNavOrderHistoryCard.click();
    }

    public void clickQuickNavReturns() {
        quickNavReturnsCard.click();
    }

    public String getTotalProductsCount() {
        return statTotalProductsValue.getText();
    }

    public String getOrdersProcessedCount() {
        return statOrdersProcessedValue.getText();
    }
}
