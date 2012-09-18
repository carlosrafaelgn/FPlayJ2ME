//
// WindowOptions.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/WindowOptions.java
//

package ui;

import baseControls.DigitInputBox;
import baseControls.ItemChoice;
import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.OverlayListener;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;
import player.Player;

final class WindowOptions extends Window implements OverlayListener, ControlListener {
	private final Player player;
	
	private final int envFont, envVibrate, envVisualEffects;
	private final boolean envMenu, envPreventVerticalMenu, envRightHanded;
	private final Command commandSave, commandCancel;
	private final int[] availableTransitions;
	private final ItemChoice itemMenu, itemPreventVerticalMenu, itemFont, itemVisual, itemTransition, itemVolumeControl, itemSongFormat, itemRightHanded, itemTouchFeedback, itemControlPlayback, itemLoadNext;
	private final DigitInputBox textSS;
	
	public WindowOptions(Player player) {
		super(Main.Customizer.getTitleHeight());
		this.player = player;
		
		commandSave = Main.commandSave();
		commandCancel = Main.commandCancel();
		envMenu = Main.environmentIsMenuAbove();
		envPreventVerticalMenu = Main.environmentIsPreventingVerticalMenu();
		envFont = Main.environmentGetFontSizeIndex();
		envVisualEffects = Main.environmentGetUIEffects();
		envVibrate = Main.environmentGetTouchFeedbackLevel();
		envRightHanded = Main.environmentIsRightHanded();
		
		itemMenu = new ItemChoice(getContainer(), 0, 0, 16, this, "Posição do menu:", new String[] { "Abaixo", "Acima" });
		itemPreventVerticalMenu = new ItemChoice(getContainer(), 0, 0, 16, this, "Menu vertical:", new String[] { "Permitir", "Evitar" });
		itemFont = new ItemChoice(getContainer(), 0, 0, 16, this, "Tamanho da fonte:", new String[] { "Pequeno", "Médio", "Grande" });
		itemVisual = new ItemChoice(getContainer(), 0, 0, 16, this, "Efeitos visuais:", new String[] { "Nenhum", "Tipo 1", "Tipo 2" });
		
		availableTransitions = Main.Customizer.getAvailableTransitions();
		final String[] transitionNames = new String[1 + availableTransitions.length];
		transitionNames[0] = Main.Customizer.getTransitionName(0);
		for (int i = 1; i < transitionNames.length; i++) {
			transitionNames[i] = Main.Customizer.getTransitionName(availableTransitions[i - 1]);
		}
		itemTransition = new ItemChoice(getContainer(), 0, 0, 16, this, "Transição:", transitionNames);
		
		itemVolumeControl = new ItemChoice(getContainer(), 0, 0, 16, this, "Controle de volume:", new String[] { "Desabilitado", "Habilitado" });
		itemSongFormat = new ItemChoice(getContainer(), 0, 0, 16, this, "Formato dos títulos:", new String[] { "Nome do arquivo", "Título", "Título - Artista", "Título - Autor", "Artista - Título", "Autor - Título" });
		
		textSS = new DigitInputBox(getContainer(), 0, 0, 16, false, "Tempo para tela preta:");
		
		itemMenu.setSelectedIndex(envMenu ? 1 : 0);
		itemPreventVerticalMenu.setSelectedIndex(envPreventVerticalMenu ? 1 : 0);
		itemFont.setSelectedIndex(envFont);
		itemVisual.setSelectedIndex(envVisualEffects);
		itemTransition.setSelectedIndex(transitionToIndex(Main.environmentGetTransition()));
		itemVolumeControl.setSelectedIndex(Behaviour.environmentHasVolumeControl() ? 1 : 0);
		itemSongFormat.setSelectedIndex(Behaviour.environmentGetSongFormat());
		textSS.setDigitLimit(3);
		textSS.setInt((Main.SSThreshold > 300000 || Main.SSThreshold <= 1000) ? 0 : (int)(Main.SSThreshold / 1000) - 1);
		
		if (player.isRadioMode()) {
			itemLoadNext = new ItemChoice(getContainer(), 0, 0, 16, this, "Rádio estéreo:", new String[] { "Não", "Sim" });
			itemLoadNext.setSelectedIndex(player.isRadioStereo() ? 1 : 0);
		} else {
			itemLoadNext = new ItemChoice(getContainer(), 0, 0, 16, this, "Pré-carregar músicas:", new String[] { "Não", "Sim" });
			itemLoadNext.setSelectedIndex(Behaviour.environmentGetLoadNextSong() ? 1 : 0);
		}
		
		getContainer().addControl(itemMenu, false);
		getContainer().addControl(itemPreventVerticalMenu, false);
		getContainer().addControl(itemFont, false);
		getContainer().addControl(itemVisual, false);
		getContainer().addControl(itemTransition, false);
		getContainer().addControl(itemVolumeControl, false);
		getContainer().addControl(itemSongFormat, false);
		getContainer().addControl(itemLoadNext, false);
		
		if (Main.environmentHasPointer()) {
			itemTouchFeedback = new ItemChoice(getContainer(), 0, 0, 16, this, "Efeitos do toque:", new String[] { "Desabilitado", "Nível 1", "Nível 2", "Nível 3", "Padrão" });
			itemTouchFeedback.setSelectedIndex(envVibrate);
			getContainer().addControl(itemTouchFeedback, false);
			
			itemRightHanded = new ItemChoice(getContainer(), 0, 0, 16, this, "Orientação:", new String[] { "Canhoto", "Destro" });
			itemRightHanded.setSelectedIndex(envRightHanded ? 1 : 0);
			getContainer().addControl(itemRightHanded, false);
			
			itemControlPlayback = new ItemChoice(getContainer(), 0, 0, 16, this, "Controles:", new String[] { "Nenhum", "Abaixo", "Acima" });
			itemControlPlayback.setSelectedIndex(Behaviour.environmentGetControlButtons());
			getContainer().addControl(itemControlPlayback, false);
		} else {
			itemTouchFeedback = null;
			itemRightHanded = null;
			itemControlPlayback = null;
		}
		getContainer().addControl(textSS, false);
		
		getContainer().setPaintBackground(true);
		getContainer().processLayout();
	}
	
