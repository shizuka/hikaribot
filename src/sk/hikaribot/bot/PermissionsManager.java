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

import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Maintains list of user accounts and their permissions levels.
 *
 * @author Shizuka Kamishima
 */
public class PermissionsManager {

  private static final Logger log = LogManager.getLogger("Permissions");

  private final HikariBot bot;
  private final CommandRegistry cr;
  private final HashMap<String, Integer> accounts;

  public PermissionsManager(HikariBot bot) {
    log.debug("PermissionsManager started...");
    this.bot = bot;
    this.cr = bot.getCommandRegistry();
    this.accounts = new HashMap();
    this.accounts.put(bot.getOwner(), 99);
    //TODO - load permissions.properties and populate accounts list with them
    //TODO - dedicated registerAccount(canonicalNick) to save accounts to properties
  }

  public int getUserLevel(String nick) {
    if (this.accounts.containsKey(nick)) {
      return accounts.get(nick);
    } else {
      return 0;
    }
  }

}
