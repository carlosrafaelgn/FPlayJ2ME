//
// Window.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/Window.java
//

package baseUI;

import baseGraphics.Point;
import javax.microedition.lcdui.Graphics;

public abstract class Window implements CommandListener, Container {
	private static int FLAG_FULLSCREEN = 0x01;
	private static int FLAG_OPEN = 0x02;
	private static int FLAG_OPENSCHEDULED = 0x04;
	private static int FLAG_CLOSESCHEDULED = 0x08;
	private int left, top, height, width, flags;
	private Menu subMenu;
	private ControlContainer container;
	final Window previousWindow;
	private final int titleHeight;
	private boolean drawingTitle;
	
	protected Window() {
		this(Main.appWindow(), false, 0);
	}
	
	protected Window(int titleHeight) {
		this(Main.appWindow(), false, titleHeight);
	}
	
	protected Window(boolean isFullScreen) {
		this(Main.appWindow(), isFullScreen, 0);
	}
	
	protected Window(boolean isFullScreen, int titleHeight) {
		this(Main.appWindow(), isFullScreen, titleHeight);
	}
	
	Window(Window previous, boolean isFullScreen, int titleHeight) {
		previousWindow = previous;
		flags = (isFullScreen ? FLAG_FULLSCREEN : 0);
		this.titleHeight = titleHeight;
		drawingTitle = (titleHeight > 0);
		maximizeWindowPrivate();
		container = new ControlContainer(this, 0, 0, width, height, false);
	}
	
	//public String toString() {
	//	return "Window " + super.toString() + (subMenu == null ? "" : " SubMenu: " + subMenu.toString());
	//}
	
	public final boolean isFullScreen() {
		return ((flags & FLAG_FULLSCREEN) != 0);
	}
	
	final void setClosed() {
		//this is safe, because this method is always called from
		//the same thread
		flags &= ~FLAG_OPEN;
	}
	
	final void setOpen() {
		//this is safe, because this method is always called from
		//the same thread
		flags |= FLAG_OPEN;
	}
	
	final boolean isOpen() {
		return ((flags & FLAG_OPEN) != 0);
	}
	
	final void setOpenScheduled() {
		flags |= FLAG_OPENSCHEDULED;
	}
	
	final boolean isOpenScheduled() {
		return ((flags & FLAG_OPENSCHEDULED) != 0);
	}
	
	final void setCloseScheduled() {
		flags |= FLAG_CLOSESCHEDULED;
	}
	
	final boolean isCloseScheduled() {
		return ((flags & FLAG_CLOSESCHEDULED) != 0);
	}
	
	public final int getLeft() {
		return left;
	}
	
	public final int getTop() {
		return top;
	}
	
	public final int getWidth() {
		return width;
	}
	
	public final int getHeight() {
		return height;
	}
	
	public final int getRight() {
		return left + width;
	}
	
	public final int getBottom() {
		return top + height;
	}
	
	public final int getTitleHeight() {
		return titleHeight;
	}
	
	public final void setDrawingTitle(boolean drawingTitle) {
		this.drawingTitle = ((titleHeight > 0) ? drawingTitle : false);
	}
	
	public final boolean isDrawingTitle() {
		return drawingTitle;
	}
	
	private final void maximizeWindowPrivate() {
		if (isFullScreen()) {
			left = 0;
			top = 0;
			width = Main.ScreenWidth;
			height = Main.ScreenHeight;
		} else {
			left = Main.MaximizedX;
			top = Main.MaximizedY;
			width = Main.MaximizedWidth;
			height = Main.MaximizedHeight;
		}
	}
	
	final void maximizeWindow() {
		maximizeWindowPrivate();
		eventResize();
	}
	
	final void reposition(int x, int y, int width, int height) {
		this.left = x;
		this.top = y;
		this.width = width;
		this.height = height;
	}
	
	public final boolean containsPoint(int x, int y) {
		return (left <= x) && (top <= y) && ((left + width) > x) && ((top + height) > y);
	}
	
	public final boolean intersectsRectangle(int left, int top, int width, int height) {
		return (this.left < (left + width)) && (this.top < (top + height)) && (left < (this.left + this.width)) && (top < (this.top + this.height));
	}
	
	public final void invalidate() {
		if (Main.appWindow() == this || (this instanceof Menu))
			Main.invalidateArea(left, top, width, height);
	}
	
	public final void invalidate(int x, int y, int width, int height) {
		if (Main.appWindow() == this || (this instanceof Menu))
			Main.invalidateArea(left + x, top + y, width, height);
	}
	
	public final void invalidateChild(int x, int y, int width, int height) {
		if (Main.appWindow() == this || (this instanceof Menu))
			Main.invalidateArea(left + x, top + y, width, height);
	}
	
	public final void containerPointToScreen(Point pt) {
		pt.x += getLeft();
		pt.y += getTop();
		//no need to call Main.containerPointToScreen because Main does not have any offsets
	}
	
	public final void screenPointToContainer(Point pt) {
		//no need to call Main.screenPointToContainer because Main does not have any offsets
		pt.x -= getLeft();
		pt.y -= getTop();
	}
	
	public void invalidateTitle() {
		if (Main.appWindow() == this || (this instanceof Menu))
			Main.invalidateArea(left, top, width, titleHeight);
	}
	
	final void opening() {
		if (!isOpen()) {
			eventOpening();
			if (!(this instanceof Menu)) {
				maximizeWindow();
			}
		}
	}
	
	final void opened() {
		if (!isOpen()) {
			setOpen();
			eventOpened();
		}
		focus(true);
	}
	
