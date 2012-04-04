//
// Rectangle.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseGraphics/Rectangle.java
//

package baseGraphics;

public class Rectangle {
	public int left, top, width, height;
	
	public Rectangle() { }
	
	public Rectangle(int left, int top, int width, int height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}
	
	public Rectangle clone() {
		return new Rectangle(left, top, width, height);
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof Rectangle) {
			final Rectangle r = (Rectangle)obj;
			return (r.left == left) && (r.top == top) && (r.width == width) && (r.height == height);
		}
		return false;
	}
	
	public int hashCode() {
		return left ^ (top << 8) ^ (width << 16) ^ (height << 24);
	}
	
	public int right() {
		return left + width;
	}
	
	public int bottom() {
		return top + height;
	}
	
	public void set(int left, int top, int width, int height) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}
	
	public void setLocation(int left, int top) {
		this.left = left;
		this.top = top;
	}
	
	public void setDimension(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public boolean containsPoint(int x, int y) {
		return (this.left <= x) && (this.top <= y) && ((this.left + this.width) > x) && ((this.top + this.height) > y);
	}
	
	public boolean containsPoint(Point pt) {
		return (left <= pt.x) && (top <= pt.y) && ((left + width) > pt.x) && ((top + height) > pt.y);
	}
	
	public boolean intersectsRectangle(int left, int top, int width, int height) {
		return (this.left < (left + width)) && (this.top < (top + height)) && (left < (this.left + this.width)) && (top < (this.top + this.height));
	}
	
	public boolean intersectsRectangle(Rectangle rectangle) {
		return (left < (rectangle.left + rectangle.width)) && (top < (rectangle.top + rectangle.height)) && (rectangle.left < (left + width)) && (rectangle.top < (top + height));
	}
	
	public void union(Rectangle rectangle) {
		final int r = Math.max(right(), rectangle.right()), b = Math.max(bottom(), rectangle.bottom());
		if (rectangle.left < left) left = rectangle.left;
		if (rectangle.top < top) top = rectangle.top;
		width = r - left;
		height = b - top;
	}
	
	public void intersection(int left, int top, int width, int height) {
		final int r = Math.min(this.right(), left + width), b = Math.min(this.bottom(), top + height);
		if (left > this.left) this.left = left;
		if (top > this.top) this.top = top;
		this.width = r - this.left;
		this.height = b - this.top;
		if (this.width <= 0 || this.height <= 0) {
			this.left = 0;
			this.top = 0;
			this.width = 0;
			this.height = 0;
		}
	}
	
	public void intersection(Rectangle rectangle) {
		final int r = Math.min(right(), rectangle.right()), b = Math.min(bottom(), rectangle.bottom());
		if (rectangle.left > left) left = rectangle.left;
		if (rectangle.top > top) top = rectangle.top;
		width = r - left;
		height = b - top;
		if (width <= 0 || height <= 0) {
			left = 0;
			top = 0;
			width = 0;
			height = 0;
		}
	}
}
