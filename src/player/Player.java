//
// Player.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/player/Player.java
//

package player;

import baseUI.Main;
import baseUtil.Vector;
import javax.microedition.media.Manager;
import javax.microedition.media.control.VolumeControl;
import ui.Behaviour;

public final class Player implements javax.microedition.media.PlayerListener, Runnable {
	//private static final int[] VolumeLevels = { 0, 5, 15, 25, 35, 45, 55, 65, 75, 85, 100 };
	
	private static final int VolumeGranularity = 5;
	private static final int MinimumVolume = 0;
	private static final int MaximumVolume = 100;
	private static final String[] SupportedFormats;
	
	private VolumeControl ctrlVol, ctrlVolNext;
	private javax.microedition.media.Player mplayer, mplayerNext, mplayerLostDevice;
	private int timeTrackMS;
	private int timeSec;
	private Song currentSong, nextSong; //song currently being played (or null if none)
	
	private PlayerListener listener;
	private int volume; //0 to 10 (0 = mute)
	private boolean paused, radioMode, radioStereo, reloadTime;
	private String totalTime;
	private int totalTimeMS;
	private boolean playAfterRecovery;
	private boolean alive;
	private final Vector actionsPeding;
	
	static {
		final Vector fmts = new Vector(16);
		
		try {
			String[] ss = Manager.getSupportedContentTypes("file");
			if (ss != null && ss.length > 0) {
				for (int i = 0; i < ss.length; i++) {
					String tmpFmt = ss[i].toLowerCase();
					if (tmpFmt.startsWith("audio")) {
						tmpFmt = tmpFmt.substring(6);
						int idx = tmpFmt.lastIndexOf('-');
						if (idx >= 0) {
							tmpFmt = tmpFmt.substring(idx + 1);
						}
						if (!fmts.contains(tmpFmt)) {
							fmts.addElement(tmpFmt);
						}
					}
				}
				fmts.sortText();
			}
		} catch (Throwable ex) {
		}
		
		SupportedFormats = new String[fmts.size()];
		fmts.copyInto(SupportedFormats);
	}
	
	public static String[] getSupportedFormats() {
		return SupportedFormats;
	}
	
	public static boolean isFileSupported(String fileName) {
		int x = fileName.lastIndexOf('.');
		if (x < 0) return false;
		fileName = fileName.substring(x + 1).toLowerCase();
		
		int start = 0;
		int end = SupportedFormats.length - 1;
		x = (end + 1) >> 1;
		
		do {
			final int r = fileName.compareTo(SupportedFormats[x]);
			if (r < 0) {
				end = x - 1;
			} else if (r > 0) {
				start = x + 1;
			} else {
				return true;
			}
			x = start + ((end - start + 1) >> 1);
		} while (start <= end);
		
		return false;
	}
	
	public Player(int volume, int lasttime, Song currentSong, PlayerListener listener, boolean radioMode, boolean radioStereo) {
		this.actionsPeding = new Vector(4, 1);
		this.alive = true;
		this.timeTrackMS = lasttime;
		
		if (!radioMode && currentSong == null)
			this.timeTrackMS = 0;
		
		this.currentSong = currentSong;
		this.volume = ((volume > MaximumVolume) ? MaximumVolume : ((volume < MinimumVolume) ? MinimumVolume : volume));
		this.radioMode = radioMode;
		this.radioStereo = radioStereo;
		setListener(listener);
		
		(new Thread(this, "Actions")).start();
		
		reloadLast();
	}
	
	private final void setGlobalVolume_(int volume) {
		final VolumeControl control = (VolumeControl)javax.microedition.amms.GlobalManager.getControl("javax.microedition.media.control.VolumeControl");
		if (control != null) {
			control.setLevel(volume);
		}
	}
	
	public final void setGlobalVolume(int volume) {
		try {
			if (java.lang.Class.forName("javax.microedition.amms.GlobalManager") != null) {
				setGlobalVolume_(volume);
			}
		} catch (Throwable ex) {
		}
	}

