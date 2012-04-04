//
// WindowPlayer.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/WindowPlayer.java
//

package ui;

import baseUI.Command;
import baseUI.Control;
import baseUI.ControlListener;
import baseUI.Main;
import baseUI.Menu;
import baseUI.MenuItem;
import baseUI.MessageListener;
import baseUI.MessageThread;
import baseUI.Window;
import baseUtil.Map;
import baseControls.Button;
import baseControls.ListBox;
import baseGraphics.Point;
import baseIO.File;
import commonDialogs.WindowColorConfig;
import commonDialogs.WindowKeyConfig;
import commonDialogs.WindowKeyConfigListener;
import commonDialogs.WindowFileChooser;
import commonDialogs.WindowFileChooserListener;
import javax.microedition.lcdui.Graphics;
import player.Equalizer;
import player.Player;
import player.PlayerListener;
import player.Song;
import player.SongList;

final class WindowPlayer extends Window implements MessageListener, PlayerListener, ControlListener, WindowFileChooserListener, WindowKeyConfigListener {
	private final Player player;
	private boolean hasChangedPosition, hasListChanged, isCarMode, isChangingMode;
	private int cycleIndex,
				lastIndex, //index of the last played song
				lastRadioIndex; //index of the last played song in radio mode
	private int titleY;
	private final Command commandMenu, commandOK, commandBack, commandCancel, commandExit, commandPrev, commandPause, commandNext, commandMove, commandDelete;
	private final ListBox listBox;
	private final StringBuffer titleText;
	private Button btnPrev, btnPause, btnNext;
	private final Equalizer equalizer;
	private MessageThread thread;
	
	private static final int CFG_VOLUME = 0x0101;
	private static final int CFG_LASTINDEX = 0x0102;
	private static final int CFG_LASTRADIOINDEX = 0x0103;
	private static final int CFG_ISRADIOMODE = 0x0104;
	private static final int CFG_ISRADIOSTEREO = 0x0105;
	private static final int CFG_ISCARMODE = 0x0106;
	private static final int CFG_LASTTIME = 0x0107;
	
	private static final int MSG_LISTOPEN = 0x0001;
	private static final int MSG_LISTSAVE = 0x0002;
	private static final int MSG_REFRESHTITLES = 0x0003;
	private static final int MSG_CHANGEMODE = 0x0004;
	
