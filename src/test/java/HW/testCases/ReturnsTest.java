package HW.testCases;

import HW.pages.NewOrderPage;
import HW.pages.ReturnsPage;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReturnsTest {

    private static final Logger logger = LogManager.getLogger(ReturnsTest.class);

    private WebDriver driver;
    private NewOrderPage orderPage;
    private ReturnsPage returnsPage;
    private JSONObject returnsData;

    @Before
    public void setUp() {
        logger.info("Starting ReturnsTest setup.");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Always start from the Order page since a purchase is required before returning
        driver.get("https://nano-flow-order-direct.base44.app/order");
        orderPage = new NewOrderPage(driver);
        returnsPage = new ReturnsPage(driver);

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/returns_data.json");
            returnsData = (JSONObject) jsonParser.parse(reader);
            logger.info("Successfully loaded returns_data.json.");
        } catch (Exception e) {
            logger.error("Failed to load JSON data file.", e);
        }
    }

    // ==================== TESTS ====================

    @Test
    public void testValidReturnUpdatesStockAndRemovesFromList() throws InterruptedException {
        logger.info("Starting data-driven test: Valid return updates stock and removes from list.");
        JSONArray validTests = (JSONArray) returnsData.get("validReturnTests");

        for (int i = 0; i < validTests.size(); i++) {
            JSONObject testCase = (JSONObject) validTests.get(i);
            String category = (String) testCase.get("category");
            String productName = (String) testCase.get("productName");
            int orderQuantity = ((Long) testCase.get("orderQuantity")).intValue();
            int returnQuantity = ((Long) testCase.get("returnQuantity")).intValue();

            executeValidReturnVerification(category, productName, orderQuantity, returnQuantity);
        }
        logger.info("Finished test: Valid return updates stock.");
    }

    @Test
    public void testInvalidReturnShowsErrorAndKeepsStock() throws InterruptedException {
        logger.info("Starting data-driven test: Invalid return shows error and keeps stock.");
        JSONArray invalidTests = (JSONArray) returnsData.get("invalidReturnTests");

        for (int i = 0; i < invalidTests.size(); i++) {
            JSONObject testCase = (JSONObject) invalidTests.get(i);
            String category = (String) testCase.get("category");
            String productName = (String) testCase.get("productName");
            int orderQuantity = ((Long) testCase.get("orderQuantity")).intValue();
            int invalidReturnQuantity = ((Long) testCase.get("returnQuantity")).intValue();
            String expectedError = (String) testCase.get("expectedErrorMessage");

            executeInvalidReturnVerification(category, productName, orderQuantity, invalidReturnQuantity, expectedError);
        }
        logger.info("Finished test: Invalid return shows error.");
    }

    @Test
    public void testPartialReturnUpdatesDropdownQuantity() throws InterruptedException {
        logger.info("Starting data-driven test: Partial return updates dropdown quantity.");
        JSONArray partialTests = (JSONArray) returnsData.get("partialReturnTests");

        for (int i = 0; i < partialTests.size(); i++) {
            JSONObject testCase = (JSONObject) partialTests.get(i);
            String category = (String) testCase.get("category");
            String productName = (String) testCase.get("productName");
            int orderQuantity = ((Long) testCase.get("orderQuantity")).intValue();
            int returnQuantity = ((Long) testCase.get("returnQuantity")).intValue();

            executePartialReturnVerification(category, productName, orderQuantity, returnQuantity);
        }
        logger.info("Finished test: Partial return updates dropdown.");
    }

    // ==================== HELPER METHODS ====================

    private void executeValidReturnVerification(String category, String productName, int orderQuantity, int returnQuantity) throws InterruptedException {
        try {
            // Step 1: Purchase the product
            logger.debug("Phase 1: Ordering {} units of '{}' from category '{}'", orderQuantity, productName, category);
            orderPage.selectCategoryByVisibleText(category);
            Thread.sleep(1000);

            int initialStock = orderPage.getProductStock(productName);
            logger.debug("Initial stock for '{}' is {}", productName, initialStock);

            orderPage.addProductByName(productName);
            orderPage.setOrderItemQuantity(0, orderQuantity);
            orderPage.submitOrder();
            Thread.sleep(2000); // Wait for order registration

            // Step 2: Navigate to returns page
            logger.debug("Phase 2: Navigating to Returns page.");
            orderPage.header().clickReturns();
            Thread.sleep(1500);

            // Step 3: Execute return
            logger.debug("Returning {} units of '{}'", returnQuantity, productName);
            returnsPage.selectProductToReturn(productName);
            returnsPage.setReturnQuantity(returnQuantity);
            returnsPage.submitReturn();
            Thread.sleep(2000);

            // Verification 1: If full quantity is returned, product should disappear from returns list
            if (orderQuantity == returnQuantity) {
                boolean isStillAvailable = returnsPage.isProductAvailableForReturn(productName);
                assertTrue("Validation failed: Product is still in returns list after full return.", !isStillAvailable);
                logger.debug("Verified: Product removed from returns list.");
            }

            // Step 4: Navigate back to Order page to verify stock update
            logger.debug("Phase 3: Navigating back to Order page to verify stock update.");
            orderPage.header().clickNewOrder();
            Thread.sleep(1500);
            orderPage.selectCategoryByVisibleText(category);
            Thread.sleep(1500);

            // Verification 2: Stock is restored
            int finalStock = orderPage.getProductStock(productName);
            int expectedFinalStock = initialStock - orderQuantity + returnQuantity;

            assertEquals("Validation failed: Stock was not updated correctly after return.", expectedFinalStock, finalStock);
            logger.info("Success: Stock updated correctly for '{}'. Final stock is {}", productName, finalStock);

        } catch (AssertionError e) {
            logger.error("Validation failed for valid return test: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during valid return test: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void executeInvalidReturnVerification(String category, String productName, int orderQuantity, int invalidReturnQuantity, String expectedError) throws InterruptedException {
        try {
            // Step 1: Purchase the product
            logger.debug("Phase 1: Ordering {} units of '{}'", orderQuantity, productName);
            orderPage.selectCategoryByVisibleText(category);
            Thread.sleep(1000);

            int initialStock = orderPage.getProductStock(productName);

            orderPage.addProductByName(productName);
            orderPage.setOrderItemQuantity(0, orderQuantity);
            orderPage.submitOrder();
            Thread.sleep(2000);

            int expectedStockAfterOrder = initialStock - orderQuantity;

            // Step 2: Navigate to returns page
            logger.debug("Phase 2: Navigating to Returns page.");
            orderPage.header().clickReturns();
            Thread.sleep(1500);

            // Step 3: Attempt exceeding return
            logger.debug("Attempting to return {} units (ordered only {}).", invalidReturnQuantity, orderQuantity);
            returnsPage.selectProductToReturn(productName);
            returnsPage.setReturnQuantity(invalidReturnQuantity);
            returnsPage.submitReturn();

            // Verification 1: Error message pops up
            String actualError = returnsPage.getErrorMessage();
            assertTrue("Validation failed: Expected error message not found.", actualError.contains(expectedError));
            logger.debug("Verified: Expected error message displayed: '{}'", actualError);

            // Step 4: Navigate back to Order page to verify stock did not change
            logger.debug("Phase 3: Navigating back to Order page to verify stock remained untouched.");
            orderPage.header().clickNewOrder();
            Thread.sleep(1500);
            orderPage.selectCategoryByVisibleText(category);
            Thread.sleep(1500);

            // Verification 2: Stock remains as it was after the order
            int finalStock = orderPage.getProductStock(productName);
            assertEquals("Validation failed: Stock changed despite invalid return attempt.", expectedStockAfterOrder, finalStock);
            logger.info("Success: Invalid return blocked and stock remained untouched for '{}'.", productName);

        } catch (AssertionError e) {
            logger.error("Validation failed for invalid return test: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during invalid return test: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void executePartialReturnVerification(String category, String productName, int orderQuantity, int returnQuantity) throws InterruptedException {
        try {
            // Step 1: Purchase the product
            logger.debug("Phase 1: Ordering {} units of {}", orderQuantity, productName);
            orderPage.selectCategoryByVisibleText(category);
            Thread.sleep(1000);
            orderPage.addProductByName(productName);
            orderPage.setOrderItemQuantity(0, orderQuantity);
            orderPage.submitOrder();
            Thread.sleep(2000);

            // Step 2: Navigate to returns page
            logger.debug("Phase 2: Navigating to Returns page.");
            orderPage.header().clickReturns();
            Thread.sleep(1500);

            // Verification 1: Ensure dropdown shows the full ordered quantity
            int initialQuantityInDropdown = returnsPage.getRemainingQuantityFromDropdown(productName);
            assertEquals("Validation failed: Dropdown does not show the correct initial ordered quantity.",
                    orderQuantity, initialQuantityInDropdown);
            logger.debug("Verified initial dropdown quantity is: {}", initialQuantityInDropdown);

            // Step 3: Execute partial return
            logger.debug("Phase 3: Performing partial return of {} units.", returnQuantity);
            returnsPage.selectProductToReturn(productName);
            returnsPage.setReturnQuantity(returnQuantity);
            returnsPage.submitReturn();
            Thread.sleep(2000);

            // Verification 2: Product is still in the list, but quantity decreased
            boolean isStillAvailable = returnsPage.isProductAvailableForReturn(productName);
            assertTrue("Validation failed: Product completely disappeared from list after partial return.", isStillAvailable);

            int updatedQuantityInDropdown = returnsPage.getRemainingQuantityFromDropdown(productName);
            int expectedRemainingQuantity = orderQuantity - returnQuantity;

            assertEquals("Validation failed: Dropdown quantity did not update correctly after partial return.",
                    expectedRemainingQuantity, updatedQuantityInDropdown);

            logger.info("Success: Dropdown quantity successfully updated from {} to {} for '{}'.",
                    initialQuantityInDropdown, updatedQuantityInDropdown, productName);

        } catch (AssertionError e) {
            logger.error("Validation failed for partial return test: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during partial return test: {}", e.getMessage(), e);
            throw e;
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