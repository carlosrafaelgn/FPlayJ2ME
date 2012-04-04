//
// WindowAbout.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/WindowAbout.java
//

package ui;

import baseControls.StaticTextBox;
import baseGraphics.Font;
import baseUI.Command;
import baseUI.Main;
import baseUI.MessageListener;
import baseUI.MessageThread;
import baseUI.Window;
import baseUtil.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;

import player.Player;

final class WindowAbout extends Window implements MessageListener {
	private final Command commandRefresh, commandBack;
	private final StaticTextBox txtInfo;
	private Image icon;
	
	private static final int MSG_LOADICON = 0x0001;
	
	public WindowAbout() {
		super(Math.max(48, Math.max(Main.FontTitle.height + ((Font.getSmall().height * 3) >> 1), Main.Customizer.getTitleHeight())));
		
		(new MessageThread(this)).start(MSG_LOADICON);
		
		commandRefresh = new Command("Atualizar", 1);
		commandBack = Main.commandBack();
		
		txtInfo = new StaticTextBox(getContainer(), 0, 0, getWidth() - Main.Customizer.getScrollWidth(), 0, "", true);
		txtInfo.setFont(Font.getSmall());
		
		getContainer().setPaintBackground(true);
		
		getContainer().addControl(txtInfo, true);
		
		refreshInfo();
	}

	protected final Command getLeftCommand() {
		return commandRefresh;
	}

	protected final Command getMiddleCommand() {
		return null;
	}

	protected final Command getRightCommand() {
		return commandBack;
	}
	
	public void eventMessage(int message, int iParam, Object oParam) {
		switch (message) {
		case MSG_LOADICON:
			try {
				icon = Image.createImage("/Icon.png");
				invalidateTitle();
			} catch (Throwable ex) {
			}
			break;
		}
	}
	
	protected final void eventResize() {
		txtInfo.reposition(0, 0, getWidth() - Main.Customizer.getScrollWidth(), 0, false);
		getContainer().reposition(0, getTitleHeight(), getWidth(), getHeight() - getTitleHeight(), false);
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandRefresh)) {
			refreshInfo();
		} else if (command.equals(commandBack)) {
			icon = null;
			close();
		}
	}
	
	private final void refreshInfo() {
		System.gc();
		
		Runtime rt = Runtime.getRuntime();
		final long t = rt.totalMemory(), f = rt.freeMemory(), u = t - f;
		
		StringBuffer sb = new StringBuffer(512);
		sb.append("Memória\n");
		sb.append("Total: ");
		sb.append(Long.toString(t));
		sb.append("\nEm uso: ");
		sb.append(Long.toString(u));
		sb.append(" (");
		sb.append(Long.toString((u * 100) / t));
		sb.append("%)");
		sb.append("\nLivre: ");
		sb.append(Long.toString(f));
		sb.append('\n');
		
		sb.append('\n');
		
		String s;
		s = System.getProperty("microedition.configuration");
		if (s != null) {
			sb.append("Config: ");
			sb.append(s);
			sb.append('\n');
		}
		s = System.getProperty("microedition.platform");
		if (s != null) {
			sb.append("Platform: ");
			sb.append(s);
			sb.append('\n');
		}
		s = System.getProperty("microedition.locale");
		if (s != null) {
			sb.append("Locale: ");
			sb.append(s);
			sb.append('\n');
		}
		
		s = System.getProperty("microedition.encoding");
		if (s != null) {
			sb.append("Encoding: ");
			sb.append(s);
			sb.append('\n');
		}
		
		sb.append('\n');
		
		String[] ss;
		try {
			ss = Manager.getSupportedContentTypes("capture");
			if (ss == null || ss.length == 0) {
				sb.append("Sem captura");
				sb.append('\n');
			} else {
				sb.append("Captura:");
				sb.append('\n');
				Vector.sortArrayText(ss, 0, ss.length);
				for (int i = 0; i < ss.length; i++) {
					sb.append("  ");
					sb.append(ss[i]);
					sb.append('\n');
				}
			}
		} catch (Exception ex) {
			sb.append("Erro de captura:");
			sb.append(ex.getMessage());
			sb.append('\n');
		}
		
		sb.append('\n');
		
		ss = Player.getSupportedFormats();
		if (ss == null || ss.length == 0) {
			sb.append("Sem formatos aceitos");
			sb.append('\n');
		} else {
			sb.append("Formatos aceitos:");
			sb.append('\n');
			for (int i = 0; i < ss.length; i++) {
				sb.append("  ");
				sb.append(ss[i]);
				sb.append('\n');
			}
		}
		
		txtInfo.setText(sb.toString());
		
		getContainer().processLayout();
		getContainer().invalidate();
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		int iconW = 0;
		if (icon != null) {
			iconW = icon.getWidth();
			g.drawImage(icon, screenTitleX, screenTitleY, 0);
		}
		g.setColor(baseUI.Behaviour.ColorTitleText);
		Main.FontTitle.select(g);
		g.drawString("FPlay", screenTitleX + 2 + iconW, screenTitleY, 0);
		Font.getSmall().select(g);
		g.drawString("Por Carlos Rafael", screenTitleX + getWidth() - 2 - Font.getSmall().stringWidth("Por Carlos Rafael"), screenTitleY + getTitleHeight() - ((Font.getSmall().height * 5) >> 2), 0);
	}
}