	private final void cleanupMsg(String where, String fileName, String errMsg, boolean failQuiet) {
		stop_(true);
		if (!failQuiet && fileName != null) {
			if (errMsg == null)
				Main.alertShow(where + " Não é possível tocar: " + fileName, true);
			else
				Main.alertShow(where + " " + fileName + ": " + errMsg, true);
		}
		System.gc();
	}
	
	public final void setListener(PlayerListener listener) {
		if (this.listener != null) {
			this.listener.detached();
		}
		this.listener = listener;
		if (this.listener != null) {
			this.listener.attached();
		}
	}
	
	public final PlayerListener getListener() {
		return this.listener;
	}
	
	public final boolean isRadioMode() {
		return radioMode;
	}
	
	public final void setRadioMode(boolean radioMode, Song currentSong) {
		stop_(true);
		
		this.radioMode = radioMode;
		this.currentSong = currentSong;
		
		reloadLast();
	}
	
	public final boolean isRadioStereo() {
		return radioStereo;
	}
	
	public final void setRadioStereo(boolean radioStereo) {
		this.radioStereo = radioStereo;
		
		if (radioMode) {
			//if it was playing, restart the playback
			if (!paused && isSongLoaded()) {
				play(currentSong);
			} else {
				stop();
			}
		}
	}
	
	public final Song getCurrentSong() {
		return currentSong;
	}
	
	public final boolean isSongLoaded() {
		return (mplayer != null);
	}
	
	public final boolean isPaused() {
		return paused;
	}
	
	public final int getTimeTrackMS() {
		return timeTrackMS;
	}
	
	public final int getCurrentPlayTime() {
		return timeSec;
	}
	
	public final String getCurrentSongLength() {
		return totalTime;
	}
	
	public final void terminate() {
		alive = false;
		setAction(PlayerAction.terminate());
		waitTermination(2500);
	}
	
	private final void waitTermination(int timeoutMS) {
		final long startTime = System.currentTimeMillis();
		while (!alive && ((int)(System.currentTimeMillis() - startTime) < timeoutMS)) {
			try {
				Thread.sleep(10);
			} catch (Throwable ex) {
			}
		}
	}
	
	public final void clearVolumeControl() {
		setAction(PlayerAction.clearVolumeControl());
	}
	
	public final int getVolume() {
		return volume;
	}
	
	public final void setVolume(int volume) {
		setAction(PlayerAction.setVolume((volume > MaximumVolume) ? MaximumVolume : ((volume < MinimumVolume) ? MinimumVolume : volume)));
	}
	
	public final void volumeDown() {
		setAction(PlayerAction.volumeDown());
	}
	
	public final void volumeUp() {
		setAction(PlayerAction.volumeUp());
	}
	
	public final void pause() {
		setAction(PlayerAction.pause(mplayer));
	}
	
	public final void play(Song song) {
		if (song != null)
			setAction(PlayerAction.playSong(song));
	}
	
	public final void clearNext() {
		setAction(PlayerAction.clearNext());
	}
	
	public final void clearNextAndWait() {
		setActionWaitToComplete(PlayerAction.clearNext());
	}
	
	public final void stopAndClear() {
		setAction(PlayerAction.stopAndClear());
	}
	
	public final void stop() {
		setAction(PlayerAction.stop());
	}
	
	public final void stopAndWait() {
		setActionWaitToComplete(PlayerAction.stop());
	}
	
	private final void reloadLast() {
		if (timeTrackMS != 0 && !radioMode) {
			//load the last played song at the given time
			if (currentSong != null) {
				paused = true;
				setAction(PlayerAction.reloadLast());
			} else {
				paused = false;
				timeTrackMS = 0;
			}
		}
	}
	
	private final void setAction(PlayerAction action) {
		synchronized (actionsPeding) {
			actionsPeding.addElement(action);
			actionsPeding.notify();
		}
	}
	
