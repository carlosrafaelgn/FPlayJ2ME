//
// Control.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/Control.java
//

package baseUI;

import baseGraphics.Point;
import javax.microedition.lcdui.Graphics;

public abstract class Control implements MessageListener {
	private final Container container;
	private boolean pressed, focused, enabled;
	private final boolean focusable, draggable, vibrateOnPointerDown;
	private int left, top, width, height;
	
	private static int longPressVisualSize = 16;
	private static final int longPressBonusCounter = 6;
	private static final int longPressTriggerCounter = 9;
	private static Control longPressControl;
	private static int longPressVisualCounter, longPressVisualX, longPressVisualY, longPressKey, longPressX, longPressY, longPressVersion = 0, longPressLastVersion = - 1;
	
	public Control(Container container, int left, int top, int width, int height, boolean focusable, boolean draggable, boolean vibrateOnPointerDown) {
		this.container = container;
		this.enabled = true;
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.focusable = focusable;
		this.draggable = draggable;
		this.vibrateOnPointerDown = vibrateOnPointerDown;
	}
	
	protected static int getDefault2RowItemHeight() {
		return 2 + (Main.FontUI.height >> 1) + Main.FontUI.height + Main.Customizer.getItemHeight();
	}
	
	public final boolean pointInBounds(int x, int y) {
		return (left <= x) && (top <= y) && ((left + width) > x) && ((top + height) > y);
	}
	
	protected final boolean pointInClient(int x, int y) {
		return (0 <= x) && (0 <= y) && (width > x) && (height > y);
	}
	
	protected final boolean isLongPressProcessing() {
		return (longPressVersion == longPressLastVersion);
	}

	protected final void drawLongPressClue(Graphics g, int screenOffsetX, int screenOffsetY) {
		if (longPressVersion == longPressLastVersion && longPressControl == this && longPressVisualCounter > longPressBonusCounter) {
			//g.setColor(Main.ColorHilightText);
			//g.drawRect(screenOffsetX + longPressVisualX, screenOffsetY + longPressVisualY, longPressVisualSize - 1, longPressVisualSize - 1);
			g.setColor(Behaviour.ColorWindow);
			g.fillRect(screenOffsetX + longPressVisualX, screenOffsetY + longPressVisualY, longPressVisualSize, longPressVisualSize);
			final int cr = (((Behaviour.ColorHilight & 0xFF0000) >> 16) * (longPressVisualCounter - longPressBonusCounter)) / longPressTriggerCounter;
			final int cg = (((Behaviour.ColorHilight & 0x00FF00) >> 8) * (longPressVisualCounter - longPressBonusCounter)) / longPressTriggerCounter;
			final int cb = ((Behaviour.ColorHilight & 0x0000FF) * (longPressVisualCounter - longPressBonusCounter)) / longPressTriggerCounter;
			final int cs = ((longPressVisualSize - 2) * (longPressVisualCounter - longPressBonusCounter)) / longPressTriggerCounter;
			g.setColor((cr << 16) | (cg << 8) | cb);
			g.fillRect(screenOffsetX + longPressVisualX + 1, screenOffsetY + longPressVisualY + 1, cs, cs);
		}
	}
	
	protected final void longPressProcessStart(int x, int y) {
		longPressProcessStart(Integer.MIN_VALUE, x, y);
	}
	
	protected final void longPressProcessStart(int keyCode, int x, int y) {
		longPressProcessAbort();
		if (!isEnabled()) return;
		
		longPressLastVersion = longPressVersion;
		longPressControl = this;
		longPressKey = keyCode;
		longPressX = x;
		longPressY = y;
		longPressVisualCounter = 0;
		longPressVisualX = x;
		longPressVisualY = y;
		
		(new MessageThread(this, "Long Press Processing")).start(Main.SYSMSG_CONTROLSTARTLONGPRESSLOOPING, longPressVersion, null);
	}
	
	protected static boolean longPressProcessAbort() {
		if (longPressVersion != longPressLastVersion) return true;
		
		longPressVersion++;
		
		//return true to indicate that no action has been cancelled
		if (longPressVisualCounter <= longPressBonusCounter)
			return true;
		
		longPressControl.invalidate(longPressVisualX, longPressVisualY, longPressVisualSize, longPressVisualSize);
		return false;
	}
	
	public final boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public final Container getContainer() {
		return container;
	}
	
	public final int getLeft() {
		return left;
	}
	
	public final int getTop() {
		return top;
	}
	
	public final int getRight() {
		return left + width;
	}
	
	public final int getBottom() {
		return top + height;
	}
	
	public final int getWidth() {
		return width;
	}
	
	public final int getHeight() {
		return height;
	}
	
	public final void invalidate() {
		container.invalidateChild(left, top, width, height);
	}
	
	public final void invalidate(int x, int y, int width, int height) {
		container.invalidateChild(left + x, top + y, width, height);
	}
	
	public void move(int x, int y, boolean repaint) {
		if (repaint) {
			int rl = left, rt = top;
			final int rr = left + width, rb = top + height;
			
			left = x;
			top = y;
			
			if (x < rl) rl = x;
			if (y < rt) rt = y;
			
			container.invalidateChild(rl, rt, rr - rl, rb - rt);
		} else {
			left = x;
			top = y;
		}
	}
	
