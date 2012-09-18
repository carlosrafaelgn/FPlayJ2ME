//
// ListBox.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseControls/ListBox.java
//

package baseControls;

import baseGraphics.Point;
import baseUI.Behaviour;
import baseUI.Control;
import baseUI.ControlContainer;
import baseUI.ControlListener;
import baseUI.Main;
import baseUtil.Iterator;
import baseUtil.Vector;
import javax.microedition.lcdui.Graphics;

public final class ListBox extends Control implements ControlListener {
	private Vector items;
	private final ScrollBar scrollBar;
	private ControlListener listener;
	private boolean nextClickValid, scrollBarFocus, wrapList, processingLongPress, marking, moving;
	private int firstOffset, itemHeight, lastPointerMoveY,
				offsetScrollBar, listWidth,
				currentIndex,
				hilightIndex,
				displayCount, //amount of items which can be displayed on the screen
				firstIndex, //first visible item
				initialMarkIndex,
				firstMarked, //index of the first selected item
				lastMarked; //index of the last item selected (usually == FirstSel)
	private ItemPainter itemPainter;
	
	public static final int EVENT_SELECTIONCHANGED = 0x3000;
	public static final int EVENT_ITEMSELECTED = 0x3001;
	public static final int EVENT_LONGKEY = 0x3002;
	public static final int EVENT_LONGPOINTER = 0x3003;
	
	public ListBox(ControlContainer container, int left, int top, int width, int height) {
		super(container, left, top, width, height, true, true, false);
		scrollBar = new ScrollBar(container, width, top, height);
		scrollBar.setListener(this);
		itemHeight = Main.Customizer.getItemHeight();
		listWidth = width - scrollBar.getWidth();
		firstIndex = 0;
		currentIndex = -1;
		firstMarked = -1;
		lastMarked = -1;
		hilightIndex = -1;
		items = new Vector();
		wrapList = true;
	}

	public final void setMarking(boolean marking) {
		this.marking = marking;
		if (marking) {
			this.moving = false;
			
			if (items.size() == 0) {
				this.marking = false;
				return;
			}
			
			boolean inv = false;
			
			if (currentIndex < 0) {
				currentIndex = 0;
				inv = true;
			}
			
			firstMarked = currentIndex;
			lastMarked = currentIndex;
			
			initialMarkIndex = currentIndex;
			
			if (!ensureVisible(currentIndex) || inv) {
				invalidate();
			}
		}
	}

	public final boolean isMarking() {
		return this.marking;
	}

	public final void setMoving(boolean moving) {
		this.moving = moving;
		if (moving) {
			this.marking = false;
			
			if (items.size() == 0) {
				this.moving = false;
				return;
			}
			
			boolean inv = false;
			
			if (firstMarked < 0 || lastMarked < 0) {
				if (currentIndex < 0) {
					currentIndex = 0;
				}
				
				firstMarked = currentIndex;
				lastMarked = currentIndex;
				
				inv = true;
			}
			
			if (currentIndex < firstMarked) {
				currentIndex = firstMarked;
				inv = true;
			} else if (currentIndex > lastMarked) {
				currentIndex = lastMarked;
				inv = true;
			}
			
			if (!ensureVisible(currentIndex) || inv) {
				invalidate();
			}
		}
	}

	public final boolean isMoving() {
		return this.moving;
	}

	public final void setWrapList(boolean wrapList) {
		this.wrapList = wrapList;
	}

	public final boolean isWrapList() {
		return this.wrapList;
	}

	private final boolean canStartLongPress() {
		return (processingLongPress && !marking && !moving);
	}
	
	public final void setProcessingLongPress(boolean processingLongPress) {
		this.processingLongPress = processingLongPress;
	}

	public final boolean isProcessingLongPress() {
		return this.processingLongPress;
	}
	
	public final ControlListener getListener() {
		return this.listener;
	}
	
	public final void setListener(ControlListener listener) {
		this.listener = listener;
	}

	public final ItemPainter getItemPainter() {
		return this.itemPainter;
	}

	public final void setItemPainter(ItemPainter itemPainter) {
		this.itemPainter = itemPainter;
	}

	public final Object itemAt(int index) {
		return items.elementAt(index);
	}
	
