package com.univocity.trader.exchange.tdameritrade.api.auth;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TDAAuthenticator {
    private String authURL;
    private String redirectURL;
    private String encodedRedirectURL;
    private String encodedConsumerKey;
    private WebDriver driver;
    private String baseAuthURL;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public TDAAuthenticator(String redirectURL, String consumerKey, String driverPath) {
        try {
            System.setProperty("webdriver.gecko.driver", driverPath);
            this.redirectURL = redirectURL;
            this.encodedRedirectURL = URLEncoder.encode(redirectURL, "UTF-8");
            this.encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
            this.baseAuthURL = MessageFormat.format("https://auth.tdameritrade.com/auth?response_type=code&redirect_uri={0}&client_id={1}%40AMER.OAUTHAP",
                    this.encodedRedirectURL, this.encodedConsumerKey);
            driver = new FirefoxDriver();
        } catch (Exception e){
           logger.error("[Univocity TDA] Error constructing TDAuthenticator: " + e.getMessage());
        }
    }

    public String authenticateAndGetAuthCode() {
        driver.get(authURL);
        Pattern authCodePattern = Pattern.compile("code=[]]");
        if(driver.getCurrentUrl().startsWith(redirectURL)){
            Matcher matcher = authCodePattern.matcher(driver.getCurrentUrl());
            if(matcher.find()){
                return matcher.group(0);
            }
        }
        return null;
    }
}
