//
// WindowKeyConfig.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/commonDialogs/WindowKeyConfig.java
//

package commonDialogs;

import baseUI.Command;
import baseUI.Main;
import baseUI.Menu;
import baseUI.MenuItem;
import baseUI.OverlayListener;
import baseUI.Window;
import javax.microedition.lcdui.Graphics;

public final class WindowKeyConfig extends Window implements OverlayListener {
	private final Command commandMenu, commandSave;
	private final int[] keys;
	private final String[] keyNames;
	private final WindowKeyConfigListener listener;
	private final boolean allowEmptyKeys;
	private int keyConfigMode;
	
	public WindowKeyConfig(int[] keys, String[] keyNames, WindowKeyConfigListener listener, boolean allowEmptyKeys) {
		super(Main.Customizer.getTitleHeight());
		keyConfigMode = 0;
		
		this.keys = new int[keys.length];
		System.arraycopy(keys, 0, this.keys, 0, keys.length);
		this.keyNames = new String[keyNames.length];
		System.arraycopy(keyNames, 0, this.keyNames, 0, keyNames.length);
		this.listener = listener;
		this.allowEmptyKeys = allowEmptyKeys;
		
		commandMenu = Main.commandMenu();
		commandSave = Main.commandSave();
	}
	
	public final void eventOverlay(int alertId, int replyCode) {
		if (alertId == 1) {
			close();
		}
	}
	
	protected final Command getLeftCommand() {
		return commandMenu;
	}
	
	protected final Command getMiddleCommand() {
		return null;
	}
	
	protected final Command getRightCommand() {
		return commandSave;
	}
	
	private final void moveNext() {
		if (keyConfigMode < (keyNames.length - 1)) {
			keyConfigMode++;
			invalidate();
		}
	}
	
	private final void movePrevious() {
		if (keyConfigMode > 0) {
			keyConfigMode--;
			invalidate();
		}
	}
	
	private final void attribKey(int keyCode) {
		keys[keyConfigMode] = keyCode;
		
		if (keyConfigMode >= (keyNames.length - 1)) {
			invalidate();
			eventCommand(commandSave);
		} else {
			moveNext();
		}
	}
	
	public final void eventMenuCommand(Menu menu, MenuItem item) {
		if (menu.getId() == 1) {
			switch (item.getId()) {
			case 1: //Anterior
				movePrevious();
				break;
			case 2: //Próxima
				moveNext();
				break;
			case 3: //Sem tecla
				attribKey(0);
				break;
			case 4: //Cancelar
				close();
				break;
			}
		}
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandSave)) {
			for (int i = 0; i < keyNames.length; i++) {
				if (!allowEmptyKeys && keys[i] == 0) {
					Main.alertShow("Há teclas inválidas!", true);
					return;
				}
				for (int j = (i + 1); j < keyNames.length; j++) {
					if (keys[i] == keys[j] && keys[i] != 0) {
						Main.alertShow("Há teclas repetidas!", true);
						return;
					}
				}
			}
			
			if (listener.eventKeyConfigCompleted(keys)) {
				Main.alertShow("Teclas configuradas com sucesso!", true, 1, this);
			}
		} else if (command.equals(commandMenu)) {
			showMenu(1, allowEmptyKeys ? new MenuItem[] {
				new MenuItem("Anterior", 1),
				new MenuItem("Próxima", 2),
				null,
				new MenuItem("Sem tecla", 3),
				null,
				new MenuItem("Cancelar", 4)
			} : new MenuItem[] {
				new MenuItem("Anterior", 1),
				new MenuItem("Próxima", 2),
				null,
				new MenuItem("Cancelar", 4)
			});
		}
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Main.KeySoftL ||
			keyCode == Main.KeySoftR ||
			keyCode == Main.KeyOK) {
			//invalid keys!
			return true;
		}
		
		if (repeatCount == 0) {
			attribKey(keyCode);
		}
		
		return true;
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		drawTextAsTitle(g, "Configurar Teclas", screenTitleX, screenTitleY);
	}
	
	protected final void paintContents(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		final int bodyY = screenOffsetY + getTitleHeight();
		
		g.setColor(baseUI.Behaviour.ColorWindow);
		g.fillRect(clipX, Math.max(clipY, bodyY), clipWidth, clipHeight);
		g.setColor(baseUI.Behaviour.ColorWindowText);
		
		final StringBuffer sb = new StringBuffer("Pressione uma tecla para: ");
		sb.append(keyNames[keyConfigMode]);
		
		int lineH = Main.FontUI.drawString(sb.toString(), g, screenOffsetX, bodyY, getWidth(), getHeight() - getTitleHeight(), 0) * Main.FontUI.height;
		
		sb.delete(0, sb.length());
		
		sb.append("Atualmente: ");
		final int kc = keys[keyConfigMode];
		sb.append((kc == 0) ? "[Sem tecla]" : Main.getKeyDescription(kc));
		Main.FontUI.drawString(sb.toString(), g, screenOffsetX, bodyY + lineH, getWidth(), getHeight() - getTitleHeight() - lineH, 0);
	}
}
