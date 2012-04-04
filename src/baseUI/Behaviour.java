//
// Behaviour.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/Behaviour.java
//

package baseUI;

import baseGraphics.Color;
import baseUtil.Map;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Behaviour {
	private int scrollWidth;
	private Image imageButtonNotFocused, imageButtonPressed, imageButtonFocused;
	private Image imageMenuItem;
	private Image imageSelectionNotFocused, imageSelectionPressed, imageSelectionFocused;
	private Image imageScroll, imageScrollPressed;
	private Image imageTitle;
	
	public static final int ColorCount = 11;
	public static int ColorButtonText, ColorButton, ColorHilightText, ColorMenuText, ColorMenu, ColorSelectionText, ColorSelection, ColorWindowText, ColorWindow, ColorTitleText, ColorTitle;
	static int ColorLtButton, ColorDkButton, ColorDkButton2, ColorLtSelection, ColorDkSelection, ColorLtTitle, ColorDkTitle, ColorBlendedSelection;
	
	public static final int ENV_COLORS = 0x0001;
	public static final int ENV_FONTSIZE = 0x0002;
	public static final int ENV_MENUPOSITION = 0x0004;
	public static final int ENV_TRANSITION = 0x0008;
	public static final int ENV_RIGHTHAND = 0x0010;
	
	private static final int NotFocusedOpacity = 100;
	
	private static final int CFG_COLORFIRST = -0x0101;
	
	public Behaviour() { }
	
	private final void environmentPrepareColors() {
		ColorLtButton = Color.offsetColor(ColorButton, 25);
		ColorLtSelection = Color.offsetColor(ColorSelection, 25);
		ColorLtTitle = Color.offsetColor(ColorTitle, 25);
		ColorDkButton = Color.offsetColor(ColorButton, -25);
		ColorDkSelection = Color.offsetColor(ColorSelection, -25);
		ColorDkTitle = Color.offsetColor(ColorTitle, -25);
		ColorDkButton2 = Color.offsetColor(ColorButton, -45);
		ColorBlendedSelection = Color.blendColor(ColorSelection, ColorWindow, NotFocusedOpacity);
	}
	
	private static Image environmentCreateGradientVertical(Graphics gr, Image mimg, int colorUp, int colorDown, int height) {
		Color.fillGradient(gr, colorUp, colorDown, 0, 0, 16, height, true);
		return Image.createImage(mimg);
	}
	
	private static Image environmentCreateGradientHorizontal(Graphics gr, Image mimg, int colorUp, int colorDown, int width) {
		Color.fillGradient(gr, colorUp, colorDown, 0, 0, width, 16, false);
		return Image.createImage(mimg);
	}
	
	private final void environmentCreateGradient() {
		Image mimg;
		Graphics gr;
		
		final int ih = getItemHeight() - 2;
		
		if (ih <= 0) {
			imageButtonNotFocused = null;
			imageButtonPressed = null;
			imageButtonFocused = null;
			imageMenuItem = null;
			imageSelectionNotFocused = null;
			imageSelectionPressed = null;
			imageSelectionFocused = null;
			imageTitle = null;
		} else {
			try {
				mimg = Image.createImage(16, getMenuItemHeight() - 2);
				gr = mimg.getGraphics();
				
				imageMenuItem = environmentCreateGradientVertical(gr, mimg, ColorLtSelection, ColorDkSelection, getMenuItemHeight() - 2);
				
				mimg = Image.createImage(16, ih);
				gr = mimg.getGraphics();
				
				imageButtonNotFocused = environmentCreateGradientVertical(gr, mimg, ColorLtButton, ColorDkButton, ih);
				
				if (Main.environmentHasPointer()) {
					imageButtonPressed = environmentCreateGradientVertical(gr, mimg, Color.offsetColor(ColorLtButton, -20), Color.offsetColor(ColorDkButton, -25), ih);
				}
				
				imageButtonFocused = environmentCreateGradientVertical(gr, mimg, Color.offsetColor(ColorLtButton, 50), Color.offsetColor(ColorDkButton, 35), ih);
				
				imageSelectionNotFocused = environmentCreateGradientVertical(gr, mimg, Color.blendColor(ColorLtSelection, ColorWindow, NotFocusedOpacity), Color.blendColor(ColorDkSelection, ColorWindow, NotFocusedOpacity), ih);
				
				if (ColorButton == ColorSelection) {
					imageSelectionPressed = imageButtonPressed;
					imageSelectionFocused = imageButtonNotFocused;
				} else {
					if (Main.environmentHasPointer()) {
						imageSelectionPressed = environmentCreateGradientVertical(gr, mimg, Color.offsetColor(ColorLtSelection, -20), Color.offsetColor(ColorDkSelection, -25), ih);
					}
					
					imageSelectionFocused = environmentCreateGradientVertical(gr, mimg, ColorLtSelection, ColorDkSelection, ih);
				}
				
				mimg = Image.createImage(16, Main.FontTitle.userHeight - 2);
				gr = mimg.getGraphics();
				
				imageTitle = environmentCreateGradientVertical(gr, mimg, ColorLtTitle, ColorDkTitle, Main.FontTitle.userHeight - 2);
			} catch (Exception ex) {
				imageButtonNotFocused = null;
				imageButtonPressed = null;
				imageButtonFocused = null;
				imageMenuItem = null;
				imageSelectionNotFocused = null;
				imageSelectionPressed = null;
				imageSelectionFocused = null;
				imageTitle = null;
			}
		}
		
		final int sw = scrollWidth - 2;
		
		if (sw < 10) {
			imageScroll = null;
			imageScrollPressed = null;
		} else {
			try {
				mimg = Image.createImage(sw, 16);
				gr = mimg.getGraphics();
				
				imageScroll = environmentCreateGradientHorizontal(gr, mimg, ColorLtButton, ColorDkButton, sw);
				
				if (Main.environmentHasPointer()) {
					imageScrollPressed = environmentCreateGradientHorizontal(gr, mimg, Color.offsetColor(ColorLtButton, -20), Color.offsetColor(ColorDkButton, -25), sw);
				}
			} catch (Exception ex) {
				imageScroll = null;
				imageScrollPressed = null;
			}
		}
	}
	
	private static Image environmentCreateCrystalVertical(Graphics gr, Image grImg, int colorUp1, int colorUp2, int colorDown1, int colorDown2, int height) {
		final int half = height >> 1;
		Color.fillGradient(gr, colorUp1, colorUp2, 0, 0, 16, half, true);
		Color.fillGradient(gr, colorDown1, colorDown2, 0, half, 16, height - half, true);
		return Image.createImage(grImg);
	}
	
	private static Image environmentCreateCrystalHorizontal(Graphics gr, Image grImg, int colorUp1, int colorUp2, int colorDown1, int colorDown2, int width) {
		final int half = width >> 1;
		Color.fillGradient(gr, colorUp1, colorUp2, 0, 0, half, 16, false);
		Color.fillGradient(gr, colorDown1, colorDown2, half, 0, width - half, 16, false);
		return Image.createImage(grImg);
	}
	
	private final void environmentCreateCrystal() {
		final int ltb = Color.offsetColor(ColorButton, 75);
		final int lts = Color.offsetColor(ColorSelection, 75);
		final int ltt = Color.offsetColor(ColorTitle, 75);
		
		Image mimg;
		Graphics gr;
		
		final int ih = getItemHeight() - 2;
		
		if (ih <= 0) {
			imageButtonNotFocused = null;
			imageButtonPressed = null;
			imageButtonFocused = null;
			imageMenuItem = null;
			imageSelectionNotFocused = null;
			imageSelectionPressed = null;
			imageSelectionFocused = null;
			imageTitle = null;
		} else {
			try {
				mimg = Image.createImage(16, getMenuItemHeight() - 2);
				gr = mimg.getGraphics();
				
				imageMenuItem = environmentCreateCrystalVertical(gr, mimg, lts, ColorLtSelection, ColorSelection, ColorDkSelection, getMenuItemHeight() - 2);
				
				mimg = Image.createImage(16, ih);
				gr = mimg.getGraphics();
				
				imageButtonNotFocused = environmentCreateCrystalVertical(gr, mimg, ltb, ColorLtButton, ColorButton, ColorDkButton, ih);
				
				if (Main.environmentHasPointer()) {
					imageButtonPressed = environmentCreateCrystalVertical(gr, mimg, Color.offsetColor(ltb, -35), Color.offsetColor(ColorLtButton, -35), 
						//inverted
						Color.offsetColor(ColorButton, -30), Color.offsetColor(ColorDkButton, -30), ih);
				}
				
				imageButtonFocused = environmentCreateCrystalVertical(gr, mimg, 0xFFFFFF, Color.offsetColor(ColorLtButton, 35),
					//inverted
					Color.offsetColor(ColorButton, 50), Color.offsetColor(ColorDkButton, 50), ih);
				
				imageSelectionNotFocused = environmentCreateCrystalVertical(gr, mimg, Color.blendColor(lts, ColorWindow, NotFocusedOpacity), Color.blendColor(ColorLtSelection, ColorWindow, NotFocusedOpacity),
					Color.blendColor(ColorSelection, ColorWindow, NotFocusedOpacity), Color.blendColor(ColorDkSelection, ColorWindow, NotFocusedOpacity), ih);
				
				if (ColorButton == ColorSelection) {
					imageSelectionPressed = imageButtonPressed;
					imageSelectionFocused = imageButtonNotFocused;
				} else {
					if (Main.environmentHasPointer()) {
						imageSelectionPressed = environmentCreateCrystalVertical(gr, mimg, Color.offsetColor(lts, -35), Color.offsetColor(ColorLtSelection, -35),
							//inverted
							Color.offsetColor(ColorSelection, -30), Color.offsetColor(ColorDkSelection, -30), ih);
					}
					
					imageSelectionFocused = environmentCreateCrystalVertical(gr, mimg, lts, ColorLtSelection, ColorSelection, ColorDkSelection, ih);
				}
				
				mimg = Image.createImage(16, Main.FontTitle.userHeight - 2);
				gr = mimg.getGraphics();
				
				imageTitle = environmentCreateCrystalVertical(gr, mimg, ltt, ColorLtTitle, ColorTitle, ColorDkTitle, Main.FontTitle.userHeight - 2);
			} catch (Exception ex) {
				imageButtonNotFocused = null;
				imageButtonPressed = null;
				imageButtonFocused = null;
				imageMenuItem = null;
				imageSelectionNotFocused = null;
				imageSelectionPressed = null;
				imageSelectionFocused = null;
				imageTitle = null;
			}
		}
		
		final int sw = scrollWidth - 2;
		
		if (sw < 10) {
			imageScroll = null;
			imageScrollPressed = null;
		} else {
			try {
				mimg = Image.createImage(sw, 16);
				gr = mimg.getGraphics();
				
				imageScroll = environmentCreateCrystalHorizontal(gr, mimg, ltb, ColorLtButton, ColorButton, ColorDkButton, sw);
				
				if (Main.environmentHasPointer()) {
					imageScrollPressed = environmentCreateCrystalHorizontal(gr, mimg, Color.offsetColor(ltb, -35), Color.offsetColor(ColorLtButton, -35),
						//inverted
						Color.offsetColor(ColorButton, -30), Color.offsetColor(ColorDkButton, -30), sw);
				}
			} catch (Exception ex) {
				imageScroll = null;
				imageScrollPressed = null;
			}
		}
	}
	
	public void environmentChanged(int changedFlags) {
		scrollWidth = (Main.environmentHasPointer() ? 24 : 6);
		
		if ((changedFlags & (ENV_COLORS | ENV_FONTSIZE)) != 0) {
			environmentPrepareColors();
			switch (Main.environmentGetUIEffects()) {
			case 0:
				imageButtonNotFocused = null;
				imageButtonPressed = null;
				imageButtonFocused = null;
				imageMenuItem = null;
				imageSelectionNotFocused = null;
				imageSelectionPressed = null;
				imageSelectionFocused = null;
				imageTitle = null;
				imageScroll = null;
				imageScrollPressed = null;
				break;
			case 1:
				environmentCreateGradient();
				break;
			case 2:
				environmentCreateCrystal();
				break;
			}
		}
	}
	
	public void loadConfig(Map map) {
		final int[] defColors = getDefaultColors();
		for (int i = 0; i < ColorCount; i++) {
			colorSet(i, map.getInt(CFG_COLORFIRST - i, defColors[i]));
		}
	}
	
	public void saveConfig(Map map) {
		for (int i = 0; i < ColorCount; i++) {
			map.putInt(CFG_COLORFIRST - i, colorGet(i));
		}
	}
	
	public int[] getDefaultColors() {
		return new int[] {
				0xFF8800, //ColorHilightText
				0x555555, //ColorTitle
				0xFFFFFF, //ColorTitleText
				0x000000, //ColorWindow
				0xFFFFFF, //ColorWindowText
				0x6688FF, //ColorSelection
				0x000000, //ColorSelectionText
				0xFFFFFF, //ColorMenu
				0x0000CC, //ColorMenuText
				0x3355AA, //ColorButton
				0xFFFFFF //ColorButtonText
			};
	}
	
	public final void colorLoadDefault() {
		final int[] colors = getDefaultColors();
		for (int i = 0; i < colors.length; i++) {
			colorSet(i, colors[i]);
		}
	}

	public final String colorGetName(int index) {
		if (index < ColorCount && index >= 0) {
			switch (index) {
			case 0:
				return "Destaque";
			case 1:
				return "Título (Fundo)";
			case 2:
				return "Título (Texto)";
			case 3:
				return "Janela (Fundo)";
			case 4:
				return "Janela (Texto)";
			case 5:
				return "Seleção (Fundo)";
			case 6:
				return "Seleção (Texto)";
			case 7:
				return "Menu (Fundo)";
			case 8:
				return "Menu (Texto)";
			case 9:
				return "Botão (Fundo)";
			case 10:
				return "Botão (Texto)";
			}
		}
		return null;
	}

	public final String[] colorGetName() {
		final String[] names = new String[ColorCount];
		for (int i = 0; i < ColorCount; i++)
			names[i] = colorGetName(i);
		return names;
	}

	public final void colorSet(int index, int color) {
		if (index < ColorCount && index >= 0) {
			//prevent UI colors from having transparency
			color &= 0xFFFFFF;
			switch (index) {
			case 0:
				ColorHilightText = color;
				break;
			case 1:
				ColorTitle = color;
				break;
			case 2:
				ColorTitleText = color;
				break;
			case 3:
				ColorWindow = color;
				break;
			case 4:
				ColorWindowText = color;
				break;
			case 5:
				ColorSelection = color;
				break;
			case 6:
				ColorSelectionText = color;
				break;
			case 7:
				ColorMenu = color;
				break;
			case 8:
				ColorMenuText = color;
				break;
			case 9:
				ColorButton = color;
				break;
			case 10:
				ColorButtonText = color;
				break;
			}
		}
	}

	public final int colorGet(int index) {
		if (index < ColorCount && index >= 0) {
			switch (index) {
			case 0:
				return ColorHilightText;
			case 1:
				return ColorTitle;
			case 2:
				return ColorTitleText;
			case 3:
				return ColorWindow;
			case 4:
				return ColorWindowText;
			case 5:
				return ColorSelection;
			case 6:
				return ColorSelectionText;
			case 7:
				return ColorMenu;
			case 8:
				return ColorMenuText;
			case 9:
				return ColorButton;
			case 10:
				return ColorButtonText;
			}
		}
		return -1;
	}

	public final int[] colorGet() {
		final int[] colors = new int[ColorCount];
		for (int i = 0; i < ColorCount; i++)
			colors[i] = colorGet(i);
		return colors;
	}

	public final void colorSet(int[] colors) {
		for (int i = 0; i < colors.length; i++)
			colorSet(i, colors[i]);
	}
	
	public abstract Window createMainWindow();
	
	public void terminate() { }
	
	public int[] getAvailableTransitions() {
		return new int[] { -1, -2 }; 
	}
	
	public Transition getTransition(boolean closing) {
		switch (Main.environmentGetTransition()) {
		case -1:
			return new Transition1(closing);
		case -2:
			return new Transition2(closing);
		default:
			return null;
		}
	}
	
	public String getTransitionName(int transition) {
		switch (transition) {
		case -1:
			return Transition1.NAME;
		case -2:
			return Transition2.NAME;
		default:
			return "Nenhuma";
		}
	}
	
	public void processUnusedKey(int keyCode, int repeatCount) { }
	
	public boolean isKeyRepeatable(int keyCode) {
		return (keyCode == Main.KeyUp) ||
				(keyCode == Main.KeyDown) ||
				(keyCode == Main.KeyLeft) ||
				(keyCode == Main.KeyRight) ||
				(keyCode == Main.KeyPgUp) ||
				(keyCode == Main.KeyPgDn);
	}
	
	public int getScrollWidth() {
		return scrollWidth;
	}
	
	public void drawFrame(Graphics g, boolean inverted, int ltColor, int dkColor, int x, int y, int width, int height) {
		final int ex = x + width - 1, ey = y + height - 1;
		g.setColor(inverted ? dkColor : ltColor);
		g.drawLine(x, y, ex - 1, y);
		g.drawLine(x, y, x, ey);
		g.setColor(inverted ? ltColor : dkColor);
		g.drawLine(x + 1, ey, ex, ey);
		g.drawLine(ex, y, ex, ey);
	}
	
	public void paintScroll(Graphics g, boolean pressed, int x, int y, int height, int thumbY, int thumbHeight) {
		final Image img = (pressed ? imageScrollPressed : imageScroll);
		
		g.setColor(ColorWindow);
		if (scrollWidth > 12) {
			g.fillRect(x, y, 5, height);
			g.fillRect(x + scrollWidth - 5, y, 5, height);
			
			g.fillRect(x + 5, y, scrollWidth - 10, 5);
			g.fillRect(x + 5, y + height - 5, scrollWidth - 10, 5);
			
			drawFrame(g, true, ColorLtButton, ColorDkButton, x + 5, y + 5, scrollWidth - 10, height - 10);
			g.setColor(ColorDkButton2);
			g.fillRect(x + 6, y + 6, scrollWidth - 12, height - 12);
		} else {
			g.fillRect(x, y, scrollWidth, height);
			drawFrame(g, false, ColorLtButton, ColorDkButton, x + 2, y + 4, scrollWidth - 4, height - 8);
		}
		
		y += thumbY;
		
		drawFrame(g, pressed, ColorLtButton, ColorDkButton, x, y, scrollWidth, thumbHeight);
		
		x++;
		
		if (img != null) {
			y++;
			thumbHeight -= 2; //remove the outter border
			final int ly = y + (thumbHeight & ~0x0F); //get only integer multiples
			for ( ; y < ly; y += 16) {
				g.drawImage(img, x, y, 0);
			}
			//draw the last piece of the thumb, just in case the 
			y = thumbHeight & 15;
			if (y != 0) {
				g.drawRegion(img, 0, 0, scrollWidth - 2, y, 0, x, ly, 0);
			}
		} else {
			g.setColor(pressed ? ColorDkButton : ColorButton);
			g.fillRect(x, y + 1, scrollWidth - 2, thumbHeight - 2);
		}
	}
	
	public final int getButtonHeight() {
		return getItemHeight();
	}
	
	public void paintButton(Graphics g, boolean pressed, boolean focused, int x, int y, int width) {
		final Image img = (pressed ? imageButtonPressed : (focused ? imageButtonFocused : imageButtonNotFocused));
		
		drawFrame(g, pressed, ColorLtButton, ColorDkButton, x, y, width, getButtonHeight());
		
		if (img != null) {
			final int lx = x + width - 16 - 1;
			y++;
			x++;
			for ( ; x < lx; x += 16) {
				g.drawImage(img, x, y, 0);
			}
			g.drawImage(img, lx, y, 0);
		} else {
			g.setColor(pressed ? ColorDkButton : (focused ? ColorLtButton : ColorButton));
			g.fillRect(x + 1, y + 1, width - 2, getButtonHeight() - 2);
		}
	}
	
	public int getItemHeight() {
		return Main.FontUI.userHeight;
	}
	
	public void paintItem(Graphics g, boolean pressed, boolean focused, int x, int y, int width) {
		final Image img = (pressed ? imageSelectionPressed : (focused ? imageSelectionFocused : imageSelectionNotFocused));
		final int height = getItemHeight();
		
		drawFrame(g, pressed, ColorLtSelection, ColorDkSelection, x, y, width, height);
		
		if (img != null) {
			final int lx = x + width - 16 - 1;
			y++;
			x++;
			for ( ; x < lx; x += 16) {
				g.drawImage(img, x, y, 0);
			}
			g.drawImage(img, lx, y, 0);
		} else {
			g.setColor(pressed ? ColorDkSelection : (focused ? ColorSelection : ColorBlendedSelection));
			g.fillRect(x + 1, y + 1, width - 2, height - 2);
		}
	}
	
	public int getItemTextColor(boolean focused, boolean hilighted) {
		return (hilighted ? ColorHilightText : (focused ? ColorSelectionText : ColorWindowText));
	}
	
	public void paintMenuBarSpace(Graphics g, int x, int y, int width, int height) {
		drawFrame(g, false, ColorLtButton, ColorDkButton, x, y, width, height);
		g.setColor(ColorButton);
		g.fillRect(x + 1, y + 1, width - 2, height - 2);
	}
	
	public int getMenuItemHeight() {
		return Main.FontUI.userHeightNotRelaxed;
	}
	
	public void paintMenuItem(Graphics g, boolean selected, int x, int y, int width) {
		g.setColor(ColorMenu);
		final int height = getMenuItemHeight();
		if (selected) {
			g.setColor(ColorMenu);
			g.drawLine(x, y, x, y + height - 1);
			g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
			
			x++;
			width -= 2;
			
			drawFrame(g, false, ColorLtSelection, ColorDkSelection, x, y, width, height);
			
			final Image img = imageMenuItem;
			
			if (img != null) {
				final int lx = x + width - 16 - 1;
				y++;
				x++;
				for ( ; x < lx; x += 16) {
					g.drawImage(img, x, y, 0);
				}
				g.drawImage(img, lx, y, 0);
			} else {
				g.setColor(ColorSelection);
				g.fillRect(x + 1, y + 1, width - 2, height - 2);
			}
		} else {
			g.fillRect(x, y, width, height);
		}
	}
	
	public int getTitleHeight() {
		return Main.FontTitle.userHeight;
	}
	
	public void paintTitle(Graphics g, int x, int y, int width, int height) {
		final int h = getTitleHeight();
		if (height < h) height = h;
		
		drawFrame(g, false, ColorLtTitle, ColorDkTitle, x, y, width, height);
		
		final Image img = imageTitle;
		
		if (img != null) {
			final int lx = x + width - 16 - 1;
			y++;
			x++;
			if (height == h) {
				for ( ; x < lx; x += 16) {
					g.drawImage(img, x, y, 0);
				}
				g.drawImage(img, lx, y, 0);
			} else {
				g.setColor(ColorTitle);
				final int ih = img.getHeight();
				final int hup = ih >> 1;
				final int hdown = ih - hup;
				final int yd = y + height - hdown - 1;
				g.fillRect(x, y + hup, width - 2, height - 1 - ih);
				
				for ( ; x < lx; x += 16) {
					g.drawRegion(img, 0, 0, 16, hup, 0, x, y, 0);
					g.drawRegion(img, 0, hup, 16, hdown, 0, x, yd, 0);
				}
				g.drawRegion(img, 0, 0, 16, hup, 0, lx, y, 0);
				g.drawRegion(img, 0, hup, 16, hdown, 0, lx, yd, 0);
			}
		} else {
			g.setColor(ColorTitle);
			g.fillRect(x + 1, y + 1, width - 2, height - 2);
		}
	}
}
