/*
 * hikaribot - BanCount
 * Shizuka Kamishima - 2015-04-11
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
package sk.hikaribot.banhammer.cmd;

import java.util.Observable;
import java.util.Observer;
import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.ModeResponse;
import sk.hikaribot.api.exception.ImproperArgsException;

/**
 * Class description
 * 
 * @author Shizuka Kamishima
 */
public class BanCount extends Command implements Observer {

  public BanCount() {
    this.name = "howManyBans";
    this.helpInfo = "counts number of bans set in channel";
    this.numArgs = 1;
    this.helpArgs.add("channel");
    this.reqPerm = 0;
  }
  
  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    ModeResponse mr = new ModeResponse(params, "b", channel);
    mr.addObserver(this);
    bot.getServerResponder().addObserver(mr);
    bot.sendRawLine("MODE " + channel + " +b");
    log.info("HOWMANYBANS IN " + params + " FROM " + sender + " IN " + channel);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    this.execute(channel, sender, channel);
  }

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof ModeResponse) {
      ModeResponse mr = (ModeResponse) arg;
      bot.getServerResponder().deleteObserver((Observer) o);
      bot.sendMessage(mr.getReplyToChannel(), Colors.BLUE + "BANHAMMER: " + Colors.NORMAL + "There are " + Colors.OLIVE + mr.getEntries().size() + Colors.NORMAL + " bans in " + mr.getChannel());
    }
  }

}
