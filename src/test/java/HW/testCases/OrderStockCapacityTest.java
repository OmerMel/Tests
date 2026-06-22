package HW.testCases;

import HW.pages.NewOrderPage;
import HW.pages.OrderHistoryPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderStockCapacityTest {

    private static final Logger logger = LogManager.getLogger(OrderStockCapacityTest.class);

    private WebDriver driver;
    private NewOrderPage newOrderPage;
    private OrderHistoryPage orderHistoryPage;
    private JSONObject allStockCapacityData;

    @Before
    public void setUp() {
        logger.info("Starting OrderStockCapacityTest setup.");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/stock_capacity_data.json");
            allStockCapacityData = (JSONObject) jsonParser.parse(reader);
            logger.info("Successfully loaded stock_capacity_data.json.");
        } catch (Exception e) {
            logger.error("Failed to load stock_capacity_data.json.", e);
        }
    }

    // ==================== TESTS ====================

    @Test
    public void testOrderWithinStockCapacity() {
        logger.info("Starting test: Order items within available stock capacity.");

        JSONArray testDataArray = (JSONArray) allStockCapacityData.get("withinStockTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            JSONArray items = (JSONArray) testData.get("items");
            String expectedStatus = (String) testData.get("expectedStatus");

            executeOrderWithinStockCapacity(items, expectedStatus);
        }
        logger.info("Finished test: Order within stock capacity.");
    }

    @Test
    public void testOrderExceedsStockCapacity() {
        logger.info("Starting test: Order items exceeding available stock capacity.");

        JSONArray testDataArray = (JSONArray) allStockCapacityData.get("exceedsStockTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            JSONArray items = (JSONArray) testData.get("items");
            String expectedErrorMessage = (String) testData.get("expectedErrorMessage");

            executeOrderExceedsStockCapacity(items, expectedErrorMessage);
        }
        logger.info("Finished test: Order exceeding stock capacity.");
    }

    @Test
    public void testOutOfStockButtonDisabled() {
        logger.info("Starting test: Verify 'Add to Order' button is disabled when stock is depleted.");

        JSONArray testDataArray = (JSONArray) allStockCapacityData.get("outOfStockTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            String categoryName = (String) testData.get("category");
            String productName = (String) testData.get("productName");
            int stockQuantity = ((Number) testData.get("stockQuantity")).intValue();

            executeOutOfStockButtonDisabled(categoryName, productName, stockQuantity);
        }
        logger.info("Finished test: 'Add to Order' button disabled.");
    }

    // ==================== HELPER METHODS ====================

    private void executeOrderWithinStockCapacity(JSONArray items, String expectedStatus) {
        List<String> productNames = new ArrayList<>();

        // Maps to store data for stock verification at the end of the test
        Map<String, Integer> initialStocks = new HashMap<>();
        Map<String, Integer> orderedQuantities = new HashMap<>();
        Map<String, String> productCategories = new HashMap<>();

        try {
            logger.debug("Opening New Order page.");
            driver.get("https://nano-flow-order-direct.base44.app/order");
            newOrderPage = new NewOrderPage(driver);

            for (int i = 0; i < items.size(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                String categoryName = (String) item.get("category");
                String productName = (String) item.get("productName");
                int quantity = ((Number) item.get("quantity")).intValue();

                productNames.add(productName);
                orderedQuantities.put(productName, quantity);
                productCategories.put(productName, categoryName);

                logger.debug("Selecting category: {}", categoryName);
                newOrderPage.selectCategoryByVisibleText(categoryName);
                pauseForDemo();

                // Read and store current stock before adding to cart
                int stock = newOrderPage.getProductStock(productName);
                initialStocks.put(productName, stock);
                logger.debug("Initial stock for '{}': {}", productName, stock);

                logger.debug("Adding product to order: {}", productName);
                newOrderPage.addProductByName(productName);
                pauseForDemo();

                logger.debug("Changing product quantity to: {}", quantity);
                newOrderPage.setOrderItemQuantity(i, quantity);
            }

            logger.debug("Submitting order.");
            newOrderPage.submitOrder();
            pauseForDemo();

            logger.debug("Navigating to Order History using Header.");
            newOrderPage.header().clickOrderHistory();
            orderHistoryPage = new OrderHistoryPage(driver);

            logger.debug("Verifying Order History page.");
            assertEquals("Order History", orderHistoryPage.getPageTitle());

            for (String productName : productNames) {
                logger.debug("Verifying ordered product appears in history: {}", productName);
                assertTrue("The ordered product was not found in order history: " + productName,
                        orderHistoryPage.isOrderDisplayed(productName));
            }

            logger.debug("Verifying order status is displayed: {}", expectedStatus);
            assertTrue("Expected order status was not displayed: " + expectedStatus,
                    orderHistoryPage.isStatusDisplayed(expectedStatus));

            // Navigate back to the Order Page and verify stock decreased
            logger.debug("Returning to Order Page to verify stock updates.");
            newOrderPage.header().clickNewOrder();
            pauseForDemo();

            for (String productName : productNames) {
                String category = productCategories.get(productName);

                logger.debug("Selecting category '{}' to check new stock for '{}'", category, productName);
                newOrderPage.selectCategoryByVisibleText(category);
                pauseForDemo();

                int newStock = newOrderPage.getProductStock(productName);
                int expectedStock = initialStocks.get(productName) - orderedQuantities.get(productName);

                logger.debug("Verifying stock for '{}'. Expected: {}, Actual: {}", productName, expectedStock, newStock);
                assertEquals("Stock did not decrease correctly for " + productName, expectedStock, newStock);
            }

            logger.info("Success: Within stock capacity and stock update verification passed.");
            pauseForDemo();

        } catch (AssertionError e) {
            logger.error("Validation failed for products {}: {}", productNames, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing products {}: {}", productNames, e.getMessage(), e);
            throw e;
        }
    }

    private void executeOrderExceedsStockCapacity(JSONArray items, String expectedErrorMessage) {
        List<String> productNames = new ArrayList<>();
        try {
            logger.debug("Opening New Order page.");
            driver.get("https://nano-flow-order-direct.base44.app/order");
            newOrderPage = new NewOrderPage(driver);

            for (int i = 0; i < items.size(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                String categoryName = (String) item.get("category");
                String productName = (String) item.get("productName");
                int quantity = ((Number) item.get("quantity")).intValue();

                productNames.add(productName);

                logger.debug("Selecting category: {}", categoryName);
                newOrderPage.selectCategoryByVisibleText(categoryName);
                pauseForDemo();

                logger.debug("Adding product to order: {}", productName);
                newOrderPage.addProductByName(productName);
                pauseForDemo();

                logger.debug("Changing product quantity to: {}", quantity);
                newOrderPage.setOrderItemQuantity(i, quantity);
            }

            logger.debug("Attempting to submit order.");
            newOrderPage.clickSubmitOrderButton();
            pauseForDemo();

            logger.debug("Verifying validation error message appears.");
            assertTrue("Expected validation error message was not displayed.",
                    newOrderPage.isValidationErrorDisplayed(expectedErrorMessage));

            logger.info("Success: Exceeds stock capacity verification passed.");
            pauseForDemo();

        } catch (AssertionError e) {
            logger.error("Validation failed for products {}: {}", productNames, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing products {}: {}", productNames, e.getMessage(), e);
            throw e;
        }
    }

    private void executeOutOfStockButtonDisabled(String categoryName, String productName, int stockQuantity) {
        try {
            logger.debug("Phase 1: Ordering the entire available stock.");
            driver.get("https://nano-flow-order-direct.base44.app/order");
            newOrderPage = new NewOrderPage(driver);

            logger.debug("Selecting category: {}", categoryName);
            newOrderPage.selectCategoryByVisibleText(categoryName);
            pauseForDemo();

            logger.debug("Adding product to order: {}", productName);
            newOrderPage.addProductByName(productName);
            pauseForDemo();

            logger.debug("Changing product quantity to max stock: {}", stockQuantity);
            newOrderPage.setFirstOrderItemQuantity(stockQuantity);

            logger.debug("Submitting order.");
            newOrderPage.submitOrder();
            pauseForDemo();

            logger.debug("Phase 2: Returning to Order Page to verify button state.");
            newOrderPage.header().clickNewOrder();
            pauseForDemo();

            logger.debug("Selecting category again: {}", categoryName);
            newOrderPage.selectCategoryByVisibleText(categoryName);
            pauseForDemo();

            logger.debug("Verifying 'Add to Order' button is disabled for product: {}", productName);
            assertTrue("Expected 'Add to Order' button to be disabled for out of stock product: " + productName,
                    newOrderPage.isProductAddButtonDisabled(productName));

            logger.info("Success: Out of stock button verification passed.");
            pauseForDemo();

        } catch (AssertionError e) {
            logger.error("Validation failed for product '{}': {}", productName, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing product '{}': {}", productName, e.getMessage(), e);
            throw e;
        }
    }

    private void pauseForDemo() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.warn("Thread sleep was interrupted during demo pause.", e);
            Thread.currentThread().interrupt();
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            logger.info("Closing WebDriver.");
            driver.quit();
        }
    }
}