//
// WindowEqualizerCustom.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/WindowEqualizerCustom.java
//

package ui;

import baseControls.Slider;
import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;
import player.Equalizer;
import player.Preset;

final class WindowEqualizerCustom extends Window implements ControlListener {
	private final Equalizer equalizer;
	private final Preset preset;
	private final Command commandSave, commandCancel;
	private final Slider[] sliders; 
	private final int[] originalLevels, currentLevels;
	private final String title;
	
	public WindowEqualizerCustom(Equalizer equalizer, Preset preset) {
		super(Main.Customizer.getTitleHeight());
		this.equalizer = equalizer;
		this.preset = preset;
		this.title = (preset.isCustom ? "Configurar Preset" : "Ver Preset");
		
		commandSave = (preset.isCustom ? Main.commandSave() : null);
		commandCancel = Main.commandCancel();
		
		final StringBuffer lbl = new StringBuffer(16);
		final int[] bands = equalizer.getBands();
		final int min = equalizer.getMinBandLevel();
		final int max = equalizer.getMaxBandLevel();
		if (preset.isCustom) {
			originalLevels = equalizer.getPresetBandsLevel(preset);
			currentLevels = equalizer.getPresetBandsLevel(preset);
		} else {
			originalLevels = equalizer.getCurrentBandsLevel();
			currentLevels = equalizer.getCurrentBandsLevel();
		}
		
		sliders = new Slider[bands.length];
		
		final int freqDiv = ((bands[0] >= 6000) ? 1000 : 1);
		
		for (int i = 0; i < bands.length; i++) {
			lbl.delete(0, lbl.length());
			lbl.append(bands[i] / freqDiv);
			lbl.append(" Hz");
			sliders[i] = new Slider(getContainer(), 0, 0, 16, this, lbl.toString() + ":", min, max, originalLevels[i], true);
			sliders[i].setGranularity(20);
			sliders[i].setUnit("dB");
			sliders[i].setDisplayScalePower(2);
			sliders[i].setEnabled(preset.isCustom);
			getContainer().addControl(sliders[i], false);
		}
		
		getContainer().setPaintBackground(true);
		getContainer().processLayout();
	}
	
	protected Command getLeftCommand() {
		return commandSave;
	}
	
	protected Command getMiddleCommand() {
		return null;
	}
	
	protected Command getRightCommand() {
		return commandCancel;
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		switch (eventId) {
			case Slider.EVENT_CHANGED:
				for (int i = 0; i < sliders.length; i++) {
					if (sliders[i] == control) {
						currentLevels[i] = eventArg1;
						equalizer.setPresetBandsLevel(preset, currentLevels);
						break;
					}
				}
				break;
		}
	}
	
	protected final void eventResize() {
		final int availableHeight = getHeight() - getTitleHeight();
		final int availableWidth = getWidth() - Main.Customizer.getScrollWidth();
		for (int i = 0; i < sliders.length; i++) {
			sliders[i].reposition(0, i * sliders[i].getHeight(), availableWidth, 0, false);
		}
		getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandSave)) {
			close();
		} else if (command.equals(commandCancel)) {
			boolean changed = false;
			for (int i = 0; i < originalLevels.length; i++) {
				if (originalLevels[i] != currentLevels[i]) {
					changed = true;
					break;
				}
			}
			if (changed) {
				equalizer.setPresetBandsLevel(preset, originalLevels);
			}
			close();
		}
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		drawTextAsTitle(g, title, screenTitleX, screenTitleY);
	}
}
