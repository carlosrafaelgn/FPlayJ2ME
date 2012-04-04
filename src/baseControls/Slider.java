//
// Slider.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/Slider.java
//

package baseControls;

import javax.microedition.lcdui.Graphics;

import baseUI.Behaviour;
import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.ControlListener;
import baseUI.Main;

public final class Slider extends Control {
	private String label, unit;
	private int labelWidth, valueWidth, buttonWidth, buttonHeight, offset, minimum, maximum, value, displayScale, displayScalePower, granularity;
	private boolean stickingToCenter;
	private ControlListener listener;
	private String valueString;
	private final StringBuffer displayScaleAdjust;
	
	public static final int EVENT_CHANGED = 0x5000;
	public static final int EVENT_CHANGING = 0x5001;
	
	public Slider(ControlContainer container, int left, int top, int width, ControlListener listener, String label, int minimum, int maximum, int value, boolean stickingToCenter) {
		super(container, left, top, width, getDefault2RowItemHeight(), true, false, true);
		this.granularity = 1;
		this.displayScale = 1;
		this.displayScalePower = 0;
		this.displayScaleAdjust = new StringBuffer(8);
		this.stickingToCenter = stickingToCenter;
		setLimits(minimum, maximum);
		setValue(value);
		setListener(listener);
		setLabel(label);
	}
	
	public final void setGranularity(int granularity) {
		if (granularity > (maximum - minimum)) granularity = (maximum - minimum);
		if (granularity < 1) granularity = 1;
		this.granularity = granularity;
	}
	
	public final int getGranularity() {
		return granularity;
	}
	
	public final void setLimits(int minimum, int maximum) {
		if (maximum <= minimum) maximum = minimum + 1;
		this.minimum = minimum;
		this.maximum = maximum;
		setValue(value);
	}

	public final int getMinimum() {
		return minimum;
	}

	public final int getMaximum() {
		return maximum;
	}

	public final boolean isStickingToCenter() {
		return stickingToCenter;
	}

	public final void setStickingToCenter(boolean stickingToCenter) {
		this.stickingToCenter = stickingToCenter;
	}

	public final void setValue(int value) {
		if (value < minimum) this.value = minimum;
		else if (value > maximum) this.value = maximum;
		else this.value = value;
		if (displayScalePower == 0) {
			valueString = Integer.toString(this.value);
		} else {
			final int v = Math.abs(this.value);
			displayScaleAdjust.delete(0, displayScaleAdjust.length());
			displayScaleAdjust.append(v % displayScale);
			while (displayScaleAdjust.length() < displayScalePower)
				displayScaleAdjust.insert(0, '0');
			displayScaleAdjust.insert(0, '.');
			displayScaleAdjust.insert(0, v / displayScale);
			if (this.value < 0)
				displayScaleAdjust.insert(0, '-');
			displayScaleAdjust.insert(0, ' ');
			valueString = displayScaleAdjust.toString();
		}
		calculateOffsets(getWidth());
	}

	public final int getValue() {
		return value;
	}

	public final void setDisplayScalePower(int displayScalePower) {
		if (displayScalePower < 0) this.displayScalePower = 0;
		else this.displayScalePower = displayScalePower;
		this.displayScale = 1;
		for (int i = 0; i < this.displayScalePower; i++) {
			this.displayScale *= 10;
		}
		setValue(this.value);
	}

	public final int getDisplayScale() {
		return displayScale;
	}

	private final void calculateOffsets(int width) {
		buttonHeight = Main.Customizer.getItemHeight();
		buttonWidth = 18;
		offset = 2 + (((width - buttonWidth - 4) * (value - minimum)) / (maximum - minimum));
		labelWidth = ((label == null) ? 0 : Main.FontUI.stringWidth(label));
		valueWidth = ((valueString == null) ? 0 : Main.FontUI.stringWidth(valueString));
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
		calculateOffsets(getWidth());
	}
	
	public final String getUnit() {
		return unit;
	}
	
	public final void setUnit(String unit) {
		this.unit = unit;
	}
	
	public final void resize(int width, int height, boolean repaint) {
		calculateOffsets(width);
		super.resize(width, getDefault2RowItemHeight(), repaint);
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Main.KeyLeft || keyCode == Main.KeyRight) {
			final int v = getValue();
			final int g = granularity - Math.abs(v % granularity);
			setValue((keyCode == Main.KeyRight) ? (v + g) : (v - g));
			invalidate();
			if (listener != null) {
				listener.eventControl(this, EVENT_CHANGED, getValue(), null);
			}
			return true;
		}
		return false;
	}
	
	private final void changeValue(int x) {
		final int w = getWidth();
		int value;
		if (stickingToCenter && x >= ((w >> 1) - 14) && x <= ((w >> 1) + 10)) {
			value = minimum + ((maximum - minimum) >> 1);
		} else {
			value = minimum + (((x - (buttonWidth >> 1)) * (maximum - minimum)) / (w - buttonWidth - 4));
		}
		
		final int g = granularity - Math.abs(value % granularity);
		if (g != granularity) {
			if (value < 0)
				value -= g;
			else
				value += g;
		}
		if (value < minimum) value = minimum;
		else if (value > maximum) value = maximum;
		
		if (value != this.value) {
			setValue(value);
			invalidate();
			if (listener != null) {
				listener.eventControl(this, EVENT_CHANGING, getValue(), null);
			}
		}
	}
	
	protected final void eventPointerDown(int x, int y) {
		if (y >= (getHeight() - buttonHeight - 1)) {
			super.eventPointerDown(x, y);
			changeValue(x);
		}
	}
	
	protected final boolean eventPointerMove(int x, int y) {
		if (isPressed()) {
			if (y >= (getHeight() - buttonHeight - 1) && y < getHeight())
				changeValue(x);
			return true;
		}
		return false;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		final boolean pressed = isPressed();
		super.eventPointerUp(x, y, isValid);
		if (pressed && listener != null) {
			listener.eventControl(this, EVENT_CHANGED, getValue(), null);
		}
	}
	
	protected final void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		Main.FontUI.select(g);
		g.setColor(Behaviour.ColorWindowText);
		g.drawString(label, screenOffsetX + 2, screenOffsetY + 2, 0);
		g.drawString(valueString, screenOffsetX + 2 + labelWidth, screenOffsetY + 2, 0);
		if (unit != null) {
			g.drawString(unit, screenOffsetX + 2 + labelWidth + valueWidth, screenOffsetY + 2, 0);
		}
		final int y = screenOffsetY + Main.FontUI.height + 2 + 1;
		g.drawLine(screenOffsetX + 2 + (buttonWidth >> 1), y + (buttonHeight >> 1), screenOffsetX + getWidth() - 2 - (buttonWidth >> 1), y + (buttonHeight >> 1));
		Main.Customizer.paintItem(g, isPressed(), isFocused(), screenOffsetX + offset, y, buttonWidth);
	}
}
