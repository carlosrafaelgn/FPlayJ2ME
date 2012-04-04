//
// StringUtil.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUtil/StringUtil.java
//

package baseUtil;

import baseIO.ByteOutStream;
import java.io.UnsupportedEncodingException;

public final class StringUtil {
	private static final char[] HexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
//Bits	Last CP		Byte 1		Byte 2		Byte 3		Byte 4		Byte 5		Byte 6
//   7	U+007F		0xxxxxxx
//  11	U+07FF		110xxxxx	10xxxxxx
//  16	U+FFFF		1110xxxx	10xxxxxx	10xxxxxx
//  21	U+1FFFFF	11110xxx	10xxxxxx	10xxxxxx	10xxxxxx
//  26	U+3FFFFFF	111110xx	10xxxxxx	10xxxxxx	10xxxxxx	10xxxxxx
//  31	U+7FFFFFFF	1111110x	10xxxxxx	10xxxxxx	10xxxxxx	10xxxxxx	10xxxxxx
	public static byte[] toUTF8(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			final int tot = s.length();
			final ByteOutStream buf = new ByteOutStream(tot);
			for (int i = 0; i < tot; i++) {
				final char c = s.charAt(i);
				if (c < 0x80) {
					buf.writeByte(c);
				} else if (c < 0x0800) {
					//2 bytes
					buf.writeByte( 0xC0 | (c >> 6) );
					buf.writeByte( 0x80 | (c & 0x3F) );
				} else {
					//3 bytes
					buf.writeByte( 0xE0 | (c >> 12) );
					buf.writeByte( 0x80 | ((c >> 6) & 0x3F) );
					buf.writeByte( 0x80 | (c & 0x3F) );
				}
			}
			return ((buf.capacity() == buf.size()) ? buf.getBuffer() : buf.toByteArray());
		}
	}
	
	public static String fromUTF8(byte[] buffer) {
		return fromUTF8(buffer, 0, buffer.length);
	}
	
	public static String fromUTF8(byte[] buffer, int offset, int length) {
		try {
			return new String(buffer, offset, length, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			final StringBuffer s = new StringBuffer(length);
			for (int i = offset; i < length; ) {
				final byte b = buffer[i];
				if (b < 0x80) {
					s.append( (char)b );
					i++;
				} else if (b < 0xE0) {
					//2 bytes
					s.append( (char)(((b & 0x1F) << 6) | (buffer[i + 1] & 0x3F)) );
					i += 2;
				} else if (b < 0xF0) {
					//3 bytes
					s.append( (char)(((b & 0x0F) << 12) | ((buffer[i + 1] & 0x3F) << 6) | (buffer[i + 2] & 0x3F)) );
					i += 3;
				} else {
					//upper plane not supported! (can't be returned in a single char)
					throw new IllegalArgumentException("Caractere UTF-8 não suportado");
					//if (b < 0xF8) {
					//	//4 bytes
					//}
					//if (b < 0xFC) {
					//	//5 bytes
					//}
					////6 bytes
				}
			}
			return s.toString();
		}
	}
	
	private static void getHexChar(StringBuffer sb, int c) {
		sb.append('%');
		sb.append(HexChars[(c >> 4) & 0x0F]);
		sb.append(HexChars[c & 0x0F]);
	}
	
	//Similar to the encodeURI from JavaScript: replaces
	//spaces and special chars (except , / ? : @ & = + $ #)
	//into their %XX representation
	public static String encodeURI(String prefixNotToBeEncoded, String uri) {
		final int tot = uri.length();
		final StringBuffer sb = new StringBuffer(tot << 1);
		if (prefixNotToBeEncoded != null) {
			sb.append(prefixNotToBeEncoded);
		}
		for (int i = 0; i < tot; i++) {
			final char c = uri.charAt(i);
			if (c <= 32 || c == 0x25) { //control chars, space or % itself
				getHexChar(sb, c);
			} else if (c < 0x80) {
				sb.append(c);
			} else if (c < 0x0800) {
				//2 bytes
				getHexChar(sb, 0xC0 | (c >> 6) );
				getHexChar(sb, 0x80 | (c & 0x3F) );
			} else {
				//3 bytes
				getHexChar(sb, 0xE0 | (c >> 12) );
				getHexChar(sb, 0x80 | ((c >> 6) & 0x3F) );
				getHexChar(sb, 0x80 | (c & 0x3F) );
			}
		}
		return sb.toString();
	}
}
