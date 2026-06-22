package HW.testCases;

import HW.pages.NewOrderPage;
import HW.pages.ReturnsPage;
import io.github.sridharbandi.HtmlCsRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.time.Duration;

public class AccessibilityJourneyTest {

    private static final Logger logger = LogManager.getLogger(AccessibilityJourneyTest.class);

    private WebDriver driver;
    private HtmlCsRunner htmlCsRunner;
    private NewOrderPage orderPage;
    private ReturnsPage returnsPage;

    @Before
    public void setUp() {
        logger.info("Initializing WebDriver for Accessibility Journey.");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        htmlCsRunner = new HtmlCsRunner(driver);
    }

    // ==================== TESTS ====================

    @Test
    public void testAccessibilityEndToEndJourney() throws Exception {
        logger.info("Starting Full Accessibility (a11y) Journey using HtmlCsRunner.");

        try {
            executeStation1HomePage();
            executeStation2NewOrderPageEmpty();
            executeStation3OrderSummaryWithProducts();
            executeStation4OrderHistory();
            executeStation5ReturnsPage();
            executeStation6DynamicErrorToast();

            logger.info("Success: Accessibility scans completed for all stations.");

        } catch (Exception e) {
            logger.error("An unexpected error occurred during the Accessibility Journey: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ==================== HELPER METHODS ====================

    private void executeStation1HomePage() throws InterruptedException, IOException {
        logger.debug("Station 1: Scanning Home Page");
        driver.get("https://nano-flow-order-direct.base44.app/");
        Thread.sleep(1500);
        htmlCsRunner.execute();
    }

    private void executeStation2NewOrderPageEmpty() throws InterruptedException, IOException {
        logger.debug("Station 2: Scanning New Order Page (Empty State)");
        driver.get("https://nano-flow-order-direct.base44.app/order");
        orderPage = new NewOrderPage(driver);
        Thread.sleep(1500);
        htmlCsRunner.execute();
    }

    private void executeStation3OrderSummaryWithProducts() throws InterruptedException, IOException {
        logger.debug("Station 3: Scanning Order Summary with Products");
        orderPage.selectCategoryByVisibleText("Mobile Accessories");
        Thread.sleep(1000);
        orderPage.addProductByName("Apple Airpods");
        orderPage.setOrderItemQuantity(0, 2);
        Thread.sleep(1000);
        htmlCsRunner.execute();

        orderPage.submitOrder();
        Thread.sleep(2000);
    }

    private void executeStation4OrderHistory() throws InterruptedException, IOException {
        logger.debug("Station 4: Scanning Order History Page");
        orderPage.header().clickOrderHistory();
        Thread.sleep(1500);
        htmlCsRunner.execute();
    }

    private void executeStation5ReturnsPage() throws InterruptedException, IOException {
        logger.debug("Station 5: Scanning Returns Page");
        orderPage.header().clickReturns();
        returnsPage = new ReturnsPage(driver);
        Thread.sleep(1500);
        htmlCsRunner.execute();
    }

    private void executeStation6DynamicErrorToast() throws InterruptedException, IOException {
        logger.debug("Station 6: Scanning Return Error Toast (Dynamic UI)");
        returnsPage.selectProductToReturn("Apple Airpods");
        returnsPage.setReturnQuantity(5);
        returnsPage.submitReturn();
        Thread.sleep(500);
        htmlCsRunner.execute();
    }

    @After
    public void tearDown() throws Exception {
        if (htmlCsRunner != null) {
            logger.info("Generating consolidated HTMLCS accessibility report in target directory.");
            htmlCsRunner.generateHtmlReport();
        }

        if (driver != null) {
            logger.info("Closing WebDriver.");
            driver.quit();
        }
    }
}