	private final void setActionWaitToComplete(PlayerAction action) {
		synchronized (actionsPeding) {
			actionsPeding.addElement(action);
			actionsPeding.notify();
		}
		
		action.waitToComplete();
	}
	
	public final void run() {
		do {
			//check if there is a pending action
			PlayerAction action;
			
			synchronized (actionsPeding) {
				if (actionsPeding.size() > 0) {
					action = (PlayerAction) actionsPeding.elementAt(0);
					actionsPeding.removeElementAt(0);
				} else {
					action = null;
				}
				
				if (action == null) {
					try {
						//if paused or does not have a loaded song, wait until
						//notified
						if (paused || mplayer == null) {
							//must not call wait() because that way
							//the screen saver would never activate
							actionsPeding.wait(1000); 
						} else {
							actionsPeding.wait(100);
						}
					} catch (Throwable ex) {
					}
					
					if (actionsPeding.size() > 0) {
						action = (PlayerAction) actionsPeding.elementAt(0);
						actionsPeding.removeElementAt(0);
					}
				}
			}
			
			if (!alive)
				break;
			
			if (action != null) {
				//Main.setThreadProcessing(true);
				
				//if (mplayer != null)
				//	Main.alertShow(Integer.toString(action.getNumber()) + " " + mplayer.toString(), false);
				//else
				//	Main.alertShow(Integer.toString(action.getNumber()) + " null", false);
				
				final int actionNumber = action.getNumber();
				
				switch (actionNumber) {
					case PlayerAction.END_OF_MEDIA:
						if (mplayer == action.getPlayer()) {
							start_(null);
						} else {
							action.getPlayer().close();
						}
						break;
					case PlayerAction.PLAY_SONG:
						if (action.getObject() != null)
							start_((Song)action.getObject());
						break;
					case PlayerAction.SET_VOLUME:
						volume_(((Integer)action.getObject()).intValue());
						break;
					case PlayerAction.VOLUME_DOWN:
						volume_(-1);
						break;
					case PlayerAction.VOLUME_UP:
						volume_(-2);
						break;
					case PlayerAction.PAUSE:
						//if (mplayer == action.getPlayer())
							pause_();
						break;
					case PlayerAction.CLEAR_NEXT:
						clear_(true);
						break;
					case PlayerAction.STOP:
						stop_(true);
						break;
					case PlayerAction.STOP_AND_CLEAR:
						stop_(true);
						timeTrackMS = 0;
						break;
					case PlayerAction.RELOAD_LAST:
						reloadLast_();
						break;
					case PlayerAction.CLEAR_VOLUME_CONTROL:
						ctrlVol = null;
						ctrlVolNext = null;
						break;
					//case PlayerAction.UPDATE_DURATION:
					//	if (mplayer == action.getPlayer())
					//		updateTotalTime_(((Long)action.getObject()).longValue());
					//	break;
						
					case PlayerAction.ERROR:
						if (mplayer == action.getPlayer()) {
							cleanupMsg("Erro em ", currentSong.getTitle(), action.getObject().toString(), false);
						} else if (mplayerNext == action.getPlayer()) {
							clear_(true);
						}
						break;
					case PlayerAction.DEVICE_UNAVAILABLE:
						if (mplayer == action.getPlayer()) {
							deviceUnavailable_();
						}
						break;
					case PlayerAction.DEVICE_AVAILABLE:
						if (mplayerLostDevice == action.getPlayer()) {
							mplayerLostDevice = null;
							if (playAfterRecovery) {
								playAfterRecovery = false;
								pause_();
							}
						} else if (mplayerNext == action.getPlayer() && action.getPlayer() != null) {
							try {
								mplayerNext.realize();
								mplayerNext.prefetch();
							} catch (Throwable ex) {
							}
						}
						break;
					case PlayerAction.STOPPED:
						if (mplayer == action.getPlayer()) {
							//if paused == true, then we have already called
							//pause_, and then the player triggered
							//the STOPPED event, otherwise, an error may have
							//occurred
							if (!paused) {
								deviceUnavailable_();
							}
						}
						break;
				}
				//Main.setThreadProcessing(false);
				
				action.setCompleted();
				
				if (actionNumber != PlayerAction.CLEAR_NEXT) {
					listener.stateChanged();
				}
			} else {
				if (!paused && mplayer != null && !radioMode) {
					//it's not possible to seek in radio, so don't bother
					//keeping track of play time
					
					int t = 0;
					
					try {
						t = (int)(mplayer.getMediaTime() / 1000);
					} catch (Throwable ex) {
						//error getting the current time
						continue;
					}
					
					if (t > (totalTimeMS - 400) && mplayerNext != null) {
						try {
							//350 was 300
							while (alive && t > (totalTimeMS - 350) && mplayer.getState() == javax.microedition.media.Player.STARTED) {
								t = (int)(mplayer.getMediaTime() / 1000);
								Thread.yield();
							}
						} catch (Throwable ex) {
							//error getting the current time
							continue;
						}
						if (alive) {
							start_(null);
							listener.stateChanged();
						}
					} else if (t >= timeTrackMS) {
						if ((t - timeTrackMS) <= 10000) {
							if (!paused && mplayer != null) {
								timeTrackMS = t;
								
								final int ts = t / 1000;
								//refresh the timer display screen if the time has changed
								if (ts != timeSec) {
									timeSec = ts;
									listener.stateChanged();
								}
							}
						} else {
							deviceUnavailable_();
						}
					}
				}
				//check for the screen saver only when there
				//are no actions pending
				Main.SSProcess();
			}
		} while (alive);
		
		stop_(true);
		
		alive = true;
	}
	
