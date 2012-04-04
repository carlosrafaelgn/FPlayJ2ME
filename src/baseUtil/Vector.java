//
// Vector.java is distributed under the FreeBSD License
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
// https://raw.github.com/carlosrafaelgn/FPlay/src/baseUtil/Vector.java
//

package baseUtil;

public final class Vector extends java.util.Vector {
	public Vector() {
		super();
	}
	
	public Vector(int initialCapacity) {
		super(initialCapacity);
	}

	public Vector(int initialCapacity, int capacityIncrement) {
		super(initialCapacity, capacityIncrement);
	}
	
	public Vector(Object[] objs) {
		super(objs.length);
		addElements(objs);
	}

	public Vector(Object[] objs, int count) {
		super(count);
		addElements(objs, count);
	}

	public Vector(java.util.Vector vector) {
		super(vector.size());
		vector.copyInto(elementData);
		elementCount += vector.size();
	}
	
	public final Object[] getArray() {
		return elementData;
	}
	
	public final Iterator getIterator() {
		return new Iterator() {
			private int i;
			private final int tot;
			{ tot = size(); }
			public void remove() {
			}
			public Object next() {
				return (i < tot) ? elementData[i++] : null;
			}
			public boolean hasNext() {
				return (i < tot);
			}
		};
	}

	public final int indexOfReference(Object elem) {
		for (int i = 0; i < elementCount; ++i) {
			if (elementData[i] == elem) return i;
		}
		return -1;
	}
	
	public final void addElements(Object[] objs) {
		addElements(objs, objs.length);
	}
	
	public final void addElements(Object[] objs, int count) {
		if (count > objs.length) count = objs.length;
		if (count <= 0) return;
		ensureCapacity(elementCount + count);
		System.arraycopy(objs, 0, elementData, elementCount, count);
		elementCount += count;
	}
	
	public final void insertElementsAt(Object[] objs, int index) {
		insertElementsAt(objs, index, objs.length);
	}
	
	public final void insertElementsAt(Object[] objs, int index, int count) {
		if (count > objs.length) count = objs.length;
		if (count <= 0) return;
		if (index > elementCount) index = elementCount;
		if (index < 0) index = 0;
		ensureCapacity(elementCount + count);
		System.arraycopy(elementData, index, elementData, index + count, elementCount - index);
		System.arraycopy(objs, 0, elementData, index, count);
		elementCount += count;
	}
	
	public final void removeElementsAt(int index, int count) {
		if (index >= elementCount) index = elementCount - 1;
		if (index < 0) index = 0;
		if (count > (elementCount - index)) count = (elementCount - index);
		if (count <= 0) return;
		System.arraycopy(elementData, index + count, elementData, index, elementCount - (index + count));
		elementCount -= count;
	}
	
	private static void mergeText(Object[] elements, int iA, int iB, int endB) {
		String sA = elements[iA].toString();
		Object eB = elements[iB];
		String sB = eB.toString();
		for (;;) {
			if (sB.compareTo(sA) < 0) {
				System.arraycopy(elements, iA, elements, iA + 1, iB - iA);
				elements[iA] = eB;
				if ((++iB) >= endB || (++iA) >= iB) break; //MUST INCREMENT iB BEFORE COMPARING iA and iB
				eB = elements[iB];
				sB = eB.toString();
			} else {
				if ((++iA) >= iB) break;
				sA = elements[iA].toString();
			}
		}
	}
	
	private static void mergeSortText(Object[] elements, int i, int n) {
		if (n > 1) {
			final int m = n >> 1;
			mergeSortText(elements, i, m);
			mergeSortText(elements, i + m, n - m);
			mergeText(elements, i, i + m, i + n);
		}
	}
	
	private static void merge(Object[] elements, int iA, int iB, int endB) {
		Sortable eA = (Sortable)elements[iA];
		Sortable eB = (Sortable)elements[iB];
		for (;;) {
			if (eB.compare(eA) < 0) {
				System.arraycopy(elements, iA, elements, iA + 1, iB - iA);
				elements[iA] = eB;
				if ((++iB) >= endB || (++iA) >= iB) break; //MUST INCREMENT iB BEFORE COMPARING iA and iB
				eB = (Sortable)(elements[iB]);
			} else {
				if ((++iA) >= iB) break;
				eA = (Sortable)elements[iA];
			}
		}
	}
	
	private static void mergeSort(Object[] elements, int i, int n) {
		if (n > 1) {
			final int m = n >> 1;
			mergeSort(elements, i, m);
			mergeSort(elements, i + m, n - m);
			merge(elements, i, i + m, i + n);
		}
	}
	
	private static void merge(Object[] elements, int iA, int iB, int endB, Sorter sorter) {
		do {
			if (sorter.compare(elements[iB], elements[iA]) < 0) {
				//the item in B is smaller
				final Object t = elements[iB];
				System.arraycopy(elements, iA, elements, iA + 1, iB - iA);
				elements[iA] = t;
				if ((++iB) >= endB) break;
			}
		} while ((++iA) < iB);
	}
	
	private static void mergeSort(Object[] elements, int i, int n, Sorter sorter) {
		if (n > 1) {
			final int m = n >> 1;
			mergeSort(elements, i, m, sorter);
			mergeSort(elements, i + m, n - m, sorter);
			merge(elements, i, i + m, i + n, sorter);
		}
	}
	
	public final void sort() {
		mergeSort(elementData, 0, elementCount);
	}
	
	public final void sortText() {
		mergeSortText(elementData, 0, elementCount);
	}
	
	public final void sort(Sorter sorter) {
		mergeSort(elementData, 0, elementCount, sorter);
	}
	
	public static Sortable[] sortArray(Sortable[] array, int index, int count) {
		mergeSort(array, index, count);
		return array;
	}
	
	public static Object[] sortArray(Object[] array, int index, int count, Sorter sorter) {
		mergeSort(array, index, count, sorter);
		return array;
	}
	
	public static Object[] sortArrayText(Object[] array, int index, int count) {
		mergeSortText(array, index, count);
		return array;
	}
}
