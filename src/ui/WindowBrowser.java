//
// WindowBrowser.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/WindowBrowser.java
//

package ui;

import baseControls.ItemPainter;
import baseControls.ListBox;
import baseUI.Behaviour;
import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.Menu;
import baseUI.MenuItem;
import baseUI.MessageListener;
import baseUI.MessageThread;
import baseUI.Window;
import baseUtil.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import player.Player;
import player.Song;

final class WindowBrowser extends Window implements ControlListener, MessageListener, ItemPainter {
	private static final int MSG_LOAD = 0x0001;
	private static final int MSG_ADD = 0x0002;
	private static final int MSG_ADDFOLDER = 0x0003;
	private static final int MSG_ADDFOLDER_REC = 0x0004;
	private static final int MSG_GETCURRENTDIR = 0x0005;
	
	private static String CurrentDir = "";
	private final Command commandMenu, commandUpDir;
	private final ListBox listBox;
	private final ListBox playerListBox;
	private final WindowPlayer player;
	private MessageThread thread;
	private Image imgFolder, imgSong, imgUp;
	private String title, nextBrowserFolderSel;
	private int firstAddedIndex;
	
	public WindowBrowser(WindowPlayer player, ListBox playerListBox) {
		super(Math.max(Main.FontTitle.height << 1, Main.Customizer.getTitleHeight()));
		this.player = player;
		this.playerListBox = playerListBox;
		commandMenu = Main.commandMenu();
		commandUpDir = Main.commandUpDir();
		firstAddedIndex = -1;
		
		listBox = new ListBox(getContainer(), 0, 0, 16, 16);
		listBox.setListener(this);
		listBox.setItemPainter(this);
		getContainer().addControl(listBox, true);
		
		title = CurrentDir;
		
		setProcessingThread(new MessageThread(this, "Browser Image Load"));
		startProcessingThread(MSG_LOAD);
	}
	
	static String getCurrentFolder() {
		return CurrentDir;
	}
	
	static void setCurrentFolder(String folder) {
		CurrentDir = folder;
	}
	
	//<editor-fold defaultstate="collapsed" desc=" Window ">
	protected final Command getLeftCommand() {
		return commandMenu;
	}
	
	protected final Command getMiddleCommand() {
		return null;
	}
	
	protected final Command getRightCommand() {
		return commandUpDir;
	}
	
	private final boolean setProcessingThread(MessageThread thread) {
		if (thread != null) {
			if (this.thread != null) {
				Main.threadProcessingAlert();
				return false;
			}
			
			Main.setThreadProcessing(true);
		} else if (this.thread != null) {
			Main.setThreadProcessing(false);
		}
		
		this.thread = thread;
		
		return true;
	}
	
	private final void startProcessingThread(int message) {
		this.thread.start(message);
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		switch (eventId) {
			case ListBox.EVENT_ITEMSELECTED:
				//either open the folder, or add the file to the playlist
				if (listBox.itemCount() > 0 && listBox.selectedIndex() >= 0) {
					//1 item selected
					if (listBox.selectedIndex() == 0 && CurrentDir.length() > 0) {
						//go back 1 level
						browserGetCurrentList(true);
					} else {
						final BrowserItem bi = (BrowserItem)listBox.selectedItem();
						//either open the folder, or add the file to the playlist
						if (bi.Folder) {
							CurrentDir = bi.getItemFullPath(CurrentDir);
							browserGetCurrentList(false);
						} else {
							browserAddSong(bi.getItemFullPath(CurrentDir));
						}
					}
				}
				break;
		}
	}
	
	protected final void eventResize() {
		final int availableHeight = getHeight() - getTitleHeight();
		listBox.reposition(0, 0, getWidth(), availableHeight, false);
		getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
	}
	