	public final boolean isItemVisible(int index) {
		return ((index < (firstIndex + displayCount)) && (index >= firstIndex)); 
	}
	
	public final void ensureCapacity(int minCapacity) {
		items.ensureCapacity(minCapacity);
	}

	public final int itemCount() {
		return items.size();
	}

	public final Iterator getIterator() {
		return items.getIterator();
	}
	
	public final int indexOfString(String element) {
		for (int i = 0; i < items.size(); i++) {
			if (items.elementAt(i).toString().equals(element)) {
				return i;
			}
		}
		return -1;
	}
	
	public final int indexOfStringIgnoreCase(String element) {
		for (int i = 0; i < items.size(); i++) {
			if (items.elementAt(i).toString().equalsIgnoreCase(element)) {
				return i;
			}
		}
		return -1;
	}
	
	public final int indexOf(Object element) {
		for (int i = 0; i < items.size(); i++) {
			if (items.elementAt(i).equals(element)) {
				return i;
			}
		}
		return -1;
	}
	
	public final void clear() {
		items.removeAllElements();
		items.trimToSize();
		firstOffset = 0;
		firstIndex = 0;
		marking = false;
		moving = false;
		currentIndex = -1;
		firstMarked = -1;
		lastMarked = -1;
		hilightIndex = -1;
		scrollBar.setup(0, 1, 1, false);
		invalidate();
		System.gc();
	}
	
	public final void addItem(Object item) {
		insertItems(new Object[] { item }, items.size(), 1);
	}
	
	public final void addItems(Vector vector) {
		insertItems(vector.getArray(), items.size(), vector.size());
	}
	
	public final void addItems(Object[] items) {
		insertItems(items, this.items.size(), items.length);
	}
	
	public final void addItems(Object[] items, int count) {
		insertItems(items, this.items.size(), count);
	}
	
	public final void insertItem(Object item, int index) {
		insertItems(new Object[] { item }, index, 1);
	}
	
	public final void insertItems(Vector vector, int index) {
		insertItems(vector.getArray(), index, vector.size());
	}
	
	public final void insertItems(Object[] items, int index) {
		insertItems(items, index, items.length);
	}
	
	public final void insertItems(Object[] items, int index, int count) {
		if (marking || moving) return;
		
		if (count > items.length) count = items.length;
		if (count <= 0) return;
		if (index > this.items.size()) index = this.items.size();
		if (index < 0) index = 0;
		
		if (index <= currentIndex) {
			currentIndex += count;
		}
		
		if (index <= hilightIndex) {
			hilightIndex += count;
		}
		
		if (index <= firstMarked) {
			firstMarked += count;
			lastMarked += count;
		} else if (index <= lastMarked) {
			lastMarked += count;
		}
		
		if (firstIndex < 0) firstIndex = 0;
		
		this.items.insertElementsAt(items, index, count);
		
		final int expectedOffset = (firstIndex * itemHeight) - firstOffset;
		scrollBar.setup(expectedOffset, this.items.size() * itemHeight, getHeight(), false);
		final int realOffset = scrollBar.getOffset();
		if (realOffset != expectedOffset) {
			//simulate a scroll event to try not to leave a empty space
			//after the last item
			firstIndex = (realOffset / itemHeight);
			firstOffset = (firstIndex * itemHeight) - realOffset;
			invalidate();
		} else {
			if (isItemVisible(index))
				invalidate();
			else
				scrollBar.invalidate();
		}
	}
	
	public final void removeMarkedItems() {
		if (firstMarked < 0 || lastMarked < 0) return;
		
		removeAt(firstMarked, lastMarked - firstMarked + 1);
	}
	
	public final void removeSelectedItem() {
		removeAt(currentIndex, 1);
	}
	
	public final void removeItem(Object item) {
		removeAt(items.indexOf(item), 1);
	}
	
	public final void removeAt(int index) {
		removeAt(index, 1);
	}
	
