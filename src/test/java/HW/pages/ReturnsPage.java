package HW.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

public class ReturnsPage {

    private WebDriver driver;

    @FindBy(id = "return-quantity-input")
    private WebElement quantityInput;

    @FindBy(id = "submit-return-button")
    private WebElement submitButton;

    @FindBy(id = "return-product-select")
    private WebElement productDropdown;

    public ReturnsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    /**
     * בדיקה האם תיבת בחירת המוצרים קיימת על המסך
     */
    public boolean isProductSelectAvailable() {
        try {
            return productDropdown.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    /**
     * בוחר מוצר להחזרה לפי השם שלו בתיבת הבחירה
     */
    public void selectProductToReturn(String productName) {
        Select dropdown = new Select(productDropdown);
        dropdown.selectByVisibleText(productName);
    }

    /**
     * מחזירה את כמות המוצרים הזמינים בתיבת הבחירה
     */
    public int getProductCount() {
        Select dropdown = new Select(productDropdown);
        // getOptions מחזירה רשימה (List) של כל האופציות בתיבה
        return dropdown.getOptions().size();
    }

    /**
     * מחזירה רשימה של שמות כל המוצרים הקיימים בתיבת הבחירה
     */
    public List<String> getAllProductNames() {
        Select dropdown = new Select(productDropdown);
        List<String> productNames = new ArrayList<>();

        // עוברים בלולאה על כל האופציות ושולפים את הטקסט שלהן
        for (WebElement option : dropdown.getOptions()) {
            // מסננים את האופציה הראשונה אם היא טקסט ברירת מחדל כמו "-- Select a product --"
            if (!option.getText().contains("--")) {
                productNames.add(option.getText());
            }
        }
        return productNames;
    }

    /**
     * בדיקה האם יש הודעת שגיאה (כמו "Exceeds ordered quantity")
     * שימושית מאוד לבדיקות שליליות
     */
    public boolean isToastMessageDisplayed(String messagePart) {
        // כאן ניתן להוסיף לוגיקה שמחפשת הודעות שגיאה שקופצות ב-DOM
        return driver.getPageSource().contains(messagePart);
    }

    public void performReturn(String productName, int quantity) {
        selectProductToReturn(productName);
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(quantity));
        submitButton.click();
    }
}