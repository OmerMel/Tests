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

        downloadPath = Paths.get(System.getProperty("user.dir"), "target", "downloads").toAbsolutePath().toString();
        File downloadDir = new File(downloadPath);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs(); // יוצר את התיקייה אם היא לא קיימת
        }

        logger.info("Configured download path: " + downloadPath);

        // הגדרת ChromeOptions כדי שיוריד לתיקייה שלנו בכוח
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadPath);
        chromePrefs.put("download.prompt_for_download", false); // מונע חלון קופץ של "שמור בשם"
        chromePrefs.put("download.directory_upgrade", true); // מכריח שימוש בתיקייה שהגדרנו
        chromePrefs.put("safebrowsing.enabled", true); // מונע מ-Chrome לחסום את ה-CSV בטענה שהוא מסוכן

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);

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

    @Test
    public void testExportOrderHistoryToCsvDataDriven() throws Exception {
        logger.info("Starting Data-Driven test: Export Order History to CSV with multiple items.");

        // נניח שטענת את הקובץ לתוך allHistoryData ב-setUp()
        JSONArray exportTests = (JSONArray) allOrderHistoryData.get("exportCsvTests");

        for (int i = 0; i < exportTests.size(); i++) {
            JSONObject testCase = (JSONObject) exportTests.get(i);
            JSONArray itemsToOrder = (JSONArray) testCase.get("items");
            String expectedStatus = (String) testCase.get("expectedStatus");

            executeCsvExportVerification(itemsToOrder, expectedStatus);
        }
    }

    private void executeCsvExportVerification(JSONArray items, String expectedStatus) throws Exception {
        try {
            // 1. התחלה נקייה ויצירת ההזמנה עם כל המוצרים
            driver.get("https://nano-flow-order-direct.base44.app/order");
            NewOrderPage orderPage = new NewOrderPage(driver);
            Thread.sleep(1500);

            logger.info("Adding {} items to the order.", items.size());
            for (int j = 0; j < items.size(); j++) {
                JSONObject item = (JSONObject) items.get(j);
                String category = (String) item.get("category");
                String productName = (String) item.get("productName");
                int quantity = ((Long) item.get("quantity")).intValue();

                logger.debug("Adding product: {}", productName);
                orderPage.selectCategoryByVisibleText(category);
                Thread.sleep(1000); // המתנה לטעינת מוצרים

                orderPage.addProductByName(productName);

                // j מייצג את האינדקס של המוצר בתוך ה-Order Summary!
                orderPage.setOrderItemQuantity(j, quantity);
            }

            logger.info("Submitting order with multiple items.");
            orderPage.submitOrder();
            Thread.sleep(2000); // המתנה שההזמנה תירשם

            // 2. מעבר להיסטוריית הזמנות
            orderPage.header().clickOrderHistory();
            OrderHistoryPage historyPage = new OrderHistoryPage(driver);
            Thread.sleep(1500);

            // 3. ניקוי תיקיית ההורדות מקבצי CSV קודמים
            File dir = new File(downloadPath);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".csv"));
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }

            // 4. לחיצה על ייצוא
            logger.info("Clicking export to CSV button.");
            historyPage.clickExportToCsv();

            // 5. המתנה להורדת הקובץ (עד 15 שניות)
            File downloadedFile = null;
            boolean fileDownloaded = false;
            String targetFileName = "order_history.csv";

            // נתיב לתיקיית ההורדות הסטנדרטית של מערכת ההפעלה כגיבוי
            String userDownloads = System.getProperty("user.home") + File.separator + "Downloads";

            for (int i = 0; i < 15; i++) {
                dir = new File(downloadPath); // התיקייה בפרויקט
                File userDir = new File(userDownloads); // התיקייה במחשב

                File targetInProject = new File(dir, targetFileName);
                File targetInUser = new File(userDir, targetFileName);

                if (targetInProject.exists()) {
                    downloadedFile = targetInProject;
                    fileDownloaded = true; break;
                } else if (targetInUser.exists()) {
                    downloadedFile = targetInUser;
                    fileDownloaded = true; break;
                }

                Thread.sleep(1000); // המתנה של שנייה בין בדיקה לבדיקה
            }

            assertTrue("Validation failed: CSV file was not downloaded within the timeout.", fileDownloaded);
            logger.info("Success: CSV file downloaded successfully at: " + downloadedFile.getAbsolutePath());

            // 6. קריאת תוכן הקובץ ואימות הנתונים - מוודאים ש*כל* המוצרים נמצאים בפנים
            String csvContent = new String(Files.readAllBytes(downloadedFile.toPath()));
            logger.debug("CSV Content:\n" + csvContent);

            for (int j = 0; j < items.size(); j++) {
                JSONObject item = (JSONObject) items.get(j);
                String productName = (String) item.get("productName");
                int quantity = ((Long) item.get("quantity")).intValue();

                assertTrue("CSV does not contain the product name: " + productName,
                        csvContent.contains(productName));

                // מוודאים שהכמות מופיעה איפשהו בקובץ. בבדיקה מחמירה יותר אפשר לחפש באותה שורה.
                assertTrue("CSV does not contain the quantity " + quantity + " for product " + productName,
                        csvContent.contains(String.valueOf(quantity)));
            }

            // אימות הסטטוס
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

    @After
    public void tearDown() {
        if (driver != null) {
            logger.info("Closing WebDriver.");
            driver.quit();
        }
    }
}
