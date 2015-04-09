/*
 * hikaribot - PermissionsManager
 * Shizuka Kamishima - 2014-11-14
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.Colors;
import sk.hikaribot.api.WhoResponse;
import sk.hikaribot.api.WhoisResponse;
import sk.hikaribot.api.exception.*;

/**
 * Maintains list of user accounts and their permissions levels.
 *
 * @author Shizuka Kamishima
 */
public final class PermissionsManager implements Observer {

  private static final Logger log = LogManager.getLogger("Permissions");

  private final HikariBot bot;
  private final CommandRegistry cr;
  private final HashMap<String, Integer> accounts;
  private final HashMap<String, String> cache;

  /**
   * Initialize permissions. Loads accounts from properties file and initializes
   * cache.
   *
   * @param bot the HikariBot we operate on
   */
  public PermissionsManager(HikariBot bot) {
    log.debug("PermissionsManager started...");
    this.bot = bot;
    this.cr = bot.getCommandRegistry();
    this.accounts = new HashMap();
    this.cache = new HashMap();
    try {
      this.loadAccounts();
    } catch (FileNotFoundException ex) {
      log.fatal("Could not find file permissions.properties!");
      bot.quitServer("Fatal exception");
    } catch (IOException ex) {
      log.fatal("Could not read permissions.properties!");
      bot.quitServer("Fatal exception");
    }
  }

  /**
   * Gets userlevel for a nick.
   *
   * @param nick the nick to fetch userlevel for, checks current nick first,
   * then nick as canonical name
   * @return account's userlevel, 0 if account doesn't exist or isn't identified
   */
  public int getUserLevel(String nick) {
    if (isIdentified(nick)) {
      return accounts.get(cache.get(nick));
    } else {
      return 0; //no special treatment for an unidentified account
    }
  }

  public void setUserLevel(String nick, int level) throws NoSuchAccountException {
    String account;
    if (isIdentified(nick)) {
      account = cache.get(nick);
    } else if (isRegistered(nick)) {
      account = nick;
    } else {
      throw new NoSuchAccountException(nick);
    }
    accounts.put(account, level);
  }

  /**
   * @param canonNick canonical nick to check
   * @return is this a canonical nick in our accounts list?
   */
  public boolean isRegistered(String canonNick) {
    return accounts.containsKey(canonNick);
  }

  /**
   * @param nick user current nick
   * @return is this nick identified for a canonical account in our cache?
   */
  public boolean isIdentified(String nick) {
    return cache.containsKey(nick);
  }

  /**
   * Performs WHOIS on nick to get Nickserv canonical nick.
   *
   * @param nick the user to check
   * @param channel channel to print response to
   * @param flag who is requesting the whois? IDENTIFY or REGISTER
   */
  public void getCanonNick(String nick, String channel, String flag) {
    WhoisResponse wir = new WhoisResponse(nick, channel, flag);
    wir.addObserver(this);
    bot.sendWhois(nick, wir);
  }

  /**
   * Updates cache to reflect new nick, if necessary. Passed by
   * HikariBot.onNickChange()
   *
   * @param oldnick this user...
   * @param newnick ...has changed to this nick
   */
  public void onNickChange(String oldnick, String newnick) {
    if (isIdentified(oldnick)) {
      String canonNick = cache.get(oldnick);
      cache.remove(oldnick);
      cache.put(newnick, canonNick);
      log.info("NICK CHANGE FOR " + canonNick + ": " + oldnick + " -> " + newnick);
    }
  }

  /**
   * Removes nick from cache. If a user PARTs from a channel we're in, we won't
   * know if they QUIT somewhere else, so we have to treat it the same as QUIT.
   * Called by HikariBot.onQuit() and HikariBot.onPart()
   *
   * @param nick the nick that PARTed or QUIT
   */
  public void onPartOrQuit(String nick) {
    if (isIdentified(nick)) {
      //TODO on PART check our other channels, on QUIT keep this logic
      //TODO also call this if we part a channel
      String canonNick = cache.get(nick);
      cache.remove(nick);
      log.info("LOGGED OUT " + canonNick);
    }
  }

  /**
   * Check invoker's WHOIS for a Nickserv login, if present then identify for a
   * user account and cache it.
   *
   * @param invoker invoker trying to identify
   * @param channel channel to print response to
   */
  public void identify(String invoker, String channel) {
    if (isIdentified(invoker)) {
      log.error("ALREADY IDENTIFIED " + invoker + " FOR " + cache.get(invoker));
      bot.sendMessage(channel, Colors.OLIVE + "PERMISSIONS: " + Colors.NORMAL + "You are already identified for " + cache.get(invoker));
      return;
    }
    getCanonNick(invoker, channel, "IDENTIFY");
    //control is passed to update() and identCanonNick() to complete the identify
  }

  /**
   * Called when WhoisResponse returns, identifies invoker.
   *
   * @param invoker invoker trying to identify
   * @param canonNick their Nickserv nick, if present
   * @param channel channel to print response to
   */
  public void identCanonNick(String invoker, String canonNick, String channel) {
    if (isIdentified(invoker)) {
      log.warn("ALREADY IDENTIFIED " + invoker + " FOR " + cache.get(invoker));
      bot.sendMessage(channel, Colors.OLIVE + "PERMISSIONS: " + Colors.NORMAL + invoker + " is already identified for " + Colors.OLIVE + canonNick);
      return;
    } else if (!isRegistered(canonNick)) {
      log.error("NO SUCH ACCOUNT " + canonNick);
      bot.sendMessage(channel, Colors.BROWN + "PERMISSIONS: " + Colors.OLIVE + canonNick + Colors.NORMAL + " is not registered with Permissions");
      return;
    }
    cache.put(invoker, canonNick);
    log.info("IDENTIFIED " + invoker + " FOR " + canonNick);
    bot.sendMessage(channel, Colors.DARK_GREEN + "PERMISSIONS: " + Colors.NORMAL + invoker + ": You are now identified for " + Colors.OLIVE + canonNick);
  }

