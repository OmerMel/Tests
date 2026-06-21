package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class SearchGooglFromExl_page {
	
WebDriver driver;
	
	//Constructor that will be automatically called as soon as the object of the class is created
	public  SearchGooglFromExl_page(WebDriver driver) {
          this.driver = driver;
	}

	
	By qName=By.name("q");
	
	
	//Method to enter username
		public void enterSearchQuery(String query) {
			driver.findElement(qName).sendKeys(query);
			driver.findElement(qName).sendKeys(Keys.ENTER);
		}
}
