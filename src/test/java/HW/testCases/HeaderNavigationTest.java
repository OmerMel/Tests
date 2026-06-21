package HW.testCases;

import HW.pages.HomePage;
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

public class HeaderNavigationTest {

    private static final Logger logger = LogManager.getLogger(HeaderNavigationTest.class);
    private WebDriver driver;
    private JSONObject navData;
    private HomePage homePage;

    @Before
    public void setUp() {
        logger.info("Initializing WebDriver and loading navigation test data.");

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Loading test data from JSON
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("src/test/resources/navigation_data.json");
            navData = (JSONObject) jsonParser.parse(reader);
        } catch (Exception e) {
            logger.error("Failed to load navigation_data.json", e);
        }

        driver.get("https://nano-flow-order-direct.base44.app/");
        homePage = new HomePage(driver);
    }

    @Test
    public void testHeaderNavigationDataDriven() throws InterruptedException {
        JSONArray navArray = (JSONArray) navData.get("navItems"); // נניח שהגדרנו מערך בשם navItems ב-JSON

        for (int i = 0; i < navArray.size(); i++) {
            JSONObject obj = (JSONObject) navArray.get(i);
            String buttonName = (String) obj.get("button");
            String expectedPath = (String) obj.get("expectedPath");

            logger.info("Testing navigation to: {}", buttonName);

            try {
                switch (buttonName) {
                    case "newOrder": homePage.header().clickNewOrder(); break;
                    case "orderHistory": homePage.header().clickOrderHistory(); break;
                    case "returns": homePage.header().clickReturns(); break;
                    case "home" : homePage.header().clickHome(); break;
                    default: logger.error("Button '{}' not found in switch case", buttonName);
                }

                Thread.sleep(1000);
                String currentUrl = driver.getCurrentUrl();
                logger.debug("Current URL after clicking {}: {}", buttonName, currentUrl);

                assertTrue("Navigation to " + buttonName + " failed! Expected: " + expectedPath,
                        currentUrl.contains(expectedPath));

            } catch (AssertionError e) {
                logger.error("Navigation validation failed for {}: {}", buttonName, e.getMessage());
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