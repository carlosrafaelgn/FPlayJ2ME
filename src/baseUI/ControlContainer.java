//
// ControlContainer.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/ControlContainer.java
//

package baseUI;

import baseControls.ScrollBar;
import baseGraphics.Point;
import baseUtil.Vector;
import javax.microedition.lcdui.Graphics;

public class ControlContainer extends Control implements Container, ControlListener {
	private final Vector controls;
	private int offsetScrollBar, offsetY, contentHeight, lastPointerMoveY;
	private boolean hasFocusableControls, paintBackground;
	private ScrollBar scrollBar;
	private Control pointerControl, focusControl;
	
	public ControlContainer(Container container, int left, int top, int width, int height, boolean paintBackground) {
		super(container, left, top, width, height, true, true, false);
		
		this.paintBackground = paintBackground;
		controls = new Vector();
	}
	
	public final void invalidateChild(int x, int y, int width, int height) {
		//convert the coordinates from inside container space
		//to control space
		x += offsetScrollBar;
		y += offsetY;
		width += x; //width and height are now right and bottom
		height += y;
		//clip to inside the container
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		if (width > getWidth()) width = getWidth();
		if (height > getHeight()) height = getHeight();
		
		if (width > x && height > y) {
			getContainer().invalidateChild(getLeft() + x, getTop() + y, width - x, height - y);
		}
	}
	
	public final void containerPointToScreen(Point pt) {
		pt.x += getLeft();
		pt.y += getTop() + offsetY;
		getContainer().containerPointToScreen(pt);
	}
	
	public final void screenPointToContainer(Point pt) {
		getContainer().screenPointToContainer(pt);
		pt.x -= getLeft();
		pt.y -= getTop() + offsetY;
	}
	
	public final Control getControlAt(int x, int y) {
		//convert the coordinates from control space
		//to inside container space
		x -= offsetScrollBar;
		y -= offsetY;
		//must look for the controls in the reverse order, because they
		//are drawn from 0 to n, and that way, higher indexes will
		//be drawn on the top of lower indexes
		for (int i = controls.size() - 1; i >= 0; i--) {
			final Control c = (Control)controls.elementAt(i);
			if (c.pointInBounds(x, y)) return c;
		}
		return null;
	}
	
	public final Control getPointerControl() {
		return pointerControl;
	}
	
	public final Control getFocusControl() {
		return focusControl;
	}
	
	public final Control getControl(int index) {
		return (Control)controls.elementAt(index);
	}
	
	public final int getContentsHeight() {
		return contentHeight;
	}
	
	public final void scrollContents(int offset) {
		if (scrollBar.getOffset() == offset) return;
		final int off = scrollBar.getOffset();
		scrollBar.setOffset(offset, false);
		if (scrollBar.getOffset() != off)
			eventControl(scrollBar, ScrollBar.EVENT_CHANGED, scrollBar.getOffset(), null);
	}

	private final void ensureVisibility(int y, int height, boolean repaint, boolean byKey) {
		if (scrollBar != null) {
			final int ctlTop = y + offsetY;
			final int ctlBot = ctlTop + height;
			
			//already fully visible!
			if (ctlTop >= 0 && ctlBot <= getHeight()) return;
			
			//when using the keyboard, the control must be fully visible,
			//but when using the pointer, if at least 8 pixels of the control are
			//already visible, then it's ok!
			if ((!byKey || (height > getHeight())) && ctlTop <= (getHeight() - 8) && ctlBot >= 8) return;
			
			if (ctlTop < 0 || height > getHeight()) {
				//make it visible at the top
				scrollBar.setOffset(y, false);
			} else {
				//make it visible at the bottom
				scrollBar.setOffset(y - getHeight() + height, false);
			}
			offsetY = -scrollBar.getOffset();
			scrollBar.move(scrollBar.getLeft(), -offsetY, false);
			if (repaint) invalidate();
		}
	}
	
	public final void ensureControlVisibility(Control control) {
		ensureVisibility(control.getTop(), control.getHeight(), true, true);
	}
	
	public final void ensureControlVisibility(Control control, int controlY, int areaHeight) {
		ensureVisibility(control.getTop() + controlY, areaHeight, true, true);
	}
	
	private final void ensureControlVisibility(Control control, boolean repaint, boolean byKey) {
		ensureVisibility(control.getTop(), control.getHeight(), repaint, byKey);
	}
	
	public final void setFocusControl(Control control) {
		setFocusControl(control, true);
	}
	