	public final void eventMenuCommand(Menu menu, MenuItem item) {
		if (menu.getId() == 1) {
			switch (item.getId()) {
			case 1: //Retornar ao Player
				close();
				break;
			case 2: //Adicionar Arquivo
				eventControl(listBox, ListBox.EVENT_ITEMSELECTED, listBox.selectedIndex(), listBox.selectedItem());
				break;
			default:
				browserFolderAdd(((BrowserItem)listBox.selectedItem()).getItemFullPath(CurrentDir), item.getId() == 4);
				break;
			}
		}
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandMenu)) {
			final BrowserItem bi = (BrowserItem)listBox.selectedItem();
			if (bi != null && bi.Folder) {
				showMenu(1, new MenuItem[] {
					new MenuItem("Retornar ao Player", 1),
					null,
					new MenuItem("Adicionar Pasta", 3),
					new MenuItem("Adicionar Pasta e Sub Pastas", 4)
				});
			} else if (listBox.selectedIndex() > 0) {
				showMenu(1, new MenuItem[] {
					new MenuItem("Retornar ao Player", 1),
					null,
					new MenuItem("Adicionar Arquivo", 2),
				});
			} else {
				showMenu(1, new MenuItem[] {
					new MenuItem("Retornar ao Player", 1)
				});
			}
		} else if (command.equals(commandUpDir)) {
			browserGetCurrentList(true);
		}
	}
	
	protected final void eventClosed() {
		if (firstAddedIndex >= 0 && firstAddedIndex < playerListBox.itemCount()) {
			playerListBox.selectItem(firstAddedIndex);
		}
	}
	
	protected final void paintTitleText(Graphics g, int screenTitleX, int screenTitleY) {
		g.setColor(Behaviour.ColorTitleText);
		Main.FontTitle.drawString(title, g, screenTitleX, screenTitleY, getWidth(), getTitleHeight(), 0);
	}
	//</editor-fold>

	public final void paintItem(Graphics g, int itemIndex, Object item, int itemX, int itemY, int itemWidth, int itemHeight, int itemTextX, int itemTextY) {
		final BrowserItem bi = (BrowserItem)item;
		final Image img;
		if (bi.Folder) {
			img = imgFolder;
		} else {
			if (itemIndex == 0) {
				img = imgUp;
			} else {
				g.setColor(Behaviour.ColorHilightText);
				img = imgSong;
			}
		}
		if (img != null) {
			g.drawImage(img, itemTextX, itemY + (itemHeight >> 1) - (img.getHeight() >> 1), 0);
			itemTextX += img.getWidth();
		}
		g.drawString(item.toString(), itemTextX, itemTextY, 0);
	}

	public final void eventMessage(int message, int iParam, Object oParam) {
		switch (message) {
		case MSG_LOAD:
			final int size;
			if (Main.FontUI.userHeight >= 48) {
				size = 48;
			} else if (Main.FontUI.userHeight >= 32) {
				size = 32;
			} else if (Main.FontUI.userHeight >= 16) {
				size = 16;
			} else {
				size = 0;
			}
			try {
				imgFolder = Image.createImage("/f" + Integer.toString(size) + ".png");
			} catch (Throwable ex) {
			}
			try {
				imgSong = Image.createImage("/s" + Integer.toString(size) + ".png");
			} catch (Throwable ex) {
			}
			try {
				imgUp = Image.createImage("/u" + Integer.toString(size) + ".png");
			} catch (Throwable ex) {
			}
			browserGetCurrentList_();
			break;
		case MSG_GETCURRENTDIR:
			browserGetCurrentList_();
			break;
		case MSG_ADDFOLDER:
			//this one runs on the player window...
			browserFolderAdd_(oParam.toString(), false, true);
			player.setProcessingThread(null);
			return;
		case MSG_ADDFOLDER_REC:
			//this one runs on the player window...
			browserFolderAdd_(oParam.toString(), true, true);
			player.setProcessingThread(null);
			return;
		case MSG_ADD:
			//this one runs on the player window...
			browserAddSong_(oParam.toString());
			player.setProcessingThread(null);
			return;
		default:
			return;
		}
		
		setProcessingThread(null);
	}
	
	//<editor-fold defaultstate="collapsed" desc=" Browser ">
	private final void browserAddSong_(String song) {
		playerListBox.addItem(new Song(song));
		player.listSetChanged();
		if (firstAddedIndex < 0) {
			firstAddedIndex = playerListBox.itemCount() - 1;
		}
	}
	
	private final void browserAddSong(String song) {
		if (!player.setProcessingThread(new MessageThread(this, "Browser Add Song"))) {
			return;
		}
		
		player.startProcessingThread(MSG_ADD, 0, song);
	}
	
	private final void browserFolderAdd_(String folder, boolean recursive, boolean first) {
		Vector f = new Vector();
		browserGetFolderContents_(folder, f);
		
		if (f.size() > 0) {
			playerListBox.ensureCapacity(playerListBox.itemCount() + f.size());
			//first add the local files, and then the files in the children folders
			for (int i = 0; i < f.size(); ++i) {
				final BrowserItem bi = (BrowserItem)f.elementAt(i);
				if (!bi.Folder) {
					if (firstAddedIndex < 0) {
						firstAddedIndex = playerListBox.itemCount();
					}
					playerListBox.addItem(new Song(bi.getItemFullPath(folder)));
				}
			}
			player.listSetChanged();
			
			if (recursive) {
				for (int i = 0; i < f.size(); ++i) {
					final BrowserItem bi = (BrowserItem)f.elementAt(i);
					if (bi.Folder) {
						browserFolderAdd_(bi.getItemFullPath(folder), true, false);
					} else {
						//over!
						break;
					}
				}
			}
		}
		
		f = null;
		folder = null;
		
		System.gc();
		
		if (first) {
			player.invalidateTitle();
		}
	}
	
	private final void browserFolderAdd(String folder, boolean recursive) {
		if (!player.setProcessingThread(new MessageThread(this, "Browser Add Song"))) {
			return;
		}
		
		player.startProcessingThread(recursive ? MSG_ADDFOLDER_REC : MSG_ADDFOLDER, 0, folder);
	}
	
	private final boolean browserGetFolderContents_(String folder, Vector vector) {
		boolean ret = false;
		java.util.Enumeration e = null;
		javax.microedition.io.file.FileConnection dir = null;
		try {
			dir = (javax.microedition.io.file.FileConnection)javax.microedition.io.Connector.open("file:///" + folder, javax.microedition.io.Connector.READ);
			e = dir.list();
		} catch (Exception ex) {
			if (dir != null) {
				try { dir.close(); } catch (Exception ex2) { }
			}
			dir = null;
			Main.alertShow("Erro: " + ex.getMessage(), true);
			return false;
		}
		if (e != null) {
			ret = true;
			while (e.hasMoreElements()) {
				final String f = e.nextElement().toString();
				final boolean isFolder = (f.charAt(f.length() - 1) == '/');
				if (isFolder || Player.isFileSupported(f))
					vector.addElement(new BrowserItem(f, isFolder));
			}
			e = null;
			vector.sort();
		}
		if (dir != null) {
			try {
				dir.close();
			} catch (Exception ex) {
				Main.alertShow("Erro: " + ex.getMessage(), true);
			}
			dir = null;
		}
		return ret;
	}
	
	private final void browserGetCurrentList_() {
		if (CurrentDir.length() < 1) {
			CurrentDir = "";
			//list the root drivers
			java.util.Enumeration e = null;
			try {
				e = javax.microedition.io.file.FileSystemRegistry.listRoots();
			} catch (Exception ex) { }
			if (e != null) {
				while (e.hasMoreElements())
					listBox.addItem(new BrowserItem(e.nextElement().toString(), true));
				e = null;
			}
		} else {
			final Vector v = new Vector();
			browserGetFolderContents_(CurrentDir, v);
			//the ".." item MUST always be the first item
			v.insertElementAt(new BrowserItem(), 0);
			listBox.replaceContents(v);
		}
		
		if (nextBrowserFolderSel != null) {
			for (int i = 0; i < listBox.itemCount(); i++) {
				if (listBox.itemAt(i).toString().equalsIgnoreCase(nextBrowserFolderSel)) {
					listBox.selectItem(i);
					break;
				}
			}
			
			nextBrowserFolderSel = null;
		} else {
			listBox.selectItem(0);
		}
		
		listBox.invalidate();
	}
	
	private final void browserGetCurrentList(boolean upDir) {
		if (!setProcessingThread(new MessageThread(this, "Browser Get List"))) {
			return;
		}
		
		nextBrowserFolderSel = null;
		if (upDir) {
			int i;
			if (CurrentDir.length() >= 1) {
				i = CurrentDir.lastIndexOf('/', CurrentDir.length() - 2);
				if (i < 0) {
					nextBrowserFolderSel = ((CurrentDir.length() > 1) ? CurrentDir.substring(0, CurrentDir.length() - 1) : CurrentDir);
					CurrentDir = "";
				} else {
					nextBrowserFolderSel = CurrentDir.substring(i + 1, CurrentDir.length() - 1);
					CurrentDir = CurrentDir.substring(0, i + 1);
				}
			}
		}
		
		title = CurrentDir;
		
		listBox.clear();
		
		invalidateTitle();
		
		startProcessingThread(MSG_GETCURRENTDIR);
	}
	//</editor-fold>
}
