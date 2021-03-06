/*
 * hikaribot - GetWhois
 * Shizuka Kamishima - 2014-11-13
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
package sk.hikaribot.cmd;

import java.util.Observable;
import java.util.Observer;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.WhoisResponse;
import sk.hikaribot.api.exception.ImproperArgsException;

/**
 * DEVELOPMENT - Performs WHOIS on target, prints information to console.
 *
 * @author Shizuka Kamishima
 */
public class GetWhois extends Command implements Observer {

  public GetWhois() {
    this.name = "whois";
    this.helpInfo = "does a whois";
    this.numArgs = 1;
    this.helpArgs.add("target");
    this.reqPerm = 3;
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    WhoisResponse wir = new WhoisResponse(params, channel);
    wir.addObserver(this);
    bot.sendWhois(params, wir);
    log.info("WHOIS " + params + " FROM " + sender + " IN " + channel + " SENT");
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

  @Override
  public void update(Observable o, Object arg) {
    //Called when WhoisResponse.onEndOfWhois() is called by HikariBot
    //At this point WhoisResponse is considered complete, fetch bits
    if (arg instanceof WhoisResponse) {
      WhoisResponse wir = (WhoisResponse) arg;
      log.debug("WHOIS COMMAND got whois response for " + wir.getTarget());
      bot.getServerResponder().deleteObserver((Observer) o); //i hope this works
      bot.sendMessage(wir.getChannel(), "Did a whois on " + wir.getTarget() + ", see console for results");
    }
  }

}