	public final void removeAt(int index, int count) {
		if (index >= items.size()) index = items.size() - 1;
		if (index < 0) index = 0;
		if (count > (items.size() - index)) count = (items.size() - index);
		if (count <= 0) return;
		
		boolean changedSelection = false;
		final int last = index + count - 1;

		if (index <= currentIndex) {
			if (last >= currentIndex) {
				changedSelection = true;
				currentIndex = index;
			} else {
				currentIndex -= count;
			}
		}

		if (index <= hilightIndex) {
			if (last >= hilightIndex) {
				hilightIndex = -1;
			} else {
				hilightIndex -= count;
			}
		}
		
		if (firstMarked < 0 || lastMarked < 0 || (index <= firstMarked && last >= lastMarked)) {
			firstMarked = -1;
			lastMarked = -1;
		} else {
			if (index <= firstMarked) {
				if (last >= firstMarked) {
					firstMarked = index;
				} else {
					firstMarked -= count;
				}
			}
			if (index <= lastMarked) {
				if (last >= lastMarked) {
					lastMarked = index - 1;
				} else {
					lastMarked -= count;
				}
			}
			
			if (firstMarked >= items.size()) firstMarked = items.size() - 1;
			if (firstMarked < 0) firstMarked = 0;
			
			if (lastMarked >= items.size()) lastMarked = items.size() - 1;
			if (lastMarked < 0) lastMarked = 0;
			
			if (lastMarked < firstMarked) lastMarked = firstMarked;
		}
		
		items.removeElementsAt(index, count);
		
		if (currentIndex >= items.size()) {
			changedSelection = true;
			currentIndex = items.size() - 1;
		}
		if (currentIndex < 0) {
			changedSelection = true;
			currentIndex = 0;
		}
		
		if (firstIndex >= items.size()) firstIndex = items.size() - 1;
		if (firstIndex < 0) firstIndex = 0;
		
		final int expectedOffset = (firstIndex * itemHeight) - firstOffset;
		scrollBar.setup(expectedOffset, this.items.size() * itemHeight, getHeight(), false);
		final int realOffset = scrollBar.getOffset();
		if (realOffset != expectedOffset) {
			//simulate a scroll event, to try not to leave a empty space
			//after the last item
			firstIndex = (realOffset / itemHeight);
			firstOffset = (firstIndex * itemHeight) - realOffset;
		}
		
		if (this.items.size() == 0) {
			firstIndex = 0;
			currentIndex = -1;
			firstMarked = -1;
			lastMarked = -1;
		}
		
		invalidate();
		
		if (currentIndex >= 0 && currentIndex < this.items.size() && changedSelection) {
			triggerChanged();
		}
	}
	
	public final void replaceContents(Vector vector) {
		if (vector != null && vector.size() > 0) {
			items = vector;
			firstOffset = 0;
			firstIndex = 0;
			marking = false;
			moving = false;
			currentIndex = -1;
			firstMarked = -1;
			lastMarked = -1;
			hilightIndex = -1;
			scrollBar.setup(0, items.size() * itemHeight, getHeight(), false);
		} else {
			clear();
		}
	}
	
	private final boolean ensureVisible(int index) {
		if (index < firstIndex || index >= (firstIndex + displayCount) || (items.size() - firstIndex) < displayCount) {
			//adjust the list in order to display the current item
			//(if the item was already on the screen, just redraw the screen)
			
			//try to place the new item on the middle of the screen
			firstIndex = index - (displayCount >> 1);
			if (items.size() > displayCount) {
				//make sure not to display extra empty space at the end
				if ((items.size() - firstIndex) < displayCount) {
					firstIndex = items.size() - displayCount;
				}
			} else {
				//if there are less items than screen space, first index must be 0
				firstIndex = 0;
			}
			if (firstIndex < 0) {
				firstIndex = 0;
			}
			
			final int expectedOffset = (firstIndex * itemHeight);
			scrollBar.setOffset(expectedOffset, false);
			final int realOffset = scrollBar.getOffset();
			if (realOffset != expectedOffset) {
				//simulate a scroll event, to try not to leave a empty space
				//after the last item
				firstIndex = (realOffset / itemHeight);
				firstOffset = (firstIndex * itemHeight) - realOffset;
			} else {
				firstOffset = 0;
			}
			return false;
		} else if (index == firstIndex && firstOffset < 0) {
			firstOffset = 0;
			scrollBar.setOffset(firstIndex * itemHeight, false);
			return false;
		}
		
		return true;
	}
	