	public WindowPlayer(Map map) {
		super(Math.max(Main.Customizer.getTitleHeight(), Main.FontTitle.height << 1));
		setDrawingTitle(false);
		
		int volume = 10;
		int lasttime = 0;
		boolean isRadioMode = false, isRadioStereo = true;
		
		cycleIndex = -1;
		titleText = new StringBuffer();
		
		commandMenu = Main.commandMenu();
		commandOK = Main.commandOK();
		commandBack = Main.commandBack();
		commandCancel = Main.commandCancel();
		commandExit = Main.commandExit();
		commandPrev = new Command(null, 1);
		commandPause = new Command(null, 2);
		commandNext = new Command(null, 3);
		commandMove = new Command(null, 20);
		commandDelete = new Command(null, 21);
		listBox = new ListBox(getContainer(), 0, 0, 16, 16);
		listBox.setListener(this);
		listBox.setProcessingLongPress(true);
		getContainer().addControl(listBox, true);
		preparePlaybackControls();
		
		//load the configurations
		volume = map.getUByte(CFG_VOLUME, 100);
		lastIndex = map.getUShort(CFG_LASTINDEX, 0);
		lastRadioIndex = map.getUShort(CFG_LASTRADIOINDEX, 0);
		isRadioMode = map.getBoolean(CFG_ISRADIOMODE, false);
		isRadioStereo = map.getBoolean(CFG_ISRADIOSTEREO, false);
		isCarMode = map.getBoolean(CFG_ISCARMODE, false);
		lasttime = map.getInt(CFG_LASTTIME, 0);
		
		listLoad_(isRadioMode ? "listRDefault" : "listDefault");
		
		/*listBox.addItem(new Song("sons/som1.wav"));
		listBox.addItem(new Song("sons/som2.wav"));
		listBox.addItem(new Song("sons/som3.wav"));
		listBox.addItem(new Song("sons/som4.wav"));
		listBox.addItem(new Song("sons/som5.wav"));
		listBox.addItem(new Song("sons/som6.wav"));
		listBox.addItem(new Song("sons/som7.wav"));
		listBox.addItem(new Song("sons/som8.wav"));
		listBox.addItem(new Song("sons/som9.wav"));
		listBox.addItem(new Song("sons/somA.wav"));
		listBox.addItem(new Song("sons/somB.wav"));
		listBox.addItem(new Song("sons/somC.wav"));
		listBox.addItem(new Song("sons/somD.wav"));
		listBox.addItem(new Song("sons/somE.wav"));
		listBox.addItem(new Song("sons/somF.wav"));
		listBox.addItem(new Song("sons/somG.wav"));
		listBox.addItem(new Song("sons/somH.wav"));
		listBox.addItem(new Song("sons/somI.wav"));
		listBox.addItem(new Song("sons/somJ.wav"));
		listBox.addItem(new Song("sons/somK.wav"));*/
		
		final int currentIndex = isRadioMode ? lastRadioIndex : lastIndex;
		final Song currentSong;
		if (currentIndex < listBox.itemCount() && currentIndex >= 0) {
			currentSong = (Song)listBox.itemAt(currentIndex);
			listBox.selectItemAndHilight(currentIndex);
		} else {
			currentSong = null;
		}
		
		player = new Player(volume, lasttime, currentSong, this, isRadioMode, isRadioStereo);
		equalizer = Equalizer.createEqualizer(player, map);
		
		resetGlobalVolume();
	}
	
	private final void resetGlobalVolume() {
		if (Behaviour.environmentHasVolumeControl()) {
			player.setGlobalVolume(100);
		}
	}
	
	public final void terminate() {
		player.terminate();
		
		//save the default list
		listSaveDefault_();
		
		Main.configSave();
	}
	
	public final void setCarMode(boolean isCarMode) {
		this.isCarMode = isCarMode;
	}
	
	public final void saveConfig(Map map) {
		map.putInt(CFG_VOLUME, player.getVolume());
		map.putInt(CFG_LASTINDEX, player.isRadioMode() ? lastIndex : listBox.getHilightIndex());
		map.putInt(CFG_LASTRADIOINDEX, player.isRadioMode() ? listBox.getHilightIndex() : lastRadioIndex);
		map.putBoolean(CFG_ISRADIOMODE, player.isRadioMode());
		map.putBoolean(CFG_ISRADIOSTEREO, player.isRadioStereo());
		map.putBoolean(CFG_ISCARMODE, isCarMode);
		map.putInt(CFG_LASTTIME, player.getTimeTrackMS());
		equalizer.saveConfig(map);
	}
	
	public final void invalidateTitle() {
		invalidate(0, titleY, getWidth(), getTitleHeight());
	}
	
	private final void preparePlaybackControls() {
		if (!Main.environmentHasPointer()) return;
		
		if (Behaviour.environmentGetControlButtons() != 0) {
			if (btnPause == null) {
				btnPrev = new Button(getContainer(), 0, 0, 16, "<<", commandPrev, this, false);
				btnPause = new Button(getContainer(), 0, 0, 16, "||", commandPause, this, false);
				btnNext = new Button(getContainer(), 0, 0, 16, ">>", commandNext, this, false);
				getContainer().addControl(btnPrev, false);
				getContainer().addControl(btnPause, false);
				getContainer().addControl(btnNext, true);
			}
		} else {
			if (btnPause != null) {
				getContainer().removeControl(btnPrev, false);
				getContainer().removeControl(btnPause, false);
				getContainer().removeControl(btnNext, true);
				btnPrev = null;
				btnPause = null;
				btnNext = null;
			}
		}
		
		repositionPlaybackControls();
	}
	
