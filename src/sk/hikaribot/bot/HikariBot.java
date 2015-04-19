/*
 * Hikari IRC Bot - HikariBot
 * Shizuka Kamishima - 2014-11-06
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
package sk.hikaribot.bot;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Observer;
import java.util.Properties;
import org.jibble.pircbot.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.api.*;
import sk.hikaribot.api.exception.*;
import sk.hikaribot.banhammer.Banhammer;
import sk.hikaribot.twitter.TwitBot;

/**
 * Our heroine, the Hikari IRC Bot.
 *
 * @author Shizuka Kamishima
 * @version banhammer
 */
public class HikariBot extends PircBot {

  private static final Logger log = LogManager.getLogger("Bot");

  private final long startMillis;

  private Database db;

  private final Banhammer bh;
  private final CommandRegistry cr;
  private final PermissionsManager pm;
  private final ServerResponse sr;
  private final TwitBot twit;

  private final String owner;
  private final String delimiter;
  private final String defaultChannel;
  private final String defaultNick;
  private final String nickservPassword;
  private final String server;
  private final String version;

  private boolean _isConnected = false;
  private boolean _isVerbose = false;

  /**
   * Start HikariBot with runtime properties.
   *
   * @param config config.properties settings
   * @param twitConfig twitbot.properties settings
   */
  public HikariBot(Properties config, Properties twitConfig) {
    this.startMillis = System.currentTimeMillis();
    log.debug("HikariBot started...");

    try {
      this.db = new Database("hikaribot.sqlite");
    } catch (ClassNotFoundException ex) {
      log.fatal("SQLite Driver not found! " + ex.getMessage());
      System.exit(1);
    } catch (SQLException ex) {
      log.fatal("SQL Error: " + ex.getMessage());
      System.exit(1);
    }

    /*
     * load config
     */
    this.owner = config.getProperty("owner");
    this.delimiter = config.getProperty("delimiter");
    this.defaultChannel = config.getProperty("chan");
    this.defaultNick = config.getProperty("nick");
    this.nickservPassword = config.getProperty("pass");
    this.server = config.getProperty("server");
    this.version = config.getProperty("version");
    _isVerbose = false; //TODO load this from config
    
    /*
     * initialize components
     */
    this.cr = new CommandRegistry(this);
    this.pm = new PermissionsManager(this);
    this.sr = new ServerResponse();
    this.bh = new Banhammer(this);
    this.twit = new TwitBot(this, twitConfig);

    /*
     * register commands
     */
    log.info("Registering commands...");
    cr.add(new sk.hikaribot.cmd.Verbose());
    cr.add(new sk.hikaribot.cmd.RawLine());
    cr.add(new sk.hikaribot.cmd.Help());
    cr.add(new sk.hikaribot.cmd.Die());
    cr.add(new sk.hikaribot.cmd.Join());
    cr.add(new sk.hikaribot.cmd.Part());
    cr.add(new sk.hikaribot.cmd.Say());
    cr.add(new sk.hikaribot.cmd.Nick());
    cr.add(new sk.hikaribot.cmd.DoAction());
    cr.add(new sk.hikaribot.cmd.Version());
    cr.add(new sk.hikaribot.cmd.GetUserLevel());
    cr.add(new sk.hikaribot.cmd.GetWhois());
    cr.add(new sk.hikaribot.cmd.AccountIdentify());
    cr.add(new sk.hikaribot.cmd.SetUserLevel());
    cr.add(new sk.hikaribot.cmd.AccountRegister());
    cr.add(new sk.hikaribot.cmd.AccountSave());
    cr.add(new sk.hikaribot.cmd.AccountReload());
    cr.add(new sk.hikaribot.cmd.SQLQuery());
    cr.add(new sk.hikaribot.twitter.cmd.LoadProfile());
    cr.add(new sk.hikaribot.twitter.cmd.UnloadProfile());
    cr.add(new sk.hikaribot.twitter.cmd.GetActiveProfile());
    cr.add(new sk.hikaribot.twitter.cmd.RequestNewToken());
    cr.add(new sk.hikaribot.twitter.cmd.ConfirmNewToken());
    cr.add(new sk.hikaribot.twitter.cmd.CancelNewToken());
    cr.add(new sk.hikaribot.twitter.cmd.Tweet());
    cr.add(new sk.hikaribot.twitter.cmd.ListenerAssign());
    cr.add(new sk.hikaribot.twitter.cmd.ListenerToggleEcho());
    cr.add(new sk.hikaribot.twitter.cmd.ListenerFollowUser());
    cr.add(new sk.hikaribot.banhammer.cmd.BanCount());
    cr.add(new sk.hikaribot.banhammer.cmd.AddChannel());
    cr.add(new sk.hikaribot.banhammer.cmd.SetOptions());
    log.info("Commands registered");

    /*
     * start bot
     */
    this.setName(defaultNick);
    this.setVersion(version);
    this.setLogin("hikaribot");
    try {
      this.connect(server);
      log.info("Connecting to " + server + "...");
    } catch (IOException ex) {
      log.fatal("Failed to connect to server!");
      System.exit(1);
    }
  }