	private final void moveUp() {
		if (items.size() > 0) {
			selectItem((currentIndex > 0) ? (currentIndex - 1) : (items.size() - 1));
			triggerChanged();
		}
	}
	
	private final void moveDown() {
		if (items.size() > 0) {
			selectItem((currentIndex < (items.size() - 1)) ? (currentIndex + 1) : 0);
			triggerChanged();
		}
	}
	
	private final void pageUp() {
		if (items.size() > 0) {
			int s;
			if (currentIndex != 0) {
				s = firstIndex - 1;
				if (s < 0)
					s = 0;
			} else {
				s = items.size() - 1;
			}
			selectItem(s);
			triggerChanged();
		}
	}
	
	private final void pageDown() {
		if (items.size() > 0) {
			int s;
			if (currentIndex != (items.size() - 1)) {
				s = firstIndex + displayCount;
				if (s >= items.size())
					s = items.size() - 1;
			} else {
				s = 0;
			}
			selectItem(s);
			triggerChanged();
		}
	}

	public final int getItemPositionFromIndex(int index) {
		return ((index - firstIndex) * itemHeight) + firstOffset;
	}

	public final int getItemIndexFromPosition(int y) {
		return ((scrollBar.getOffset() + y) / itemHeight);
	}

	public final int getHilightIndex() {
		return hilightIndex;
	}
	
	public final void setHilightIndex(int hilightIndex) {
		if (hilightIndex < items.size() && hilightIndex >= 0) {
			final int old = this.hilightIndex;
			this.hilightIndex = hilightIndex;
			final int min, max;
			if (old < hilightIndex) {
				min = old;
				max = hilightIndex + 1;
			} else {
				min = hilightIndex;
				max = old + 1;
			}
			invalidate(offsetScrollBar, ((min - firstIndex) * itemHeight) + firstOffset, listWidth, (max - min) * itemHeight);
			//if (isItemVisible(old)) {
			//	invalidate(offsetScrollBar, ((old - firstIndex) * itemHeight) + firstOffset, listWidth, itemHeight);
			//}
			//if (isItemVisible(hilightIndex)) {
			//	invalidate(offsetScrollBar, ((hilightIndex - firstIndex) * itemHeight) + firstOffset, listWidth, itemHeight);
			//}
		} else if (this.hilightIndex != -1) {
			this.hilightIndex = -1;
			invalidate();
		}
	}
	
	public final int markedCount() {
		return lastMarked - firstMarked + 1;
	}
	
	public final void clearMarks() {
		if (firstMarked < 0 && lastMarked < 0) return;
		firstMarked = -1;
		lastMarked = -1;
		invalidate();
	}

	public final boolean isMarked(int index) {
		return (index >= firstMarked && index <= lastMarked);
	}
	
	public final int firstMarkedIndex() {
		return firstMarked;
	}
	
	public final Object firstMarkedItem() {
		return (((firstMarked >= 0) && (firstMarked < items.size())) ? items.elementAt(firstMarked) : null);
	}
	
	public final int lastMarkedIndex() {
		return lastMarked;
	}
	
	public final Object lastMarkedItem() {
		return (((lastMarked >= 0) && (lastMarked < items.size())) ? items.elementAt(lastMarked) : null);
	}
	
	private final void changeMarkedItems(int previousIndex) {
		if (currentIndex == initialMarkIndex) {
			firstMarked = currentIndex;
			lastMarked = currentIndex;
		} else if (currentIndex < initialMarkIndex) {
			firstMarked = currentIndex;
			lastMarked = initialMarkIndex;
		} else {
			firstMarked = initialMarkIndex;
			lastMarked = currentIndex;
		}
		invalidate();
	}
	
