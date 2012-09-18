//
// Behaviour.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/Behaviour.java
//

package ui;

import player.Song;
import baseUI.Main;
import baseUI.Window;
import baseUtil.Map;

public final class Behaviour extends baseUI.Behaviour {
	private static boolean EnvVolumeControl, EnvLoadNextSong;
	private static int EnvControlButtons;
	public static int KeyDel, KeyVolDn, KeyVolUp, KeyPause, KeyNext, KeyPrev, KeySel;
	
	public static final int ENV_VOLUMECONTROL = 0x00010000;
	
	private static WindowPlayer mainWindow;
	
	private static final int CFG_KEYDEL = 0x0001;
	private static final int CFG_KEYPAUSE = 0x0002;
	private static final int CFG_KEYNEXT = 0x0003;
	private static final int CFG_KEYPREV = 0x0004;
	private static final int CFG_KEYVOLDN = 0x0005;
	private static final int CFG_KEYVOLUP = 0x0006;
	private static final int CFG_ENVCONTROLBUTTONS = 0x0007;
	private static final int CFG_ENVVOLUMECONTROL = 0x0008;
	private static final int CFG_ENVLOADNEXTSONG = 0x0009;
	private static final int CFG_SONGFORMAT = 0x000A;
	private static final int CFG_BROWSERFOLDER = 0x000B;
	private static final int CFG_KEYSEL = 0x000C;
	
	public static boolean environmentGetLoadNextSong() {
		return EnvLoadNextSong;
	}
	
	public static void environmentSetLoadNextSong(boolean loadNextSong) {
		EnvLoadNextSong = loadNextSong;
	}
	
	public static int environmentGetControlButtons() {
		return EnvControlButtons;
	}
	
	public static void environmentSetControlButtons(int controlButtons) {
		if (controlButtons < 0) controlButtons = 0;
		else if (controlButtons > 2) controlButtons = 2;
		EnvControlButtons = controlButtons;
		Main.environmentSetMenuAbove(Main.environmentIsMenuAbove());
	}
	
	public static int environmentGetSongFormat() {
		switch (Song.Format) {
			case Song.FMT_TITLE: return 1;
			case Song.FMT_TITLE_ARTIST: return 2;
			case Song.FMT_TITLE_AUTHOR: return 3;
			case Song.FMT_ARTIST_TITLE: return 4;
			case Song.FMT_AUTHOR_TITLE: return 5;
			default: return 0;
		}
	}
	
	public static boolean environmentSetSongFormat(int songFormat) {
		switch (songFormat) {
			case 1: Song.Format = Song.FMT_TITLE; break;
			case 2: Song.Format = Song.FMT_TITLE_ARTIST; break;
			case 3: Song.Format = Song.FMT_TITLE_AUTHOR; break;
			case 4: Song.Format = Song.FMT_ARTIST_TITLE; break;
			case 5: Song.Format = Song.FMT_AUTHOR_TITLE; break;
			default: Song.Format = Song.FMT_NONE; break;
		}
		
		return mainWindow.listRefreshSongsTitle();
	}
	
	public static boolean environmentHasVolumeControl() {
		return EnvVolumeControl;
	}
	
	public static void environmentSetVolumeControl(boolean volumeControl) {
		EnvVolumeControl = volumeControl;
		mainWindow.eventEnvironment(ENV_VOLUMECONTROL);
	}
	
	public final void loadConfig(Map map) {
		super.loadConfig(map);
		
		KeyDel = map.getShort(CFG_KEYDEL, '4');
		KeyPause = map.getShort(CFG_KEYPAUSE, '0');
		KeyNext = map.getShort(CFG_KEYNEXT, Main.KeyRight);
		KeyPrev = map.getShort(CFG_KEYPREV, Main.KeyLeft);
		KeyVolDn = map.getShort(CFG_KEYVOLDN, '*');
		KeyVolUp = map.getShort(CFG_KEYVOLUP, '#');
		KeySel = map.getShort(CFG_KEYSEL, '5');
		EnvControlButtons = map.getUByte(CFG_ENVCONTROLBUTTONS, 2);
		EnvVolumeControl = map.getBoolean(CFG_ENVVOLUMECONTROL, false);
		EnvLoadNextSong = map.getBoolean(CFG_ENVLOADNEXTSONG, false);
		Song.Format = map.getUByte(CFG_SONGFORMAT, Song.FMT_NONE);
		WindowBrowser.setCurrentFolder(map.getString(CFG_BROWSERFOLDER, ""));
		
		if (EnvControlButtons > 2) EnvControlButtons = 2;
		
		mainWindow = new WindowPlayer(map);
	}
	
	public final void saveConfig(Map map) {
		super.saveConfig(map);
		
		map.putInt(CFG_KEYDEL, KeyDel);
		map.putInt(CFG_KEYPAUSE, KeyPause);
		map.putInt(CFG_KEYNEXT, KeyNext);
		map.putInt(CFG_KEYPREV, KeyPrev);
		map.putInt(CFG_KEYVOLDN, KeyVolDn);
		map.putInt(CFG_KEYVOLUP, KeyVolUp);
		map.putInt(CFG_KEYSEL, KeySel);
		map.putInt(CFG_ENVCONTROLBUTTONS, EnvControlButtons);
		map.putBoolean(CFG_ENVVOLUMECONTROL, EnvVolumeControl);
		map.putBoolean(CFG_ENVLOADNEXTSONG, EnvLoadNextSong);
		map.putInt(CFG_SONGFORMAT, Song.Format);
		map.putString(CFG_BROWSERFOLDER, WindowBrowser.getCurrentFolder());
		
		mainWindow.saveConfig(map);
	}
	
	public final Window createMainWindow() {
		if (mainWindow == null) mainWindow = new WindowPlayer(null);
		return mainWindow;
	}
	
	public final void terminate() {
		mainWindow.terminate();
	}
}
