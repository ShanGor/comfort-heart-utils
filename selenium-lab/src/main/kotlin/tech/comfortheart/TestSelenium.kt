package tech.comfortheart


import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated
import org.openqa.selenium.support.ui.WebDriverWait
import ru.yandex.qatools.ashot.AShot
import ru.yandex.qatools.ashot.shooting.ShootingStrategies
import tech.comfortheart.util.CommandRunner
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO


/**
 * Take a screenshot and save as png.
 */
class App {}

fun main() {
    val geckoPath = App::class.java.classLoader.getResource("geckodriver-mac").file
    print("Gecko is in: $geckoPath")
    System.setProperty("webdriver.gecko.driver", geckoPath)
    val driver = FirefoxDriver()
    val wait = WebDriverWait(driver, 10)
    driver.manage().window().maximize()
    try {
        driver.get("https://baidu.com")
        driver.findElement(By.id("kw")).sendKeys("cheese" + Keys.ENTER)
        val firstResult = wait.until(presenceOfElementLocated(By.cssSelector("h3>a")))
        println(firstResult.getAttribute("textContent"))
//        val ts = driver as TakesScreenshot
//        val source = ts.getScreenshotAs(OutputType.FILE)
//        Files.copy(source.toPath(), Paths.get("/tmp/screen.png"), StandardCopyOption.REPLACE_EXISTING)
//        println("Screenshot is taken!")
        val screenshot = AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver)
        try {
            ImageIO.write(screenshot.image, "PNG", File("/tmp/screen.png"))
        } catch (e: IOException) { // TODO Auto-generated catch block
            e.printStackTrace()
        }

        /**
         * Save as HTML. has some image data
         */
        Files.write(Paths.get("/tmp/screenshot.html"), driver.pageSource.toByteArray(), StandardOpenOption.CREATE)
        CommandRunner.runCommand("wkhtmltopdf /tmp/screenshot.html /tmp/screenshot.pdf")
    } finally {
        driver.quit()
    }
}

