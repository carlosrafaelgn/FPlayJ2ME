//
// MessageThread.java is distributed under the FreeBSD License
//
// Copyright (c) 2012, Carlos Rafael Gimenes das Neves
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those
// of the authors and should not be interpreted as representing official policies,
// either expressed or implied, of the FreeBSD Project.
//
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/MessageThread.java
//

package baseUI;

public final class MessageThread extends Thread {
	private final MessageListener target;
	private int message, iParam, interval;
	private Object oParam;
	
	public MessageThread(MessageListener target) {
		super("MessageThread");
		this.target = target;
	}
	
	public MessageThread(MessageListener target, String name) {
		super(name);
		this.target = target;
	}
	
	public final void run() {
		if (interval <= 0) {
			target.eventMessage(message, iParam, oParam);
		} else {
			while (interval > 0) {
				try {
					synchronized (this) {
						this.wait(interval);
					}
				} catch (Exception ex) {
					break;
				}
				if (interval > 0) target.eventMessage(message, iParam, oParam);
			}
		}
	}
	
	public final void start() {
		super.start();
	}
	
	public final void start(int message) {
		this.message = message;
		super.start();
	}
	
	public final void start(int message, int iParam, Object oParam) {
		this.message = message;
		this.iParam = iParam;
		this.oParam = oParam;
		super.start();
	}
	
	public final void startInterval(int interval) {
		this.message = Main.SYSMSG_TIMER;
		this.oParam = this;
		this.interval = interval;
		super.start();
	}
	
	public final void startInterval(int interval, int iParam) {
		this.message = Main.SYSMSG_TIMER;
		this.iParam = iParam;
		this.oParam = this;
		this.interval = interval;
		super.start();
	}
	
	public final void interrupt() {
		if (interval <= 0) {
			super.interrupt();
		} else {
			interval = 0;
			synchronized (this) {
				this.notify();
			}
		}
	}
}
