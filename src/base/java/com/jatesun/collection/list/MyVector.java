package com.jatesun.collection.list;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Vector;
//import java.util.Vector.Itr;
//import java.util.Vector.ListItr;
//import java.util.Vector.VectorSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * 
 * @author jatesun
 * @description 数组实现的线程安全list 1、删除方法基本上核心为removeElementAt();
 *              2、根arraylist的区别主要为有synchronized关键字。
 *              3、synchronized关键字作用为保证每个时间点只有一个线程访问，互斥锁。详见博客
 * @param <E>
 */
public class MyVector<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

	protected Object[] elementData;

	protected int elementCount;

	protected int capacityIncrement;// 指定的扩容大小（扩容时如果此值大于零赋值）

	private static final long serialVersionUID = -2767605614048989439L;

	public MyVector(int initialCapacity, int capacityIncrement) {
		super();// 先完成父类初始化工作
		// 校验参数
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		this.elementData = new Object[initialCapacity];// 初始化数组
		this.capacityIncrement = capacityIncrement;
	}

	public MyVector(int initialCapacity) {
		this(initialCapacity, 0);
	}

	public MyVector() {
		this(10);
	}

	public MyVector(Collection<? extends E> c) {
		elementData = c.toArray();
		elementCount = elementData.length;
		// c.toArray might (incorrectly) not return Object[] (see 6260652)
		if (elementData.getClass() != Object[].class)
			elementData = Arrays.copyOf(elementData, elementCount, Object[].class);
	}

	// 拷贝元素进一个数组
	public synchronized void copyInto(Object[] anArray) {
		System.arraycopy(elementData, 0, anArray, 0, elementCount);
	}

	// 去除额外元素
	public synchronized void trimToSize() {
		modCount++;
		int oldCapacity = elementData.length;
		if (elementCount < oldCapacity) {
			elementData = Arrays.copyOf(elementData, elementCount);
		}
	}

	// TODO 没有返回如何确定？而且min<0失效？
	public synchronized void ensureCapacity(int minCapacity) {
		if (minCapacity > 0) {
			modCount++;
			ensureCapacityHelper(minCapacity);
		}
	}

	private void ensureCapacityHelper(int minCapacity) {
		if (minCapacity - elementData.length > 0)// 注意这里没有用elementcount（格式没有大括号=_=）
			grow(minCapacity);
	}

	// 最近声明原则，变量声明离变量使用越近越好
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	// 扩容方法
	private void grow(int minCapacity) {
		int oldCapacity = elementData.length;
		// 计算新容量
		int newCapacity = oldCapacity + ((capacityIncrement > 0) ? capacityIncrement : oldCapacity);
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;// 还不够直接赋值为参数
		if (newCapacity - MAX_ARRAY_SIZE > 0)
			// 特大数组方法
			newCapacity = hugeCapacity(minCapacity);
		elementData = Arrays.copyOf(elementData, newCapacity);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError();
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	// newsize如果大于elecount就扩容，小于就把多余的元素置null
	public synchronized void setSize(int newSize) {
		modCount++;
		if (newSize > elementCount) {
			ensureCapacityHelper(newSize);
		} else {
			for (int i = newSize; i < elementCount; i++) {
				elementData[i] = null;
			}
		}
		elementCount = newSize;
	}

	public synchronized int capacity() {
		return elementData.length;// 容量
	}

	public synchronized int size() {
		return elementCount;// 大小，与上面的方法有所区别
	}

	public synchronized boolean isEmpty() {
		return elementCount == 0;
	}

	public Enumeration<E> elements() {
		return new Enumeration<E>() {
			int count = 0;

			public boolean hasMoreElements() {
				return count < elementCount;
			}

			public E nextElement() {
				synchronized (MyVector.this) {
					if (count < elementCount) {
						return elementData(count++);
					}
				}
				throw new NoSuchElementException("Vector Enumeration");
			}
		};
	}

	public boolean contains(Object o) {
		return indexOf(o, 0) >= 0;
	}

	public int indexOf(Object o) {
		return indexOf(o, 0);
	}

	// 线程安全的indexof方法
	public synchronized int indexOf(Object o, int index) {
		if (o == null) {
			for (int i = index; i < elementCount; i++)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = index; i < elementCount; i++)
				if (o.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	public synchronized int lastIndexOf(Object o) {
		return lastIndexOf(o, elementCount - 1);
	}

	public synchronized int lastIndexOf(Object o, int index) {
		if (index >= elementCount)
			throw new IndexOutOfBoundsException(index + " >= " + elementCount);

		if (o == null) {
			for (int i = index; i >= 0; i--)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = index; i >= 0; i--)
				if (o.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	public synchronized E elementAt(int index) {
		if (index >= elementCount) {
			throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
		}

		return elementData(index);
	}

	public synchronized E firstElement() {
		if (elementCount == 0) {
			throw new NoSuchElementException();
		}
		return elementData(0);
	}

	public synchronized E lastElement() {
		if (elementCount == 0) {
			throw new NoSuchElementException();
		}
		return elementData(elementCount - 1);
	}

	public synchronized void setElementAt(E obj, int index) {
		if (index >= elementCount) {
			throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
		}
		elementData[index] = obj;
	}

	// 删除操作，校验参数，过了index后面的数组复制到前面，最后一个元素置null
	public synchronized void removeElementAt(int index) {
		modCount++;
		if (index >= elementCount) {
			throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
		} else if (index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		int j = elementCount - index - 1;
		if (j > 0) {
			System.arraycopy(elementData, index + 1, elementData, index, j);
		}
		elementCount--;
		elementData[elementCount] = null; /*
											 * to let gc do its work
											 * 注意这里置null，否则会产生内存泄漏
											 */
	}

	// 插入操作
	public synchronized void insertElementAt(E obj, int index) {
		modCount++;
		if (index > elementCount) {
			throw new ArrayIndexOutOfBoundsException(index + " > " + elementCount);
		}
		ensureCapacityHelper(elementCount + 1);
		System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
		elementData[index] = obj;
		elementCount++;
	}

	public synchronized void addElement(E obj) {
		modCount++;
		ensureCapacityHelper(elementCount + 1);
		elementData[elementCount++] = obj;
	}

	public synchronized boolean removeElement(Object obj) {
		modCount++;
		int i = indexOf(obj);
		if (i >= 0) {
			removeElementAt(i);
			return true;
		}
		return false;
	}

	public synchronized void removeAllElements() {
		modCount++;
		// Let gc do its work
		for (int i = 0; i < elementCount; i++)
			elementData[i] = null;

		elementCount = 0;
	}

	public synchronized Object clone() {
		try {
			@SuppressWarnings("unchecked")
			MyVector<E> v = (MyVector<E>) super.clone();
			v.elementData = Arrays.copyOf(elementData, elementCount);
			v.modCount = 0;
			return v;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError(e);
		}
	}

	public synchronized Object[] toArray() {
		return Arrays.copyOf(elementData, elementCount);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T[] toArray(T[] a) {
		if (a.length < elementCount)
			return (T[]) Arrays.copyOf(elementData, elementCount, a.getClass());

		System.arraycopy(elementData, 0, a, 0, elementCount);

		if (a.length > elementCount)
			a[elementCount] = null;

		return a;
	}

	@SuppressWarnings("unchecked")
	E elementData(int index) {
		return (E) elementData[index];
	}

	public synchronized E get(int index) {
		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index);

		return elementData(index);
	}

	public synchronized E set(int index, E element) {
		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index);

		E oldValue = elementData(index);
		elementData[index] = element;
		return oldValue;
	}

	public synchronized boolean add(E e) {
		modCount++;
		ensureCapacityHelper(elementCount + 1);
		elementData[elementCount++] = e;
		return true;
	}

	public boolean remove(Object o) {
		return removeElement(o);
	}

	public void add(int index, E element) {
		insertElementAt(element, index);
	}

	public synchronized E remove(int index) {
		modCount++;
		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index);
		E oldValue = elementData(index);

		int numMoved = elementCount - index - 1;
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		elementData[--elementCount] = null; // Let gc do its work

		return oldValue;
	}

	public void clear() {
		removeAllElements();
	}

	// Bulk Operations

	public synchronized boolean containsAll(Collection<?> c) {
		return super.containsAll(c);
	}

	public synchronized boolean addAll(Collection<? extends E> c) {
		modCount++;
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacityHelper(elementCount + numNew);
		System.arraycopy(a, 0, elementData, elementCount, numNew);
		elementCount += numNew;
		return numNew != 0;
	}

	public synchronized boolean removeAll(Collection<?> c) {
		return super.removeAll(c);
	}

	public synchronized boolean retainAll(Collection<?> c) {
		return super.retainAll(c);
	}

	public synchronized boolean addAll(int index, Collection<? extends E> c) {
		modCount++;
		if (index < 0 || index > elementCount)
			throw new ArrayIndexOutOfBoundsException(index);

		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacityHelper(elementCount + numNew);

		int numMoved = elementCount - index;
		if (numMoved > 0)
			System.arraycopy(elementData, index, elementData, index + numNew, numMoved);

		System.arraycopy(a, 0, elementData, index, numNew);
		elementCount += numNew;
		return numNew != 0;
	}

	public synchronized boolean equals(Object o) {
		return super.equals(o);
	}

	public synchronized int hashCode() {
		return super.hashCode();
	}

	public synchronized String toString() {
		return super.toString();
	}

	// public synchronized List<E> subList(int fromIndex, int toIndex) {
	// return Collections.synchronizedList(super.subList(fromIndex, toIndex),
	// this);
	// }

	protected synchronized void removeRange(int fromIndex, int toIndex) {
		modCount++;
		int numMoved = elementCount - toIndex;
		System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

		// Let gc do its work
		int newElementCount = elementCount - (toIndex - fromIndex);
		while (elementCount != newElementCount)
			elementData[--elementCount] = null;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		final java.io.ObjectOutputStream.PutField fields = s.putFields();
		final Object[] data;
		synchronized (this) {
			fields.put("capacityIncrement", capacityIncrement);
			fields.put("elementCount", elementCount);
			data = elementData.clone();
		}
		fields.put("elementData", data);
		s.writeFields();
	}

	public synchronized ListIterator<E> listIterator(int index) {
		if (index < 0 || index > elementCount)
			throw new IndexOutOfBoundsException("Index: " + index);
		return new ListItr(index);
	}

	public synchronized ListIterator<E> listIterator() {
		return new ListItr(0);
	}

	public synchronized Iterator<E> iterator() {
		return new Itr();
	}

	/**
	 * An optimized version of AbstractList.Itr
	 */
	private class Itr implements Iterator<E> {
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		int expectedModCount = modCount;

		public boolean hasNext() {
			// Racy but within spec, since modifications are checked
			// within or after synchronization in next/previous
			return cursor != elementCount;
		}

		public E next() {
			synchronized (MyVector.this) {
				checkForComodification();
				int i = cursor;
				if (i >= elementCount)
					throw new NoSuchElementException();
				cursor = i + 1;
				return elementData(lastRet = i);
			}
		}

		public void remove() {
			if (lastRet == -1)
				throw new IllegalStateException();
			synchronized (MyVector.this) {
				checkForComodification();
				MyVector.this.remove(lastRet);
				expectedModCount = modCount;
			}
			cursor = lastRet;
			lastRet = -1;
		}

		@Override
		public void forEachRemaining(Consumer<? super E> action) {
			Objects.requireNonNull(action);
			synchronized (MyVector.this) {
				final int size = elementCount;
				int i = cursor;
				if (i >= size) {
					return;
				}
				@SuppressWarnings("unchecked")
				final E[] elementData = (E[]) MyVector.this.elementData;
				if (i >= elementData.length) {
					throw new ConcurrentModificationException();
				}
				while (i != size && modCount == expectedModCount) {
					action.accept(elementData[i++]);
				}
				// update once at end of iteration to reduce heap write traffic
				cursor = i;
				lastRet = i - 1;
				checkForComodification();
			}
		}

		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}

	/**
	 * An optimized version of AbstractList.ListItr
	 */
	final class ListItr extends Itr implements ListIterator<E> {
		ListItr(int index) {
			super();
			cursor = index;
		}

		public boolean hasPrevious() {
			return cursor != 0;
		}

		public int nextIndex() {
			return cursor;
		}

		public int previousIndex() {
			return cursor - 1;
		}

		public E previous() {
			synchronized (MyVector.this) {
				checkForComodification();
				int i = cursor - 1;
				if (i < 0)
					throw new NoSuchElementException();
				cursor = i;
				return elementData(lastRet = i);
			}
		}

		public void set(E e) {
			if (lastRet == -1)
				throw new IllegalStateException();
			synchronized (MyVector.this) {
				checkForComodification();
				MyVector.this.set(lastRet, e);
			}
		}

		public void add(E e) {
			int i = cursor;
			synchronized (MyVector.this) {
				checkForComodification();
				MyVector.this.add(i, e);
				expectedModCount = modCount;
			}
			cursor = i + 1;
			lastRet = -1;
		}
	}

	@Override
	public synchronized void forEach(Consumer<? super E> action) {
		Objects.requireNonNull(action);
		final int expectedModCount = modCount;
		@SuppressWarnings("unchecked")
		final E[] elementData = (E[]) this.elementData;
		final int elementCount = this.elementCount;
		for (int i = 0; modCount == expectedModCount && i < elementCount; i++) {
			action.accept(elementData[i]);
		}
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized boolean removeIf(Predicate<? super E> filter) {
		Objects.requireNonNull(filter);
		// figure out which elements are to be removed
		// any exception thrown from the filter predicate at this stage
		// will leave the collection unmodified
		int removeCount = 0;
		final int size = elementCount;
		final BitSet removeSet = new BitSet(size);
		final int expectedModCount = modCount;
		for (int i = 0; modCount == expectedModCount && i < size; i++) {
			@SuppressWarnings("unchecked")
			final E element = (E) elementData[i];
			if (filter.test(element)) {
				removeSet.set(i);
				removeCount++;
			}
		}
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}

		// shift surviving elements left over the spaces left by removed
		// elements
		final boolean anyToRemove = removeCount > 0;
		if (anyToRemove) {
			final int newSize = size - removeCount;
			for (int i = 0, j = 0; (i < size) && (j < newSize); i++, j++) {
				i = removeSet.nextClearBit(i);
				elementData[j] = elementData[i];
			}
			for (int k = newSize; k < size; k++) {
				elementData[k] = null; // Let gc do its work
			}
			elementCount = newSize;
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
			modCount++;
		}

		return anyToRemove;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized void replaceAll(UnaryOperator<E> operator) {
		Objects.requireNonNull(operator);
		final int expectedModCount = modCount;
		final int size = elementCount;
		for (int i = 0; modCount == expectedModCount && i < size; i++) {
			elementData[i] = operator.apply((E) elementData[i]);
		}
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
		modCount++;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void sort(Comparator<? super E> c) {
		final int expectedModCount = modCount;
		Arrays.sort((E[]) elementData, 0, elementCount, c);
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
		modCount++;
	}

	/**
	 * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
	 * and <em>fail-fast</em> {@link Spliterator} over the elements in this
	 * list.
	 *
	 * <p>
	 * The {@code Spliterator} reports {@link Spliterator#SIZED},
	 * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}. Overriding
	 * implementations should document the reporting of additional
	 * characteristic values.
	 *
	 * @return a {@code Spliterator} over the elements in this list
	 * @since 1.8
	 */
	@Override
	public Spliterator<E> spliterator() {
		return new VectorSpliterator<>(this, null, 0, -1, 0);
	}

	/** Similar to ArrayList Spliterator */
	static final class VectorSpliterator<E> implements Spliterator<E> {
		private final MyVector<E> list;
		private Object[] array;
		private int index; // current index, modified on advance/split
		private int fence; // -1 until used; then one past last index
		private int expectedModCount; // initialized when fence set

		/** Create new spliterator covering the given range */
		VectorSpliterator(MyVector<E> list, Object[] array, int origin, int fence, int expectedModCount) {
			this.list = list;
			this.array = array;
			this.index = origin;
			this.fence = fence;
			this.expectedModCount = expectedModCount;
		}

		private int getFence() { // initialize on first use
			int hi;
			if ((hi = fence) < 0) {
				synchronized (list) {
					array = list.elementData;
					expectedModCount = list.modCount;
					hi = fence = list.elementCount;
				}
			}
			return hi;
		}

		public Spliterator<E> trySplit() {
			int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
			return (lo >= mid) ? null : new VectorSpliterator<E>(list, array, lo, index = mid, expectedModCount);
		}

		@SuppressWarnings("unchecked")
		public boolean tryAdvance(Consumer<? super E> action) {
			int i;
			if (action == null)
				throw new NullPointerException();
			if (getFence() > (i = index)) {
				index = i + 1;
				action.accept((E) array[i]);
				if (list.modCount != expectedModCount)
					throw new ConcurrentModificationException();
				return true;
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		public void forEachRemaining(Consumer<? super E> action) {
			int i, hi; // hoist accesses and checks from loop
			MyVector<E> lst;
			Object[] a;
			if (action == null)
				throw new NullPointerException();
			if ((lst = list) != null) {
				if ((hi = fence) < 0) {
					synchronized (lst) {
						expectedModCount = lst.modCount;
						a = array = lst.elementData;
						hi = fence = lst.elementCount;
					}
				} else
					a = array;
				if (a != null && (i = index) >= 0 && (index = hi) <= a.length) {
					while (i < hi)
						action.accept((E) a[i++]);
					if (lst.modCount == expectedModCount)
						return;
				}
			}
			throw new ConcurrentModificationException();
		}

		public long estimateSize() {
			return (long) (getFence() - index);
		}

		public int characteristics() {
			return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
		}
	}

}