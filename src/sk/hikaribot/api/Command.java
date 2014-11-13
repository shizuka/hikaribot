/*
 * hikaribot - Command Interface
 * Shizuka Kamishima - 2014-11-06
 * Licensed under bsd3
 */
package sk.hikaribot.api;

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

  public static class ImproperArgsException extends Exception {

    public ImproperArgsException(String command) {
      super(command);
    }
  }
}
