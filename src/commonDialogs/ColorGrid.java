//
// ColorGrid.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/commonDialogs/ColorGrid.java
//

package commonDialogs;

import baseGraphics.Color;
import baseUI.Behaviour;
import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.ControlListener;
import baseUI.Main;
import javax.microedition.lcdui.Graphics;

final class ColorGrid extends Control {
	private final ControlContainer container;
	private final int[] colors;
	private int downIndex, selectedIndex, cellSize, col, row, colCount, rowCount;
	private boolean nextClickValid;
	private ControlListener listener;
	
	public static final int EVENT_COLORSELECTED = 0x1000;
	public static final int EVENT_COLORCHANGED = 0x1001;
	
	public ColorGrid(ControlContainer container, int left, int top, int width, int initialColor, ControlListener listener) {
		super(container, left, top, width, width, true, false, true);
		
		this.container = container;
		this.listener = listener;
		
		int i, x, v, s;
		
		/*float[] V = new float[] {
			0.65f, 0.50f, 0.40f, 0.25f,
			0.60f, 0.50f, 0.40f, 0.25f,
			0.60f, 0.50f, 0.40f, 0.25f,
			0.60f, 0.50f, 0.40f, 0.25f
		};
		float[] S = new float[] {
			1.00f, 1.00f, 1.00f, 1.00f,
			0.75f, 0.75f, 0.75f, 0.75f,
			0.50f, 0.50f, 0.50f, 0.50f,
			0.25f, 0.25f, 0.25f, 0.25f
		};*/
		//0.0 = black
		//0.5 = color
		//1.0 = white
		float[] V = new float[] {
			0.75f,
			0.65f,
			0.50f,
			0.38f,
			0.20f
		};
		float[] S = new float[] {
			1.00f,
			0.50f,
			0.10f
		};
		
		colors = new int[16 + 16 + (16 * V.length * S.length)];
		
		colors[0] = 0x6688FF;
		colors[1] = 0x0000CC;
		colors[2] = 0x3355AA;
		colors[3] = 0xFF8800;
		colors[4] = 0xFF0000;
		colors[5] = 0x800000;
		colors[6] = 0x00FF00;
		colors[7] = 0x008000;
		colors[8] = 0x0000FF;
		colors[9] = 0x000080;
		colors[10] = 0xFFFF00;
		colors[11] = 0x808000;
		colors[12] = 0xFF00FF;
		colors[13] = 0x800080;
		colors[14] = 0x00FFFF;
		colors[15] = 0x008080;
		colors[16] = 0x000000;
		colors[17] = 0xFFFFFF;
		
		//14 shades of gray (black and white have been
		//moved to the first block)
		for (i = 1; i < 15; i++) {
			x = i;
			x |= (x << 4);
			x |= (x << 8);
			x |= (x << 8);
			colors[18 + i] = x;
		}
		
		//more colors
		/*for (x = 0; x < 16; x++) {
			float h = (float)x * 0.0625f; //0 to 1
			for (y = 0; y < 16; y++) {
				colors[32 + (y << 4) + x] = Color.HSLtoRGB(h, S[y], V[y]);
			}
		}*/
		i = 32;
		for (s = 0; s < S.length; s++) {
			for (x = 0; x < 16; x++) {
				float h = (float)x * 0.0625f; //0 to 1
				for (v = 0; v < V.length; v++) {
					colors[i++] = Color.hslToRGB(h, S[s], V[v]);
				}
			}
		}
		
		colCount = colors.length;
		rowCount = 1;
		setSelectedIndex(indexFromColor(initialColor));
	}
	
	private final int indexFromColor(int color) {
		for (int i = 0; i < colors.length; i++) {
			if (color == colors[i]) {
				return i;
			}
		}
		return -1;
	}
	
	private final int indexFromPos(int x, int y) {
		final int col = x / cellSize;
		return (col >= 0 && col < colCount) ? indexFromColRow(col, y / cellSize) : -1;
	}
	
	private final int indexFromColRow(int col, int row) {
		final int idx = (row * colCount) + col;
		if (idx < colors.length) return idx;
		return -1;
	}
	
	private final int colFromIndex(int index) {
		return index % colCount;
	}
	
	private final int rowFromIndex(int index) {
		return index / colCount;
	}
	
	private final void setSelectedIndex(int index) {
		if (index < 0) index = 0;
		else if (index >= colors.length) index = colors.length - 1;
		
		final boolean trigger = (index != selectedIndex);
		
		selectedIndex = index;
		row = rowFromIndex(selectedIndex);
		col = colFromIndex(selectedIndex);
		
		invalidate();
		
		if (trigger)
			triggerChanged();
	}
	
