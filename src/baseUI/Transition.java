//
// Transition.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/Transition.java
//

package baseUI;

import baseGraphics.TextureGrid;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Transition implements MessageListener {
	private static boolean Is3DChecked, Is3DAvailable;
	private MessageThread thread;
	
	private static boolean is3DAvailablePrivate() {
		try {
			if (javax.microedition.m3g.Graphics3D.getInstance() != null) {
				java.util.Hashtable ht = javax.microedition.m3g.Graphics3D.getProperties();
				if (((Integer)ht.get("maxTextureDimension")).intValue() > 0) {
					return true;
				}
			}
		} catch (Throwable ex) { }
		return false;
	}
	
	public static boolean is3DAvailable() {
		if (Is3DChecked) return Is3DAvailable;
		Is3DChecked = true;
		Is3DAvailable = false;
		try {
			if (java.lang.Class.forName("javax.microedition.m3g.Graphics3D") != null) {
				Is3DAvailable = is3DAvailablePrivate();
			}
		} catch (Throwable ex) {
		}
		return Is3DAvailable;
	}
	
	public static TextureGrid createTextureFromWindow(Window window) {
		final Image image = Image.createImage(Main.ScreenWidth, Main.ScreenHeight);
		final Graphics g = image.getGraphics();
		Main.paintWindow(g, window);
		final TextureGrid t = new TextureGrid();
		if (!t.init(image)) {
			return null;
		}
		return t;
	}
	
	/*
	protected static short[] createScreenCubeVertexs(short cz) {
		short
			cx = (short)(MainCanvas.ScreenWidth >> 1),
			cy = (short)(MainCanvas.ScreenHeight >> 1);
		short
			mcx = (short)-cx,
			mcy = (short)-cy,
			mcz = (short)-cz;
        return new short[] {
			 cx, cy, cz,  mcx, cy, cz,   cx,mcy, cz,  mcx,mcy, cz,   // front
			mcx, cy,mcz,   cx, cy,mcz,  mcx,mcy,mcz,   cx,mcy,mcz,   // back
			mcx, cy, cz,  mcx, cy,mcz,  mcx,mcy, cz,  mcx,mcy,mcz,   // left
			 cx, cy,mcz,   cx, cy, cz,   cx,mcy,mcz,   cx,mcy, cz,   // right
			 cx, cy,mcz,  mcx, cy,mcz,   cx, cy, cz,  mcx, cy, cz,   // top
			 cx,mcy, cz,  mcx,mcy, cz,   cx,mcy,mcz,  mcx,mcy,mcz }; // bottom
	}

	protected static byte[] createScreenCubeNormals() {
        return new byte[] {
			0, 0, 127,    0, 0, 127,    0, 0, 127,    0, 0, 127,
			0, 0,-127,    0, 0,-127,    0, 0,-127,    0, 0,-127,
		   -127, 0, 0,   -127, 0, 0,   -127, 0, 0,   -127, 0, 0,
			127, 0, 0,    127, 0, 0,    127, 0, 0,    127, 0, 0,
			0, 127, 0,    0, 127, 0,    0, 127, 0,    0, 127, 0,
			0,-127, 0,    0,-127, 0,    0,-127, 0,    0,-127, 0 };
	}

	protected static short[] createScreenCubeTextureCoords(int pow2Wid, int pow2Hei) {
        short w = (short)(MainCanvas.ScreenWidth);
        short h = (short)(MainCanvas.ScreenHeight);
		if (pow2Wid != pow2Hei) {
			if (pow2Wid < pow2Hei) {
				w = (short)((int)w * (pow2Hei / pow2Wid));
			} else {
				h = (short)((int)h * (pow2Wid / pow2Hei));
			}
		}
		return new short[] {
            w, 0,       0, 0,       w, h,       0, h,
            w, 0,       0, 0,       w, h,       0, h,
            w, 0,       0, 0,       w, h,       0, h,
            w, 0,       0, 0,       w, h,       0, h,
            w, 0,       0, 0,       w, h,       0, h,
            w, 0,       0, 0,       w, h,       0, h };
	}

	protected static float getScreenCubeTextureCoordsScale(int pow2Wid, int pow2Hei) {
		return ((pow2Wid < pow2Hei) ? (1.0f / (float)pow2Hei) : (1.0f / (float)pow2Wid));
	}
	*/

	public final void eventMessage(int message, int iParam, Object oParam) {
		Main.invalidateAll();
	}
	
	protected boolean start(int interval) {
		if (thread != null) return false;
		
		thread = new MessageThread(this, "Transition");
		thread.startInterval(interval);
		
		return true;
	}
	
	public void stop() {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	public abstract boolean init1(Window from);
	public abstract boolean init2(Window to);
	public abstract boolean paintFrame(Graphics g);
}
