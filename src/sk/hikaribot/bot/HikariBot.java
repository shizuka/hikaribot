/*
 * Hikari IRC Bot - HikariBot
 * Shizuka Kamishima - 2014-11-06
 */

/*
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

import sk.hikaribot.api.exception.CommandNotFoundException;
import sk.hikaribot.api.exception.InsufficientPermissionsException;
import java.util.Properties;
import org.jibble.pircbot.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.api.exception.ImproperArgsException;
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
    cmdRegistry.add(new sk.hikaribot.cmd.RawLine());
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
    cmdRegistry.add(new sk.hikaribot.twitter.cmd.LoadProfile());
    cmdRegistry.add(new sk.hikaribot.twitter.cmd.UnloadProfile());
    cmdRegistry.add(new sk.hikaribot.twitter.cmd.GetActiveProfile());
    cmdRegistry.add(new sk.hikaribot.twitter.cmd.RequestNewToken());
    cmdRegistry.add(new sk.hikaribot.twitter.cmd.ConfirmNewToken());
    cmdRegistry.add(new sk.hikaribot.twitter.cmd.CancelNewToken());
    cmdRegistry.add(new sk.hikaribot.twitter.cmd.Tweet());
    log.info("Commands registered");
  }

  /**
   * Handles PRIVMSG detected to start with command delimiter.
   *
   * @param channel the channel command came from (sender if PM)
   * @param sender who sent the command
   * @param message contents of line including command and delimiter
   */
  private void command(String channel, String sender, String message) {
    int permission = this.getUserPermission(channel, sender);
    try {
      cmdRegistry.execute(channel, sender, permission, message);
    } catch (CommandNotFoundException ex) {
      /* suppressing 404 altogether
      if (permission > 0) { //only 404 if it was an op
        this.sendMessage(channel, Colors.RED + "NO: " + Colors.NORMAL + "Command not found '" + ex.getMessage() + "'");
      } */
    } catch (InsufficientPermissionsException ex) {
      if (permission > 0) {
        this.sendMessage(channel, Colors.RED + "NO: " + Colors.NORMAL + "Insufficient permissions - "
                + Colors.BLUE + "you: " + Colors.BROWN + permission + Colors.BLUE + ", needed: " + Colors.RED + ex.getMessage());
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
    //ignoring opchat until permissions overhaul
    if(channel.startsWith("@")) {
      return;
    }
    message = Colors.removeFormattingAndColors(message);
    if (message.startsWith(config.getProperty("delimiter"))) { //then it's a command
      this.command(channel, sender, message);
    }
  }

  /**
   * Called when we get a PM
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
