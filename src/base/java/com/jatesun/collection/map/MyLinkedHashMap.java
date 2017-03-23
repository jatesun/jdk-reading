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
		for (Entry<K, V> e = header.after; e != header; e = e.after) {
			int index = indexFor(e.hash, newCapacity);
			e.next = newTable[index];
			newTable[index] = e;
		}
	}

	public boolean containsValue(Object value) {
		// Overridden to take advantage of faster iterator
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

	/**
	 * LinkedHashMap entry.
	 */
	private static class Entry<K, V> extends MyHashMap.Entry<K, V> {
		// These fields comprise the doubly linked list used for iteration.
		Entry<K, V> before, after;

		Entry(int hash, K key, V value, MyHashMap.Entry<K, V> next) {
			super(hash, key, value, next);
		}

		/**
		 * Removes this entry from the linked list.
		 */
		private void remove() {
			before.after = after;
			after.before = before;
		}

		/**
		 * Inserts this entry before the specified existing entry in the list.
		 */
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

	/**
	 * This override alters behavior of superclass put method. It causes newly
	 * allocated entry to get inserted at the end of the linked list and
	 * removes the eldest entry if appropriate.
	 */
	void addEntry(int hash, K key, V value, int bucketIndex) {
		createEntry(hash, key, value, bucketIndex);

		// Remove eldest entry if instructed, else grow capacity if appropriate
		Entry<K, V> eldest = header.after;
		if (removeEldestEntry(eldest)) {
			removeEntryForKey(eldest.key);
		} else {
			if (size >= threshold)
				resize(2 * table.length);
		}
	}

	/**
	 * This override differs from addEntry in that it doesn't resize the
	 * table or remove the eldest entry.
	 */
	void createEntry(int hash, K key, V value, int bucketIndex) {
		MyHashMap.Entry<K, V> old = table[bucketIndex];
		Entry<K, V> e = new Entry<K, V>(hash, key, value, old);
		table[bucketIndex] = e;
		e.addBefore(header);
		size++;
	}

	/**
	 * Returns <tt>true</tt> if this map should remove its eldest entry.
	 * This method is invoked by <tt>put</tt> and <tt>putAll</tt> after
	 * inserting a new entry into the map.  It provides the implementor
	 * with the opportunity to remove the eldest entry each time a new one
	 * is added.  This is useful if the map represents a cache: it allows
	 * the map to reduce memory consumption by deleting stale entries.
	 *
	 * <p>Sample use: this override will allow the map to grow up to 100
	 * entries and then delete the eldest entry each time a new entry is
	 * added, maintaining a steady state of 100 entries.
	 * <pre>
	 *     private static final int MAX_ENTRIES = 100;
	 *
	 *     protected boolean removeEldestEntry(Map.Entry eldest) {
	 *        return size() > MAX_ENTRIES;
	 *     }
	 * </pre>
	 *
	 * <p>This method typically does not modify the map in any way,
	 * instead allowing the map to modify itself as directed by its
	 * return value.  It <i>is</i> permitted for this method to modify
	 * the map directly, but if it does so, it <i>must</i> return
	 * <tt>false</tt> (indicating that the map should not attempt any
	 * further modification).  The effects of returning <tt>true</tt>
	 * after modifying the map from within this method are unspecified.
	 *
	 * <p>This implementation merely returns <tt>false</tt> (so that this
	 * map acts like a normal map - the eldest element is never removed).
	 *
	 * @param    eldest The least recently inserted entry in the map, or if
	 *           this is an access-ordered map, the least recently accessed
	 *           entry.  This is the entry that will be removed it this
	 *           method returns <tt>true</tt>.  If the map was empty prior
	 *           to the <tt>put</tt> or <tt>putAll</tt> invocation resulting
	 *           in this invocation, this will be the entry that was just
	 *           inserted; in other words, if the map contains a single
	 *           entry, the eldest entry is also the newest.
	 * @return   <tt>true</tt> if the eldest entry should be removed
	 *           from the map; <tt>false</tt> if it should be retained.
	 */
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return false;
	}

}
