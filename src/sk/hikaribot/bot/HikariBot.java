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
  private final Properties config;

  public HikariBot(Properties config) {
    this.config = config;
    this.setName(config.getProperty("nick"));
  }

  @Override
  protected void onMessage(String channel, String sender, String login, String hostname, String message) {
    message = Colors.removeFormattingAndColors(message);
    if (sender.equals("Shizuka") && message.equalsIgnoreCase("die")) {
      log.info("received die command");
      this.quitServer("Killed by " + sender);
    }
    if (sender.equals("Shizuka") && message.equalsIgnoreCase("time")) {
      log.info("received time command");
      String time = new java.util.Date().toString();
      this.sendMessage(channel, sender + ": current timestamp is " + time);
    }
  }

  @Override
  protected void onConnect() {
    this.identify(this.config.getProperty("pass"));
  }

  @Override
  protected void onDisconnect() {
    log.warn("Disconnected, exiting...");
    System.exit(0);
  }

}
