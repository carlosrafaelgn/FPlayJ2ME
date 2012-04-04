//
// Main.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/Main.java
//

package baseUI;

import baseControls.Button;
import baseControls.Spacer;
import baseGraphics.Font;
import baseGraphics.Point;
import baseUtil.Map;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.midlet.MIDlet;

public final class Main extends Canvas implements CommandListener, Container, MessageListener {
	public static Font FontTitle;
	public static Font FontUI;
	
	private static ControlContainer MenuContainer;
	private static Button MenuLBtn, MenuRBtn;
	private static Command MenuMiddleCommand;
	private static Spacer MenuSpacer;
	
	public static int ScreenHeight, ScreenWidth, ScreenProcessingWidth, ScreenProcessingHeight;
	static int MaximizedWidth, MaximizedHeight, MaximizedX, MaximizedY, MaximizedRight, MaximizedBottom;
	public static int KeyLeft, KeyUp, KeyRight, KeyDown, KeyOK, KeySoftL, KeySoftR, KeyPgDn, KeyPgUp;
	
	public static final int PLATFORM_UNKNOWN = 0;
	public static final int PLATFORM_NOKIA = 1;
	public static final int PLATFORM_SONYERICSSON = 2;
	public static final int PLATFORM_SAMSUNG = 3;
	public static final int PLATFORM_SIEMENS = 4;
	public static final int PLATFORM_MOTOROLA = 5;
	
	private static final int MSG_OPENWINDOW = 0x0001;
	private static final int MSG_CLOSEWINDOW = 0x0002;
	private static final int MSG_KEYLOOPING = 0x0003;
	private static final int MSG_KEYPROCESS = 0x0004;
	private static final int MSG_OPENALERT = 0x0005;
	private static final int MSG_CLOSEALERT = 0x0006;
	private static final int MSG_SHOWCANVAS = 0x0007;
	private static final int MSG_SHOWFORM = 0x0008;
	private static final int MSG_SHOWFORMITEM = 0x0009;
	
	//system wide messages
	public static final int SYSMSG_TIMER = -0x0001;
	public static final int SYSMSG_CONTROLPROCESSLONGPRESS = -0x0002;
	public static final int SYSMSG_CONTROLSTARTLONGPRESSLOOPING = -0x0003;
	
	private static final int CFG_KEYPGDN = -0x0001;
	private static final int CFG_KEYPGUP = -0x0002;
	private static final int CFG_ENVTOUCHLEVEL = -0x0003;
	private static final int CFG_ENVRIGHTHANDED = -0x0004;
	private static final int CFG_ENVMENUABOVE = -0x0005;
	private static final int CFG_ENVUIEFFECTS = -0x0006;
	private static final int CFG_ENVFONTUIINDEX = -0x0007;
	private static final int CFG_ENVTRANSITION = -0x0008;
	private static final int CFG_SSTIMEOUT = -0x0009;
	
	private static int EnvPlatform;
	private static int EnvMenuX, EnvMenuY, EnvUIEffects, EnvFontUIIndex, EnvTouchFeedbackLevel, EnvTransition;
	private static boolean EnvHasPointer, EnvMenuAbove, EnvMenuVertical, EnvRightHanded;
	
	private static int ThreadProcessing;
	private static Window FocusWindow, AppWindow;
	
	private static Main Main;
	static Window MainWindow;
	public static Behaviour Customizer;
	private static MIDlet Parent;
	private static Display ParentDisplay;
	private static Transition Trans;
	private static OverlayAlert InfoAlert;
	
	private Main(MIDlet parent, Behaviour customizer) {
		EnvHasPointer = super.hasPointerEvents();
		super.hasPointerMotionEvents();
		super.hasRepeatEvents();
		
		Main = this;
		Parent = parent;
		ParentDisplay = Display.getDisplay(parent);
		Customizer = customizer;
		ScreenProcessingWidth = Font.getSmall().stringWidth("Processando...") + 4;
		ScreenProcessingHeight = Font.getSmall().height + 4;
		
		if (EnvHasPointer) TouchFeedback.initialize(ParentDisplay);
		
		MainWindow = new WindowNull();
		FocusWindow = MainWindow;
		AppWindow = MainWindow;
		
		FontTitle = Font.getLarge();
		
		initializeDefaultKeys();
		
		configLoad();
		
		MenuContainer = new ControlContainer(this, 0, 0, 16, 16, false);
		MenuLBtn = new Button(MenuContainer, 0, 0, 16, null, null, this, false);
		MenuLBtn.setTextAlignment(Graphics.LEFT);
		MenuRBtn = new Button(MenuContainer, 0, 0, 16, null, null, this, false);
		MenuRBtn.setTextAlignment(Graphics.RIGHT);
		MenuSpacer = new Spacer(MenuContainer, 0, 0, 16, 16, false);
		MenuContainer.addControl(MenuLBtn, false);
		MenuContainer.addControl(MenuSpacer, false);
		MenuContainer.addControl(MenuRBtn, false);
		
		SSCounter = System.currentTimeMillis();
		
		environmentRecalculateDimensions();
		
		//everything has changed!
		environmentDispatchChange(-1);
		
		MainWindow = Customizer.createMainWindow();
		FocusWindow = MainWindow;
		AppWindow = MainWindow;
	}
	
	private final void initializeDefaultKeysDetection(int from, int to) {
		for (int i = from; i <= to; i++) {
			try {
				final int a = getGameAction(i);
				if (a != 0) {
					if (a == Canvas.LEFT) {
						if (KeyLeft == 0) KeyLeft = i;
					} else if (a == Canvas.UP) {
						if (KeyUp == 0) KeyUp = i;
					} else if (a == Canvas.RIGHT) {
						if (KeyRight == 0) KeyRight = i;
					} else if (a == Canvas.DOWN) {
						if (KeyDown == 0) KeyDown = i;
					} else if (a == Canvas.FIRE) {
						if (KeyOK == 0) KeyOK = i;
					}
				}
			} catch (Exception ex) {
			}
		}
	}
	