	private final void setFocusControl(Control control, boolean byKey) {
		if (control == focusControl || (control != null && !control.isFocusable())) return;
		
		final Control oldControl = focusControl;
		focusControl = control;
		if (oldControl != null) {
			oldControl.setFocused(false);
		}
		if (control != null) {
			control.setFocused(true);
			ensureControlVisibility(control, true, byKey);
		}
		//must invalidate AFTER calling ensureControlVisibility because of the offsets
		if (oldControl != null) oldControl.invalidate();
		if (control != null) control.invalidate();
	}
	
	public final int getControlCount() {
		return controls.size();
	}
	
	private final void refreshOffsets(int width, int height) {
		if (contentHeight <= height) {
			//no need for the scrollBar
			if (scrollBar != null) {
				//if there was no scrollBar already, just ignore
				offsetScrollBar = 0;
				offsetY = 0;
				scrollBar = null;
			}
		} else {
			if (scrollBar == null) {
				//create a new scrollbar if needed
				final int sbw = Main.Customizer.getScrollWidth();
				offsetScrollBar = Main.environmentIsRightHanded() ? 0 : sbw;
				scrollBar = new ScrollBar(this, Main.environmentIsRightHanded() ? (width - sbw) : -sbw, 0, height);
				scrollBar.setListener(this);
			}
			scrollBar.setup(-offsetY, contentHeight, height, false);
			offsetY = -scrollBar.getOffset();
			scrollBar.move(scrollBar.getLeft(), -offsetY, false);
			if (focusControl != null) ensureControlVisibility(focusControl, false, true);
		}
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		switch (eventId) {
		case ScrollBar.EVENT_CHANGED:
			offsetY = -eventArg1;
			scrollBar.move(scrollBar.getLeft(), eventArg1, false);
			invalidate();
			break;
		}
	}
	
	public final void addControl(Control control, boolean processLayout) {
		this.controls.addElement(control);
		if (processLayout) {
			refreshLayout(getWidth(), getHeight());
			if (control.getTop() < getHeight() && control.getBottom() > 0)
				invalidate();
			else if (scrollBar != null)
				scrollBar.invalidate();
		}
	}
	
	public final void insertControlAt(Control control, int insertAtIndex, boolean processLayout) {
		this.controls.insertElementAt(control, insertAtIndex);
		if (processLayout) {
			refreshLayout(getWidth(), getHeight());
			if (control.getTop() < getHeight() && control.getBottom() > 0)
				invalidate();
			else if (scrollBar != null)
				scrollBar.invalidate();
		}
	}
	
	public final void addControls(Control[] controls, boolean processLayout) {
		this.controls.addElements(controls);
		if (processLayout) {
			refreshLayout(getWidth(), getHeight());
			invalidate();
		}
	}
	
	public final void insertControlsAt(Control[] controls, int insertAtIndex, boolean processLayout) {
		this.controls.insertElementsAt(controls, insertAtIndex);
		if (processLayout) {
			refreshLayout(getWidth(), getHeight());
			invalidate();
		}
	}
	
	public final void removeControlAt(int index, boolean processLayout) {
		if (focusControl == this.controls.elementAt(index)) {
			setFocusControl(null, false);
		}
		this.controls.removeElementAt(index);
		if (processLayout) {
			refreshLayout(getWidth(), getHeight());
			invalidate();
		}
	}
	
	public final void removeControl(Control control, boolean processLayout) {
		if (focusControl == control) {
			setFocusControl(null, false);
		}
		this.controls.removeElement(control);
		if (processLayout) {
			refreshLayout(getWidth(), getHeight());
			invalidate();
		}
	}
	
	public final void removeAllControls(boolean processLayout) {
		setFocusControl(null, false);
		this.controls.removeAllElements();
		if (processLayout) {
			refreshLayout(getWidth(), getHeight());
			invalidate();
		}
	}
	
	public final void processLayout() {
		refreshLayout(getWidth(), getHeight());
	}
	
	private final void refreshLayout(int width, int height) {
		contentHeight = 0;
		hasFocusableControls = false;
		for (int i = 0; i < controls.size(); i++) {
			final Control c = (Control)controls.elementAt(i);
			final int b = c.getBottom();
			if (b > contentHeight)
				contentHeight = b;
			if (c.isFocusable()) hasFocusableControls = true;
		}
		refreshOffsets(width, height);
	}
	
	public final boolean isPaintBackground() {
		return paintBackground;
	}
	
