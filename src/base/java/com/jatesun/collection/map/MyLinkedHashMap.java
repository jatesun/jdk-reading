package com.jatesun.collection.map;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
//import java.util.LinkedHashMap.Entry;
//import java.util.LinkedHashMap.EntryIterator;
//import java.util.LinkedHashMap.KeyIterator;
//import java.util.LinkedHashMap.LinkedHashIterator;
//import java.util.LinkedHashMap.ValueIterator;

/**
 * 
 * @author jatesun
 * @description linkedhashmap是有序的hashmap，通过先后加入的元素之间构建链表形成
 * @question
 * 		·什么是有序、无序？
 * 		·linkedhashmap是如何实现有序的？
 * @date 2017年3月24日
 * @param <K>
 * @param <V>
 */
public class MyLinkedHashMap<K, V> extends MyHashMap<K, V> implements Map<K, V> {

	private static final long serialVersionUID = 3801124242820219131L;

	private transient Entry<K, V> header;// 头节点

	private final boolean accessOrder;// TODO

	public MyLinkedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		accessOrder = false;
	}

	public MyLinkedHashMap(int initialCapacity) {
		super(initialCapacity);
		accessOrder = false;
	}

	public MyLinkedHashMap() {
		super();
		accessOrder = false;
	}

	public MyLinkedHashMap(Map<? extends K, ? extends V> m) {
		super(m);
		accessOrder = false;
	}

	public MyLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor);
		this.accessOrder = accessOrder;
	}

	void init() {
		header = new Entry<K, V>(-1, null, null, null);
		header.before = header.after = header;
	}

	void transfer(MyHashMap.Entry[] newTable) {
		int newCapacity = newTable.length;
		// 转移到新的enry中。直接遍历有序链表即可。
		for (Entry<K, V> e = header.after; e != header; e = e.after) {
			int index = indexFor(e.hash, newCapacity);
			e.next = newTable[index];
			newTable[index] = e;
		}
	}

	public boolean containsValue(Object value) {
		if (value == null) {
			for (Entry e = header.after; e != header; e = e.after)
				if (e.value == null)
					return true;
		} else {
			for (Entry e = header.after; e != header; e = e.after)
				if (value.equals(e.value))
					return true;
		}
		return false;
	}

	public V get(Object key) {
		Entry<K, V> e = (Entry<K, V>) getEntry(key);
		if (e == null)
			return null;
		e.recordAccess(this);
		return e.value;
	}

	public void clear() {
		super.clear();
		header.before = header.after = header;
	}

	private static class Entry<K, V> extends MyHashMap.Entry<K, V> {
		Entry<K, V> before, after;

		Entry(int hash, K key, V value, MyHashMap.Entry<K, V> next) {
			super(hash, key, value, next);
		}

		private void remove() {
			before.after = after;
			after.before = before;
		}

		// addbefore方法
		private void addBefore(Entry<K, V> existingEntry) {
			after = existingEntry;
			before = existingEntry.before;
			before.after = this;
			after.before = this;
		}

		void recordAccess(MyHashMap<K, V> m) {
			MyLinkedHashMap<K, V> lm = (MyLinkedHashMap<K, V>) m;
			if (lm.accessOrder) {
				lm.modCount++;
				remove();
				addBefore(lm.header);
			}
		}

		void recordRemoval(HashMap<K, V> m) {
			remove();
		}
	}

	private abstract class LinkedHashIterator<T> implements Iterator<T> {
		Entry<K, V> nextEntry = header.after;
		Entry<K, V> lastReturned = null;

		/**
		 * The modCount value that the iterator believes that the backing
		 * List should have.  If this expectation is violated, the iterator
		 * has detected concurrent modification.
		 */
		int expectedModCount = modCount;

		public boolean hasNext() {
			return nextEntry != header;
		}

		public void remove() {
			if (lastReturned == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();

			MyLinkedHashMap.this.remove(lastReturned.key);
			lastReturned = null;
			expectedModCount = modCount;
		}

		Entry<K, V> nextEntry() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			if (nextEntry == header)
				throw new NoSuchElementException();

			Entry<K, V> e = lastReturned = nextEntry;
			nextEntry = e.after;
			return e;
		}
	}

	private class KeyIterator extends LinkedHashIterator<K> {
		public K next() {
			return nextEntry().getKey();
		}
	}

	private class ValueIterator extends LinkedHashIterator<V> {
		public V next() {
			return nextEntry().value;
		}
	}

	private class EntryIterator extends LinkedHashIterator<Map.Entry<K, V>> {
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	// These Overrides alter the behavior of superclass view iterator() methods
	Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

	void addEntry(int hash, K key, V value, int bucketIndex) {
		createEntry(hash, key, value, bucketIndex);
		Entry<K, V> eldest = header.after;
		if (removeEldestEntry(eldest)) {
			removeEntryForKey(eldest.key);
		} else {
			if (size >= threshold)
				resize(2 * table.length);
		}
	}

	// 新建节点方法
	void createEntry(int hash, K key, V value, int bucketIndex) {
		MyHashMap.Entry<K, V> old = table[bucketIndex];
		Entry<K, V> e = new Entry<K, V>(hash, key, value, old);
		table[bucketIndex] = e;
		e.addBefore(header);
		size++;
	}
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return false;
	}

}