	private final void initializeDefaultKeys() {
		//load the default values (don't use getKeyCode(Canvas.LEFT)...
		//because on some phones, they are mapped to the numeric pad,
		//even if the phone has arrow keys!
		KeyLeft = -3;
		KeyUp = -1;
		KeyRight = -4;
		KeyDown = -2;
		KeyOK = -5;
		KeySoftL = -6;
		KeySoftR = -7;
	
		//the first three ones use the same default key codes!
		
		//technique taken from
		//http://knol.google.com/k/j2me-keycodes#
		//http://www.iteye.com/topic/179073
		//http://www.developer.nokia.com/Community/Wiki/Platform_independent_key_events_processing_in_Java_ME

		//detecting NOKIA or SonyEricsson
		try {
			final String currentPlatform = System.getProperty("microedition.platform");
			if (currentPlatform.toUpperCase().indexOf("NOKIA") != -1) {
				EnvPlatform = PLATFORM_NOKIA;
				return;
			} else if (currentPlatform.toUpperCase().indexOf("SONYERICSSON") != -1) {
				EnvPlatform = PLATFORM_SONYERICSSON;
				return;
			}
		} catch (Throwable ex) {
		}
		//detecting SAMSUNG
		try {
			Class.forName("com.samsung.util.Vibration");
			EnvPlatform = PLATFORM_SAMSUNG;
			return;
		} catch (Throwable ex) {
		}
		
		//the rest... weird guys... :(
		boolean found = false;
		final String soft = "SOFT";
		final String left = "LEFT";
		final String right = "RIGHT";
		
		//detecting SIEMENS
		try {
			Class.forName("com.siemens.mp.io.File");
			found = true;
		} catch (Throwable ex) {
		}
		if (found) {
			KeyLeft = -61;
			KeyUp = -59;
			KeyRight = -62;
			KeyDown = -60;
			KeyOK = -26;
			KeySoftL = -1;
			KeySoftR = -4;
			EnvPlatform = PLATFORM_SIEMENS;
			return;
		}
		
		//detecting MOTOROLA
		try {
			Class.forName("com.motorola.multimedia.Vibrator");
			found = true;
		} catch (Throwable ex) {
			try {
				Class.forName("com.motorola.graphics.j3d.Effect3D");
				found = true;
			} catch (Throwable ex2) {
				try {
					Class.forName("com.motorola.multimedia.Lighting");
					found = true;
				} catch (Throwable ex3) {
					try {
						Class.forName("com.motorola.multimedia.FunLight");
						found = true;
					} catch (Throwable ex4) {
						try {
							if (getKeyName(-21).toUpperCase().indexOf(soft) > -1) {
								found = true;
							}
						} catch (Throwable ex5) {
							try {
								if (getKeyName(21).toUpperCase().indexOf(soft) > -1) {
									found = true;
								}
							} catch (Throwable ex6) {
								try {
									if (getKeyName(-20).toUpperCase().indexOf(soft) > -1) {
										found = true;
									}
								} catch (Throwable ex7) {
								}
							}
						}
					}
				}
			}
		}
		if (found) {
			KeyLeft = 0;
			KeyUp = 0;
			KeyRight = 0;
			KeyDown = 0;
			KeyOK = 0;
			initializeDefaultKeysDetection(-6, -1);
			initializeDefaultKeysDetection(-20, -7);
			initializeDefaultKeysDetection(1, 20);
			if (KeyLeft == 0) KeyLeft = -2;
			if (KeyUp == 0)KeyUp = -1;
			if (KeyRight == 0)KeyRight = -5;
			if (KeyDown == 0)KeyDown = -6;
			if (KeyOK == 0)KeyOK = -20;
			
			String softkeyMoto = "";
			String softkeyMoto1 = "";
			String softkeyMoto2 = "";
			try {
				softkeyMoto = getKeyName(-21).toUpperCase();
			} catch (Throwable ex) {
			}
			try {
				softkeyMoto1 = getKeyName(21).toUpperCase();
			} catch (Throwable ex) {
			}
			try {
				softkeyMoto2 = getKeyName(-20).toUpperCase();
			} catch (Throwable ex) {
			}
			if (softkeyMoto.indexOf(soft) >= 0 && softkeyMoto.indexOf("1") >= 0) {
				KeySoftL = -21;
			} else if (softkeyMoto1.indexOf(soft) >= 0 && softkeyMoto1.indexOf("1") >= 0) {
				KeySoftL = 21;
			} else if (softkeyMoto2.indexOf(soft) >= 0 && softkeyMoto2.indexOf("1") >= 0) {
				KeySoftL = -20;
			} else if (softkeyMoto.indexOf(soft) >= 0 && softkeyMoto.indexOf(left) >= 0) {
				KeySoftL = -21;
			} else if (softkeyMoto1.indexOf(soft) >= 0 && softkeyMoto1.indexOf(left) >= 0) {
				KeySoftL = 21;
			} else if (softkeyMoto2.indexOf(soft) >= 0 && softkeyMoto2.indexOf(left) >= 0) {
				KeySoftL = -20;
			}
			try {
				softkeyMoto = getKeyName(-22).toUpperCase();
			} catch (Throwable ex) {
			}
			try {
				softkeyMoto1 = getKeyName(21).toUpperCase();
			} catch (Throwable ex) {
			}
			try {
				softkeyMoto2 = getKeyName(22).toUpperCase();
			} catch (Throwable ex) {
			}
			if (softkeyMoto.indexOf(soft) >= 0 && softkeyMoto.indexOf("2") >= 0) {
				KeySoftR = -22;
			} else if (softkeyMoto1.indexOf(soft) >= 0 && softkeyMoto1.indexOf("2") >= 0) {
				KeySoftR = -22;
			} else if (softkeyMoto2.indexOf(soft) >= 0 && softkeyMoto2.indexOf("2") >= 0) {
				KeySoftR = 22;
			} else if (softkeyMoto.indexOf(soft) >= 0 && softkeyMoto.indexOf(right) >= 0) {
				KeySoftR = -21;
			} else if (softkeyMoto1.indexOf(soft) >= 0 && softkeyMoto1.indexOf(right) >= 0) {
				KeySoftR = 22;
			} else if (softkeyMoto2.indexOf(soft) >= 0 && softkeyMoto2.indexOf(right) >= 0) {
				KeySoftR = -22;
			}
			EnvPlatform = PLATFORM_MOTOROLA;
			return;
		}
		
		//go with the default values...
		EnvPlatform = PLATFORM_UNKNOWN;
	}
	
