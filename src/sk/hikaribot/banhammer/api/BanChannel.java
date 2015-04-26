/*
 * hikaribot - BanChannel
 * Shizuka Kamishima - 2015-04-15
 * 
 * Copyright (c) 2015, Shizuka Kamishima
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
package sk.hikaribot.banhammer.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.Colors;
import sk.hikaribot.banhammer.Banhammer;
import sk.hikaribot.bot.HikariBot;

/**
 * Handles Banhammer operations for a single channel.
 *
 * @author Shizuka Kamishima
 */
public class BanChannel implements Observer {

  private static final Logger log = LogManager.getLogger("BHC");
  private final HikariBot bot;
  private final Banhammer bh;
  private final BanDatabase db;
  private final String channel;
  private final ChannelOptions ops;
  private final List<String> inactiveBanmasks;
  private int numActive;
  private int numInactive;

  /**
   * Initialize and perform sanity checks.
   *
   * @param bot our HikariBot
   * @param channel the channel we operate in
   */
  public BanChannel(HikariBot bot, String channel) {
    log.info(channel + " initializing...");
    //wake up
    this.bot = bot;
    this.bh = bot.getBanhammer();
    this.db = bh.getDatabase();
    this.channel = channel;
    this.inactiveBanmasks = new ArrayList();
    this.ops = db.getChannelOptions(channel); //creates us if we don't exist
    log.info(channel + " DONE: L=" + ops.loThreshold + " H=" + ops.hiThreshold + " K=" + ops.kickMessage);
    /*
     * bot.sendMessage(channel, Colors.DARK_GREEN + "BANHAMMER: " +
     * Colors.NORMAL
     * + "Initialized with thresholds " + Colors.OLIVE + ops.loThreshold +
     * Colors.NORMAL
     * + "/" + Colors.BROWN + ops.hiThreshold + Colors.NORMAL + ", scraping list...");
     */
    bot.sendMessage(channel, Colors.OLIVE + "BANHAMMER: " + Colors.NORMAL + "Standby...");
    this.requestBanlist();
  }

  /**
   * Create a BanlistResponse and send request to HikariBot for server.
   */
  private void requestBanlist() {
    BanlistResponse br = new BanlistResponse(channel);
    br.addObserver(this);
    bot.getServerResponder().addObserver(br);
    log.debug(channel + " REQUESTING BANLIST...");
    bot.sendRawLine("MODE " + channel + " +b");
  }

  //BanlistResponse has called us, pass it along and destroy the object
  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof BanlistResponse) {
      //don't know why it ever wouldn't be but sanity check nonetheless
      List<ScrapedBan> sb = (List<ScrapedBan>) arg;
      bot.getServerResponder().deleteObserver((Observer) o);
      this.scrapeBanlist(sb);
    }
  }

  /**
   * Handle the returning List of bans from BanlistResponse.
   *
   * @param scrapedBans List of scraped bans: banmask, author, timestamp
   */
  private void scrapeBanlist(List<ScrapedBan> scrapedBans) {
    int numScraped = scrapedBans.size();

    log.debug(channel + " SCRAPED " + numScraped + " BANS...");
    db.upsertScrapedBans(channel, scrapedBans);

    numActive = db.countActiveBans(channel);
    numInactive = db.countInactiveBans(channel);

    //report status scrape finished
    String status = Colors.DARK_GREEN + "BANHAMMER: " + Colors.NORMAL + "Initialized with ";
    if (numActive > this.ops.hiThreshold) {
      status += Colors.OLIVE + this.ops.loThreshold + Colors.NORMAL + "/" + Colors.BROWN + this.ops.hiThreshold + Colors.NORMAL + "/" + Colors.RED + "[" + numActive + "] " + Colors.NORMAL;
    } else if (numActive > this.ops.loThreshold) {
      status += Colors.OLIVE + this.ops.loThreshold + Colors.NORMAL + "/" + Colors.YELLOW + "[" + numActive + "]" + Colors.NORMAL + "/" + Colors.BROWN + this.ops.hiThreshold + " " + Colors.NORMAL;
    } else {
      status += Colors.GREEN + "[" + numActive + "]" + Colors.NORMAL + "/" + Colors.OLIVE + this.ops.loThreshold + Colors.NORMAL + "/" + Colors.BROWN + this.ops.hiThreshold + " " + Colors.NORMAL;
    }
    status += "bans";
    if (numInactive > 0) {
      status += " and tracking " + Colors.OLIVE + numInactive + Colors.NORMAL + " inactive bans";
    }
    bot.sendMessage(channel, status);

    if (numActive > this.ops.hiThreshold) {
      //do rotation until it would be below
    }
  }

  /**
   * Incoming JOIN, check against the Inactive ban list.
   *
   * @param usermask of the user that just joined
   */
  public void onJoin(String usermask) {

  }

  /**
   * Passed from HikariBot.onSetChannelBan() by Banhammer, updates database.
   *
   * @param banmask the ban set
   * @param author nick of who set it
   */
  public void onBan(String banmask, String author) {
    log.debug("MODE " + channel + " +b " + banmask + " BY " + author);
    //get userlist from HikariBot, try and find a user that matches the banmask
    db.upsertNewBan(channel, banmask, author, ":NYI:");
  }

  /**
   * Passed from HikariBot.onRemoveChannelBan() by Banhammer, updates database.
   *
   * @param banmask the ban unset
   * @param author nick of who unset it
   */
  public void onUnban(String banmask, String author) {
    log.debug("MODE " + channel + " -b " + banmask + " BY " + author);
    db.unsetBan(channel, banmask, author);
  }

  /*
   * TODO: configuration and query ops
   * like search ban by id, mask, nick
   * dump notes to PM
   * SmartUnban? (given nick, try to whois it, match usermask against DB)
   * SmartBan? (whowas it, etc)
   * logging commands: loglast, loglastN, logban by id
   * setting commands: unban by id, unbanLast, force active/inactive?
   * permanent bans config
   */
}
