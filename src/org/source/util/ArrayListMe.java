package org.source.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Override;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Objects;

public class ArrayListMe<E> extends AbstractListMe<E> implements ListMe<E>, Cloneable {
	private static final int DEFAULT_CAPACITY = 10;
	private static final Object[] EMPTY_ELEMENTDATA = {};
	private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
	
	transient Object[] elementData;
	private int size;
	
	public ArrayListMe() {
		this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
	}
	
	public ArrayListMe(int initialCapacity) {
		if (initialCapacity > 0) {
			this.elementData = new Object[initialCapacity];
		} else if (initialCapacity == 0) {
			this.elementData = EMPTY_ELEMENTDATA;
		} else {
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		}
	}
	
	public ArrayListMe(CollectionMe<? extends E> c) {
		elementData = c.toArray();
		if ((size = elementData.length) != 0) {
			if (elementData.getClass() != Object[].class) //如果toArray返回的数组类型不是Object[]，则按指定类型重新copy一份，为什么？
				elementData = Arrays.copyOf(elementData, size, Object[].class);
		} else {
			this.elementData = EMPTY_ELEMENTDATA;
		}
	}
	
	/**
	 * 去除多余空间
	 */
	public void trimToSize() {
		modCount++; //?
		if (size < elementData.length) { //如果实际的size小于数组当前的长度，则copy一份减少存储空间，如果里面没有值则置空
			elementData = (size == 0) 
				? EMPTY_ELEMENTDATA 
				: Arrays.copyOf(elementData, size);
		}
	}
	
	/**
	 * 公共方法，list接口中未声明该方法，如果是直接创建的ArrayList对象，可通过该方法手动扩展数组的长度，但是这样并不安全
	 */
	public void ensureCapacity(int minCapacity) {
		//最小扩展数，如果当前数据不为空，则大于0就好，数据为空，则扩展数必须大于默认的空间（10）
		int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
				? 0
				: DEFAULT_CAPACITY;
		
		if (minCapacity > minExpand)
			ensureExplicitCapacity(minCapacity);
	}
	
	/**
	 * @param minCapacity 所需的最小空间（内部方法）
	 */
	private void ensureCapacityInternal(int minCapacity) {
		if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
			minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity); //所需的最小空间和默认的最小空间，取最大值
		
