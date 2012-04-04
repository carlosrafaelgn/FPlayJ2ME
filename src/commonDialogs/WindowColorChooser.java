//
// WindowColorChooser.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/commonDialogs/WindowColorChooser.java
//

package commonDialogs;

import baseUI.Behaviour;
import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;

public final class WindowColorChooser extends Window implements ControlListener {
	private final WindowColorChooserListener listener;
	private final Command commandSelect, commandCancel;
	private final ColorGrid colorGrid;
	private final String title;
	private int selectedColor;
	private final int id;
	
	public WindowColorChooser(int id, int initialColor, String title, WindowColorChooserListener listener) {
		super(Main.Customizer.getTitleHeight());
		this.id = id;
		this.title = ((title == null) ? "Cor Atual" : title);
		this.listener = listener;
		
		commandSelect = Main.commandMenuSelect();
		commandCancel = Main.commandCancel();
		
		colorGrid = new ColorGrid(getContainer(), 0, 0, 16, initialColor, this);
		
		this.selectedColor = colorGrid.getSelectedColor();
		
		getContainer().addControl(colorGrid, false);
		
		getContainer().processLayout();
	}

	public final Command getLeftCommand() {
		return commandSelect;
	}

	public final Command getMiddleCommand() {
		return null;
	}

	public final Command getRightCommand() {
		return commandCancel;
	}

	public final int getId() {
		return id;
	}
	
	protected final void eventOpened() {
		colorGrid.ensureCursorVisible();
	}

	public void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		if (control.equals(colorGrid) && (eventId == ColorGrid.EVENT_COLORSELECTED || eventId == ColorGrid.EVENT_COLORCHANGED)) {
			selectedColor = eventArg1;
			invalidateTitle();
			if (eventId == ColorGrid.EVENT_COLORSELECTED)
				eventCommand(commandSelect);
		}
	}
	
	protected final void eventResize() {
		final int availableHeight = getHeight() - getTitleHeight();
		final int availableWidth = getWidth() - Main.Customizer.getScrollWidth();
		//the height is calculated by the control
		colorGrid.reposition(0, 0, availableWidth, 0, false);
		getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandSelect)) {
			close();
			listener.eventColorSelected(this, selectedColor);
		} else if (command.equals(commandCancel)) {
			close();
		}
	}

	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		g.setColor(Behaviour.ColorTitleText);
		Main.FontTitle.select(g);
		g.drawString(title, screenTitleX + ((getWidth() - getTitleHeight()) >> 1) - (Main.FontTitle.stringWidth(title) >> 1), screenTitleY + (getTitleHeight() >> 1) - (Main.FontTitle.height >> 1), 0);
		
		g.setColor(Behaviour.ColorTitle);
		g.drawRect(screenTitleX + getWidth() - getTitleHeight(), screenTitleY, getTitleHeight() - 1, getTitleHeight() - 1);
		g.setColor(selectedColor);
		g.fillRect(screenTitleX + getWidth() - getTitleHeight() + 1, screenTitleY + 1, getTitleHeight() - 2, getTitleHeight() - 2);
	}
}
