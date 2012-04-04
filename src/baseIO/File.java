//
// File.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseIO/File.java
//

package baseIO;

import baseUtil.Sortable;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

public final class File implements Sortable {
	private final String fullName, name, lname;
	private String lastError;
	private RecordStore rs;
	private int singleRecordId;
	private final boolean singleRecordMode;

	public File(String fullName, boolean singleMode) {
		this.fullName = fullName;
		this.name = ((fullName.charAt(0) == '_') ? fullName.substring(2) : fullName);
		this.lname = this.name.toLowerCase();
		this.singleRecordId = -1;
		this.singleRecordMode = singleMode;
	}

	public File(String name, char prefix, boolean singleMode) {
		this.fullName = "_" + prefix + name;
		this.name = name;
		this.lname = this.name.toLowerCase();
		this.singleRecordId = -1;
		this.singleRecordMode = singleMode;
	}
	
	public final String toString() {
		return name;
	}
	
	public final int compare(Sortable item) {
		return lname.compareTo(((File)item).lname);
	}

	public final boolean isSystemFile() {
		return (fullName.charAt(0) != '_');
	}

	public final String getLastError() {
		return lastError;
	}

	public final String getFullName() {
		return fullName;
	}

	public final String getName() {
		return name;
	}

	public final boolean openTruncate() {
		return open(true, true);
	}

	public final boolean openExisting() {
		return open(false, false);
	}

	public final boolean openCreate() {
		return open(false, true);
	}

	private final boolean open(boolean truncateIfFound, boolean createIfNotFound) {
		close();
		if (truncateIfFound) {
			try {
				RecordStore.deleteRecordStore(fullName);
				createIfNotFound = true;
			} catch (Exception ex) { }
		}
		RecordEnumeration re = null;
		try {
			rs = RecordStore.openRecordStore(fullName, createIfNotFound);
			if (singleRecordMode) {
				re = rs.enumerateRecords(null, null, false);
				if (re.hasNextElement()) {
					singleRecordId = re.nextRecordId();
				}
			}
			lastError = null;
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			close();
			return false;
		} finally {
			if (re != null) re.destroy();
		}
	}
	
	public final boolean close() {
		try
		{
			if (rs != null) rs.closeRecordStore();
			lastError = null;
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		} finally {
			rs = null;
			singleRecordId = -1;
		}
	}

	public final boolean delete() {
		close();
		try {
			RecordStore.deleteRecordStore(fullName);
			lastError = null;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
		return true;
	}
	
	public final boolean read(byte[] b, int offset) {
		if (!singleRecordMode) return false;
		try
		{
			rs.getRecord(singleRecordId, b, offset);
			lastError = null;
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
	}
	
	public final byte[] read() {
		if (!singleRecordMode) return null;
		try
		{
			lastError = null;
			return rs.getRecord(singleRecordId);
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return null;
		}
	}
	
	public final ByteInStream readStream() {
		if (!singleRecordMode) return null;
		try
		{
			lastError = null;
			return new ByteInStream(rs.getRecord(singleRecordId));
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return null;
		}
	}
	
	public final boolean write(ByteStream stream) {
		return write(stream.getBuffer(), stream.startOffset(), stream.size());
	}
	
	public final boolean write(byte[] b) {
		return write(b, 0, b.length);
	}
	
	public final boolean write(byte[] b, int offset, int length) {
		if (!singleRecordMode) return false;
		try
		{
			if (singleRecordId == -1) {
				singleRecordId = rs.addRecord(b, offset, length);
			} else {
				rs.setRecord(singleRecordId, b, offset, length);
			}
			lastError = null;
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
	}

	public final int getRecordCount() {
		try {
			return rs.getNumRecords();
		} catch (Exception e) {
			return -1;
		}
	}
	
	public final boolean enumerateRecords(FileEnumerationListener listener) {
		RecordEnumeration re = null;
		try
		{
			final ByteOutStream outp = new ByteOutStream(64);
			final ByteInStream inp = new ByteInStream(outp);
			
			re = rs.enumerateRecords(null, null, false);
			while (re.hasNextElement()) {
				final int i = re.nextRecordId();
				final int l = rs.getRecordSize(i);
				outp.ensureCapacity(l);
				rs.getRecord(i, outp.getBuffer(), 0);
				//don't use inp.reset(outp), because outp.size is 0,
				//since it's used just for resizing the byte[] buffer
				inp.reset(outp.getBuffer(), 0, l);
				if (!listener.recordEnumerated(inp)) break;
			}
			
			lastError = null;
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		} finally {
			if (re != null) re.destroy();
		}
	}
	
	public final boolean addRecord(ByteStream stream) {
		return addRecord(stream.getBuffer(), stream.startOffset(), stream.size());
	}
	
	public final boolean addRecord(byte[] b) {
		return addRecord(b, 0, b.length);
	}
	
	public final boolean addRecord(byte[] b, int offset, int length) {
		try
		{
			rs.addRecord(b, offset, length);
			lastError = null;
			return true;
		} catch (Exception ex) {
			lastError = ex.getMessage();
			return false;
		}
	}
	
	public static File[] listFiles(char prefix, boolean singleMode) {
		String[] l = RecordStore.listRecordStores();
		if (l == null || l.length == 0) return new File[0];
		
		int i, ii, c = 0;
		for (i = 0; i < l.length; ++i) {
			if (l[i].charAt(0) == '_' &&
				l[i].charAt(1) == prefix) {
				++c;
			}
		}
		
		File[] r = new File[c];
		for (i = 0, ii = 0; i < l.length; ++i) {
			if (l[i].charAt(0) == '_' &&
				l[i].charAt(1) == prefix) {
				r[ii] = new File(l[i], singleMode);
				++ii;
			}
		}

		baseUtil.Vector.sortArray(r, 0, r.length);
		
		return r;
	}
}
