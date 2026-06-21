package HW.pages;

import HW.components.HeaderComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public class NewOrderPage {

    private WebDriver driver;
    private HeaderComponent header;
    private WebDriverWait wait;

    @FindBy(xpath = "//h1[text()='New Order']")
    private WebElement pageTitle;

    @FindBy(id = "btn-submit-order")
    private WebElement submitOrderBtn;

    @FindBy(id = "btn-confirm-order")
    private WebElement confirmOrderBtn;

    @FindBy(id = "category-select")
    private WebElement categoryDropdown;

    @FindBy(id = "products-empty")
    private WebElement emptyProductsMessage;

    @FindBy(id = "product-search")
    private WebElement searchInput;

    @FindBy(id = "price-range-slider")
    private WebElement priceSlider;

    @FindBy(id = "in-stock-toggle")
    private WebElement inStockToggle;

    @FindBy(id = "product-grid")
    private WebElement productGridContainer;

    // All products name
    @FindBy(css = "#product-grid h3")
    private List<WebElement> productsName;

    // All "Add To Order" buttons
    @FindBy(css = "button[id^='select-product-']")
    private List<WebElement> addToOrderButtons;

    @FindBy(tagName = "body")
    private WebElement body;

    @FindBy(id = "validation-error-0")
    private WebElement validationErrorMessage;

    @FindBy(id = "order-total")
    private WebElement orderTotal;

    @FindBy(css = "input[id^='quantity-input-']")
    private List<WebElement> quantityInputs;

    public NewOrderPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        this.header = new HeaderComponent(driver);

        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public HeaderComponent header() {
        return this.header;
    }

    public String getPageTitle() {
        return pageTitle.getText();
    }

    // בחירת קטגוריה מתוך התפריט הנפתח לפי הטקסט (למשל: "Laptops")
    public void selectCategoryByVisibleText(String categoryName) {
        Select dropdown = new Select(categoryDropdown);
        dropdown.selectByVisibleText(categoryName);

        wait.until(ExpectedConditions.visibilityOf(productGridContainer));
    }

    public void searchProduct(String productName) {
        // נוודא ששדה החיפוש גלוי לפני שמקלידים
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.clear();
        searchInput.sendKeys(productName);
    }

    public void toggleInStockOnly() {
        wait.until(ExpectedConditions.elementToBeClickable(inStockToggle));
        inStockToggle.click();
    }

    // --- מתודות לעבודה עם רשימת המוצרים ---

    // קבלת כמות המוצרים המוצגת כרגע
    public int getDisplayedProductsCount() {
        return productsName.size();
    }

    public List<String> getDisplayedProductNames() {
        List<String> names = new ArrayList<>();
        // מעבר על רשימת ה-WebElements של השמות וחילול הטקסט שלהם
        for (WebElement element : productsName) {
            names.add(element.getText());
        }
        return names;
    }

    // לחיצה על "הוסף להזמנה" של המוצר הראשון ברשימה
    public void addFirstProductToOrder() {
        // נוודא שיש לפחות כפתור אחד
        if (!addToOrderButtons.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(addToOrderButtons.get(0)));
            addToOrderButtons.get(0).click();
        } else {
            System.out.println("No products found to add.");
        }
    }

    // פונקציה חכמה: הוספת מוצר לפי השם שלו (עוברת על כל הרשימה)
    public void addProductByName(String name) {
        // לולאה שעוברת על כל שמות המוצרים
        for (int i = 0; i < productsName.size(); i++) {
            if (productsName.get(i).getText().equalsIgnoreCase(name)) {
                // אם מצאנו את השם, נלחץ על הכפתור שנמצא באותו אינדקס (i)
                wait.until(ExpectedConditions.elementToBeClickable(addToOrderButtons.get(i)));
                addToOrderButtons.get(i).click();
                return; // יוצאים מהפונקציה אחרי שמצאנו
            }
        }
        System.out.println("Product not found: " + name);
    }

    public void setPriceSliderValue(int targetPrice) {
        // נוודא שהסליידר אכן נטען ומוצג על המסך
        wait.until(ExpectedConditions.visibilityOf(priceSlider));

        // יצירת אובייקט מסוג JavascriptExecutor מתוך הדרייבר
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 1. משנים את הערך (value) של הסליידר למחיר המבוקש
        // 2. מפעילים אירועי 'input' ו-'change' כדי שקוד ה-React/JS של האתר יזהה שהזזנו את הסליידר ויסנן את המוצרים
        String reactWorkaroundScript =
                "var slider = arguments[0];" +
                        "var value = arguments[1];" +
                        "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "nativeInputValueSetter.call(slider, value);" +
                        "slider.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "slider.dispatchEvent(new Event('change', { bubbles: true }));";

//        js.executeScript(reactWorkaroundScript, priceSlider, targetPrice); // Comment by Tomer!!!!
        js.executeScript(reactWorkaroundScript, priceSlider, Integer.valueOf(targetPrice));
    }

