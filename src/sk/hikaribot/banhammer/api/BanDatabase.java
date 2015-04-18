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
 * Provides queries to and from database for listing/updating bans. This should
 * probably be an interface->implementation, to support non-SQLite, but I just
 * want it done.
 *
 * @author Shizuka Kamishima
 */
public class BanDatabase {
  
  private static final Logger log = LogManager.getLogger("BHDB");
  private final Connection db;
  private final HikariBot bot;

  public BanDatabase(HikariBot bot) {
    this.bot = bot;
    this.db = bot.getDatabase();
  }
  
  /*
   * get list of inactive banmasks - SELECT banId, banMask FROM bans WHERE type='I'
   * HashMap int banId, String banmask
   * 
   * get list of active banmasks - SELECT banId, banMask FROM bans WHERE type IN ('A', 'P', 'T')
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
