/*
 * hikaribot - Command Interface
 * Shizuka Kamishima - 2014-11-06
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.PircBot;

/**
 * Defines a :command to be run from IRC.
 *
 * @author Shizuka Kamishima
 */
public abstract class Command {
  
  protected static final Logger log = LogManager.getLogger("Cmd");
  
  /**
   * Command name, 'foo' in ":foo [bar] - baz" help string.
   */
  public String name = "command";
  
  /**
   * Command argument (if any), 'bar' in ":foo [bar] - baz" help string.
   */
  public String arg = "";

  /**
   * Invocation info, 'baz' in ":foo [bar] - baz" help string.
   */
  public String info = "help not implemented for this command";

  /**
   * Required permission level to invoke. Defaults to owner.
   * 
   * 0 - any user
   * 1 - voice in channel
   * 2 - op in channel
   * 3 - owner (can command in pm)
   */
  public int reqPerm = 0;
  
  /**
   * The bot we act on. Commands must super() pass the bot.
   */
  protected PircBot bot;
  
  /**
   * MUST define Strings name and info, and SHOULD define permissions/source
   * @return 
   */
  public Command Command() {
    this.bot = null;
    return this;
  }
  
  public void setBot(PircBot bot) {
    this.bot = bot;
  }
  
  /**
   * Self evident. Should be called only if invoker is allowed.
   *
   * @param channel channel command was sent from
   * @param sender who sent this command
   * @param message the rest of the line after stripping :command
   */
  public abstract void execute(String channel, String sender, String message);
}