	private final void repositionPlaybackControls() {
		if (btnPause != null) {
			final int w = getWidth() / 3;
			btnPrev.reposition(0, ((Behaviour.environmentGetControlButtons() == 2) ? 0 : (getHeight() - getTitleHeight() - btnPrev.getHeight())), w, 0, false);
			btnPause.reposition(btnPrev.getRight(), btnPrev.getTop(), getWidth() - (w << 1), 0, false);
			btnNext.reposition(btnPause.getRight(), btnPause.getTop(), w, 0, false);
		}
	}
	
	protected final Command getLeftCommand() {
		return (listBox.isMoving() ? null : commandMenu);
	}
	
	protected final Command getMiddleCommand() {
		return null;
	}
	
	protected final Command getRightCommand() {
		return (listBox.isMoving() ? commandBack : (listBox.isMarking() ? commandCancel : commandExit));
	}
	
	final boolean setProcessingThread(MessageThread thread) {
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
	
	final boolean isProcessingThreadFree() {
		if (thread == null) {
			return true;
		} else {
			Main.threadProcessingAlert();
			return false;
		}
	}
	
	final void startProcessingThread(int message) {
		this.thread.start(message);
	}
	
	final void startProcessingThread(int message, int iParam, Object oParam) {
		this.thread.start(message, iParam, oParam);
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		switch (eventId) {
			case ListBox.EVENT_LONGKEY:
			case ListBox.EVENT_LONGPOINTER:
				if (listBox.selectedIndex() < 0) break;
				final Point pt = (Point) eventArg2;
				listBox.clientPointToScreen(pt);
				showMenu(5, new MenuItem[] {
					new MenuItem("Iniciar Seleção", 1),
					new MenuItem("Mover Selecionado", 2),
					null,
					new MenuItem("Remover", 3)
				}, pt.x, pt.y);
				break;
			case ListBox.EVENT_SELECTIONCHANGED:
				hasChangedPosition = true;
				break;
			case ListBox.EVENT_ITEMSELECTED:
				if (listBox.isMarking()) {
					hasChangedPosition = true;
					eventCommand(commandMove);
				} else if (listBox.isMoving()) {
					hasChangedPosition = true;
					eventCommand(commandBack);
				} else {
					hasChangedPosition = false;
					eventCommand(commandOK);
				}
				break;
		}
	}
	
	protected final void eventEnvironment(int changedFlags) {
		if ((changedFlags & Behaviour.ENV_MENUPOSITION) != 0) {
			preparePlaybackControls();
		}
		if ((changedFlags & Behaviour.ENV_VOLUMECONTROL) != 0) {
			if (!Behaviour.environmentHasVolumeControl()) {
				player.clearVolumeControl();
				invalidateTitle();
			}
		}
		super.eventEnvironment(changedFlags);
	}
	
	protected final void eventResize() {
		final int btnH = (btnPause != null ? btnPause.getHeight() : 0);
		if (Behaviour.environmentGetControlButtons() == 2) {
			titleY = btnH;
			listBox.reposition(0, getTitleHeight() + btnH, getWidth(), getHeight() - getTitleHeight() - btnH, false);
			repositionPlaybackControls();
			getContainer().reposition(0, 0, getWidth(), getHeight(), false);
		} else {
			titleY = 0;
			final int availableHeight = getHeight() - getTitleHeight();
			listBox.reposition(0, 0, getWidth(), availableHeight - btnH, false);
			repositionPlaybackControls();
			getContainer().reposition(0, getTitleHeight(), getWidth(), availableHeight, false);
		}
	}
	
	public boolean eventKeyConfigCompleted(int[] keys) {
		Main.KeyPgUp = keys[0];
		Main.KeyPgDn = keys[1];
		Behaviour.KeyDel = keys[2];
		Behaviour.KeyPrev = keys[3];
		Behaviour.KeyNext = keys[4];
		Behaviour.KeyPause = keys[5];
		Behaviour.KeyVolDn = keys[6];
		Behaviour.KeyVolUp = keys[7];
		Main.configSave();
		return true;
	}
	
	public final void eventMessage(int message, int iParam, Object oParam) {
		switch (message) {
		case MSG_LISTOPEN:
			if (!listLoad_(((File)oParam).getFullName()))
				Main.alertShow("Erro ao abrir lista!", true);
			listSetChanged();
			break;
		case MSG_LISTSAVE:
			if (!listSave_(((File)oParam).getFullName()))
				Main.alertShow("Erro ao salvar lista!", true);
			break;
		case MSG_REFRESHTITLES:
			listRefreshSongsTitle_();
			break;
		case MSG_CHANGEMODE:
			listChangeMode_();
			break;
		default:
			return;
		}
		
		setProcessingThread(null);
	}
	
	public final void eventFileSelected(WindowFileChooser window, File file) {
		if (!setProcessingThread(new MessageThread(this, "Player List Operation"))) {
			return;
		}
		
		listSetChanged();
		
		startProcessingThread((window.getId() == 0) ? MSG_LISTOPEN : MSG_LISTSAVE, 0, file);
	}
	
	public final void eventFileSelectionChanged(WindowFileChooser window, final File file) {
	}
	
	public final void eventFileCancelled(WindowFileChooser window) {
	}
	
	public final void eventMenuCommand(Menu menu, MenuItem item) {
		switch (menu.getId()) {
		case 1: //player menu
			switch (item.getId()) {
			case 1: //Modo Carro
				Main.openWindow(new WindowPlayerCar(player, equalizer, this, commandExit, commandPrev, commandPause, commandNext));
				break;
			case 2: //Ouvir...
				listChangeMode();
				break;
			case 3: //Adicionar...
				if (player.isRadioMode()) {
					Main.showForm(new FormRadio(this, listBox));
				} else {
					Main.openWindow(new WindowBrowser(this, listBox));
				}
				break;
			}
			break;

		case 2: //list menu
			switch (item.getId()) {
			case 1: //Limpar
				listClear();
				break;
			case 2: //Abrir...
				Main.openWindow(new WindowFileChooser(0, true, player.isRadioMode() ? 'r' : 'l', false, "Abrir Lista", this));
				break;
			case 3: //Salvar...
				Main.openWindow(new WindowFileChooser(1, false, player.isRadioMode() ? 'r' : 'l', false, "Salvar Lista", this));
				break;
			}
			break;

		case 3: //options menu
			switch (item.getId()) {
			case 1: //Geral...
				Main.openWindow(new WindowOptions(player));
				break;
			case 2: //Teclas...
				Main.openWindow(new WindowKeyConfig(new int[] {
						Main.KeyPgUp,
						Main.KeyPgDn,
						Behaviour.KeyDel,
						Behaviour.KeyPrev,
						Behaviour.KeyNext,
						Behaviour.KeyPause,
						Behaviour.KeyVolDn,
						Behaviour.KeyVolUp }, new String[] {
						"Página Anterior",
						"Próxima Página",
						"Remover",
						"Música Anterior",
						"Próxima Música",
						"Pausar",
						"Diminuir Volume",
						"Aumentar Volume" }, this, true));
				break;
			case 3: //Cores...
				Main.openWindow(new WindowColorConfig());
				break;
			case 4: //Equalizador
				if (!equalizer.isAlive()) {
					Main.alertShow("Equalizador não suportado!", true);
				} else {
					Main.openWindow(new WindowEqualizer(equalizer));
				}
				break;
			case 5: //Sobre
				Main.openWindow(new WindowAbout());
				break;
			}
			break;

		case 4: //mark menu
			switch (item.getId()) {
			case 1: //Mover
				eventCommand(commandMove);
				break;
				
			case 2: //Remover
				eventCommand(commandDelete);
				break;
			}
			break;

		case 5: //context menu
			switch (item.getId()) {
			case 1: //Iniciar Seleção
				if (isProcessingThreadFree()) {
					//can't let mark while inserting...
					listBox.setMarking(true);
					Main.commandBarRefresh();
				}
				break;
				
			case 2: //Mover Selecionado
				eventCommand(commandMove);
				break;
				
			case 3: //Remover
				eventCommand(commandDelete);
				break;
			}
			break;
		}
	}
	
	public final void eventCommand(Command command) {
		if (command.equals(commandExit)) {
			Main.terminate();
		} else if (!isChangingMode) {
			if (command.equals(commandMenu)) {
				if (!listBox.isMarking()) {
					//player menu
					showMenu(1, new MenuItem[] {
						new MenuItem("Modo Carro", 1),
						new MenuItem(player.isRadioMode() ? "Ouvir Músicas" : "Ouvir Rádio", 2),
						new MenuItem(player.isRadioMode() ? "Adicionar Estação..." : "Adicionar Músicas...", 3),
						null,
						new MenuItem("Lista", 2, new MenuItem[] {
							new MenuItem("Limpar", 1),
							new MenuItem("Abrir...", 2),
							new MenuItem("Salvar...", 3)
						}),
						null,
						new MenuItem("Opções", 3, new MenuItem[] {
							new MenuItem("Geral...", 1),
							new MenuItem("Teclas...", 2),
							new MenuItem("Cores...", 3),
							null,
							new MenuItem("Equalizador", 4),
							//null,
							//player.isRadioMode() ? new MenuItem("Estéreo", player.isRadioStereo()) : new MenuItem("Pré-Carregar Músicas", Behaviour.environmentGetLoadNextSong()),
							null,
							new MenuItem("Sobre", 5)
						})
					});
				} else {
					//mark menu
					showMenu(4, new MenuItem[] {
						new MenuItem("Mover", 1),
						null,
						new MenuItem("Remover", 2)
					});
				}
			} else if (command.equals(commandOK)) {
				//start playing current file/unpause
				if (player.isPaused() && (listBox.selectedIndex() == listBox.getHilightIndex())) {
					player.pause();
				} else {
					if (listBox.selectedItem() != null) {
						listBox.setHilightIndex(listBox.selectedIndex());
						player.play((Song)listBox.selectedItem());
					}
				}
			} else if (command.equals(commandPrev)) {
				player.play(getNextPrevSongAndSelect(false));
			} else if (command.equals(commandPause)) {
				player.pause();
			} else if (command.equals(commandNext)) {
				player.play(getNextPrevSongAndSelect(true));
			} else if (command.equals(commandBack) || command.equals(commandCancel)) {
				if (listBox.isMarking() || listBox.isMoving()) {
					listBox.setMarking(false);
					listBox.setMoving(false);
					listBox.clearMarks();
					Main.commandBarRefresh();
				}
			} else if (command.equals(commandMove)) {
				if (isProcessingThreadFree()) {
					listBox.setMoving(true);
					listSetChanged();
					Main.commandBarRefresh();
				}
			} else if (command.equals(commandDelete)) {
				if (isProcessingThreadFree()) {
					if (listBox.isMarking() || listBox.isMoving()) {
						listBox.removeMarkedItems();
						listBox.setMarking(false);
						Main.commandBarRefresh();
					} else {
						listBox.removeSelectedItem();
					}
					listSetChanged();
				}
			}
		}
	}
	
	protected final void eventOpened() {
		if (isCarMode)
			Main.openWindow(new WindowPlayerCar(player, equalizer, this, commandExit, commandPrev, commandPause, commandNext), false);
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Behaviour.KeyDel) {
			//remove the selected song from the list
			if (repeatCount == 0)
				eventCommand(commandDelete);
		} else if (keyCode == Behaviour.KeyPause) {
			if (repeatCount == 0)
				eventCommand(commandPause);
		} else if (keyCode == Behaviour.KeyPrev) {
			if (repeatCount == 0)
				eventCommand(commandPrev);
		} else if (keyCode == Behaviour.KeyNext) {
			if (repeatCount == 0)
				eventCommand(commandNext);
		} else if (keyCode == Behaviour.KeyVolDn) {
			player.volumeDown();
		} else if (keyCode == Behaviour.KeyVolUp) {
			player.volumeUp();
		} else {
			return super.eventKeyPress(keyCode, repeatCount);
		}
		return true;
	}
	
