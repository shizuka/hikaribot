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
import sk.hikaribot.twitter.TwitBot;

/**
 * Our heroine, the Hikari IRC Bot.
 */
public class HikariBot extends PircBot {

  private static final Logger log = LogManager.getLogger("Bot");
  private final Properties config;
  protected final CommandRegistry cmdRegistry;
  public final TwitBot twit;
  private final long startMillis;

  /**
   * Start HikariBot with runtime properties.
   *
   * @param config config.properties settings
   * @param twitConfig twitbot.properties settings
   */
  public HikariBot(Properties config, Properties twitConfig) {
    this.startMillis = System.currentTimeMillis();
    log.debug("HikariBot started...");
    this.config = config;
    this.setName(config.getProperty("nick"));
    this.setVersion(config.getProperty("version"));
    this.cmdRegistry = new CommandRegistry(this, config.getProperty("delimiter"));
    this.twit = new TwitBot(this, twitConfig);
    /* register commands */
    log.info("Registering commands...");
    cmdRegistry.add(new sk.hikaribot.cmd.Verbose());
    cmdRegistry.add(new sk.hikaribot.cmd.NoVerbose());
    cmdRegistry.add(new sk.hikaribot.cmd.Help());
    cmdRegistry.add(new sk.hikaribot.cmd.Die());
    cmdRegistry.add(new sk.hikaribot.cmd.Join());
    cmdRegistry.add(new sk.hikaribot.cmd.Part());
    cmdRegistry.add(new sk.hikaribot.cmd.Say());
    cmdRegistry.add(new sk.hikaribot.cmd.Nick());
    cmdRegistry.add(new sk.hikaribot.cmd.DoAction());
    cmdRegistry.add(new sk.hikaribot.cmd.Version());
    cmdRegistry.add(new sk.hikaribot.cmd.Uptime());
    cmdRegistry.add(new sk.hikaribot.cmd.GetPermission());
    cmdRegistry.add(new sk.hikaribot.twitter.LoadProfile());
    cmdRegistry.add(new sk.hikaribot.twitter.UnloadProfile());
    cmdRegistry.add(new sk.hikaribot.twitter.GetActiveProfile());
    cmdRegistry.add(new sk.hikaribot.twitter.RequestNewToken());
    cmdRegistry.add(new sk.hikaribot.twitter.ConfirmNewToken());
    cmdRegistry.add(new sk.hikaribot.twitter.CancelNewToken());
    cmdRegistry.add(new sk.hikaribot.twitter.Tweet());
    log.info("Commands registered");
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
        this.sendMessage(channel, Colors.RED + "NO: " + Colors.NORMAL + "Command not found '" + ex.getMessage() + "'");
      }
    } catch (CommandRegistry.InsufficientPermissionsException ex) {
      if (permission > 0) {
        this.sendMessage(channel, Colors.RED + "NO: " + Colors.NORMAL + "Insufficient permissions - "
                + Colors.BLUE + "you: " + Colors.YELLOW + permission + Colors.BLUE + ", needed: " + Colors.RED + ex.getMessage());
      }
    } catch (Command.ImproperArgsException ex) {
      log.fatal("This should have been caught by HELP!");
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
    log.info("Joined " + channel);
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
    User user = this.getUser(channel, nick.trim());
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

  public TwitBot getTwitBot() {
    return twit;
  }

  public long getTimeStarted() {
    return startMillis;
  }

}