	public final int getSelectedColor() {
		return colors[selectedIndex];
	}
	
	public final void setListener(ControlListener listener) {
		this.listener = listener;
	}
	
	public final ControlListener getListener() {
		return listener;
	}
	
	public final void resize(int width, int height, boolean repaint) {
		cellSize = Main.Customizer.getButtonHeight();
		
		colCount = width / cellSize;
		if (colCount > colors.length)
			colCount = colors.length;
		
		rowCount = colors.length / colCount;
		if (rowCount <= 0) rowCount = 1;
		if ((rowCount * colCount) < colors.length) rowCount++;
		
		super.resize(width, rowCount * cellSize, false);
		
		setSelectedIndex(selectedIndex);
		
		if (repaint)
			invalidate();
	}
	
	public final void ensureCursorVisible() {
		container.ensureControlVisibility(this, rowFromIndex(selectedIndex) * cellSize, cellSize);
	}
	
	private final void triggerChanged() {
		if (listener != null) {
			listener.eventControl(this, EVENT_COLORCHANGED, getSelectedColor(), null);
		}
	}
	
	private final void triggerSelected() {
		if (listener != null) {
			listener.eventControl(this, EVENT_COLORSELECTED, getSelectedColor(), null);
		}
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Main.KeyOK) {
			triggerSelected();
		} else if (keyCode == Main.KeyUp) {
			row--;
			if (row < 0) row = rowCount - 1;
		} else if (keyCode == Main.KeyDown) {
			row++;
			if (row >= rowCount) row = 0;
		} else if (keyCode == Main.KeyPgUp) {
			if (row == 0) {
				row = rowCount - 1;
			} else {
				row -= ((container.getHeight() / cellSize) - 1);
				if (row < 0) row = 0;
			}
		} else if (keyCode == Main.KeyPgDn) {
			if (row == (rowCount - 1)) {
				row = 0;
			} else {
				row += ((container.getHeight() / cellSize) - 1);
				if (row >= rowCount) row = rowCount - 1;
			}
		} else if (keyCode == Main.KeyLeft) {
			if (col <= 0) {
				col = colCount - 1;
				row--;
			} else {
				col--;
			}
		} else if (keyCode == Main.KeyRight) {
			if (col == (colCount - 1)) {
				col = 0;
				row++;
			} else {
				col++;
			}
		} else {
			return false;
		}
		
		final int old = selectedIndex;
		
		setSelectedIndex(indexFromColRow(col, row));
		
		if (selectedIndex != old)
			ensureCursorVisible();
		
		return true;
	}
	
	protected final void eventPointerDown(int x, int y) {
		if (pointInClient(x, y)) {
			downIndex = indexFromPos(x, y);
			if (downIndex >= 0 && downIndex < colors.length) {
				//only vibrate if the user clicked on a valid color
				super.eventPointerDown(x, y);
				
				if (downIndex != selectedIndex)
					setSelectedIndex(downIndex);
				else
					nextClickValid = true;
			}
		}
	}
	
	protected boolean eventPointerMove(int x, int y) {
		return false;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		super.eventPointerUp(x, y, isValid);
		
		if (pointInClient(x, y) && isValid && nextClickValid) {
			nextClickValid = false;
			if (downIndex == indexFromPos(x, y))
				triggerSelected();
		}
	}
	
	protected final void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		final int soX = screenOffsetX;
		final int clipR = clipX + clipWidth, clipB = clipY + clipHeight;
		
		for (int r = 0; r < rowCount; r++, screenOffsetY += cellSize) {
			if ((screenOffsetY + cellSize) <= clipY || screenOffsetY >= clipB) continue;
			
			for (int c = 0; c < colCount; c++, screenOffsetX += cellSize) {
				if ((screenOffsetX + cellSize) <= clipX || screenOffsetX >= clipR) continue;
				
				final int idx = indexFromColRow(c, r);
				
				if (idx < 0) break;
				
				g.setColor(colors[idx]);
				g.fillRect(screenOffsetX, screenOffsetY, cellSize, cellSize);
				
				if (idx == selectedIndex) {
					g.setColor(Color.bwForRGB(colors[idx]));
					g.drawRect(screenOffsetX, screenOffsetY, cellSize - 1, cellSize - 1);
					g.drawRect(screenOffsetX + 1, screenOffsetY + 1, cellSize - 3, cellSize - 3);
				}
			}
			
			if (screenOffsetX >= clipX && screenOffsetX < clipR) {
				g.setColor(Behaviour.ColorWindow);
				g.fillRect(screenOffsetX, screenOffsetY, clipR - screenOffsetX, cellSize);
			}
			
			screenOffsetX = soX;
		}
	}
}