	protected final void paintContents(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		final int titY = screenOffsetY + titleY;
		
		if (clipY < (titY + getTitleHeight()) && (clipY + clipHeight) > titY) {
			final Song cursong = player.getCurrentSong();
			final String[] title = { "", "" };
			titleText.delete(0, titleText.length());
			if (cursong == null) {
				title[0] = "[Nenhuma música]";
			} else {
				title[0] = cursong.getTitle();
				
				//update the player stuff
				if (!player.isSongLoaded()) {
					titleText.append("[Parado]");
				} else {
					if (player.isPaused()) {
						titleText.append("[Pausado]");
					} else if (player.isRadioMode()) {
						titleText.append("[Recebendo]");
					} else {
						int playTime = player.getCurrentPlayTime();
						titleText.append(playTime / 60);
						titleText.append('\'');
						playTime %= 60;
						if (playTime < 10) titleText.append('0');
						titleText.append(playTime);
						titleText.append('\"');
						titleText.append(player.getCurrentSongLength());
					}
				}
			}
			
			if (Behaviour.environmentHasVolumeControl()) {
				//after preparing the second line of text, append the
				//volume to its end
				if (cursong != null)
					titleText.append(" - ");
				if (player.getVolume() == 0) {
					titleText.append("Mudo");
				} else {
					titleText.append(player.getVolume());
					titleText.append('%');
				}
			}
			title[1] = titleText.toString();
			
			Main.Customizer.paintTitle(g, screenOffsetX, titY, getWidth(), getTitleHeight());
			g.setColor(baseUI.Behaviour.ColorTitleText);
			Main.FontTitle.select(g);
			g.drawString(title[0], screenOffsetX, titY, 0);
			g.drawString(title[1], screenOffsetX, titY + Main.FontTitle.height, 0);
		}
	}
	
