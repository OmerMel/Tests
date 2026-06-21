package HW.testCases;

import HW.pages.NewOrderPage;
import HW.pages.OrderHistoryPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

import java.time.Duration;

import static org.junit.Assert.assertTrue;

public class OrderHistoryTest {

    private static final Logger logger = LogManager.getLogger(OrderHistoryTest.class);

    private WebDriver driver;
    private NewOrderPage newOrderPage;
    private OrderHistoryPage orderHistoryPage;
    private JSONObject allOrderHistoryData;


    private void pauseForDemo() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Before
    public void setUp() {
        logger.info("Initializing WebDriver for Order History tests.");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/order_history_data.json");
            allOrderHistoryData = (JSONObject) jsonParser.parse(reader);
            logger.info("Successfully loaded order_history_data.json.");
        } catch (Exception e) {
            logger.error("Failed to load order_history_data.json.", e);
        }
    }

    @Test
    public void testOrderHistoryDisplayedWhenOrderExists() {
        logger.info("Starting data-driven test: Order History displays existing orders.");

        JSONArray orderHistoryArray = (JSONArray) allOrderHistoryData.get("orderHistoryTests");

        for (int i = 0; i < orderHistoryArray.size(); i++) {
            JSONObject obj = (JSONObject) orderHistoryArray.get(i);

            String categoryName = (String) obj.get("category");
            String productName = (String) obj.get("productName");
            String expectedStatus = (String) obj.get("expectedStatus");
            boolean shouldAppearInHistory = (boolean) obj.get("shouldAppearInHistory");

            logger.info("Running Test {}: Category: {} | Product: {} | Expected Status: {}",
                    i + 1, categoryName, productName, expectedStatus);

            try {
                executeOrderHistoryVerification(categoryName, productName, expectedStatus, shouldAppearInHistory);

                logger.info("Test passed: Product '{}' was displayed correctly in Order History.", productName);

            } catch (AssertionError e) {
                logger.error("Validation failed for product '{}': {}", productName, e.getMessage());
                throw e;
            }
        }
    }

    @Test
    public void testEmptyOrderHistoryMessageDisplayed() {
        logger.info("Starting data-driven test: Empty Order History message is displayed.");

        JSONArray emptyHistoryArray = (JSONArray) allOrderHistoryData.get("emptyOrderHistoryTests");

        for (int i = 0; i < emptyHistoryArray.size(); i++) {
            JSONObject obj = (JSONObject) emptyHistoryArray.get(i);

            String expectedCounterText = (String) obj.get("expectedCounterText");
            String expectedMessage = (String) obj.get("expectedMessage");

            logger.info("Running Empty History Test {}: Expected Counter: {} | Expected Message: {}",
                    i + 1, expectedCounterText, expectedMessage);

            try {
                executeEmptyOrderHistoryVerification(expectedCounterText, expectedMessage);

                logger.info("Test passed: Empty Order History message was displayed correctly.");

            } catch (AssertionError e) {
                logger.error("Validation failed for empty Order History test: {}", e.getMessage());
                throw e;
            }
        }
    }

    private void executeEmptyOrderHistoryVerification(String expectedCounterText, String expectedMessage) {
        driver.get("https://nano-flow-order-direct.base44.app/history");
        orderHistoryPage = new OrderHistoryPage(driver);

        pauseForDemo();

        logger.info("Validating Order History page title.");
        assertTrue("Order History page title is not displayed correctly.",
                orderHistoryPage.getPageTitle().contains("Order History"));

        String actualCounterText = orderHistoryPage.getOrdersCounterText();
        logger.info("Orders counter text: {}", actualCounterText);

        assertTrue("Expected empty orders counter was not displayed. Actual text: " + actualCounterText,
                actualCounterText.contains(expectedCounterText));

        logger.info("Validating empty Order History message.");
        assertTrue("Expected empty history message was not displayed: " + expectedMessage,
                orderHistoryPage.isNoOrdersMessageDisplayed());
    }

    private void executeOrderHistoryVerification(String categoryName,
                                                 String productName,
                                                 String expectedStatus,
                                                 boolean shouldAppearInHistory) {

        driver.get("https://nano-flow-order-direct.base44.app/order");
        newOrderPage = new NewOrderPage(driver);

        logger.info("Creating order: Category: {} | Product: {}", categoryName, productName);
        newOrderPage.createOrder(categoryName, productName);

        pauseForDemo();

        logger.info("Navigating to Order History using header navigation.");
        newOrderPage.header().clickOrderHistory();

        pauseForDemo();

        orderHistoryPage = new OrderHistoryPage(driver);

        logger.info("Validating Order History page title.");
        assertTrue("Order History page title is not displayed correctly.",
                orderHistoryPage.getPageTitle().contains("Order History"));

        String ordersCounter = orderHistoryPage.getOrdersCounterText();
        logger.info("Orders counter text: {}", ordersCounter);

        assertTrue("Orders counter does not show an existing order. Actual text: " + ordersCounter,
                !ordersCounter.contains("0 order"));

        logger.info("Validating order history list is displayed.");
        assertTrue("Order history list is not displayed.",
                orderHistoryPage.isOrderHistoryListDisplayed());

        if (shouldAppearInHistory) {
            logger.info("Validating product appears in Order History: {}", productName);
            assertTrue("The ordered product was not found in order history: " + productName,
                    orderHistoryPage.isOrderDisplayed(productName));
        }

        logger.info("Validating order status: {}", expectedStatus);
        assertTrue("Expected status was not found in order history: " + expectedStatus,
                orderHistoryPage.isStatusDisplayed(expectedStatus));

        logger.info("Validating Export CSV button is displayed.");
        assertTrue("Export CSV button is not displayed.",
                orderHistoryPage.isExportCsvButtonDisplayed());
    }

    @After
    public void tearDown() {
        if (driver != null) {
            logger.info("Closing WebDriver.");
            driver.quit();
        }
    }
}
