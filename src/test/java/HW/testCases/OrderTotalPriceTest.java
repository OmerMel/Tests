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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderTotalPriceTest {

    private static final Logger logger = LogManager.getLogger(OrderTotalPriceTest.class);

    private WebDriver driver;
    private NewOrderPage newOrderPage;
    private OrderHistoryPage orderHistoryPage;
    private JSONObject allOrderTotalData;

    @Before
    public void setUp() {
        logger.info("Starting OrderTotalTest setup.");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/order_total_data.json");
            allOrderTotalData = (JSONObject) jsonParser.parse(reader);
            logger.info("Successfully loaded order_total_data.json.");
        } catch (Exception e) {
            logger.error("Failed to load order_total_data.json.", e);
        }
    }

    // ==================== TESTS ====================

    @Test
    public void testPriceOrderTotalLimit() {
        logger.info("Starting test: Order below total limit is submitted successfully.");

        JSONArray testDataArray = (JSONArray) allOrderTotalData.get("priceLimitOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            JSONArray items = (JSONArray) testData.get("items");
            double maxPriceAllowed = ((Number) testData.get("maxPriceAllowed")).doubleValue();
            String expectedStatus = (String) testData.get("expectedStatus");

            executePriceOrderTotalLimit(items, maxPriceAllowed, expectedStatus);
        }
        logger.info("Finished test: Order below total price limit is submitted successfully.");
    }

    @Test
    public void testPriceOrderTotalLimitExceeded() {
        logger.info("Starting test: Order above total limit is blocked with an error message.");

        JSONArray testDataArray = (JSONArray) allOrderTotalData.get("aboveLimitOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            JSONArray items = (JSONArray) testData.get("items");
            double maxPriceAllowed = ((Number) testData.get("maxPriceAllowed")).doubleValue();
            String expectedErrorMessage = (String) testData.get("expectedErrorMessage");

            executePriceOrderTotalLimitExceeded(items, maxPriceAllowed, expectedErrorMessage);
        }
        logger.info("Finished test: Order above total limit is blocked.");
    }

    @Test
    public void testItemRemovalUpdatesTotal() {
        logger.info("Starting test: Verify removing an item updates the total price.");

        JSONArray testDataArray = (JSONArray) allOrderTotalData.get("itemRemovalTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            JSONArray itemsToAdd = (JSONArray) testData.get("itemsToAdd");
            String itemToRemove = (String) testData.get("itemToRemove");

            executeItemRemovalVerification(itemsToAdd, itemToRemove);
        }
        logger.info("Finished test: Item removal updates total.");
    }

    // ==================== HELPER METHODS ====================

    private void executePriceOrderTotalLimit(JSONArray items, double maxPriceAllowed, String expectedStatus) {
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

            double actualTotal = newOrderPage.getOrderTotalValue();

            // Removed redundant String.valueOf() - Log4j handles double natively
            logger.debug("Verifying total price order is below limit. Actual total: {}, Max allowed: {}", actualTotal, maxPriceAllowed);

            assertTrue("Total price order is not below the allowed limit. Actual total: " + actualTotal, actualTotal < maxPriceAllowed);

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

            logger.info("Success: Below total limit order verification passed.");
            pauseForDemo();

        } catch (AssertionError e) {
            logger.error("Validation failed for products {}: {}", productNames, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing products {}: {}", productNames, e.getMessage(), e);
            throw e;
        }
    }

    private void executePriceOrderTotalLimitExceeded(JSONArray items, double maxPriceAllowed, String expectedErrorMessage) {
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

            double actualTotal = newOrderPage.getOrderTotalValue();

            // Removed redundant String.valueOf() here as well
            logger.debug("Verifying total price order is above limit. Actual total: {}, Limit: {}", actualTotal, maxPriceAllowed);

            assertTrue("Total price order should be above the allowed limit. Actual total: " + actualTotal, actualTotal > maxPriceAllowed);

            logger.debug("Submitting order above limit.");
            newOrderPage.clickSubmitOrderButton();
            pauseForDemo();

            logger.debug("Verifying validation error message appears");
            assertTrue("Expected validation error message was not displayed.",
                    newOrderPage.isValidationErrorDisplayed(expectedErrorMessage));

            logger.info("Success: Above total limit order verification passed.");
            pauseForDemo();

        } catch (AssertionError e) {
            logger.error("Validation failed for products {}: {}", productNames, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while processing products {}: {}", productNames, e.getMessage(), e);
            throw e;
        }
    }

    private void executeItemRemovalVerification(JSONArray itemsToAdd, String itemToRemove) {
        try {
            logger.debug("Opening New Order page.");
            driver.get("https://nano-flow-order-direct.base44.app/order");
            newOrderPage = new NewOrderPage(driver);

            // 1. Add the products
            for (int i = 0; i < itemsToAdd.size(); i++) {
                JSONObject item = (JSONObject) itemsToAdd.get(i);
                String categoryName = (String) item.get("category");
                String productName = (String) item.get("productName");
                int quantity = ((Number) item.get("quantity")).intValue();

                logger.debug("Adding product: {} from category: {}", productName, categoryName);
                newOrderPage.selectCategoryByVisibleText(categoryName);
                pauseForDemo();

                newOrderPage.addProductByName(productName);
                pauseForDemo();

                newOrderPage.setOrderItemQuantity(i, quantity);
            }

            // 2. Save the total price before removal
            double initialTotal = newOrderPage.getOrderTotalValue();
            logger.debug("Initial total before removal: {}", initialTotal);

            // 3. Remove the product
            logger.debug("Removing product: {}", itemToRemove);
            newOrderPage.removeProductFromSummary(itemToRemove);
            pauseForDemo();

            // 4. Verify: Product is no longer in the list
            boolean isStillThere = newOrderPage.isProductInSummary(itemToRemove);
            assertTrue("Validation failed: Product '" + itemToRemove + "' is still visible after removal.", !isStillThere);
            logger.info("Success: Product '{}' was successfully removed from the UI.", itemToRemove);

            // 5. Verify: Total price updated (decreased compared to initial total)
            double newTotal = newOrderPage.getOrderTotalValue();
            logger.debug("New total after removal: {}", newTotal);

            assertTrue("Validation failed: Total price did not decrease after item removal. Initial: " + initialTotal + ", New: " + newTotal,
                    newTotal < initialTotal);
            logger.info("Success: Total price updated correctly after removal.");

        } catch (AssertionError e) {
            logger.error("Validation failed during item removal test: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during item removal test: {}", e.getMessage(), e);
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