	private final void listRefreshSongsTitle_() {
		for (int i = 0; i < listBox.itemCount(); ++i) {
			final Object o = listBox.itemAt(i);
			if (o != null) {
				((Song)o).refreshTitle(true);
			}
		}
		invalidate();
	}
	
	public final boolean listRefreshSongsTitle() {
		if (!setProcessingThread(new MessageThread(this, "Player Refresh Titles"))) {
			return false;
		}
		
		listSetChanged();
		
		startProcessingThread(MSG_REFRESHTITLES);
		
		return true;
	}
	
	public final void listSetChanged() {
		hasListChanged = true;
	}
	
	private final boolean listLoad_(String fileName) {
		if (player != null) player.stopAndWait();
		
		hasChangedPosition = false;
		
		listBox.clear();
		
		hasListChanged = false;
		
		final SongList songList = new SongList();
		
		if (songList.load(fileName)) {
			listBox.replaceContents(songList.getSongs());
			listBox.invalidate();
			listBox.selectItem(0);
			
			System.gc();
			
			return true;
		} else {
			System.gc();
			
			return false;
		}
	}
	
	private final boolean listSave_(String fileName) {
		return (new SongList()).save(fileName, listBox.getIterator());
	}
	
	private final void listSaveDefault_() {
		if (player.isRadioMode()) {
			lastRadioIndex = listBox.getHilightIndex();
		} else {
			lastIndex = listBox.getHilightIndex();
		}
		
		if (hasListChanged) {
			hasListChanged = false;
			listSave_(player.isRadioMode() ? "listRDefault" : "listDefault");
		}
	}
	
