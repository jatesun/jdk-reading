package com.jatesun.collection.map;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
//import java.util.Hashtable.EmptyEnumerator;
//import java.util.Hashtable.EmptyIterator;
//import java.util.Hashtable.Entry;
//import java.util.Hashtable.EntrySet;
//import java.util.Hashtable.Enumerator;
//import java.util.Hashtable.KeySet;
//import java.util.Hashtable.ValueCollection;

/**
 * 
 * @author jatesun
 * @description
 * 		注意：阅读hashtable之前你需要先阅读到hashmap的源码(很多hashmap中解释的东西，这里不再解释)
 * 		hashtable是线程安全的hash集合，内部实现的数据结构跟hashmap相同。
 * @question
 * 		·hashtable和hashmap的关联？
 * 			相同：数据结构相同（都是entry数组加链表实现）
 * 			不同：hashtable不允许null值、hashtable线程安全
 * @date 2017年3月20日
 * @param <K>
 * @param <V>
 */
public class MyHashTable<K, V> extends Dictionary<K, V> implements Map<K, V>, Cloneable, java.io.Serializable {

	private transient Entry[] table;// 存放元素数组

	private transient int count;// 元素大小

	private int threshold;// 临界

	private float loadFactor;// 装填因子

	private transient int modCount = 0;

	private static final long serialVersionUID = 1421746759512286392L;

	// 最重要的构造方法
	public MyHashTable(int initialCapacity, float loadFactor) {
		// 校验参数
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal Load: " + loadFactor);

		if (initialCapacity == 0)
			initialCapacity = 1;
		// 初始化entry、计算临界容量
		this.loadFactor = loadFactor;
		table = new Entry[initialCapacity];
		threshold = (int) (initialCapacity * loadFactor);
	}

