/*
 * hikaribot - ListenerFollowUser
 * Shizuka Kamishima - 2014-11-18
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
package sk.hikaribot.twitter.cmd;

import java.util.List;
import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.exception.*;
import sk.hikaribot.twitter.TwitListener;

/**
 * Class description
 *
 * @author Shizuka Kamishima
 */
public class ListenerFollowUser extends Command {

  public ListenerFollowUser() {
    this.name = "twitFollow";
    this.numArgs = 2;
    this.helpArgs.add("add|rem|list");
    this.helpArgs.add("user ID");
    this.helpInfo = "modify users to echo tweets from, list needs no args, 'rem all' clears list";
    this.reqPerm = 3;
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    String[] args = params.split(" ");
    if ("list".equals(args[0])) {
      //print list of users we're following, ignore further args
      /*TwitListener twl = bot.getTwitBot().getListener();
      List<String> users = twl.getUsersFollowing();
      String userlist = "";
      for (String user : users) {
      userlist += user + ", ";
      }
      bot.sendMessage(channel, Colors.BLUE + "TWITFOLLOW " + Colors.OLIVE + "Following: " + Colors.NORMAL + userlist);*/
    } else if (args.length != numArgs) {
      throw new ImproperArgsException(this.name);
    } else {
      log.trace(args[0] + " " + args[1]);
      TwitListener twl = bot.getTwitBot().getListener();
      switch (args[0]) {
        case "add":
          if ("all".equals(args[1])) {
            throw new ImproperArgsException(this.name);
          }
          try {
            twl.followUser(args[1]);
          } catch (InvalidFollowException ex) {
            //already following, do nothing
            break;
          }
          log.info("TWITFOLLOW ADD " + args[1] + " FROM " + sender + " in " + channel);
          break;
        case "rem":
          if ("all".equals(args[1])) {
            twl.unfollowAll();
            bot.sendMessage(channel, Colors.BLUE + "TWITFOLLOW: " + Colors.NORMAL + "Unfollowed all users");
            break;
          }
          try {
            twl.unfollowUser(args[1]);
          } catch (InvalidFollowException ex) {
            //not following yet, don't worry about it
            break;
          }
          log.info("TWITFOLLOW REM " + args[1] + " FROM " + sender + " in " + channel);
          break;
        default:
          throw new ImproperArgsException(this.name);
      }
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