	private final void listChangeMode_() {
		player.stopAndWait();
		
		listSaveDefault_();
		
		final boolean isRadioMode = !player.isRadioMode();
		
		//load the default list and the configurations
		listLoad_(isRadioMode ? "listRDefault" : "listDefault");
		
		final Song currentSong;
		
		//try to restore the previous current item
		final int i = (isRadioMode ? lastRadioIndex : lastIndex);
		if (i < listBox.itemCount() && i >= 0) {
			listBox.setHilightIndex(i);
			currentSong = (Song)listBox.itemAt(i);
		} else {
			currentSong = null;
		}
		
		player.setRadioMode(isRadioMode, currentSong);
		
		System.gc();
		
		//if the user has already moved the cursor, then just refresh,
		//otherwise, go to the last position
		if (!hasChangedPosition) {
			listBox.selectItem((i >= 0) && (i < listBox.itemCount()) ? i : 0);
		}
		
		invalidate();
		
		isChangingMode = false;
		
		resetGlobalVolume();
	}
	
	private final void listChangeMode() {
		if (!setProcessingThread(new MessageThread(this, "Player Mode Change"))) {
			return;
		}
		
		isChangingMode = true;
		
		startProcessingThread(MSG_CHANGEMODE);
	}
	
	private final void listClear() {
		if (isProcessingThreadFree()) {
			player.stopAndClear();
			listBox.clear();
			hasChangedPosition = false;
			listSetChanged();
		}
	}
	