  /**
   * Called when Extended-Join reports a Nickserv account join.
   * 
   * @param nick the nick joining
   * @param canonNick their Nickserv nick
   */
  public void onAccount(String nick, String canonNick) {
    if (isRegistered(canonNick)) {
      if (!isIdentified(nick)) {
        cache.put(nick, canonNick);
        log.info("IDENTIFIED " + nick + " FOR " + canonNick + " ON JOIN");
      }
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    //Called when WhoisResponse has completed collecting WHOIS data
    if (arg instanceof WhoisResponse) {
      log.debug("Got WhoisResponse");
      WhoisResponse wir = (WhoisResponse) arg;
      bot.getServerResponder().deleteObserver((Observer) o); //i hope this works
      if (wir.isIdented()) {
        if (null != wir.getFlag()) {
          switch (wir.getFlag()) {
            case "IDENTIFY":
              this.identCanonNick(wir.getTarget(), wir.getCanonicalNick(), wir.getChannel());
              break;
            case "REGISTER":
              this.completeRegistration(wir.getCanonicalNick(), wir.getChannel());
              break;
          }
        }
      } else {
        bot.sendMessage(wir.getChannel(), Colors.BROWN + "PERMISSIONS: " + Colors.OLIVE + wir.getTarget() + Colors.NORMAL + " is not registered with Nickserv");
      }
    } else if (arg instanceof WhoResponse) {
      log.debug("Got WhoResponse");
      WhoResponse wr = (WhoResponse) arg;
      bot.getServerResponder().deleteObserver((Observer) o); //still hope this works
      this.handleWhox(wr.getResponses());
    }
  }

  /**
   * Start registering an account. Does WHOIS to get canonical nick.
   *
   * @param nick the nick to register
   * @param channel channel to send response to
   * @throws AccountAlreadyExistsException if account already exists
   */
  public void registerAccount(String nick, String channel) throws AccountAlreadyExistsException {
    if (isRegistered(nick)) {
      throw new AccountAlreadyExistsException(nick);
    }
    getCanonNick(nick, channel, "REGISTER");
  }

  /**
   * Completes account registration.
   *
   * @param canonNick who we just got from WHOIS
   * @param channel channel to send response to
   */
  public void completeRegistration(String canonNick, String channel) {
    accounts.put(canonNick, 1);
    log.info("REGISTERED " + canonNick);
    bot.sendMessage(channel, Colors.DARK_GREEN + "PERMISSIONS: " + Colors.NORMAL + "Successfully registered account " + Colors.OLIVE + canonNick + Colors.NORMAL + " and set to level " + Colors.OLIVE + "1");
    bot.sendMessage(channel, canonNick + ": Call " + bot.getDelimiter() + "identify to identify");
  }

  /**
   * Loads accounts from permissions.properties.
   *
   * @throws FileNotFoundException if file not found, kills bot
   * @throws IOException if file couldn't be read, kills bot
   */
  public void loadAccounts() throws FileNotFoundException, IOException {
    accounts.clear();
    FileReader permFile = new FileReader("permissions.properties");
    Properties props = new Properties();
    props.load(permFile);
    for (Map.Entry<Object, Object> prop : props.entrySet()) {
      String nick = (String) prop.getKey();
      String strLevel = (String) prop.getValue();
      int level = Integer.parseInt(strLevel);
      accounts.put(nick, level);
      log.debug(nick + " : " + level);
    }
    accounts.put(bot.getOwner(), 99);
    log.info("Loaded all accounts");
  }

  /**
   * Writes accounts to permissions.properties.
   *
   * @throws FileNotFoundException if permissions.properties vanished since bot
   * was started (bot dies if it's missing at runtime)
   * @throws IOException if file couldn't be written, kills bot
   */
  public void storeAccounts() throws FileNotFoundException, IOException {
    FileWriter permFile = new FileWriter("permissions.properties");
    Properties accountProps = new Properties();
    for (Map.Entry<String, Integer> account : accounts.entrySet()) {
      String nick = account.getKey();
      String strLevel = account.getValue().toString();
      accountProps.setProperty(nick, strLevel);
    }
    accountProps.store(permFile, null);
    log.info("Accounts saved to permissions.properties");
  }

  /**
   * Called when HikariBot finishes joining a channel (gets a user list),
   * performs WHOX against channel to identify all visible Permissions accounts.
   *
   * @param channel the channel HikariBot just joined
   */
  public void onJoinChannel(String channel) {
    log.debug("Requesting WHOX for " + channel);
    WhoResponse wr = new WhoResponse(channel);
    wr.addObserver(this);
    bot.sendWhox(channel, wr);
  }

  private void handleWhox(HashMap<String, String> users) {
    Set<String> nicks = users.keySet();
    for (String nick : nicks) {
      if (isRegistered(users.get(nick))) {
        //is the nickserv account identified by this nick part of our list?
        //WHOX gives 0 if a nick isn't registered/idented, and 0 isn't a valid
        //  nick so it's never going to be in our account list, so ignored
        if (!isIdentified(nick)) {
          //if this nick is not identified for a Permissions account
          cache.put(nick, users.get(nick));
          log.debug("WHOX IDENTIFIED " + nick + " FOR " + users.get(nick));
        }
      }
    }
  }

}
