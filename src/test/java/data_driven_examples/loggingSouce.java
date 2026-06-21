package data_driven_examples;




import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;

import org.apache.logging.log4j.*;

public class loggingSouce {
	
public static WebDriver driver;
	
    public static void main(String[] args) {
    	
         // TODO Auto-generated method stub
    	//System.setProperty("webdriver.chrome.driver","C:\\Users\\acer\\Downloads\\chromedriver_win32\\chromedriver.exe");

    	driver = new ChromeDriver();
	   

         //Logger log = Logger.getLogger("devpinoyLogger");
         Logger logger=LogManager.getLogger(LoggingDemo.class);
         
         driver.get("https://www.saucedemo.com");
		 logger.info("opening webiste");
       //  driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		 logger.debug("entring user name");
		
		 driver.findElement(By.id("user-name")).sendKeys("standard_user");
         logger.debug("entering password");
        
         
         driver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys("secret_sauce");
         
         logger.debug("hitting Button");
         
        
         driver.findElement(By.xpath("//*[@id=\"login-button\"]")).click();
         
		driver.quit();
	}

}