	private final int transitionToIndex(int transition) {
		for (int i = 0; i < availableTransitions.length; i++) {
			if (availableTransitions[i] == transition) {
				return i + 1;
			}
		}
		return 0;
	}
	
	private final int transitionFromIndex(int index) {
		if (index <= 0 || index > availableTransitions.length) return 0;
		return availableTransitions[index - 1];
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		if (control == itemMenu) {
			Main.environmentSetMenuAbove(itemMenu.getSelectedIndex() != 0);
		} else if (control == itemPreventVerticalMenu) {
			Main.environmentSetPreventVerticalMenu(itemPreventVerticalMenu.getSelectedIndex() != 0);
		} else if (control == itemFont) {
			Main.environmentSetFontSizeIndex(itemFont.getSelectedIndex());
		} else if (control == itemVisual) {
			Main.environmentSetUIEffects(itemVisual.getSelectedIndex());
		} else if (control == itemTouchFeedback) {
			Main.environmentSetTouchFeedbackLevel(itemTouchFeedback.getSelectedIndex());
		} else if (control == itemRightHanded) {
			Main.environmentSetRightHanded(itemRightHanded.getSelectedIndex() != 0);
		}
	}
	
	public final void eventOverlay(int alertId, int replyCode) {
		if (alertId == 1) {
			close();
		}
	}
	
	protected final Command getLeftCommand() {
		return commandSave;
	}
	
	protected final Command getMiddleCommand() {
		return null;
	}
	
	protected final Command getRightCommand() {
		return commandCancel;
	}
	
