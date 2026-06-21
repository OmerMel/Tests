package afeka2025;






import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class Sort2OptionsMultCode {
  private WebDriver driver;
  
  private Map<String, Object> vars;
  JavascriptExecutor js;
  @Before
  public void setUp() throws IOException {
		//System.setProperty("webdriver.chrome.driver","C:\\Users\\acer\\Downloads\\chromedriver_win32\\chromedriver.exe");
	    driver = new ChromeDriver();
	    js = (JavascriptExecutor) driver;
	    vars = new HashMap<String, Object>();
	    
			    
	    
	  }
  @After
  public void tearDown() {
    driver.quit();
  }
  @Test
  public void untitled() {
    driver.get("https://www.saucedemo.com/inventory.html");
    driver.manage().window().setSize(new Dimension(1052, 666));
    driver.findElement(By.cssSelector(".product_sort_container")).click();
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
      WebElement dropdown = driver.findElement(By.cssSelector(".product_sort_container"));
      dropdown.findElement(By.xpath("//option[. = 'Name (Z to A)']")).click();
    
    
    List<WebElement> products=driver.findElements(By.cssSelector("div.inventory_item_name"));
	    
	    
		
	    
		Iterator<WebElement> itr = products.iterator();
		while(itr.hasNext()) {
		    System.out.println(itr.next().getText());

	  }
    
		 System.out.println("_________________________");
    
    
    
    
      dropdown.findElement(By.xpath("//option[. = 'Name (A to Z)']")).click();
    
    
    
     products=driver.findElements(By.cssSelector("div.inventory_item_name"));
    
    
	
    

    itr=products.iterator();
	while(itr.hasNext()) {
	    System.out.println(itr.next().getText());

  }
    
    
    
    
  /*     dropdown = driver.findElement(By.cssSelector(".product_sort_container"));
      dropdown.findElement(By.xpath("//option[. = 'Price (low to high)']")).click();
    
    driver.findElement(By.cssSelector(".product_sort_container")).click();
    driver.findElement(By.cssSelector(".product_sort_container")).click();
    driver.findElement(By.cssSelector(".product_sort_container")).click();*/
  }




	  public static void main(String args[]) {
		  JUnitCore junit = new JUnitCore();
		  junit.addListener(new TextListener(System.out));
		  org.junit.runner.Result result = junit.run(Sort2OptionsMultCode.class); // Replace "SampleTest" with the name of your class
		  if (result.getFailureCount() > 0) {
		    System.out.println("Test failed.");
		    System.exit(1);
		  } else {
		    System.out.println("Test finished successfully.");
		    System.exit(0);
		  }
		}
	}



