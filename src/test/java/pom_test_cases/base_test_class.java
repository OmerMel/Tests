package pom_test_cases;


import org.openqa.selenium.WebDriver;

import org.openqa.selenium.chrome.ChromeDriver;




import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;

//import org.apache.poi.hssf.usermodel.HSSFWorkbook;

//import org.apache.poi.ss.usermodel.Row;

//import org.apache.poi.ss.usermodel.Sheet;

//import org.apache.poi.ss.usermodel.Workbook;


public class base_test_class {
	
	
	//public static int rowCount;
	//public static Sheet guru99Sheet;
	
	public static WebDriver initializeDriver()
    {
		//System.setProperty("webdriver.chrome.driver","C:\\Users\\acer\\Downloads\\chromedriver_win32\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        return driver;
    }
	
	



	

}
