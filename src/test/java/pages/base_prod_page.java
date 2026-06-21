package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import pages.Add_to_cart_sauce_demo;

public class base_prod_page<AddToCartBtn> {

	
WebDriver driver;

	
	//Constructor that will be automatically called as soon as the object of the class is created
	public  base_prod_page(WebDriver driver) {
          this.driver = driver;
         
	}
	
	//Locator for title
	
	By ProdTitle=By.cssSelector("#inventory_item_container > div > div > div.inventory_details_desc_container > div.inventory_details_name.large_size");
	
	//Locator for description
	
	By ProdDesc=By.cssSelector("#inventory_item_container > div > div > div.inventory_details_desc_container > div.inventory_details_desc.large_size");
	
	

	
	
	
	//Method for display title
	
	public void display_title() {
		System.out.println (driver.findElement(ProdTitle).getText());
	}
	
	
	//Method for display description
	
		public void display_desc() {
			System.out.println (driver.findElement(ProdDesc).getText());
		}
		
		
		//method for click button add to cart
		
		public void click_add_to_cart (String YourProd) {
			Add_to_cart_sauce_demo atcsd=new Add_to_cart_sauce_demo(driver);
			atcsd.click_add_to_cart(YourProd);
			
		}


		
	
	
}

