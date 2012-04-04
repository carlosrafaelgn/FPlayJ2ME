//
// TextureGrid.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseGraphics/TextureGrid.java
//

package baseGraphics;

import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.m3g.Appearance;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.Image2D;
import javax.microedition.m3g.IndexBuffer;
import javax.microedition.m3g.Material;
import javax.microedition.m3g.Texture2D;
import javax.microedition.m3g.Transform;
import javax.microedition.m3g.TriangleStripArray;
import javax.microedition.m3g.VertexArray;
import javax.microedition.m3g.VertexBuffer;

import baseUI.Main;

public class TextureGrid {
	private class Cell {
		VertexBuffer mVb;
		Texture2D mTexture;
		
		Cell(int x, int y, int width, int height, int texWidth, int texHeight, Image image, VertexArray normals) {
			int w = width;
			int h = height;
			if (texWidth != texHeight) {
				if (texWidth < texHeight) {
					w *= (texHeight / texWidth);
				} else {
					h *= (texWidth / texHeight);
				}
			}

			VertexArray vertArray = new VertexArray(4, 3, 2);
			vertArray.set(0, 4, new short[] { (short)(x+width),(short)y,0,  (short)x,(short)y,0,  (short)(x+width),(short)(y-height),0,  (short)x,(short)(y-height),0 });
			
			VertexArray texArray = new VertexArray(4, 2, 2);
			texArray.set(0, 4, new short[] { (short)w,0,  0,0,  (short)w,(short)h,  0,(short)h });

			// create the VertexBuffer for our object
			mVb = new VertexBuffer();
			mVb.setPositions(vertArray, 1.0f, null); // unit scale, zero bias
			mVb.setNormals(normals);
			mVb.setTexCoords(0, texArray
				,((texWidth < texHeight) ? (1.0f / (float)texHeight) : (1.0f / (float)texWidth))
				,null);

			Image img = Image.createImage(texWidth, texHeight);
			x += (Main.ScreenWidth>>1);
			y = -(y - (Main.ScreenHeight>>1));
			w = image.getWidth();
			h = image.getHeight();
			if ((w - x) < texWidth) {
				texWidth = (w - x);
			}
			if ((h - y) < texHeight) {
				texHeight = (h - y);
			}
			Graphics g = img.getGraphics();
			g.drawRegion(image, x, y, texWidth, texHeight, 0, 0, 0, 0);
			Image2D image2D = new Image2D(Image2D.RGB, Image.createImage(img));
			mTexture = new Texture2D(image2D);
			mTexture.setFiltering(Texture2D.FILTER_NEAREST, //FILTER_LINEAR, //FILTER_NEAREST,
								 Texture2D.FILTER_NEAREST); //FILTER_LINEAR); //FILTER_NEAREST);
			mTexture.setWrapping(Texture2D.WRAP_CLAMP,
								Texture2D.WRAP_CLAMP);
			mTexture.setBlending(Texture2D.FUNC_MODULATE);
		}
	}

	private Cell[][] cells;
	private static int MaxTextureSize;
    private Appearance appearance;
    private Material material;
	private IndexBuffer indexBuffer;

	public TextureGrid() {
		if (MaxTextureSize == 0) {
			try {
				Hashtable ht = javax.microedition.m3g.Graphics3D.getProperties();
				MaxTextureSize = ((Integer)ht.get("maxTextureDimension")).intValue();
			} catch (Exception ex) {
				MaxTextureSize = -1;
			}
		}
	}

	protected static int roundPower2(int x) {
		int i = 1;
		while (i < x) {
			i <<= 1;
		}
		return i;
	}

	protected static int[] roundPower2(int maxSize, int x) {
		int i;
		if (x == maxSize) {
			return new int[] { x };
		} else if (x > maxSize) {
			i = 0;
			while (x >= maxSize) {
				i++;
				x -= maxSize;
			}
			int[] splitX;
			if (x > 0) {
				splitX = new int[i + 1];
				splitX[i] = roundPower2(x);
			} else {
				splitX = new int[i];
			}
			for (x = 0; x < i; x++) {
				splitX[x] = maxSize;
			}
			return splitX;
		} else {
			maxSize = roundPower2(x);
			i = roundPower2(x >> 1);
			if ((i << 1) >= maxSize) {
				return new int[] { maxSize };
			} else if (i >= x) {
				return new int[] { i };
			} else {
				return new int[] { i, roundPower2(x - i) };
			}
		}
	}

	public boolean init(Image image) {
		if (MaxTextureSize <= 0) return false;
		
		int imageW = image.getWidth();
		final int imageH = image.getHeight();
		final int[] w = roundPower2(MaxTextureSize, imageW);
		final int[] h = roundPower2(MaxTextureSize, imageH);
		
		VertexArray normArray = new VertexArray(4, 3, 1);
		normArray.set(0, 4, new byte[] { 0,0,127,  0,0,127,  0,0,127,  0,0,127 });
		
		indexBuffer = new TriangleStripArray(0, new int[] { 4 });
		
		material = new Material();
		material.setColor(Material.DIFFUSE, 0xFFFFFF);
		material.setColor(Material.SPECULAR, 0xFFFFFF);
		material.setShininess(100.0f);
		
		appearance = new Appearance();
		appearance.setMaterial(material);
		
		cells = new Cell[w.length][h.length];
		int cx = -(Main.ScreenWidth >> 1);
		for (int i = 0; i < w.length; i++) {
			int cy = (Main.ScreenHeight >> 1);
			final int cw = w[i];
			int ch = imageH;
			for (int j = 0; j < h.length; j++) {
				int hy = h[j];
				cells[i][j] = new Cell(cx, cy, cw < imageW ? cw : imageW, hy < ch ? hy : ch, cw, hy, image, normArray);
				cy -= hy;
				ch -= hy;
			}
			cx += cw;
			imageW -= cw;
		}
		return true;
	}

	public void render(Graphics3D g3d, Transform transform) {
		final int w = cells.length, h = cells[0].length;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				appearance.setTexture(0, cells[x][y].mTexture);
				g3d.render(cells[x][y].mVb, indexBuffer, appearance, transform);
			}
		}
	}
}
