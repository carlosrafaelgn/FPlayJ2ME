//
// ItemChoice.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/ItemChoice.java
//

package baseControls;

import baseUI.Behaviour;
import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.ControlListener;
import baseUI.Main;
import javax.microedition.lcdui.Graphics;

public final class ItemChoice extends Control {
	private int selected, textOffsetX, textOffsetY, buttonHeight;
	private String label;
	private final String[] items;
	private ControlListener listener;
	
	public static final int EVENT_CHANGED = 0x2000;
	
	public ItemChoice(ControlContainer container, int left, int top, int width, ControlListener listener, String label, String[] items) {
		super(container, left, top, width, getDefault2RowItemHeight(), true, false, true);
		this.listener = listener;
		this.label = label;
		this.items = items;
		this.selected = 0;
		calculateOffsets(width);
	}
	
	private final void calculateOffsets(int width) {
		buttonHeight = Main.Customizer.getItemHeight();
		textOffsetX = (width >> 1) - (Main.FontUI.stringWidth(items[selected]) >> 1);
		textOffsetY = Main.FontUI.height + 2 + 1 + (buttonHeight >> 1) - (Main.FontUI.height >> 1);
	}
	
	public final void setListener(ControlListener listener) {
		this.listener = listener;
	}

	public final ControlListener getListener() {
		return listener;
	}
	
	public final String getLabel() {
		return label;
	}
	
	public final void setLabel(String label) {
		this.label = label;
	}
	
	public final int getSelectedIndex() {
		return selected;
	}
	
	public final void setSelectedIndex(int index) {
		if (index >= this.items.length) index = 0;
		else if (index < 0) index = (this.items.length - 1);
		this.selected = index;
		
		calculateOffsets(getWidth());
	}
	
	public final String getItem(int index) {
		return items[index];
	}
	
	public final void resize(int width, int height, boolean repaint) {
		calculateOffsets(width);
		
		super.resize(width, getDefault2RowItemHeight(), repaint);
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if ((repeatCount == 0) &&
			((keyCode == Main.KeyOK) ||
			(keyCode == Main.KeyRight) ||
			(keyCode == Main.KeyLeft))) {
			setSelectedIndex(selected + ((keyCode == Main.KeyLeft) ? -1 : 1));
			invalidate();
			if (listener != null) {
				listener.eventControl(this, EVENT_CHANGED, selected, items[selected]);
			}
			return true;
		}
		return false;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		final boolean pressed = isPressed();
		super.eventPointerUp(x, y, isValid);
		if (pressed && pointInClient(x, y)) {
			if (selected >= (items.length - 1)) selected = 0;
			else selected++;
			calculateOffsets(getWidth());
			invalidate();
			if (listener != null) {
				listener.eventControl(this, EVENT_CHANGED, selected, items[selected]);
			}
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
		g.drawString(items[selected], screenOffsetX + textOffsetX, screenOffsetY + textOffsetY, 0);
	}
}
