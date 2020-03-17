## List
`Collection` 表示的数据集合有基本的增、删、查、遍历等方法，但没有定义元素间的顺序或位置，也没有规定是否有重复元素。
`List` 是 `Collection` 的子接口，表示有顺序或位置的数据集合，增加了根据索引位置进行操作的方法。它有两个主要的实现类，`ArrayList` 和 `LinkedList`，`ArrayList` 基于数组实现，`LinkedList` 基于链表实现，`ArrayList` 的随机访问效率很高，但从中间插入和删除元素需要移动元素，效率比较低，`LinkedList` 随机访问效率比较低，但增删元素只需要调整邻近节点的链接

## ArrayList
```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

  private static final long serialVersionUID = 8683452581122892189L;
  private static final int DEFAULT_CAPACITY = 10;
  private static final Object[] EMPTY_ELEMENTDATA = {};
  private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
  transient Object[] elementData;
  private int size;
  
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
      this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
      this.elementData = EMPTY_ELEMENTDATA;
    } else {
      throw new IllegalArgumentException("Illegal Capacity: "+
                                           initialCapacity);
    }
  }

  public ArrayList() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
  }

  public ArrayList(Collection<? extends E> c) {
    elementData = c.toArray();
    if ((size = elementData.length) != 0) {
      // c.toArray might (incorrectly) not return Object[] (see 6260652)
      if (elementData.getClass() != Object[].class)
        elementData = Arrays.copyOf(elementData, size, Object[].class);
    } else {
      // replace with empty array.
      this.elementData = EMPTY_ELEMENTDATA;
    }
  }
}
```

### 实现的接口
`List`: 表示有顺序或者位置的顺序集合，在 `Collection` 基础上进行了扩展
`RandomAccess`: 标记接口，表示该类可以随机访问

### 内部实现
#### 添加元素
```java
public boolean add(E e) {
  ensureCapacityInternal(size + 1);  // Increments modCount!!
  elementData[size++] = e;
  return true;
}
```
```java
private void ensureCapacityInternal(int minCapacity) {
  if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
    minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
  }

  ensureExplicitCapacity(minCapacity);
}
```
```java
private void ensureExplicitCapacity(int minCapacity) {
  modCount++;

  // overflow-conscious code
  if (minCapacity - elementData.length > 0)
    grow(minCapacity);
}
```
```java
private void grow(int minCapacity) {
  // overflow-conscious code
  int oldCapacity = elementData.length;
  int newCapacity = oldCapacity + (oldCapacity >> 1);
  if (newCapacity - minCapacity < 0)
    newCapacity = minCapacity;
  if (newCapacity - MAX_ARRAY_SIZE > 0)
    newCapacity = hugeCapacity(minCapacity);
  // minCapacity is usually close to size, so this is a win:
  elementData = Arrays.copyOf(elementData, newCapacity);
}
```
```java
private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // overflow
        throw new OutOfMemoryError();
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
}
```

#### 删除元素
按索引删除
```java
public E remove(int index) {
  rangeCheck(index);

  modCount++;
  E oldValue = elementData(index);

  int numMoved = size - index - 1;
  if (numMoved > 0)
    System.arraycopy(elementData, index+1, elementData, index,
                       numMoved);
  elementData[--size] = null; // clear to let GC do its work

  return oldValue;
}

private void rangeCheck(int index) {
    if (index >= size)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
```
按值删除
```java
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
```
```java
private void fastRemove(int index) {
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,
                         numMoved);
    elementData[--size] = null; // clear to let GC do its work
}
```