	public final void playerUpdate(javax.microedition.media.Player player, String event, Object eventData) {
		//Main.alertShow(event, true);
		//System.err.println(event);
		if (event == STOPPED_AT_TIME || event == END_OF_MEDIA) {
			setAction(PlayerAction.endOfMedia(player));
		//} else if (event == DURATION_UPDATED) {
		//	setAction(PlayerAction.updateDuration(player, (Long)eventData));
		} else if (event == DEVICE_UNAVAILABLE) {
			setAction(PlayerAction.deviceUnavailable(player));
		} else if (event == DEVICE_AVAILABLE) {
			setAction(PlayerAction.deviceAvailable(player));
		} else if (event == ERROR) {
			setAction(PlayerAction.error(player, eventData.toString()));
		} else if (event == STOPPED) {
			setAction(PlayerAction.stopped(player));
		}
		//else {
		//	Main.alertShow(event + " " + eventData.toString() + " " + player.toString(), false);
		//}
		//BUFFERING_STARTED
		//BUFFERING_STOPPED
		//RECORD_ERROR
		//RECORD_STARTED
		//RECORD_STOPPED
		//SIZE_CHANGED
		//STARTED
		//STOPPED
		//STOPPED_AT_TIME
		//VOLUME_CHANGED
	}
	
	private final String playerCreate_(Song song, boolean nextPlayer, boolean startCurrent) {
		javax.microedition.media.Player p = null;
		String errMsg = null;
		if (song != null) {
			try {
				//try to create the player using 2 types of URL!!!
				p = Manager.createPlayer(radioMode ? song.getRadioURL(radioStereo) : song.getDirectURL());
			} catch (Throwable ex) {
				if (radioMode)
					errMsg = ex.getMessage();
			}
			if (p == null && !radioMode) {
				try {
					p = Manager.createPlayer(song.getEncodedURL());
				} catch (Throwable ex) {
					errMsg = ex.getMessage();
				}
			}
			
			if (errMsg == null) {
				if (nextPlayer) {
					try {
						p.realize();
						if (Behaviour.environmentHasVolumeControl()) {
							ctrlVolNext = (VolumeControl)p.getControl("VolumeControl");
						}
						mplayerNext = p;
					} catch (Throwable ex) {
						errMsg = ex.getMessage();
					}
					
					if (errMsg == null) {
						//if the realization worked, try to prefetch the data,
						//but if the prefetch fails, the next player can still
						//be usefull, so, don't set the errMsg!
						try {
							p.prefetch();
						} catch (Throwable ex) {
						}
						
						//addPlayerListener MUST come before start!
						p.addPlayerListener(this);
					} else {
						clear_(true);
					}
				} else {
					try {
						p.realize();
						p.prefetch();
						if (Behaviour.environmentHasVolumeControl()) {
							ctrlVol = (VolumeControl)p.getControl("VolumeControl");
							if (ctrlVol != null) {
								ctrlVol.setLevel(volume);
							}
						}
						//addPlayerListener MUST come before start!
						p.addPlayerListener(this);
						if (startCurrent)
							p.start();
						mplayer = p;
					} catch (Throwable ex) {
						errMsg = ex.getMessage();
					}
				}
			}
		}
		
		return errMsg;
	}
	
