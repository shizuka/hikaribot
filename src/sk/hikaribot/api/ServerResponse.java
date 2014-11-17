/*
 * hikaribot - ServerResponse
 * Shizuka Kamishima - 2014-11-13
 * Observable
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
package sk.hikaribot.api;

import java.util.Observable;
import java.util.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Middleman between HikariBot.onServerResponse and commands that need to hear
 * them.
 *
 * @author Shizuka Kamishima
 */
public class ServerResponse extends Observable {

  private static final Logger log = LogManager.getLogger("ServerResponse");

  private String line;

  /**
   * Notify all Observers of a new server response in form "code|response line".
   * Effectively a mirror of HikariBot.onServerResponse, but usable by everyone.
   *
   * @param code response code, bot has enum of them all
   * @param response incoming line after stripping everything up to the code
   */
  public void onServerResponse(int code, String response) {
    this.line = code + ":" + response;
    this.setChanged();
    this.notifyObservers(line);
  }

  @Override
  public synchronized void addObserver(Observer o) {
    super.addObserver(o);
    log.trace("server response observer added");
  }

  @Override
  public synchronized void deleteObserver(Observer o) {
    super.deleteObserver(o);
    log.trace("server response observer removed");
  }

}
