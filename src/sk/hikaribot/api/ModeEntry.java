/*
 * hikaribot - ModeEntry
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
package sk.hikaribot.api;

/**
 * A single entry in a list of modes set in a channel.
 * 
 * @author Shizuka Kamishima
 */
public class ModeEntry {
  
  /**
   * The mode. Generally 'b', but could be e/I/q, given by ModelistResponse.
   */
  public String mode;
  
  /**
   * Channel the mode is set in.
   */
  public String channel;
  
  /**
   * Mask the mode applies to. Could be wildcarded, *!*@foo.bar.baz
   */
  public String mask;
  
  /**
   * The usermask of who set this mode.
   */
  public String usermaskWhoSet;
  
  /**
   * Unix timestamp when the mode was set. Could be an int I suppose.
   */
  public String timestamp;

  public ModeEntry(String channel, String mode, String mask, String usermaskWhoSet, String timestamp) {
    this.channel = channel;
    this.mode = mode;
    this.mask = mask;
    this.usermaskWhoSet = usermaskWhoSet;
    this.timestamp = timestamp;
  }
}