	public final void setPaintBackground(boolean paintBackground) {
		this.paintBackground = paintBackground;
		invalidate();
	}
	
	public final void resize(int width, int height, boolean repaint) {
		if (scrollBar != null) {
			offsetScrollBar = Main.environmentIsRightHanded() ? 0 : scrollBar.getWidth();
			scrollBar.reposition(Main.environmentIsRightHanded() ? (width - scrollBar.getWidth()) : -scrollBar.getWidth(), 0, scrollBar.getWidth(), height, false);
		}
		
		refreshLayout(width, height);
		
		super.resize(width, height, repaint);
	}
	
	protected final void eventClose() {
		for (int i = 0; i < controls.size(); i++) {
			((Control)controls.elementAt(i)).eventClose();
		}
	}
	
	protected final void eventFocus() {
		if (focusControl != null) {
			focusControl.setFocused(isFocused());
		} else if (isFocused()) {
			for (int i = 0; i < controls.size(); i++) {
				final Control c = (Control)controls.elementAt(i);
				if (c.isFocusable()) {
					setFocusControl(c, true);
					break;
				}
			}
		}
	}
	
	protected final void eventEnvironment(int changedFlags) {
		for (int i = 0; i < controls.size(); i++) {
			((Control)controls.elementAt(i)).eventEnvironment(changedFlags);
		}
	}
	
	private final boolean eventKeyPressScrollFocusControl(boolean scrollDown) {
		//check if we only need to scroll the view a bit
		int amount = (3 * getHeight()) >> 4;
		if (amount < 8) amount = 8;
		if (scrollDown) {
			if ((focusControl.getBottom() + offsetY) > getHeight()) {
				//scroll the view down a bit
				ensureVisibility(amount - offsetY, getHeight(), true, true);
				return true;
			}
		} else {
			if ((focusControl.getTop() + offsetY) < 0) {
				//scroll the view up a bit
				ensureVisibility(-offsetY - amount, getHeight(), true, true);
				return true;
			}
		}
		return false;
	}
	
	private final void eventKeyPressFocusControl(Control control, boolean scrollDown) {
		//first, make an area of the control visible, then set its focus
		ensureVisibility(scrollDown ?
			control.getTop() : //make the top area visible
			control.getBottom() - getHeight(), //make the bottom area visible
			getHeight(), true, true);
		setFocusControl(control, false);
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (hasFocusableControls) {
			//return true to indicate that the key has been used
			if (focusControl != null && focusControl.isEnabled() && focusControl.eventKeyPress(keyCode, repeatCount)) {
				return true;
			}
			
			final boolean scrollDown = ((keyCode == Main.KeyDown) || (keyCode == Main.KeyRight));
			
			if (scrollDown || keyCode == Main.KeyLeft || keyCode == Main.KeyUp) {
				int idx;
				if (focusControl == null) {
					//create a fake previous position
					if (scrollDown) {
						idx = controls.size() - 1;
					} else {
						idx = ((controls.size() > 1) ? 1 : 0);
					}
				} else {
					idx = controls.indexOf(focusControl);
					if (idx < 0) idx = 0;
					
					if (focusControl.getHeight() > getHeight() &&
						eventKeyPressScrollFocusControl(scrollDown)) {
						return true;
					}
				}
				
				final int oidx = idx;
				
				if (scrollDown) {
					//switch to the next focusable control
					do {
						idx++;
						if (idx >= controls.size()) idx = 0;
					} while (idx != oidx && !((Control)controls.elementAt(idx)).isFocusable());
				} else {
					//switch to the previous focusable control
					do {
						idx--;
						if (idx < 0) idx = controls.size() - 1;
					} while (idx != oidx && !((Control)controls.elementAt(idx)).isFocusable());
				}
				
				final Control ctl = (Control)controls.elementAt(idx);
				if (ctl != focusControl && ctl.isFocusable()) {
					if (ctl.getHeight() <= getHeight()) {
						setFocusControl(ctl, true);
					} else {
						eventKeyPressFocusControl(ctl, scrollDown);
					}
					return true;
				}
			}
		}
		
		return false;
	}
	
	protected final void eventKeyRelease(int keyCode) {
		if (focusControl != null && focusControl.isEnabled())
			focusControl.eventKeyRelease(keyCode);
	}
	
