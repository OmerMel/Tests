package tests;

import data_driven_examples.LoggingDemo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class test1 {

    private WebDriver driver;
    private Map<String, Object> vars;
    private JSONArray users;
    JavascriptExecutor js;

    @Before
    public void setUp() throws IOException, ParseException {
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();

        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader;
            reader = new FileReader("login.json");
            //Read JSON file
            users = (JSONArray)jsonParser.parse(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void simple() throws IOException, InterruptedException {

        Logger logger= LogManager.getLogger(LoggingDemo.class);

        logger.info("Opening website");
        driver.get("https://www.demoblaze.com/");
        // 2 | setWindowSize | 1599x961 |
        driver.manage().window().setSize(new Dimension(1599, 961));



        for (Object user : users) {
            JSONObject obj = (JSONObject) user;

            //Click on login button
            driver.findElement(By.id("login2")).click();
            logger.info("Login Clicked");

            //Wait for the pop-up
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            logger.debug("Entering username");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginusername"))).sendKeys((String) obj.get("username"));

            logger.debug("Entering password");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginpassword"))).sendKeys((String) obj.get("password"));

            //Login button
            logger.info("Clicking login (inside pup-up)");
            driver.findElement(By.xpath("//*[@id=\"logInModal\"]/div/div/div[3]/button[2]")).click();


            Thread.sleep(2000);
            //check if logout exists
            if (driver.findElement(By.id((String) obj.get("expectedResult"))).isDisplayed()) {
                System.out.println("test passed");
                logger.debug("Test passed");
            } else {
                System.out.println("test failed");
                logger.debug("Test failed");
            }

            driver.findElement(By.id((String) obj.get("expectedResult"))).click(); //logout
//            driver.get("https://www.demoblaze.com/");
//            Thread.sleep(1000);
        }
    }
}