	private final void moveMarkedItems(int previousIndex) {
		//the algorithm used here is a bit slow, but
		//does not require the creation of an extra buffer
		
		Object tmp;
		int d = 0, srcMarked = firstMarked;
		final Object[] array = items.getArray();
		final int countMarked = lastMarked - firstMarked + 1;
		
		if (currentIndex < firstMarked) {
			//move the items up
			d = currentIndex - firstMarked;
			
			while (srcMarked > currentIndex) {
				tmp = array[srcMarked - 1];
				System.arraycopy(array, srcMarked, array, srcMarked - 1, countMarked);
				srcMarked--;
				array[srcMarked + countMarked] = tmp;
			}
			
			if (hilightIndex >= currentIndex && hilightIndex < firstMarked) {
				hilightIndex += countMarked;
			} else if (hilightIndex >= firstMarked && hilightIndex <= lastMarked) {
				hilightIndex += d;
			}
		} else if (currentIndex > lastMarked) {
			//move the items down
			d = currentIndex - lastMarked;
			
			while ((srcMarked + countMarked) <= currentIndex) {
				tmp = array[srcMarked + countMarked];
				System.arraycopy(array, srcMarked, array, srcMarked + 1, countMarked);
				array[srcMarked] = tmp;
				srcMarked++;
			}
			
			if (hilightIndex <= currentIndex && hilightIndex > lastMarked) {
				hilightIndex -= countMarked;
			} else if (hilightIndex >= firstMarked && hilightIndex <= lastMarked) {
				hilightIndex += d;
			}
		} else {
			//don't change any items
			return;
		}
		
		firstMarked += d;
		lastMarked += d;
		
		invalidate();
	}
	
	public final boolean isSelected(int index) {
		return (index == currentIndex);
	}
	
	public final int selectedIndex() {
		return currentIndex;
	}
	
	public final Object selectedItem() {
		return (((currentIndex >= 0) && (currentIndex < items.size())) ? items.elementAt(currentIndex) : null);
	}
	
	public final void selectItem(int index) {
		if (index < items.size() && index >= 0) {
			final int old = currentIndex;
			final int min, max;
			if (old < index) {
				min = old;
				max = index + 1;
			} else {
				min = index;
				max = old + 1;
			}
			
			currentIndex = index;
			
			if (ensureVisible(index)) {
				invalidate(offsetScrollBar, ((min - firstIndex) * itemHeight) + firstOffset, listWidth, (max - min) * itemHeight);
			} else {
				invalidate();
			}
			
			if (marking) {
				changeMarkedItems(old);
			} else if (moving) {
				moveMarkedItems(old);
			}
		}
	}
	
	public final void selectItemAndHilight(int index) {
		setHilightIndex(index);
		selectItem(index);
	}
	
	private final void selectItemByPointer(int index) {
		if (index < items.size() && index >= 0) {
			final int old = currentIndex;
			currentIndex = index;
			
			if (index == firstIndex && firstOffset != 0) {
				//if the item being selected is the first on the screen, check if
				//there is enough space to show it
				firstOffset = 0;
				scrollBar.setOffset(index * itemHeight, false);
				invalidate();
			} else if ((index >= (firstIndex + displayCount)) &&
					(((index - firstIndex) * itemHeight) + firstOffset + itemHeight) > getHeight()
					) {
				//if the item being selected is the last on the screen, check if
				//there is enough space to show it
				scrollBar.setOffset(((index + 1) * itemHeight) - getHeight(), false);
				eventControl(scrollBar, ScrollBar.EVENT_CHANGED, scrollBar.getOffset(), null);
			} else {
				invalidate(offsetScrollBar, ((Math.min(old, index) - firstIndex) * itemHeight) + firstOffset, listWidth, (Math.abs(old - index) + 1) * itemHeight);
			}
			
			if (marking) {
				changeMarkedItems(old);
			} else if (moving) {
				moveMarkedItems(old);
			}
		}
	}
	
	public final void move(int x, int y, boolean repaint) {
		scrollBar.move(x + (Main.environmentIsRightHanded() ? listWidth : 0), y, false);
		
		super.move(x, y, repaint);
	}
	
