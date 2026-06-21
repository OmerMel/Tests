package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Add_to_cart_sauce_demo {
	
	
WebDriver driver;
	
	//Constructor that will be automatically called as soon as the object of the class is created
	public  Add_to_cart_sauce_demo(WebDriver driver) {
          this.driver = driver;
	}
	
	
	//Locator for backpack item
		By bckpck = By.cssSelector("#add-to-cart-sauce-labs-backpack");
		
		
		//Method to click on backpack button
		public void clickbackpack() {
			driver.findElement(bckpck).click();
		}
		
		//locator for link of bakpak
		
		By bcpakLink=By.cssSelector("#item_4_title_link > div");
		/*
		//locator for link of bikelight
		
		By bikelightLink=By.cssSelector("#item_0_title_link > div");
				
				
		//Method to click on backpack link
		public void clickbackpacklink() {
					driver.findElement(bcpakLink).click();
				}
		
		
		//Method to click on bikelight link
				public void clickbikelightlink() {
							driver.findElement(bikelightLink).click();
						}*/
	
	
	//method for pressing product link. calculte css locator on the fly
	
	public void press_link_prod_page (String YourProd)
	
	{
		String my_link="#item_";
		
		if(YourProd.equalsIgnoreCase("backpack")) {my_link=my_link+"4"+"_title_link > div";}
    	if(YourProd.equalsIgnoreCase("lightbike")) {my_link=my_link+"0"+"_title_link > div";}
		
    	By mylinkCss=By.cssSelector(my_link);
    	
    	driver.findElement(mylinkCss).click();
		
	}
				
		//method for more generic button press. calculate css locator on the fly
				
			   public void click_add_to_cart(String YourProd)
				    {
				    	String bunLocator="#add-to-cart-sauce-labs-";
				    	if(YourProd.equalsIgnoreCase("backpack")) {bunLocator=bunLocator.concat("backpack");}
				    	if(YourProd.equalsIgnoreCase("lightbike")) {bunLocator=bunLocator.concat("bike-light");}
				    	
				    	By myButton=By.cssSelector(bunLocator);
				    	driver.findElement(myButton).click();
				    	
				    }

}
