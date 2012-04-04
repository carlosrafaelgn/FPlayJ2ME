//
// Font.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseGraphics/Font.java
//

package baseGraphics;

import baseUI.Main;
import baseUtil.Vector;
import javax.microedition.lcdui.Graphics;

public final class Font {
	private static final Font Small = new Font(javax.microedition.lcdui.Font.getFont(javax.microedition.lcdui.Font.FACE_PROPORTIONAL, javax.microedition.lcdui.Font.STYLE_PLAIN, javax.microedition.lcdui.Font.SIZE_SMALL));
	private static final Font Large = new Font(javax.microedition.lcdui.Font.getFont(javax.microedition.lcdui.Font.FACE_PROPORTIONAL, javax.microedition.lcdui.Font.STYLE_PLAIN, javax.microedition.lcdui.Font.SIZE_LARGE));
	
	public final javax.microedition.lcdui.Font font;
	public final int height, userHeight, y, userHeightNotRelaxed;
	private final int whitespaceWidth;
	
	private Font(javax.microedition.lcdui.Font font) {
		final int h = font.getHeight();
		this.font = font;
		this.height = h;
		this.whitespaceWidth = 1 + Math.max(font.charWidth(' '), font.charWidth('|') >> 1);
		//this.userHeight = (Main.environmentHasPointer() ? ((h * 7) >> 2) : (h + 2));
		//this.y = (Main.environmentHasPointer() ? ((h * 3) >> 3) : 1);
		this.userHeightNotRelaxed = (Main.environmentHasPointer() ? ((h * 7) >> 2) : (h + 2));
		this.userHeight = (Main.environmentHasPointer() ? ((h * 7) >> 2) : ((h * 3) >> 1));
		this.y = (Main.environmentHasPointer() ? ((h * 3) >> 3) : ((h * 1) >> 2));
	}
	
	public static Font getBySizeIndex(int sizeIndex) {
		return (sizeIndex == 2 ? Large : (sizeIndex == 1 ? getMedium() : Small));
	}
	
	public static Font getFixedBySizeIndex(int sizeIndex) {
		return (sizeIndex == 2 ? getFixedLarge() : (sizeIndex == 1 ? getFixedMedium() : getFixedSmall()));
	}
	
	public static Font getSmall() {
		return Small;
	}
	
	public static Font getMedium() {
		return new Font(javax.microedition.lcdui.Font.getFont(javax.microedition.lcdui.Font.FACE_PROPORTIONAL, javax.microedition.lcdui.Font.STYLE_PLAIN, javax.microedition.lcdui.Font.SIZE_MEDIUM));
	}
	
	public static Font getLarge() {
		return Large;
	}
	
	public static Font getFixedSmall() {
		return new Font(javax.microedition.lcdui.Font.getFont(javax.microedition.lcdui.Font.FACE_MONOSPACE, javax.microedition.lcdui.Font.STYLE_PLAIN, javax.microedition.lcdui.Font.SIZE_SMALL));
	}
	
	public static Font getFixedMedium() {
		return new Font(javax.microedition.lcdui.Font.getFont(javax.microedition.lcdui.Font.FACE_MONOSPACE, javax.microedition.lcdui.Font.STYLE_PLAIN, javax.microedition.lcdui.Font.SIZE_MEDIUM));
	}
	
	public static Font getFixedLarge() {
		return new Font(javax.microedition.lcdui.Font.getFont(javax.microedition.lcdui.Font.FACE_MONOSPACE, javax.microedition.lcdui.Font.STYLE_PLAIN, javax.microedition.lcdui.Font.SIZE_LARGE));
	}
	
	public final int charsWidth(char[] ch, int offset, int length) {
		return this.font.charsWidth(ch, offset, length);
	}
	
	public final int charWidth(char[] ch, int index) {
		return this.font.charsWidth(ch, index, 1);
	}
	
