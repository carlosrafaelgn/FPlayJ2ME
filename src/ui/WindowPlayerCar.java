//
// WindowPlayerCar.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/WindowPlayerCar.java
//

package ui;

import baseUI.Command;
import baseUI.Main;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;
import player.Equalizer;
import player.Player;
import player.PlayerListener;
import player.Song;

final class WindowPlayerCar extends Window implements PlayerListener {
	private final Player player;
	private final Equalizer equalizer;
	private final WindowPlayer windowPlayer;
	private final Command commandBack, commandExit, commandPrev, commandPause, commandNext;
	private final StringBuffer titleText;
	private final String txtEq;
	private long lastPressTime;
	private boolean isMoving;
	private int movThres;
	private int btnEqY, btnEqW, btnEqTxtX, btnEqTxtY;
	private int x1, x2, y1, y2;
	
	private static final int MOVEMENT_CLICK = -1;
	private static final int MOVEMENT_DEADZONE = -2;
	private static final int MOVEMENT_LEFT = 0x11;
	private static final int MOVEMENT_RIGHT = 0x12;
	private static final int MOVEMENT_UP = 0x21;
	private static final int MOVEMENT_DOWN = 0x22;
	
	public WindowPlayerCar(Player player, Equalizer equalizer, WindowPlayer windowPlayer, Command commandExit, Command commandPrev, Command commandPause, Command commandNext) {
		this.player = player;
		this.equalizer = equalizer;
		this.windowPlayer = windowPlayer;
		this.commandBack = Main.commandBack();
		this.commandExit = commandExit;
		this.commandPrev = commandPrev;
		this.commandPause = commandPause;
		this.commandNext = commandNext;
		this.titleText = new StringBuffer(32);
		this.txtEq = " Eq... ";
		
		player.setListener(this);
		
		windowPlayer.setCarMode(true);
	}
	
	protected final Command getLeftCommand() {
		return commandBack;
	}
	
	protected final Command getMiddleCommand() {
		return null;
	}
	
	protected final Command getRightCommand() {
		return commandExit;
	}
	
