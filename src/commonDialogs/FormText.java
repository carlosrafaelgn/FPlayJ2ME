//
// FormText.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/commonDialogs/FormText.java
//

package commonDialogs;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;

import baseUI.Main;

public final class FormText extends Form implements CommandListener, ItemStateListener {
	private final FormTextListener listener;
	private final int id, minLength, maxLength;
	private final TextField text;
	private final Command commandSelect, commandCancel;

	public FormText(String title, String message, String initialText, int id, int minLength, int maxLength, FormTextListener listener) {
		super(title);
		
		this.id = id;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.listener = listener;
		text = new TextField(message, initialText, maxLength, TextField.ANY);
		
		commandSelect = new Command("OK", Command.OK, 2);
		commandCancel = new Command("Cancelar", Command.BACK, 1);

		addCommand(commandSelect);
		addCommand(commandCancel);
		setCommandListener(this);

		append(text);
		setItemStateListener(this);
	}

	public final void itemStateChanged(Item item) {
	}

	public final void commandAction(Command c, Displayable d) {
		if (c == commandCancel) {
			Main.showCanvas();
		} else if (c == commandSelect) {
			String s = text.getString();
			if (s.length() >= minLength && s.length() <= maxLength) {
				Main.showCanvas();
				listener.eventTextEntered(s, id);
			}
		}
	}
}