		ensureExplicitCapacity(minCapacity);
	}
	
	private void ensureExplicitCapacity(int minCapacity) {
		modCount++;
		
		if (minCapacity - elementData.length > 0) //所需的最小空间数大于当前的size，扩展数组大小
			grow(minCapacity);
	}

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	private void grow(int minCapacity) {
		int oldCapacity = elementData.length;
		int newCapacity = oldCapacity + (oldCapacity >> 1); //扩展1.5倍
		if (newCapacity - minCapacity < 0) //比自动扩展的要大
			newCapacity = minCapacity;
		if (newCapacity - MAX_ARRAY_SIZE > 0) //如果比允许的最大空间数大，进行截取
			newCapacity = hugeCapacity(minCapacity);
		elementData = Arrays.copyOf(elementData, newCapacity);
	}

	private static int hugeCapacity(int minCapacity) { //用静态方法?
        if (minCapacity < 0)
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public boolean isEmpty() {
		return size == 0;
	}
	
	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}
	
	@Override
	public int indexOf(Object o) {
		if (o == null) {
			for (int i = 0; i < size; i++) {
				if (elementData[i] == null)
					return i;
			}
		}else {
			for (int i = 0; i < size; i++) {
				if (o.equals(elementData[i]))
					return i;
			}
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object o) {
		if (o == null) {
			for (int i = size-1; i >=0; i--)
				if(elementData[i] == null)
					return i;
		}else {
			for (int i = size-1; i >=0; i--)
				if(o.equals(elementData[i]))
					return i;
		}
		return -1;
	}
	
	@Override
	public Object clone() {
		try {
			ArrayListMe<?> v = (ArrayListMe<?>) super.clone(); //克隆对象
			v.elementData = Arrays.copyOf(elementData, size); //去除多余的空间，节省开支
			v.modCount = 0; //初始化
			return v;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}
	
	@Override
	public Object[] toArray() {
		return Arrays.copyOf(elementData, size);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (a.length < size) //长度小于当前list，以a的类型把当前list的内容复一份并返回
			return (T[]) Arrays.copyOf(elementData, size, a.getClass());
		System.arraycopy(elementData, 0, a, 0, size); //把内容复制到a
		if (a.length > size) //如果a的空间比按原数组大，最后一位置为null，来确定列表的长度
			a[size] = null;
		return a;
	}
	
	@SuppressWarnings("unchecked")
	E elementData(int index) {
		return (E) elementData[index];
	}
	
	@Override
	public E get(int index) {
		rangeCheck(index);
		return elementData(index);
	}
	
	@Override
	public E set(int index, E element) {
		rangeCheck(index);
		
		E oldValue = elementData(index);
		elementData[index] = element;
		return oldValue;
	}
	
	/** 
	 * 事实证明可以加null元素，关键是size要++
	 */
	@Override
	public boolean add(E e) {
		ensureCapacityInternal(size + 1); //所需空间 = 当前size + 1
		elementData[size++] = e;
		return true;
	}
	
	/**
	 * @param index 索引位置只能<=size，即前面或在末尾位置插入元素
	 * 如数组a[1,2]，size=2，那只能在a[0-2]的位置上赋值，a[2]相当于add方法
	 * index=1相当于把原a[1]，a[2]的元素往后挪一位，然后在a[1]位置插入新的元素
	 */
	@Override
	public void add(int index, E element) {
		rangeCheckForAdd(index);
		ensureCapacityInternal(size + 1);
		System.arraycopy(elementData, index, elementData,
				index + 1, size - index); //把index位置后面的元素统一右移一位
		elementData[index] = element;
		size++;
	}

	@Override
	public E remove(int index) {
		rangeCheck(index);
		
		modCount++;
		E oldValue = elementData(index);
		int numMoved = size - index - 1;
		if (numMoved > 0) //根据移除元素的位置，决定有多少个元素需要复制向左移动，把原位置的元素覆盖掉
			System.arraycopy(elementData, index + 1, 
					elementData, index, numMoved);
		elementData[--size] = null; //多出来的位置设为null,让GC回收，不置的话会出现重复元素，因为copy只是复制覆盖，没被覆盖掉的元素还在那
		return oldValue;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o == null) {
			for (int index = 0; index < size; index++)
				if (elementData[index] == null) {
					fastRemove(index);
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
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData,
					index, numMoved);
		elementData[--size] = null;
		
	}
	
	@Override
	public void clear() {
		modCount++;
		
		for (int i = 0; i < size; i++)
			elementData[i] = null; //数组置null，GC回收
		
		size = 0;
	}
	
	@Override
	public boolean addAll(CollectionMe<? extends E> c) {
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacityInternal(size + numNew);
		System.arraycopy(a, 0, elementData, size, numNew);
		size += numNew;
		return numNew != 0;
	}
	
	@Override
	public boolean addAll(int index, CollectionMe<? extends E> c) {
		rangeCheckForAdd(index);
		
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacityInternal(size + numNew);
		
		int numMoved = size - index;
		if (numMoved > 0)  //插在元素中间，后面的元素需要挪
			System.arraycopy(elementData, index, elementData,
					index + numNew, numMoved);
		
		//index等于size，那接着往后加就行了
		System.arraycopy(a, 0, elementData, index, numNew);
		size += numNew;
		return numNew != 0;
	}
	
	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		modCount++;
		int numMoved = size - toIndex; //后面元素需要移动的位置
		System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
		
		//把空的位置，手动置为null
		int newSize = size - (toIndex-fromIndex);
		for (int i = newSize; i < size; i++) {
			elementData[i] = null;
		}
		size = newSize;
	}
	
	private void rangeCheck(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	private String outOfBoundsMsg(int index) {
		return "Index: " + index + ", Size: " + size;
	}
	
	@Override
	public boolean removeAll(CollectionMe<?> c) {
		Objects.requireNonNull(c);
		return batchRemove(c, false);
	}
	
	@Override
	public boolean retainAll(CollectionMe<?> c) {
		Objects.requireNonNull(c);
		return batchRemove(c, true);
	}

	/**
	 * ?
	 * @param c
	 * @param complement
	 * @return
	 */
	private boolean batchRemove(CollectionMe<?> c, boolean complement) {
		final Object[] elementData = this.elementData;
		int r = 0, w = 0;
		boolean modified = false;
		try {
			for (; r < size; r++)
				if (c.contains(elementData[r]) == complement) //false 把不包含的元素留下来，true把包含的元素留下来
					elementData[w++] = elementData[r];
		} finally {
			// Preserve behavioral compatibility with AbstractCollection,
			// even if c.contains() throws.
			if (r != size) {
				System.arraycopy(elementData, r, elementData, w, size - r);
				w += size - r;
			}
			if (w != size) {
				// clear to let GC do its work
				for (int i = w; i < size; i++)
					elementData[i] = null;
				modCount += size - w;
				size = w;
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * non-static and non-transient fields 
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		int expectedModCount = modCount;
		s.defaultWriteObject();
		s.writeInt(size);
		
		for (int i = 0; i < size; i++) {
			s.writeObject(elementData[i]);
		}
		
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
	}
	
	private void readObject(ObjectInputStream s) 
		throws ClassNotFoundException, IOException {
		elementData = EMPTY_ELEMENTDATA;
		
		s.defaultReadObject();
		s.readInt();
		
		if (size > 0) {
			ensureCapacityInternal(size);
			
			Object[] a = elementData;
			for (int i = 0; i < size; i++) {
				a[i] = s.readObject();
			}
		}
	}

}
