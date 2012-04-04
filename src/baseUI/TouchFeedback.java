//
// TouchFeedback.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/TouchFeedback.java
//

package baseUI;

import javax.microedition.lcdui.Display;

abstract class TouchFeedback {
	private static TouchFeedback touch;
	
	public abstract void doTouchFeedback_(int type);
	
	public static void initialize(final Display display) {
		try {
			final Class c1 = Class.forName("com.nokia.mid.ui.TactileFeedback");
			//final Class c2 = Class.forName("com.nokia.mid.ui.DeviceControl");
			if (c1 != null) {// && c2 != null) {
				final Object o1 = c1.newInstance();
				if (o1 != null) {
					touch = new TouchFeedback() {
						private final com.nokia.mid.ui.TactileFeedback tf;
						{ tf = (com.nokia.mid.ui.TactileFeedback)o1; }
						public void doTouchFeedback_(int type) {
							if (type == 4) {
								tf.directFeedback(1); //FEEDBACK_STYLE_BASIC
								return;
							}
							display.vibrate(0);
							switch (type) {
							case 1:
								display.vibrate(30);
								break;
							case 2:
								display.vibrate(40);
								break;
							case 3:
								display.vibrate(60);
								break;
							}
						}
					};
				}
			}
		} catch (Throwable ex) { }
		
		touch = new TouchFeedback() {
			public void doTouchFeedback_(int type) {
				display.vibrate(0);
				switch (type) {
				case 4:
				case 1:
					display.vibrate(30);
					break;
				case 2:
					display.vibrate(40);
					break;
				case 3:
					display.vibrate(60);
					break;
				}
			}
		};
	}
	
	public static void doTouchFeedback(int type) {
		touch.doTouchFeedback_(type);
	}
}
