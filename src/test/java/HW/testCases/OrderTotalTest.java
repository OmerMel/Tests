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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrderTotalTest {

    private static final Logger logger = LogManager.getLogger(OrderTotalTest.class);

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

    @Test
    public void testOrderBelowTotalLimitIsSubmittedSuccessfully() {
        logger.info("Starting test: order below total limit is submitted successfully.");

        JSONArray testDataArray = (JSONArray) allOrderTotalData.get("belowLimitOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            String categoryName = (String) testData.get("category");
            String productName = (String) testData.get("productName");
            double maxAllowedTotal = ((Number) testData.get("maxAllowedTotal")).doubleValue();
            String expectedStatus = (String) testData.get("expectedStatus");

            executeBelowTotalLimitOrderVerification(
                    categoryName,
                    productName,
                    maxAllowedTotal,
                    expectedStatus
            );
        }

        logger.info("Finished test: order below total limit is submitted successfully.");
    }

    private void executeBelowTotalLimitOrderVerification(String categoryName,
                                                         String productName,
                                                         double maxAllowedTotal,
                                                         String expectedStatus) {
        logger.info("Opening New Order page.");
        driver.get("https://nano-flow-order-direct.base44.app/order");

        newOrderPage = new NewOrderPage(driver);

        logger.info("Selecting category: {}", categoryName);
        newOrderPage.selectCategoryByVisibleText(categoryName);
        pauseForDemo();

        logger.info("Adding product to order: {}", productName);
        newOrderPage.addProductByName(productName);
        pauseForDemo();

        double actualTotal = newOrderPage.getOrderTotalValue();
        logger.info("Verifying order total is below limit. Actual total: {}, Max allowed: {}",
                actualTotal, maxAllowedTotal);

        assertTrue("Order total is not below the allowed limit. Actual total: " + actualTotal,
                actualTotal < maxAllowedTotal);

        logger.info("Submitting order.");
        newOrderPage.submitOrder();
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

        logger.info("Below total limit order verification passed.");
    }

    @Test
    public void testOrderAboveTotalLimitIsBlocked() {
        logger.info("Starting test: order above total limit is blocked.");

        JSONArray testDataArray = (JSONArray) allOrderTotalData.get("aboveLimitOrderTests");

        for (Object testDataObject : testDataArray) {
            JSONObject testData = (JSONObject) testDataObject;

            String categoryName = (String) testData.get("category");
            String productName = (String) testData.get("productName");
            int quantity = ((Number) testData.get("quantity")).intValue();
            double limit = ((Number) testData.get("limit")).doubleValue();
            String expectedErrorMessage = (String) testData.get("expectedErrorMessage");

            executeAboveTotalLimitOrderVerification(
                    categoryName,
                    productName,
                    quantity,
                    limit,
                    expectedErrorMessage
            );
        }

        logger.info("Finished test: order above total limit is blocked.");
    }

    private void executeAboveTotalLimitOrderVerification(String categoryName,
                                                         String productName,
                                                         int quantity,
                                                         double limit,
                                                         String expectedErrorMessage) {
        logger.info("Opening New Order page.");
        driver.get("https://nano-flow-order-direct.base44.app/order");

        newOrderPage = new NewOrderPage(driver);

        logger.info("Selecting category: {}", categoryName);
        newOrderPage.selectCategoryByVisibleText(categoryName);
        pauseForDemo();

        logger.info("Adding product to order: {}", productName);
        newOrderPage.addProductByName(productName);
        pauseForDemo();

        logger.info("Changing product quantity to: {}", quantity);
        newOrderPage.setFirstOrderItemQuantity(quantity);
        pauseForDemo();

        double actualTotal = newOrderPage.getOrderTotalValue();
        logger.info("Verifying order total is above limit. Actual total: {}, Limit: {}",
                actualTotal, limit);

        assertTrue("Order total is not above the allowed limit. Actual total: " + actualTotal,
                actualTotal > limit);

        logger.info("Submitting order above total limit.");
        newOrderPage.clickSubmitOrderButton();
        pauseForDemo();

        logger.info("Verifying expected validation error appears: {}", expectedErrorMessage);
        assertTrue("Expected validation error message was not displayed: " + expectedErrorMessage,
                newOrderPage.isValidationErrorDisplayed(expectedErrorMessage));

        logger.info("Above total limit order verification passed.");
    }

    private void pauseForDemo() {
        try {
            Thread.sleep(1000);
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