	protected final void eventPointerDown(int x, int y) {
		if (scrollBar != null &&
			(x >= (scrollBar.getLeft() + offsetScrollBar - 16)) &&
			(x < (scrollBar.getRight() + offsetScrollBar + 16))) {
			pointerControl = scrollBar;
		} else {
			pointerControl = getControlAt(x, y);
		}
		
		if (pointerControl != null) {
			//must call setFocusControl before sending the eventPointerDown,
			//because of the offsets
			setFocusControl(pointerControl, false);
			if (pointerControl.isEnabled())
				pointerControl.eventPointerDown(x - pointerControl.getLeft() - offsetScrollBar, y - pointerControl.getTop() - offsetY);
		}
	}
	
	protected final boolean eventPointerMove(int x, int y) {
		//if there is nothing to scroll, behave normally, otherwise,
		//if the user has moved vertically an amount greater than
		//the threshold, cancel the pointer control and start scrolling...
		boolean used = ((pointerControl == null || !pointerControl.isEnabled()) ? false : pointerControl.eventPointerMove(x - pointerControl.getLeft() - offsetScrollBar, y - pointerControl.getTop() - offsetY));
		if (scrollBar != null && !used) {
			//release the current control
			if (Main.pointerExceededThreshold()) {
				if (pointerControl != null || Main.pointerExceededThresholdForTheFirstTime()) {
					if (pointerControl != null) {
						pointerControl.eventPointerUp(Integer.MIN_VALUE, Integer.MIN_VALUE, false);
						pointerControl = null;
					}
					lastPointerMoveY = y;
					used = true;
				} else {
					//scroll the screen
					final int off = scrollBar.getOffset();
					//scrolling here must be upside-down in order to feel natural
					scrollBar.setOffset(off - (y - lastPointerMoveY), false);
					if (scrollBar.getOffset() != off) {
						lastPointerMoveY = y;
						eventControl(scrollBar, ScrollBar.EVENT_CHANGED, scrollBar.getOffset(), null);
					}
				}
			}
		}
		return used;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		if (pointerControl != null) {
			if (pointerControl.isEnabled())
				pointerControl.eventPointerUp(x - pointerControl.getLeft() - offsetScrollBar, y - pointerControl.getTop() - offsetY, isValid);
			pointerControl = null;
		}
	}
	
	protected void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		if (paintBackground) {
			g.setColor(Behaviour.ColorWindow);
			g.fillRect(clipX, clipY, clipWidth, clipHeight);
		}
		
		final int lclipX = clipX - screenOffsetX;
		final int lclipY = clipY - screenOffsetY;
		int x, y, r, b;
		
		for (int i = 0; i < controls.size(); i++) {
			final Control c = (Control)controls.elementAt(i);
			x = c.getLeft() + offsetScrollBar; y = c.getTop() + offsetY;
			r = c.getWidth() + x; b = c.getHeight() + y;
			
			if (lclipX > x) x = lclipX;
			if (lclipY > y) y = lclipY;
			if ((lclipX + clipWidth) < r) r = lclipX + clipWidth;
			if ((lclipY + clipHeight) < b) b = lclipY + clipHeight;
			r -= x; //r and b are now width and height
			b -= y;
			if (r > 0 && b > 0) {
				g.setClip(x + screenOffsetX, y + screenOffsetY, r, b);
				c.paint(g, c.getLeft() + screenOffsetX + offsetScrollBar, c.getTop() + screenOffsetY + offsetY, x + screenOffsetX, y + screenOffsetY, r, b);
			}
		}
		
		if (scrollBar != null) {
			final Control c = scrollBar;
			x = c.getLeft() + offsetScrollBar; y = 0;
			r = getWidth() + x; b = getHeight();
			
			if (lclipX > x) x = lclipX;
			if (lclipY > y) y = lclipY;
			if ((lclipX + clipWidth) < r) r = lclipX + clipWidth;
			if ((lclipY + clipHeight) < b) b = lclipY + clipHeight;
			r -= x; //r and b are now width and height
			b -= y;
			if (r > 0 && b > 0) {
				g.setClip(x + screenOffsetX, y + screenOffsetY, r, b);
				//c.paint(g, getWidth() - c.getWidth() + screenOffsetX, screenOffsetY, x + screenOffsetX, y + screenOffsetY, r, b);
				c.paint(g, c.getLeft() + screenOffsetX + offsetScrollBar, screenOffsetY, x + screenOffsetX, y + screenOffsetY, r, b);
			}
		}
		
		g.setClip(clipX, clipY, clipWidth, clipHeight);
	}
}