  /**
   * Handles PRIVMSG detected to start with command delimiter.
   *
   * @param channel the channel command came from (sender if PM)
   * @param sender who sent the command
   * @param message contents of line including command and delimiter
   */
  private void command(String channel, String sender, String message) {
    int permission = pm.getUserLevel(sender);
    try {
      cr.execute(channel, sender, permission, message);
    } catch (CommandNotFoundException ex) {
      /*
       * suppressing 404 altogether if (permission > 0) { //only 404 if it was
       * an op this.sendMessage(channel, Colors.RED + "NO: " + Colors.NORMAL +
       * "Command not found '" + ex.getMessage() + "'"); }
       */
    } catch (InsufficientPermissionsException ex) {
      if (permission > 0) {
        this.sendMessage(channel, Colors.RED + "NO: " + Colors.NORMAL + "Insufficient permissions - "
                + Colors.BLUE + "you: " + Colors.OLIVE + permission + Colors.BLUE + ", needed: " + Colors.BROWN + ex.getMessage());
      }
    } catch (ImproperArgsException ex) {
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
    if (message.startsWith(delimiter)) { //then it's a command
      this.command(channel, sender, message);
    }
  }

  /**
   * Called when we get a PM
   */
  @Override
  protected void onPrivateMessage(String sender, String login, String hostname, String message) {
    message = Colors.removeFormattingAndColors(message);
    if (message.startsWith(delimiter)) { //then it's a command
      this.command(sender, sender, message);
    }
  }

  /**
   * Called when we finish connecting to the server
   */
  @Override
  protected void onConnect() {
    this.identify(this.nickservPassword);
    log.info("Sent Nickserv ident");
    this.joinChannel(this.defaultChannel);
    log.info("Joining " + this.defaultChannel);
  }

  /**
   * Called when we disconnect from the server
   */
  @Override
  protected void onDisconnect() {
    log.info("Disconnected, exiting...");
    try {
      twit.stopListener();
      pm.storeAccounts();
      db.disconnect();
    } catch (IOException ex) {
      log.fatal("Could not write permissions.properties!");
      System.exit(1);
    } catch (SQLException ex) {
      log.fatal("SQLException on close: " + ex.getMessage());
      System.exit(1);
    }
    System.exit(0);
  }

  /**
   * Called when we get a list of users from a channel
   */
  @Override
  protected void onUserList(String channel, User[] users) {
    log.info("Joined " + channel);
    pm.onJoinChannel(channel);
  }

  /**
   * Called when we get FINGERed, ew
   */
  @Override
  protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
    super.onFinger(sourceNick, sourceLogin, sourceHostname, target);
    log.warn("FINGER from " + sourceNick + "!" + sourceLogin + "@" + sourceHostname);
  }