#### 迭代器
```java
public Iterator<E> iterator() {
    return new Itr();
}
```
```java
private class Itr implements Iterator<E> {
    int cursor;       // index of next element to return
    int lastRet = -1; // index of last element returned; -1 if no such
    int expectedModCount = modCount;

    public boolean hasNext() {
        return cursor != size;
    }

    @SuppressWarnings("unchecked")
    public E next() {
        checkForComodification();
        int i = cursor;
        if (i >= size)
            throw new NoSuchElementException();
        Object[] elementData = ArrayList.this.elementData;
        if (i >= elementData.length)
            throw new ConcurrentModificationException();
        cursor = i + 1;
        return (E) elementData[lastRet = i];
    }

    public void remove() {
        if (lastRet < 0)
            throw new IllegalStateException();
        checkForComodification();

        try {
            ArrayList.this.remove(lastRet);
            cursor = lastRet;
            lastRet = -1;
            expectedModCount = modCount;
        } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
        }
    }

    final void checkForComodification() {
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }
}
```

#### 遍历
迭代过程中调用容器删除方法会导致异常
```java
public void remove(ArrayList<Integer> list) {
  for (Integer elem : list) {
    if (elem < 100) {
      list.remove(elem);
    }
  }
}
```
如果使用迭代器进行删除则可以避免，这与 `ArrayList` 内部迭代器实现有关
```java
public void remove(ArrayList<Integer> list) {
  Iterator<Integer> iter = list.iterator();
  while (iter.hasNext()) {
    if (iter.next() <= 100) {
      iter.remove();
    }
  }
}
```
需要注意，即使使用迭代器删除，调用 `remove` 方法前需要先调用 `next`，如果在调用 `remove` 之前没有调用 `next` 也会引发异常
```java
public static void removeAll(ArrayList<Integer> list) {
  Iterator<Integer> iter = list.iterator();
  while (list.hasNext()) {
    iter.next();
    iter.remove();
  }
}
```

#### `subList`
相当于返回原列表指定区间视图，对子列表的改动也会反应到原列表上
```java
public List<E> subList(int fromIndex, int toIndex) {
    subListRangeCheck(fromIndex, toIndex, size);
    return new SubList(this, 0, fromIndex, toIndex);
}
```

#### 与数组的转换
```java
public Object[] toArray() {
  return Arrays.copyOf(elementData, size);
}

public <T> T[] toArray(T[] a) {
  if (a.length < size)
    return (T[]) Arrays.copyOf(elementData, size, a.getClass());
  System.arraycopy(elementData, 0, a, 0, size);
  if (a.length > size)
    a[size] = null;
  return a;
}
```
```java
Integer[] arr = {1, 2, 3};

// 返回的 list 的实现类并非 java.util.ArrayList 不能使用 ArrayList 所有方法
List<Integer> list = Arrays.asList(arr);
// 可以使用 ArrayList 完整方法
List<Integer> list = new ArrayList<Integer>(Arrays.asList(arr));
```

## LinkedList
```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable {

  transient int size = 0;
  transient Node<E> first;
  transient Node<E> last;
  
  public LinkedList() {
  }

  public LinkedList(Collection<? extends E> c) {
    this();
    addAll(c);
  }

  private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;

    Node(Node<E> prev, E element, Node<E> next) {
      this.item = element;
      this.next = next;
      this.prev = prev;
    }
  }
}
```

### Queue 接口
`Queue`: 先进后出，在尾部添加元素，从头部删除元素
```java
public interface Queue<E> extends Collection<E> {
  boolean add(E e);
  boolean offer(E e);
  E remove();
  E poll();
  E element();
  E peek();
}
```

- `add` 与 `offer` 在尾部添加元素
- `element` 与 `peek` 返回头部元素，但不改变队列
- `remove` 与 `poll` 返回头部元素，并从队列中删除
- 队列为空时，`element` 与 `remove` 会抛出异常
- 队列为空时，`peek` 与 `poll` 返回 `null`
- 队列满时，`add` 抛出异常
- 队列满时，`offer` 返回 `false`

