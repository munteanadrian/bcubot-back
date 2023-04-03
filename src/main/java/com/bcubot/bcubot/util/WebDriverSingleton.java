package com.bcubot.bcubot.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class WebDriverSingleton {
    private static WebDriverSingleton instance;
    private final WebDriver driver;

    private WebDriverSingleton() {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        // start in headless mode
//            options.addArguments("--headless");
        // ignore some certificate errors
        options.addArguments("--ignore-certificate-errors");
        // start browser in full screen
        options.addArguments("--start-maximized");
        // remove "this browser is being controlled by automation software" message
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        // prevent browser from closing
        options.setExperimentalOption("detach", true);
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
    }

    public static WebDriverSingleton getInstance() {
        if (instance == null) {
            instance = new WebDriverSingleton();
        }

        return instance;
    }

    public WebDriver getDriver() {
        return driver;
    }
}
