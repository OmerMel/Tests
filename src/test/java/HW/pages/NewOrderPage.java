package HW.pages;

import HW.components.HeaderComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(NewOrderPage.class);

    private final WebDriver driver;
    private final HeaderComponent header;
    private final WebDriverWait wait;

    // ==================== WEB ELEMENTS ====================

    @FindBy(xpath = "//h1[text()='New Order']")
    private WebElement pageTitle;

    @FindBy(id = "btn-submit-order")
    private WebElement submitOrderBtn;

    @FindBy(id = "btn-confirm-order")
    private WebElement confirmOrderBtn;

    @FindBy(id = "category-select")
    private WebElement categoryDropdown;

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

    @FindBy(css = "#product-grid h3")
    private List<WebElement> productsName;

    @FindBy(css = "button[id^='select-product-']")
    private List<WebElement> addToOrderButtons;

    @FindBy(css = "[id^='validation-error-']")
    private List<WebElement> validationErrorMessages;

    @FindBy(id = "order-total")
    private WebElement orderTotal;

    @FindBy(css = "input[id^='quantity-input-']")
    private List<WebElement> quantityInputs;

    @FindBy(css = "[id^='stock-']")
    private List<WebElement> stockElements;

    // ==================== CONSTRUCTOR ====================

    public NewOrderPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        this.header = new HeaderComponent(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public HeaderComponent header() {
        return this.header;
    }

    // ==================== ACTIONS & INTERACTIONS ====================

    public void selectCategoryByVisibleText(String categoryName) {
        logger.debug("Selecting category: {}", categoryName);
        Select dropdown = new Select(categoryDropdown);
        dropdown.selectByVisibleText(categoryName);
        wait.until(ExpectedConditions.visibilityOf(productGridContainer));
    }

    public void searchProduct(String productName) {
        logger.debug("Searching for product: {}", productName);
        wait.until(ExpectedConditions.visibilityOf(searchInput));
        searchInput.clear();
        searchInput.sendKeys(productName);
    }

    public void toggleInStockOnly() {
        logger.debug("Toggling 'In Stock Only' filter");
        wait.until(ExpectedConditions.elementToBeClickable(inStockToggle));
        inStockToggle.click();
    }

    public void addFirstProductToOrder() {
        if (!addToOrderButtons.isEmpty()) {
            logger.debug("Adding the first available product to order");
            wait.until(ExpectedConditions.elementToBeClickable(addToOrderButtons.get(0)));
            addToOrderButtons.get(0).click();
        } else {
            logger.warn("Attempted to add the first product, but no products were found.");
        }
    }

    public void addProductByName(String name) {
        logger.debug("Looking for product '{}' to add to order", name);
        for (int i = 0; i < productsName.size(); i++) {
            if (productsName.get(i).getText().equalsIgnoreCase(name)) {
                wait.until(ExpectedConditions.elementToBeClickable(addToOrderButtons.get(i)));
                addToOrderButtons.get(i).click();
                return;
            }
        }
        logger.warn("Product not found in grid: {}", name);
    }

    public void setPriceSliderValue(int targetPrice) {
        logger.debug("Setting price slider value to: {}", targetPrice);
        wait.until(ExpectedConditions.visibilityOf(priceSlider));

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Simulate React slider input and change events
        String reactWorkaroundScript =
                "var slider = arguments[0];" +
                        "var value = arguments[1];" +
                        "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "nativeInputValueSetter.call(slider, value);" +
                        "slider.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "slider.dispatchEvent(new Event('change', { bubbles: true }));";

        js.executeScript(reactWorkaroundScript, priceSlider, Integer.valueOf(targetPrice));
    }

    public void submitOrder() {
        logger.debug("Clicking submit order button");
        wait.until(ExpectedConditions.elementToBeClickable(submitOrderBtn));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", submitOrderBtn);
        pauseForDemo();
        js.executeScript("arguments[0].click();", submitOrderBtn);

        logger.debug("Clicking confirm order button");
        wait.until(ExpectedConditions.visibilityOf(confirmOrderBtn));
        wait.until(ExpectedConditions.elementToBeClickable(confirmOrderBtn));

        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", confirmOrderBtn);
        pauseForDemo();
        js.executeScript("arguments[0].click();", confirmOrderBtn);
    }

    public void clickSubmitOrderButton() {
        logger.debug("Clicking submit order button (without confirmation step)");
        wait.until(ExpectedConditions.elementToBeClickable(submitOrderBtn));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", submitOrderBtn);
        pauseForDemo();
        js.executeScript("arguments[0].click();", submitOrderBtn);
    }

    public void createOrder(String categoryName, String productName) {
        logger.info("Creating a full order flow for category: '{}', product: '{}'", categoryName, productName);
        pauseForDemo();
        selectCategoryByVisibleText(categoryName);
        pauseForDemo();
        addProductByName(productName);
        pauseForDemo();
        submitOrder();
        pauseForDemo();
    }

    public void setFirstOrderItemQuantity(int quantity) {
        if (!quantityInputs.isEmpty()) {
            logger.debug("Setting first order item quantity to: {}", quantity);
            WebElement quantityInput = quantityInputs.getFirst();
            wait.until(ExpectedConditions.visibilityOf(quantityInput));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", quantityInput);

            quantityInput.clear();
            quantityInput.sendKeys(String.valueOf(quantity));
        } else {
            logger.warn("No quantity input found in order summary.");
        }
    }

    public void setOrderItemQuantity(int index, int quantity) {
        if (quantityInputs.size() > index) {
            logger.debug("Setting order item at index {} quantity to: {}", index, quantity);
            WebElement quantityInput = quantityInputs.get(index);
            wait.until(ExpectedConditions.visibilityOf(quantityInput));

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", quantityInput);

            quantityInput.clear();
            quantityInput.sendKeys(String.valueOf(quantity));
        } else {
            logger.warn("Quantity input at index {} was not found.", index);
        }
    }

    /**
     * Clicks the remove (trash bin) button for a specific product in the Order Summary
     */
    public void removeProductFromSummary(String productName) {
        logger.debug("Removing product '{}' from order summary", productName);
        String xpath = String.format("//p[text()='%s']/ancestor::div[starts-with(@id, 'order-item-')]//button[starts-with(@id, 'remove-item-')]", productName);
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        removeButton.click();
    }

    // ==================== GETTERS & VALIDATIONS ====================

    public int getDisplayedProductsCount() {
        return productsName.size();
    }

    public List<String> getDisplayedProductImageUrls() {
        // Wait for images to load to prevent returning an empty list
        wait.until(ExpectedConditions.visibilityOfAllElements(productImages));
        List<String> urls = new ArrayList<>();
        for (WebElement img : productImages) {
            urls.add(img.getAttribute("src"));
        }
        return urls;
    }

    public List<String> getDisplayedProductNames() {
        List<String> names = new ArrayList<>();
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
                return !button.isEnabled() || button.getAttribute("disabled") != null;
            }
        }
        throw new RuntimeException("Product not found in grid: " + name);
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
     * Returns the current stock quantity for a specific product by name
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

    public String getOrderTotalText() {
        wait.until(ExpectedConditions.visibilityOf(orderTotal));
        return orderTotal.getText();
    }

    public double getOrderTotalValue() {
        String totalText = getOrderTotalText();
        return Double.parseDouble(totalText.replace("$", "").replace(",", "").trim());
    }

    /**
     * Checks if a specific product is currently displayed in the Order Summary form
     */
    public boolean isProductInSummary(String productName) {
        String xpath = String.format("//div[starts-with(@id, 'order-item-')]//p[text()='%s']", productName);
        // Using findElements to avoid throwing an exception if the element does not exist (expected after removal)
        return !driver.findElements(By.xpath(xpath)).isEmpty();
    }

    // ==================== HELPER METHODS ====================

    private void pauseForDemo() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}