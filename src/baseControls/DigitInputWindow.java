//
// DigitInputWindow.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/DigitInputWindow.java
//

package baseControls;

import baseUI.Behaviour;
import baseUI.Command;
import baseUI.Main;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;

final class DigitInputWindow extends Window {
	private final DigitInputBox box;
	private final Button[] btnD;
	private final Button btnAll, btnC;
	private final Command commandBack;
	
	public DigitInputWindow(DigitInputBox box) {
		this.box = box;
		
		commandBack = Main.commandBack();
		
		btnD = new Button[box.isHexadecimal() ? 16 : 10];
		for (int i = 0; i < 9; ++i) {
			btnD[i] = new Button(getContainer(), 0, 0, 10, String.valueOf((char)('1' + i)), new Command(null, '1' + i), this, false);
		}
		btnD[9] = new Button(getContainer(), 0, 0, 10, String.valueOf('0'), new Command(null, '0'), this, false);
		if (box.isHexadecimal()) {
			for (int i = 10; i < 16; ++i) {
				btnD[i] = new Button(getContainer(), 0, 0, 10, String.valueOf((char)('A' + i - 10)), new Command(null, 'A' + i - 10), this, false);
			}
		}
		btnAll = new Button(getContainer(), 0, 0, 10, "Limpar", new Command(null, 0x2E), this, false);
		btnC = new Button(getContainer(), 0, 0, 10, "Retr.", new Command(null, 8), this, false);
		
		getContainer().addControl(btnAll, false);
		getContainer().addControl(btnC, false);
		getContainer().addControls(btnD, false);
		
		getContainer().processLayout();
	}
	
	protected final void eventResize() {
		final int usableWidth = getWidth() - Main.Customizer.getScrollWidth() - 4;
		final int btnWidth = (usableWidth - 12) >> 2;
		
		btnAll.reposition(2, 2, (usableWidth >> 1) - 2, 0, false);
		btnC.reposition(btnAll.getRight() + 4, 2, usableWidth - 2 - btnAll.getRight(), 0, false);
		
		int y = btnC.getTop();
		for (int i = 0; i < btnD.length; ++i) {
			if ((i & 3) == 0) {
				y += 3 + btnD[i].getHeight();
			}
			btnD[i].reposition(2 + ((i & 3) * btnWidth) + ((i & 3) << 2), y, (((i & 3) == 3) ? (usableWidth - 12 - (3 * btnWidth)) : btnWidth), 0, false);
		}
		getContainer().reposition(0, box.getHeight(), getWidth(), getHeight() - box.getHeight(), false);
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandBack)) {
			close(false);
		} else {
			box.input((char)command.getCode());
			invalidate();
		}
	}	
	
	protected final Command getLeftCommand() {
		return null;
	}
	
	protected final Command getMiddleCommand() {
		return null;
	}
	
	protected final Command getRightCommand() {
		return commandBack;
	}
	
	protected final void paintContents(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		g.setColor(Behaviour.ColorWindow);
		g.fillRect(clipX, clipY, clipWidth, clipHeight);
		
		//hehehe
		if (clipY < box.getHeight()) {
			box.paint(g, screenOffsetX, screenOffsetY, clipX, clipY, clipWidth, clipHeight);
		}
	}
}
