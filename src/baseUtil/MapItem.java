//
// MapItem.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUtil/MapItem.java
//

package baseUtil;

import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseIO.Serializable;

class MapItem implements Serializable {
	public int key;
	
	public MapItem(int key) {
		this.key = key;
	}
	
	public final int hashCode() {
		return key;
	}
	
	public final boolean equals(Object obj) {
		return ((MapItem)obj).key == key;
	}
	
	public static MapItem fromStream(ByteInStream stream) {
		final int type = stream.readUByte();
		final int key = stream.readInt();
		final int size = stream.readUShort();
		
		switch (type) {
		case MapItemI.TYPE:
			return new MapItemI(key, stream.readInt());
			
		case MapItemL.TYPE:
			return new MapItemL(key, stream.readLong());
			
		case MapItemB.TYPE:
			return new MapItemB(key, stream.read(size));
			
		default:
			stream.skipBytes(size);
			return new MapItem(key);
		}
	}
	
	public int getType() {
		return 0x00;
	}
	
	public int getSize() {
		return 0;
	}
	
	public void serialize(ByteOutStream stream) {
		stream.writeByte(getType());
		stream.writeInt(this.key);
		stream.writeShort(getSize());
	}
}