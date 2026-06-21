package pom_test_cases;

import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.Before;

import org.junit.After;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Dimension;
import java.io.IOException;
import pages.Login_pom_sauce_demo;

import pages.Add_to_cart_sauce_demo;
import pom_test_cases.base_test_class;

import pages.Set_Your_product;

//import pages.backpak_prod_page;


public class Login_display_prod_description {
	
	
	private WebDriver driver;
	
	
	 @After
	  public void tearDown() {
	  //  driver.quit();
	  }
	
	
	  @Before
	  public void setUp() throws IOException {
		//System.setProperty("webdriver.chrome.driver","C:\\Users\\acer\\Downloads\\chromedriver_win32\\chromedriver.exe");
	   // driver = new ChromeDriver();
		  driver =base_test_class.initializeDriver();
		 
	    
			    
	    
	  }
	    
	   
	  
	    
	    
	  
	 
	  
	  
	  
	  @Test
	  public void simple() throws InterruptedException {
		  
		  
	    driver.get("https://www.saucedemo.com");
	    driver.manage().window().setSize(new Dimension(1004, 724));
	    
	    Login_pom_sauce_demo LoginPage= new Login_pom_sauce_demo (driver);
	    
	     
	    
	  LoginPage.enterUsername("standard_user");
	
	  	  
	  LoginPage.enterPassword("secret_sauce");
		   
		  
	  LoginPage.clickLogin();
	  
	  Thread.sleep(1500);
	  
	 //get current url for the cart test case-not a must
	//  driver.get(driver.getCurrentUrl());
	  Thread.sleep(3000);
	  
	 Add_to_cart_sauce_demo AddTocrt = new Add_to_cart_sauce_demo(driver);
	  
     AddTocrt.press_link_prod_page("backpack");
	 
	// AddTocrt.clickbikelightlink();
     
     Thread.sleep(3000);
     
     
	
  

    Set_Your_product syp= new Set_Your_product(driver);
	  
	 syp.setProd("backpack").display_title();
	 syp.setProd("backpack").display_desc(); 
	 syp.setProd("backpack").click_add_to_cart("backpack");
			
	 //AddTocrt.click_add_to_cart("lightbike");					
	   
			
		  
		  }
	  
	  public static void main(String args[]) {
		  JUnitCore junit = new JUnitCore();
		  junit.addListener(new TextListener(System.out));
		  org.junit.runner.Result result = junit.run(Login_display_prod_description.class); // Replace "SampleTest" with the name of your class
		  if (result.getFailureCount() > 0) {
		    System.out.println("Test failed.");
		    System.exit(1);
		  } else {
		    System.out.println("Test finished successfully.");
		    System.exit(0);
		  }
		}
	}