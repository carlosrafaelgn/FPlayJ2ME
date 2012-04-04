//
// Color.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseGraphics/Color.java
//

package baseGraphics;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public final class Color {
	private Color() { }
	
	public static int blendColor(int color1, int color2, int color1Opacity) {
		return
			(((
					(((color1 >> 24) & 0xFF) * color1Opacity)
					+
					(((color2 >> 24) & 0xFF) * (255 - color1Opacity))
				) / 255) << 24) |
			(((
					(((color1 >> 16) & 0xFF) * color1Opacity)
					+
					(((color2 >> 16) & 0xFF) * (255 - color1Opacity))
				) / 255) << 16) |
			(((
					(((color1 >> 8 ) & 0xFF) * color1Opacity)
					+
					(((color2 >> 8 ) & 0xFF) * (255 - color1Opacity))
				) / 255) << 8 ) |
			((
					(((color1      ) & 0xFF) * color1Opacity)
					+
					(((color2      ) & 0xFF) * (255 - color1Opacity))
				) / 255);
	}
	
	public static int offsetColor(int color, int offset) {
		int r = ((color >> 16) & 0xFF) + offset;
		int g = ((color >> 8 ) & 0xFF) + offset;
		int b = ((color      ) & 0xFF) + offset;
		if (offset < 0) {
			if (r < 0) r = 0;
			if (g < 0) g = 0;
			if (b < 0) b = 0;
		} else {
			if (r > 255) r = 255;
			if (g > 255) g = 255;
			if (b > 255) b = 255;
		}
		return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
	}
	
	public static void fillGradient(Graphics g, int color1, int color2, int x, int y, int width, int height, boolean vertical) {
		final int size = (vertical ? height : width);
		
		int a1 = (color1 >> 1) & 0x7F800000;
		int r1 = (color1 << 7) & 0x7F800000;
		int g1 = (color1 << 15) & 0x7F800000;
		int b1 = (color1 << 23) & 0x7F800000;
		
		final int ma = (((color2 >> 1) & 0x7F800000) - a1) / size;
		final int mr = (((color2 << 7) & 0x7F800000) - r1) / size;
		final int mg = (((color2 << 15) & 0x7F800000) - g1) / size;
		final int mb = (((color2 << 23) & 0x7F800000) - b1) / size;
		
		final int[] buf = new int[size];
		
		final int tot = size - 1;
		buf[tot] = color2;
		
		int i;
		
		for (i = 0; i < tot; i++) {
			buf[i] = (a1 << 1) | ((r1 >> 7) & 0xFF0000) | ((g1 >> 15) & 0xFF00) | ((b1 >> 23) & 0xFF);
			
			a1 += ma;
			r1 += mr;
			g1 += mg;
			b1 += mb;
		}
		
		try {
			final Image strip;
			
			if (vertical) {
				strip = Image.createRGBImage(buf, 1, size, false);
				for (i = 0; i < width; ++i) {
					g.drawImage(strip, x + i, y, 0);
				}
			} else {
				strip = Image.createRGBImage(buf, size, 1, false);
				for (i = 0; i < height; ++i) {
					g.drawImage(strip, x, y + i, 0);
				}
			}
		} catch (Exception ex) {
		}
	}
	
	public static float[] rgbToHSL(int color)
	{
		final float rf = (float)((color >> 16) & 0xFF) / 255.0f;
		final float gf = (float)((color >> 8 ) & 0xFF) / 255.0f;
		final float bf = (float)((color      ) & 0xFF) / 255.0f;
		
		final float[] hsl = { 0, 0, 0 };
		
		float v = (rf > gf ? rf : gf);
		if (bf > v) v = bf;
		float m = (rf < gf ? rf : gf);
		if (bf < m) m = bf;
		
		final float l = (m + v) * .5f;
		if (l <= 0)
			return hsl;
		hsl[2] = l;
		
		final float vm = v - m;
		if (vm <= 0)
			return hsl;
		hsl[1] = vm / ((l <= 0.5f) ? (v + m) : (2.0f - v - m));
		
		if (rf == v)
			hsl[0] = (gf == m ? 5.0f + ((v - bf) / vm) : 1.0f - ((v - gf) / vm)) / 6.0f;
		else if (gf == v)
			hsl[0] = (bf == m ? 1.0f + ((v - rf) / vm) : 3.0f - ((v - bf) / vm)) / 6.0f;
		else
			hsl[0] = (rf == m ? 3.0f + ((v - gf) / vm) : 5.0f - ((v - rf) / vm)) / 6.0f;
		
		return hsl;
	}
	
	public static int hslToRGB(float h, float s, float l) {
		h *= 6.0f;
		final float c = (1.0f - Math.abs((2.0f * l) - 1.0f)) * s;
		final float m = l - (0.5f * c);
		final int x = (int)(((c * (1.0f - Math.abs((h % 2) - 1.0f))) + m) * 255.0f);
		
		int r, g, b;
		
		g = (int)((c + m) * 255.0f);
		b = (int)(m * 255.0f);
		
		switch ((int)h) {
		case 1:
			r = x;
			break;
		case 2:
			r = b;
			b = x;
			break;
		case 3:
			r = b;
			b = g;
			g = x;
			break;
		case 4:
			r = x;
			final int tmp = b;
			b = g;
			g = tmp;
			break;
		case 5:
			r = g;
			g = b;
			b = x;
			break;
		default: //0 or 6
			r = g;
			g = x;
			break;
		}
		
		if (r > 255) r = 255;
		if (g > 255) g = 255;
		if (b > 255) b = 255;
		
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}
	
	public static int bwForRGB(int color) {
		return (color & 0xFF000000) | (getBrightness(color) < 120 ? 0xFFFFFF : 0);
	}
	
	public static int rgbToGray(int color) {
		final int l = getBrightness(color);
		return (color & 0xFF000000) | (l << 16) | (l << 8) | l;
	}
	
	public static int getBrightness(int color) {
		//0.299 * 1024 = 306.176
		//0.587 * 1024 = 601.088
		//0.114 * 1024 = 116.736
		return	((306 * ((color >> 16) & 0xFF)) +
				(601 * ((color >> 8 ) & 0xFF)) +
				(117 * ((color      ) & 0xFF))) >> 10;
	}
	
	//http://www.w3.org/TR/2007/WD-WCAG20-TECHS-20070517/Overview.html#G18
	public static double getRelativeLuminance(int color) {
		final double RsRGB = ((color >> 16) & 0xFF) / 255.0;
		final double GsRGB = ((color >> 8) & 0xFF) / 255.0;
		final double BsRGB = (color & 0xFF) / 255.0;
		final double R, G, B;
		double t;
		if (RsRGB <= 0.03928) R = RsRGB / 12.92; else R = (t = ((RsRGB + 0.055) / 1.055)) * t * Math.sqrt(t);
		if (GsRGB <= 0.03928) G = GsRGB / 12.92; else G = (t = ((GsRGB + 0.055) / 1.055)) * t * Math.sqrt(t);
		if (BsRGB <= 0.03928) B = BsRGB / 12.92; else B = (t = ((BsRGB + 0.055) / 1.055)) * t * Math.sqrt(t);
		return (0.2126 * R) + (0.7152 * G) + (0.0722 * B);
	}
	
	//http://www.w3.org/TR/2007/WD-WCAG20-TECHS-20070517/Overview.html#G18
	public static double getContrastRatio(int color1, int color2) {
		return getContrastRatio(getRelativeLuminance(color1), getRelativeLuminance(color2));
	}
	
	//http://www.w3.org/TR/2007/WD-WCAG20-TECHS-20070517/Overview.html#G18
	public static double getContrastRatio(double luminance1, double luminance2) {
		return (luminance1 >= luminance2) ? ((luminance1 + 0.05) / (luminance2 + 0.05)) : ((luminance2 + 0.05) / (luminance1 + 0.05));
		//Check that the contrast ratio is equal to or greater than 5:1
	}
}
