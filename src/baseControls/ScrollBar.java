//
// ScrollBar.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/ScrollBar.java
//

package baseControls;

import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.ControlListener;
import baseUI.Main;

import javax.microedition.lcdui.Graphics;

public final class ScrollBar extends Control {
	private int offset, contentSize, pageSize, thumbPos, thumbSize, offsetPointerMove;
	private ControlListener listener;
	
	public static final int EVENT_CHANGED = 0x4000;
	
	public ScrollBar(ControlContainer container, int left, int top, int height) {
		super(container, left, top, Main.Customizer.getScrollWidth(), height, false, true, true);
		offset = 0;
		contentSize = 1;
		pageSize = 1;
		thumbPos = 0;
		thumbSize = height;
	}
	
	public final int getOffset() {
		return offset;
	}
	
	public final void setOffset(int offset, boolean repaint) {
		if (contentSize != pageSize) {
			if (offset < 0) this.offset = 0;
			else if (offset > (contentSize - pageSize)) this.offset = (contentSize - pageSize);
			else this.offset = offset;
			
			final int ntp = ((getHeight() - thumbSize) * this.offset) / (contentSize - pageSize);
			
			if (thumbPos != ntp) {
				thumbPos = ntp;
				if (repaint) invalidate();
			}
		} else {
			this.offset = 0;
			
			if (thumbPos != 0) {
				thumbPos = 0;
				if (repaint) invalidate();
			}
		}
	}
	
	public final int getContentSize() {
		return contentSize;
	}
	
	public final int getPageSize() {
		return pageSize;
	}
	
	public final void setup(int offset, int contentSize, int pageSize, boolean repaint) {
		if (contentSize <= 0) contentSize = 1;
		if (pageSize > contentSize) pageSize = contentSize;
		this.contentSize = contentSize;
		this.pageSize = pageSize;
		this.thumbSize = (pageSize * getHeight()) / contentSize;
		if (this.thumbSize < 4) this.thumbSize = 4;
		setOffset(offset, false);
		if (repaint) invalidate();
	}
	
	public final ControlListener getListener() {
		return listener;
	}
	
	public final void setListener(ControlListener listener) {
		this.listener = listener;
	}
	
	public final void resize(int width, int height, boolean repaint) {
		super.resize(Main.Customizer.getScrollWidth(), height, false);
		
		if (contentSize != pageSize) {
			thumbSize = (pageSize * height) / contentSize;
			if (thumbSize < 4) thumbSize = 4;
			thumbPos = ((height - thumbSize) * offset) / (contentSize - pageSize);
		} else {
			thumbSize = height;
			thumbPos = 0;
		}
		
		if (repaint) {
			invalidate();
		}
	}
	
	protected final void eventPointerDown(int x, int y) {
		super.eventPointerDown(x, y);
		
		if (y < thumbPos || y >= (thumbPos + thumbSize)) {
			//just set the offset and then start the dragging process
			offsetPointerMove = -(thumbSize >> 1);
			eventPointerMove(x, y);
		}
		offsetPointerMove = thumbPos - y;
	}
	
	protected final boolean eventPointerMove(int x, int y) {
		final int maxtp = getHeight() - thumbSize;
		int tp = y + offsetPointerMove;
		if (tp < 0) tp = 0;
		else if (tp > maxtp) tp = maxtp;
		if (thumbPos != tp) {
			thumbPos = tp;
			offset = (tp * (contentSize - pageSize)) / maxtp;
			if (listener != null) {
				listener.eventControl(this, EVENT_CHANGED, offset, null);
			}
			invalidate();
		}
		return true;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		super.eventPointerUp(x, y, isValid);
	}
	
	protected final void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		Main.Customizer.paintScroll(g, isPressed(), screenOffsetX, screenOffsetY, getHeight(), thumbPos, thumbSize);
	}
}