	final void focus(boolean focused) {
		container.setFocused(focused);
		eventFocus(focused);
	}
	
	final void closing() {
		if (isOpen()) {
			closeSubMenu();
			focus(false);
			container.eventClose();
			eventClosing();
		}
	}
	
	final void closed() {
		if (isOpen()) {
			setClosed();
			eventClosed();
		}
	}
	
	final void subMenuClosing() {
		subMenu = null;
	}
	
	protected final void close() {
		Main.closeWindow(this, true);
	}
	
	protected final void close(boolean allowTransition) {
		Main.closeWindow(this, allowTransition);
	}
	
	public final void showMenu(int id, MenuItem[] items) {
		showMenu(Menu.createMenu(id, items));
	}
	
	public final void showMenu(int id, MenuItem[] items, int x, int y) {
		showMenu(Menu.createMenu(id, items, x, y, false));
	}
	
	final void showMenu(Menu menu) {
		if (this != Main.focusWindow()) return;
		closeSubMenu();
		setSubMenu(menu);
		Main.openWindow(menu);
	}
	
	final void closeSubMenu() {
		if (getSubMenu() != null) {
			Main.closeWindow(getSubMenu(), false);
		}
	}
	
	final Menu getDeepestSubMenu() {
		Menu m;
		
		if (this instanceof Menu) {
			m = (Menu)this; 
		} else {
			m = getSubMenu();
			if (m == null) return null;
		}
		
		if (m.getSubMenu() == null) return m;
		do {
			m = m.getSubMenu();
		} while (m.getSubMenu() != null);
		
		return m;
	}
	
	private final void setSubMenu(Menu menu) {
		subMenu = menu;
	}
	
	final Menu getSubMenu() {
		return subMenu;
	}
	
	protected abstract Command getLeftCommand();
	protected abstract Command getMiddleCommand();
	protected abstract Command getRightCommand();
	
	public final ControlContainer getContainer() {
		return container;
	}
	
	public boolean canEnterBlackScreen() { return true; }
	
	protected void eventEnvironment(int changedFlags) {
		container.eventEnvironment(changedFlags);
	}
	
	protected void eventResize() {
		final int th = (drawingTitle ? titleHeight : 0);
		container.reposition(0, th, width, height - th, false);
	}
	public void eventMenuCommand(Menu menu, MenuItem item) { }
	public void eventCommand(Command command) { }
	protected void eventOpening() { }
	protected void eventOpened() { }
	protected void eventClosing() { }
	protected void eventClosed() { }
	protected void eventFocus(boolean focused) { }
	
	protected boolean eventKeyPress(int keyCode, int repeatCount) {
		return container.eventKeyPress(keyCode, repeatCount);
	}
	
	protected void eventKeyRelease(int keyCode) {
		container.eventKeyRelease(keyCode);
	}
	
	protected void eventPointerDown(int x, int y) {
		//convert to control coordinates
		container.eventPointerDown(x - container.getLeft(), y - container.getTop());
	}
	
	protected boolean eventPointerMove(int x, int y) {
		//convert to control coordinates
		return container.eventPointerMove(x - container.getLeft(), y - container.getTop());
	}
	
	protected void eventPointerUp(int x, int y, boolean isValid) {
		//convert to control coordinates
		container.eventPointerUp(x - container.getLeft(), y - container.getTop(), isValid);
	}
	
	protected final void drawTextAsTitle(Graphics g, String title, int screenTitleX, int screenTitleY) {
		g.setColor(baseUI.Behaviour.ColorTitleText);
		Main.FontTitle.select(g);
		g.drawString(title, screenTitleX + (width >> 1) - (Main.FontTitle.stringWidth(title) >> 1), screenTitleY + (titleHeight >> 1) - (Main.FontTitle.height >> 1), 0);
	}
	
	protected void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) { }
	
	protected void paintContents(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) { }
	
	final void paint(Graphics g, int clipX, int clipY, int clipWidth, int clipHeight) {
		int x = left, y = top;
		int r = width + x, b = height + y;
		
		if (clipX > x) x = clipX;
		if (clipY > y) y = clipY;
		if ((clipX + clipWidth) < r) r = clipX + clipWidth;
		if ((clipY + clipHeight) < b) b = clipY + clipHeight;
		r -= x; //r and b are now width and height
		b -= y;
		
		if (r > 0 && b > 0) {
			g.setClip(x, y, r, b);
			if (y < (top + titleHeight) && (b + y) > top && drawingTitle) {
				Main.Customizer.paintTitle(g, left, top, getWidth(), titleHeight);
				paintTitleText(g, left, top);
			}
			paintContents(g, left, top, x, y, r, b);
			
			//if (container.getControlCount() > 0) {
				//instead of using clipXXX, use x, y, r and b, which
				//are already clipped to the bounds of the parent window
				int x2 = left + container.getLeft(), y2 = top + container.getTop();
				int r2 = container.getWidth() + x2, b2 = container.getHeight() + y2;
				
				if (x > x2) x2 = x;
				if (y > y2) y2 = y;
				if ((x + r) < r2) r2 = x + r;
				if ((y + b) < b2) b2 = y + b;
				r2 -= x2; //r2 and b2 are now width and height
				b2 -= y2;
				
				if (r2 > 0 && b2 > 0) {
					g.setClip(x2, y2, r2, b2);
					container.paint(g, left + container.getLeft(), top + container.getTop(), x2, y2, r2, b2);
				}
			//}
			
			g.setClip(clipX, clipY, clipWidth, clipHeight);
		}
	}
}
