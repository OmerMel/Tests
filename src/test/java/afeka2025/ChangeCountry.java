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

import junit.framework.Assert;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import org.openqa.selenium.support.ui.Select;



import java.util.Collections;

public class ChangeCountry {
	
	

	private WebDriver driver;
	  private Map<String, Object> vars;
	  JavascriptExecutor js;
	  
	  @After
	  public void tearDown() {
	    driver.quit();
	  }
	
	
	  @Before
	  public void setUp() throws IOException {
	//	System.setProperty("webdriver.chrome.driver","C:\\Users\\acer\\Downloads\\chromedriver_win32\\chromedriver.exe");
	    driver = new ChromeDriver();
	    js = (JavascriptExecutor) driver;
	    vars = new HashMap<String, Object>();
	    
			    
	    
	  }
	  
	  @Test
	  public void simple() throws InterruptedException {
		  
		  
		  driver.get("http://demo.guru99.com/test/newtours/register.php");
		    driver.manage().window().setSize(new Dimension(1004, 724));
	    
	    
	   

		Select drpCountry = new Select(driver.findElement(By.name("country")));
		
		
		
		List<WebElement> options=drpCountry.getOptions();
		
		
				
		Iterator<WebElement> itr = options.iterator();
		while(itr.hasNext()) {
			    System.out.println(itr.next().getText());

		  }
			
			
			ArrayList<String> originalList=new ArrayList<String>();

			for(int i=0;i<options.size();i++)

			{

			originalList.add(options.get(i).getText());
			}
			

ArrayList<String> copiedList =new ArrayList<String>();


for(int i=0;i<originalList.size();i++)

{

copiedList.add(originalList.get(i));

}

Collections.sort(copiedList);

//Collections.reverse(copiedList);


for(String s1:originalList )

{

System.out.println(s1);

}
System.out.println("*******************");
for(String s2:copiedList )

{

System.out.println(s2);

}

if (originalList.equals(copiedList)) System.out.println(" ok!!!"); else System.out.println(" NOTTT  ok!!!") ;

		
	    
	    
	    
	  }
	
	
	  public static void main(String args[]) {
		  JUnitCore junit = new JUnitCore();
		  junit.addListener(new TextListener(System.out));
		  org.junit.runner.Result result = junit.run(ChangeCountry.class); // Replace "SampleTest" with the name of your class
		  if (result.getFailureCount() > 0) {
		    System.out.println("Test failed.");
		    System.exit(1);
		  } else {
		    System.out.println("Test finished successfully.");
		    System.exit(0);
		  }
		}
	
	
	
	
	

}
