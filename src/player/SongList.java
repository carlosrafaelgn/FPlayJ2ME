//
// SongList.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/player/SongList.java
//

package player;

import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseIO.File;
import baseIO.FileEnumerationListener;
import baseUtil.Iterator;
import baseUtil.Vector;

public final class SongList {
	private Vector songs;
	
	public SongList() {
		songs = new Vector();
	}
	
	public SongList(int capacity) {
		songs = new Vector(capacity);
	}
	
	public final Vector getSongs() {
		return songs;
	}
	
	public final void setSongs(Vector songs) {
		this.songs = songs;
	}
	
	public final boolean load(String fileName) {
		final File file = new File(fileName, false);
		
		if (file.openExisting()) {
			if (songs == null) {
				songs = new Vector(file.getRecordCount());
			} else {
				songs.removeAllElements();
				songs.ensureCapacity(file.getRecordCount());
			}
			file.enumerateRecords(new FileEnumerationListener() {
				public boolean recordEnumerated(ByteInStream stream) {
					songs.addElement(Song.deserialize(stream));
					return true;
				}
			});
			
			file.close();
			
			songs.sort();
			
			System.gc();
			
			return true;
		} else {
			file.close();
			
			System.gc();
			
			return false;
		}
	}
	
	public final boolean save(String fileName) {
		return save(fileName, null);
	}
	
	public final boolean save(String fileName, Iterator iterator) {
		final File file = new File(fileName, false);
		
		if (file.openTruncate()) {
			final ByteOutStream outp = new ByteOutStream(64);
			
			int i = 0;
			Song s;
			
			if (iterator == null) {
				final int tot = songs.size();
				for (; i < tot; i++) {
					outp.reset();
					
					s = (Song)songs.elementAt(i);
					s.setOrder(i);
					s.serialize(outp);
					file.addRecord(outp);
				}
			} else {
				while (iterator.hasNext()) {
					outp.reset();
					
					s = (Song)iterator.next();
					s.setOrder(i++);
					s.serialize(outp);
					file.addRecord(outp);
				}
			}
			
			file.close();
			
			System.gc();
			
			return true;
		} else {
			file.close();
			
			System.gc();
			
			return false;
		}
	}
}
