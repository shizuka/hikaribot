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
 * "bh_CHANNEL_bans"
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
 * "bh_CHANNEL_notes"
 * noteid PK | banid | timestamp | author | note
 * noteid - to unambiguously grab a note for editing
 * banid - the ban this note applies to, get all notes WHERE banid=X
 * timestamp - when note was created
 * author - who created the note (nick, or Banhammer for automatic logs)
 * note - content of the note, such as "ban initially set", or "what an asshole"
 *
 * "bh_options"
 * channel PK | loThreshold | hiThreshold | kickMessage
 * channel - channel this applies to, or @ for the global defaults
 * loThreshold - above which we rotate oldest active ban to make room for new +b
 * hiThreshold - above which we Inactivate bans until below
 * kickMessage - message to give when KICKing an Inactive ban JOINing
 * NOTE: Thresholds must be greater than zero, for obvious reasons.
 *
 * @author Shizuka Kamishima
 */
public class BanDatabase {

  private static final Logger log = LogManager.getLogger("BHDB");
  private final Connection db;
  private final HikariBot bot;

  //Magic values to insert into DB if the options table is missing
  private int defLoThreshold = 40;
  private int defHiThreshold = 45;
  private String defKickMessage = "Your ban was not lifted.";

  public BanDatabase(HikariBot bot) {
    this.bot = bot;
    this.db = bot.getDatabase();
    log.info("Initialized");
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
   * TABLE bh_options
   * channel | loThreshold | hiThreshold | kickMessage
   *
   * @ | 40 | 45 | Your ban was not lifted.
   */
  private void loadDefaults() {
    log.info("Loading default options...");
    try {
      Statement stat = db.createStatement();
      stat.execute("CREATE TABLE IF NOT EXISTS bh_options(channel PRIMARY KEY, loThreshold, hiThreshold, kickMessage);");
      ResultSet rs = stat.executeQuery("SELECT * FROM bh_options WHERE channel IS '@';");
      if (rs.next()) {
        int loThreshold = rs.getInt("loThreshold");
        int hiThreshold = rs.getInt("hiThreshold");
        String kickMessage = rs.getString("kickMessage");
        if (loThreshold == 0) {
          log.warn("Default loThreshold missing, using magic value: " + this.defLoThreshold);
          stat.execute("UPDATE bh_options SET loThreshold=" + this.defLoThreshold + " WHERE channel IS '@';");
        }
        if (hiThreshold == 0) {
          log.warn("Default hiThreshold missing, using magic value: " + this.defHiThreshold);
          stat.execute("UPDATE bh_options SET hiThreshold=" + this.defHiThreshold + " WHERE channel IS '@';");
        }
        if (kickMessage == null) {
          log.warn("Default kickMessage missing, using magic value: " + this.defKickMessage);
          stat.execute("UPDATE bh_options SET kickMessage='" + this.defKickMessage + "' WHERE channel IS '@';");
        }
        this.defLoThreshold = loThreshold;
        this.defHiThreshold = hiThreshold;
        this.defKickMessage = kickMessage;
      } else {
        //global entry didn't exist
        log.error("Default options missing, adding using magic values");
        log.warn("loThreshold=" + this.defLoThreshold + ", hiThreshold=" + this.defHiThreshold + ", kickMessage=" + this.defKickMessage);
        this.setOptions("@");
      }
      rs.close();
      stat.close();
    } catch (SQLException ex) {
      this.handleSQLException(ex);
    }
    log.info("Done");
  }

  /**
   * Sets channel options in the database.
   *
   * @param target the channel to modify, @ for global defaults
   * @param loThreshold the low threshold
   * @param hiThreshold the high threshold
   * @param kickMessage the message for KICKing an Inactive-banned JOINer
   */
  public void setOptions(String target, int loThreshold, int hiThreshold, String kickMessage) {
    try {
      Statement stat = db.createStatement();
      stat.execute("INSERT OR REPLACE INTO bh_options VALUES ('" + target + "', " + loThreshold + ", " + hiThreshold + ", '" + kickMessage + "');");
      //because channel is the PRIMARY KEY, we don't need to use a WHERE
      stat.close();
      if ("@".equals(target)) {
        this.defLoThreshold = loThreshold;
        this.defHiThreshold = hiThreshold;
        this.defKickMessage = kickMessage;
      }
    } catch (SQLException ex) {
      this.handleSQLException(ex);
    }
  }

  /**
   * Sets default channel options.
   *
   * @param target the channel to modify
   */
  public void setOptions(String target) {
    this.setOptions(target, defLoThreshold, defHiThreshold, defKickMessage);
  }

  /**
   * Fetches channel options, and loads global defaults if necessary.
   *
   * @param channel the channel to load
   *
   * @return struct of options
   */
  public ChannelOptions getChannelOptions(String channel) {
    try {
      Statement stat = db.createStatement();
      stat.execute("CREATE TABLE IF NOT EXISTS 'bh_" + channel + "_bans'(banId INTEGER PRIMARY KEY AUTOINCREMENT, banMask, userMask, author, timeCreated, timeModified)");
      stat.execute("CREATE TABLE IF NOT EXISTS 'bh_" + channel + "_notes'(noteId INTEGER PRIMARY KEY AUTOINCREMENT, banId, timestamp, author, note)");
      ResultSet rs = stat.executeQuery("SELECT * FROM bh_options WHERE channel IS '" + channel + "';");
      if (rs.next()) {
        //we'll assume all values are present and sane
        //database-side checks could be put in if we get really paranoid
        int lo = rs.getInt("loThreshold");
        int hi = rs.getInt("hiThreshold");
        String kick = rs.getString("kickMessage");
        ChannelOptions ops = new ChannelOptions(lo, hi, kick);
        rs.close();
        stat.close();
        return ops;
      } else {
        this.setOptions(channel);
        rs.close();
        stat.close();
        return this.getChannelOptions(channel); //recursion ftw
      }
    } catch (SQLException ex) {
      this.handleSQLException(ex);
    }
    //if all fails, return the magic defaults
    return new ChannelOptions(defLoThreshold, defHiThreshold, defKickMessage);
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
