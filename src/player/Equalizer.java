//
// Equalizer.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/player/Equalizer.java
//

package player;

import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseUtil.Map;
import baseUtil.Vector;

public final class Equalizer {
	private final javax.microedition.amms.control.audioeffect.EqualizerControl equalizer;
	private final Player player;
	private final Vector presets;
	private final int[] bands;
	private Preset preset;
	
	private static final int CFG_ENABLED = 0x0201;
	private static final int CFG_SELPRESETNAME = 0x0202;
	private static final int CFG_CUSTOMPRESETCOUNT = 0x0203;
	private static final int CFG_CUSTOMPRESETFIRST = 0x0210;
	
	private Equalizer(javax.microedition.amms.control.audioeffect.EqualizerControl equalizer, Player player, Map map) {
		this.equalizer = equalizer;
		this.player = player;
		this.presets = new Vector(8);
		
		if (equalizer != null) {
			this.bands = new int[equalizer.getNumberOfBands()];
			for (int i = 0; i < this.bands.length; i++) {
				this.bands[i] = equalizer.getCenterFreq(i);
			}
		} else {
			this.bands = new int[0];
		}
		
		final boolean enabled = map.getBoolean(CFG_ENABLED, false);
		final String presetName = map.getString(CFG_SELPRESETNAME, "");
		final int customPresetCount = map.getInt(CFG_CUSTOMPRESETCOUNT, 0);
		
		final int minLevel = getMinBandLevel();
		final int maxLevel = getMaxBandLevel();
		
		for (int i = 0; i < customPresetCount; i++) {
			final byte[] b = map.getBytes(CFG_CUSTOMPRESETFIRST + i, null);
			if (b != null && b.length > 0) {
				final Preset p = new Preset(new ByteInStream(b), this.bands.length, minLevel, maxLevel);
				this.presets.addElement(p);
				if (this.preset == null && p.name.equalsIgnoreCase(presetName)) {
					this.preset = p;
				}
			}
		}
		
		final String[] defpresets = equalizer.getPresetNames();
		if (defpresets != null && defpresets.length > 0) {
			for (int i = 0; i < defpresets.length; i++) {
				if (defpresets[i] != null && defpresets[i].length() > 0) {
					final Preset p = new Preset(defpresets[i]);
					this.presets.addElement(p);
					if (this.preset == null && p.name.equalsIgnoreCase(presetName)) {
						this.preset = p;
					}
				}
			}
		}
		
		this.presets.sort();
		
		setEnabled(false);
		if (this.preset != null) {
			final Preset p = this.preset;
			this.preset = null;
			setPreset(p);
			setEnabled(enabled);
		}
	}
	
	private static Equalizer create(Player player, Map map) {
		try {
			javax.microedition.amms.control.audioeffect.EqualizerControl equalizer = (javax.microedition.amms.control.audioeffect.EqualizerControl)javax.microedition.amms.GlobalManager.getControl("javax.microedition.amms.control.audioeffect.EqualizerControl");
			if (equalizer != null) {
				equalizer.setEnabled(true);
				return new Equalizer(equalizer, player, map);
			}
		} catch (Throwable ex) { }
		return create();
	}
	
	private static Equalizer create() {
		return new Equalizer(null, null, new Map());
	}
	
	public static Equalizer createEqualizer(Player player, Map map) {
		try {
			if (java.lang.Class.forName("javax.microedition.amms.GlobalManager") != null &&
				java.lang.Class.forName("javax.microedition.amms.control.audioeffect.EqualizerControl") != null) {
				return create(player, map);
			}
		} catch (Throwable ex) { }
		return create();
	}
	
	public final void saveConfig(Map map) {
		map.putBoolean(CFG_ENABLED, isEnabled());
		map.putString(CFG_SELPRESETNAME, (preset != null) ? preset.name : "");
		int customPresetCount = 0;
		for (int i = 0; i < presets.size(); i++) {
			if (((Preset)(presets.elementAt(i))).isCustom) {
				customPresetCount++;
			}
		}
		map.putInt(CFG_CUSTOMPRESETCOUNT, customPresetCount);
		ByteOutStream stream = new ByteOutStream(32);
		customPresetCount = 0;
		for (int i = 0; i < presets.size(); i++) {
			final Preset p = (Preset)(presets.elementAt(i));
			if (p.isCustom) {
				stream.reset();
				p.serialize(stream);
				map.putBytes(CFG_CUSTOMPRESETFIRST + customPresetCount, stream.toByteArray());
				customPresetCount++;
			}
		}
	}
	
	public final boolean isAlive() {
		return (equalizer != null);
	}
	
	public final Preset[] getPresets() {
		if (equalizer != null) {
			final Preset[] p = new Preset[presets.size()];
			for (int i = 0; i < p.length; i++) {
				p[i] = (Preset)(presets.elementAt(i));
			}
			return p;
		} else {
			return new Preset[0];
		}
	}
	
