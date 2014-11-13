/*
 * hikaribot - Command Interface
 * Shizuka Kamishima - 2014-11-06
 * Interface
 */

/*
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
package sk.hikaribot.api;

import sk.hikaribot.api.exception.ImproperArgsException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.bot.HikariBot;

/**
 * Defines a :command to be run from IRC.
 *
 * @author Shizuka Kamishima
 */
public abstract class Command {

  protected static final Logger log = LogManager.getLogger("Cmd");

  /**
   * Command name, 'foo' in ":foo [bar] - baz" helpInfo string.
   */
  public String name = "command";

  /**
   * Number of arguments this command takes.
   */
  public int numArgs = 0;

  /**
   * Command argument (if any), 'bar' in ":foo [bar] - baz" helpInfo string.
   */
  public List<String> helpArgs = new ArrayList();

  /**
   * Invocation helpInfo, 'baz' in ":foo [bar] - baz" helpInfo string.
   */
  public String helpInfo = "help not implemented for this command";

  /**
   * Required permission level to invoke. Defaults to owner.
   *
   * 0 - any user 1 - voice in channel 2 - op in channel 3 - owner (can command
   * in pm)
   */
  public int reqPerm = 0;
  
  /**
   * The HikariBot we act upon.
   */
  protected HikariBot bot;

  /**
   * MUST define Strings name and info, and SHOULD define permissions/source
   *
   * @return this Command
   */
  public Command Command() {
    this.bot = null;
    return this;
  }

  /**
   * Initialize this Command with bot and registry.
   *
   * @param bot HikariBot we act on
   */
  public void setup(HikariBot bot) {
    this.bot = bot;
  }

  /**
   * Self evident. Should be called only if invoker is allowed.
   *
   * @param channel channel command was sent from
   * @param sender nick of who sent the command
   * @param params the rest of the command line after stripping command and delimiter
   * @throws ImproperArgsException if command had the wrong amount of arguments passed
   */
  public abstract void execute(String channel, String sender, String params) throws ImproperArgsException;

  /**
   * Self evident. Should be called only if invoker is allowed.
   *
   * @param channel channel command was sent from
   * @param sender nick of who sent the command
   * @throws ImproperArgsException if command requires arguments
   */
  public abstract void execute(String channel, String sender) throws ImproperArgsException;

}
