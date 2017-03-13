package com.jatesun.collection.list;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
//import java.util.LinkedList.DescendingIterator;
//import java.util.LinkedList.Entry;
//import java.util.LinkedList.ListItr;

/**
 * @Title: MyLinkedList.java
 * @Description: 使用内部节点实现list接口的集合类,节点为双向的（即linkedlist为双向链表实现）
 * @author: jateSun
 * @date: 2017年3月5日 下午7:03:46
 * @version: V1.0
 */
public class MyLinkedList<E> extends AbstractSequentialList<E>
		implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
	private transient Entry<E> header = new Entry<E>(null, null, null);
	private transient int size = 0;

	// 空构造方法，next和previous都指向自己,linkedlist没有指定size的构造方法
	public MyLinkedList() {
		header.next = header.previous = header;
	}

	public MyLinkedList(Collection<? extends E> c) {
		this();
		addAll(c);
	}

	public E getFirst() {
		// 没有元素抛异常，否则返回header.next
		if (size == 0)
			throw new NoSuchElementException();
		return header.next.element;
	}

	public E getLast() {
		// 这里可以看出list实际是环装结构
		if (size == 0)
			throw new NoSuchElementException();
		return header.previous.element;
	}

	public E removeFirst() {
		// 调用remove(Entry
		// e)方法，这里当集合里只剩header是，header是指向自己而不是null，所有remove方法校验null
		return remove(header.next);
	}

	public E removeLast() {
		return remove(header.previous);
	}

	public void addFirst(E e) {
		// 调用addBefore(E e,Entry entry)在entry前插入元素
		addBefore(e, header.next);
	}

	public void addLast(E e) {
		addBefore(e, header);
	}

	public boolean contains(Object o) {
		// 跟arraylist相似的indexOf方法
		return indexOf(o) != -1;
	}

	public int size() {
		return size;
	}

	public boolean add(E e) {
		addBefore(e, header);
		return true;
	}

	// 删除方法,分null和非null 相等即调用remove(entry)方法
	public boolean remove(Object o) {
		if (o == null) {
			for (Entry<E> e = header.next; e != header; e = e.next) {
				if (e.element == null) {
					remove(e);
					return true;
				}
			}
		} else {
			for (Entry<E> e = header.next; e != header; e = e.next) {
				if (o.equals(e.element)) {
					remove(e);
					return true;
				}
			}
		}
		return false;
	}

	public boolean addAll(Collection<? extends E> c) {
		return addAll(size, c);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		Object[] a = c.toArray();
		int numNew = a.length;
		if (numNew == 0)
			return false;
		modCount++;

		Entry<E> successor = (index == size ? header : entry(index));
		Entry<E> predecessor = successor.previous;
		for (int i = 0; i < numNew; i++) {
			// 构造新节点
			Entry<E> e = new Entry<E>((E) a[i], successor, predecessor);
			predecessor.next = e;// pre.next指向e
			predecessor = e;// pre指向e，实际为指向新加入元素，即prev和successor总是挨着
		}
		// 结束后将suc的prev指向pred
		successor.previous = predecessor;

		size += numNew;
		return true;
	}

	// 清除方法，迭代每个节点将next prev element置为null。
	public void clear() {
		Entry<E> e = header.next;
		while (e != header) {
			Entry<E> next = e.next;
			e.next = e.previous = null;
			e.element = null;
			e = next;
		}
		header.next = header.previous = header;
		size = 0;
		modCount++;
	}

	public E get(int index) {
		return entry(index).element;
	}

	public E set(int index, E element) {
		Entry<E> e = entry(index);
		E oldVal = e.element;
		e.element = element;
		return oldVal;
	}

	public void add(int index, E element) {
		addBefore(element, (index == size ? header : entry(index)));
	}

	public E remove(int index) {
		return remove(entry(index));
	}

	private Entry<E> entry(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		Entry<E> e = header;
		if (index < (size >> 1)) {
			for (int i = 0; i <= index; i++)
				e = e.next;
		} else {
			for (int i = size; i > index; i--)
				e = e.previous;
		}
		return e;
	}

	public int indexOf(Object o) {
		int index = 0;
		if (o == null) {
			for (Entry e = header.next; e != header; e = e.next) {
				if (e.element == null)
					return index;
				index++;
			}
		} else {
			for (Entry e = header.next; e != header; e = e.next) {
				if (o.equals(e.element))
					return index;
				index++;
			}
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		int index = size;
		if (o == null) {
			for (Entry e = header.previous; e != header; e = e.previous) {
				index--;
				if (e.element == null)
					return index;
			}
		} else {
			for (Entry e = header.previous; e != header; e = e.previous) {
				index--;
				if (o.equals(e.element))
					return index;
			}
		}
		return -1;
	}

	public E peek() {
		if (size == 0)
			return null;
		return getFirst();
	}

	public E element() {
		return getFirst();
	}

	/******* queue相关算法，使用的也是上面的函数 *******/
	public E poll() {
		if (size == 0)
			return null;
		return removeFirst();
	}

	public E remove() {
		return removeFirst();
	}

	public boolean offer(E e) {
		return add(e);
	}

	public boolean offerFirst(E e) {
		addFirst(e);
		return true;
	}

	public boolean offerLast(E e) {
		addLast(e);
		return true;
	}

	public E peekFirst() {
		if (size == 0)
			return null;
		return getFirst();
	}

	public E peekLast() {
		if (size == 0)
			return null;
		return getLast();
	}

	public E pollFirst() {
		if (size == 0)
			return null;
		return removeFirst();
	}

	public E pollLast() {
		if (size == 0)
			return null;
		return removeLast();
	}

	public void push(E e) {
		addFirst(e);
	}

	public E pop() {
		return removeFirst();
	}

	public boolean removeFirstOccurrence(Object o) {
		return remove(o);
	}

	public boolean removeLastOccurrence(Object o) {
		if (o == null) {
			for (Entry<E> e = header.previous; e != header; e = e.previous) {
				if (e.element == null) {
					remove(e);
					return true;
				}
			}
		} else {
			for (Entry<E> e = header.previous; e != header; e = e.previous) {
				if (o.equals(e.element)) {
					remove(e);
					return true;
				}
			}
		}
		return false;
	}

	// TODO Iterator方法
	public ListIterator<E> listIterator(int index) {
		return new ListItr(index);
	}

	private class ListItr implements ListIterator<E> {
		private Entry<E> lastReturned = header;
		private Entry<E> next;
		private int nextIndex;
		private int expectedModCount = modCount;

		ListItr(int index) {
			if (index < 0 || index > size)
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			if (index < (size >> 1)) {
				next = header.next;
				for (nextIndex = 0; nextIndex < index; nextIndex++)
					next = next.next;
			} else {
				next = header;
				for (nextIndex = size; nextIndex > index; nextIndex--)
					next = next.previous;
			}
		}

		public boolean hasNext() {
			return nextIndex != size;
		}

		public E next() {
			checkForComodification();
			if (nextIndex == size)
				throw new NoSuchElementException();

			lastReturned = next;
			next = next.next;
			nextIndex++;
			return lastReturned.element;
		}

		public boolean hasPrevious() {
			return nextIndex != 0;
		}

		public E previous() {
			if (nextIndex == 0)
				throw new NoSuchElementException();

			lastReturned = next = next.previous;
			nextIndex--;
			checkForComodification();
			return lastReturned.element;
		}

		public int nextIndex() {
			return nextIndex;
		}

		public int previousIndex() {
			return nextIndex - 1;
		}

		public void remove() {
			checkForComodification();
			Entry<E> lastNext = lastReturned.next;
			try {
				MyLinkedList.this.remove(lastReturned);
			} catch (NoSuchElementException e) {
				throw new IllegalStateException();
			}
			if (next == lastReturned)
				next = lastNext;
			else
				nextIndex--;
			lastReturned = header;
			expectedModCount++;
		}

		public void set(E e) {
			if (lastReturned == header)
				throw new IllegalStateException();
			checkForComodification();
			lastReturned.element = e;
		}

		public void add(E e) {
			checkForComodification();
			lastReturned = header;
			addBefore(e, next);
			nextIndex++;
			expectedModCount++;
		}

		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}

	// 内部节点类 entry,存放元素以及上一个和下一个节点
	private static class Entry<E> {
		E element;
		Entry<E> next;
		Entry<E> previous;

		Entry(E element, Entry<E> next, Entry<E> previous) {
			this.element = element;
			this.next = next;
			this.previous = previous;
		}
	}

	// add最重要的方法
	private Entry<E> addBefore(E e, Entry<E> entry) {
		// 构造一个新的entry节点，next节点为entry prev节点为entry.prev
		Entry<E> newEntry = new Entry<E>(e, entry, entry.previous);
		// 将新的节点插入到链表中（上一步只是将新增的entry构造完，但是彼此之间没有形成联系）
		// 将新节点元素中的前节点的next和后节点的prev付给自己，这样就完成了插入方法
		newEntry.previous.next = newEntry;
		newEntry.next.previous = newEntry;
		size++;
		modCount++;
		return newEntry;
	}

	// 最重要的remove方法
	private E remove(Entry<E> e) {
		if (e == header)
			throw new NoSuchElementException();

		E result = e.element;
		e.previous.next = e.next;
		e.next.previous = e.previous;
		e.next = e.previous = null;
		e.element = null;
		size--;
		modCount++;
		return result;
	}

	/**
	 * @since 1.6
	 */
	public Iterator<E> descendingIterator() {
		return new DescendingIterator();
	}

	/** Adapter to provide descending iterators via ListItr.previous */
	private class DescendingIterator implements Iterator {
		final ListItr itr = new ListItr(size());

		public boolean hasNext() {
			return itr.hasPrevious();
		}

		public E next() {
			return itr.previous();
		}

		public void remove() {
			itr.remove();
		}
	}

	public Object clone() {
		MyLinkedList<E> clone = null;
		try {
			clone = (MyLinkedList<E>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
		// Put clone into "virgin" state
		clone.header = new Entry<E>(null, null, null);
		clone.header.next = clone.header.previous = clone.header;
		clone.size = 0;
		clone.modCount = 0;
		// Initialize clone with our elements
		for (Entry<E> e = header.next; e != header; e = e.next)
			clone.add(e.element);

		return clone;
	}

	public Object[] toArray() {
		Object[] result = new Object[size];
		int i = 0;
		for (Entry<E> e = header.next; e != header; e = e.next)
			result[i++] = e.element;
		return result;
	}

	public <T> T[] toArray(T[] a) {
		if (a.length < size)
			a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		int i = 0;
		Object[] result = a;
		for (Entry<E> e = header.next; e != header; e = e.next)
			result[i++] = e.element;

		if (a.length > size)
			a[size] = null;

		return a;
	}

	private static final long serialVersionUID = 876323262645176354L;

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		// Write out any hidden serialization magic
		s.defaultWriteObject();

		// Write out size
		s.writeInt(size);

		// Write out all elements in the proper order.
		for (Entry e = header.next; e != header; e = e.next)
			s.writeObject(e.element);
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		// Read in any hidden serialization magic
		s.defaultReadObject();

		// Read in size
		int size = s.readInt();

		// Initialize header
		header = new Entry<E>(null, null, null);
		header.next = header.previous = header;

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++)
			addBefore((E) s.readObject(), header);
	}

}