	public static void createShow(MIDlet parent, Behaviour customizer) {
		if (Main == null) {
			new Main(parent, customizer);
			ParentDisplay.setCurrent(Main);
			Main.setFullScreenMode(true);
			openWindow(MainWindow);
			System.gc();
		} else {
			showCanvas();
		}
	}
	
	public static void terminate() {
		if (Trans != null) {
			Trans.stop();
			Trans = null;
		}
		
		//before proceeding, just check if there are no other threads running
		Window w = FocusWindow;
		while (w != null) {
			final Window cw = w;
			w = cw.previousWindow;
			cw.closing();
			cw.closed();
		}
		
		Customizer.terminate();
		
		Parent.notifyDestroyed();
	}
	
	private static void configLoad() {
		//load the configuration
		final Map map = Map.fromFile("cfg");
		
		KeyPgDn = map.getShort(CFG_KEYPGDN, '8');
		KeyPgUp = map.getShort(CFG_KEYPGUP, '5');
		EnvTouchFeedbackLevel = map.getUByte(CFG_ENVTOUCHLEVEL, (EnvHasPointer ? 4 : 0));
		EnvRightHanded = map.getBoolean(CFG_ENVRIGHTHANDED, true);
		EnvMenuAbove = map.getBoolean(CFG_ENVMENUABOVE, false);
		EnvUIEffects = map.getUByte(CFG_ENVUIEFFECTS, 2);
		EnvFontUIIndex = map.getUByte(CFG_ENVFONTUIINDEX, (EnvHasPointer ? 2 : 0));
		EnvTransition = map.getInt(CFG_ENVTRANSITION, 0);
		int ss = map.getUShort(CFG_SSTIMEOUT, 0x7FFF);
		
		if (EnvTouchFeedbackLevel > 4) EnvTouchFeedbackLevel = 4;
		if (!EnvHasPointer) EnvTouchFeedbackLevel = 0;
		if (EnvUIEffects > 2) EnvUIEffects = 2;
		if (EnvFontUIIndex > 2) EnvFontUIIndex = 2;
		FontUI = Font.getBySizeIndex(EnvFontUIIndex);
		if (ss > 300 || ss <= 1) {
			SSThreshold = Long.MAX_VALUE; //disabled
		} else {
			SSThreshold = (long)ss * 1000;
		}
		
		Customizer.loadConfig(map);
	}
	
	public static void configSave() {
		Map map = new Map(64);
		
		map.putInt(CFG_KEYPGDN, KeyPgDn);
		map.putInt(CFG_KEYPGUP, KeyPgUp);
		map.putInt(CFG_ENVTOUCHLEVEL, EnvTouchFeedbackLevel);
		map.putBoolean(CFG_ENVRIGHTHANDED, EnvRightHanded);
		map.putBoolean(CFG_ENVMENUABOVE, EnvMenuAbove);
		map.putInt(CFG_ENVUIEFFECTS, EnvUIEffects);
		map.putInt(CFG_ENVFONTUIINDEX, EnvFontUIIndex);
		map.putInt(CFG_ENVTRANSITION, EnvTransition);
		map.putInt(CFG_SSTIMEOUT, (SSThreshold > 300000 || SSThreshold <= 1000) ? 0x7FFF : (int)(SSThreshold / 1000));
		
		Customizer.saveConfig(map);
		
		map.toFile("cfg");
		
		System.gc();
	}
	
	public static void invalidateAll() {
		if (!SSActive)
			Main.repaint();
	}
	
	public static void invalidateArea(int x, int y, int width, int height) {
		if (!SSActive)
			Main.repaint(x, y, width, height);
	}
	
	public final void invalidateChild(int x, int y, int width, int height) {
		if (!SSActive)
			repaint(x, y, width, height);
	}
	
	public final void containerPointToScreen(Point pt) {
		//main canvas does not add any offsets
	}
	
	public final void screenPointToContainer(Point pt) {
		//main canvas does not subtract any offsets
	}
	
	public static String getKeyDescription(int keyCode) {
		return Main.getKeyName(keyCode);
	}
	
	public static void showErrorAlert(String text, Displayable nextDisplayable) {
		inputCancel();
		ParentDisplay.setCurrent(new Alert("Oops...", text, null, AlertType.ERROR), nextDisplayable);
		System.gc();
	}
	
	private static void internalShowCanvas() {
		ParentDisplay.setCurrent(Main);
		Main.setFullScreenMode(true);
		SSProcessInput(); //to hide the screen saver if it was visible
		commandBarRefresh();
		invalidateAll();
		System.gc();
	}
	
	public static void showCanvas() {
		postMessage(Main, MSG_SHOWCANVAS);
	}
	
	private static void internalShowForm(Displayable form) {
		inputCancel();
		closeMenus();
		ParentDisplay.setCurrent(form);
		System.gc();
	}
	
	public static void showForm(Displayable form) {
		postMessage(Main, MSG_SHOWFORM, 0, form);
	}
	
	private static void internalShowFormItem(Item item) {
		inputCancel();
		closeMenus();
		ParentDisplay.setCurrentItem(item);
		System.gc();
	}
	
