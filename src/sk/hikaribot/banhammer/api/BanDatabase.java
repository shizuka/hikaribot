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
import java.util.List;
import java.util.logging.Level;
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
 * nick - nick of user just banned (logic is left to BanChannel)
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
      kickMessage = kickMessage.replace("'", "''");
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
   * Fetches channel options, creates default channel if necessary.
   * Flagrant violation of do one thing, but getting options effectively doubles
   * as the channel's initialization, so what better time to perform this logic?
   *
   * If they don't yet exist, creates the _bans and _notes tables, along with
   * triggers for automatic recordkeeping.
   *
   * @param channel the channel to load
   *
   * @return struct of options
   */
  public ChannelOptions getChannelOptions(String channel) {
    try {
      Statement stat = db.createStatement();

      /*
       * EVIL SIDE EFFECT CODE BEGINS
       */
      stat.execute("CREATE TABLE IF NOT EXISTS 'bh_" + channel + "_bans'(banId INTEGER PRIMARY KEY AUTOINCREMENT, type, banMask UNIQUE, nick, author, timeCreated, timeModified)");
      stat.execute("CREATE TABLE IF NOT EXISTS 'bh_" + channel + "_notes'(noteId INTEGER PRIMARY KEY AUTOINCREMENT, banId, timestamp, author, note)");

      //Inserting a scraped ban doesn't include time first created, so catch that and update it to be the same as scraped timestamp
      //also add a new note indicating it was scraped
      stat.execute("CREATE TRIGGER IF NOT EXISTS 'bh_" + channel + "_scrapedNewBan' AFTER INSERT ON 'bh_" + channel + "_bans' WHEN NEW.timeCreated IS NULL "
              + "BEGIN UPDATE 'bh_" + channel + "_bans' SET timeCreated=timeModified, nick='' WHERE banMask=NEW.banMask; "
              + "INSERT INTO 'bh_" + channel + "_notes'(banId,timestamp,author,note) VALUES (NEW.banId,strftime('%s','now'),'Banhammer','A: Scraped from banlist, set by '||NEW.author); "
              + "END;");

      //Inserting a fresh ban (when timeCreated is present) should log who set it
      stat.execute("CREATE TRIGGER IF NOT EXISTS 'bh_" + channel + "_newBan' AFTER INSERT ON 'bh_" + channel + "_bans' WHEN NEW.timeCreated IS NOT NULL "
              + "BEGIN INSERT INTO 'bh_" + channel + "_notes'(banId,timestamp,author,note) VALUES (NEW.banId,strftime('%s','now'),'Banhammer','A: Ban set by '||NEW.author); "
              + "END;");

      //When a ban is set active (from a non-active type)
      //can replace AND OLD.type!='A' with AND OLD.type NOT IN ('A','P','T') when we have the extra types
      stat.execute("CREATE TRIGGER IF NOT EXISTS 'bh_" + channel + "_activateBan' AFTER UPDATE OF type ON 'bh_" + channel + "_bans' WHEN NEW.type='A' AND OLD.type!='A' "
              + "BEGIN INSERT INTO 'bh_" + channel + "_notes'(banId,timestamp,author,note) VALUES (NEW.banId,NEW.timeModified,'Banhammer','A: Ban re-set by '||NEW.author); "
              + "END;");

      //When a ban is set inactive (always due to rotating out)
      stat.execute("CREATE TRIGGER IF NOT EXISTS 'bh_" + channel + "_inactivateBan' AFTER UPDATE OF type ON 'bh_" + channel + "_bans' WHEN NEW.type='I' "
              + "BEGIN INSERT INTO 'bh_" + channel + "_notes'(banId,timestamp,author,note) VALUES (NEW.banId,NEW.timeModified,'Banhammer','I: Rotated out of list'); "
              + "END;");

      //When a ban is unset due to missing from scrape
      stat.execute("CREATE TRIGGER IF NOT EXISTS 'bh_" + channel + "_missingBan' AFTER UPDATE OF type ON 'bh_" + channel + "_bans' WHEN NEW.type='M' "
              + "BEGIN INSERT INTO 'bh_" + channel + "_notes'(banId,timestamp,author,note) VALUES (NEW.banId,NEW.timeModified,'Banhammer','U: Missing from scraped list'); "
              + "UPDATE 'bh_" + channel + "_bans' SET type='U' WHERE banId=NEW.banId; "
              + "END;");
      /*
       * EVIL SIDE EFFECT CODE ENDS
       */

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

  /**
   * Merge the scraped list of +b from channel with the database, inserting new
   * records or marking old records as active, marking missing bans as Unset.
   *
   * @param channel the channel to modify
   * @param scrapedBans list of ScrapedBans: banmask, author, timestamp
   */
  public void upsertScrapedBans(String channel, List<ScrapedBan> scrapedBans) {
    try {
      db.setAutoCommit(false);
      log.debug(channel + " CREATING TEMPORARY BANLIST TABLE...");
      //create temp table for scraped banlist, include type column all 'A'
      Statement stat = db.createStatement();
      stat.execute("DROP TABLE IF EXISTS 'bh_" + channel + "_temp';");
      stat.execute("CREATE TABLE 'bh_" + channel + "_temp'(banmask,author,timeModified);");
      db.commit();

      PreparedStatement insScrapeTemp = db.prepareStatement("INSERT INTO 'bh_" + channel + "_temp'(banmask,author,timeModified) VALUES (?,?,?);");
      for (ScrapedBan ban : scrapedBans) {
        insScrapeTemp.setString(1, ban.banmask);
        insScrapeTemp.setString(2, ban.author);
        insScrapeTemp.setString(3, ban.timestamp);
        insScrapeTemp.execute();
      }
      db.commit();
      insScrapeTemp.close();
      log.debug(channel + " DONE temp banlist table");

      log.debug(channel + " MERGE BANLIST TO DATABASE, MARK BANS ACTIVE...");
      stat.execute("INSERT OR IGNORE INTO 'bh_" + channel + "_bans'(type,banmask,author,timeModified) SELECT 'A',* FROM 'bh_" + channel + "_temp';");
      stat.execute("UPDATE 'bh_" + channel + "_bans' SET type='A' WHERE banmask IN (SELECT banmask FROM 'bh_" + channel + "_temp');");
      db.commit();

      log.debug(channel + " MERGE DATABASE TO BANLIST, MARK MISSING BANS...");
      stat.execute("UPDATE 'bh_" + channel + "_bans' SET type='M' WHERE banmask NOT IN (SELECT banmask FROM 'bh_" + channel + "_temp');");
      db.commit();

      log.debug(channel + " DROP TEMPORARY TABLE...");
      stat.execute("DROP TABLE 'bh_" + channel + "_temp';");
      db.commit();

      stat.close();
      db.setAutoCommit(true);
      log.debug(channel + " SCRAPE DONE");
    } catch (SQLException ex) {
      this.handleSQLException(ex);
    } finally {
      try {
        db.setAutoCommit(true);
      } catch (SQLException ex) {
        log.error("Impossible SQLException! : " + ex.getMessage());
      }
    }
  }

  /**
   * Insert or update a new +b in channel.
   *
   * @param channel the channel the ban is in
   * @param banmask the ban
   * @param author nick of who set the ban
   * @param nick nick of who the ban applies to
   */
  public void upsertNewBan(String channel, String banmask, String author, String nick) {
    /*
     * INSERT OR IGNORE INTO
     * bh_#CHANNEL_BANS(type,banmask,usermask,author,timeCreated,timeModified)
     * VALUES ('A',banmask,nick,author,<now>,<now>)
     * UPDATE bh_#channel_bans SET type='A',author=author,
     */
    try {
      PreparedStatement insPrep = db.prepareStatement("INSERT OR IGNORE INTO 'bh_" + channel + "_bans'(type,banmask,author,nick,timeCreated,timeModified) VALUES ('A',?,?,?,strftime('%s','now'),strftime('%s','now'));");
      PreparedStatement updPrep = db.prepareStatement("UPDATE 'bh_" + channel + "_bans' SET type='A',author=?,timeModified=strftime('%s','now') WHERE banmask=?;");
      insPrep.setString(1, banmask);
      insPrep.setString(2, author);
      insPrep.setString(3, nick);
      insPrep.executeUpdate();
      updPrep.setString(1, author);
      updPrep.setString(2, banmask);
      updPrep.executeUpdate();
      insPrep.close();
      updPrep.close();
      log.info(channel + " INSERTED NEW BAN ON " + banmask + " BY " + author);
    } catch (SQLException ex) {
      this.handleSQLException(ex);
    }
  }

  /**
   * Update a new -b in channel.
   * 
   * @param channel the channel the ban was unset in
   * @param banmask the ban
   * @param author nick of who unset it
   */
  public void unsetBan(String channel, String banmask, String author) {
    try {
      PreparedStatement prep = db.prepareStatement("UPDATE 'bh_" + channel + "_bans' SET type='U',author=? WHERE banmask=?;");
      prep.setString(1, author);
      prep.setString(2, banmask);
      prep.executeUpdate();
      prep.close();
      log.info(channel + " UPDATED BAN UNSET ON " + banmask + " BY " + author);
    } catch (SQLException ex) {
      this.handleSQLException(ex);
    }
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
