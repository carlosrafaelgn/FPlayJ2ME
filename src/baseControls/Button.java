//
// Button.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/Button.java
//

package baseControls;

import baseUI.Behaviour;
import baseUI.Command;
import baseUI.CommandListener;
import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.Main;

import javax.microedition.lcdui.Graphics;

public final class Button extends Control {
	private String text;
	private int textAlignment, textXOffset, textYOffset, textWidth;
	private Command command;
	private CommandListener listener;
	
	public static final int EVENT_COMMAND = 0x1000;
	
	public Button(ControlContainer container, int left, int top, int width, String text, Command command, CommandListener listener) {
		this(container, left, top, width, text, command, listener, true);
	}
	
	public Button(ControlContainer container, int left, int top, int width, String text, Command command, CommandListener listener, boolean focusable) {
		super(container, left, top, width, Main.Customizer.getButtonHeight(), focusable, false, true);
		textAlignment = Graphics.HCENTER;
		setText(text);
		setCommand(command);
		setCommandListener(listener);
	}
	
	private final void calculateOffsets(int width) {
		if (textAlignment == Graphics.HCENTER) {
			textXOffset = (width >> 1) - (textWidth >> 1);
		} else if (textAlignment == Graphics.RIGHT) {
			textXOffset = width - textWidth - 2;
		} else {
			textXOffset = 2;
		}
		textYOffset = (Main.Customizer.getButtonHeight() >> 1) - (Main.FontUI.height >> 1);
	}
	
	public final String getCommandLabel() {
		return ((command == null) ? "" : command.getLabel());
	}
	
	public final Command getCommand() {
		return command;
	}
	
	public final void setCommand(Command command) {
		this.command = command;
	}
	
	public final CommandListener getCommandListener() {
		return listener;
	}
	
	public final void setCommandListener(CommandListener listener) {
		this.listener = listener;
	}
	
	public final int getTextAlignment() {
		return textAlignment;
	}
	
	public final void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
		calculateOffsets(getWidth());
	}
	
	public final String getText() {
		return this.text;
	}
	
	public final void setText(String text) {
		this.text = ((text == null) ? "" : text);
		textWidth = Main.FontUI.stringWidth(this.text);
		calculateOffsets(getWidth());
	}
	
	public final void resize(int width, int height, boolean repaint) {
		calculateOffsets(width);
		
		super.resize(width, Main.Customizer.getButtonHeight(), repaint);
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Main.KeyOK) {
			if (getCommandListener() != null && getCommand() != null) {
				getCommandListener().eventCommand(getCommand());
			}
			return true;
		}
		return false;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		super.eventPointerUp(x, y, isValid);
		if (isValid && pointInClient(x, y) && getCommandListener() != null && getCommand() != null) {
			getCommandListener().eventCommand(getCommand());
		}
	}
	
	protected final void eventEnvironment(int changedFlags) {
		if ((changedFlags & Behaviour.ENV_FONTSIZE) != 0) {
			textWidth = Main.FontUI.stringWidth(this.text);
		}
	}
	
	protected final void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		Main.Customizer.paintButton(g, isPressed(), isFocused(), screenOffsetX, screenOffsetY, getWidth());
		Main.FontUI.select(g);
		
		g.setColor(Behaviour.ColorButtonText);
		if (isPressed()) {
			screenOffsetX += 1;
			screenOffsetY += 1;
		}
		
		g.drawString(text, screenOffsetX + textXOffset, screenOffsetY + textYOffset, 0);
	}
}
