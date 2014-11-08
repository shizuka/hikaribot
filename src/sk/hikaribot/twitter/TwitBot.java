/*
 * hikaribot - TwitBot
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.PircBot;
import sk.hikaribot.bot.Main;
import twitter4j.*;
import twitter4j.auth.*;

/**
 * Provides bot-level Twitter API object for bot commands to call on.
 */
public class TwitBot {

  protected static final Logger log = LogManager.getLogger("Twit");

  private static final String[] reqTokenProps = {
    "accessToken.name",
    "accessToken.id",
    "accessToken.token",
    "accessToken.secret"
  };

  public final Twitter twitter;
  private final PircBot bot;
  private final Properties twitConfig;

  private String activeTwitName;
  private int activeTwitId;

  /**
   * Feed TwitBot its parent Bot and runtime properties.
   *
   * @param bot
   * @param twitConfig
   */
  public TwitBot(PircBot bot, Properties twitConfig) {
    log.trace("TwitBot started...");
    AccessToken token;
    this.bot = bot;
    this.twitConfig = twitConfig;
    twitter = TwitterFactory.getSingleton();
    twitter.setOAuthConsumer(twitConfig.getProperty("consumer.key"), twitConfig.getProperty("consumer.secret"));
  }

  /**
   * Sets the active AccessToken.
   *
   * @param profile
   * @throws TokenMismatchException
   * @throws IOException
   * @throws sk.hikaribot.bot.Main.MissingRequiredPropertyException
   */
  public void setProfile(String profile) throws TokenMismatchException, IOException, Main.MissingRequiredPropertyException {
    AccessToken token = this.loadAccessToken(profile); //will also set activeTwitName and activeTwitId
    twitter.setOAuthAccessToken(token);
    log.info("Active Twitter profile set to @" + activeTwitName + " id:" + activeTwitId);
  }

  /* Bot dies if sanity check fails */
  private void sanityCheckToken(Properties props) throws Main.MissingRequiredPropertyException {
    for (String prop : reqTokenProps) {
      if (props.getProperty(prop) == null) {
        throw new Main.MissingRequiredPropertyException(prop);
      }
    }
  }

  /* Writes AccessToken to @profile.properties */
  private String storeAccessToken(int useId, AccessToken accessToken) throws IOException {
    String name = accessToken.getScreenName();
    log.debug("Storing token for @" + accessToken.getScreenName());
    FileWriter proFile = new FileWriter("@" + name + ".properties");
    Properties props = new Properties();
    props.setProperty("accessToken.name", name);
    props.setProperty("accessToken.id", Integer.toString(useId));
    props.setProperty("accessToken.token", accessToken.getToken());
    props.setProperty("accessToken.secret", accessToken.getTokenSecret());
    props.store(proFile, null);
    log.debug("Token stored for @" + name);
    return name;
  }

  /* Reads AccessToken from @profile.properties */
  private AccessToken loadAccessToken(String profile) throws IOException, TokenMismatchException, Main.MissingRequiredPropertyException {
    log.debug("Loading token for @" + profile);
    FileReader proFile = new FileReader("@" + profile + ".properties");
    Properties props = new Properties();
    props.load(proFile);
    if (!props.getProperty("accessToken.name").equals(profile)) { //no reason we should fetch @bar if we wanted @foo.properties
      throw new TokenMismatchException(profile, props.getProperty("accessToken.name"));
    }
    sanityCheckToken(props); //throws MissingRequiredPropertyException
    log.debug("Token for @" + profile + " is sane");
    this.activeTwitName = props.getProperty("accessToken.name");
    this.activeTwitId = Integer.parseInt(props.getProperty("accessToken.id"));
    String token = props.getProperty("accessToken.token");
    String tokenSecret = props.getProperty("accessToken.secret");
    return new AccessToken(token, tokenSecret);
  }

  public static class TokenMismatchException extends Exception {

    public TokenMismatchException(String expected, String got) {
      super(expected);
      log.error("Token mismatch! Accessed @" + expected + " but was reported as @" + got);
    }
  }

  public String getActiveTwitName() {
    return activeTwitName;
  }

  public int getActiveTwitId() {
    return activeTwitId;
  }

}