	public static void showFormItem(Item item) {
		postMessage(Main, MSG_SHOWFORMITEM, 0, item);
	}
	
	public static boolean isMainWindowActive() {
		return ((!SSActive) && (AppWindow == MainWindow));
	}
	
	public static boolean makePhoneCall(String number) {
		try {
			return !Parent.platformRequest("tel:" + number);
		} catch (Exception ex) {
			return false;
		}
	}
	
	public static boolean switchToPhoneCallMode() {
		if (!(AppWindow instanceof WindowPhoneCall)) {
			openWindow(new WindowPhoneCall());
			return true;
		} else {
			return false;
		}
	}
	
	public static void postMessage(MessageListener target, int message) {
		//do not check for the current thread (in order to run the target
		//without invoking callSerially)... it would spend too much stack space...
		ParentDisplay.callSerially(new MainMessageListenerAdapter(target, message, 0, null));
	}
	
	public static void postMessage(MessageListener target, int message, int iParam, Object oParam) {
		//do not check for the current thread (in order to run the target
		//without invoking callSerially)... it would spend too much stack space...
		ParentDisplay.callSerially(new MainMessageListenerAdapter(target, message, iParam, oParam));
	}
	
	private static void environmentPrepareForChanges() {
		inputCancel();
		closeMenus();
	}
	
	private static void environmentDispatchChange(int changedFlags) {
		Customizer.environmentChanged(changedFlags);
		MenuContainer.eventEnvironment(changedFlags);
		Window w = FocusWindow;
		while (w != null) {
			w.eventEnvironment(changedFlags);
			w = w.previousWindow;
		}
	}
	
	public static int environmentGetPlatform() {
		return EnvPlatform;
	}
	
	public static int environmentBaseMenuX() {
		return EnvMenuX;
	}
	
	public static int environmentBaseMenuY() {
		return EnvMenuY;
	}
	
	public static int environmentGetTouchFeedbackLevel() {
		return EnvTouchFeedbackLevel;
	}
	
	public static void environmentSetTouchFeedbackLevel(int level) {
		if (level >= 0 && level <= 4) {
			EnvTouchFeedbackLevel = level;
		}
	}
	
	public static void environmentDoTouchFeedback() {
		if (EnvTouchFeedbackLevel == 0) return;
		TouchFeedback.doTouchFeedback(EnvTouchFeedbackLevel);
	}
	
	public static int environmentGetFontSizeIndex() {
		return EnvFontUIIndex;
	}
	
	public static void environmentSetFontSizeIndex(int sizeIndex) {
		if (sizeIndex >= 0 && sizeIndex <= 2 && EnvFontUIIndex != sizeIndex) {
			environmentPrepareForChanges();
			
			EnvFontUIIndex = sizeIndex;
			FontUI = Font.getBySizeIndex(sizeIndex);
			
			environmentDispatchChange(Behaviour.ENV_FONTSIZE);
			
			Main.sizeChanged(Main.getWidth(), Main.getHeight());
			
			System.gc();
		}
	}
	
	public static int environmentGetTransition() {
		return EnvTransition;
	}
	
	public static void environmentSetTransition(int transition) {
		if (EnvTransition != transition) {
			environmentPrepareForChanges();
			
			EnvTransition = transition;
			
			environmentDispatchChange(Behaviour.ENV_TRANSITION);
			
			System.gc();
		}
	}
	
	public static boolean environmentHasPointer() {
		return EnvHasPointer;
	}
	
	public static boolean environmentIsRightHanded() {
		return EnvRightHanded;
	}
	
	public static void environmentSetRightHanded(boolean rightHanded) {
		if (EnvRightHanded != rightHanded) {
			environmentPrepareForChanges();
			
			EnvRightHanded = rightHanded;
			
			environmentDispatchChange(Behaviour.ENV_RIGHTHAND);
			
			Main.sizeChanged(Main.getWidth(), Main.getHeight());
			
			System.gc();
		}
	}
	
	public static boolean environmentIsMenuAbove() {
		return EnvMenuAbove;
	}
	
	public static void environmentSetMenuAbove(boolean isMenuAbove) {
		if (EnvMenuAbove != isMenuAbove) {
			environmentPrepareForChanges();
			
			EnvMenuAbove = isMenuAbove;
			
			environmentDispatchChange(Behaviour.ENV_MENUPOSITION);
			
			Main.sizeChanged(Main.getWidth(), Main.getHeight());
			
			System.gc();
		}
	}
	
	public static boolean environmentIsMenuVertical() {
		return EnvMenuVertical;
	}
	
