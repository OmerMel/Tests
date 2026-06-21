package HW.components;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HeaderComponent {

    private WebDriver driver;

    @FindBy(id = "nav-home")
    private WebElement navHome;

    @FindBy(id = "nav-new-order")
    private WebElement navNewOrder;

    @FindBy(id = "nav-order-history")
    private WebElement navOrderHistory;

    @FindBy(id = "nav-returns")
    private WebElement navReturns;

    public HeaderComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void clickHome() {
        navHome.click();
    }

    public void clickNewOrder() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // המתנה עד שהכפתור יהיה לחיץ (גם קיים ב-HTML וגם גלוי על המסך)
        wait.until(ExpectedConditions.elementToBeClickable(navNewOrder));

        navNewOrder.click();
    }

    public void clickOrderHistory() {
        navOrderHistory.click();
    }

    public void clickReturns() {
        navReturns.click();
    }
}
