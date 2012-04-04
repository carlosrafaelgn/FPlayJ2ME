//
// Song.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/player/Song.java
//

package player;

import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseIO.Serializable;
import baseUtil.Sortable;
import baseUtil.StringUtil;

import javax.microedition.media.Control;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.MetaDataControl;

public final class Song implements Sortable, Serializable {
	public static final int TITLE = 0x1;
	public static final int ARTIST = 0x2;
	public static final int AUTHOR = 0x3;
	public static final int FMT_NONE = 0x00;
	public static final int FMT_TITLE = 0x01;
	public static final int FMT_TITLE_ARTIST = 0x21;
	public static final int FMT_TITLE_AUTHOR = 0x31;
	public static final int FMT_ARTIST_TITLE = 0x12;
	public static final int FMT_AUTHOR_TITLE = 0x13;
	public static int Format;
	
	private final String fileName;
	private String title, author, name;
	private int order;
	
	public Song(String fileName) {
		this.fileName = fileName;
		refreshTitle(true);
	}
	
	public Song(String fileName, String songName) {
		this(-1, fileName, songName, null);
	}
	
	public Song(String fileName, String songName, String songAuthor) {
		this(-1, fileName, songName, songAuthor);
	}
	
	private Song(int order, String fileName, String songName, String songAuthor) {
		this.order = order;
		this.fileName = fileName;
		this.name = songName;
		this.author = songAuthor;
		refreshTitle(false);
	}
	
	public final String toString() {
		return title;
	}
	
	final int getOrder() {
		return order;
	}
	
	final void setOrder(int order) {
		this.order = order;
	}

	public final String getTitle() {
		return title;
	}
	
	public final String getFileName() {
		return fileName;
	}
	
	private final void refreshMetaData() {
		if (Format == 0) {
			name = null;
			author = null;
			return;
		}
		
		Player p = null;
		try {
			//try to create the player using 2 types of URL!!!
			p = Manager.createPlayer(getDirectURL());
		} catch (Throwable ex) {
		}
		if (p == null) {
			try {
				p = Manager.createPlayer(getEncodedURL());
			} catch (Throwable ex) {
			}
		}
		if (p == null) return;
		try {
			p.prefetch();
		} catch (Throwable ex) {
			return;
		}
		
		final Control c;
		try {
			c = p.getControl("MetaDataControl");
		} catch (Throwable ex) {
			p.close();
			return;
		}
		if (c == null) {
			p.close();
			return;
		}
		
		final MetaDataControl mc = (MetaDataControl)c;
		//String[] ks = mc.getKeys();
		//for (int i = 0; i < ks.length; i++) {
		//	Main.alertShow(ks[i] + ":" + mc.getKeyValue(ks[i]), true);
		//}
		int fmt = Format;
		while (fmt != 0) {
			switch ((fmt & 0xF)) {
				case 0x1:
					name = mc.getKeyValue(MetaDataControl.TITLE_KEY).trim();
					if (name != null && name.equals("unknown")) name = null;
					break;
				case 0x2:
					try {
						author = mc.getKeyValue("artist").trim();
					} catch (Throwable ex) {
						author = mc.getKeyValue(MetaDataControl.AUTHOR_KEY).trim();
					}
					if (author != null && author.equals("unknown")) author = null;
					break;
				case 0x3:
					author = mc.getKeyValue(MetaDataControl.AUTHOR_KEY).trim();
					if (author != null && author.equals("unknown")) author = null;
					break;
			}
			fmt >>= 4;
		}
		
		p.close();
	}
	
	public final void refreshTitle(boolean readMetaData) {
		if (readMetaData) refreshMetaData();
		
		if (name == null || name.length() == 0) {
			author = null;
			
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				name = fileName.substring(fileName.lastIndexOf('/') + 1, i);
			} else {
				name = fileName.substring(fileName.lastIndexOf('/') + 1);
			}
		} else if (author != null && author.length() == 0) {
			author = null;
		}
		
		int fmt = Format;
		title = null;
		while (fmt != 0) {
			final String str = (((fmt & 0xF) == 0x1) ? name : author);
			if (str != null) {
				if (title == null) {
					title = str;
				} else {
					title += " - ";
					title += str;
				}
			}
			fmt >>= 4;
		}
		
		if (title == null) {
			title = name;
		}
	}
	
	public static Song deserialize(ByteInStream stream) {
		final int o = stream.readInt();
		final String f = stream.readString();
		final String n = stream.readString();
		final String a = stream.readString();
		return new Song(o, f, n, a);
	}
	
	public final void serialize(ByteOutStream stream) {
		stream.writeInt(order);
		stream.writeString(fileName);
		stream.writeString(name);
		stream.writeString(author);
	}
	
	public final int compare(Sortable item) {
		return order - ((Song)item).order;
	}
	
	public final String getRadioURL(boolean forceStereo) {
		return "capture://radio?f=" + fileName + (forceStereo ? "M&st=stereo" : "M");
	}
	
	public final String getDirectURL() {
		return "file:///" + fileName;
	}
	
	public final String getEncodedURL() {
		return StringUtil.encodeURI("file:///", fileName);
	}
}
