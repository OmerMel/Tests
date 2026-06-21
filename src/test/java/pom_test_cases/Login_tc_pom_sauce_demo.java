package pom_test_cases;


import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.Before;

import org.junit.After;

import org.openqa.selenium.WebDriver;

import org.openqa.selenium.chrome.ChromeDriver;


import org.openqa.selenium.Dimension;




import java.io.IOException;





import pages.Login_pom_sauce_demo;
import pom_test_cases.base_test_class;





public class Login_tc_pom_sauce_demo {
	
	
	private WebDriver driver;
	//  private Map<String, Object> vars;
	//  JavascriptExecutor js;
	  
	 
	  
	   

	
	
	 @After
	  public void tearDown() {
	  //  driver.quit();
	  }
	
	
	  @Before
	  public void setUp() throws IOException {
		//System.setProperty("webdriver.chrome.driver","C:\\Users\\acer\\Downloads\\chromedriver_win32\\chromedriver.exe");
	  //  driver = new ChromeDriver();
	    
	    driver =base_test_class.initializeDriver();
	
	    
			    
	    
	  }
	    
	   
	  
	    
	    
	  
	 
	  
	  
	  
	  @Test
	  public void simple() throws InterruptedException {
		  
		  
	    driver.get("https://www.saucedemo.com");
	    driver.manage().window().setSize(new Dimension(1004, 724));
	    
	    Login_pom_sauce_demo LoginPage= new Login_pom_sauce_demo (driver);
	   
	    
	   // driver.findElement(By.id("user-name")).sendKeys("standard_user");
	    
	  LoginPage.enterUsername("standard_user");
	
	   // driver.findElement(By.id("password")).sendKeys("secret_sauce");
	  
	  LoginPage.enterPassword("secret_sauce");
		   
	  //  driver.findElement(By.id("login-button")).click();
	  
	  LoginPage.clickLogin();
	   
		
			
							
	   
			
		  
		  }
	  
	  public static void main(String args[]) {
		  JUnitCore junit = new JUnitCore();
		  junit.addListener(new TextListener(System.out));
		  org.junit.runner.Result result = junit.run(Login_tc_pom_sauce_demo.class); // Replace "SampleTest" with the name of your class
		  if (result.getFailureCount() > 0) {
		    System.out.println("Test failed.");
		    System.exit(1);
		  } else {
		    System.out.println("Test finished successfully.");
		    System.exit(0);
		  }
		}
	}