	private final Song getNextPrevSongAndSelect(boolean nextSong) {
		final int i = getNextPrevSongIndex(nextSong, true);
		if (i < 0) return null;
		if (listBox.isMarking() || listBox.isMoving()) {
			listBox.setHilightIndex(i);
		} else {
			listBox.selectItemAndHilight(i);
		}
		return (Song)listBox.itemAt(i);
	}
	
	private final int getNextPrevSongIndex(boolean nextSong, boolean updateCurrent) {
		int i = listBox.getHilightIndex();
		if (nextSong) {
			//try to get the next file
			i++;
			if (i >= listBox.itemCount()) {
				i = 0;
			}
		} else {
			//try to get the previous file
			i--;
			if (i < 0) {
				i = listBox.itemCount() - 1;
			}
		}
		if (i >= listBox.itemCount() || i < 0) i = -1;
		if (updateCurrent) listBox.setHilightIndex(i);
		return i;
	}
	
	public final void attached() {
	}
	
	public final void detached() {
	}
	
	public final Song getSong(boolean nextSong, boolean justPeakNext) {
		if (!nextSong) {
			int currentIndex = listBox.getHilightIndex();
			if (currentIndex < 0) {
				currentIndex = 0;
			} else if (currentIndex >= listBox.itemCount()) {
				currentIndex = listBox.itemCount() - 1;
			}
			if (currentIndex < 0 || currentIndex >= listBox.itemCount()) {
				return null;
			}
			if (listBox.isMarking() || listBox.isMoving()) {
				listBox.setHilightIndex(currentIndex);
			} else {
				listBox.selectItemAndHilight(currentIndex);
			}
			return (Song)listBox.itemAt(currentIndex);
		}
		
		final int i = getNextPrevSongIndex(true, !justPeakNext);
		if (i < 0) return null;
		if (!justPeakNext) {
			if (!hasChangedPosition && !listBox.isMarking() && !listBox.isMoving()) {
				listBox.selectItemAndHilight(i);
			} else {
				listBox.setHilightIndex(i);
				hasChangedPosition = false;
			}
		}
		if (i == cycleIndex) return null;
		if (cycleIndex < 0) cycleIndex = i;
		return (Song)listBox.itemAt(i);
	}
	
	public final void resetSongCycling() {
		cycleIndex = -1;
	}
	
	public final void stateChanged() {
		invalidateTitle();
	}
}
