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
import java.util.logging.Level;
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
  private long activeTwitId;
  private RequestToken tempRequestToken;
  private long tempRequestId;

  /**
   * Feed TwitBot its parent Bot and runtime properties.
   *
   * @param bot
   * @param twitConfig
   */
  public TwitBot(PircBot bot, Properties twitConfig) {
    log.debug("TwitBot started...");
    AccessToken token;
    this.bot = bot;
    this.twitConfig = twitConfig;
    this.activeTwitId = -1;
    this.activeTwitName = null;
    this.tempRequestId = -1;
    this.tempRequestToken = null;
    twitter = TwitterFactory.getSingleton();
    twitter.setOAuthConsumer(twitConfig.getProperty("consumer.key"), twitConfig.getProperty("consumer.secret"));
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
  private String storeAccessToken(long userId, AccessToken accessToken) throws IOException {
    String name = accessToken.getScreenName();
    log.info("Storing token for @" + accessToken.getScreenName());
    FileWriter proFile = new FileWriter("@" + name + ".properties");
    Properties props = new Properties();
    props.setProperty("accessToken.name", name);
    props.setProperty("accessToken.id", Long.toString(userId));
    props.setProperty("accessToken.token", accessToken.getToken());
    props.setProperty("accessToken.secret", accessToken.getTokenSecret());
    props.store(proFile, null);
    log.info("Token stored for @" + name);
    return name;
  }

  /**
   * Loads access token for given @profile.
   *
   * @param profile
   * @return name of profile just activated
   * @throws IOException If token file could not be read
   * @throws TokenMismatchException If supplied name did not match stored name
   * @throws Main.MissingRequiredPropertyException If token file was incomplete
   * @throws TwitterException If something went wrong with Twitter
   */
  public String loadAccessToken(String profile) throws IOException, TokenMismatchException, Main.MissingRequiredPropertyException, TwitterException {
    log.info("Loading token for @" + profile);
    FileReader proFile = new FileReader("@" + profile + ".properties");
    Properties props = new Properties();
    props.load(proFile);
    if (!props.getProperty("accessToken.name").equals(profile)) { //no reason we should fetch @bar if we wanted @foo.properties
      throw new TokenMismatchException(profile, props.getProperty("accessToken.name"));
    }
    this.sanityCheckToken(props); //throws MissingRequiredPropertyException
    log.info("Token for @" + profile + " is sane");
    String token = props.getProperty("accessToken.token");
    String tokenSecret = props.getProperty("accessToken.secret");
    this.setAccessToken(new AccessToken(token, tokenSecret));
    return twitter.verifyCredentials().getScreenName();
  }

  private void setAccessToken(AccessToken accessToken) throws TwitterException {
    twitter.setOAuthAccessToken(accessToken);
    User user = twitter.verifyCredentials();
    activeTwitId = user.getId();
    activeTwitName = user.getScreenName();
    log.info("Active Twitter profile set to @" + activeTwitName + " id:" + activeTwitId);
  }

  public void clearAccessToken() {
    twitter.setOAuthAccessToken(null);
    activeTwitId = -1;
    activeTwitName = null;
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

  public long getActiveTwitId() {
    return activeTwitId;
  }

  public long getRequestId() {
    return tempRequestId;
  }

  /**
   * Generates a new requestToken. Starts request process
   *
   * @return authorization URL to direct user to
   * @throws RequestCancelledException
   * @throws RequestInProgressException
   */
  public String requestNewToken() throws RequestInProgressException, RequestCancelledException {
    if (tempRequestToken != null) {
      //we already have a request going
      throw new RequestInProgressException();
    }
    try {
      RequestToken requestToken = twitter.getOAuthRequestToken();
      String authUrl = requestToken.getAuthorizationURL();
      tempRequestToken = requestToken;
      tempRequestId = requestToken.hashCode();
      log.info("Request token " + tempRequestId + " was generated at: " + authUrl);
      return authUrl;
    } catch (TwitterException ex) {
      cancelNewToken();
      throw new RequestCancelledException();
    }
  }

  /**
   * Exchanges requestToken for accessToken given authorization PIN.
   *
   * @param pin the 7-digit number from the authorization process
   * @return username we just authorized for
   * @throws RequestCancelledException
   */
  public String confirmNewToken(String pin) throws RequestCancelledException {
    try {
      AccessToken accessToken = twitter.getOAuthAccessToken(tempRequestToken, pin);
      //at this point we either threw TwitterException or have authorized
      //and are now acting as this token
      long userId = accessToken.getUserId();
      String name = accessToken.getScreenName();
      log.info("Successfully got accessToken for @" + name + " id:" + userId);
      this.storeAccessToken(userId, accessToken);
      this.setAccessToken(accessToken);
      return twitter.verifyCredentials().getScreenName();
    } catch (TwitterException ex) {
      if (ex.getStatusCode() == 401) { //UNAUTHORIZED
        log.error("PIN was not authorized");
      } else {
        log.error("Twitter exception");
      }
      cancelNewToken();
      throw new RequestCancelledException(); //to be caught by command
    } catch (IOException ex) {
      java.util.logging.Logger.getLogger(TwitBot.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public void cancelNewToken() {
    tempRequestId = -1;
    tempRequestToken = null;
    log.info("Token request cancelled");
  }

  public static class RequestInProgressException extends Exception {

    public RequestInProgressException() {
      super();
      log.error("Token request already in progress");
    }
  }

  public static class RequestCancelledException extends Exception {

    public RequestCancelledException() {
      super();
      log.error("Token request cancelled");
    }
  }

  public Status tweet(String message) throws NoProfileLoadedException, TweetTooLongException, TwitterException {
    if (activeTwitId == -1) {
      throw new NoProfileLoadedException();
    }
    if (message.length() >= 140) {
      throw new TweetTooLongException();
    }
    return twitter.updateStatus(message);
  }

  public static class NoProfileLoadedException extends Exception {

    public NoProfileLoadedException() {
      super();
      log.error("No profile loaded");
    }
  }

  public static class TweetTooLongException extends Exception {

    public TweetTooLongException() {
      super();
      log.error("Tweet is too long");
    }
  }

}
