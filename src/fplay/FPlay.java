//
// FPlay.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/fplay/FPlay.java
//

package fplay;

/*import baseIO.ByteInStream;
import baseIO.ByteOutStream;
import baseUtil.Map;*/

public final class FPlay extends javax.microedition.midlet.MIDlet {
	/*private void test(Map m) {
		if (m.getInt(0) != 0)
			throw new RuntimeException();
		if (m.getInt(1) != 10)
			throw new RuntimeException();
		if (m.getInt(2) != -20)
			throw new RuntimeException();
		if (m.getBoolean(3) != false)
			throw new RuntimeException();
		if (m.getBoolean(4) != true)
			throw new RuntimeException();
		if (m.getChar(5) != (char)0xF000)
			throw new RuntimeException();
		if (m.getLong(6) != 0xabcdef09)
			throw new RuntimeException();
		if (!m.getString(7).equals("Sete"))
			throw new RuntimeException();
		if (!m.getString(8).equals("oito"))
			throw new RuntimeException();
	}*/
	
	protected void startApp() {
		/*Map m = new Map();
		m.putInt(0, 0);
		m.putInt(1, 10);
		m.putInt(2, -20);
		m.putBoolean(3, false);
		m.putBoolean(4, true);
		m.putChar(5, (char)0xF000);
		m.putLong(6, 0xabcdef09);
		m.putString(7, "Sete");
		m.putBytes(8, "oito".getBytes());
		test(m);
		ByteOutStream o = new ByteOutStream(1024);
		test(m);
		m.write(o);
		test(m);
		ByteInStream i = new ByteInStream(o);
		m = new Map(i);
		test(m);*/
		baseUI.Main.createShow(this, new ui.Behaviour());
	}
	
	protected void pauseApp() {
	}

	protected void destroyApp(boolean unconditional) {
		baseUI.Main.terminate();
	}
}