	public final int charWidth(char ch) {
		return this.font.charWidth(ch);
	}
	
	public final int stringWidth(String str) {
		return this.font.stringWidth(str);
	}
	
	public final int stringWidth(String str, int offset, int length) {
		return this.font.substringWidth(str, offset, length);
	}
	
	public final void select(Graphics g) {
		g.setFont(this.font);
	}

	public final int drawString(String s, Graphics g, int x, int y, int w, int h, int firstLine) {
		if (w <= 1) return 1;
		final int last = s.length() - 1;
		final int right = w + x;
		boolean lineBreakAtEnd = false;
		h += y + (firstLine * height); //h = bottom (relative to the first line)
		int l = 0, m, cx = x, pi = 0, i;
		g.setFont(font);
		for (i = 0; (i <= last) && (y < h); i++) {
			final char c_i = s.charAt(i);
			if (c_i == ' ' || c_i == '\n' || i == last) {
				if (c_i == '\n') {
					lineBreakAtEnd = true;
				} else if (i == last) {
					i++; //in order to draw the last char!
				} 
				m = font.substringWidth(s, pi, i - pi);
				if ((m + cx) <= right) {
					//draw only the word, but skip the width of (word + space)
					if (l >= firstLine) {
						g.drawSubstring(s, pi, i - pi, cx, y, 0);
					}
					cx += m + whitespaceWidth;
				} else if (m <= w) {
					//skip to the next line then draw
					cx = x;
					l++;
					if (l > firstLine) {
						y += height;
						if (y >= h) break;
					}
					if (l >= firstLine){
						//draw only the word, but skip the width of (word + space)
						g.drawSubstring(s, pi, i - pi, cx, y, 0);
					}
					cx += m + whitespaceWidth;
				} else {
					//the word is larger than the given width!
					//draw char by char up to the end of the word, breaking
					//whenever it's necessary
					int pli = pi;
					int pcx = cx;
					do {
						m = font.charWidth(s.charAt(pi));
						if ((m + cx) > right) {
							if (l >= firstLine) {
								//don't draw the char at pi, leave it for the next line
								g.drawSubstring(s, pli, pi - pli, pcx, y, 0);
							}
							//skip to the next line then draw
							cx = x;
							pcx = x;
							pli = pi;
							l++;
							if (l > firstLine) {
								y += height;
								if (y >= h) break;
							}
						}
						cx += m;
						pi++;
					} while (pi < i); //< i in order not to draw the last blank space
					
					//draw the remaining chars, but don't draw the char
					//at pi (which is i)
					g.drawSubstring(s, pli, pi - pli, pcx, y, 0);
					
					//after that, skip the blank space
					cx += whitespaceWidth;
				}
				if (lineBreakAtEnd) {
					//skip to the next line
					cx = x;
					l++;
					if (l > firstLine) {
						y += height;
						if (y >= h) break;
					}
					lineBreakAtEnd = false;
				}
				//start at the next char
				pi = i + 1;
			}
		}
		return (l + 1);
	}

	public final int drawString(String s, Graphics g, int x, int y, int[] linesEndIndex, int h, int firstLine, boolean skipYAccordinglyToFirstLine) {
		final int end = linesEndIndex.length;
		int idx;
		g.setFont(font);
		if (firstLine == 0) {
			idx = 0;
		} else {
			idx = linesEndIndex[firstLine - 1];
			if (idx < 0) idx = -idx;
			if (skipYAccordinglyToFirstLine) y += firstLine * height;
		}
		h += y; //h = bottom (relative to the first line)
		
		int l;
		for (l = firstLine; (l < end) && (y < h); l++) {
			final int leil = linesEndIndex[l];
			if (leil < 0) {
				//DON'T DRAW THE LAST CHAR
				g.drawSubstring(s, idx, -leil - idx - 1, x, y, 0);
				idx = -leil;
			} else {
				g.drawSubstring(s, idx, leil - idx, x, y, 0);
				idx = leil;
			}
			y += height;
		}
		return (l - firstLine + 1);
	}
	
