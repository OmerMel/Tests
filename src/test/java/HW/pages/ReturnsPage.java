package HW.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ReturnsPage {

    private static final Logger logger = LogManager.getLogger(ReturnsPage.class);

    private final WebDriverWait wait;

    // ==================== WEB ELEMENTS ====================

    @FindBy(id = "return-product-select")
    private WebElement productSelectDropdown;

    @FindBy(id = "return-quantity-input")
    private WebElement quantityInput;

    @FindBy(id = "btn-submit-return")
    private WebElement submitReturnButton;

    @FindBy(css = ".destructive .font-semibold")
    private WebElement toastErrorMessage;

    // ==================== CONSTRUCTOR ====================

    public ReturnsPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    // ==================== ACTIONS & INTERACTIONS ====================

    /**
     * Selects a product from the dropdown based on a partial name match
     * (since the text includes the quantity).
     */
    public void selectProductToReturn(String productName) {
        logger.debug("Selecting product to return: '{}'", productName);
        wait.until(ExpectedConditions.visibilityOf(productSelectDropdown));
        Select select = new Select(productSelectDropdown);

        for (WebElement option : select.getOptions()) {
            if (option.getText().contains(productName)) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }

        String errorMsg = "Product '" + productName + "' was not found in the returns dropdown.";
        logger.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }

    public void setReturnQuantity(int quantity) {
        logger.debug("Setting return quantity to: {}", quantity);
        wait.until(ExpectedConditions.visibilityOf(quantityInput));
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(quantity));
    }

    public void submitReturn() {
        logger.debug("Clicking submit return button");
        wait.until(ExpectedConditions.elementToBeClickable(submitReturnButton));
        submitReturnButton.click();
    }

    // ==================== GETTERS & VALIDATIONS ====================

    /**
     * Checks if a product is currently available in the return options list.
     */
    public boolean isProductAvailableForReturn(String productName) {
        logger.debug("Checking if product '{}' is available for return", productName);
        wait.until(ExpectedConditions.visibilityOf(productSelectDropdown));
        Select select = new Select(productSelectDropdown);

        for (WebElement option : select.getOptions()) {
            if (option.getText().contains(productName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the ordered quantity number from the dropdown text
     * (e.g., "Apple Airpods (ordered: 2)").
     */
    public int getRemainingQuantityFromDropdown(String productName) {
        logger.debug("Extracting remaining quantity for product: '{}'", productName);
        wait.until(ExpectedConditions.visibilityOf(productSelectDropdown));
        Select select = new Select(productSelectDropdown);

        for (WebElement option : select.getOptions()) {
            String text = option.getText();
            if (text.contains(productName)) {
                // Substring to find the number after "ordered: "
                int startIndex = text.indexOf("ordered: ") + 9;
                int endIndex = text.indexOf(")", startIndex);
                String numberStr = text.substring(startIndex, endIndex).trim();

                int quantity = Integer.parseInt(numberStr);
                logger.debug("Found remaining quantity: {}", quantity);
                return quantity;
            }
        }

        String errorMsg = "Product '" + productName + "' was not found in the dropdown to check quantity.";
        logger.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }

    public String getErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(toastErrorMessage));
        String error = toastErrorMessage.getText();
        logger.debug("Retrieved toast error message: '{}'", error);
        return error;
    }
}