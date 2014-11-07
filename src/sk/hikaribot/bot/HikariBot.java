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
  private final CommandRegistry cmdRegistry = new CommandRegistry(this);

  public HikariBot(Properties config) {
    this.config = config;
    this.setName(config.getProperty("nick"));
    /* register commands */
    cmdRegistry.add(new sk.hikaribot.cmd.Help());
    cmdRegistry.add(new sk.hikaribot.cmd.Die());
  }

  private void command(String channel, String sender, String message) {
    int permission;
    User invoker = this.getUser(channel, sender);
    if (invoker.equals(config.getProperty("owner"))) {
      permission = 3; //owner
    } else if (invoker.isOp()) {
      permission = 2; //channel operator
    } else if (invoker.hasVoice()) {
      permission = 1; //is voiced
    } else {
      permission = 0; //normal user
    }
    //TODO - 0-banned 3-superuser 4-owner
    //maintain access lists of superusers and banned users

    try {
      cmdRegistry.execute(channel, sender, permission, message);
    } catch (CommandRegistry.CommandNotFoundException ex) {
      log.error("Command " + ex.getMessage() + " not found");
      this.sendMessage(channel, sender + ": I couldn't find command '" + ex.getMessage() + "'");
    } catch (CommandRegistry.InsufficientPermissionsException ex) {
      log.error(sender + " has insufficient permissions to invoke " + ex.getMessage());
      this.sendMessage(channel, sender + ": You do not have permission to do that.");
    }
  }

  @Override
  /**
   * Called when we get a message from a channel
   */
  protected void onMessage(String channel, String sender, String login, String hostname, String message) {
    message = Colors.removeFormattingAndColors(message);
    if (message.startsWith(config.getProperty("delimiter"))) { //then it's a command
      this.command(channel, sender, message);
    }
  }

  /**
   * Called when we get a PM or opchat
   */
  @Override
  protected void onPrivateMessage(String sender, String login, String hostname, String message) {
    message = Colors.removeFormattingAndColors(message);
    if (message.startsWith(config.getProperty("delimiter"))) { //then it's a command
      this.command(sender, sender, message);
    }
  }

  @Override
  protected void onConnect() {
    this.identify(this.config.getProperty("pass"));
  }

  @Override
  protected void onDisconnect() {
    log.info("Disconnected, exiting...");
    System.exit(0);
  }

  @Override
  protected void onUserList(String channel, User[] users) {
    log.info("Finished joining " + channel);
  }

  private User getUser(String channel, String nick) {
    User[] users = this.getUsers(channel);
    for (User user : users) {
      if (user.equals(nick)) {
        return user;
      }
    }
    return null;
  }
}