	private final void volume_(int volume) {
		if (volume == -1) { //down
			final int d = (this.volume % VolumeGranularity);
			volume = this.volume - ((d == 0) ? VolumeGranularity : d);
		} else if (volume == -2) { //up
			final int d = (this.volume % VolumeGranularity);
			volume = this.volume + ((d == 0) ? VolumeGranularity : (VolumeGranularity - d));
		}
		
		this.volume = ((volume > MaximumVolume) ? MaximumVolume : ((volume < MinimumVolume) ? MinimumVolume : volume));
		
		if (ctrlVol != null) {
			ctrlVol.setLevel(volume);
		}
		if (ctrlVolNext != null) {
			ctrlVolNext.setLevel(volume);
		}
	}
	
	private final void updateTotalTime_(long duration) {
		totalTimeMS = (int)(duration / 1000);
		if (totalTimeMS > 0) {
			final int t = totalTimeMS / 1000;
			StringBuffer sb = new StringBuffer();
			sb.append(" [");
			sb.append(t / 60);
			final int s = t % 60;
			sb.append('\'');
			if (s < 10)
				sb.append('0');
			sb.append(s);
			sb.append("\"]");
			totalTime = sb.toString();
		} else {
			totalTime = " -";
		}
	}
	
	private final void finalPreparations_() {
		if (!radioMode) {
			//when using radio there is no need to prefetch the next song
			
			if (mplayer != null) {
				updateTotalTime_(mplayer.getDuration());
			}
			
			if (Behaviour.environmentGetLoadNextSong()) {
				//try to get the next file
				nextSong = listener.getSong(true, true);
				if (nextSong != null)
					playerCreate_(nextSong, true, false);
			}
		}
	}
	
	private final void reloadLast_() {
		if (currentSong == null || radioMode)
			return;
		
		if (playerCreate_(currentSong, false, false) != null) {
			cleanupMsg("Erro ao recarregar ", currentSong.getTitle(), "", true);
		} else {
			reloadTime = true;
			mplayerLostDevice = mplayer;
			finalPreparations_();
		}
	}
	
	private final void deviceUnavailable_() {
		reloadTime = true;
		mplayerLostDevice = mplayer;
		if (!paused) {
			playAfterRecovery = true;
			pause_();
		} else {
			playAfterRecovery = false;
		}
	}
	
