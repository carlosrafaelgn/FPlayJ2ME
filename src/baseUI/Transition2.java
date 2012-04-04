//
// Transition2.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/Transition2.java
//

package baseUI;

import baseGraphics.TextureGrid;
import baseUI.Main;
import baseUI.Transition;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;
import javax.microedition.m3g.Background;
import javax.microedition.m3g.Camera;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.m3g.Light;
import javax.microedition.m3g.Transform;

public final class Transition2 extends Transition {
	public static final String NAME = "Giro";
	
	private Graphics3D mG3D;
    private Camera mCamera;
    private Light mLight;
    private Transform mTransform;
    private Background mBackground;
	private TextureGrid mGrid1, mGrid2;
	private float mAngle, mPosZ;
	private final boolean closing;
	
	public Transition2(boolean closing) {
		this.closing = closing;
	}
	
	public final void stop() {
		super.stop();
		mG3D = null;
		mCamera = null;
		mLight = null;
		mTransform = null;
		mBackground = null;
		mGrid1 = null;
		mGrid2 = null;
	}
	
	public final boolean init1(Window from) {
		try {
			mGrid1 = createTextureFromWindow(from);
			
			mTransform = new Transform();
			
			mG3D = Graphics3D.getInstance();
			
			mCamera = new Camera();
			mCamera.setPerspective(60.0f, // field of view
				(float)Main.ScreenWidth / (float)Main.ScreenHeight, // aspectRatio
				1.0f, // near clipping plane
				1000.0f); // far clipping plane
			//1.7320508075688772935274463415059 = Math.sqrt(3)
			float cameraZ = (float)(1.7320508075688772935274463415059 * 0.5 * (double)Math.max(Main.ScreenWidth, Main.ScreenHeight));
			
			mLight = new Light();
			mLight.setColor(0xFFFFFF);
			mLight.setIntensity(1.25f);
			
			mBackground = new Background();
			mBackground.setColor(0x202020);
			
			mTransform.setIdentity();
			mTransform.postTranslate(0.0f, 0.0f, cameraZ);
			mG3D.setCamera(mCamera, mTransform);
			
			mG3D.resetLights();
			mG3D.addLight(mLight, mTransform);
			
			mAngle = 0;
		} catch (Exception ex) {
			mGrid1 = null;
		}
		return (mGrid1 != null);
	}
	
	public final boolean init2(Window to) {
		try {
			mGrid2 = createTextureFromWindow(to);
		} catch (Exception ex) {
			mGrid2 = null;
		}
		return (mGrid2 != null) ? start(15) : false;
	}
	
	public final boolean paintFrame(Graphics g) {
		mG3D.bindTarget(g, false, 0);//Graphics3D.DITHER | Graphics3D.TRUE_COLOR);
		
		mG3D.clear(mBackground);
		
		mAngle += 9.0f;
		mPosZ -= 20.0f;
		mTransform.setIdentity();
		if (mAngle < 90.0f) {
			mTransform.postTranslate(0, 0, mPosZ);
			if (closing) {
				mTransform.postRotate(-mAngle, 1.0f, 0.0f, 0.0f);
			} else {
				mTransform.postRotate(mAngle, 1.0f, 0.0f, 0.0f);
			}
			mGrid1.render(mG3D, mTransform);
		} else {
			mTransform.postTranslate(0, 0, -200.0f - (mPosZ + 200.0f));
			if (closing) {
				mTransform.postRotate(90.0f - (mAngle - 90.0f), 1.0f, 0.0f, 0.0f);
			} else {
				mTransform.postRotate(-(90.0f - (mAngle - 90.0f)), 1.0f, 0.0f, 0.0f);
			}
			mGrid2.render(mG3D, mTransform);
		}

		mG3D.releaseTarget();
		return (mAngle < 179.0f);
	}
}