//    public void submitOrder() {
//        addFirstProductToOrder();
//        submitOrderBtn.click();
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        wait.until(ExpectedConditions.visibilityOf(confirmOrderBtn));
//        wait.until(ExpectedConditions.elementToBeClickable(confirmOrderBtn));
//        confirmOrderBtn.click();
//    }

//    public void submitOrder() {
//        wait.until(ExpectedConditions.elementToBeClickable(submitOrderBtn));
//        submitOrderBtn.click();
//
//        wait.until(ExpectedConditions.visibilityOf(confirmOrderBtn));
//        wait.until(ExpectedConditions.elementToBeClickable(confirmOrderBtn));
//        confirmOrderBtn.click();
//    }

    // Function to delay action for display only
    private void pauseForDemo() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(submitOrderBtn));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", submitOrderBtn);

        pauseForDemo();

        js.executeScript("arguments[0].click();", submitOrderBtn);

        wait.until(ExpectedConditions.visibilityOf(confirmOrderBtn));
        wait.until(ExpectedConditions.elementToBeClickable(confirmOrderBtn));

        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", confirmOrderBtn);

        pauseForDemo();

        js.executeScript("arguments[0].click();", confirmOrderBtn);
    }

    // Function to Create new order
    public void createOrder(String categoryName, String productName) {
        pauseForDemo();

        selectCategoryByVisibleText(categoryName);
        pauseForDemo();

        addProductByName(productName);
        pauseForDemo();

        submitOrder();
        pauseForDemo();
    }

    public boolean isValidationErrorDisplayed(String expectedErrorMessage) {
        wait.until(ExpectedConditions.visibilityOf(validationErrorMessage));

        return validationErrorMessage.getText()
                .toLowerCase()
                .contains(expectedErrorMessage.toLowerCase());
    }

    // Use when error message appear
    public void clickSubmitOrderButton() {
        wait.until(ExpectedConditions.elementToBeClickable(submitOrderBtn));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", submitOrderBtn);

        pauseForDemo();

        js.executeScript("arguments[0].click();", submitOrderBtn);
    }

    // return the price on display (what we see on screen)
    public String getOrderTotalText() {
        wait.until(ExpectedConditions.visibilityOf(orderTotal));
        return orderTotal.getText();
    }

    // return the real value that we compare
    public double getOrderTotalValue() {
        String totalText = getOrderTotalText();

        return Double.parseDouble(
                totalText.replace("$", "").replace(",", "").trim()
        );
    }

    public void setFirstOrderItemQuantity(int quantity) {
        if (!quantityInputs.isEmpty()) {
            WebElement quantityInput = quantityInputs.get(0);

            wait.until(ExpectedConditions.visibilityOf(quantityInput));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", quantityInput);

            quantityInput.clear();
            quantityInput.sendKeys(String.valueOf(quantity));

            return;
        }

        System.out.println("No quantity input found in order summary.");
    }

}
