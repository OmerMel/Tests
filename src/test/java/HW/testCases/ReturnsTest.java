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

import static org.junit.Assert.assertTrue;

public class ReturnsTest {

    private static final Logger logger = LogManager.getLogger(ReturnsTest.class);
    private WebDriver driver;
    private ReturnsPage returnsPage;
    private NewOrderPage orderPage;
    private JSONObject returnsData;

    @Before
    public void setUp() {
        logger.info("Initializing WebDriver and loading returns test data.");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/returns_data.json");
            returnsData = (JSONObject) jsonParser.parse(reader);
            logger.info("Successfully loaded returns_data.json.");
        } catch (Exception e) {
            logger.error("Failed to load returns_data.json", e);
        }
        //performManualOrderSetup();
        driver.get("https://nano-flow-order-direct.base44.app/returns");
        returnsPage = new ReturnsPage(driver);
    }

//    private void performManualOrderSetup() {
//        driver.get("https://nano-flow-order-direct.base44.app/order");
//        orderPage = new NewOrderPage(driver);
//
//        // שימוש במתודות שכבר קיימות לך!
//        orderPage.selectCategoryByVisibleText("Groceries");
//        orderPage.submitOrder(); // או הפעולה שאתה יודע שמבצעת הזמנה
//        // ... סיום הזמנה ...
//    }

    @Test
    public void testReturnsDataDriven() throws InterruptedException {
        if (!returnsPage.isProductSelectAvailable()) {
            logger.info("No return products available on screen. Skipping return tests.");
            return;
        }

        JSONArray returnsArray = (JSONArray) returnsData.get("returnTests");

        for (int i = 0; i < returnsArray.size(); i++) {
            JSONObject obj = (JSONObject) returnsArray.get(i);
            String product = (String) obj.get("productName");
            int quantity = ((Long) obj.get("quantity")).intValue();
            boolean shouldSucceed = (boolean) obj.get("shouldSucceed");

            logger.info("Running Test: Product: {} | Qty: {}", product, quantity);

            try {
                // ביצוע הפעולה דרך ה-Page Object
                returnsPage.performReturn(product, quantity);

                // המתנה קלה לקפיצת הודעת ה-Toast
                Thread.sleep(1000);

                if (shouldSucceed) {
                    // וידוא הודעת הצלחה
                    assertTrue("Expected success message but it was not displayed.",
                            returnsPage.isToastMessageDisplayed("Success"));
                    logger.info("Test passed: Return processed successfully.");
                } else {
                    // וידוא הודעת שגיאה (למשל עבור כמות לא תקינה)
                    assertTrue("Expected error message for invalid quantity but it was not displayed.",
                            returnsPage.isToastMessageDisplayed("Exceeds"));
                    logger.info("Test passed: System correctly blocked invalid return quantity.");
                }
            } catch (AssertionError e) {
                logger.error("Validation failed for test '{}': {}", product, e.getMessage());
                throw e;
            }
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