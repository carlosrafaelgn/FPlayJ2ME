//
// ByteOutStream.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseIO/ByteOutStream.java
//

package baseIO;

import baseUtil.StringUtil;
import java.io.OutputStream;

public final class ByteOutStream extends OutputStream implements ByteStream {
	private byte[] buf;
	private int count;
	
	public ByteOutStream() {
		buf = new byte[32];
	}
	
	public ByteOutStream(int capacity) {
		buf = new byte[capacity];
	}
	
	public ByteOutStream(byte[] b) {
		buf = b;
	}
	
	public final void close() { }
	
	public final void flush() { }
	
	public final byte[] toByteArray() {
		final byte[] b = new byte[count];
		System.arraycopy(buf, 0, b, 0, count);
		return b;
	}
	
	public final byte[] getBuffer() {
		return buf;
	}
	
	public final void ensureCapacity(int size) {
		if ((buf.length - count) >= size) return;
		final byte[] newBuf = new byte[buf.length + size + (buf.length >> 3)]; //add an extra 12.5%
		System.arraycopy(buf, 0, newBuf, 0, count);
		buf = newBuf;
	}
	
	public final int capacity() {
		return buf.length;
	}
	
	public final int startOffset() {
		return 0;
	}
	
	public final int size() {
		return count;
	}
	
	public final void reset() {
		count = 0;
	}
	
	public final void write(int b) {
		writeByte(b);
	}
	
	public final void write(byte[] b) {
		ensureCapacity(b.length);
		System.arraycopy(b, 0, buf, count, b.length);
		count += b.length;
	}
	
	public final void write(byte[] b, int offset, int length) {
		ensureCapacity(length);
		System.arraycopy(b, offset, buf, count, length);
		count += length;
	}
	
	public final void writeBoolean(boolean x) {
		ensureCapacity(1);
		buf[count++] = (x ? (byte)-1 : 0);
	}
	
	public final void writeByte(int x) {
		ensureCapacity(1);
		buf[count++] = (byte)x;
	}
	
	public final void writeChar(char x) {
		ensureCapacity(2);
		BitConverter.shortToByte(buf, count, x);
		count += 2;
	}
	
	public final void writeShort(int x) {
		ensureCapacity(2);
		BitConverter.shortToByte(buf, count, x);
		count += 2;
	}
	
	public final void writeInt3(int x) {
		ensureCapacity(3);
		BitConverter.int3ToByte(buf, count, x);
		count += 3;
	}
	
	public final void writeInt(int x) {
		ensureCapacity(4);
		BitConverter.intToByte(buf, count, x);
		count += 4;
	}
	
	public final void writeLong(long x) {
		ensureCapacity(8);
		BitConverter.longToByte(buf, count, x);
		count += 8;
	}
	
	public final void writeString(String x) {
		if (x == null || x.length() == 0) {
			writeShort(0);
		} else {
			final byte[] b = StringUtil.toUTF8(x);
			final int t = 2 + b.length;
			ensureCapacity(t);
			BitConverter.shortToByte(buf, count, b.length);
			System.arraycopy(b, 0, buf, count + 2, b.length);
			count += t;
		}
	}
}
