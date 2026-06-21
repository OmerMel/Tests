package HW.testCases;

import HW.pages.NewOrderPage;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NewOrderFiltersTests {

    // Initialize the Log4j2 logger for this specific class
    private static final Logger logger = LogManager.getLogger(NewOrderFiltersTests.class);

    private WebDriver driver;
    private NewOrderPage orderPage;
    private JSONObject allFiltersData;

    @Before
    public void setUp() {
        logger.info("Initializing WebDriver and navigating to the Order page.");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        driver.get("https://nano-flow-order-direct.base44.app/order");
        orderPage = new NewOrderPage(driver);

        // Load JSON test data from resources
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/filters_data.json");
            allFiltersData = (JSONObject) jsonParser.parse(reader);
            logger.info("Successfully loaded filters_data.json.");
        } catch (Exception e) {
            logger.error("Failed to load JSON data file.", e);
        }
    }

    @Test
    public void testPriceFiltersDataDriven() throws InterruptedException {
        logger.info("Starting data-driven price filter tests.");
        JSONArray priceArray = (JSONArray) allFiltersData.get("sliderTests");

        for (int i = 0; i < priceArray.size(); i++) {
            JSONObject obj = (JSONObject) priceArray.get(i);

            String category = (String) obj.get("category");
            int maxPrice = ((Long) obj.get("maxPrice")).intValue();
            int expectedCount = ((Long) obj.get("expectedCount")).intValue();

            executePriceFilterVerification(category, maxPrice, expectedCount);
        }
    }

    private void executePriceFilterVerification(String category, int maxPrice, int expectedCount) throws InterruptedException {
        logger.debug("Verifying price filter for category: '" + category + "' | Max Price: $" + maxPrice);

        orderPage.selectCategoryByVisibleText(category);
        orderPage.setPriceSliderValue(maxPrice);

        Thread.sleep(2000);

        int actualProductsCount = orderPage.getDisplayedProductsCount();

        try {
            assertEquals("Test failed for category '" + category + "' with max price " + maxPrice,
                    expectedCount, actualProductsCount);

            logger.debug("Success: Found {} products as expected.", actualProductsCount);

        } catch (AssertionError e) {
            logger.error("Mismatch: Expected {} products, but found {}.", expectedCount, actualProductsCount);
            throw e;
        }
    }

    @Test
    public void testSearchFiltersDataDriven() throws InterruptedException {
        logger.info("Starting data-driven search filter tests.");
        JSONArray searchArray = (JSONArray) allFiltersData.get("searchTests");

        for (int i = 0; i < searchArray.size(); i++) {
            JSONObject obj = (JSONObject) searchArray.get(i);

            String category = (String) obj.get("category");
            String phrase = (String) obj.get("searchPhrase");
            boolean expectResults = (boolean) obj.get("shouldExpectResults");

            executeSearchVerification(category, phrase, expectResults);
        }
    }

    private void executeSearchVerification(String category, String searchPhrase, boolean shouldExpectResults) throws InterruptedException {
        logger.debug("Verifying search filter for category: '" + category + "' | Phrase: '" + searchPhrase + "'");

        orderPage.selectCategoryByVisibleText(category);
        orderPage.searchProduct(searchPhrase);

        // Allow React to filter products based on search input
        Thread.sleep(1000);

        int productsCount = orderPage.getDisplayedProductsCount();

        try {
            if (shouldExpectResults) {
                assertTrue("Validation failed: No products found for '" + searchPhrase + "'", productsCount > 0);

                // Deep verification: Ensure all displayed products actually contain the search phrase
                List<String> actualProductNames = orderPage.getDisplayedProductNames();
                for (String productName : actualProductNames) {
                    assertTrue("Validation failed: Found unrelated product '" + productName + "' when searching for '" + searchPhrase + "'",
                            productName.toLowerCase().contains(searchPhrase.toLowerCase()));
                }

                logger.debug("Success: Found " + productsCount + " matching products for '" + searchPhrase + "'.");
            } else {
                assertEquals("Validation failed: Found products for a phrase that should yield empty results ('" + searchPhrase + "')", 0, productsCount);
                logger.debug("Success: No products found for '" + searchPhrase + "' as expected.");
            }
        } catch (AssertionError e) {
            logger.error("Test Failed! " + e.getMessage());
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