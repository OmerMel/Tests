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
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class OrderHistoryTest {

    private static final Logger logger = LogManager.getLogger(OrderHistoryTest.class);

    private WebDriver driver;
    private NewOrderPage newOrderPage;
    private OrderHistoryPage orderHistoryPage;
    private JSONObject allOrderHistoryData;
    private String downloadPath;

    @Before
    public void setUp() {
        logger.info("Initializing WebDriver for Order History tests.");

        downloadPath = Paths.get(System.getProperty("user.dir"), "target", "downloads").toAbsolutePath().toString();
        File downloadDir = new File(downloadPath);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        logger.info("Configured download path: {}", downloadPath);

        // Configure ChromeOptions to force downloads to the specific path
        HashMap<String, Object> chromePrefs = new HashMap<>();

        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadPath);
        chromePrefs.put("download.prompt_for_download", false); // Prevent 'Save As' prompt
        chromePrefs.put("download.directory_upgrade", true); // Force use of configured directory
        chromePrefs.put("safebrowsing.enabled", true); // Prevent Chrome from blocking the CSV download

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);

        driver = new ChromeDriver(options);
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

    // ==================== TESTS ====================

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

            logger.info("Running Test {}: Category: '{}' | Product: '{}' | Expected Status: '{}'",
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

            logger.info("Running Empty History Test {}: Expected Counter: '{}' | Expected Message: '{}'",
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

    @Test
    public void testExportOrderHistoryToCsvDataDriven() throws Exception {
        logger.info("Starting Data-Driven test: Export Order History to CSV with multiple items.");

        JSONArray exportTests = (JSONArray) allOrderHistoryData.get("exportCsvTests");

        for (int i = 0; i < exportTests.size(); i++) {
            JSONObject testCase = (JSONObject) exportTests.get(i);
            JSONArray itemsToOrder = (JSONArray) testCase.get("items");
            String expectedStatus = (String) testCase.get("expectedStatus");

            executeCsvExportVerification(itemsToOrder, expectedStatus);
        }
    }

    // ==================== HELPER METHODS ====================

    private void executeEmptyOrderHistoryVerification(String expectedCounterText, String expectedMessage) {
        driver.get("https://nano-flow-order-direct.base44.app/history");
        orderHistoryPage = new OrderHistoryPage(driver);
        pauseForDemo();

        logger.debug("Validating Order History page title.");
        assertTrue("Order History page title is not displayed correctly.",
                orderHistoryPage.getPageTitle().contains("Order History"));

        String actualCounterText = orderHistoryPage.getOrdersCounterText();
        logger.debug("Orders counter text: {}", actualCounterText);

        assertTrue("Expected empty orders counter was not displayed. Actual text: " + actualCounterText,
                actualCounterText.contains(expectedCounterText));

        logger.debug("Validating empty Order History message.");
        assertTrue("Expected empty history message was not displayed: " + expectedMessage,
                orderHistoryPage.isNoOrdersMessageDisplayed());
    }

    private void executeOrderHistoryVerification(String categoryName, String productName,
                                                 String expectedStatus, boolean shouldAppearInHistory) {

        driver.get("https://nano-flow-order-direct.base44.app/order");
        newOrderPage = new NewOrderPage(driver);

        logger.debug("Creating order: Category: '{}' | Product: '{}'", categoryName, productName);
        newOrderPage.createOrder(categoryName, productName);
        pauseForDemo();

        logger.debug("Navigating to Order History using header navigation.");
        newOrderPage.header().clickOrderHistory();
        pauseForDemo();

        orderHistoryPage = new OrderHistoryPage(driver);

        logger.debug("Validating Order History page title.");
        assertTrue("Order History page title is not displayed correctly.",
                orderHistoryPage.getPageTitle().contains("Order History"));

        String ordersCounter = orderHistoryPage.getOrdersCounterText();
        logger.debug("Orders counter text: {}", ordersCounter);

        assertTrue("Orders counter does not show an existing order. Actual text: " + ordersCounter,
                !ordersCounter.contains("0 order"));

        logger.debug("Validating order history list is displayed.");
        assertTrue("Order history list is not displayed.",
                orderHistoryPage.isOrderHistoryListDisplayed());

        if (shouldAppearInHistory) {
            logger.debug("Validating product appears in Order History: {}", productName);
            assertTrue("The ordered product was not found in order history: " + productName,
                    orderHistoryPage.isOrderDisplayed(productName));
        }

        logger.debug("Validating order status: {}", expectedStatus);
        assertTrue("Expected status was not found in order history: " + expectedStatus,
                orderHistoryPage.isStatusDisplayed(expectedStatus));

        logger.debug("Validating Export CSV button is displayed.");
        assertTrue("Export CSV button is not displayed.",
                orderHistoryPage.isExportCsvButtonDisplayed());
    }

    private void executeCsvExportVerification(JSONArray items, String expectedStatus) throws Exception {
        try {
            // Step 1: Clean start and create order with all products
            driver.get("https://nano-flow-order-direct.base44.app/order");
            NewOrderPage orderPage = new NewOrderPage(driver);
            Thread.sleep(1500);

            logger.debug("Adding {} items to the order.", items.size());
            for (int j = 0; j < items.size(); j++) {
                JSONObject item = (JSONObject) items.get(j);
                String category = (String) item.get("category");
                String productName = (String) item.get("productName");
                int quantity = ((Long) item.get("quantity")).intValue();

                logger.debug("Adding product: {}", productName);
                orderPage.selectCategoryByVisibleText(category);
                Thread.sleep(1000); // Wait for products to load

                orderPage.addProductByName(productName);

                orderPage.setOrderItemQuantity(j, quantity);
            }

            logger.debug("Submitting order with multiple items.");
            orderPage.submitOrder();
            Thread.sleep(2000); // Wait for the order to be registered

            // Step 2: Navigate to order history
            orderPage.header().clickOrderHistory();
            OrderHistoryPage historyPage = new OrderHistoryPage(driver);
            Thread.sleep(1500);

            // Step 3: Clean download directory from previous CSV files
            File dir = new File(downloadPath);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".csv"));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }

            // Step 4: Click export to CSV
            logger.debug("Clicking export to CSV button.");
            historyPage.clickExportToCsv();

            // Step 5: Wait for file download (up to 15 seconds)
            File downloadedFile = null;
            boolean fileDownloaded = false;
            String targetFileName = "order_history.csv";

            // Path to standard OS downloads folder as fallback
            String userDownloads = System.getProperty("user.home") + File.separator + "Downloads";

            for (int i = 0; i < 15; i++) {
                dir = new File(downloadPath);
                File userDir = new File(userDownloads);

                File targetInProject = new File(dir, targetFileName);
                File targetInUser = new File(userDir, targetFileName);

                if (targetInProject.exists()) {
                    downloadedFile = targetInProject;
                    fileDownloaded = true;
                    break;
                } else if (targetInUser.exists()) {
                    downloadedFile = targetInUser;
                    fileDownloaded = true;
                    break;
                }
                Thread.sleep(1000);
            }

            assertTrue("Validation failed: CSV file was not downloaded within the timeout.", fileDownloaded);
            logger.info("Success: CSV file downloaded successfully at: {}", downloadedFile.getAbsolutePath());

            // Step 6: Read file content and verify all products exist
            String csvContent = new String(Files.readAllBytes(downloadedFile.toPath()));
            logger.debug("CSV Content:\n{}", csvContent);

            for (int j = 0; j < items.size(); j++) {
                JSONObject item = (JSONObject) items.get(j);
                String productName = (String) item.get("productName");
                int quantity = ((Long) item.get("quantity")).intValue();

                assertTrue("CSV does not contain the product name: " + productName,
                        csvContent.contains(productName));

                // Verify quantity exists in file. For stricter validation, search within the same row.
                assertTrue("CSV does not contain the quantity " + quantity + " for product " + productName,
                        csvContent.contains(String.valueOf(quantity)));
            }

            // Verify status
            assertTrue("CSV does not contain the expected status: " + expectedStatus,
                    csvContent.contains(expectedStatus));

            logger.info("Success: CSV file accurately contains all {} items ordered.", items.size());

        } catch (AssertionError e) {
            logger.error("Validation failed during CSV export test: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during CSV export test: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void pauseForDemo() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.warn("Demo pause interrupted", e);
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