	private final void start_(Song song) {
		paused = false;
		playAfterRecovery = false;
		reloadTime = false;
		mplayerLostDevice = null;
		if (!radioMode) timeTrackMS = 0;
		timeSec = 0;
		totalTime = "";
		totalTimeMS = -1;
		
		final boolean autoCalled = (song == null);
		String errMsg = null;
		
		listener.resetSongCycling();
		
		if (song == null) {
			//don't call getSong(true, false), in order not to update the ui
			song = listener.getSong(true, true);
		}
		
		if (song == nextSong && mplayerNext != null) {
			//if we are asked to play the very next song,
			//and we already have it prepared
			try {
				//before proceeding, try to start playing the next song
				if (Behaviour.environmentHasVolumeControl()) {
					if (ctrlVolNext == null) {
						try {
							ctrlVolNext = (VolumeControl)mplayerNext.getControl("VolumeControl");
						} catch (Throwable ex) {
						}
					}
					if (ctrlVolNext != null) {
						ctrlVolNext.setLevel(volume);
					}
				}
				mplayerNext.start();
				song = nextSong;
				
				//stop the previous current player
				stop_(false);
				
				currentSong = nextSong;
				
				mplayer = mplayerNext;
				ctrlVol = ctrlVolNext;
				
				mplayerNext = null;
				ctrlVolNext = null;
				nextSong = null;
				
				//update the ui here
				if (autoCalled)
					listener.getSong(true, false);
			} catch (Throwable ex) {
				//update the ui here
				if (autoCalled)
					listener.getSong(true, false);
				
				cleanupMsg("Erro ao tocar ", song.getTitle(), ex.getMessage(), autoCalled);
				
				if (autoCalled) {
					//keep on trying...
					song = null;
				} else {
					//give up here
					return;
				}
			}
		} else {
			//stop the player before proceeding
			stop_(true);
			
			//update the ui here
			if (autoCalled)
				listener.getSong(true, false);
			
			if (song == null) {
				//only get the next song, if the user has not provided one
				song = listener.getSong(true, false);
				if (song == null) {
					//it's no longer possible to proceed
					return;
				}
			}
			
			//try to play the given file
			errMsg = playerCreate_(song, false, true);
			if (errMsg != null) {
				cleanupMsg("Erro ao criar ", song.getTitle(), errMsg, autoCalled);
				
				if (autoCalled) {
					//keep on trying...
					song = null;
				} else {
					//give up here
					return;
				}
			} else {
				currentSong = song;
			}
		}
		
		System.gc();
		
		if (autoCalled && song == null) {
			//try to cycle throught the songs in order to find the
			//next valid song
			for (; ; ) {
				currentSong = listener.getSong(true, false);
				if (currentSong == null) break;
				errMsg = playerCreate_(currentSong, false, true);
				if (errMsg != null) {
					cleanupMsg("Erro ao abrir ", currentSong.getTitle(), errMsg, autoCalled);
				} else {
					break;
				}
			}
		}
		
		finalPreparations_();
	}
	
	private final void clear_(boolean nextPlayer) {
		final javax.microedition.media.Player p;
		if (nextPlayer) {
			p = mplayerNext;
			mplayerNext = null;
			ctrlVolNext = null;
			nextSong = null;
		} else {
			p = mplayer;
			mplayer = null;
			ctrlVol = null;
			currentSong = null;
		}
		if (p != null) {
			p.close();
		}
	}
	
	private final void pause_() {
		if (mplayer != null) {
			//try to pause/resume the current file
			if (paused) {
				try {
					//it's not possible to seek in radio!
					if (!radioMode && reloadTime) {
						//we paused due to something that went wrong..
						//try to perform a seek to the last position
						mplayer.prefetch();
						mplayer.setMediaTime((long)timeTrackMS * 1000);
					}
					mplayer.start();
				} catch (Throwable ex) {
					//cleanupMsg("Erro ao retomar ", currentSong.getTitle(), ex.getMessage(), false);
					Main.alertShow("Erro ao retomar: " + ex.getMessage(), true);
					return;
				}
				reloadTime = false;
				paused = false;
			} else {
				try {
					mplayer.stop();
				} catch (Throwable ex) {
					cleanupMsg("Erro ao parar ", currentSong.getTitle(), ex.getMessage(), false);
					return;
				}
				paused = true;
				System.gc();
			}
		} else {
			//simulate a play event
			start_(listener.getSong(false, false));
		}
	}
	
	private final void stop_(boolean fullStop) {
		//kill the update timer (if it exists)
		clear_(false);
		if (fullStop)
			clear_(true);
		paused = false;
		playAfterRecovery = false;
		reloadTime = false;
		mplayerLostDevice = null;
		timeSec = 0;
		totalTime = "";
		totalTimeMS = -1;
	}
}