	protected final void eventResize() {
		super.eventResize();
		
		btnEqW = Main.FontTitle.stringWidth(txtEq) << 1;
		btnEqTxtX = btnEqW >> 2;
		btnEqTxtY = getHeight() - Main.FontTitle.userHeight - (Main.FontTitle.height >> 1);
		btnEqY = (getHeight() - (Main.FontTitle.userHeight << 1));
		
		movThres = Math.min(getWidth(), getHeight()) >> 1;
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandBack)) {
			windowPlayer.setCarMode(false);
			player.setListener(windowPlayer);
			close();
		} else if (command.equals(commandExit)) {
			windowPlayer.eventCommand(commandExit);
		}
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Behaviour.KeyPause ||
			keyCode == Behaviour.KeyPrev ||
			keyCode == Behaviour.KeyNext ||
			keyCode == Behaviour.KeyVolDn ||
			keyCode == Behaviour.KeyVolUp) {
			return windowPlayer.eventKeyPress(keyCode, repeatCount);
		}
		return false;
	}
	
	private final int getMovementType() {
		final int mX = Math.abs(x2 - x1);
		final int mY = Math.abs(y2 - y1);
		final int max = ((mX > mY) ? mX : mY);
		
		if (max <= movThres) {
			//only allow small movements to be seen as clicks 
			if (max >= (movThres / 3)) return MOVEMENT_DEADZONE;
			
			return MOVEMENT_CLICK;
		} else if (mX > movThres) {
			return ((x2 < x1) ? MOVEMENT_LEFT : MOVEMENT_RIGHT);
		} else {
			return ((y2 < y1) ? MOVEMENT_UP : MOVEMENT_DOWN);
		}
	}
	
	protected final void eventPointerDown(int x, int y) {
		lastPressTime = System.currentTimeMillis();
		x1 = x;
		x2 = x;
		y1 = y;
		y2 = y;
		isMoving = true;
	}
	
	protected final boolean eventPointerMove(int x, int y) {
		if (!isMoving) return true;
		
		x2 = x;
		y2 = y;
		
		switch (getMovementType()) {
			case MOVEMENT_LEFT:
				windowPlayer.eventCommand(commandPrev);
				break;
				
			case MOVEMENT_RIGHT:
				windowPlayer.eventCommand(commandNext);
				break;
				
			case MOVEMENT_UP:
				player.volumeUp();
				break;
				
			case MOVEMENT_DOWN:
				player.volumeDown();
				break;
				
			default:
				return true;
		}
		
		isMoving = false;
		Main.environmentDoTouchFeedback();
		
		return true;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		if (!isMoving) return;
		
		isMoving = false;
		
		x2 = x;
		y2 = y;
		
		//ignore short actions
		if (((int)(System.currentTimeMillis() - lastPressTime)) < 75) return;
		
		switch (getMovementType()) {
			case MOVEMENT_CLICK:
				if (x1 < btnEqW && y1 >= btnEqY) {
					if (!equalizer.isAlive()) {
						Main.alertShow("Equalizador não suportado!", true);
					} else {
						Main.openWindow(new WindowEqualizer(equalizer));
					}
				} else {
					windowPlayer.eventCommand(commandPause);
				}
				Main.environmentDoTouchFeedback();
				break;
		}
	}

	protected final void paintContents(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		final Song cursong = player.getCurrentSong();
		final String title;
		
		titleText.delete(0, titleText.length());
		
		if (cursong == null) {
			title = "[Nenhuma música]";
		} else {
			title = cursong.getTitle();
			
			//update the player stuff
			if (!player.isSongLoaded()) {
				titleText.append("[Parado]");
			} else {
				if (player.isPaused()) {
					titleText.append("[Pausado]");
				} else if (player.isRadioMode()) {
					titleText.append("[Recebendo]");
				} else {
					int playTime = player.getCurrentPlayTime();
					titleText.append(playTime / 60);
					titleText.append('\'');
					playTime %= 60;
					if (playTime < 10) titleText.append('0');
					titleText.append(playTime);
					titleText.append('\"');
					titleText.append(player.getCurrentSongLength());
				}
			}
		}
		
		if (Behaviour.environmentHasVolumeControl()) {
			//after preparing the second line of text, append the
			//volume to its end
			if (cursong != null)
				titleText.append(" - ");
			if (player.getVolume() == 0) {
				titleText.append("Mudo");
			} else {
				titleText.append(player.getVolume());
				titleText.append('%');
			}
		}
		
		g.setColor(baseUI.Behaviour.ColorWindow);
		g.fillRect(clipX, clipY, clipWidth, clipHeight);
		Main.FontTitle.select(g);
		g.setColor(baseUI.Behaviour.ColorWindowText);
		if ((clipY + clipHeight) > btnEqTxtY) {
			g.drawString(txtEq, screenOffsetX + btnEqTxtX, screenOffsetY + btnEqTxtY, 0);
		}
		g.drawString(title, screenOffsetX + 2, screenOffsetY + (getHeight() >> 1) - Main.FontTitle.height - 2, 0);
		g.drawString(titleText.toString(), screenOffsetX + 2, screenOffsetY + (getHeight() >> 1) + 2, 0);
	}
	
	public final void attached() {
	}
	
	public final void detached() {
	}
	
	public final Song getSong(boolean nextSong, boolean justPeakNext) {
		return windowPlayer.getSong(nextSong, justPeakNext);
	}
	
	public final void resetSongCycling() {
		windowPlayer.resetSongCycling();
	}
	
	public final void stateChanged() {
		invalidate(0, (getHeight() >> 1) - Main.FontTitle.height - 2, getWidth(), (Main.FontTitle.height << 1) + 4);
	}
}
