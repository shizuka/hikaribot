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
import sk.hikaribot.cmd.Command;

/**
 * Class description
 */
public class HikariBot extends PircBot {

  private static final Logger log = LogManager.getLogger("Bot");
  private final Properties config;
  protected final CommandRegistry cmdRegistry;

  public HikariBot(Properties config) {
    this.config = config;
    this.setName(config.getProperty("nick"));
    this.setVersion(config.getProperty("version"));
    this.cmdRegistry = new CommandRegistry(this, config.getProperty("delimiter"));
    /* register commands */
    cmdRegistry.add(new sk.hikaribot.cmd.Help());
    cmdRegistry.add(new sk.hikaribot.cmd.Die());
    cmdRegistry.add(new sk.hikaribot.cmd.Join());
    cmdRegistry.add(new sk.hikaribot.cmd.Part());
    cmdRegistry.add(new sk.hikaribot.cmd.Say());
    cmdRegistry.add(new sk.hikaribot.cmd.DoAction());
    cmdRegistry.add(new sk.hikaribot.cmd.Version());
  }

  /**
   * Handles PRIVMSG detected to start with command delimiter.
   *
   * @param channel
   * @param sender
   * @param message
   */
  private void command(String channel, String sender, String message) {
    int permission = this.getUserPermission(channel, sender);
    try {
      cmdRegistry.execute(channel, sender, permission, message);
    } catch (CommandRegistry.CommandNotFoundException ex) {
      if (permission > 0) { //only 404 if it was an op
        log.error("Command " + ex.getMessage() + " not found");
        this.sendMessage(channel, sender + ": I couldn't find command '" + ex.getMessage() + "'");
      }
    } catch (CommandRegistry.InsufficientPermissionsException ex) {
      log.error(sender + " in " + channel + " has insufficient permissions to invoke " + ex.getMessage());
      if (permission > 0) {
        this.sendMessage(channel, sender + ": You do not have permission to do that.");
      }
    } catch (Command.ImproperArgsException ex) {
      log.fatal("This should have been caught by help!");
      this.quitServer("Fatal exception");
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
    /* //disabled until i have better access control in place
    message = Colors.removeFormattingAndColors(message);
    if (message.startsWith(config.getProperty("delimiter"))) { //then it's a command
      this.command(sender, sender, message);
    }
    */
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

  @Override
  protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
    //we're ignoring finger
  }

  @Override
  protected void onIncomingChatRequest(DccChat chat) {
    //we're ignoring DCC
  }

  @Override
  protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
    //we're ignoring DCC
  }

  @Override
  protected void onIncomingFileTransfer(DccFileTransfer transfer) {
    //we're ignoring DCC
  }
  
  public User getUser(String channel, String nick) {
    User[] users = this.getUsers(channel);
    for (User user : users) {
      if (user.equals(nick)) {
        return user;
      }
    }
    return null;
  }

  public CommandRegistry getCommandRegistry() {
    return cmdRegistry;
  }

  public int getUserPermission(String channel, String nick) {
    User user = this.getUser(channel, nick);
    if (user.equals(config.getProperty("owner"))) {
      return 3; //owner
    } else if (user.isOp()) {
      return 2; //channel operator
    } else if (user.hasVoice()) {
      return 1; //is voiced
    } else {
      return 0; //normal user
    }
  }
}