	public final void resize(int width, int height, boolean repaint) {
		super.resize(width, height, false);
		
		final int sbw = scrollBar.getWidth();
		listWidth = width - sbw;
		displayCount = height / itemHeight;
		offsetScrollBar = (Main.environmentIsRightHanded() ? 0 : sbw);
		scrollBar.reposition(getLeft() + (Main.environmentIsRightHanded() ? listWidth : 0), getTop(), 0, height, false);
		
		//repeat part of the code from ensureVisible...
		final int expectedOffset = scrollBar.getOffset();
		
		scrollBar.setup(expectedOffset, items.size() * itemHeight, height, false);
		
		final int realOffset = scrollBar.getOffset();
		if (realOffset != expectedOffset) {
			//simulate a scroll event, to try not to leave a empty space
			//after the last item
			firstIndex = (realOffset / itemHeight);
			firstOffset = (firstIndex * itemHeight) - realOffset;
		}
		
		if (currentIndex >= 0) {
			ensureVisible(currentIndex);
		}
		
		if (repaint) {
			invalidate();
		}
	}
	
	public final void eventControl(Control control, int eventId, int eventArg1, Object eventArg2) {
		if (eventId == ScrollBar.EVENT_CHANGED) {
			firstIndex = (eventArg1 / itemHeight);
			firstOffset = (firstIndex * itemHeight) - eventArg1;
			invalidate();
		}
	}
	
	private final void triggerChanged() {
		if (listener != null) {
			listener.eventControl(this, EVENT_SELECTIONCHANGED, currentIndex, items.elementAt(currentIndex));
		}
	}
	
	private final void triggerSelected() {
		if (currentIndex >= 0 && currentIndex < items.size() && listener != null) {
			listener.eventControl(this, EVENT_ITEMSELECTED, currentIndex, items.elementAt(currentIndex));
		}
	}
	
	protected final boolean eventKeyPress(int keyCode, int repeatCount) {
		if (keyCode == Main.KeyDown) {
			if (!wrapList && currentIndex == (items.size() - 1))
				return false;
			moveDown();
		} else if (keyCode == Main.KeyUp) {
			if (!wrapList && currentIndex == 0)
				return false;
			moveUp();
		} else if (keyCode == Main.KeyOK) {
			//test twice (processingLongPress and canStartLongPress)
			//just to make the response consistent
			if (processingLongPress) {
				if (canStartLongPress() && currentIndex >= 0 && currentIndex < items.size())
					longPressProcessStart(keyCode, itemHeight >> 1, getItemPositionFromIndex(currentIndex) + (itemHeight >> 1));
			} else {
				triggerSelected();
			}
		} else if (keyCode == Main.KeyPgDn) {
			pageDown();
		} else if (keyCode == Main.KeyPgUp) {
			pageUp();
		} else {
			return false;
		}
		return true;
	}
	
	protected final void eventKeyRelease(int keyCode) {
		if (processingLongPress && keyCode == Main.KeyOK) {
			//the long press did not succeeded
			triggerSelected();
		}
	}	
	
	protected void eventLongPress(boolean byKey, int keyCode, int x, int y) {
		//return true to suppress the key up event (if byKey == true)
		if (listener != null) {
			if (byKey)
				listener.eventControl(this, EVENT_LONGKEY, keyCode, new Point(x, y));
			else
				listener.eventControl(this, EVENT_LONGPOINTER, Integer.MIN_VALUE, new Point(x, y));
		}
	}
	
	protected final void eventPointerDown(int x, int y) {
		if ((x >= (scrollBar.getLeft() - 16)) &&
			(x < (scrollBar.getRight() + 16))) {
			scrollBarFocus = true;
			scrollBar.eventPointerDown(0, y);
		} else {
			nextClickValid = false;
			final int itemIdx = getItemIndexFromPosition(y);
			if (itemIdx >= 0 && itemIdx < items.size()) {
				Main.environmentDoTouchFeedback();
				if (itemIdx != currentIndex) {
					selectItemByPointer(itemIdx);
					triggerChanged();
				} else {
					nextClickValid = true;
				}
				if (canStartLongPress())
					longPressProcessStart(x, y);
			}
		}
	}
	
