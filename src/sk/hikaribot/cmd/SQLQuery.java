/*
 * hikaribot - SQLQuery
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
package sk.hikaribot.cmd;

import java.sql.*;
import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.exception.ImproperArgsException;

/**
 * Performs arbitrary SQL query against database.
 *
 * @author Shizuka Kamishima
 */
public class SQLQuery extends Command {

  public SQLQuery() {
    this.name = "sql";
    this.numArgs = 1;
    this.helpArgs.add("query");
    this.helpInfo = "perform SQL query on database - use with caution!";
    this.reqPerm = 4;
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length < numArgs) {
      throw new ImproperArgsException(this.name);
    }
    log.info("SQLSELECT FROM " + sender + " IN " + channel + ": " + params);
    Connection db = bot.getDatabase();
    try {
      Statement stat = db.createStatement();
      ResultSet rs = stat.executeQuery(params);
      ResultSetMetaData rsmd = rs.getMetaData();

      int numOfColumns = rsmd.getColumnCount();
      String out = "";
      for (int i = 1; i <= numOfColumns; i++) {
        if (i > 1) {
          out += "|";
        }
        out += rsmd.getColumnName(i);
      }
      log.debug(out);
      while (rs.next()) {
        out = "";
        for (int i = 1; i <= numOfColumns; i++) {
          if (i > 1) {
            out += "|";
          }
          out += rs.getString(i);
        }
        log.debug(out);
      }
      bot.sendMessage(channel, Colors.BLUE + "SQL: " + Colors.NORMAL + "See log for query results");
      stat.close();
    } catch (SQLException ex) {

      if (ex.getMessage().equals("query does not return ResultSet")) {
        log.debug("No returns needed");
        bot.sendMessage(channel, Colors.DARK_GREEN + "SQL: " + Colors.NORMAL + "Query successful");
      } else {
        log.error("SQLException: " + ex.getMessage());
        bot.sendMessage(channel, Colors.BROWN + "SQLSelect: " + Colors.NORMAL + ex.getMessage());
      }
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