	public void resize(int width, int height, boolean repaint) {
		if (repaint) {
			final int rl = left, rt = top;
			int rw = this.width, rh = this.height;
			
			this.width = width;
			this.height = height;
			
			if (width > rw) rw = width;
			if (height > rh) rh = height;
			
			container.invalidateChild(rl, rt, rw, rh);
		} else {
			this.width = width;
			this.height = height;
		}
	}
	
	public final void reposition(int x, int y, int width, int height, boolean repaint) {
		if (repaint) {
			int rl = left, rt = top, rr = left + this.width, rb = top + this.height;
			
			resize(width, height, false);
			move(x, y, false);
			
			if (x < rl) rl = x;
			if (y < rt) rt = y;
			if ((left + this.width) > rr) rr = left + this.width;
			if ((top + this.height) > rb) rb = top + this.height;
			
			container.invalidateChild(rl, rt, rr - rl, rb - rt);
		} else {
			resize(width, height, false);
			move(x, y, false);
		}
	}
	
	public final boolean isPressed() {
		return pressed;
	}
	
	private final void setPressed(boolean pressed) {
		this.pressed = pressed;
	}
	
	public final boolean isFocusable() {
		return focusable;
	}
	
	public final boolean isDraggable() {
		return draggable;
	}
	
	public final boolean isFocused() {
		return focused;
	}
	
	final void setFocused(boolean focused) {
		if (this.focused != focused) {
			this.focused = focused;
			eventFocus();
		}
	}
	
	public final void clientPointToScreen(Point pt) {
		pt.x += left;
		pt.y += top;
		container.containerPointToScreen(pt);
	}
	
	public final void screenPointToClient(Point pt) {
		container.screenPointToContainer(pt);
		pt.x -= left;
		pt.y -= top;
	}
	
	public void eventMessage(int message, int iParam, Object oParam) {
		switch (message) {
		case Main.SYSMSG_CONTROLPROCESSLONGPRESS:
			if (iParam == longPressVersion) {
				Main.inputCancel();
				longPressControl.eventLongPress((longPressKey != Integer.MIN_VALUE), longPressKey, longPressX, longPressY);
			}
			break;
		case Main.SYSMSG_CONTROLSTARTLONGPRESSLOOPING:
			while (iParam == longPressVersion) {
				longPressVisualCounter++;
				
				try {
					Thread.sleep(50);
				} catch (Exception ex) {
				}
				
				if (longPressVisualCounter >= (longPressBonusCounter + longPressTriggerCounter)) {
					if (iParam == longPressVersion) {
						Main.postMessage(this, Main.SYSMSG_CONTROLPROCESSLONGPRESS, iParam, null);
					} else {
						longPressProcessAbort();
						break;
					}
				} else if (longPressVisualCounter > longPressBonusCounter) {
					//while counter <= longPressBonusCounter it's bonus time
					if (iParam == longPressVersion) {
						if (longPressVisualCounter == (longPressBonusCounter + 1)) {
							final int size2 = (longPressVisualSize >> 1);
							if ((longPressVisualX + size2) > width) longPressVisualX = width - size2;
							if (longPressVisualX < size2)
								longPressVisualX = 0;
							else
								longPressVisualX -= size2;
							
							if ((longPressVisualY + size2) > height) longPressVisualY = height - size2;
							if (longPressVisualY < size2)
								longPressVisualY = 0;
							else
								longPressVisualY -= size2;
						}
						
						//refresh the screen here update the visual clue
						longPressControl.invalidate(longPressVisualX, longPressVisualY, longPressVisualSize, longPressVisualSize);
					} else {
						longPressProcessAbort();
						break;
					}
				}
			}
			break;
		}
	}
	
	protected void eventClose() { }
	
	protected void eventFocus() { }
	
	protected void eventEnvironment(int changedFlags) { }
	
	protected boolean eventKeyPress(int keyCode, int repeatCount) {
		//return true to indicate that the key has been used
		return false;
	}
	
	protected void eventKeyRelease(int keyCode) { }
	
	protected void eventLongPress(boolean byKey, int keyCode, int x, int y) { }
	
	protected void eventPointerDown(int x, int y) {
		//if (pressed && vibrateOnPointerDown && pressed != this.pressed) MainCanvas.environmentVibrate();
		setPressed(true);
		invalidate();
		if (vibrateOnPointerDown) Main.environmentDoTouchFeedback();
	}
	
	protected boolean eventPointerMove(int x, int y) {
		if (Main.pointerExceededThreshold()) {
			final boolean p = pointInClient(x, y);
			if (p != isPressed()) {
				setPressed(p);
				invalidate();
			}
		}
		//return false to indicate that the control
		//has not used the movement. if isDraggable
		//is false, the control container will ignore
		//the return value
		return false;
	}
	
	protected void eventPointerUp(int x, int y, boolean isValid) {
		setPressed(false);
		invalidate();
	}
	
	protected abstract void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight);
}
