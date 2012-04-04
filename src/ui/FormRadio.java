//
// FormRadio.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/FormRadio.java
//

package ui;

import baseControls.ListBox;
import baseUI.Main;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;
import player.Song;

public final class FormRadio extends Form implements CommandListener, ItemStateListener {
	private final TextField textName, textFrequency;
	private final Command commandOK, commandBack;
	private final ListBox playerListBox;
	private final WindowPlayer player;
	private boolean selected;

	public FormRadio(WindowPlayer player, ListBox playerListBox) {
		super("Criar Estação");
		
		this.playerListBox = playerListBox;
		this.player = player;
		
		textName = new TextField("Nome", "", 100, 0);
		textFrequency = new TextField("Freqüência", "", 16, TextField.DECIMAL);
		
		commandOK = new Command("OK", Command.OK, 1);
		commandBack = new Command("Voltar", Command.CANCEL, 0);
		addCommand(commandOK);
		addCommand(commandBack);
		setCommandListener(this);
		
		append(textName);
		append(textFrequency);
		setItemStateListener(this);
	}

	public void itemStateChanged(Item item) {
	}

	public void commandAction(Command c, Displayable d) {
		if (c.equals(commandBack)) {
			Main.showCanvas();
		}
		else if (c.equals(commandOK)) {
			//if we are coming back from the radio editor,
			//try to add the station
			if (!isFrequencyValid()) {
				Main.showErrorAlert("Freqüência inválida!", this);
			} else if (getName().length() < 1) {
				Main.showErrorAlert("Nome inválido!", this);
			} else {
				final int i = playerListBox.itemCount();
				playerListBox.addItem(new Song(Float.toString(getFrequency()), getName()));
				player.listSetChanged();
				if (!selected) {
					playerListBox.selectItem(i);
					selected = true;
				}
				textName.setString("");
				textFrequency.setString("");
				Main.showFormItem(textName);
			}
		}
	}

	public String getName() {
		return textName.getString();
	}

	public boolean isFrequencyValid() {
		float f = getFrequency();
		return (f >= 70.0f && f <= 110.0f);
	}

	public float getFrequency() {
		try {
			return Float.parseFloat(textFrequency.getString());
		} catch (Exception ex) {
			return -1;
		}
	}
}
