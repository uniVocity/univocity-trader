package com.univocity.trader.exchange.tdameritrade.api.auth;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TDAAuthenticator {
    private String authURL;
    private String redirectURL;
    private String encodedRedirectURL;
    private String encodedConsumerKey;
    private int timeOutSeconds;
    private WebDriver driver;
    private String authCode;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String localAuthToken;


    public TDAAuthenticator(String redirectURL, String consumerKey, String driverPath) {
        try {
            System.setProperty("webdriver.gecko.driver", driverPath);
            this.redirectURL = redirectURL;
            this.encodedRedirectURL = URLEncoder.encode(redirectURL, "UTF-8");
            this.encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
            this.authCode = MessageFormat.format("https://auth.tdameritrade.com/auth?response_type=code&redirect_uri={0}&client_id={1}%40AMER.OAUTHAP",
            this.encodedRedirectURL, this.encodedConsumerKey);
            this.timeOutSeconds = 1000;
            driver = new FirefoxDriver();
        } catch (Exception e){
           logger.error("[Univocity TDA] Error constructing TDAuthenticator: " + e.getMessage());
        }
    }

    public String getAuthCode() {
        driver.get(this.authCode);
        WebDriverWait wait = new WebDriverWait(driver, 120);
        String encodedAuthToken = null;
        try {
            wait.until(ExpectedConditions.urlContains(this.redirectURL));
            Pattern authCodePattern = Pattern.compile("(code=)(.*)");
            Matcher matcher = authCodePattern.matcher(driver.getCurrentUrl());
            if (matcher.find())
                encodedAuthToken = matcher.group(2);
        } catch(Exception e) {
            logger.error(e.getMessage());
        } finally {
            driver.close();
        }
        if(encodedAuthToken != null)
            this.localAuthToken = URLDecoder.decode(encodedAuthToken, StandardCharsets.UTF_8);
        return this.localAuthToken;
    }

    public void setTimeOutSeconds(int timeOutSeconds) {
        this.timeOutSeconds = timeOutSeconds;
    }
}
