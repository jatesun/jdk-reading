package com.jatesun.collection.set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 
 * @author jatesun
 * @description hashset内部维护hashmap，用key来存储值。很简单，不做解释
 * @question
 * @date 2017年3月25日
 * @param <E>
 */
public class MyHashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable {

	static final long serialVersionUID = -5024744406713321676L;

	private transient HashMap<E, Object> map;

	private static final Object PRESENT = new Object();//占位对象，放在hashmap中value中

	public MyHashSet() {
		map = new HashMap<E, Object>();
	}

	public MyHashSet(Collection<? extends E> c) {
		map = new HashMap<E, Object>(Math.max((int) (c.size() / .75f) + 1, 16));
		addAll(c);
	}

	public MyHashSet(int initialCapacity, float loadFactor) {
		map = new HashMap<E, Object>(initialCapacity, loadFactor);
	}

	public MyHashSet(int initialCapacity) {
		map = new HashMap<E, Object>(initialCapacity);
	}

	MyHashSet(int initialCapacity, float loadFactor, boolean dummy) {
		map = new LinkedHashMap<E, Object>(initialCapacity, loadFactor);
	}

	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	public boolean add(E e) {
		return map.put(e, PRESENT) == null;
	}

	public boolean remove(Object o) {
		return map.remove(o) == PRESENT;
	}

	public void clear() {
		map.clear();
	}

	public Object clone() {
		try {
			MyHashSet<E> newSet = (MyHashSet<E>) super.clone();
			newSet.map = (HashMap<E, Object>) map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		// Write out any hidden serialization magic
		s.defaultWriteObject();

		// Write out HashMap capacity and load factor
		// s.writeInt(map.capacity());
		// s.writeFloat(map.loadFactor());

		// Write out size
		s.writeInt(map.size());

		// Write out all elements in the proper order.
		for (Iterator i = map.keySet().iterator(); i.hasNext();)
			s.writeObject(i.next());
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		// Read in any hidden serialization magic
		s.defaultReadObject();

		// Read in HashMap capacity and load factor and create backing HashMap
		int capacity = s.readInt();
		float loadFactor = s.readFloat();
		// map = (((MyHashSet)this) instanceof LinkedHashSet ?
		// new LinkedHashMap<E,Object>(capacity, loadFactor) :
		// new HashMap<E,Object>(capacity, loadFactor));

		// Read in size
		int size = s.readInt();

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++) {
			E e = (E) s.readObject();
			map.put(e, PRESENT);
		}
	}

}
