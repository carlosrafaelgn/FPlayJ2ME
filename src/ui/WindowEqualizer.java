//
// WindowEqualizer.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/WindowEqualizer.java
//

package ui;

import baseControls.ItemChoice;
import baseControls.ListBox;
import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.Menu;
import baseUI.MenuItem;
import baseUI.OverlayAlert;
import baseUI.OverlayListener;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;

import commonDialogs.FormText;
import commonDialogs.FormTextListener;

import player.Equalizer;
import player.Preset;

final class WindowEqualizer extends Window implements ControlListener, FormTextListener, OverlayListener {
	private final Equalizer equalizer;
	private final Command commandBack, commandMenu, commandSelect;
	private final ItemChoice chkEnabled;
	private final ListBox listBox;
	private boolean doneAtLeastOnce;
	
	public WindowEqualizer(Equalizer equalizer) {
		super(Main.Customizer.getTitleHeight());
		this.equalizer = equalizer;
		
		commandSelect = Main.commandMenuSelect();
		commandMenu = Main.commandMenu();
		commandBack = Main.commandBack();
		
		chkEnabled = new ItemChoice(getContainer(), 0, 0, 16, this, "Habilitar", new String[] { "Não", "Sim" });
		chkEnabled.setSelectedIndex(equalizer.isEnabled() ? 1 : 0);
		
		listBox = new ListBox(getContainer(), 0, 0, 16, 16);
		listBox.setWrapList(false);
		listBox.setListener(this);
		
		refreshList();
		
		getContainer().addControl(chkEnabled, false);
		getContainer().addControl(listBox, false);
		
		getContainer().setPaintBackground(true);
		getContainer().processLayout();
	}

	protected final Command getLeftCommand() {
		return commandMenu;
	}

	protected final Command getMiddleCommand() {
		return null;
	}

	protected final Command getRightCommand() {
		return commandBack;
	}

	private final void refreshList() {
		listBox.clear();
		listBox.addItems(equalizer.getPresets());
		
		final Preset preset = equalizer.getPreset();
		if (preset != null) {
			final int idx = listBox.indexOf(preset);
			if (idx >= 0) {
				listBox.selectItemAndHilight(idx);
			} else {
				listBox.selectItem(0);
			}
		} else {
			listBox.selectItem(0);
		}
	}
	
	public final void eventTextEntered(String text, int id) {
		Preset p;
		text = text.trim();
		if (equalizer.containsPresetName(text)) {
			Main.alertShow("Esse preset já existe!", true);
			return;
		}
		
		switch (id) {
		case 0: //Criar preset...
			p = equalizer.createPreset(text, new int[0]);
			if (p != null) {
				listBox.addItem(p);
				listBox.selectItem(listBox.itemCount() - 1);
			}
			break;
			
		case 1: //Renomear...
			p = (Preset)listBox.selectedItem();
			if (p != null) {
				equalizer.renamePreset(p, text);
				refreshList();
			}
			break;
		}
	}

	public final void eventOverlay(int alertId, int replyCode) {
		switch (alertId) {
		case 1:
			if (replyCode == OverlayAlert.REPLY_OK) {
				listBox.removeSelectedItem();
			}
			break;
		}
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		switch (eventId) {
		case ItemChoice.EVENT_CHANGED:
			equalizer.setEnabled(chkEnabled.getSelectedIndex() != 0);
			break;
		case ListBox.EVENT_ITEMSELECTED:
			equalizer.setPreset((Preset)eventArg2);
			if (chkEnabled.getSelectedIndex() != 1) {
				chkEnabled.setSelectedIndex(1);
				chkEnabled.invalidate();
			}
			listBox.setHilightIndex(eventArg1);
			break;
		}
	}
	
	protected final void eventResize() {
		final int availableHeight = getHeight() - getTitleHeight();
		chkEnabled.reposition(0, 0, getWidth(), 16, false);
		listBox.reposition(0, chkEnabled.getHeight(), getWidth(), availableHeight - chkEnabled.getHeight(), false);
		getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
	}

	public final void eventMenuCommand(Menu menu, MenuItem item) {
		if (menu.getId() == 1) {
			switch (item.getId()) {
			case 1: //Configurar.../Ver...
				//first select the custom preset then configure it
				if (listBox.getHilightIndex() != listBox.selectedIndex() ||
					!doneAtLeastOnce) {
					doneAtLeastOnce = true;
					eventCommand(commandSelect);
				}
				Main.openWindow(new WindowEqualizerCustom(equalizer, (Preset)listBox.selectedItem()));
				break;
			case 2: //Selecionar
				eventCommand(commandSelect);
				break;
			case 3: //Criar Preset...
				Main.showForm(new FormText("Criar Preset", "Nome do preset", "", 0, 1, 64, this));
				break;
			case 4: //Excluir...
				Main.alertShowOkCancel("Excluir " + listBox.selectedItem().toString() + "?", true, 1, this);
				break;
			case 5: //Renomear...
				Main.showForm(new FormText("Renomear Preset", "Nome do preset", listBox.selectedItem().toString(), 1, 1, 64, this));
				break;
			}
		}
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandSelect)) {
			eventControl(listBox, ListBox.EVENT_ITEMSELECTED, listBox.selectedIndex(), listBox.selectedItem());
		} else if (command.equals(commandMenu)) {
			if (listBox.selectedItem() != null) {
				if (((Preset)(listBox.selectedItem())).isCustom) {
					showMenu(1,
							new MenuItem[] {
								new MenuItem("Configurar...", 1),
								new MenuItem("Renomear...", 5),
								null,
								new MenuItem("Selecionar", 2),
								null,
								new MenuItem("Criar Preset...", 3),
								new MenuItem("Excluir...", 4)
							});
				} else {
					showMenu(1,
							new MenuItem[] {
								new MenuItem("Ver...", 1),
								null,
								new MenuItem("Selecionar", 2),
								null,
								new MenuItem("Criar Preset...", 3)
							});
				}
			} else {
				showMenu(1,
						new MenuItem[] {
							new MenuItem("Criar Preset...", 3)
						});
			}
		} else if (command.equals(commandBack)) {
			close();
		}
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		drawTextAsTitle(g, "Equalizador", screenTitleX, screenTitleY);
	}
}
