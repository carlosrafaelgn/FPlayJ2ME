//
// BrowserItem.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/ui/BrowserItem.java
//

package ui;

import baseUtil.Sortable;

public final class BrowserItem implements Sortable {
	public final String Name;
	public final boolean Folder;
	private final String lname;

	public BrowserItem (String name, boolean folder) {
		if (folder) {
			if (name.length() == 0) {
				Name = "/";
			} else if (name.length() == 1) {
				Name = name;
			} else {
				Name = ((name.charAt(name.length() - 1) == '/') ? name.substring(0, name.length() - 1) : name);
			}
		} else {
			Name = name;
		}
		Folder = folder;
		lname = Name.toLowerCase();
	}

	public BrowserItem () {
		Name = "[Acima...]";
		Folder = false;
		lname = Name;
	}

	public String toString() {
		return Name;
	}

	public String getItemFullPath(String currentPath) {
		if (currentPath == null || currentPath.length() == 0)
			currentPath = "";
		else if (currentPath.charAt(currentPath.length() - 1) != '/')
			currentPath += "/";
		
		if (!Folder)
			return ((currentPath.length() == 0) ? "/" : currentPath) + Name;
		
		if (Name.length() == 0) {
			return ((currentPath.length() < 1) ? "/" : currentPath);
		} else {
			if (currentPath.length() == 0)
				currentPath = Name;
			else
				currentPath += Name;
			
			if (currentPath.charAt(currentPath.length() - 1) != '/')
				currentPath += "/";
			
			return currentPath;
		}
	}
	
	public int compare(Sortable item) {
		final BrowserItem e = (BrowserItem)item;
		if (Folder == e.Folder) return lname.compareTo(e.lname);
		if (Folder && !e.Folder) return -1;
		return 1;
	}
}