	public final int countLines(String s, int w) {
		if (w <= 1) return 1;
		final int last = s.length() - 1, x = 0;
		final int right = w + x;
		boolean lineBreakAtEnd = false;
		int l = 0, m, cx = x, pi = 0, i;
		for (i = 0; i <= last; i++) {
			final char c_i = s.charAt(i);
			if (c_i == ' ' || c_i == '\n' || i == last) {
				if (c_i == '\n') {
					lineBreakAtEnd = true;
				} else if (i == last) {
					i++; //in order to draw the last char!
				}
				m = font.substringWidth(s, pi, i - pi);
				if ((m + cx) <= right) {
					//draw only the word, but skip the width of (word + space)
					cx += m + whitespaceWidth;
				} else if (m <= w) {
					//skip to the next line then draw
					cx = x;
					l++;
					//draw only the word, but skip the width of (word + space)
					cx += m + whitespaceWidth;
				} else {
					//the word is larger than the given width!
					//draw char by char up to the end of the word, breaking
					//whenever it's necessary
					do {
						m = font.charWidth(s.charAt(pi));
						if ((m + cx) > right) {
							//skip to the next line then draw
							cx = x;
							l++;
						}
						cx += m;
						pi++;
					} while (pi < i); //< i in order not to draw the last blank space
					//after that, skip the blank space
					cx += whitespaceWidth;
				}
				if (lineBreakAtEnd) {
					//skip to the next line
					cx = x;
					l++;
					lineBreakAtEnd = false;
				}
				//start at the next char
				pi = i + 1;
			}
		}
		return (l + 1);
	}
	
	public final int[] countLinesAndEndIndexes(String s, int w) {
		if (w <= 1) return new int[0];
		Vector endIndexes = new Vector(16);
		final int sw = whitespaceWidth, last = s.length() - 1, x = 0;
		final int right = w + x;
		boolean lineBreakAtEnd = false;
		int m, cx = x, pi = 0, i;
		for (i = 0; i <= last; i++) {
			final char c_i = s.charAt(i);
			if (c_i == ' ' || c_i == '\n' || i == last) {
				if (c_i == '\n') {
					lineBreakAtEnd = true;
				} else if (i == last) {
					i++; //in order to draw the last char!
				}
				m = font.substringWidth(s, pi, i - pi);
				if ((m + cx) <= right) {
					//draw only the word, but skip the width of (word + space)
					cx += m + sw;
				} else if (m <= w) {
					//skip to the next line then draw
					cx = x;
					endIndexes.addElement(new Integer(pi));
					//draw only the word, but skip the width of (word + space)
					cx += m + sw;
				} else {
					//the word is larger than the given width!
					//draw char by char up to the end of the word, breaking
					//whenever it's necessary
					do {
						m = font.charWidth(s.charAt(pi));
						if ((m + cx) > right) {
							//skip to the next line then draw
							cx = x;
							endIndexes.addElement(new Integer(pi));
						}
						cx += m;
						pi++;
					} while (pi < i); //< i in order not to draw the last blank space
					//after that, skip the blank space
					cx += sw;
				}
				if (lineBreakAtEnd) {
					//skip to the next line
					cx = x;
					//use "-" in order to indicate that the last char MUST NOT BE DRAWN!
					endIndexes.addElement(new Integer((i == last) ? i : -(i + 1)));
					lineBreakAtEnd = false;
				}
				//start at the next char
				pi = i + 1;
			}
		}
		final int[] endIndexesA = new int[endIndexes.size() + 1];
		endIndexesA[endIndexes.size()] = i - 1;
		for (i = 0; i < (endIndexesA.length - 1); i++) {
			endIndexesA[i] = ((Integer)endIndexes.elementAt(i)).intValue();
		}
		return endIndexesA;
	}
}