	public MyHashTable(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public MyHashTable() {
		this(11, 0.75f);
	}

	public MyHashTable(Map<? extends K, ? extends V> t) {
		this(Math.max(2 * t.size(), 11), 0.75f);
		putAll(t);
	}

	public synchronized int size() {
		return count;
	}

	public synchronized boolean isEmpty() {
		return count == 0;
	}

	public synchronized Enumeration<K> keys() {
		return this.<K> getEnumeration(KEYS);
	}

	public synchronized Enumeration<V> elements() {
		return this.<V> getEnumeration(VALUES);
	}

	public synchronized boolean contains(Object value) {
		// hashtable不允许null
		if (value == null) {
			throw new NullPointerException();
		}

		Entry tab[] = table;
		// 双层for循环遍历
		for (int i = tab.length; i-- > 0;) {
			for (Entry<K, V> e = tab[i]; e != null; e = e.next) {
				if (e.value.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsValue(Object value) {
		return contains(value);
	}

	public synchronized boolean containsKey(Object key) {
		Entry tab[] = table;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;// 与hashmap的index计算方法不同。也没有再hash
		// for循环查找链表
		for (Entry<K, V> e = tab[index]; e != null; e = e.next) {
			if ((e.hash == hash) && e.key.equals(key)) {
				return true;
			}
		}
		return false;
	}

	public synchronized V get(Object key) {
		Entry tab[] = table;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry<K, V> e = tab[index]; e != null; e = e.next) {
			if ((e.hash == hash) && e.key.equals(key)) {
				return e.value;
			}
		}
		return null;
	}

	// 需要扩容之后的rehash。
	protected void rehash() {
		int oldCapacity = table.length;
		Entry[] oldMap = table;

		int newCapacity = oldCapacity * 2 + 1;
		Entry[] newMap = new Entry[newCapacity];

		modCount++;
		threshold = (int) (newCapacity * loadFactor);
		table = newMap;

		for (int i = oldCapacity; i-- > 0;) {
			for (Entry<K, V> old = oldMap[i]; old != null;) {
				Entry<K, V> e = old;
				old = old.next;

				int index = (e.hash & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	public synchronized V put(K key, V value) {
		if (value == null) {
			throw new NullPointerException();
		}

		// 如果已经存在就替换value。
		Entry tab[] = table;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry<K, V> e = tab[index]; e != null; e = e.next) {
			if ((e.hash == hash) && e.key.equals(key)) {
				V old = e.value;
				e.value = value;
				return old;
			}
		}

		modCount++;
		// 如果不存在节点，需要判断是否扩容
		if (count >= threshold) {
			rehash();
			tab = table;
			index = (hash & 0x7FFFFFFF) % tab.length;
		}

		// 在链表中添加新的节点
		Entry<K, V> e = tab[index];
		tab[index] = new Entry<K, V>(hash, key, value, e);
		count++;
		return null;
	}

	public synchronized V remove(Object key) {
		Entry tab[] = table;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry<K, V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
			if ((e.hash == hash) && e.key.equals(key)) {
				modCount++;
				if (prev != null) {
					prev.next = e.next;
				} else {
					// 删除的首元素，在hashmap中的remove也有类似地if else。
					tab[index] = e.next;
				}
				count--;
				V oldValue = e.value;
				e.value = null;
				return oldValue;
			}
		}
		return null;
	}

	public synchronized void putAll(Map<? extends K, ? extends V> t) {
		for (Map.Entry<? extends K, ? extends V> e : t.entrySet())
			put(e.getKey(), e.getValue());
	}

	// 跟hashmap差不多，不赘述
	public synchronized void clear() {
		Entry tab[] = table;
		modCount++;
		for (int index = tab.length; --index >= 0;)
			tab[index] = null;
		count = 0;
	}

	public synchronized Object clone() {
		try {
			MyHashTable<K, V> t = (MyHashTable<K, V>) super.clone();
			t.table = new Entry[table.length];
			for (int i = table.length; i-- > 0;) {
				t.table[i] = (table[i] != null) ? (Entry<K, V>) table[i].clone() : null;
			}
			t.keySet = null;
			t.entrySet = null;
			t.values = null;
			t.modCount = 0;
			return t;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}

	public synchronized String toString() {
		int max = size() - 1;
		if (max == -1)
			return "{}";

		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry<K, V>> it = entrySet().iterator();

		sb.append('{');
		for (int i = 0;; i++) {
			Map.Entry<K, V> e = it.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == this ? "(this Map)" : key.toString());
			sb.append('=');
			sb.append(value == this ? "(this Map)" : value.toString());

			if (i == max)
				return sb.append('}').toString();
			sb.append(", ");
		}
	}

	private <T> Enumeration<T> getEnumeration(int type) {
		if (count == 0) {
			return (Enumeration<T>) emptyEnumerator;
		} else {
			return new Enumerator<T>(type, false);
		}
	}

	private <T> Iterator<T> getIterator(int type) {
		if (count == 0) {
			return (Iterator<T>) emptyIterator;
		} else {
			return new Enumerator<T>(type, true);
		}
	}

	// Views

	private transient volatile Set<K> keySet = null;// 对应的key set
	private transient volatile Set<Map.Entry<K, V>> entrySet = null;// 对应的entryset
	private transient volatile Collection<V> values = null;

	public Set<K> keySet() {
		// if (keySet == null)
		// keySet = Collections.synchronizedSet(new KeySet(), this);
		return null;
	}

	private class KeySet extends AbstractSet<K> {
		public Iterator<K> iterator() {
			return getIterator(KEYS);
		}

		public int size() {
			return count;
		}

		public boolean contains(Object o) {
			return containsKey(o);
		}

		public boolean remove(Object o) {
			return MyHashTable.this.remove(o) != null;
		}

		public void clear() {
			MyHashTable.this.clear();
		}
	}

	/**
	 * Returns a {@link Set} view of the mappings contained in this map.
	 * The set is backed by the map, so changes to the map are
	 * reflected in the set, and vice-versa.  If the map is modified
	 * while an iteration over the set is in progress (except through
	 * the iterator's own <tt>remove</tt> operation, or through the
	 * <tt>setValue</tt> operation on a map entry returned by the
	 * iterator) the results of the iteration are undefined.  The set
	 * supports element removal, which removes the corresponding
	 * mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
	 * <tt>clear</tt> operations.  It does not support the
	 * <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @since 1.2
	 */
	public Set<Map.Entry<K, V>> entrySet() {
		// if (entrySet == null)
		// entrySet = Collections.synchronizedSet(new EntrySet(), this);
		return null;
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		public Iterator<Map.Entry<K, V>> iterator() {
			return getIterator(ENTRIES);
		}

		public boolean add(Map.Entry<K, V> o) {
			return super.add(o);
		}

		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry entry = (Map.Entry) o;
			Object key = entry.getKey();
			Entry[] tab = table;
			int hash = key.hashCode();
			int index = (hash & 0x7FFFFFFF) % tab.length;

			for (Entry e = tab[index]; e != null; e = e.next)
				if (e.hash == hash && e.equals(entry))
					return true;
			return false;
		}

		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			K key = entry.getKey();
			Entry[] tab = table;
			int hash = key.hashCode();
			int index = (hash & 0x7FFFFFFF) % tab.length;

			for (Entry<K, V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
				if (e.hash == hash && e.equals(entry)) {
					modCount++;
					if (prev != null)
						prev.next = e.next;
					else
						tab[index] = e.next;

					count--;
					e.value = null;
					return true;
				}
			}
			return false;
		}

		public int size() {
			return count;
		}

		public void clear() {
			MyHashTable.this.clear();
		}
	}

	/**
	 * Returns a {@link Collection} view of the values contained in this map.
	 * The collection is backed by the map, so changes to the map are
	 * reflected in the collection, and vice-versa.  If the map is
	 * modified while an iteration over the collection is in progress
	 * (except through the iterator's own <tt>remove</tt> operation),
	 * the results of the iteration are undefined.  The collection
	 * supports element removal, which removes the corresponding
	 * mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
	 * support the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @since 1.2
	 */
	public Collection<V> values() {
		// if (values == null)
		// values = Collections.synchronizedCollection(new ValueCollection(),
		// this);
		return null;
	}

	private class ValueCollection extends AbstractCollection<V> {
		public Iterator<V> iterator() {
			return getIterator(VALUES);
		}

		public int size() {
			return count;
		}

		public boolean contains(Object o) {
			return containsValue(o);
		}

		public void clear() {
			MyHashTable.this.clear();
		}
	}

	// Comparison and hashing

	public synchronized boolean equals(Object o) {
		if (o == this)
			return true;

		if (!(o instanceof Map))
			return false;
		Map<K, V> t = (Map<K, V>) o;
		if (t.size() != size())
			return false;

		try {
			Iterator<Map.Entry<K, V>> i = entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<K, V> e = i.next();
				K key = e.getKey();
				V value = e.getValue();
				if (value == null) {
					if (!(t.get(key) == null && t.containsKey(key)))
						return false;
				} else {
					if (!value.equals(t.get(key)))
						return false;
				}
			}
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}

		return true;
	}

	public synchronized int hashCode() {
		int h = 0;
		if (count == 0 || loadFactor < 0)
			return h; // Returns zero

		loadFactor = -loadFactor; // Mark hashCode computation in progress
		Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			for (Entry e = tab[i]; e != null; e = e.next)
				h += e.key.hashCode() ^ e.value.hashCode();
		loadFactor = -loadFactor; // Mark hashCode computation complete

		return h;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream s) throws IOException {
		// Write out the length, threshold, loadfactor
		s.defaultWriteObject();

		// Write out length, count of elements and then the key/value objects
		s.writeInt(table.length);
		s.writeInt(count);
		for (int index = table.length - 1; index >= 0; index--) {
			Entry entry = table[index];

			while (entry != null) {
				s.writeObject(entry.key);
				s.writeObject(entry.value);
				entry = entry.next;
			}
		}
	}

	/**
	 * Reconstitute the Hashtable from a stream (i.e., deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
		// Read in the length, threshold, and loadfactor
		s.defaultReadObject();

		// Read the original length of the array and number of elements
		int origlength = s.readInt();
		int elements = s.readInt();

		// Compute new size with a bit of room 5% to grow but
		// no larger than the original size. Make the length
		// odd if it's large enough, this helps distribute the entries.
		// Guard against the length ending up zero, that's not valid.
		int length = (int) (elements * loadFactor) + (elements / 20) + 3;
		if (length > elements && (length & 1) == 0)
			length--;
		if (origlength > 0 && length > origlength)
			length = origlength;

		Entry[] table = new Entry[length];
		count = 0;

		// Read the number of elements and then all the key/value objects
		for (; elements > 0; elements--) {
			K key = (K) s.readObject();
			V value = (V) s.readObject();
			// synch could be eliminated for performance
			reconstitutionPut(table, key, value);
		}
		this.table = table;
	}

	/**
	 * The put method used by readObject. This is provided because put
	 * is overridable and should not be called in readObject since the
	 * subclass will not yet be initialized.
	 *
	 * <p>This differs from the regular put method in several ways. No
	 * checking for rehashing is necessary since the number of elements
	 * initially in the table is known. The modCount is not incremented
	 * because we are creating a new instance. Also, no return value
	 * is needed.
	 */
	private void reconstitutionPut(Entry[] tab, K key, V value) throws StreamCorruptedException {
		if (value == null) {
			throw new java.io.StreamCorruptedException();
		}
		// Makes sure the key is not already in the hashtable.
		// This should not happen in deserialized version.
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry<K, V> e = tab[index]; e != null; e = e.next) {
			if ((e.hash == hash) && e.key.equals(key)) {
				throw new java.io.StreamCorruptedException();
			}
		}
		// Creates the new entry.
		Entry<K, V> e = tab[index];
		tab[index] = new Entry<K, V>(hash, key, value, e);
		count++;
	}

	// hashtable的entry节点。与hashmap的一样，不在详解
	private static class Entry<K, V> implements Map.Entry<K, V> {
		int hash;
		K key;
		V value;
		Entry<K, V> next;

		protected Entry(int hash, K key, V value, Entry<K, V> next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}

		protected Object clone() {
			return new Entry<K, V>(hash, key, value, (next == null ? null : (Entry<K, V>) next.clone()));
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			if (value == null)
				throw new NullPointerException();

			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry e = (Map.Entry) o;

			return (key == null ? e.getKey() == null : key.equals(e.getKey()))
					&& (value == null ? e.getValue() == null : value.equals(e.getValue()));
		}

		public int hashCode() {
			return hash ^ (value == null ? 0 : value.hashCode());
		}

		public String toString() {
			return key.toString() + "=" + value.toString();
		}
	}

	// Types of Enumerations/Iterations
	private static final int KEYS = 0;
	private static final int VALUES = 1;
	private static final int ENTRIES = 2;

	/**
	 * A hashtable enumerator class.  This class implements both the
	 * Enumeration and Iterator interfaces, but individual instances
	 * can be created with the Iterator methods disabled.  This is necessary
	 * to avoid unintentionally increasing the capabilities granted a user
	 * by passing an Enumeration.
	 */
	private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
		Entry[] table = MyHashTable.this.table;
		int index = table.length;
		Entry<K, V> entry = null;
		Entry<K, V> lastReturned = null;
		int type;

		/**
		 * Indicates whether this Enumerator is serving as an Iterator
		 * or an Enumeration.  (true -> Iterator).
		 */
		boolean iterator;

		/**
		 * The modCount value that the iterator believes that the backing
		 * Hashtable should have.  If this expectation is violated, the iterator
		 * has detected concurrent modification.
		 */
		protected int expectedModCount = modCount;

		Enumerator(int type, boolean iterator) {
			this.type = type;
			this.iterator = iterator;
		}

		public boolean hasMoreElements() {
			Entry<K, V> e = entry;
			int i = index;
			Entry[] t = table;
			/* Use locals for faster loop iteration */
			while (e == null && i > 0) {
				e = t[--i];
			}
			entry = e;
			index = i;
			return e != null;
		}

		public T nextElement() {
			Entry<K, V> et = entry;
			int i = index;
			Entry[] t = table;
			/* Use locals for faster loop iteration */
			while (et == null && i > 0) {
				et = t[--i];
			}
			entry = et;
			index = i;
			if (et != null) {
				Entry<K, V> e = lastReturned = entry;
				entry = e.next;
				return type == KEYS ? (T) e.key : (type == VALUES ? (T) e.value : (T) e);
			}
			throw new NoSuchElementException("Hashtable Enumerator");
		}

		// Iterator methods
		public boolean hasNext() {
			return hasMoreElements();
		}

		public T next() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			return nextElement();
		}

		public void remove() {
			if (!iterator)
				throw new UnsupportedOperationException();
			if (lastReturned == null)
				throw new IllegalStateException("Hashtable Enumerator");
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();

			synchronized (MyHashTable.this) {
				Entry[] tab = MyHashTable.this.table;
				int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

				for (Entry<K, V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
					if (e == lastReturned) {
						modCount++;
						expectedModCount++;
						if (prev == null)
							tab[index] = e.next;
						else
							prev.next = e.next;
						count--;
						lastReturned = null;
						return;
					}
				}
				throw new ConcurrentModificationException();
			}
		}
	}

	private static Enumeration emptyEnumerator = new EmptyEnumerator();
	private static Iterator emptyIterator = new EmptyIterator();

	/**
	 * A hashtable enumerator class for empty hash tables, specializes
	 * the general Enumerator
	 */
	private static class EmptyEnumerator implements Enumeration<Object> {

		EmptyEnumerator() {
		}

		public boolean hasMoreElements() {
			return false;
		}

		public Object nextElement() {
			throw new NoSuchElementException("Hashtable Enumerator");
		}
	}

	/**
	 * A hashtable iterator class for empty hash tables
	 */
	private static class EmptyIterator implements Iterator<Object> {

		EmptyIterator() {
		}

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new NoSuchElementException("Hashtable Iterator");
		}

		public void remove() {
			throw new IllegalStateException("Hashtable Iterator");
		}

	}

}
