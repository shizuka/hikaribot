/*
 * hikaribot - Database
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
package sk.hikaribot.api;

import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton to get connection to SQLite database. Should probably be an
 * interface->implementation but I just want it done. Unlikely to use another
 * type of DB anyway.
 *
 * @author Shizuka Kamishima
 */
public class Database {

  private static final Logger log = LogManager.getLogger("Database");
  private final Connection db;
  
  public Database(String dbPath) throws ClassNotFoundException, SQLException{
    Class.forName("org.sqlite.JDBC");
    this.db = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    log.debug("Database " + dbPath + " connected");
  }

  public void disconnect() throws SQLException {
    db.close();
  }
  
  public Connection getDatabase() {
    return this.db;
  }
}
