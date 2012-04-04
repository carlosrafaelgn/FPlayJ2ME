//
// OverlayAlert.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/OverlayAlert.java
//

package baseUI;

import baseGraphics.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class OverlayAlert {
	public static final int TYPE_OK = 0;
	public static final int TYPE_OKCANCEL = 1;
	public static final int REPLY_NONE = -1;
	public static final int REPLY_OK = 0;
	public static final int REPLY_CANCEL = 1;

	private static int bgImageCount;
	private static Image bgImage;
	
	private final String message;
	private final Font font;
	private final int id, type;
	private final OverlayListener listener;
	private final Command commandOK, commandCancel;
	private int alertX, alertY, alertWidth, alertHeight, clientWidth, firstLine, lineCount, result;
	private int[] linesEndIndex;
	OverlayAlert next, previous;
	
	OverlayAlert(String message, Font font, int type, int alertId, OverlayListener listener) {
		result = REPLY_NONE;
		this.message = message;
		this.font = font;
		this.id = alertId;
		this.type = type;
		this.listener = listener;
		
		if (type == TYPE_OK) {
			commandOK = Main.commandBack();
			commandCancel = null;
		} else if (type == TYPE_OKCANCEL) {
			commandOK = Main.commandOK();
			commandCancel = Main.commandCancel();
		} else {
			commandOK = null;
			commandCancel = null;
		}
		
		prepareBgImage(true);
		
		maximizeWindow();
	}
	
	private synchronized static void prepareBgImage(boolean create) {
		if (create) {
			if (bgImageCount <= 0) {
				bgImageCount = 1;
			} else {
				bgImageCount++;
			}
			
			if (bgImage == null) {
				final int[] bg = new int [16 * 16];
				if (Main.isAlphaSupported()) {
					for (int i = 0; i < 256; ++i) {
						bg[i] = 0x80000000;
					}
				} else {
					for (int i = 0; i < 256; ++i) {
						//alternating pattern (just fill the black opaque
						//dots, since the transparent ones are 0)
						//even rows with even columns, and odd rows with odd columns
						if ((i & 0x11) == 0 ||
							(i & 0x11) == 0x11) {
							bg[i] = 0xFF000000;
						}
					}
				}
				bgImage = Image.createRGBImage(bg, 16, 16, true);
			}
		} else {
			bgImageCount--;
			
			if (bgImageCount <= 0) {
				bgImageCount = 0;
				bgImage = null;
			}
		}
	}
	
	final void closed() {
		if (listener != null) {
			listener.eventOverlay(id, result);
		}
		
		prepareBgImage(false);
	}
	
	final Command getLeftCommand() {
		return ((type == TYPE_OKCANCEL) ? commandOK : null);
	}
	
	final Command getMiddleCommand() {
		return ((type == TYPE_OK) ? commandOK : null);
	}
	
	final Command getRightCommand() {
		return ((type == TYPE_OK) ? commandOK : ((type == TYPE_OKCANCEL) ? commandCancel : null));
	}
	
	final boolean eventCommand(Command command) {
		if (command.equals(commandOK)) {
			result = REPLY_OK;
			Main.alertClose(this);
			return true;
		} else if (command.equals(commandCancel)) {
			result = REPLY_CANCEL;
			Main.alertClose(this);
			return true;
		}
		return false;
	}
	
	public final int getId() {
		return id;
	}
	
	public final int getResult() {
		return result;
	}
	
	private final boolean canMoveUp() {
		return (firstLine > 0 && lineCount > 4);
	}
	
	private final boolean canMoveDown() {
		return (firstLine < (lineCount - 4) && lineCount > 4);
	}
	
	final void maximizeWindow() {
		firstLine = 0;
		
		alertX = Main.MaximizedX;
		alertWidth = Main.MaximizedWidth;
		
		clientWidth = alertWidth - 4;
		linesEndIndex = font.countLinesAndEndIndexes(message, clientWidth);
		int lc = linesEndIndex.length;
		if (lc > 4) {
			//save space for the scrollbar
			clientWidth -= (font.height >> 1);
			linesEndIndex = font.countLinesAndEndIndexes(message, clientWidth);
			lc = linesEndIndex.length;
		}
		
		lineCount = lc;
		alertHeight = 4 + ((lc > 4 ? 4 : (lc < 2 ? 2 : lc)) * font.height);
		alertY = (Main.environmentIsMenuAbove() ? Main.MaximizedY : (Main.MaximizedBottom - alertHeight));
	}
	
	final void eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Main.KeyUp) {
			moveUp();
		} else if (keyCode == Main.KeyDown) {
			moveDown();
		} else if (keyCode == Main.KeyPgUp) {
			pageUp();
		} else if (keyCode == Main.KeyPgDn) {
			pageDown();
		}
	}
	
	private final void invalidate() {
		Main.invalidateArea(alertX, alertY, alertWidth, alertHeight);
	}
	
	private final void moveUp() {
		if (!canMoveUp()) return;
		--firstLine;
		invalidate();
	}
	
	private final void moveDown() {
		if (!canMoveDown()) return;
		++firstLine;
		invalidate();
	}
	
	private final void pageUp() {
		if (!canMoveUp()) return;
		if (firstLine < 4) firstLine = 0;
		else firstLine -= 4;
		invalidate();
	}
	
	private final void pageDown() {
		if (!canMoveDown()) return;
		if ((lineCount - firstLine - 4) <= 4) firstLine = lineCount - 4;
		else firstLine += 4;
		invalidate();
	}
	
	final void paint(Graphics g, int clipX, int clipY, int clipWidth, int clipHeight) {
		//the alert covers the entire window area 
		int x = Main.MaximizedX, y = Main.MaximizedY;
		int r = Main.MaximizedWidth + x, b = Main.MaximizedHeight + y;
		
		if (clipX > x) x = clipX;
		if (clipY > y) y = clipY;
		if ((clipX + clipWidth) < r) r = clipX + clipWidth;
		if ((clipY + clipHeight) < b) b = clipY + clipHeight;
		
		if (r > x && b > y) {
			//paint the dark background
			final int bgY = (Main.environmentIsMenuAbove() ? Math.max(alertY + alertHeight, y) : y);
			final int bgBottom = (Main.environmentIsMenuAbove() ? b : Math.min(alertY, b)) - 16;
			if (bgBottom > bgY && bgImage != null) {
				int iy = bgY;
				int ix;
				
				r -= 16;
				
				for ( ; iy <= bgBottom; iy += 16) {
					for (ix = x; ix <= r; ix += 16) {
						g.drawImage(bgImage, ix, iy, 0);
					}
					//paint the last column
					if (ix < (r + 16)) {
						g.drawRegion(bgImage, 0, 0, (r + 16) - ix, 16, 0, ix, iy, 0);
					}
				}
				
				//paint the last row
				final int notPaintedHeight = (bgBottom + 16) - iy;
				if (notPaintedHeight > 0) {
					for (ix = x; ix <= r; ix += 16) {
						g.drawRegion(bgImage, 0, 0, 16, notPaintedHeight, 0, ix, iy, 0);
					}
					//paint the last column
					if (ix < (r + 16)) {
						g.drawRegion(bgImage, 0, 0, (r + 16) - ix, notPaintedHeight, 0, ix, iy, 0);
					}
				}
				
				r += 16;
			}
			
			if (b > alertY && y < (alertY + alertHeight)) {
				//drawRect and fillRect treat width and height in different ways!
				g.setColor(Behaviour.ColorMenu);
				g.fillRect(alertX + 1, alertY + 1, alertWidth - 2, alertHeight - 2);
				g.setColor(Behaviour.ColorMenuText);
				font.drawString(message, g, alertX + 2, alertY + 2, linesEndIndex, alertHeight - 4, firstLine, false);
				g.setColor(Behaviour.ColorSelection);
				g.drawRect(alertX, alertY, alertWidth - 1, alertHeight - 1);
				if (lineCount > 4) {
					final int h = alertHeight - 8;
					final int lx = alertX + alertWidth - 2 - (font.height >> 2);
					final int pos = alertY + 4 + (((h * firstLine) / (lineCount - 4)) >> 1);
					g.drawLine(lx, alertY + 4, lx, alertY + 4 - 1 + h);
					g.fillRect(lx - 2, pos, 5, h - (h >> 1));
				}
			}
		}
	}
}
