/*
 * hikaribot - Banhammer
 * Shizuka Kamishima - 2015-04-14
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
package sk.hikaribot.banhammer;

import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.banhammer.api.*;
import sk.hikaribot.bot.HikariBot;

/**
 * Ban management component. Allows us to keep a limit of concurrent bans set
 * in IRC. Dispatches IRC events to BanChannel worker objects.
 *
 * Channels have a hard limit of bans that can be applied. Banhammer maintains
 * an "active" ban list that mirrors the actual +b in channel. If the length of
 * that list exceeds loThreshold, then new +b modes will cause the oldest active
 * +b to be marked "inactive", set -b, so the list doesn't grow. The list of
 * inactive bans, then, is scanned on user JOINs to see if the new user matches
 * a banmask in the list. If they do, the ban is marked "active" again, set +b,
 * and the user kicked (with appropriate message). If this would exceed the low
 * threshold, again the oldest active ban is rotated out.
 *
 * If a ban is unset by operators, it is likewise marked "unset" in our list,
 * but the record remains for logging purposes (we can see how many times a ban
 * was set, unset, set again, etc). This leaves channel operators in full
 * control over the active bans. MODE -b always comes through even if the mask
 * isn't currently set, which Banhammer can pick up and check against its lists.
 * Inactive bans can be easily unset in this way, or else by command.
 *
 * BanChannel worker objects are responsible for maintaining the lists of active
 * and inactive bans, synchronizing them with the channel, and checking incoming
 * JOINs against the inactive list. Banhammer dispatches JOIN, +b, and -b to the
 * relevant BanChannel object. BanChannels will also construct a BanlistResponse
 * object to be subscribed to HikariBot's ServerResponse, to scrape the current
 * MODEs +b in their channel. They will then perform queries via BanDatabase to
 * update the truly active bans. This only needs to happen on construction, as
 * changes to the +b will be picked up as +b/-b thereafter and invoke logic.
 *
 * FUTURE ENHANCEMENTS: Timebans, channel synchronization
 * Timebans would be implemented via a cron-like Observer to ServerResponse, or
 * to a dedicated Cron component in HikariBot, to wake up frequently. Bans so
 * marked would store an expiration timestamp in the database. When the time
 * passes, the ban is simply unset.
 * Channel synchronization would necessitate an object like BanChannel, but
 * without any of the direct list management. They would forward their joins,
 * bans, unbans, etc, to their parent BanChannel. The usual logic would apply,
 * with the parent updating records, then passing any mode-setting or kicking
 * commands back to its synchronized channels.
 *
 * General Architecture:
 * Banhammer
 * |
 * |--BanChannel - object managing one channel
 * |--BanChannel - receives onBan(), onUnban(), and onJoin() from HikariBot
 * |--BanChannel - can also call out to ServerResponse for BanlistResponse
 * | |
 * |-+--BanDatabase - encapsulates BanChannel requests to SQLite queries
 * . | |--SQLite database file
 * . | |
 * . | |--BanEntry(s) - List from a query
 * . | |--BanNote(s) - for each ban
 * . |
 * . |--BanlistResponse - returns to HikariBot to gather RPL_BANLIST responses
 * . |--ScrapedBan - trimmed BanEntry for the available info from banlisting
 *
 * @author Shizuka Kamishima
 */
public class Banhammer {

  private static final Logger log = LogManager.getLogger("BH");
  private final HikariBot bot;
  private final BanDatabase db;
  private final HashMap<String, BanChannel> channels;

  public Banhammer(HikariBot bot) {
    this.bot = bot;
    this.db = new BanDatabase(bot); //will sanity check DB for options
    this.channels = new HashMap();
  }

  public void addChannel(String channel) {
    if (this.channels.containsKey(channel)) {
      return;
    }
    BanChannel bc = new BanChannel(this, channel);
    this.channels.put(channel, bc);
    //BanChannel will establish itself in the database and start scraping
  }

  public void removeChannel(String channel) {
    if (!this.channels.containsKey(channel)) {
      return;
    }
    this.channels.remove(channel);
  }

  public void onJoin(String channel, String usermask) {
    if (!this.channels.containsKey(channel)) {
      return;
    }
    this.channels.get(channel).onJoin(usermask);
  }

  public void onBan(String channel, String banmask, String userWhoSet) {
    if (!this.channels.containsKey(channel)) {
      return;
    }
    this.channels.get(channel).onBan(banmask, userWhoSet);
  }

  public void onUnban(String channel, String banmask, String userWhoSet) {
    if (!this.channels.containsKey(channel)) {
      return;
    }
    this.channels.get(channel).onUnban(banmask, userWhoSet);
  }

  public BanDatabase getDatabase() {
    return this.db;
  }

}
