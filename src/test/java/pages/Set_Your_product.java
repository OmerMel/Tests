package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Set_Your_product {
	
	 WebDriver driver;
	 
	  

	    public Set_Your_product(WebDriver driver) {
	        this.driver = driver;
	       

	    }

	    public Product_page setProd(String YourProd)
	    {
	        if(YourProd.equalsIgnoreCase("backpack"))
	        {
	           return new backpak_prod_page(driver);
	        }
	        if(YourProd.equalsIgnoreCase("lightbike"))
	        {
	            return new bikelight_prod_page(driver);
	        }
	        return null;
	    }
	    
	   
	    	
	    	
	 

}