	protected final boolean eventPointerMove(int x, int y) {
		if (scrollBarFocus) {
			scrollBar.eventPointerMove(0, y);
		} else if (Main.pointerExceededThreshold()) {
			if (Main.pointerExceededThresholdForTheFirstTime()) {
				//start simulating the scrolling process
				nextClickValid = false;
				if (processingLongPress)
					longPressProcessAbort();
				lastPointerMoveY = y;
			} else {
				//scroll the screen
				final int off = scrollBar.getOffset();
				//scrolling here must be upside-down in order to feel natural
				scrollBar.setOffset(off - (y - lastPointerMoveY), false);
				if (scrollBar.getOffset() != off) {
					lastPointerMoveY = y;
					eventControl(scrollBar, ScrollBar.EVENT_CHANGED, scrollBar.getOffset(), null);
				}
			}
		}
		return true;
	}
	
	protected final void eventPointerUp(int x, int y, boolean isValid) {
		if (scrollBarFocus) {
			scrollBar.eventPointerUp(0, y, isValid);
			scrollBarFocus = false;
		} else if (nextClickValid) {
			nextClickValid = false;
			if (isValid)
				triggerSelected();
		}
	}
	
	protected final void eventEnvironment(int changedFlags) {
		if ((changedFlags & Behaviour.ENV_FONTSIZE) != 0) {
			itemHeight = Main.Customizer.getItemHeight();
			resize(getWidth(), getHeight(), false);
			final int realOffset = scrollBar.getOffset();
			//sometimes, these lines are not executed in
			//the resize event (when there are too many 
			//items in the list)
			firstIndex = (realOffset / itemHeight);
			firstOffset = (firstIndex * itemHeight) - realOffset;
		}
	}
	
	protected final void paint(Graphics g, int screenOffsetX, int screenOffsetY, int clipX, int clipY, int clipWidth, int clipHeight) {
		final int cx = clipX - screenOffsetX;
		final int cr = cx + clipWidth;
		final int fy = Main.FontUI.y;
		final int bottom = clipY + clipHeight;
		if (cx < scrollBar.getLeft() || cr > scrollBar.getRight()) {
			Main.FontUI.select(g);
			int cy = screenOffsetY + firstOffset;
			int by = cy + itemHeight;
			if (items.size() > 0) {
				for (int i = firstIndex; i < items.size(); ++i) {
					final boolean h = (hilightIndex == i);
					if (cy >= bottom)
						break;
					if (clipY < by && cy <= bottom) {
						if (itemPainter == null) {
							if (isSelected(i)) {
								Main.Customizer.paintItem(g, false, isFocused(), h, screenOffsetX + offsetScrollBar, cy, listWidth);
								g.setColor(Main.Customizer.getItemTextColor(isFocused(), h));
							} else if (isMarked(i)) {
								Main.Customizer.paintItem(g, false, false, h, screenOffsetX + offsetScrollBar, cy, listWidth);
								g.setColor(Main.Customizer.getItemTextColor(false, h));
							} else {
								g.setColor(Behaviour.ColorWindow);
								g.fillRect(clipX, cy, clipWidth, itemHeight);
								g.setColor(Main.Customizer.getItemTextColor(false, h));
							}
							g.drawString(items.elementAt(i).toString(), screenOffsetX + 1 + offsetScrollBar, cy + fy, 0);
						} else {
							itemPainter.paintItem(g, i, items.elementAt(i), isSelected(i), screenOffsetX + offsetScrollBar, cy, listWidth, itemHeight, screenOffsetX + 1 + offsetScrollBar, cy + fy);
						}
					}
					cy += itemHeight;
					by += itemHeight;
				}
			} else {
				g.setColor(Behaviour.ColorWindow);
				g.fillRect(clipX, clipY, clipWidth, clipHeight);
				g.setColor(Behaviour.ColorHilight);
				g.drawString("[Vazio]", screenOffsetX + offsetScrollBar + (listWidth >> 1) - (Main.FontUI.stringWidth("[Vazio]") >> 1), cy + (getHeight() >> 1) - (itemHeight >> 1) + fy, 0);
				cy = bottom;
			}
			if (cy < bottom) {
				g.setColor(Behaviour.ColorWindow);
				g.fillRect(clipX, cy, clipWidth, bottom - cy);
			}
		}
		
		scrollBar.paint(g, screenOffsetX + scrollBar.getLeft(), screenOffsetY, clipX, clipY, clipWidth, clipHeight);
		
		if (processingLongPress)
			drawLongPressClue(g, screenOffsetX, screenOffsetY);
	}
}
