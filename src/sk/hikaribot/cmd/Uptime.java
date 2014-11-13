/*
 * hikaribot - Uptime
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import sk.hikaribot.api.Command;
import org.jibble.pircbot.Colors;

/**
 * Prints uptime of bot.
 */
public class Uptime extends Command {

  public Uptime() {
    this.name = "uptime";
    this.numArgs = 0;
    this.helpInfo = "prints uptime";
    this.reqPerm = 1; //voice
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    long started = bot.getTimeStarted();
    long now = System.currentTimeMillis();
    long ms = now - started;

    long x = ms / 1000;
    long secsElapsed = x % 60;
    x /= 60;
    long minsElapsed = x % 60;
    x /= 60;
    long hrsElapsed = x % 24;
    x /= 24;
    long daysElapsed = x;

    String timeElapsed = String.format("%02d", hrsElapsed) + ":" + String.format("%02d", minsElapsed) + ":" + String.format("%02d", secsElapsed);
    bot.sendMessage(channel, Colors.BLUE + "UPTIME: " + Colors.NORMAL + "up " + daysElapsed + " days, " + timeElapsed);
    log.info("UPTIME from " + sender + " in " + channel);
  }

}
