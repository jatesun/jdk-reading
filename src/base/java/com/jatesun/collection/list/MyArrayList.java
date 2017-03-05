package com.jatesun.collection.list;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.RandomAccess;

/**  
 * @Title: MyArrayList.java
 * @Description: 内部使用数组实现list接口的集合类
 * 	tips:
 * 		1、写代码对参数校验是必须的，也是反正NPE（null pointer exception）的好方法。
 * 		2、写一个方法步骤基本上为：①校验参数 ②执行方法内操作 处理后续（返回结果等）
 * @author: jateSun  
 * @date: 2017年3月4日 下午6:12:17
 * @version: V1.0  
 */
public class MyArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable,
		java.io.Serializable {

	private static final long serialVersionUID = 8683452581122892189L;

	// 存储元素的数组，泛型E其实底部没有泛型，还是使用的object数组存储。
	private transient Object[] elementData;

	// 存储元素个数
	private int size;

	// 指定初始容量的构造方法
	public MyArrayList(int initialCapacity) {
		super();// 先调用父类构造方法，大多数情况下这是个好习惯
		// 校验参数合法性，一般代码里面第一步就是校验参数合法性。
		if (initialCapacity < 0) throw new IllegalArgumentException("Illegal Capacity: "
				+ initialCapacity);
		// 初始化存放元素的数组，大小为入参大小。
		this.elementData = new Object[initialCapacity];
	}

	public MyArrayList() {
		this(10);
	}

	// 入参为集合的构造方法
	public MyArrayList(Collection<? extends E> c) {
		elementData = c.toArray();// elementData指向c转为数组后的元素
		size = elementData.length;
		// c.toArray might (incorrectly) not return Object[] (see 6260652)
		// 由于c.toArray方法的实现不同，不同collection的实现可能不一样，所以需要判断保证elementdata存储的是object类型数组。
		// 不一样的话需要转化
		if (elementData.getClass() != Object[].class) elementData = Arrays.copyOf(elementData,
				size, Object[].class);
	}

	// elementdata数组中有额外的空余空间（size小于length），调用此方法去除空余空间。
	public void trimToSize() {
		modCount++;
		int oldCapacity = elementData.length;
		if (size < oldCapacity) {
			elementData = Arrays.copyOf(elementData, size);
		}
	}

	// 确定数组是否还有空间存储入参大小的元素，不够扩容
	public void ensureCapacity(int minCapacity) {
		modCount++;
		int oldCapacity = elementData.length;// 取得当前容量大小(取得是数组长度，而不是size)
		if (minCapacity > oldCapacity) {
			Object oldData[] = elementData;// olddata指向原数组
			int newCapacity = (oldCapacity * 3) / 2 + 1;// 扩容1.5倍+1
			if (newCapacity < minCapacity) newCapacity = minCapacity;// 扩容后还不够就直接用入参大小
			// 将数组拷贝到新的大小中的数组
			elementData = Arrays.copyOf(elementData, newCapacity);
		}
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	// 包含某元素
	public boolean contains(Object o) {
		return indexOf(o) >= 0;// 调用indexof
	}

	// 查找某元素在数组中的位置，返回索引位置
	public int indexOf(Object o) {
		// arraylist允许存null，所以分为两种情况（因为null调用equals会有空指针异常）
		if (o == null) {
			// o为null，for循环迭代判断是否存在null，存在返回索引
			for (int i = 0; i < size; i++)
				if (elementData[i] == null) return i;
		} else {
			// o不为null，for循环得带判断是否数组存在跟o相等，相等返回索引
			for (int i = 0; i < size; i++)
				if (o.equals(elementData[i])) return i;
		}
		return -1;
	}

	// 返回入参靠近数组末端的索引，跟上述算法相似，不过从数组末端开始
	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size - 1; i >= 0; i--)
				if (elementData[i] == null) return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (o.equals(elementData[i])) return i;
		}
		return -1;// 未找到返回-1
	}

	// 数组拷贝，但是是浅拷贝，不拷贝数组元素
	public Object clone() {
		try {
			MyArrayList<E> v = (MyArrayList<E>) super.clone();
			v.elementData = Arrays.copyOf(elementData, size);
			v.modCount = 0;
			return v;
		} catch (CloneNotSupportedException e) {
			// 由于是cloneable，不会抛出错误
			throw new InternalError();
		}
	}

	public Object[] toArray() {
		return Arrays.copyOf(elementData, size);
	}

	public <T> T[] toArray(T[] a) {
		if (a.length < size)
		// Make a new array of a's runtime type, but my contents:
		return (T[]) Arrays.copyOf(elementData, size, a.getClass());
		System.arraycopy(elementData, 0, a, 0, size);
		if (a.length > size) a[size] = null;
		return a;
	}

	// get方法，直接返回
	public E get(int index) {
		RangeCheck(index);// 校验index合法
		return (E) elementData[index];// 返回对应index的元素
	}

	// set方法，替换index元素，返回旧元素
	public E set(int index, E element) {
		RangeCheck(index);// 校验index合法性
		// 替换操作
		E oldValue = (E) elementData[index];
		elementData[index] = element;
		return oldValue;
	}

	// 新增操作
	public boolean add(E e) {
		ensureCapacity(size + 1); // 检验是否需要扩容
		elementData[size++] = e;// 数组末尾增加元素
		return true;
	}

	public void add(int index, E element) {
		// 校验参数
		if (index > size || index < 0) throw new IndexOutOfBoundsException("Index: " + index
				+ ", Size: " + size);
		ensureCapacity(size + 1); // 校验是否扩容
		// 将index以及后面的元素往后移动一个元素（操作费时）
		System.arraycopy(elementData, index, elementData, index + 1, size - index);
		elementData[index] = element;// 将元素插入index
		size++;
	}

	public E remove(int index) {
		RangeCheck(index);// 校验index
		modCount++;
		E oldValue = (E) elementData[index];// 暂存删除的元素
		// 计算移动元素个数，删除元素后面所有元素前移一位
		int numMoved = size - index - 1;
		if (numMoved > 0) System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		elementData[--size] = null; // 置null，让gc回收
		return oldValue;
	}

	public boolean remove(Object o) {
		// 分两种情况删除，null和非null
		if (o == null) {
			for (int index = 0; index < size; index++)
				if (elementData[index] == null) {
					fastRemove(index);// 跟remove方法差不多，不过不校验参数，不返回删除元素
					return true;
				}
		} else {
			for (int index = 0; index < size; index++)
				if (o.equals(elementData[index])) {
					fastRemove(index);
					return true;
				}
		}
		return false;
	}

	private void fastRemove(int index) {
		modCount++;
		int numMoved = size - index - 1;
		if (numMoved > 0) System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		elementData[--size] = null; // Let gc do its work
	}

	public void clear() {
		modCount++;
		// Let gc do its work
		for (int i = 0; i < size; i++)
			elementData[i] = null;
		size = 0;
	}

	public boolean addAll(Collection<? extends E> c) {
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacity(size + numNew); // 校验是否需要扩容
		System.arraycopy(a, 0, elementData, size, numNew);// 加入新元素
		size += numNew;
		return numNew != 0;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		// 校验index
		if (index > size || index < 0) throw new IndexOutOfBoundsException("Index: " + index
				+ ", Size: " + size);
		// 跟addAll相似，不同的为从index开始添加元素
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacity(size + numNew);
		int numMoved = size - index;
		if (numMoved > 0) System.arraycopy(elementData, index, elementData, index + numNew,
				numMoved);
		System.arraycopy(a, 0, elementData, index, numNew);
		size += numNew;
		return numNew != 0;
	}

	protected void removeRange(int fromIndex, int toIndex) {
		// 比较简单，将后面的元素想前面移动(toindex-fromindex)元素，然后将数组后面的引用置null
		modCount++;
		int numMoved = size - toIndex;
		System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
		int newSize = size - (toIndex - fromIndex);
		while (size != newSize)
			elementData[--size] = null;
	}

	private void RangeCheck(int index) {
		if (index >= size) throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
				+ size);
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		// Write out element count, and any hidden stuff
		int expectedModCount = modCount;
		s.defaultWriteObject();

		// Write out array length
		s.writeInt(elementData.length);

		// Write out all elements in the proper order.
		for (int i = 0; i < size; i++)
			s.writeObject(elementData[i]);

		if (modCount != expectedModCount) { throw new ConcurrentModificationException(); }

	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException,
			ClassNotFoundException {
		// Read in size, and any hidden stuff
		s.defaultReadObject();

		// Read in array length and allocate array
		int arrayLength = s.readInt();
		Object[] a = elementData = new Object[arrayLength];

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++)
			a[i] = s.readObject();
	}

}
