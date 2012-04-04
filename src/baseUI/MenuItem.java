//
// MenuItem.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUI/MenuItem.java
//

package baseUI;

import javax.microedition.lcdui.Image;

public final class MenuItem {
	private final String text;
	private final boolean checked;
	private final int id;
	private final MenuItem[] subItems;
	private final Image image;
	private int index;
	
	public MenuItem(String text, int id) {
		this(text, false, id, null, null);
	}
	
	public MenuItem(String text, int id, Image image) {
		this(text, false, id, null, image);
	}
	
	public MenuItem(String text, boolean checked, int id) {
		this(text, checked, id, null, null);
	}
	
	public MenuItem(String text, int subMenuId, MenuItem[] subItems) {
		this(text, false, subMenuId, subItems, null);
	}
	
	public MenuItem(String text, int subMenuId, MenuItem[] subItems, Image image) {
		this(text, false, subMenuId, subItems, image);
	}
	
	private MenuItem(String text, boolean checked, int id, MenuItem[] subItems, Image image) {
		this.text = text;
		this.checked = checked;
		this.id = id;
		this.subItems = subItems;
		this.image = image;
	}
	
	public final boolean hasSubItems() {
		return (subItems != null);
	}
	
	public final String getText() {
		return text;
	}
	
	public final boolean getChecked() {
		return checked;
	}
	
	public final int getId() {
		return id;
	}
	
	public final MenuItem[] getSubItems() {
		return subItems;
	}
	
	public final int getIndex() {
		return index;
	}
	
	final void setIndex(int index) {
		this.index = index;
	}
	
	public final Image getImage() {
		return image;
	}
}
