package HW.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ReturnsPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "return-product-select")
    private WebElement productSelectDropdown;

    @FindBy(id = "return-quantity-input")
    private WebElement quantityInput;

    @FindBy(id = "btn-submit-return")
    private WebElement submitReturnButton;

    @FindBy(css = ".destructive .font-semibold")
    private WebElement toastErrorMessage;

    public ReturnsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    /**
     * בוחר מוצר מהדרופדאון על בסיס חיפוש חלקי של השם (כי יש תוספת של כמות בטקסט)
     */
    public void selectProductToReturn(String productName) {
        wait.until(ExpectedConditions.visibilityOf(productSelectDropdown));
        Select select = new Select(productSelectDropdown);

        for (WebElement option : select.getOptions()) {
            if (option.getText().contains(productName)) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }
        throw new RuntimeException("Product '" + productName + "' was not found in the returns dropdown.");
    }

    public void setReturnQuantity(int quantity) {
        wait.until(ExpectedConditions.visibilityOf(quantityInput));
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(quantity));
    }

    public void submitReturn() {
        wait.until(ExpectedConditions.elementToBeClickable(submitReturnButton));
        submitReturnButton.click();
    }

    /**
     * בודק אם מוצר מופיע כרגע ברשימת האפשרויות להחזרה
     */
    public boolean isProductAvailableForReturn(String productName) {
        wait.until(ExpectedConditions.visibilityOf(productSelectDropdown));
        Select select = new Select(productSelectDropdown);

        for (WebElement option : select.getOptions()) {
            String optionText = option.getText();

            if (optionText.contains(productName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * קוראת את הטקסט מהדרופדאון (למשל "Apple Airpods (ordered: 2)")
     * ומחלצת רק את המספר שבתוך הסוגריים.
     */
    public int getRemainingQuantityFromDropdown(String productName) {
        wait.until(ExpectedConditions.visibilityOf(productSelectDropdown));
        Select select = new Select(productSelectDropdown);

        for (WebElement option : select.getOptions()) {
            String text = option.getText();
            if (text.contains(productName)) {
                // חותכים את הטקסט כדי למצוא את המספר שאחרי המילה "ordered: "
                int startIndex = text.indexOf("ordered: ") + 9;
                int endIndex = text.indexOf(")", startIndex);
                String numberStr = text.substring(startIndex, endIndex).trim();

                return Integer.parseInt(numberStr);
            }
        }
        throw new RuntimeException("Product '" + productName + "' was not found in the dropdown to check quantity.");
    }

    public String getErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(toastErrorMessage));
        return toastErrorMessage.getText();
    }
}