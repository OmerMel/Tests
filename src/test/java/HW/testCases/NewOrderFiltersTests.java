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

    // ==================== TESTS ====================

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

    @Test
    public void testProductAddedToOrderSummaryDataDriven() throws InterruptedException {
        logger.info("Starting data-driven explicit test: Verify product is added to Order Summary.");
        JSONArray addArray = (JSONArray) allFiltersData.get("addToCartTests");

        for (int i = 0; i < addArray.size(); i++) {
            JSONObject obj = (JSONObject) addArray.get(i);
            String category = (String) obj.get("category");
            String productName = (String) obj.get("productName");

            executeAddToCartVerification(category, productName);
        }
    }

    @Test
    public void testCategoryFilteringDisplaysCorrectProducts() throws InterruptedException {
        logger.info("Starting data-driven test: Verify selected category displays correct products.");
        JSONArray categoryArray = (JSONArray) allFiltersData.get("categorySelectionTests");

        for (int i = 0; i < categoryArray.size(); i++) {
            JSONObject obj = (JSONObject) categoryArray.get(i);
            String category = (String) obj.get("category");
            String urlSlug = (String) obj.get("urlSlug");

            executeCategoryFilterVerification(category, urlSlug);
        }
    }

    @Test
    public void testCategorySwitchingUpdatesProducts() throws InterruptedException {
        logger.info("Starting data-driven test: Verify category switching updates products correctly.");
        JSONArray switchArray = (JSONArray) allFiltersData.get("categorySwitchTests");

        for (int i = 0; i < switchArray.size(); i++) {
            JSONObject obj = (JSONObject) switchArray.get(i);
            String initialCategory = (String) obj.get("initialCategory");
            String newCategory = (String) obj.get("newCategory");
            String newUrlSlug = (String) obj.get("newUrlSlug");

            executeCategorySwitchVerification(initialCategory, newCategory, newUrlSlug);
        }
    }

    @Test
    public void testInStockFilterHidesOutOfStockProducts() throws InterruptedException {
        logger.info("Starting data-driven test: Verify 'In stock only' filter hides out-of-stock products.");
        JSONArray stockArray = (JSONArray) allFiltersData.get("stockFilterTests");

        for (int i = 0; i < stockArray.size(); i++) {
            JSONObject obj = (JSONObject) stockArray.get(i);
            String category = (String) obj.get("category");
            String productName = (String) obj.get("productName");

            executeInStockFilterVerification(category, productName);
        }
    }

    // ==================== HELPER METHODS ====================

    private void executePriceFilterVerification(String category, int maxPrice, int expectedCount) throws InterruptedException {
        logger.debug("Verifying price filter for category: '{}' | Max Price: ${}", category, maxPrice);

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

    private void executeSearchVerification(String category, String searchPhrase, boolean shouldExpectResults) throws InterruptedException {
        logger.debug("Verifying search filter for category: '{}' | Phrase: '{}'", category, searchPhrase);

        orderPage.selectCategoryByVisibleText(category);
        orderPage.searchProduct(searchPhrase);

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
                logger.debug("Success: Found {} matching products for '{}'.", productsCount, searchPhrase);
            } else {
                assertEquals("Validation failed: Found products for a phrase that should yield empty results ('" + searchPhrase + "')", 0, productsCount);
                logger.debug("Success: No products found for '{}' as expected.", searchPhrase);
            }
        } catch (AssertionError e) {
            logger.error("Test Failed! {}", e.getMessage());
            throw e;
        }
    }

    private void executeAddToCartVerification(String category, String productName) throws InterruptedException {
        try {
            logger.debug("Selecting category: {}", category);
            orderPage.selectCategoryByVisibleText(category);

            Thread.sleep(1000);

            logger.debug("Adding product to order: {}", productName);
            orderPage.addProductByName(productName);

            Thread.sleep(1000);

            double totalValue = orderPage.getOrderTotalValue();

            // Verify total is greater than 0, meaning the product was successfully added
            assertTrue("Product was not added to the order summary (Total is still 0).", totalValue > 0);
            logger.info("Success: Product '{}' successfully added to Order Summary. Total is now: {}", productName, totalValue);

        } catch (AssertionError e) {
            logger.error("Validation failed: Product '{}' was not added. {}", productName, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while adding product '{}': {}", productName, e.getMessage(), e);
            throw e;
        }
    }

    private void executeCategoryFilterVerification(String category, String urlSlug) throws InterruptedException {
        try {
            logger.debug("Testing category selection for: {}", category);
            orderPage.selectCategoryByVisibleText(category);

            Thread.sleep(1500);

            List<String> imageUrls = orderPage.getDisplayedProductImageUrls();
            assertTrue("No products were displayed for category: " + category, !imageUrls.isEmpty());

            for (String url : imageUrls) {
                // Verify the category name appears in the image URL
                assertTrue("Found a product that does not belong to the category! Image URL: " + url,
                        url.toLowerCase().contains("/" + urlSlug + "/"));
            }
            logger.info("Success: All {} displayed products belong to the '{}' category.", imageUrls.size(), category);

        } catch (AssertionError e) {
            logger.error("Validation failed for category '{}': {}", category, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while testing category '{}': {}", category, e.getMessage(), e);
            throw e;
        }
    }

    private void executeCategorySwitchVerification(String initialCategory, String newCategory, String newUrlSlug) throws InterruptedException {
        try {
            logger.debug("Step 1: Selecting initial category: {}", initialCategory);
            orderPage.selectCategoryByVisibleText(initialCategory);
            Thread.sleep(1500);

            logger.debug("Step 2: Switching to new category: {}", newCategory);
            orderPage.selectCategoryByVisibleText(newCategory);
            Thread.sleep(1500);

            List<String> imageUrls = orderPage.getDisplayedProductImageUrls();
            assertTrue("No products were displayed after switching to: " + newCategory, !imageUrls.isEmpty());

            for (String url : imageUrls) {
                // Verify all products match only the new category
                assertTrue("Found a product from the old category after switching! Image URL: " + url,
                        url.toLowerCase().contains("/" + newUrlSlug + "/"));
            }
            logger.info("Success: Successfully switched to '{}' and all products updated correctly.", newCategory);

        } catch (AssertionError e) {
            logger.error("Validation failed after switching to category '{}': {}", newCategory, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while switching to category '{}': {}", newCategory, e.getMessage(), e);
            throw e;
        }
    }

    private void executeInStockFilterVerification(String category, String productName) throws InterruptedException {
        try {
            driver.get("https://nano-flow-order-direct.base44.app/order");
            Thread.sleep(1500);

            // Phase 1: Empty the stock (order the entire quantity)
            logger.debug("Phase 1: Selecting category '{}' to order all stock of '{}'", category, productName);
            orderPage.selectCategoryByVisibleText(category);
            Thread.sleep(1500);

            int currentStock = orderPage.getProductStock(productName);
            logger.info("Current stock for '{}' is {}. Ordering all of it.", productName, currentStock);

            orderPage.addProductByName(productName);
            Thread.sleep(1000);

            orderPage.setOrderItemQuantity(0, currentStock);
            orderPage.submitOrder();

            Thread.sleep(2000);

            // Phase 2: Navigate away and back to force SPA component re-render
            logger.debug("Phase 2: Navigating to Order History and back to force UI remount without losing state.");
            orderPage.header().clickOrderHistory();
            Thread.sleep(1500);

            orderPage.header().clickNewOrder();
            Thread.sleep(1500);

            logger.debug("Selecting category again: {}", category);
            orderPage.selectCategoryByVisibleText(category);
            Thread.sleep(1500);

            logger.debug("Toggling 'In stock only' filter.");
            orderPage.toggleInStockOnly();

            Thread.sleep(1500);

            List<String> displayedProducts = orderPage.getDisplayedProductNames();

            // Check if the depleted product is still displayed
            boolean isProductDisplayed = false;
            for (String name : displayedProducts) {
                if (name.equalsIgnoreCase(productName)) {
                    isProductDisplayed = true;
                    break;
                }
            }

            assertTrue("Validation failed: Out of stock product '" + productName + "' is still displayed after applying 'In stock only' filter.", !isProductDisplayed);
            logger.info("Success: Out of stock product '{}' was successfully hidden by the filter.", productName);

        } catch (AssertionError e) {
            logger.error("Validation failed for In-Stock filter test: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during In-Stock filter test: {}", e.getMessage(), e);
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