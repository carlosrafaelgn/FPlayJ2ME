//
// BitConverter.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseIO/BitConverter.java
//

package baseIO;

public final class BitConverter {
	private BitConverter() { }
	
	public static int byteToUByte(byte[] buffer, int index) {
		return (((int)buffer[index]) & 0xFF);
	}
	
	public static int byteToUShort(byte[] buffer, int index) {
		return (((int)buffer[index]) & 0xFF)
			| ((((int)buffer[index+1]) & 0xFF) << 8);
	}
	
	public static int byteToUInt3(byte[] buffer, int index) {
		return (((int)buffer[index]) & 0xFF)
			| ((((int)buffer[index+1]) & 0xFF) << 8)
			| ((((int)buffer[index+2]) & 0xFF) << 16);
	}
	
	public static int byteToShort(byte[] buffer, int index) {
		return (((int)buffer[index]) & 0xFF)
			| (((int)buffer[index+1]) << 8);
	}
	
	public static int byteToInt3(byte[] buffer, int index) {
		return (((int)buffer[index]) & 0xFF)
			| ((((int)buffer[index+1]) & 0xFF) << 8)
			| (((int)buffer[index+2]) << 16);
	}
	
	public static int byteToInt(byte[] buffer, int index) {
		return (((int)buffer[index]) & 0xFF)
			| ((((int)buffer[index+1]) & 0xFF) << 8)
			| ((((int)buffer[index+2]) & 0xFF) << 16)
			| (((int)buffer[index+3]) << 24);
	}

	public static long byteToLong(byte[] buffer, int index) {
		return (((long)buffer[index]) & 0xFF)
			 | ((((long)buffer[index+1]) & 0xFF) << 8)
			 | ((((long)buffer[index+2]) & 0xFF) << 16)
			 | ((((long)buffer[index+3]) & 0xFF) << 24)
			 | ((((long)buffer[index+4]) & 0xFF) << 32)
			 | ((((long)buffer[index+5]) & 0xFF) << 40)
			 | ((((long)buffer[index+6]) & 0xFF) << 48)
			 | ((((long)buffer[index+7]) & 0xFF) << 56);
	}
	
	public static void shortToByte(byte[] buffer, int index, int x) {
		buffer[index] = (byte)x;
		buffer[index+1] = (byte)(x >> 8);
	}
	
	public static byte[] shortToByte(int x) {
		return new byte[] {
		(byte)x,
		(byte)(x >> 8) };
	}
	
	public static void int3ToByte(byte[] buffer, int index, int x) {
		buffer[index] = (byte)x;
		buffer[index+1] = (byte)(x >> 8);
		buffer[index+2] = (byte)(x >> 16);
	}
	
	public static byte[] int3ToByte(int x) {
		return new byte[] {
		(byte)x,
		(byte)(x >> 8),
		(byte)(x >> 16) };
	}
	
	public static void intToByte(byte[] buffer, int index, int x) {
		buffer[index] = (byte)x;
		buffer[index+1] = (byte)(x >> 8);
		buffer[index+2] = (byte)(x >> 16);
		buffer[index+3] = (byte)(x >> 24);
	}
	
	public static byte[] intToByte(int x) {
		return new byte[] {
		(byte)x,
		(byte)(x >> 8),
		(byte)(x >> 16),
		(byte)(x >> 24) };
	}
	
	public static void longToByte(byte[] buffer, int index, long x) {
		buffer[index] = (byte)x;
		buffer[index+1] = (byte)(x >> 8);
		buffer[index+2] = (byte)(x >> 16);
		buffer[index+3] = (byte)(x >> 24);
		buffer[index+4] = (byte)(x >> 32);
		buffer[index+5] = (byte)(x >> 40);
		buffer[index+6] = (byte)(x >> 48);
		buffer[index+7] = (byte)(x >> 56);
	}
	
	public static byte[] longToByte(long x) {
		return new byte[] {
		(byte)x,
		(byte)(x >> 8),
		(byte)(x >> 16),
		(byte)(x >> 24),
		(byte)(x >> 32),
		(byte)(x >> 40),
		(byte)(x >> 48),
		(byte)(x >> 56) };
	}
}
