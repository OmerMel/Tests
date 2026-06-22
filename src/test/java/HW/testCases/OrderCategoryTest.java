package HW.testCases;

import HW.pages.NewOrderPage;
import HW.pages.OrderHistoryPage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderCategoryTest {

    private static final Logger logger = LogManager.getLogger(OrderCategoryTest.class);

    private WebDriver driver;
    private NewOrderPage newOrderPage;
    private OrderHistoryPage orderHistoryPage;
    private JSONObject allOrderCategoryData;

    @Before
    public void setUp() {
        logger.info("Starting OrderCategoryTest setup.");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/order_category_data.json");
            allOrderCategoryData = (JSONObject) jsonParser.parse(reader);
            logger.info("Successfully loaded order_category_data.json.");
        } catch (Exception e) {
            logger.error("Failed to load order_category_data.json.", e);
        }
    }

    // ==================== TESTS ====================

    @Test
    public void testOrderWithAllowedCategoryIsSubmittedSuccessfully() {
        logger.info("Starting test: Order with allowed category is submitted successfully.");
        JSONArray testDataArray = (JSONArray) allOrderCategoryData.get("validCategoryOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            String categoryName = (String) testData.get("category");
            String productName = (String) testData.get("productName");
            String expectedStatus = (String) testData.get("expectedStatus");

            executeAllowedCategoryOrderVerification(categoryName, productName, expectedStatus);
        }
        logger.info("Finished test: Order with allowed category is submitted successfully.");
    }

    @Test
    public void testOrderWithFurnitureAndGroceriesIsBlocked() {
        logger.info("Starting test: Order with Furniture and Groceries is blocked.");
        JSONArray testDataArray = (JSONArray) allOrderCategoryData.get("invalidMixedCategoryOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            String firstCategory = (String) testData.get("firstCategory");
            String firstProductName = (String) testData.get("firstProductName");
            String secondCategory = (String) testData.get("secondCategory");
            String secondProductName = (String) testData.get("secondProductName");
            String expectedErrorMessage = (String) testData.get("expectedErrorMessage");

            executeBlockedMixedCategoryOrderVerification(
                    firstCategory, firstProductName, secondCategory, secondProductName, expectedErrorMessage
            );
        }
        logger.info("Finished test: Order with Furniture and Groceries is blocked.");
    }

    // ==================== HELPER METHODS ====================

    private void executeAllowedCategoryOrderVerification(String categoryName, String productName, String expectedStatus) {
        logger.debug("Opening New Order page.");
        driver.get("https://nano-flow-order-direct.base44.app/order");
        newOrderPage = new NewOrderPage(driver);

        logger.debug("Creating order. Category: '{}', Product: '{}'", categoryName, productName);
        newOrderPage.createOrder(categoryName, productName);
        pauseForDemo();

        logger.debug("Navigating to Order History using Header.");
        newOrderPage.header().clickOrderHistory();
        orderHistoryPage = new OrderHistoryPage(driver);

        logger.debug("Verifying Order History page.");
        assertEquals("Order History", orderHistoryPage.getPageTitle());

        logger.debug("Verifying ordered product appears in history: '{}'", productName);
        assertTrue("The ordered product was not found in order history: " + productName,
                orderHistoryPage.isOrderDisplayed(productName));

        logger.debug("Verifying order status is displayed: '{}'", expectedStatus);
        assertTrue("Expected order status was not displayed: " + expectedStatus,
                orderHistoryPage.isStatusDisplayed(expectedStatus));

        logger.info("Success: Allowed category order verification passed for product '{}'.", productName);
    }

    private void executeBlockedMixedCategoryOrderVerification(String firstCategory, String firstProductName,
                                                              String secondCategory, String secondProductName,
                                                              String expectedErrorMessage) {
        logger.debug("Opening New Order page.");
        driver.get("https://nano-flow-order-direct.base44.app/order");
        newOrderPage = new NewOrderPage(driver);

        logger.debug("Adding first product. Category: '{}', Product: '{}'", firstCategory, firstProductName);
        newOrderPage.selectCategoryByVisibleText(firstCategory);
        pauseForDemo();
        newOrderPage.addProductByName(firstProductName);
        pauseForDemo();

        logger.debug("Adding second product. Category: '{}', Product: '{}'", secondCategory, secondProductName);
        newOrderPage.selectCategoryByVisibleText(secondCategory);
        pauseForDemo();
        newOrderPage.addProductByName(secondProductName);
        pauseForDemo();

        logger.debug("Submitting mixed category order.");
        newOrderPage.clickSubmitOrderButton();
        pauseForDemo();

        logger.debug("Verifying expected error message appears: '{}'", expectedErrorMessage);
        assertTrue("Expected validation error message was not displayed: " + expectedErrorMessage,
                newOrderPage.isValidationErrorDisplayed(expectedErrorMessage));

        logger.info("Success: Blocked mixed category order verification passed.");
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