	public final boolean containsPresetName(String name) {
		for (int i = 0; i < presets.size(); i++) {
			if (((Preset)(presets.elementAt(i))).name.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public final int getMinBandLevel() {
		return ((equalizer != null) ? equalizer.getMinBandLevel() : 0);
	}
	
	public final int getMaxBandLevel() {
		return ((equalizer != null) ? equalizer.getMaxBandLevel() : 100);
	}
	
	public final int[] getBands() {
		final int[] bands = new int[this.bands.length];
		System.arraycopy(this.bands, 0, bands, 0, bands.length);
		return bands;
	}
	
	public final int[] getCurrentBandsLevel() {
		final int[] levels = new int[bands.length];
		if (equalizer != null) {
			for (int i = 0; i < levels.length; i++) {
				levels[i] = equalizer.getBandLevel(i);
			}
		}
		return levels;
	}
	
	public final Preset createPreset(String name, int[] bandsLevel) {
		if (containsPresetName(name)) {
			return null;
		}
		final Preset p = new Preset(name, bandsLevel, bands.length, getMinBandLevel(), getMaxBandLevel());
		presets.addElement(p);
		presets.sort();
		return p;
	}
	
	public final Preset renamePreset(Preset preset, String name) {
		if (containsPresetName(name)) {
			return null;
		}
		final Preset p = new Preset(name, preset.bandsLevel, preset.bandsLevel.length, getMinBandLevel(), getMaxBandLevel());
		presets.removeElement(preset);
		presets.addElement(p);
		presets.sort();
		if (this.preset.equals(preset)) {
			//Don't call setPreset(p) because only the name has changed!
			this.preset = p;
		}
		return p;
	}
	
	public final boolean removePreset(Preset preset) {
		if (!preset.isCustom) {
			return false;
		}
		return presets.removeElement(preset);
	}
	
	public final int[] getPresetBandsLevel(Preset preset) {
		if (!preset.isCustom) {
			return new int[0];
		}
		final int[] levels = new int[preset.bandsLevel.length];
		System.arraycopy(preset.bandsLevel, 0, levels, 0, levels.length);
		return levels;
	}
	
	public final boolean setPresetBandsLevel(Preset preset, int[] bandsLevel) {
		if (!preset.isCustom) {
			return false;
		}
		final int tot = Math.min(bandsLevel.length, preset.bandsLevel.length);
		final int minLevel = getMinBandLevel();
		final int maxLevel = getMaxBandLevel();
		for (int i = 0; i < tot; i++) {
			if (bandsLevel[i] <= minLevel) preset.bandsLevel[i] = minLevel;
			else if (bandsLevel[i] >= maxLevel) preset.bandsLevel[i] = maxLevel;
			else preset.bandsLevel[i] = bandsLevel[i];
		}
		if (this.preset.equals(preset)) {
			commitCustomChanges(-1);
		}
		return true;
	}
	
	public final boolean setPresetBandLevel(Preset preset, int band, int bandLevel) {
		if (!preset.isCustom || band < 0 || band >= preset.bandsLevel.length) {
			return false;
		}
		final int minLevel = getMinBandLevel();
		final int maxLevel = getMaxBandLevel();
		if (bandLevel <= minLevel) preset.bandsLevel[band] = minLevel;
		else if (bandLevel >= maxLevel) preset.bandsLevel[band] = maxLevel;
		else preset.bandsLevel[band] = bandLevel;
		if (this.preset.equals(preset)) {
			commitCustomChanges(band);
		}
		return true;
	}

	private final void commitCustomChanges(int bandIndex) {
		if (equalizer != null && preset != null && preset.isCustom) {
			player.clearNext(); //clearNextAndWait();
			
			if (bandIndex < 0) {
				final int tot = Math.min(bands.length, preset.bandsLevel.length);
				for (int i = 0; i < tot; i++) {
					equalizer.setBandLevel(preset.bandsLevel[i], i);
				}
			} else if (bandIndex < bands.length) {
				equalizer.setBandLevel(preset.bandsLevel[bandIndex], bandIndex);
			}
			
			equalizer.setEnabled(true);
		}
	}
	
	public final Preset getPreset() {
		return ((equalizer != null) ? this.preset : null);
	}
	
	public final boolean setPreset(Preset preset) {
		if (equalizer != null && preset != null && preset != this.preset) {
			player.clearNext(); //clearNextAndWait();
			
			if (!preset.isCustom) {
				equalizer.setPreset(preset.name);
			} else {
				//equalizer.setEnabled(false);
				final int tot = Math.min(bands.length, preset.bandsLevel.length);
				for (int i = 0; i < tot; i++) {
					equalizer.setBandLevel(preset.bandsLevel[i], i);
				}
			}
			this.preset = preset;
			
			equalizer.setEnabled(true);
			
			return true;
		}
		return false;
	}
	
	public final boolean isEnabled() {
		return ((equalizer != null) ? equalizer.isEnabled() : false);
	}
	
	public final void setEnabled(boolean enabled) {
		if (equalizer != null) {
			equalizer.setEnabled(enabled);
		}
	}
}
