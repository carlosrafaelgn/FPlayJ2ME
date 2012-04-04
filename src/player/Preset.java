//
// Preset.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/player/Preset.java
//

package player;

import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseIO.Serializable;
import baseUtil.Sortable;

public final class Preset implements Sortable, Serializable {
	public final String name;
	public final boolean isCustom;
	private final int hash;
	final int[] bandsLevel;
	
	Preset(String name) {
		this.isCustom = false;
		this.name = name;
		this.hash = this.name.hashCode() ^ (isCustom ? 0xAA55 : 0x4422);
		this.bandsLevel = null;
	}
	
	Preset(String name, int[] bandsLevel, int bandCount, int minLevel, int maxLevel) {
		this.isCustom = true;
		this.name = name;
		this.hash = this.name.hashCode() ^ (isCustom ? 0xAA55 : 0x4422);
		this.bandsLevel = new int[bandCount];
		
		final int tot = Math.min(bandCount, bandsLevel.length);
		
		for (int i = 0; i < tot; i++) {
			final int level = bandsLevel[i];
			if (level <= minLevel) this.bandsLevel[i] = minLevel;
			else if (level >= maxLevel) this.bandsLevel[i] = maxLevel;
			else this.bandsLevel[i] = level;
		}
	}
	
	Preset(ByteInStream stream, int bandCount, int minLevel, int maxLevel) {
		this.isCustom = true;
		this.name = stream.readString();
		this.hash = this.name.hashCode() ^ (isCustom ? 0xAA55 : 0x4422);
		this.bandsLevel = new int[bandCount];
		
		int bcount = stream.readUByte();
		final int tot = Math.min(bandCount, bcount);
		
		for (int i = 0; i < tot; i++) {
			bcount--;
			final int level = stream.readInt();
			if (level <= minLevel) this.bandsLevel[i] = minLevel;
			else if (level >= maxLevel) this.bandsLevel[i] = maxLevel;
			else this.bandsLevel[i] = level;
		}
		stream.skipBytes(bcount << 2); //skip any unread bands
	}
	
	public final int hashCode() {
		return this.hash;
	}
	
	public final boolean equals(Object obj) {
		Preset p = (Preset)obj;
		return (p.hash == this.hash) && (p.name.equals(this.name)) && (p.isCustom == this.isCustom);  
	}
	
	public final String toString() {
		return this.name;
	}
	
	public final void serialize(ByteOutStream stream) {
		if (isCustom) {
			stream.writeString(this.name);
			stream.writeByte(this.bandsLevel.length);
			for (int i = 0; i < this.bandsLevel.length; i++) {
				stream.writeInt(this.bandsLevel[i]);
			}
		}
	}

	public int compare(Sortable item) {
		return name.compareTo(item.toString());
	}
}