	private static void environmentRecalculateDimensions() {
		ScreenWidth = Main.getWidth();
		ScreenHeight = Main.getHeight();
		
		final int offset = 0;
		final int buttonH = Customizer.getButtonHeight();
		final int containerH = buttonH + offset;
		
		if (ScreenWidth <= ScreenHeight) {
			EnvMenuVertical = false;
			
			//environmentDispatchChange(Behaviour.ENV_SIZE);
			
			MenuContainer.reposition(0, 0, ScreenWidth, containerH, false);
			if (!EnvMenuAbove) MenuContainer.move(0, ScreenHeight - MenuContainer.getHeight(), false);
			EnvMenuX = 0;
			EnvMenuY = (EnvMenuAbove ? containerH : MenuContainer.getTop());
			
			MaximizedX = 0;
			MaximizedY = (EnvMenuAbove ? containerH : 0);
			MaximizedWidth = ScreenWidth;
			MaximizedHeight = ScreenHeight - containerH;
			
			MenuLBtn.reposition(0, offset, MenuContainer.getWidth() >> 1, buttonH, false);
			MenuRBtn.reposition(MenuLBtn.getRight(), offset, MenuContainer.getWidth() - MenuLBtn.getRight(), buttonH, false);
			MenuLBtn.setTextAlignment(Graphics.LEFT);
			MenuRBtn.setTextAlignment(Graphics.RIGHT);
			MenuSpacer.reposition(0, 0, 0, 0, false);
		} else {
			EnvMenuVertical = true;
			
			//environmentDispatchChange(Behaviour.ENV_SIZE);
			
			MenuContainer.reposition(0, 0, Math.max(4 + FontUI.stringWidth("Selecionar"), 64) + offset, ScreenHeight, false);
			if (!EnvMenuAbove) MenuContainer.move(ScreenWidth - MenuContainer.getWidth(), 0, false);
			EnvMenuX = (EnvMenuAbove ? MenuContainer.getWidth() : MenuContainer.getLeft());
			EnvMenuY = ScreenHeight;
			
			MaximizedX = (EnvMenuAbove ? MenuContainer.getWidth() : 0);
			MaximizedY = 0;
			MaximizedWidth = ScreenWidth - MenuContainer.getWidth();
			MaximizedHeight = ScreenHeight;
			
			MenuLBtn.reposition(offset, MenuContainer.getHeight() - buttonH, MenuContainer.getWidth() - offset, buttonH, false);
			MenuRBtn.reposition(offset, 0, MenuLBtn.getWidth(), buttonH, false);
			MenuLBtn.setTextAlignment(Graphics.HCENTER);
			MenuRBtn.setTextAlignment(Graphics.HCENTER);
			MenuSpacer.reposition(offset, MenuRBtn.getBottom(), MenuRBtn.getWidth(), MenuContainer.getHeight() - MenuLBtn.getHeight() - MenuRBtn.getHeight(), false);
		}
		
		MenuContainer.processLayout();
		
		MaximizedRight = MaximizedX + MaximizedWidth;
		MaximizedBottom = MaximizedY + MaximizedHeight;
	}
	
	public static int environmentGetUIEffects() {
		return EnvUIEffects;
	}
	
	public static void environmentSetUIEffects(int uiEffects) {
		if (EnvUIEffects != uiEffects) {
			environmentPrepareForChanges();
			
			EnvUIEffects = uiEffects;
			environmentDispatchChange(Behaviour.ENV_COLORS);
			
			invalidateAll();
			
			System.gc();
		}
	}
	
	public static void environmentChangeColors() {
		environmentPrepareForChanges();
		
		environmentDispatchChange(Behaviour.ENV_COLORS);
		
		invalidateAll();
		
		System.gc();
	}
	
	public static void alertResize() {
		if (InfoAlert != null) {
			InfoAlert.maximizeWindow();
		}
	}
	
	public static OverlayAlert alertShow(String msg, boolean bigFont) {
		return alertShow(msg, bigFont, OverlayAlert.TYPE_OK, 0, null);
	}
	
	public static OverlayAlert alertShow(String msg, boolean bigFont, int id, OverlayListener listener) {
		return alertShow(msg, bigFont, OverlayAlert.TYPE_OK, id, listener);
	}
	
	public static OverlayAlert alertShowOkCancel(String msg, boolean bigFont, int id, OverlayListener listener) {
		return alertShow(msg, bigFont, OverlayAlert.TYPE_OKCANCEL, id, listener);
	}
	
	private static void internalAlertShowClose(OverlayAlert alert, boolean show) {
		if (show) {
			if (InfoAlert != null) InfoAlert.next = alert;
			alert.previous = InfoAlert;
			
			InfoAlert = alert;
		} else {
			if (alert.previous != null) alert.previous.next = alert.next;
			if (alert.next != null) alert.next.previous = alert.previous;
			
			if (InfoAlert == alert) InfoAlert = alert.previous;
			
			alert.closed();
			
			if (InfoAlert != null) InfoAlert.maximizeWindow();
		}
		
		commandBarRefresh();
		
		invalidateAll();
		
		System.gc();
	}
	
	private static OverlayAlert alertShow(String msg, boolean bigFont, int type, int id, OverlayListener listener) {
		final OverlayAlert alert = new OverlayAlert(msg, bigFont ? Font.getLarge() : Font.getSmall(), type, id, listener);
		
		postMessage(Main, MSG_OPENALERT, 0, alert);
		
		return alert;
	}
	
	static void alertClose(OverlayAlert alert) {
		postMessage(Main, MSG_CLOSEALERT, 0, alert);
	}
	
	protected final void sizeChanged(int w, int h) {
		super.sizeChanged(w, h);
		
		environmentPrepareForChanges();
		
		environmentRecalculateDimensions();
		
		Window window = FocusWindow;
		
		boolean mainOk = false;
		while (window != null) {
			if (!(window instanceof Menu)) {
				window.maximizeWindow();
				if (window.equals(MainWindow)) mainOk = true;
			}
			window = window.previousWindow;
		}
		
		if (MainWindow != null && !mainOk) {
			MainWindow.maximizeWindow();
		}
		
		alertResize();
		
		invalidateAll();
		
		System.gc();
	}
	
	protected final void showNotify() {
		super.showNotify();
		inputCancel();
		closeMenus();
	}
	
	protected final void hideNotify() {
		super.hideNotify();
		inputCancel();
		closeMenus();
	}
	
	public static boolean isAlphaSupported() {
		return (ParentDisplay.numAlphaLevels() > 2);
	}
	
	private static boolean changeWindow(Window nextWindow, boolean closing, boolean allowTransition) {
		if (nextWindow instanceof Menu || FocusWindow instanceof Menu) {
			Trans = null;
			
			FocusWindow = nextWindow;
			commandBarRefresh();
			
			return true;
		}
		
		Trans = ((EnvTransition != 0 && AppWindow != nextWindow && nextWindow != null && allowTransition) ?
				Customizer.getTransition(closing) : null);
		
		if (Trans != null) {
			if (!Trans.init1(FocusWindow)) {
				Trans.stop();
				Trans = null;
			}
			AppWindow = nextWindow;
			FocusWindow = nextWindow;
			commandBarRefresh();
			if (Trans != null && !Trans.init2(FocusWindow)) {
				Trans.stop();
				Trans = null;
			}
		} else {
			AppWindow = nextWindow;
			FocusWindow = nextWindow;
			commandBarRefresh();
		}
		
		return (Trans == null);
	}
	
