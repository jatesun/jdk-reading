package com.jatesun.collection.set;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//继承自hashset，唯一区别是构造时使用了linkedhashlist
public class MyLinkedHashSet<E> extends MyHashSet<E> implements Set<E>, Cloneable, java.io.Serializable {

	private static final long serialVersionUID = -2851667679971038690L;

	public MyLinkedHashSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor, true);
	}

	public MyLinkedHashSet(int initialCapacity) {
		super(initialCapacity, .75f, true);
	}

	public MyLinkedHashSet() {
		super(16, .75f, true);
	}

	public MyLinkedHashSet(Collection<? extends E> c) {
		super(Math.max(2 * c.size(), 11), .75f, true);
		addAll(c);
	}

}
