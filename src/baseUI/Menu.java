//
// Menu.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/Menu.java
//

package baseUI;

import baseGraphics.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class Menu extends Window {
	private final int id;
	private final MenuItem[] items;
	private final int[] itemPos, itemHeight;
	private final int arrowWid, itemTextY, globalOffset;
	private final Command commandMenuSelect, commandMenuClose;
	private int currentItem;
	private boolean isNextClickValid;
	
	static Menu createMenu(int id, MenuItem[] items) {
		return new Menu(id, items, Main.environmentBaseMenuX(), Main.environmentBaseMenuY(), false, Main.environmentIsMenuVertical() || !Main.environmentIsMenuAbove());
	}
	
	static Menu createMenu(int id, MenuItem[] items, int x, int y, boolean yBottom) {
		return new Menu(id, items, x, y, false, yBottom);
	}
	
	private Menu(int id, MenuItem[] items, int x, int y, boolean xRight, boolean yBottom) {
		super(Main.focusWindow(), false, 0);
		
		int i, m, ma = 0, h;
		final Font fontUI = Main.FontUI;
		this.id = id;
		this.items = items;
		for (i = 0; i < items.length; i++) {
			if (items[i] != null) {
				items[i].setIndex(i);
			}
		}
		itemPos = new int[items.length];
		itemHeight = new int[items.length];
		currentItem = 0;
		commandMenuSelect = Main.commandMenuSelect();
		commandMenuClose = Main.commandMenuClose();
		final int itemH = Main.Customizer.getMenuItemHeight();
		itemTextY = (itemH >> 1) - (fontUI.height >> 1);
		
		int maxOffsetWid = 0;
		final int markWid = fontUI.stringWidth("• ");
		arrowWid = fontUI.stringWidth(" »"); //" »"); //" >");
		
		h = 2;
		
		for (i = 0; i < items.length; ++i) {
			itemPos[i] = h;
			if (items[i] == null) {
				itemHeight[i] = 5;
				h += 5;
			} else {
				itemHeight[i] = itemH;
				h += itemH;
				m = fontUI.stringWidth(items[i].getText());
				if (items[i].hasSubItems()) m += arrowWid;
				if (m > ma) ma = m;
				
				if (items[i].getChecked()) {
					if (markWid > maxOffsetWid) maxOffsetWid = markWid;
				} else if (items[i].getImage() != null) {
					final int iw = items[i].getImage().getWidth();
					if (iw > maxOffsetWid) maxOffsetWid = iw;
				}
			}
		}
		
		globalOffset = maxOffsetWid;
		ma += maxOffsetWid;
		
		h += 2;
		if (ma < 16) ma = 16;

		int menuX = x;
		int menuY = (yBottom ? (y - h) : y);
		int menuWidth = ma + 8;
		int menuHeight = h;
		
		if (xRight) menuX -= menuWidth;
		
		//fix the menu position
		if (menuWidth > Main.MaximizedWidth) menuWidth = Main.MaximizedWidth;
		if (menuHeight > Main.MaximizedHeight) menuHeight = Main.MaximizedHeight;
		if ((menuX + menuWidth) > Main.MaximizedRight) menuX = Main.MaximizedRight - menuWidth;
		else if (menuX < Main.MaximizedX) menuX = Main.MaximizedX;
		if ((menuY + menuHeight) > Main.MaximizedBottom) menuY = Main.MaximizedBottom - menuHeight;
		else if (menuY < Main.MaximizedY) menuY = Main.MaximizedY;
		
		reposition(menuX, menuY, menuWidth, menuHeight);
	}
	
	//public String toString() {
	//	return "Menu " + items[0].getText() + " " + super.toString();
	//}
	
	private final int getItemAt(int x, int y) {
		if (x < 0 || x >= getWidth())
			return -1;
		
		int start = 0;
		int end = items.length - 1;
		x = (end + 1) >> 1;
		do {
			final int p = itemPos[x];
			if (y < p) {
				end = x - 1;
			} else if (y >= (p + itemHeight[x])) {
				start = x + 1;
			} else {
				return x;
			}
			x = start + ((end - start + 1) >> 1);
		} while (start <= end);
		
		return -1;
	}
	
	private final void selectItemAndInvalidate(int newIndex) {
		if (newIndex == -1) {
			final int old = currentItem;
			currentItem = -1;
			if (old != -1) invalidate(0, itemPos[old], getWidth(), itemHeight[old]);
		} else {
			if (items[newIndex] != null) {
				if (currentItem == -1) currentItem = newIndex;
				final int up = itemPos[Math.min(newIndex, currentItem)];
				final int maxid = Math.max(newIndex, currentItem);
				currentItem = newIndex;
				invalidate(0, up, getWidth(), itemPos[maxid] + itemHeight[maxid] - up);
			}
		}
	}
	
	private final void moveUp() {
		int nid = currentItem;
		if ((--nid) < 0) nid = items.length - 1;
		while (items[nid] == null && nid != currentItem) {
			if ((--nid) < 0) nid = items.length - 1;
		}
		selectItemAndInvalidate(nid);
	}

	private final void moveDown() {
		int nid = currentItem;
		if ((++nid) >= items.length) nid = 0;
		while (items[nid] == null && nid != currentItem) {
			if ((++nid) >= items.length) nid = 0;
		}
		selectItemAndInvalidate(nid);
	}

	private final boolean isSelectedItemValid() {
		return (currentItem >= 0 && currentItem < items.length);
	}
	
	public final int getId() {
		return id;
	}
	
	public final MenuItem[] getItems() {
		return items;
	}
	
	public final int getSelectedIndex() {
		if (isSelectedItemValid() && items[currentItem] != null) {
			return currentItem;
		}
		return -1;
	}
	
	public final MenuItem getSelectedItem() {
		if (isSelectedItemValid()) {
			return items[currentItem];
		}
		return null;
	}
	
	private final boolean openSubMenuByIndex(int itemIndex) {
		if (itemIndex >= 0 && itemIndex < items.length && items[itemIndex].hasSubItems()) {
			final boolean mv = Main.environmentIsMenuVertical();
			final boolean ma = Main.environmentIsMenuAbove();
			showMenu(
					new Menu(
						items[itemIndex].getId(),
						items[itemIndex].getSubItems(),
						((!mv || ma) ?
							(getLeft() + getWidth() - 4)
							:
							(getLeft() + 4)),
						((ma && !mv) ?
							(itemPos[itemIndex] - 2)
							:
							(itemPos[itemIndex] + itemHeight[itemIndex] + 2)) + getTop(),
						(mv && !ma),
						(!ma || mv))
					);
			return true;
		}
		return false;
	}
	
	final boolean isUnderWindow(Window window) {
		Window m = this;
		do {
			m = m.previousWindow;
			if (m == window)
				return true;
		} while (m instanceof Menu);
		return false;
	}
	
	protected final Command getLeftCommand() {
		return commandMenuSelect;
	}

	protected final Command getMiddleCommand() {
		return commandMenuSelect;
	}

	protected final Command getRightCommand() {
		return commandMenuClose;
	}

	public final void eventMenuCommand(Menu menu, MenuItem item) {
		//send the eventMenuCommand to the parent of the highest menu,
		//and close the highest menu
		Window m = this;
		while (m.previousWindow instanceof Menu) {
			m = m.previousWindow;
		}
		m.close();
		m.previousWindow.eventMenuCommand(menu, item);
	}

	public final void eventCommand(Command command) {
		if (command.equals(commandMenuSelect)) {
			int item = getSelectedIndex();
			if (item != -1) {
				if (!openSubMenuByIndex(item)) {
					eventMenuCommand(this, items[item]);
				}
			}
		} else if (command.equals(commandMenuClose)) {
			close();
		}
	}
	
	protected final void eventClosed() {
		previousWindow.subMenuClosing();
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Main.KeyUp) {
			moveUp();
		} else if (keyCode == Main.KeyDown) {
			moveDown();
		} else if (keyCode == Main.KeyRight) {
			openSubMenuByIndex(currentItem);
		} else if (keyCode == Main.KeyLeft) {
			eventCommand(commandMenuClose);
		} else {
			return false;
		}
		return true;
	}
	
	protected final void eventPointerDown(int x, int y) {
		final int i = getItemAt(x, y);
		
		if (i != getSelectedIndex()) {
			selectItemAndInvalidate(i);
		}
		
		isNextClickValid = true;
		
		if (i >= 0 && items[i] != null) {
			Main.environmentDoTouchFeedback();
		}
	}
	
	protected final boolean eventPointerMove(int x, int y) {
		if (Main.pointerExceededThreshold()) {
			final int i = getItemAt(x, y);
			if (i != currentItem) {
				selectItemAndInvalidate(i);
				
				isNextClickValid = false;
				
				if (i >= 0 && items[i] != null) {
					Main.environmentDoTouchFeedback();
				}
			}
		}
		//return true to indicate that the movement has been used
		return true;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		final int i = getItemAt(x, y);
		if (i != currentItem) {
			selectItemAndInvalidate(i);
			isNextClickValid = false;
		} else if (isNextClickValid) {
			eventCommand(commandMenuSelect);
		}
	}

	protected final void paintContents(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		Main.FontUI.select(g);
		final int width = getWidth(), height = getHeight();
		
		//drawRect and fillRect treat width and height in different ways!
		g.setColor(Behaviour.ColorSelection);
		g.drawRect(screenOffsetX, screenOffsetY, width - 1, height - 1);
		g.setColor(Behaviour.ColorMenu);
		g.drawLine(screenOffsetX + 1, screenOffsetY + 1, screenOffsetX + width - 2, screenOffsetY + 1);
		g.drawLine(screenOffsetX + 1, screenOffsetY + height - 2, screenOffsetX + width - 2, screenOffsetY + height - 2);
		
		//clipHeight = bottom
		clipHeight += clipY;
		
		int t;
		final int l = screenOffsetX + 4;
		final int a = l + width - 8 - arrowWid;
		for (int i = 0; i < items.length; ++i) {
			t = itemPos[i] + screenOffsetY;
			if ((t + itemHeight[i]) <= clipY) continue;
			if (t >= clipHeight) break;
			
			if (items[i] == null) {
				g.setColor(Behaviour.ColorMenu);
				g.fillRect(screenOffsetX + 1, t, width - 2, itemHeight[i]);
				g.setColor(Behaviour.ColorSelection);
				g.drawLine(screenOffsetX + 4, t + 2, screenOffsetX + width - 5, t + 2);
			} else {
				Main.Customizer.paintMenuItem(g, (i == currentItem), screenOffsetX + 1, t, width - 2);
				g.setColor((i == currentItem) ? Behaviour.ColorSelectionText : Behaviour.ColorMenuText);
				
				t += itemTextY;
				if (items[i].getChecked()) {
					g.drawString("• ", l, t, 0);
				} else {
					final Image img = items[i].getImage();
					if (img != null) {
						g.drawImage(img, l, t + (itemHeight[i] >> 1) - (img.getHeight() << 1) , 0);
					}
				}
				g.drawString(items[i].getText(), l + globalOffset, t, 0);
				if (items[i].hasSubItems()) {
					g.drawString(" »", a, t, 0);
				}
			}
		}
	}
}
