//
// StaticTextBox.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/StaticTextBox.java
//

package baseControls;

import baseGraphics.Font;
import baseUI.Behaviour;
import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.Main;
import javax.microedition.lcdui.Graphics;

public final class StaticTextBox extends Control {
	private String text;
	private boolean heightAuto;
	private int recommendedHeight, lastValidWidth;
	private int[] linesEndIndex;
	private Font font;
	
	public StaticTextBox(ControlContainer container, int left, int top, int width, int height, String text, boolean heightAuto) {
		super(container, left, top, width, height, true, false, false);
		this.lastValidWidth = -1;
		this.heightAuto = heightAuto;
		setText(text);
	}
	
	private final boolean computeRecommendedHeight(int width) {
		if (width == lastValidWidth) {
			return false;
		}
		
		final Font f = getFont();
		linesEndIndex = f.countLinesAndEndIndexes(text, width);
		recommendedHeight = linesEndIndex.length * f.height;
		
		lastValidWidth = width;
		
		return true;
	}
	
	public final int getRecommendedHeight() {
		return recommendedHeight;
	}
	
	public final boolean isHeightAuto() {
		return heightAuto;
	}
	
	public final void setHeightAuto(boolean heightAuto) {
		this.heightAuto = heightAuto;
		if (heightAuto) {
			super.resize(getWidth(), recommendedHeight, false);
		}
	}
	
	public final String getText() {
		return this.text;
	}
	
	public final void setText(String text) {
		this.text = ((text == null) ? "" : text);
		lastValidWidth = -1;
		computeRecommendedHeight(getWidth());
		if (heightAuto) {
			super.resize(getWidth(), recommendedHeight, false);
		}
	}
	
	public final Font getFont() {
		return ((this.font == null) ? Main.FontUI : this.font);
	}
	
	public final void setFont(Font font) {
		this.font = font;
		lastValidWidth = -1;
		computeRecommendedHeight(getWidth());
		if (heightAuto) {
			super.resize(getWidth(), recommendedHeight, false);
		}
	}
	
	public final void resize(int width, int height, boolean repaint) {
		computeRecommendedHeight(width);
		if (heightAuto) {
			height = recommendedHeight;
		}
		super.resize(width, height, repaint);
	}
	
	protected final void eventPointerDown(int x, int y) {
		//just prevent the control from doing anything
	}
	
	protected final boolean eventPointerMove(int x, int y) {
		return false;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		//just prevent the control from doing anything
	}
	
	protected final void eventEnvironment(int changedFlags) {
		if ((changedFlags & Behaviour.ENV_FONTSIZE) != 0) {
			if (computeRecommendedHeight(getWidth()) && heightAuto) {
				super.resize(getWidth(), recommendedHeight, false);
			}
		}
	}
	
	protected final void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		g.setColor(Behaviour.ColorWindowText);
		final Font f = getFont();
		final int fl = ((clipY - screenOffsetY) / f.height);
		f.drawString(text, g, screenOffsetX, screenOffsetY, linesEndIndex, clipHeight + f.height, (fl < 0) ? 0 : fl, true);
	}
}
