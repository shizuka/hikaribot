/*
 * Hikari IRC Bot - HikariBot
 * Shizuka Kamishima - 2014-11-06
 * Licensed under bsd3
 */

package sk.hikaribot.bot;

import java.util.Properties;
import org.jibble.pircbot.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class description
 */
public class HikariBot extends PircBot {
  
  private static final Logger log = LogManager.getLogger("Bot");
  
  public HikariBot(Properties config) {
    this.setName(config.getProperty("nick"));
  }

  @Override
  protected void onMessage(String channel, String sender, String login, String hostname, String message) {
    if (message.equalsIgnoreCase("time")) {
      String time = new java.util.Date().toString();
      sendMessage(channel, sender + ": The time is now " + time);
    }
    if (message.equalsIgnoreCase("die")) {
      this.disconnect();
      System.exit(0);
    }
  }
}