	protected final void eventResize() {
		final int usableWidth = getWidth() - Main.Customizer.getScrollWidth();
		
		itemMenu.reposition(0, 0, usableWidth, 0, false);
		itemPreventVerticalMenu.reposition(0, itemMenu.getBottom(), usableWidth, 0, false);
		itemFont.reposition(0, itemPreventVerticalMenu.getBottom(), usableWidth, 0, false);
		itemVisual.reposition(0, itemFont.getBottom(), usableWidth, 0, false);
		itemTransition.reposition(0, itemVisual.getBottom(), usableWidth, 0, false);
		itemVolumeControl.reposition(0, itemTransition.getBottom(), usableWidth, 0, false);
		itemSongFormat.reposition(0, itemVolumeControl.getBottom(), usableWidth, 0, false);
		itemLoadNext.reposition(0, itemSongFormat.getBottom(), usableWidth, 0, false);
		
		if (itemTouchFeedback != null) {
			itemTouchFeedback.reposition(0, itemLoadNext.getBottom(), usableWidth, 0, false);
			itemRightHanded.reposition(0, itemTouchFeedback.getBottom(), usableWidth, 0, false);
			itemControlPlayback.reposition(0, itemRightHanded.getBottom(), usableWidth, 0, false);
		}
		
		textSS.reposition(0, ((itemTouchFeedback != null) ? itemControlPlayback : itemLoadNext).getBottom(), usableWidth, 0, false);
		
		getContainer().reposition(0, getTitleHeight(), getWidth(), getHeight() - getTitleHeight(), false);
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandSave)) {
			final int ss = textSS.getInt();
			if (ss < 0 || ss > 299) {
				Main.alertShow("Tempo para a tela preta deve estar entre 0 e 299", true);
				return;
			}
			if (ss == 0) {
				Main.SSThreshold = Long.MAX_VALUE; //disabled
			} else {
				Main.SSThreshold = (long)(ss + 1) * 1000; //1 is not an acceptable value
			}
			
			if (player.isRadioMode()) {
				if ((itemLoadNext.getSelectedIndex() != 0) != player.isRadioStereo())
					player.setRadioStereo(itemLoadNext.getSelectedIndex() != 0);
			} else {
				if ((itemLoadNext.getSelectedIndex() != 0) != Behaviour.environmentGetLoadNextSong()) {
					Behaviour.environmentSetLoadNextSong(itemLoadNext.getSelectedIndex() != 0);
					if (itemLoadNext.getSelectedIndex() == 0)
						player.clearNext();
				}
			}
			
			if (itemSongFormat.getSelectedIndex() != Behaviour.environmentGetSongFormat()) {
				if (!Behaviour.environmentSetSongFormat(itemSongFormat.getSelectedIndex())) {
					Main.alertShow("Não é possível alterar o formato dos títulos no momento", true, 1, this);
					return;
				}
			}
			Main.environmentSetTransition(transitionFromIndex(itemTransition.getSelectedIndex()));
			Behaviour.environmentSetVolumeControl(itemVolumeControl.getSelectedIndex() != 0);
			if (itemControlPlayback != null) {
				Behaviour.environmentSetControlButtons(itemControlPlayback.getSelectedIndex());
			}
			//save the options and close
			Main.configSave();
			Main.alertShow("Configurações salvas com sucesso!", true, 1, this);
		} else if (command.equals(commandCancel)) {
			//restore the options and close
			Main.environmentSetTouchFeedbackLevel(envVibrate);
			Main.environmentSetRightHanded(envRightHanded);
			Main.environmentSetMenuAbove(envMenu);
			Main.environmentSetPreventVerticalMenu(envPreventVerticalMenu);
			Main.environmentSetUIEffects(envVisualEffects);
			Main.environmentSetFontSizeIndex(envFont);
			close();
		}
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		drawTextAsTitle(g, "Opções", screenTitleX, screenTitleY);
	}
}
