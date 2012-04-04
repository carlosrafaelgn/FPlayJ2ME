//
// WindowFileChooser.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/commonDialogs/WindowFileChooser.java
//

package commonDialogs;

import baseControls.ListBox;
import baseIO.File;
import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.Menu;
import baseUI.MenuItem;
import baseUI.MessageListener;
import baseUI.MessageThread;
import baseUI.OverlayAlert;
import baseUI.OverlayListener;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;

public final class WindowFileChooser extends Window implements MessageListener, FormTextListener, OverlayListener, ControlListener {
	private static MessageThread thread;
	private final int id;
	private final boolean open, singleMode;
	private final char prefix;
	private final WindowFileChooserListener listener;
	private final String title;
	private final Command commandMenu, commandOK, commandCancel;
	private final ListBox listBox;
	private File fileToDelete;
	
	public WindowFileChooser(int id, boolean open, char prefix, boolean singleMode, String title, WindowFileChooserListener listener) {
		super(Main.Customizer.getTitleHeight());
		this.id = id;
		this.open = open;
		this.prefix = prefix;
		this.singleMode = singleMode;
		this.listener = listener;
		this.title = title;

		commandMenu = Main.commandMenu();
		commandOK = Main.commandOK();
		commandCancel = Main.commandCancel();
		
		listBox = new ListBox(getContainer(), 0, 0, 16, 16);
		listBox.setListener(this);
		
		getContainer().addControl(listBox, false);
		
		getContainer().processLayout();
		
		listGetExistingLists();
	}

	protected final Command getLeftCommand() {
		return commandMenu;
	}

	protected final Command getMiddleCommand() {
		return null;
	}

	protected final Command getRightCommand() {
		return commandCancel;
	}

	public final int getId() {
		return id;
	}
	
	public final void eventTextEntered(String text, int id) {
		close();
		listener.eventFileSelected(this, new File(text, prefix, singleMode));
	}

	public final void eventMessage(int message, int iParam, Object oParam) {
		listGetExistingFiles_();
	}
	
	private final void listGetExistingFiles_() {
		Main.setThreadProcessing(true);
		
		listBox.addItems(File.listFiles(prefix, singleMode));
		
		if (listBox.itemCount() > 0) {
			listBox.selectItem(0);
			listener.eventFileSelectionChanged(this, (File)listBox.selectedItem());
		}
		
		thread = null;
		
		Main.setThreadProcessing(false);
	}

	private final void listGetExistingLists() {
		if (thread != null) {
			Main.threadProcessingAlert();
			return;
		}
		
		listBox.clear();
		
		thread = new MessageThread(this, "File Chooser");
		thread.start();
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		switch (eventId) {
		case ListBox.EVENT_ITEMSELECTED:
			eventCommand(commandOK);
			break;
		case ListBox.EVENT_SELECTIONCHANGED:
			listener.eventFileSelectionChanged(this, (File)eventArg2);
			break;
		}
	}
	
	protected final void eventResize() {
		final int availableHeight = getHeight() - getTitleHeight();
		listBox.reposition(0, 0, getWidth(), availableHeight, false);
		getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
	}
	
	public final void eventMenuCommand(Menu menu, MenuItem item) {
		StringBuffer sb;
		if (menu.getId() == 1) {
			switch (item.getId()) {
			case 1: //Abrir
			case 2: //Salvar
				eventCommand(commandOK);
				break;
			case 3: //Salvar como...
				Main.showForm(new FormText("Salvar como...", "Nome do arquivo", "", 0, 1, 32, this));
				break;
			case 4: //Excluir
				if (listBox.selectedIndex() >= 0 && listBox.itemCount() > 0) {
					fileToDelete = (File)listBox.selectedItem();
					sb = new StringBuffer();
					sb.append("Excluir ");
					sb.append(fileToDelete.getName());
					sb.append("?");
					Main.alertShowOkCancel(sb.toString(), true, 1, this);
				}
				break;
			}
		}
	}

	public final void eventCommand(Command command) {
		if (command.equals(commandMenu)) {
			if (open) {
				if (listBox.selectedItem() != null) {
					//open menu
					showMenu(1, new MenuItem[] {
						new MenuItem("Abrir", 1),
						null,
						new MenuItem("Excluir...", 4)
					});
				} else {
					Main.alertShow("Nada selecionado para abrir", true);
				}
			} else {
				if (listBox.selectedItem() != null) {
					//save menu
					showMenu(1, new MenuItem[] {
						new MenuItem("Salvar", 2),
						null,
						new MenuItem("Salvar como...", 3),
						null,
						new MenuItem("Excluir...", 4)
					});
				} else {
					//save menu 2
					showMenu(1, new MenuItem[] {
						new MenuItem("Salvar como...", 3),
					});
				}
			}
		} else if (command.equals(commandOK)) {
			if (listBox.selectedIndex() >= 0 && listBox.itemCount() > 0) {
				close();
				listener.eventFileSelected(this, (File)listBox.selectedItem());
			}
		} else if (command.equals(commandCancel)) {
			close();
			listener.eventFileCancelled(this);
		}
	}

	public final void eventOverlay(int alertId, int replyCode) {
		if (alertId == 1) {
			if (replyCode == OverlayAlert.REPLY_OK && fileToDelete != null) {
				if (!fileToDelete.delete()) {
					StringBuffer sb = new StringBuffer();
					sb.append("Erro ao excluir ");
					sb.append(fileToDelete.getName());
					sb.append(": ");
					sb.append(fileToDelete.getLastError());
					Main.alertShow(sb.toString(), true);
				} else {
					listBox.removeSelectedItem();
				}
			}
			fileToDelete = null;
		}
	}

	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		drawTextAsTitle(g, title, screenTitleX, screenTitleY);
	}
}
