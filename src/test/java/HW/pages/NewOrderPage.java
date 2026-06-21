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

    @FindBy(css = "#product-grid img")
    private List<WebElement> productImages;

    // All products name
    @FindBy(css = "#product-grid h3")
    private List<WebElement> productsName;

    // All "Add To Order" buttons
    @FindBy(css = "button[id^='select-product-']")
    private List<WebElement> addToOrderButtons;

    @FindBy(tagName = "body")
    private WebElement body;

    @FindBy(css = "[id^='validation-error-']")
    private List<WebElement> validationErrorMessages;

    @FindBy(id = "order-total")
    private WebElement orderTotal;

    @FindBy(css = "input[id^='quantity-input-']")
    private List<WebElement> quantityInputs;

    @FindBy(css = "[id^='stock-']")
    private List<WebElement> stockElements;

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

    /**
     * מחזירה רשימה של כל כתובות ה-URL (src) של התמונות המוצגות כרגע בגריד
     */
    public List<String> getDisplayedProductImageUrls() {
        // ממתינים שהתמונות יופיעו כדי לא לקרוא מערך ריק
        wait.until(ExpectedConditions.visibilityOfAllElements(productImages));

        List<String> urls = new ArrayList<>();
        for (WebElement img : productImages) {
            urls.add(img.getAttribute("src"));
        }
        return urls;
    }

    public List<String> getDisplayedProductNames() {
        List<String> names = new ArrayList<>();
        // מעבר על רשימת ה-WebElements של השמות וחילול הטקסט שלהם
        for (WebElement element : productsName) {
            names.add(element.getText());
        }
        return names;
    }

    public boolean isProductAddButtonDisabled(String name) {
        wait.until(ExpectedConditions.visibilityOf(productGridContainer));
        for (int i = 0; i < productsName.size(); i++) {
            if (productsName.get(i).getText().equalsIgnoreCase(name)) {
                WebElement button = addToOrderButtons.get(i);
                // סלניום בודק אוטומטית אם קיים המאפיין disabled ב-HTML
                return !button.isEnabled() || button.getAttribute("disabled") != null;
            }
        }
        throw new RuntimeException("Product not found in grid: " + name);
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
        wait.until(ExpectedConditions.visibilityOfAllElements(validationErrorMessages));

        for (WebElement errorMessage : validationErrorMessages) {
            String actualMessage = errorMessage.getText();

            if (actualMessage.toLowerCase().contains(expectedErrorMessage.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * מחזירה את המלאי הנוכחי של מוצר מסוים לפי השם שלו
     */
    public int getProductStock(String productName) {
        wait.until(ExpectedConditions.visibilityOf(productGridContainer));

        for (int i = 0; i < productsName.size(); i++) {
            if (productsName.get(i).getText().equalsIgnoreCase(productName)) {

                String stockText = stockElements.get(i).getText();

                if (stockText.equalsIgnoreCase("Out of stock")) {
                    return 0;
                }

                String onlyNumbers = stockText.replaceAll("[^0-9]", "");
                return Integer.parseInt(onlyNumbers);
            }
        }
        throw new RuntimeException("Product not found to check stock: " + productName);
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
            WebElement quantityInput = quantityInputs.getFirst();

            wait.until(ExpectedConditions.visibilityOf(quantityInput));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", quantityInput);

            quantityInput.clear();
            quantityInput.sendKeys(String.valueOf(quantity));

            return;
        }

        System.out.println("No quantity input found in order summary.");
    }

    public void setOrderItemQuantity(int index, int quantity) {
        if (quantityInputs.size() > index) {
            WebElement quantityInput = quantityInputs.get(index);

            wait.until(ExpectedConditions.visibilityOf(quantityInput));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", quantityInput);

            quantityInput.clear();
            quantityInput.sendKeys(String.valueOf(quantity));
        } else {
            System.out.println("Quantity input at index " + index + " was not found.");
        }
    }

    /**
     * לוחצת על כפתור המחיקה (פח אשפה) של מוצר ספציפי ב-Order Summary
     */
    public void removeProductFromSummary(String productName) {
        String xpath = String.format("//p[text()='%s']/ancestor::div[starts-with(@id, 'order-item-')]//button[starts-with(@id, 'remove-item-')]", productName);
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        removeButton.click();
    }

    /**
     * בודקת האם מוצר מסוים מופיע כרגע בטופס ה-Order Summary
     */
    public boolean isProductInSummary(String productName) {
        String xpath = String.format("//div[starts-with(@id, 'order-item-')]//p[text()='%s']", productName);
        // משתמשים ב-findElements כדי שלא יזרוק שגיאה אם האלמנט לא קיים (מה שאנחנו מצפים שיקרה אחרי מחיקה)
        return !driver.findElements(By.xpath(xpath)).isEmpty();
    }
}
