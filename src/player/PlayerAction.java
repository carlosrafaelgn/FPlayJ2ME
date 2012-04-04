//
// PlayerAction.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/player/PlayerAction.java
//

package player;

final class PlayerAction {
	public static final int END_OF_MEDIA = 2;
	public static final int PLAY_SONG = 1;
	public static final int TERMINATE = 0;
	public static final int VOLUME_DOWN = -1; 
	public static final int VOLUME_UP = -2;
	public static final int PAUSE = -3;
	public static final int CLEAR_NEXT = -4;
	public static final int STOP = -5;
	public static final int STOP_AND_CLEAR = -6;
	public static final int RELOAD_LAST = -7;
	public static final int CLEAR_VOLUME_CONTROL = -8;
	public static final int UPDATE_DURATION = -9;
	public static final int ERROR = -10;
	public static final int DEVICE_UNAVAILABLE = -11;
	public static final int DEVICE_AVAILABLE = -12;
	public static final int STOPPED = -13;
	public static final int SET_VOLUME = -14;

	private final int actionNumber;
	private final javax.microedition.media.Player player;
	private final Object actionObject;
	private boolean completed;

	private PlayerAction(int actionNumber, javax.microedition.media.Player player, Object actionObject) {
		this.actionNumber = actionNumber;
		this.player = player;
		this.actionObject = actionObject;
	}

	public static PlayerAction endOfMedia(javax.microedition.media.Player player) {
		return new PlayerAction(END_OF_MEDIA, player, null);
	}

	public static PlayerAction playSong(Song song) {
		return new PlayerAction(PLAY_SONG, null, song);
	}

	public static PlayerAction terminate() {
		return new PlayerAction(TERMINATE, null, null);
	}
	
	public static PlayerAction setVolume(int volume) {
		return new PlayerAction(SET_VOLUME, null, new Integer(volume));
	}
	
	public static PlayerAction volumeDown() {
		return new PlayerAction(VOLUME_DOWN, null, null);
	}

	public static PlayerAction volumeUp() {
		return new PlayerAction(VOLUME_UP, null, null);
	}

	public static PlayerAction pause(javax.microedition.media.Player player) {
		return new PlayerAction(PAUSE, player, null);
	}

	public static PlayerAction clearNext() {
		return new PlayerAction(CLEAR_NEXT, null, null);
	}

	public static PlayerAction stop() {
		return new PlayerAction(STOP, null, null);
	}

	public static PlayerAction stopAndClear() {
		return new PlayerAction(STOP_AND_CLEAR, null, null);
	}

	public static PlayerAction reloadLast() {
		return new PlayerAction(RELOAD_LAST, null, null);
	}

	public static PlayerAction clearVolumeControl() {
		return new PlayerAction(CLEAR_VOLUME_CONTROL, null, null);
	}

	public static PlayerAction updateDuration(javax.microedition.media.Player player, Long duration) {
		return new PlayerAction(UPDATE_DURATION, player, duration);
	}

	public static PlayerAction error(javax.microedition.media.Player player, String errorMessage) {
		return new PlayerAction(ERROR, player, errorMessage);
	}

	public static PlayerAction deviceAvailable(javax.microedition.media.Player player) {
		return new PlayerAction(DEVICE_AVAILABLE, player, null);
	}

	public static PlayerAction deviceUnavailable(javax.microedition.media.Player player) {
		return new PlayerAction(DEVICE_UNAVAILABLE, player, null);
	}

	public static PlayerAction stopped(javax.microedition.media.Player player) {
		return new PlayerAction(STOPPED, player, null);
	}
	
	public final int getNumber() {
		return actionNumber;
	}

	public final Object getObject() {
		return actionObject;
	}

	public final javax.microedition.media.Player getPlayer() {
		return player;
	}

	public final void waitToComplete() {
		while (!completed) {
			try {
				synchronized (this) {
					this.wait(10);
				}
			} catch (Exception ex) {
				break;
			}
		}
	}

	public final void waitToComplete(int timeoutMS) {
		final long startTime = System.currentTimeMillis();
		while (!completed && ((int)(System.currentTimeMillis() - startTime) < timeoutMS)) {
			try {
				synchronized (this) {
					this.wait(10);
				}
			} catch (Exception ex) {
				break;
			}
		}
	}

	public final void setCompleted() {
		synchronized (this) {
			completed = true;
			this.notify();
		}
	}
}
