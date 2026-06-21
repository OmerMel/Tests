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

    @Test
    public void testOrderWithAllowedCategoryIsSubmittedSuccessfully() {
        logger.info("Starting test: order with allowed category is submitted successfully.");

        JSONArray testDataArray = (JSONArray) allOrderCategoryData.get("validCategoryOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            String categoryName = (String) testData.get("category");
            String productName = (String) testData.get("productName");
            String expectedStatus = (String) testData.get("expectedStatus");

            executeAllowedCategoryOrderVerification(categoryName, productName, expectedStatus);
        }

        logger.info("Finished test: order with allowed category is submitted successfully.");
    }

    @Test
    public void testOrderWithFurnitureAndGroceriesIsBlocked() {
        logger.info("Starting test: order with Furniture and Groceries is blocked.");

        JSONArray testDataArray = (JSONArray) allOrderCategoryData.get("invalidMixedCategoryOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            String firstCategory = (String) testData.get("firstCategory");
            String firstProductName = (String) testData.get("firstProductName");
            String secondCategory = (String) testData.get("secondCategory");
            String secondProductName = (String) testData.get("secondProductName");
            String expectedErrorMessage = (String) testData.get("expectedErrorMessage");

            executeBlockedMixedCategoryOrderVerification(
                    firstCategory,
                    firstProductName,
                    secondCategory,
                    secondProductName,
                    expectedErrorMessage
            );
        }

        logger.info("Finished test: order with Furniture and Groceries is blocked.");
    }

    private void executeAllowedCategoryOrderVerification(String categoryName,
                                                         String productName,
                                                         String expectedStatus) {
        logger.info("Opening New Order page.");
        driver.get("https://nano-flow-order-direct.base44.app/order");

        newOrderPage = new NewOrderPage(driver);

        logger.info("Creating order. Category: {}, Product: {}", categoryName, productName);
        newOrderPage.createOrder(categoryName, productName);

        pauseForDemo();

        logger.info("Navigating to Order History using Header.");
        newOrderPage.header().clickOrderHistory();

        orderHistoryPage = new OrderHistoryPage(driver);

        logger.info("Verifying Order History page.");
        assertEquals("Order History", orderHistoryPage.getPageTitle());

        logger.info("Verifying ordered product appears in history: {}", productName);
        assertTrue("The ordered product was not found in order history: " + productName,
                orderHistoryPage.isOrderDisplayed(productName));

        logger.info("Verifying order status is displayed: {}", expectedStatus);
        assertTrue("Expected order status was not displayed: " + expectedStatus,
                orderHistoryPage.isStatusDisplayed(expectedStatus));

        logger.info("Allowed category order verification passed.");
    }

    private void executeBlockedMixedCategoryOrderVerification(String firstCategory,
                                                              String firstProductName,
                                                              String secondCategory,
                                                              String secondProductName,
                                                              String expectedErrorMessage) {
        logger.info("Opening New Order page.");
        driver.get("https://nano-flow-order-direct.base44.app/order");

        newOrderPage = new NewOrderPage(driver);

        logger.info("Adding first product. Category: {}, Product: {}", firstCategory, firstProductName);
        newOrderPage.selectCategoryByVisibleText(firstCategory);
        pauseForDemo();
        newOrderPage.addProductByName(firstProductName);
        pauseForDemo();

        logger.info("Adding second product. Category: {}, Product: {}", secondCategory, secondProductName);
        newOrderPage.selectCategoryByVisibleText(secondCategory);
        pauseForDemo();
        newOrderPage.addProductByName(secondProductName);
        pauseForDemo();

        logger.info("Submitting mixed category order.");
        newOrderPage.clickSubmitOrderButton();
        pauseForDemo();

        logger.info("Verifying expected error message appears: {}", expectedErrorMessage);
        assertTrue("Expected validation error message was not displayed: " + expectedErrorMessage,
                newOrderPage.isValidationErrorDisplayed(expectedErrorMessage));

        logger.info("Blocked mixed category order verification passed.");
    }

    private void pauseForDemo() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
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
