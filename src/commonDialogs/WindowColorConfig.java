//
// WindowColorConfig.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/commonDialogs/WindowColorConfig.java
//

package commonDialogs;

import baseControls.ListBox;
import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseIO.File;
import baseUI.Behaviour;
import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.Menu;
import baseUI.MenuItem;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;

public final class WindowColorConfig extends Window implements ControlListener, WindowColorChooserListener, WindowFileChooserListener {
	private final Command commandCancel, commandMenu;
	private final ListBox listBox;
	private final int[] initialColors;
	private int[] preColors;
	
	public WindowColorConfig() {
		super(Main.Customizer.getTitleHeight());
		commandCancel = Main.commandCancel();
		commandMenu = Main.commandMenu();
		
		listBox = new ListBox(getContainer(), 0, 0, 16, 16);
		listBox.ensureCapacity(Behaviour.ColorCount);
		
		initialColors = Main.Customizer.colorGet();
		
		listBox.addItems(Main.Customizer.colorGetName());
		listBox.setListener(this);
		listBox.setHilightIndex(0);
		listBox.selectItem(0);
		
		getContainer().addControl(listBox, false);
		
		getContainer().processLayout();
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

	private final void preColorsSave() {
		if (preColors == null)
			preColors = Main.Customizer.colorGet();
	}

	private final void preColorsLoad() {
		if (preColors != null) {
			Main.Customizer.colorSet(preColors);
			preColors = null;
			Main.environmentChangeColors();
		}
	}
	
	private final boolean fileLoad(File file, boolean verbose) {
		if (file.openExisting()) {
			ByteInStream rstream = file.readStream();
			if (rstream != null) {
				int tot = rstream.readByte();
				if (tot >= Behaviour.ColorCount) {
					for (int i = 0; i < tot; i++)
						Main.Customizer.colorSet(i, rstream.readInt3());
				}
				Main.environmentChangeColors();
				file.close();
				return true;
			}
		}
		if (verbose)
			Main.alertShow(file.getLastError(), true);
		file.close();
		return false;
	}
	
	private final boolean fileSave(File file) {
		if (file.openTruncate()) {
			ByteOutStream wstream = new ByteOutStream(Behaviour.ColorCount * 4);
			wstream.writeByte(Behaviour.ColorCount);
			for (int i = 0; i < Behaviour.ColorCount; i++)
				wstream.writeInt3(Main.Customizer.colorGet(i));
			if (file.write(wstream)) {
				file.close();
				return true;
			}
		}
		Main.alertShow(file.getLastError(), true);
		file.close();
		return false;
	}
	
	public final void eventFileSelected(WindowFileChooser window, File file) {
		try {
			switch (window.getId()) {
			case 2: //Abrir...
				if (!fileLoad(file, true))
					preColorsLoad();
				break;
			case 3: //Salvar...
				fileSave(file);
				break;
			}
		} finally {
			file.close();
		}
	}
	
	public final void eventFileSelectionChanged(WindowFileChooser window, File file) {
		if (window.getId() == 2) {
			preColorsSave();
			fileLoad(file, false);
		}
	}
	
	public final void eventFileCancelled(WindowFileChooser window) {
		if (window.getId() == 2) {
			preColorsLoad();
		}
	}

	public void eventColorSelected(WindowColorChooser window, int color) {
		Main.Customizer.colorSet(window.getId(), color);
		Main.environmentChangeColors();
	}

	public void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		switch (eventId) {
		case ListBox.EVENT_ITEMSELECTED:
			Main.openWindow(new WindowColorChooser(listBox.selectedIndex(), Main.Customizer.colorGet(listBox.selectedIndex()), Main.Customizer.colorGetName(listBox.selectedIndex()), this));
			break;
		}
	}
	
	protected final void eventResize() {
		final int availableHeight = getHeight() - getTitleHeight();
		listBox.reposition(0, 0, getWidth(), availableHeight, false);
		getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
	}

	public final void eventMenuCommand(Menu menu, MenuItem item) {
		if (menu.getId() ==  1) {
			switch (item.getId()) {
			case 1: //Aplicar
				close();
				break;
			case 2: //Abrir...
				Main.openWindow(new WindowFileChooser(item.getId(), true, 'c', true, "Abrir Esquema", this));
				break;
			case 3: //Salvar...
				Main.openWindow(new WindowFileChooser(item.getId(), false, 'c', true, "Salvar Esquema", this));
				break;
			case 4: //Padrão
				Main.Customizer.colorLoadDefault();
				Main.environmentChangeColors();
				break;
			}
		}
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandMenu)) {
			showMenu(1,
					new MenuItem[] {
						new MenuItem("Aplicar", 1),
						null,
						new MenuItem("Esquemas", 1, new MenuItem[] {
							new MenuItem("Abrir...", 2),
							new MenuItem("Salvar...", 3),
						}),
						null,
						new MenuItem("Esquema Padrão", 4),
					});
		} else if (command.equals(commandCancel)) {
			for (int i = 0; i < Behaviour.ColorCount; i++) {
				if (Main.Customizer.colorGet(i) != initialColors[i]) {
					for (i = 0; i < Behaviour.ColorCount; i++)
						Main.Customizer.colorSet(i, initialColors[i]);
					Main.environmentChangeColors();
					break;
				}
			}
			close();
		}
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		drawTextAsTitle(g, "Cores", screenTitleX, screenTitleY);
	}
}