	private static void internalCloseWindow(Window window, boolean allowTransition) {
		inputCancel();
		
		if (window.isOpen()) {
			if (window != FocusWindow) {
				//if not closing the current focus window,
				//close all the windows (which are supposed to be in the front),
				//until the given window becomes the FocusWindow
				do {
					internalCloseWindow(FocusWindow, false);
				} while (FocusWindow != null && FocusWindow != window);
			}
			
			final int x = window.getLeft(), y = window.getTop(), w = window.getWidth(), h = window.getHeight();
			
			window.closing();
			
			if (changeWindow(window.previousWindow, true, allowTransition)) {
				if (!(window instanceof Menu)) {
					//see the explanation in openWindow_
					window.previousWindow.focus(true);
				}
			}
			
			window.closed();
			
			invalidateArea(x, y, w, h);
		}
		
		System.gc();
	}
	
	static void closeWindow(Window window, boolean allowTransition) {
		if (!window.isCloseScheduled()) {
			window.setCloseScheduled();
			postMessage(Main, MSG_CLOSEWINDOW, allowTransition ? 1 : 0, window);
		}
	}
	
	private static void internalOpenWindow(Window window, boolean allowTransition) {
		inputCancel();
		
		if (!window.isOpen()) {
			if (!(window instanceof Menu)) {
				if (AppWindow.getSubMenu() != null) {
					//when opening a window, other than a menu, close
					//any open submenus
					//there is no need to worry about opening a menu
					//that is not a child of the current submenu,
					//because Window.showMenu closes the current menu
					//before showing a new one
					internalCloseWindow(AppWindow.getSubMenu(), false);
				}
				
				//don't tell the current window that it's losing
				//the focus due to visual artifacts (the entire view
				//would have to be repainted to make sure everything
				//appears correctly)
				FocusWindow.focus(false);
			}
			
			window.opening();
			
			if (changeWindow(window, false, allowTransition)) {
				window.opened();
				window.invalidate();
			}
		}
		
		System.gc();
	}
	
	public static void openWindow(Window window) {
		if (!window.isOpenScheduled()) {
			window.setOpenScheduled();
			postMessage(Main, MSG_OPENWINDOW, 1, window);
		}
	}
	
	public static void openWindow(Window window, boolean allowTransition) {
		if (!window.isOpenScheduled()) {
			window.setOpenScheduled();
			postMessage(Main, MSG_OPENWINDOW, allowTransition ? 1 : 0, window);
		}
	}
	
	public static Window focusWindow() {
		return FocusWindow;
	}
	
	public static Window appWindow() {
		return AppWindow;
	}
	
	public static Window getWindowAt(int x, int y) {
		Window w = FocusWindow;
		while (w != null) {
			if (w.containsPoint(x, y)) break;
			w = w.previousWindow;
		}
		return w;
	}
	
	public static void closeMenus() {
		if (AppWindow != null) AppWindow.closeSubMenu();
	}
	
	public final void eventMessage(int message, int iParam, Object oParam) {
		switch (message) {
		case MSG_OPENWINDOW:
			internalOpenWindow((Window)oParam, iParam != 0);
			break;
		case MSG_CLOSEWINDOW:
			internalCloseWindow((Window)oParam, iParam != 0);
			break;
		case MSG_KEYLOOPING:
			keyPressedLooping_();
			break;
		case MSG_KEYPROCESS:
			if (((int[])oParam)[0] == KeyVersion) Main.keyPressedProcess(((int[])oParam)[1], iParam);
			break;
		case MSG_OPENALERT:
			internalAlertShowClose((OverlayAlert)oParam, true);
			break;
		case MSG_CLOSEALERT:
			internalAlertShowClose((OverlayAlert)oParam, false);
			break;
		case MSG_SHOWCANVAS:
			internalShowCanvas();
			break;
		case MSG_SHOWFORM:
			internalShowForm((Displayable)oParam);
			break;
		case MSG_SHOWFORMITEM:
			internalShowFormItem((Item)oParam);
			break;
		}
	}
	
	public static boolean isThreadProcessing() {
		return (ThreadProcessing != 0);
	}
	
	public static synchronized void setThreadProcessing(boolean threadProcessing) {
		if (threadProcessing) {
			ThreadProcessing++;
		} else if (ThreadProcessing > 0) {
			ThreadProcessing--;
		}
		invalidateArea(0, 0, ScreenProcessingWidth, ScreenProcessingHeight);
	}
	
	public static void threadProcessingAlert() {
		alertShow("Ainda processando...", true);
	}
	
	public static Command commandBack() {
		return new Command("Voltar", -1);
	}
	
	public static Command commandCancel() {
		return new Command("Cancelar", -2);
	}
	
	public static Command commandUpDir() {
		return new Command("Acima", -3);
	}
	
	public static Command commandOK() {
		return new Command("OK", -4);
	}
	
	public static Command commandMenu() {
		return new Command("Menu", -5);
	}
	
	public static Command commandMenuSelect() {
		return new Command("Selecionar", -6);
	}
	
	public static Command commandMenuClose() {
		return new Command("Cancelar", -7);
	}
	
	public static Command commandExit() {
		return new Command("Sair", -8);
	}
	
	public static Command commandSave() {
		return new Command("Salvar", -9);
	}
	
