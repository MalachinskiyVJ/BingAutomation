import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.List;

public class BingTest {
    private EventFiringWebDriver driver;
    private int minImagesAmount;

    @Parameters(value = "image-amount")
    @BeforeClass
    public void getParams(int value) {
        minImagesAmount = value;
    }

    @DataProvider(name = "keywords")
    public Object[][] data() throws Exception {
        HashMap<String, String[]> dataSet = new TestData(System.getProperty("user.dir") + "\\testdata.txt").getData();

        String bingSearchStrings[] = dataSet.get("bingSearch");

        int size = bingSearchStrings.length;

        Object[][] creds = new Object[size][1];
        for (int i = 0; i < size; i++) {
            creds[i][0] = bingSearchStrings[i];

        }
        return creds;

    }

    @BeforeTest
    public void setUp() throws Exception {
        //String property = System.getProperty(("user.dir") + "\\geckodriver.exe");
        //System.setProperty("webdriver.gecko.driver",property);
        System.setProperty("webdriver.firefox.marionette", ("user.dir") + "\\geckodriver.exe");
        driver = new EventFiringWebDriver(new FirefoxDriver());
        driver.register(new EventHandler());


        //driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    }

    @Test(dataProvider = "keywords", description = "Bing_Test")
    public void search(String bingSearch) throws Exception {
        //1. Открыть главную страницу поисковой системы Bing.
        driver.get("https://www.bing.com/");

        /*2. Перейти в раздел поиска изображений. Дождаться, что заголовок страницы имеет
        название “Лента изображений Bing.”*/
        driver.findElement(By.xpath("//a[@id='scpl1']")).click();
        (new WebDriverWait(driver, 10)).until(ExpectedConditions.titleIs("Bing Image Feed"));
        Assert.assertEquals(driver.getTitle(), "Bing Image Feed");

        /*3. Выполнить прокрутку страницы несколько раз. Каждый раз проверять, что при достижении низа страницы
        подгружаются новые блоки с изображениями*/
        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("window.scrollBy(0,500)", "");
        List<WebElement> firstscroll = (new WebDriverWait(driver, 10)).
                until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("mimg")));
        System.out.println("The number of search elements after first scroll: " + firstscroll.size());

        js.executeScript("window.scrollBy(0,2500)", "");
        List<WebElement> secondscroll = (new WebDriverWait(driver, 10)).
                until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("mimg")));
        System.out.println("The number of search elements after second scroll: " + secondscroll.size());
        Assert.assertTrue(firstscroll.size() <= secondscroll.size());

        js.executeScript("window.scrollBy(0,-3000)", "");
        List<WebElement> scrollup = (new WebDriverWait(driver, 10)).
                until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("b_searchbox")));

        //4. Выполнять поиск изображений по ключевым словам, которые будут браться из текстового файла.
        WebElement search = driver.findElement(By.className("b_searchbox"));
        search.sendKeys(bingSearch);
        WebElement submitButton = driver.findElement(By.xpath("//input[contains(@type, 'submit') and contains(@class, 'b_searchboxSubmit')]"));
        submitButton.click();

        /*5. После загрузки страницы с результатами поиска навести курсор на картинку. Проверить,что отобразилась рамка
        с увеличенным изображением. Под картинкой доступны кнопки добавления в коллекцию, поиска по изображению и сообщения о нарушении*/
        Actions action = new Actions(driver);
        WebElement firstimage = driver.findElement(By.xpath(".//*[@id='dg_c']/div[1]/div/div[1]/div/a/img"));
        (new WebDriverWait(driver, 10)).
                until(ExpectedConditions.elementToBeClickable(firstimage));
        action.moveToElement(firstimage).build().perform();
        (new WebDriverWait(driver, 10)).
                until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@role='presentation']")));

        WebElement collection = driver.findElement(By.xpath("//span[@class='favC']"));
        WebElement imagematch = driver.findElement(By.xpath("//img[@class='ovrf ovrfIconMS' and @alt='Image match']"));
        WebElement markasadult = driver.findElement(By.xpath("//img[@class='ovrf ovrfIconFA' and @alt='Mark as Adult']"));
        Assert.assertTrue(collection.isDisplayed());
        Assert.assertTrue(imagematch.isDisplayed());
        Assert.assertTrue(markasadult.isDisplayed());

        //6. Нажать на кнопку поиска по изображению и дождаться загрузки слайдшоу
        imagematch.click();
        (new WebDriverWait(driver, 10)).
                until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[@id='iol_fsc']")));

        /*7. Воспользоваться кнопкой “Смотреть другие изображения” в блоке “Связанные изображения”. Проверить количество
        подгружаемых связанных изображений. Значение минимального количества картинок передавать в тест используя
        @Parameters.*/
        js.executeScript("window.scrollBy(0,500)", "");
        WebElement seemorebutton = driver.findElement(By.xpath(".//*[@id='mmComponent_images_4_1_1_exp']/span"));
        seemorebutton.click();
        List<WebElement> relatedimages = (new WebDriverWait(driver, 10)).
                until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("mimg")));
        System.out.println("Amount of related images: " + relatedimages.size());
        System.out.println("Minimum amount of related images " + minImagesAmount);
        Assert.assertTrue(minImagesAmount <= relatedimages.size());

    }

    @AfterTest
    public void tearDown() throws Exception {
        driver.quit();
    }


}
