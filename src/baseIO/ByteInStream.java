//
// ByteInStream.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseIO/ByteInStream.java
//

package baseIO;

import baseUtil.StringUtil;
import java.io.InputStream;

public final class ByteInStream extends InputStream implements ByteStream {
	private byte[] buf;
	private int length, offset, pos;
	
	public ByteInStream(byte[] b) {
		reset(b);
	}
	
	public ByteInStream(byte[] b, int offset, int length) {
		reset(b, offset, length);
	}
	
	public ByteInStream(ByteStream stream) {
		reset(stream);
	}
	
	public final byte[] toByteArray() {
		final byte[] b = new byte[length];
		System.arraycopy(buf, offset, b, 0, length);
		return b;
	}
	
	public final byte[] getBuffer() {
		return buf;
	}
	
	public final boolean eos() {
		return ((pos - offset) >= length);
	}
	
	public final int available() {
		return (length - pos + offset);
	}
	
	public final int startOffset() {
		return offset;
	}
	
	public final int size() {
		return length;
	}
	
	public final boolean markSupported() {
		return false;
	}
	
	public final int position() {
		return (pos - offset);
	}
	
	public final void reset() {
		pos = offset;
	}
	
	public final void reset(byte[] b) {
		reset(b, 0, b.length);
	}
	
	public final void reset(byte[] b, int offset, int length) {
		this.buf = b;
		this.length = length;
		this.offset = offset;
		this.pos = offset;
	}
	
	public final void reset(ByteStream stream) {
		reset(stream.getBuffer(), stream.startOffset(), stream.size());
	}
	
	public final int read() {
		if (eos()) return -1;
		return readUByte();
	}
	
	public final int read(byte[] b) {
		return read(b, 0, b.length);
	}
	
	public final int read(byte[] b, int offset, int length) {
		int a = available();
		if (a <= 0) return -1;
		if (length > a) length = a;
		System.arraycopy(buf, pos, b, offset, length);
		pos += length;
		return length;
	}
	
	public final byte[] read(int length) {
		int a = available();
		if (a <= 0) return null;
		if (length > a) length = a;
		final byte[] b = new byte[length];
		System.arraycopy(buf, pos, b, 0, length);
		pos += length;
		return b;
	}
	
	public final boolean peekBoolean() {
		if (available() < 1) return false;
		return (buf[pos] != 0);
	}
	
	public final int peekByte() {
		if (available() < 1) return 0;
		return (int)buf[pos];
	}
	
	public final int peekUByte() {
		if (available() < 1) return 0;
		return (int)buf[pos] & 0xFF;
	}
	
	public final char peekChar() {
		if (available() < 2) return 0;
		return (char)BitConverter.byteToUShort(buf, pos);
	}
	
	public final int peekShort() {
		if (available() < 2) return 0;
		return BitConverter.byteToShort(buf, pos);
	}
	
	public final int peekUShort() {
		if (available() < 2) return 0;
		return BitConverter.byteToUShort(buf, pos);
	}
	
	public final int peekInt3() {
		if (available() < 3) return 0;
		return BitConverter.byteToInt3(buf, pos);
	}
	
	public final int peekUInt3() {
		if (available() < 3) return 0;
		return BitConverter.byteToUInt3(buf, pos);
	}
	
	public final int peekInt() {
		if (available() < 4) return 0;
		return BitConverter.byteToInt(buf, pos);
	}
	
	public final long peekLong() {
		if (available() < 8) return 0;
		return BitConverter.byteToLong(buf, pos);
	}
	
	public final String peekString() {
		final int a = available();
		if (a <= 2) return ""; 
		final int s = peekUShort();
		if (s <= 0 || (s + 2) > a) return "";
		return StringUtil.fromUTF8(buf, pos + 2, s);
	}
	
	public final int peek(byte[] b) {
		return peek(b, 0, b.length);
	}
	
	public final int peek(byte[] b, int offset, int length) {
		if (length > available()) length = available();
		System.arraycopy(buf, pos, b, offset, length);
		return length;
	}
	
	public final byte[] peek(int length) {
		if (length > available()) length = available();
		final byte[] b = new byte[length];
		System.arraycopy(buf, pos, b, 0, length);
		return b;
	}
	
	public final long skip(long n) {
		return skipBytes((int)n);
	}
	
	public final int skipBytes(int n) {
		if (n > available()) n = available();
		pos += n;
		return n;
	}
	
	public final int skipBoolean() {
		return skipBytes(1);
	}
	
	public final int skipByte() {
		return skipBytes(1);
	}
	
	public final int skipChar() {
		return skipBytes(2);
	}
	
	public final int skipShort() {
		return skipBytes(2);
	}
	
	public final int skipInt3() {
		return skipBytes(3);
	}
	
	public final int skipInt() {
		return skipBytes(4);
	}
	
	public final int skipLong() {
		return skipBytes(8);
	}
	
	public final int skipString() {
		return skipBytes(peekUShort() + 2);
	}
	
	private final boolean readPrepare(int n) {
		if (available() < n) {
			pos += available();
			return false;
		}
		pos += n;
		return true;
	}
	
	public final boolean readBoolean() {
		if (!readPrepare(1)) return false;
		return (buf[pos - 1] != 0);
	}
	
	public final int readByte() {
		if (!readPrepare(1)) return 0;
		return (int)buf[pos - 1];
	}
	
	public final int readUByte() {
		if (!readPrepare(1)) return 0;
		return (int)buf[pos - 1] & 0xFF;
	}
	
	public final char readChar() {
		if (!readPrepare(2)) return 0;
		return (char)BitConverter.byteToUShort(buf, pos - 2);
	}
	
	public final int readShort() {
		if (!readPrepare(2)) return 0;
		return BitConverter.byteToShort(buf, pos - 2);
	}
	
	public final int readUShort() {
		if (!readPrepare(2)) return 0;
		return BitConverter.byteToUShort(buf, pos - 2);
	}
	
	public final int readInt3() {
		if (!readPrepare(3)) return 0;
		return BitConverter.byteToInt3(buf, pos - 3);
	}
	
	public final int readUInt3() {
		if (!readPrepare(3)) return 0;
		return BitConverter.byteToUInt3(buf, pos - 3);
	}
	
	public final int readInt() {
		if (!readPrepare(4)) return 0;
		return BitConverter.byteToInt(buf, pos - 4);
	}
	
	public final long readLong() {
		if (!readPrepare(8)) return 0;
		return BitConverter.byteToLong(buf, pos - 8);
	}
	
	public final String readString() {
		final int s = readUShort();
		if (s <= 0 || !readPrepare(s)) return "";
		return StringUtil.fromUTF8(buf, pos - s, s);
	}
}
