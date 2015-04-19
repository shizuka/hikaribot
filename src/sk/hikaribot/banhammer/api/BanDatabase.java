/*
 * hikaribot - BanDatabase
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

import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.bot.HikariBot;

/**
 * Interface between SQLite database and Banhammer.
 *
 * DATABASE STRUCTURE
 * bh_#CHANNEL_bans
 * banid | type | banmask | usermask | author | timecreated | timemodified
 * banid - master identifier for this ban, could be banmask but this is nicer
 * type - [P|A|T|I|U], permanent, active, timeban, inactive, unset
 * --permanent bans cannot be unset except explicitly by -b - TODO
 * --active bans are currently +b
 * --timebans reference another table with their expiration time - TODO
 * --inactive bans are not +b but are scanned on user JOINs
 * --unset bans are kept for logging purposes
 * banmask - the actual +b mask
 * usermask - attempts to find a user in channel that matches the +b on set
 * author - who set the ban (nick or server)
 * timecreated - when this ban was first set (scraped from banlist, or now)
 * timemodified - when this ban was most recently set (ie inactive->active)
 * --both times are unix timestamps, treated as String until parsed, stored int
 *
 * bh_#CHANNEL_notes
 * banid | timestamp | author | note
 * banid - the ban this note applies to, get all notes WHERE banid=X
 * timestamp - when note was created
 * author - who created the note (nick, or Banhammer for automatic logs)
 * note - content of the note, such as "ban initially set", or "what an asshole"
 *
 * bh_#CHANNEL_options
 * key | value
 * keys: loThreshold, hiThreshold, kickMessage
 * values: cloned from bh_global_options, or provided on BanChannel config
 * loThreshold - above this, new +b causes oldest Active ban to go Inactive
 * hiThreshold - above this, Inactive-ate oldest Active bans until below again
 * kickMessage - message to give a returning Inactive-banned user on KICK
 *
 * bh_#CHANNEL_timebans - future enhancement
 * banid | expires
 * banid - the ban
 * expires - timestamp the ban expires on
 *
 * bh_global_options - defaults for new BanChannels
 * loThreshold = 40
 * hiThreshold = 45
 * kickMessage = Your ban was not lifted.
 *
 * @author Shizuka Kamishima
 */
public class BanDatabase {

  private static final Logger log = LogManager.getLogger("BHDB");
  private final Connection db;
  private final HikariBot bot;
  
  //Magic values to insert into DB if the global options table is missing
  private int defLoThreshold = 40;
  private int defHiThreshold = 45;
  private String defKickMessage = "Your ban was not lifted.";

  public BanDatabase(HikariBot bot) {
    this.bot = bot;
    this.db = bot.getDatabase();
    log.info("Initialized, loading defaults...");
    this.loadDefaults();
  }

  private void handleSQLException(SQLException ex) {
    log.error(ex.getMessage());
  }

  /**
   * Queries the DB to get the default Banhammer options, and creates them if
   * they don't exist.
   *
   * Defaults:
   * TABLE bh_global_options
   * loThreshold - 40
   * hiThreshold - 45
   * kickMessage - "Your ban was not lifted."
   */
  private void loadDefaults() {
    try {
      Statement stat = db.createStatement();
      stat.execute("CREATE TABLE IF NOT EXISTS bh_global_options(key,value);");
      
      //get loThreshold
      ResultSet rs = stat.executeQuery("SELECT * FROM bh_global_options WHERE key='loThreshold';");
      if (rs.next()) {
        this.defLoThreshold = rs.getInt("value");
        log.debug("Default loThreshold loaded from DB: " + this.defLoThreshold);
      } else {
        //loThreshold not defined in DB
        log.warn("Default loThreshold loaded from magic: " + this.defLoThreshold);
        stat.execute("INSERT INTO bh_global_options VALUES ('loThreshold'," + this.defLoThreshold + ");");
      }
      rs.close();
      //get hiThreshold
      rs = stat.executeQuery("SELECT * FROM bh_global_options WHERE key='hiThreshold';");
      if (rs.next()) {
        this.defHiThreshold = rs.getInt("value");
        log.debug("Default hiThreshold loaded from DB: " + this.defHiThreshold);
      } else {
        //hiThreshold not defined in DB
        log.warn("Default hiThreshold loaded from magic: " + this.defHiThreshold);
        stat.execute("INSERT INTO bh_global_options VALUES ('hiThreshold'," + this.defHiThreshold + ");");
      }
      rs.close();
      //get kick message
      rs = stat.executeQuery("SELECT * FROM bh_global_options WHERE key='kickMessage';");
      if (rs.next()) {
        this.defKickMessage = rs.getString("value");
        log.debug("Default kick message loaded from DB: " + this.defKickMessage);
      } else {
        //kickMessage not defined in DB
        log.warn("Default kick message loaded from magic: " + this.defKickMessage);
        stat.execute("INSERT INTO bh_global_options VALUES ('kickMessage', '" + this.defKickMessage + "');");
      }
      rs.close();
      stat.close();
    } catch (SQLException ex) {
      this.handleSQLException(ex);
    }
    log.info("Defaults loaded");
  }

  /*
   * get list of inactive banmasks
   * SELECT banId, banMask FROM bans WHERE type='I'
   * HashMap int banId, String banmask
   *
   * get list of active banmasks - 
   * SELECT banId, banMask FROM bans WHERE type IN ('A', 'P', 'T')
   * HashMap int banId, String banmask
   *
   * get list of all banmasks - SELECT banId, banMask FROM bans
   * HashMap int banId, String banmask
   *
   * get BanEntry for ban by id - SELECT * FROM bans WHERE banId=id
   * populate with notes - SELECT * FROM notes WHERE banId=id
   * return BanEntry
   *
   * insert new active ban - INSERT INTO bans(banmask,usermask,author)
   *
   * insert new scraped ban - INSERT INTO bans(banmask,author,timecreated)
   *
   * insert new note - INSERT INTO notes(banid,author,note)
   *
   * update ban to unset - UPDATE bans SET type WHERE banId=id
   *
   * there's more i'm sure...
   */
}