  /**
   * Called when someone TIMEs us
   */
  @Override
  protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
    super.onTime(sourceNick, sourceLogin, sourceHostname, target);
    log.warn("TIME from " + sourceNick + "!" + sourceLogin + "@" + sourceHostname);
  }

  /**
   * Called when someone PINGs us
   */
  @Override
  protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
    super.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
    log.warn("PING from " + sourceNick + "!" + sourceLogin + "@" + sourceHostname);
  }

  /**
   * Called when someone VERSIONs us
   */
  @Override
  protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
    super.onVersion(sourceNick, sourceLogin, sourceHostname, target);
    log.warn("VERSION from " + sourceNick + "!" + sourceLogin + "@" + sourceHostname);
  }

  /**
   * Called on NOTICEs
   */
  @Override
  protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
    super.onNotice(sourceNick, sourceLogin, sourceHostname, target, notice);
    log.warn("NOTICE from " + sourceNick + "!" + sourceLogin + "@" + sourceHostname + ": " + notice);
  }

  /**
   * Called when someone quits
   */
  @Override
  protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
    pm.onQuit(sourceNick);
  }

  /**
   * Called when someone changes nick
   */
  @Override
  protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
    pm.onNickChange(oldNick, newNick);
  }

  /**
   * Called when someone parts a channel
   */
  @Override
  protected void onPart(String channel, String sender, String login, String hostname) {
    pm.onPart(sender);
  }

  /**
   * Called when someone logs in or out of nickserv
   */
  @Override
  protected void onAccount(String nick, String account) {
    pm.onAccount(nick, account);
  }

  /**
   * Called when MODE +b is set on a user in a channel
   */
  @Override
  protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
    bh.onBan(channel, hostmask, sourceNick + "!" + sourceLogin + "@" + sourceHostname);
  }

  /**
   * Called when MODE -b is set on a user in a channel
   */
  @Override
  protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
    bh.onUnban(channel, hostmask, sourceNick + "!" + sourceLogin + "@" + sourceHostname);
  }

  /**
   * Requests a WHOIS for a nick.
   *
   * @param target nick to WHOIS
   * @param wiResponse WhoisResponse to subscribe to ServerResponse, for
   * collecting WHOIS responses
   */
  public void sendWhois(String target, Observer wiResponse) {
    sr.addObserver(wiResponse);
    this.sendRawLine("WHOIS " + target);
  }

  /**
   * Requests extended WHO for a channel, returns nicks and accounts.
   *
   * @param channel channel to WHO against
   * @param whoResponse WhoResponse to collect responses
   */
  public void sendWhox(String channel, Observer whoResponse) {
    sr.addObserver(whoResponse);
    this.sendRawLine("WHO " + channel + " %na");
  }

  /**
   * Catch known server responses and send them to our Observable intermediary
   */
  @Override
  public synchronized void onServerResponse(int code, String response) {
    sr.onServerResponse(code, response);
  }

  /**
   * @param channel the channel to fetch from
   * @param nick the nick to fetch
   * @return User object for the nick
   */
  public User getUser(String channel, String nick) {
    User[] users = this.getUsers(channel);
    for (User user : users) {
      if (user.equals(nick)) {
        return user;
      }
    }
    return null;
  }

  /**
   * @return the CommandRegistry object
   */
  public CommandRegistry getCommandRegistry() {
    return cr;
  }

  /**
   * @return the TwitBot object
   */
  public TwitBot getTwitBot() {
    return twit;
  }

  /**
   * @return System.getCurrentMillis() from when the bot started up
   */
  public long getTimeStarted() {
    return startMillis;
  }

  /**
   * @return the ServerResponse intermediary
   */
  public ServerResponse getServerResponder() {
    return sr;
  }

  /**
   * Get the Nickserv canonical nick of our owner. Typically equal to owner's
   * nick, but not always.
   *
   * @return owner's nickserv account
   */
  public String getOwner() {
    return owner;
  }

  /**
   * @return the character denoting a command
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * @return the Permissions manager
   */
  public PermissionsManager getPermissionsManager() {
    return pm;
  }

  public boolean getVerbose() {
    return this._isVerbose;
  }

  public void toggleVerbose(boolean set) {
    this.setVerbose(set);
    this._isVerbose = set;
  }

  public Connection getDatabase() {
    return this.db.getDatabase();
  }
  
  public Banhammer getBanhammer() {
    return this.bh;
  }
  
  public boolean inChannel(String channel) {
    if (Arrays.asList(this.getChannels()).contains(channel)) {
      return true;
    }
    return false;
  }

}