### Deque 接口
- xxxFirst 操作头部，xxxLast 操作尾部
- 队列为空时，getXXX/removeXXX 抛出异常
- 队列为空时，peekXXX/pollXXX 返回 `null`
- 队列满时，addXXX 抛出异常，offerXXX 返回 `false`
```java
public interface Deque<E> extends Queue<E> {
  void addFirst(E e);
  void addLast(E e);

  E getFirst();
  E getLast();

  boolean offerFirst(E e);
  boolean offerLast(E e);

  E peekFirst();
  E peekLast();

  E pollFirst();
  E pollLast();

  E removeFirst();
  E removeLast();

  // 从后往前遍历
  Iterator<E> descendingIterator();
}
```
`Deque` 还具有 `Stack` 功能
```java
public interface Deque<E> extends Queue<E> {
  void push(E e);
  E pop();
  E peek();
}
```

### 内部实现
`ArrayList` 内部是数组，`LinkedList` 内部是双向链表

#### 添加元素
```java
public boolean add(E e) {
  linkLast(e);
  return true;
}
```
```java
void linkLast(E e) {
  final Node<E> l = last;
  final Node<E> newNode = new Node<>(l, e, null);
  last = newNode;
  if (l == null)
    first = newNode;
  else
    l.next = newNode;
  size++;
  modCount++;
}
```

#### 获取元素
按索引获取
```java
public E get(int index) {
  checkElementIndex(index);
  return node(index).item;
}
```
```java
private void checkElementIndex(int index) {
  if (!isElementIndex(index))
    throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}

private boolean isElementIndex(int index) {
  return index >= 0 && index < size;
}
```
```java
Node<E> node(int index) {
  // assert isElementIndex(index);

  if (index < (size >> 1)) {
    Node<E> x = first;
    for (int i = 0; i < index; i++)
      x = x.next;
    return x;
  } else {
    Node<E> x = last;
    for (int i = size - 1; i > index; i--)
      x = x.prev;
    return x;
  }
}
```
按元素内容获取
```java
public int indexOf(Object o) {
  int index = 0;
  if (o == null) {
    for (Node<E> x = first; x != null; x = x.next) {
      if (x.item == null)
        return index;
      index++;
    }
  } else {
    for (Node<E> x = first; x != null; x = x.next) {
      if (o.equals(x.item))
        return index;
      index++;
    }
  }
  return -1;
}
```

#### 插入元素
```java
public void add(int index, E element) {
  checkPositionIndex(index);

  if (index == size)
    linkLast(element);
  else
    linkBefore(element, node(index));
}
```
```java
private void checkPositionIndex(int index) {
  if (!isPositionIndex(index))
    throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}

private boolean isPositionIndex(int index) {
  return index >= 0 && index <= size;
}
```
```java
void linkBefore(E e, Node<E> succ) {
  // assert succ != null;
  final Node<E> pred = succ.prev;
  final Node<E> newNode = new Node<>(pred, e, succ);
  succ.prev = newNode;
  if (pred == null)
    first = newNode;
  else
    pred.next = newNode;
  size++;
  modCount++;
}
```

#### 删除元素
```java
public E remove(int index) {
  checkElementIndex(index);
  return unlink(node(index));
}
```
```java
E unlink(Node<E> x) {
  // assert x != null;
  final E element = x.item;
  final Node<E> next = x.next;
  final Node<E> prev = x.prev;

  if (prev == null) {
    first = next;
  } else {
    prev.next = next;
    x.prev = null;
  }

  if (next == null) {
    last = prev;
  } else {
    next.prev = prev;
    x.next = null;
  }

  x.item = null;
  size--;
  modCount++;
  return element;
}
```

## ArrayList LinkedList Vector
`Vector` 与 `ArrayList` 作为动态数组，内部元素以数组形式顺序存储，适合随机访问。`LinkedList` 进行节点插入、删除比较高效，但是随机访问性能比动态数组慢。`ArrayList` 与 `LinkedList` 为线程不安全的，`Vector` 为线程安全的，因此效率比 `ArrayList` 要低