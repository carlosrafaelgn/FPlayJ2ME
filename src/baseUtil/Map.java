//
// Map.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUtil/Map.java
//

package baseUtil;

import baseIO.BitConverter;
import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseIO.File;
import baseIO.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

public final class Map implements Serializable {
	private final Hashtable table;
	private final MapItem hkey;
	
	public Map() {
		table = new Hashtable();
		hkey = new MapItem(0);
	}
	
	public Map(int initialCapacity) {
		table = new Hashtable(initialCapacity);
		hkey = new MapItem(0);
	}
	
	public static Map fromStream(ByteInStream stream) {
		final byte[] tmp = new byte[6];
		
		if (stream.peek(tmp, 0, 6) != 6 ||
			BitConverter.byteToInt(tmp, 0) != 0xCAFEBABE) {
			return new Map();
		}
		
		stream.skipBytes(6);
		
		final int count = BitConverter.byteToUShort(tmp, 4);
		
		final Map map = new Map(count);
		
		for (int i = 0; i < count; i++) {
			MapItem mi = MapItem.fromStream(stream);
			map.table.put(mi, mi);
		}
		
		return map;
	}
	
	public static Map fromFile(String fullName) {
		File file = new File(fullName, true);
		file.openExisting();
		ByteInStream stream = file.readStream();
		file.close();
		file = null;
		
		final Map map = ((stream != null) ? fromStream(stream) : new Map());
		
		stream = null;
		
		return map;
	}
	
	public boolean toFile(String fullName) {
		ByteOutStream stream = new ByteOutStream(512);
		serialize(stream);
		
		File file = new File(fullName, true);
		file.openCreate();
		boolean r = file.write(stream);
		file.close();
		
		stream = null;
		file = null;
		
		return r;
	}
	
	private static int createKey(int x) {
		return 0x29f73ff7 ^ ((x << 16) | (x >>> 16));
	}
	
	public final void serialize(ByteOutStream stream) {
		stream.writeInt(0xCAFEBABE);
		stream.writeShort(table.size());
		final Enumeration elements = table.elements();
		while (elements.hasMoreElements()) {
			((MapItem)elements.nextElement()).serialize(stream);
		}
	}
	
	public final int size() {
		return table.size();
	}
	
	public final boolean contains(int key) {
		hkey.key = createKey(key);
		return table.containsKey(hkey);
	}
	
	public final void remove(int key) {
		hkey.key = createKey(key);
		table.remove(hkey);
	}
	
	public final boolean getBoolean(int key, boolean def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return (mi.value != 0);
		return def;
	}
	
	public final int getByte(int key, int def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return ((mi.value << 24) >> 24);
		return def;
	}
	
	public final int getUByte(int key, int def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return (mi.value & 0xFF);
		return def;
	}
	
	public final char getChar(int key, char def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return (char)mi.value;
		return def;
	}
	
	public final int getShort(int key, int def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return ((mi.value << 16) >> 16);
		return def;
	}
	
	public final int getUShort(int key, int def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return (mi.value & 0xFFFF);
		return def;
	}
	
	public final int getInt3(int key, int def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return ((mi.value << 8) >> 8);
		return def;
	}
	
	public final int getUInt3(int key, int def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return (mi.value & 0xFFFFFF);
		return def;
	}
	
	public final int getInt(int key, int def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return mi.value;
		return def;
	}
	
	public final float getFloat(int key, float def) {
		hkey.key = createKey(key);
		MapItemI mi = (MapItemI)table.get(hkey);
		if (mi != null)
			return Float.intBitsToFloat(mi.value);
		return def;
	}
	
	public final long getLong(int key, long def) {
		hkey.key = createKey(key);
		MapItemL mi = (MapItemL)table.get(hkey);
		if (mi != null)
			return mi.value;
		return def;
	}
	
	public final String getString(int key, String def) {
		hkey.key = createKey(key);
		MapItemB mi = (MapItemB)table.get(hkey);
		if (mi != null)
			return StringUtil.fromUTF8(mi.value);
		return def;
	}
	
	public final byte[] getBytes(int key, byte[] def) {
		hkey.key = createKey(key);
		MapItemB mi = (MapItemB)table.get(hkey);
		if (mi != null)
			return mi.value;
		return def;
	}
	
	public final void putBoolean(int key, boolean x) {
		MapItemI mi = new MapItemI(createKey(key), x ? -1 : 0);
		table.put(mi, mi);
	}
	
	public final void putChar(int key, char x) {
		MapItemI mi = new MapItemI(createKey(key), x);
		table.put(mi, mi);
	}
	
	public final void putInt(int key, int x) {
		MapItemI mi = new MapItemI(createKey(key), x);
		table.put(mi, mi);
	}
	
	public final void putFloat(int key, float x) {
		MapItemI mi = new MapItemI(createKey(key), Float.floatToIntBits(x));
		table.put(mi, mi);
	}
	
	public final void putLong(int key, long x) {
		MapItemL mi = new MapItemL(createKey(key), x);
		table.put(mi, mi);
	}
	
	public final void putString(int key, String x) {
		MapItemB mi = new MapItemB(createKey(key), StringUtil.toUTF8(x));
		table.put(mi, mi);
	}
	
	public final void putBytes(int key, byte[] x) {
		MapItemB mi = new MapItemB(createKey(key), x);
		table.put(mi, mi);
	}
}