	public static void commandBarRefresh() {
		if (InfoAlert != null) {
			MenuLBtn.setCommand(InfoAlert.getLeftCommand());
			MenuMiddleCommand = InfoAlert.getMiddleCommand();
			MenuRBtn.setCommand(InfoAlert.getRightCommand());
		} else if (FocusWindow != null) {
			MenuLBtn.setCommand(FocusWindow.getLeftCommand());
			MenuMiddleCommand = FocusWindow.getMiddleCommand();
			MenuRBtn.setCommand(FocusWindow.getRightCommand());
		}
		
		final String ll2 = MenuLBtn.getCommandLabel();
		final String lr2 = MenuRBtn.getCommandLabel();
		
		if (!MenuLBtn.getText().equals(ll2) ||
			!MenuRBtn.getText().equals(lr2)) {
			MenuLBtn.setText(ll2);
			MenuRBtn.setText(lr2);
			MenuContainer.invalidate();
		}
	}
	
	public final void eventCommand(Command command) {
		if (Trans != null) return;
		if (InfoAlert != null) {
			InfoAlert.eventCommand(command);
		} else if (FocusWindow != null) {
			FocusWindow.eventCommand(command);
		}
	}
	
	private static int KeyLastPressed;
	private static int KeyVersion;
	
	private static void keyPressedLooping_() {
		final int[] k = new int[] { KeyVersion, KeyLastPressed };
		
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
			return;
		}
		
