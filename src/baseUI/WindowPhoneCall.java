//
// WindowPhoneCall.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/WindowPhoneCall.java
//

package baseUI;

import baseControls.Button;
import baseUtil.Map;
import baseUtil.Vector;
import javax.microedition.lcdui.Graphics;

final class WindowPhoneCall extends Window {
	private final Command commandBack, commandOK, commandErase;
	private final Button btnDial;
	private final Button[] btns;
	private final char[] btnChars;
	private final Vector numbers;
	private String number;
	private int numberIndex;
	private boolean hasTyped;
	
	private static final int CFG_MAXNUMBERCOUNT = 64;
	private static final int CFG_NUMBERCOUNT = -0x0201;
	private static final int CFG_NUMBERFIRST = -0x0210;
	
	public WindowPhoneCall() {
		super(Main.Customizer.getTitleHeight());
		number = "";
		numbers = new Vector(16);
		numberIndex = 0;
		hasTyped = false;
		commandErase = new Command("Apagar", 100);
		commandOK = Main.commandOK();
		commandBack = Main.commandBack();
		btnChars = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '0', '#' };
		btns = new Button[btnChars.length];
		final int bw = getWidth() >> 2; //((getWidth() * 3) >> 2) / 3;
		final char[] tmp = new char[1];
		for (int i = 0; i < btnChars.length; i++) {
			tmp[0] = btnChars[i];
			btns[i] = new Button(getContainer(), 0, 0, bw, new String(tmp), new Command("", i), this, false);
			getContainer().addControl(btns[i], false);
		}
		
		btnDial = new Button(getContainer(), 0, 0, bw << 1, "Ligar", commandOK, this, false);
		getContainer().addControl(btnDial, false);
		
		getContainer().setPaintBackground(true);
		getContainer().processLayout();
		
		Map map = Map.fromFile("cfgCalls");
		final int count = Math.min(CFG_MAXNUMBERCOUNT, map.getInt(CFG_NUMBERCOUNT, 0));
		for (int i = 0; i < count; i++) {
			final String n = map.getString(CFG_NUMBERFIRST - i, "");
			if (n.length() > 0) {
				numbers.addElement(n);
			}
		}
		
		if (numbers.size() > 0) {
			number = (String)numbers.elementAt(0);
		}
		
		System.gc();
	}
	
	protected final Command getLeftCommand() {
		return commandErase;
	}
	
	protected final Command getMiddleCommand() {
		return commandOK;
	}
	
	protected final Command getRightCommand() {
		return commandBack;
	}
	
	public final boolean canEnterBlackScreen() { return false; }
	
	private final void getNumber(boolean next) {
		if (next) {
			if ((numberIndex + 1) >= numbers.size()) {
				numberIndex = 0;
			} else {
				numberIndex++;
			}
		} else {
			if ((numberIndex - 1) < 0) {
				numberIndex = numbers.size() - 1;
			} else {
				numberIndex--;
			}
		}
		
		if (numberIndex < 0 || numberIndex >= numbers.size()) {
			numberIndex = 0;
		} else {
			number = (String)numbers.elementAt(numberIndex);
			hasTyped = false;
		}
	}
	
	protected final void eventResize() {
		final int availableHeight = getHeight() - getTitleHeight();
		
		final int top = (availableHeight >> 1) - ((((Main.Customizer.getButtonHeight() * 5) >> 2) * 5) >> 1);
		final int bw = getWidth() >> 2; //((getWidth() * 3) >> 2) / 3;
		final int cw = getWidth() / 3;
		for (int i = 0; i < 12; i++) {
			final int r = i / 3;
			final int c = i % 3;
			btns[i].reposition((c * cw) + (cw >> 1) - (bw >> 1), top + (r * ((Main.Customizer.getButtonHeight() * 5) >> 2)), bw, 0, false);
		}
		btnDial.reposition(bw, top + (((Main.Customizer.getButtonHeight() * 5) >> 2) << 2), bw << 1, 0, false);
		
		getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandBack)) {
			close();
		} else if (command.equals(commandOK)) {
			makePhoneCall();
		} else if (command.equals(commandErase)) {
			hasTyped = true;
			if (number.length() == 1) {
				number = "";
			} else if (number.length() > 0) {
				number = number.substring(0, number.length() - 1);
			}
			invalidateTitle();
		} else if (command.getCode() < btnChars.length) {
			if (!hasTyped) {
				number = "";
				hasTyped = true;
			}
			number += btnChars[command.getCode()];
			invalidateTitle();
		}
	}
	
	protected boolean eventKeyPress(int keyCode, int repeatCount) {
		if ((keyCode >= '0' && keyCode <= '9') || keyCode == '*' || keyCode == '#') {
			if (!hasTyped) {
				number = "";
				hasTyped = true;
			}
			number += ((char)keyCode);
		} else if (keyCode == Main.KeyUp) {
			getNumber(false);
		} else if (keyCode == Main.KeyDown) {
			getNumber(true);
		} else {
			return super.eventKeyPress(keyCode, repeatCount);
		}
		invalidateTitle();
		return true;
	}
	
	protected final void eventClosed() {
		final int count = numbers.size();
		final Map map = new Map(count + 1);
		map.putInt(CFG_NUMBERCOUNT, count);
		for (int i = 0; i < count; i++) {
			map.putString(CFG_NUMBERFIRST - i, (String)numbers.elementAt(i));
		}
		map.toFile("cfgCalls");
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		drawTextAsTitle(g, number, screenTitleX, screenTitleY);
	}
	
	public void makePhoneCall() {
		if (number.length() > 0) {
			numbers.removeElement(number);
			numbers.insertElementAt(number, 0);
			if (numbers.size() > CFG_MAXNUMBERCOUNT) {
				numbers.removeElementAt(CFG_MAXNUMBERCOUNT);
			}
			
			if (Main.makePhoneCall(number)) {
				close();
			}
		}
	}
}
