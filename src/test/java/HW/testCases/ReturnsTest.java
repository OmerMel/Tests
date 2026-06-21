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
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // אנחנו תמיד מתחילים מדף ההזמנות כי חייבים לקנות משהו לפני שמחזירים
        driver.get("https://nano-flow-order-direct.base44.app/order");
        orderPage = new NewOrderPage(driver);
        returnsPage = new ReturnsPage(driver);

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/returns_data.json");
            returnsData = (JSONObject) jsonParser.parse(reader);
        } catch (Exception e) {
            logger.error("Failed to load JSON data file.", e);
        }
    }

    @Test
    public void testValidReturnUpdatesStockAndRemovesFromList() throws InterruptedException {
        logger.info("Starting valid return test.");
        JSONArray validTests = (JSONArray) returnsData.get("validReturnTests");
        JSONObject testCase = (JSONObject) validTests.get(0);

        String category = (String) testCase.get("category");
        String productName = (String) testCase.get("productName");
        int orderQuantity = ((Long) testCase.get("orderQuantity")).intValue();
        int returnQuantity = ((Long) testCase.get("returnQuantity")).intValue();

        // 1. קניית המוצר
        orderPage.selectCategoryByVisibleText(category);
        Thread.sleep(1000);
        int initialStock = orderPage.getProductStock(productName);
        logger.info("Initial stock for '{}' is {}", productName, initialStock);

        orderPage.addProductByName(productName);
        orderPage.setOrderItemQuantity(0, orderQuantity);
        orderPage.submitOrder();
        Thread.sleep(2000); // המתנה שההזמנה תירשם

        // 2. מעבר לדף ההחזרות
        logger.debug("Navigating to Returns page.");
        orderPage.header().clickReturns(); // מניח שיש לך פונקציה כזו ב-Header!
        Thread.sleep(1500);

        // 3. ביצוע ההחזרה
        logger.info("Returning {} units of '{}'", returnQuantity, productName);
        returnsPage.selectProductToReturn(productName);
        returnsPage.setReturnQuantity(returnQuantity);
        returnsPage.submitReturn();
        Thread.sleep(2000);

        // אימות 1: אם החזרנו את כל הכמות, המוצר אמור להיעלם מרשימת ההחזרות
        if (orderQuantity == returnQuantity) {
            boolean isStillAvailable = returnsPage.isProductAvailableForReturn(productName);
            assertTrue("Validation failed: Product is still in returns list after full return.", !isStillAvailable);
            logger.info("Success: Product removed from returns list.");
        }

        // 4. ניווט חזרה לדף ההזמנות לוודא שהמלאי התעדכן
        orderPage.header().clickNewOrder();
        Thread.sleep(1500);
        orderPage.selectCategoryByVisibleText(category);
        Thread.sleep(1500);

        // אימות 2: המלאי עלה חזרה
        int finalStock = orderPage.getProductStock(productName);
        int expectedFinalStock = initialStock - orderQuantity + returnQuantity;

        assertEquals("Validation failed: Stock was not updated correctly after return.", expectedFinalStock, finalStock);
        logger.info("Success: Stock updated correctly. Final stock is {}", finalStock);
    }

    @Test
    public void testInvalidReturnShowsErrorAndKeepsStock() throws InterruptedException {
        logger.info("Starting invalid return test.");
        JSONArray invalidTests = (JSONArray) returnsData.get("invalidReturnTests");
        JSONObject testCase = (JSONObject) invalidTests.get(0);

        String category = (String) testCase.get("category");
        String productName = (String) testCase.get("productName");
        int orderQuantity = ((Long) testCase.get("orderQuantity")).intValue();
        int invalidReturnQuantity = ((Long) testCase.get("returnQuantity")).intValue();
        String expectedError = (String) testCase.get("expectedErrorMessage");

        // 1. קניית המוצר
        orderPage.selectCategoryByVisibleText(category);
        Thread.sleep(1000);
        int initialStock = orderPage.getProductStock(productName);

        orderPage.addProductByName(productName);
        orderPage.setOrderItemQuantity(0, orderQuantity);
        orderPage.submitOrder();
        Thread.sleep(2000);

        int expectedStockAfterOrder = initialStock - orderQuantity;

        // 2. מעבר לדף ההחזרות
        orderPage.header().clickReturns();
        Thread.sleep(1500);

        // 3. ניסיון החזרה חורג
        logger.info("Attempting to return {} units (ordered only {}).", invalidReturnQuantity, orderQuantity);
        returnsPage.selectProductToReturn(productName);
        returnsPage.setReturnQuantity(invalidReturnQuantity);
        returnsPage.submitReturn();

        // אימות 1: הודעת שגיאה קופצת
        String actualError = returnsPage.getErrorMessage();
        assertTrue("Validation failed: Expected error message not found.", actualError.contains(expectedError));
        logger.info("Success: Expected error message displayed: '{}'", actualError);

        // 4. ניווט חזרה לדף ההזמנות לוודא שהמלאי *לא* השתנה
        orderPage.header().clickNewOrder();
        Thread.sleep(1500);
        orderPage.selectCategoryByVisibleText(category);
        Thread.sleep(1500);

        // אימות 2: המלאי נשאר כפי שהיה אחרי ההזמנה (לא חזר למלאי)
        int finalStock = orderPage.getProductStock(productName);
        assertEquals("Validation failed: Stock changed despite invalid return attempt.", expectedStockAfterOrder, finalStock);
        logger.info("Success: Stock remained untouched.");
    }

    @Test
    public void testPartialReturnUpdatesDropdownQuantity() throws InterruptedException {
        logger.info("Starting partial return test.");

        // טעינת הנתונים מה-JSON
        JSONArray partialTests = (JSONArray) returnsData.get("partialReturnTests");
        JSONObject testCase = (JSONObject) partialTests.get(0);

        String category = (String) testCase.get("category");
        String productName = (String) testCase.get("productName");
        int orderQuantity = ((Long) testCase.get("orderQuantity")).intValue();
        int returnQuantity = ((Long) testCase.get("returnQuantity")).intValue();

        // 1. קניית המוצר (קונים 2)
        logger.info("Ordering {} units of {}", orderQuantity, productName);
        orderPage.selectCategoryByVisibleText(category);
        Thread.sleep(1000);
        orderPage.addProductByName(productName);
        orderPage.setOrderItemQuantity(0, orderQuantity);
        orderPage.submitOrder();
        Thread.sleep(2000);

        // 2. מעבר לדף ההחזרות
        orderPage.header().clickReturns();
        Thread.sleep(1500);

        // 3. אימות ראשוני: מוודאים שהדרופדאון מציג את הכמות המלאה שקנינו
        int initialQuantityInDropdown = returnsPage.getRemainingQuantityFromDropdown(productName);
        assertEquals("Validation failed: Dropdown does not show the correct initial ordered quantity.",
                orderQuantity, initialQuantityInDropdown);
        logger.info("Verified initial dropdown quantity is: {}", initialQuantityInDropdown);

        // 4. ביצוע החזרה חלקית (מחזירים 1)
        logger.info("Performing partial return of {} units.", returnQuantity);
        returnsPage.selectProductToReturn(productName);
        returnsPage.setReturnQuantity(returnQuantity);
        returnsPage.submitReturn();
        Thread.sleep(2000);

        // 5. אימות סופי: מוודאים שהמוצר עדיין ברשימה, אבל הכמות שלו ירדה
        boolean isStillAvailable = returnsPage.isProductAvailableForReturn(productName);
        assertTrue("Validation failed: Product completely disappeared from list after partial return.", isStillAvailable);

        int updatedQuantityInDropdown = returnsPage.getRemainingQuantityFromDropdown(productName);
        int expectedRemainingQuantity = orderQuantity - returnQuantity;

        assertEquals("Validation failed: Dropdown quantity did not update correctly after partial return.",
                expectedRemainingQuantity, updatedQuantityInDropdown);

        logger.info("Success: Dropdown quantity successfully updated from {} to {}.",
                initialQuantityInDropdown, updatedQuantityInDropdown);
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}