		int r = 0;
		while (k[0] == KeyVersion) {
			postMessage(Main, MSG_KEYPROCESS, ++r, k);
			if (k[0] != KeyVersion) return;
			try {
				Thread.sleep(100);
			} catch (Exception ex) {
				return;
			}
		}
	}
	
	private final void keyPressedProcess(int keyCode, int repeatCount) {
		SSProcessInput();
		if (Trans != null) return;
		
		//check all the command keys first
		if (keyCode == KeyOK) {
			if (MenuMiddleCommand != null) {
				eventCommand(MenuMiddleCommand);
				return;
			}
		} else if (keyCode == KeySoftL) {
			if (MenuLBtn.getCommand() != null) {
				eventCommand(MenuLBtn.getCommand());
				return;
			}
		} else if (keyCode == KeySoftR) {
			if (MenuRBtn.getCommand() != null) {
				eventCommand(MenuRBtn.getCommand());
				return;
			}
		}
		
		if (InfoAlert != null) {
			//handle the alert key press
			InfoAlert.eventKeyPress(keyCode, repeatCount);
			return;
		} else {
			//handle the window key press
			if (FocusWindow.eventKeyPress(keyCode, repeatCount)) return;
		}
		
		Customizer.processUnusedKey(keyCode, repeatCount);
	}
	
	private static void keyCancel() {
		++KeyVersion;
		KeyLastPressed = Integer.MIN_VALUE;
	}
	
	protected final void keyPressed(int keyCode) {
		Control.longPressProcessAbort();
		
		keyCancel();
		
		KeyLastPressed = keyCode;
		
		if (keyCode == -10 || keyCode == KeySoftL || keyCode == KeySoftR || (keyCode == KeyOK && MenuMiddleCommand != null)) {
			SSProcessInput();
		} else {
			keyPressedProcess(keyCode, 0);
			
			if (Customizer.isKeyRepeatable(keyCode)) {
				++KeyVersion;
				(new MessageThread(this, "Main Key Press")).start(MSG_KEYLOOPING);
			}
		}
	}
	
	protected final void keyReleased(int keyCode) {
		final int lastKey = KeyLastPressed;
		
		keyCancel();
		
		if (!Control.longPressProcessAbort()) return;
		
		if (keyCode == lastKey) {
			if (lastKey == -10) {
				if (!switchToPhoneCallMode()) {
					((WindowPhoneCall)AppWindow).makePhoneCall();
				}
			} else if (lastKey == KeySoftL || lastKey == KeySoftR || (lastKey == KeyOK && MenuMiddleCommand != null)) {
				keyPressedProcess(keyCode, 0);
			} else if (InfoAlert == null) {
				//handle the window key
				FocusWindow.eventKeyRelease(lastKey);
			}
		}
	}
	
	protected final void paint(Graphics g) {
		final int clipX = g.getClipX();
		final int clipY = g.getClipY();
		final int clipWidth = g.getClipWidth();
		final int clipHeight = g.getClipHeight();
		
		if (SSActive) {
			g.setColor(0);
			g.fillRect(clipX, clipY, clipWidth, clipHeight);
			return;
		}
		
		if (Trans != null) {
			g.setClip(0, 0, ScreenWidth, ScreenHeight);
			if (Trans.paintFrame(g)) {
				return;
			}
			Trans.stop();
			Trans = null;
			SSCounter = System.currentTimeMillis();
			SSInputHappened = false;
			FocusWindow.opened();
			System.gc();
		}
		
		//FocusWindow is either AppWindow or a Menu
		
		if (FocusWindow != AppWindow) {
			//FocusWindow is a Menu
			Window w = AppWindow.getDeepestSubMenu();
			//w is the topmost menu
			while (w.previousWindow != null &&
				w != AppWindow &&
				(
					(clipX < w.getLeft()) ||
					(clipY < w.getTop()) ||
					((clipX + clipWidth) > w.getRight()) ||
					((clipY + clipHeight) > w.getBottom())
				)) {
				w = w.previousWindow;
			}
			
			//w is now the lowest visible window which needs to repaint
			do {
				w.paint(g, clipX, clipY, clipWidth, clipHeight);
				w = w.getSubMenu();
			} while (w != null);
		} else {
			AppWindow.paint(g, clipX, clipY, clipWidth, clipHeight);
		}
		
		if (InfoAlert != null) {
			InfoAlert.paint(g, clipX, clipY, clipWidth, clipHeight);
		}
		
		if (!AppWindow.isFullScreen()) {
			//draw the menu
			MenuContainer.paint(g, MenuContainer.getLeft(), MenuContainer.getTop(), clipX, clipY, clipWidth, clipHeight);
		}
		
		//draw the processing alert
		if (ThreadProcessing != 0 && clipX < ScreenProcessingWidth && clipY < ScreenProcessingHeight) {
			g.setColor(Behaviour.ColorMenu);
			g.fillRect(1, 1, ScreenProcessingWidth - 2, ScreenProcessingHeight - 2);
			g.setColor(Behaviour.ColorMenuText);
			Font.getSmall().select(g);
			g.drawString("Processando...", 2, 2, 0);
			g.setColor(Behaviour.ColorSelection);
			g.drawRect(0, 0, ScreenProcessingWidth - 1, ScreenProcessingHeight - 1);
		}
	}
	
	public static void paintWindow(Graphics g, Window window) {
		window.paint(g, 0, 0, ScreenWidth, ScreenHeight);
		if (!AppWindow.isFullScreen()) {
			MenuContainer.paint(g, MenuContainer.getLeft(), MenuContainer.getTop(), 0, 0, ScreenWidth, ScreenHeight);
		}
	}
	
	private static boolean SSActive, SSInputHappened;
	private static long SSCounter;
	public static long SSThreshold;
	
	private static boolean SSProcessInput() {
		SSInputHappened = true;
		if (!SSActive) return true;
		SSActive = false;
		Main.repaint();
		return false;
	}
	
	public static void SSProcess() {
		if (SSInputHappened) {
			SSCounter = System.currentTimeMillis();
			SSInputHappened = false;
		} else if (!SSActive) {
			if ((System.currentTimeMillis() - SSCounter) > SSThreshold) {
				if (Trans == null && AppWindow.canEnterBlackScreen()) {
					SSActive = true;
					Main.repaint();
				} else {
					SSCounter = System.currentTimeMillis();
				}
			}
		}
	}
	
	private static boolean PointerIsValid, PointerExceededThresholdForTheFirstTime, PointerExceededThreshold, PointerDown, PointerMenuFocus;
	private static int PointerFirstX, PointerFirstY;
	
	static void inputCancel() {
		Control.longPressProcessAbort();
		keyCancel();
		if (PointerDown) {
			PointerExceededThresholdForTheFirstTime = !PointerExceededThreshold;
			PointerExceededThreshold = true;
			PointerIsValid = false;
			Main.pointerReleased(Integer.MIN_VALUE, Integer.MIN_VALUE);
			PointerExceededThresholdForTheFirstTime = false;
			PointerExceededThreshold = false;
			PointerIsValid = true;
		}
	}
	
	public static boolean pointerExceededThreshold() {
		return PointerExceededThreshold;
	}
	
	public static boolean pointerExceededThresholdForTheFirstTime() {
		return PointerExceededThresholdForTheFirstTime;
	}
	
	protected final void pointerPressed(int x, int y) {
		Control.longPressProcessAbort();
		
		if (!SSProcessInput() || Trans != null) return;
		
		PointerExceededThresholdForTheFirstTime = false;
		PointerExceededThreshold = false;
		PointerIsValid = true;
		PointerDown = true;
		PointerFirstX = x;
		PointerFirstY = y;
		
		if (!AppWindow.isFullScreen() && MenuContainer.pointInBounds(x, y)) {
			PointerMenuFocus = true;
			//convert to control coordinates
			MenuContainer.eventPointerDown(x - MenuContainer.getLeft(), y - MenuContainer.getTop());
			if (MenuContainer.getPointerControl() == MenuSpacer) {
				AppWindow.closeSubMenu();
			}
		} else {
			PointerMenuFocus = false;
			if (InfoAlert != null) {
				//handle the alert pointer event
			} else {
				//handle the window pointer event
				final Window w = getWindowAt(x, y); 
				if (w.getSubMenu() == null) {
					//convert to window coordinates
					w.eventPointerDown(x - w.getLeft(), y - w.getTop());
				} else {
					//to prevent move and release events
					PointerDown = false;
					w.closeSubMenu();
				}
			}
		}
	}
	
	protected final void pointerReleased(int x, int y) {
		PointerIsValid &= Control.longPressProcessAbort();
		
		if (!SSProcessInput() || Trans != null || !PointerDown) return;
		PointerDown = false;
		
		if (!PointerExceededThreshold) {
			//if we have ignored the movement, replace the
			//position with the original
			x = PointerFirstX;
			y = PointerFirstY;
		}
		
		if (PointerMenuFocus) {
			PointerMenuFocus = false;
			//convert to control coordinates
			MenuContainer.eventPointerUp(x - MenuContainer.getLeft(), y - MenuContainer.getTop(), PointerIsValid);
		} else {
			//the user released the pointer and it was not over the menu
			if (InfoAlert != null) {
				//handle the alert pointer event
			} else {
				//handle the window pointer event
				FocusWindow.eventPointerUp(x - FocusWindow.getLeft(), y - FocusWindow.getTop(), PointerIsValid);
			}
		}
		
		PointerExceededThresholdForTheFirstTime = false;
		PointerExceededThreshold = false;
		PointerIsValid = true;
	}
	
	protected final void pointerDragged(int x, int y) {
		if (!SSProcessInput() || Trans != null || !PointerDown) return;
		
		if (!PointerExceededThreshold) {
			if (Math.abs(x - PointerFirstX) > 16 ||
				Math.abs(y - PointerFirstY) > 16) {
				PointerExceededThresholdForTheFirstTime = true;
				PointerExceededThreshold = true;
			}
		} else {
			PointerExceededThresholdForTheFirstTime = false;
		}
		
		if (PointerMenuFocus) {
			//convert to control coordinates
			MenuContainer.eventPointerMove(x - MenuContainer.getLeft(), y - MenuContainer.getTop());
		} else {
			if (InfoAlert != null) {
				//handle the alert pointer event
			} else {
				//handle the window pointer event
				FocusWindow.eventPointerMove(x - FocusWindow.getLeft(), y - FocusWindow.getTop());
			}
		}
	}
}
