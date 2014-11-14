/*
 * hikaribot - TwitBot
 * Shizuka Kamishima - 2014-11-08
 * 
 * Copyright (c) 2014, Shizuka Kamishima
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package sk.hikaribot.twitter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.PircBot;
import sk.hikaribot.api.exception.MissingRequiredPropertyException;
import sk.hikaribot.api.exception.TokenMismatchException;
import twitter4j.*;
import twitter4j.auth.*;

/**
 * Provides bot-level Twitter API object for bot commands to call on.
 *
 * @author Shizuka Kamishima
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
  private void sanityCheckToken(Properties props) throws MissingRequiredPropertyException {
    for (String prop : reqTokenProps) {
      if (props.getProperty(prop) == null) {
        throw new MissingRequiredPropertyException(prop);
      }
    }
  }

  /* Writes AccessToken to @profile.properties */
  private String storeAccessToken(long userId, AccessToken accessToken) throws IOException {
    String name = accessToken.getScreenName().toLowerCase();
    log.debug("Storing token for " + accessToken.getScreenName());
    FileWriter proFile = new FileWriter("" + name + ".properties");
    Properties props = new Properties();
    props.setProperty("accessToken.name", name);
    props.setProperty("accessToken.id", Long.toString(userId));
    props.setProperty("accessToken.token", accessToken.getToken());
    props.setProperty("accessToken.secret", accessToken.getTokenSecret());
    props.store(proFile, null);
    log.debug("Token stored for " + name);
    return name;
  }

  /**
   * Loads access token for given @profile.
   *
   * @param profile
   * @return name of profile just activated
   * @throws IOException If token file could not be read
   * @throws TokenMismatchException If supplied name did not match stored name
   * @throws sk.hikaribot.api.exception.MissingRequiredPropertyException
   * @throws TwitterException If something went wrong with Twitter
   */
  public String loadAccessToken(String profile) throws IOException, TokenMismatchException, MissingRequiredPropertyException, TwitterException {
    profile = profile.toLowerCase();
    log.debug("Loading token for " + profile);
    FileReader proFile = new FileReader("@" + profile + ".properties");
    Properties props = new Properties();
    props.load(proFile);
    if (!props.getProperty("accessToken.name").equals(profile)) { //no reason we should fetch @bar if we wanted @foo.properties
      throw new TokenMismatchException(profile, props.getProperty("accessToken.name"));
    }
    this.sanityCheckToken(props); //throws MissingRequiredPropertyException
    log.debug("Token for " + profile + " is sane");
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
    log.debug("Active Twitter profile set to " + activeTwitName + " id:" + activeTwitId);
  }

  public void clearAccessToken() {
    twitter.setOAuthAccessToken(null);
    activeTwitId = -1;
    activeTwitName = null;
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
    if (pendingRequest()) {
      //we already have a request going
      throw new RequestInProgressException();
    }
    try {
      RequestToken requestToken = twitter.getOAuthRequestToken();
      String authUrl = requestToken.getAuthorizationURL();
      tempRequestToken = requestToken;
      tempRequestId = requestToken.hashCode();
      log.debug("Request token " + tempRequestId + " was generated at: " + authUrl);
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
      log.debug("Successfully got accessToken for " + name + " id:" + userId);
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
      log.error("Could not write to properties file! Profile will need to revoke AccessToken. Also fix your filesystem, damn.");
      cancelNewToken();
      throw new RequestCancelledException(); //to be caught by command
      //it masquerades as a failed authorization but oh well
    }
  }

  /**
   * Cancels pending RequestToken
   */
  public void cancelNewToken() {
    tempRequestId = -1;
    tempRequestToken = null;
    log.debug("Token request cancelled");
  }

  /**
   * @return is there a RequestToken pending confirmation?
   */
  public boolean pendingRequest() {
    return (tempRequestToken != null);
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

  /**
   * Posts a tweet.
   * @param message the message to tweet
   * @return Status object for tweet
   * @throws NoProfileLoadedException if no AccessToken is loaded
   * @throws TweetTooLongException if message was too long
   * @throws TwitterException if something went wrong sending the tweet
   */
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
