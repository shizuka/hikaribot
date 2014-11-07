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

}
