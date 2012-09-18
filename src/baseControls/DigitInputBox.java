//
// DigitInputBox.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/DigitInputBox.java
//

package baseControls;

import baseUI.Behaviour;
import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.Main;
import baseUI.MessageThread;
import javax.microedition.lcdui.Graphics;

public final class DigitInputBox extends Control {
	private int textOffsetY, buttonHeight;
	private String label;
	private char[] digits;
	private int digitCount;
	private final boolean hexadecimal;
	private long lastKeyTime;
	private int lastKey, lastKeyTurn;
	private boolean cursorVisible;
	private MessageThread timer;
	
	public DigitInputBox(ControlContainer container, int left, int top, int width, boolean hexadecimal, String label) {
		super(container, left, top, width, getDefault2RowItemHeight(), true, false, true);
		digits = new char[0];
		this.hexadecimal = hexadecimal;
		this.label = label;
		lastKeyTime = System.currentTimeMillis();
		calculateOffsets(width);
	}
	
	private final void calculateOffsets(int width) {
		buttonHeight = Main.Customizer.getItemHeight();
		textOffsetY = Main.FontUI.height + 2 + 1 + (buttonHeight >> 1) - (Main.FontUI.height >> 1);
	}
	
	public final String getLabel() {
		return label;
	}
	
	public final void setLabel(String label) {
		this.label = label;
	}
	
	public final String getText() {
		return new String(digits);
	}
	
	public final void setText(String text) {
		final char[] c = text.toCharArray();
		digitCount = Math.min(c.length, digits.length);
		System.arraycopy(c, 0, digits, 0, digitCount);
	}
	
	public final boolean isHexadecimal() {
		return hexadecimal;
	}
	
	public final void input(char c) {
		if (c == (char)0x2E) {
			if (digitCount > 0) {
				digitCount = 0;
				invalidate();
			}
		} else if (c == (char)8) {
			if (digitCount > 0) {
				digitCount--;
				invalidate();
			}
		} else if (digitCount < digits.length) {
			if ((c >= '0' && c <= '9') ||
				(hexadecimal && ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))) {
				digits[digitCount++] = c;
				invalidate();
			}
		}
	}
	
	public final int getDigitLimit() {
		return digits.length;
	}
	
	public final void setDigitLimit(int digitLimit) {
		final char[] d = new char[digitLimit];
		System.arraycopy(digits, 0, d, 0, Math.min(d.length, digitCount));
		digits = d;
	}
	
	public final boolean isValidInt() {
		try {
			Integer.parseInt(new String(digits, 0, digitCount), (hexadecimal ? 16 : 10));
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	public final boolean isValidLong() {
		try {
			Long.parseLong(new String(digits, 0, digitCount), (hexadecimal ? 16 : 10));
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	public final int getInt() {
		try {
			return Integer.parseInt(new String(digits, 0, digitCount), (hexadecimal ? 16 : 10));
		} catch (Exception ex) {
			return -1;
		}
	}
	
	public final void setInt(int intValue) {
		setText(Integer.toString(intValue, (hexadecimal ? 16 : 10)));
	}
	
	public final long getLong() {
		try {
			return Long.parseLong(new String(digits, 0, digitCount), (hexadecimal ? 16 : 10));
		} catch (Exception ex) {
			return -1;
		}
	}
	
	public final void setLong(long longValue) {
		setText(Long.toString(longValue, (hexadecimal ? 16 : 10)));
	}
	
	public final void resize(int width, int height, boolean repaint) {
		calculateOffsets(width);
		
		super.resize(width, getDefault2RowItemHeight(), repaint);
	}
	
	public final void eventMessage(int message, int iParam, Object oParam) {
		if (message == Main.SYSMSG_TIMER) {
			cursorVisible = !cursorVisible;
			invalidate();
		} else {
			super.eventMessage(message, iParam, oParam);
		}
	}
	
	protected final void eventClose() {
		if (timer != null) {
			timer.interrupt();
			timer = null;
		}
	}
	
	protected final void eventFocus() {
		if (timer != null) {
			timer.interrupt();
			timer = null;
		}
		if (isFocused()) {
			cursorVisible = true;
			timer = new MessageThread(this, "Digit Input Box Cursor");
			timer.startInterval(500);
		}
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		boolean used = false;
		
		if (keyCode == Main.KeyRight) { //just to ignore the key
			used = true;
		} else if (keyCode == 8 || keyCode == -8 || keyCode == Main.KeyLeft) {
			//backspace
			input((char)8);
			used = true;
		} else if (keyCode == 0x2E) {
			//delete
			input((char)0x2E);
			used = true;
		} else if (keyCode >= '0' && keyCode <= '9') {
			if (repeatCount == 0 && hexadecimal && (keyCode == '2' || keyCode == '3')) {
				final long now = System.currentTimeMillis();
				if (keyCode == lastKey && (now - lastKeyTime) < 1000) {
					if (digitCount > 0) digitCount--;
					lastKeyTime = now;
					lastKeyTurn = (lastKeyTurn + 1) & 3;
					if (keyCode == '2') {
						if (lastKeyTurn > 0) keyCode = 'A' + lastKeyTurn - 1;
					} else {
						if (lastKeyTurn > 0) keyCode = 'D' + lastKeyTurn - 1;
					}
					input((char)keyCode);
					return true;
				} else {
					lastKeyTurn = 0;
					if (digitCount < digits.length) {
						lastKeyTime = now;
					} else {
						lastKeyTime -= 1000;
						return true;
					}
				}
			}
			input((char)keyCode);
			used = true;
		} else if (hexadecimal) {
			if (keyCode >= 'A' && keyCode <= 'F'){
				input((char)keyCode);
				used = true;
			} else if (keyCode >= 'a' && keyCode <= 'f'){
				input((char)(keyCode - 'a' + 'A'));
				used = true;
			}
		}
		
		lastKeyTurn = 0;
		lastKey = keyCode;
		
		return used;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		final boolean pressed = isPressed();
		super.eventPointerUp(x, y, isValid);
		if (pressed && pointInClient(x, y)) {
			Main.openWindow(new DigitInputWindow(this), false);
		}
	}
	
	protected final void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		Main.FontUI.select(g);
		g.setColor(Behaviour.ColorWindowText);
		g.drawString(label, screenOffsetX + 2, screenOffsetY + 2, 0);
		Main.Customizer.paintItem(g, isPressed(), isFocused(), false, screenOffsetX + 2, screenOffsetY + Main.FontUI.height + 2 + 1, getWidth() - 4);
		g.setColor(Main.Customizer.getItemTextColor(isFocused(), false));
		if (isPressed()) {
			screenOffsetX += 1;
			screenOffsetY += 1;
		}
		
		int cpos = screenOffsetX + (getWidth() >> 1);
		
		g.setColor(Main.Customizer.getItemTextColor(isFocused(), false));
		
		if (digitCount > 0) {
			final int dw = Main.FontUI.charsWidth(digits, 0, digitCount);
			g.drawChars(digits, 0, digitCount, cpos - (dw >> 1), screenOffsetY + textOffsetY, 0);
			cpos += (dw >> 1) + 2;
		}
		
		if (isFocused() && cursorVisible) {
			g.drawChar('|', cpos, screenOffsetY + textOffsetY - 1, 0);
		}
	}
}
