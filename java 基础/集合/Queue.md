## Queue
`Queue` 是 `Collection` 的子接口，表示先进先出的队列，在尾部添加，从头部查看或删除。`Deque` 是 `Queue` 的子接口，表示更为通用的双端队列，可在头或尾进行查看、添加和删除。普通队列有两个主要的实现类，`LinkedList` 和 `ArrayDeque`，其中 `LinkedList` 基于链表实现，`ArrayDeque` 基于循环数组实现，一般而言，如果只需要 `Deque` 接口，`ArrayDeque` 的效率更高一些。`Deque` 有一个特殊的实现类 `PriorityQueue`，表示优先级队列，内部用堆实现的

## PriorityQueue
```java
public class PriorityQueue<E> extends AbstractQueue<E>
    implements java.io.Serializable {

  private static final int DEFAULT_INITIAL_CAPACITY = 11;
  transient Object[] queue; // non-private to simplify nested class access
  private int size = 0;
  private final Comparator<? super E> comparator;
  transient int modCount = 0; // non-private to simplify nested class access

  public PriorityQueue() {
    this(DEFAULT_INITIAL_CAPACITY, null);
  }

  public PriorityQueue(int initialCapacity) {
    this(initialCapacity, null);
  }

  public PriorityQueue(Comparator<? super E> comparator) {
    this(DEFAULT_INITIAL_CAPACITY, comparator);
  }

  public PriorityQueue(int initialCapacity,
                         Comparator<? super E> comparator) {
    // Note: This restriction of at least one is not actually needed,
    // but continues for 1.5 compatibility
    if (initialCapacity < 1)
        throw new IllegalArgumentException();
    this.queue = new Object[initialCapacity];
    this.comparator = comparator;
  }

  @SuppressWarnings("unchecked")
  public PriorityQueue(Collection<? extends E> c) {
    if (c instanceof SortedSet<?>) {
      SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
      this.comparator = (Comparator<? super E>) ss.comparator();
      initElementsFromCollection(ss);
    }
    else if (c instanceof PriorityQueue<?>) {
      PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
      this.comparator = (Comparator<? super E>) pq.comparator();
      initFromPriorityQueue(pq);
    }
    else {
      this.comparator = null;
      initFromCollection(c);
    }
  }

  @SuppressWarnings("unchecked")
  public PriorityQueue(PriorityQueue<? extends E> c) {
    this.comparator = (Comparator<? super E>) c.comparator();
    initFromPriorityQueue(c);
  }

  @SuppressWarnings("unchecked")
  public PriorityQueue(SortedSet<? extends E> c) {
    this.comparator = (Comparator<? super E>) c.comparator();
    initElementsFromCollection(c);
  }
}
```

### Queue
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

### 内部实现
#### 添加元素
```java
public boolean offer(E e) {
  if (e == null)
    throw new NullPointerException();
  modCount++;
  int i = size;
  if (i >= queue.length)
    grow(i + 1);
  size = i + 1;
  if (i == 0)
    queue[0] = e;
  else
    siftUp(i, e);
  return true;
}
```
```java
private void grow(int minCapacity) {
  int oldCapacity = queue.length;
  // Double size if small; else grow by 50%
  int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                   (oldCapacity + 2) :
                                   (oldCapacity >> 1));
  // overflow-conscious code
  if (newCapacity - MAX_ARRAY_SIZE > 0)
    newCapacity = hugeCapacity(minCapacity);
  queue = Arrays.copyOf(queue, newCapacity);
}
```
```java
private void siftUp(int k, E x) {
  if (comparator != null)
    siftUpUsingComparator(k, x);
  else
    siftUpComparable(k, x);
}
```
```java
@SuppressWarnings("unchecked")
private void siftUpUsingComparator(int k, E x) {
  while (k > 0) {
    int parent = (k - 1) >>> 1;
    Object e = queue[parent];
    if (comparator.compare(x, (E) e) >= 0)
      break;
    queue[k] = e;
    k = parent;
  }
  queue[k] = x;
}
```

#### 查看头部元素
```java
@SuppressWarnings("unchecked")
public E peek() {
  return (size == 0) ? null : (E) queue[0];
}
```

#### 删除头部元素
```java
@SuppressWarnings("unchecked")
public E poll() {
  if (size == 0)
    return null;
  int s = --size;
  modCount++;
  E result = (E) queue[0];
  E x = (E) queue[s];
  queue[s] = null;
  if (s != 0)
    siftDown(0, x);
  return result;
}
```
```java
private void siftDown(int k, E x) {
  if (comparator != null)
    siftDownUsingComparator(k, x);
  else
    siftDownComparable(k, x);
}
```
```java
@SuppressWarnings("unchecked")
private void siftDownUsingComparator(int k, E x) {
  int half = size >>> 1;
  while (k < half) {
    int child = (k << 1) + 1;
    Object c = queue[child];
    int right = child + 1;
    if (right < size &&
        comparator.compare((E) c, (E) queue[right]) > 0)
      c = queue[child = right];
    if (comparator.compare(x, (E) c) <= 0)
      break;
    queue[k] = c;
    k = child;
  }
  queue[k] = x;
}
```

#### 查找元素
```java
public boolean contains(Object o) {
  return indexOf(o) != -1;
}
```
```java
private int indexOf(Object o) {
  if (o != null) {
    for (int i = 0; i < size; i++)
      if (o.equals(queue[i]))
        return i;
  }
  return -1;
}
```

#### 根据值删除元素
```java
public boolean remove(Object o) {
  int i = indexOf(o);
  if (i == -1)
    return false;
  else {
    removeAt(i);
    return true;
  }
}
```
```java
@SuppressWarnings("unchecked")
private E removeAt(int i) {
  // assert i >= 0 && i < size;
  modCount++;
  int s = --size;
  if (s == i) // removed last element
    queue[i] = null;
  else {
    E moved = (E) queue[s];
    queue[s] = null;
    siftDown(i, moved);

    // 没有调整过
    if (queue[i] == moved) {
      siftUp(i, moved);
      if (queue[i] != moved)
        return moved;
    }
  }
  return null;
}
```

#### 构建初始堆
```java
private void initFromCollection(Collection<? extends E> c) {
  initElementsFromCollection(c);
  heapify();
}
```
```java
private void initElementsFromCollection(Collection<? extends E> c) {
  Object[] a = c.toArray();
  // If c.toArray incorrectly doesn't return Object[], copy it.
  if (a.getClass() != Object[].class)
    a = Arrays.copyOf(a, a.length, Object[].class);
  int len = a.length;
  if (len == 1 || this.comparator != null)
    for (int i = 0; i < len; i++)
      if (a[i] == null)
        throw new NullPointerException();
  this.queue = a;
  this.size = a.length;
}
```
```java
@SuppressWarnings("unchecked")
private void heapify() {
  for (int i = (size >>> 1) - 1; i >= 0; i--)
    siftDown(i, (E) queue[i]);
}
```

## ArrayDeque
`ArrayDeque` 可以看做先进先出的队列，也可以看做先进后出的栈
```java
public class ArrayDeque<E> extends AbstractCollection<E>
                           implements Deque<E>, Cloneable, Serializable {

  transient Object[] elements; // non-private to simplify nested class access
  transient int head;
  transient int tail;
  private static final int MIN_INITIAL_CAPACITY = 8;

  public ArrayDeque() {
    elements = new Object[16];
  }

  public ArrayDeque(int numElements) {
    allocateElements(numElements);
  }

  public ArrayDeque(Collection<? extends E> c) {
    allocateElements(c.size());
    addAll(c);
  }

  private void allocateElements(int numElements) {
    int initialCapacity = MIN_INITIAL_CAPACITY;
    // Find the best power of two to hold elements.
    // Tests "<=" because arrays aren't kept full.
    if (numElements >= initialCapacity) {
      initialCapacity = numElements;

      // 将高位的 1 复制到右边的每一位
      initialCapacity |= (initialCapacity >>>  1);
      initialCapacity |= (initialCapacity >>>  2);
      initialCapacity |= (initialCapacity >>>  4);
      initialCapacity |= (initialCapacity >>>  8);
      initialCapacity |= (initialCapacity >>> 16);
      initialCapacity++;

      if (initialCapacity < 0)   // Too many elements, must back off
        initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements
    }
    elements = new Object[initialCapacity];
  }
}
```

### 内部实现
`elements` 为存储元素的数组，`head` 与 `tail` 使得数组变为一个逻辑上的循环数组

#### 尾部添加
```java
public boolean add(E e) {
  addLast(e);
  return true;
}
```
```java
public void addLast(E e) {
  if (e == null)
    throw new NullPointerException();
  elements[tail] = e;
  if ( (tail = (tail + 1) & (elements.length - 1)) == head)
    doubleCapacity();
}
```
```java
private void doubleCapacity() {
  assert head == tail;
  int p = head;
  int n = elements.length;
  int r = n - p; // number of elements to the right of p
  int newCapacity = n << 1;
  if (newCapacity < 0)
    throw new IllegalStateException("Sorry, deque too big");
  Object[] a = new Object[newCapacity];
  System.arraycopy(elements, p, a, 0, r);
  System.arraycopy(elements, 0, a, r, p);
  elements = a;
  head = 0;
  tail = n;
}
```

#### 头部添加
```java
public void addFirst(E e) {
  if (e == null)
    throw new NullPointerException();
  elements[head = (head - 1) & (elements.length - 1)] = e;
  if (head == tail)
    doubleCapacity();
}
```

#### 头部删除
```java
public E removeFirst() {
  E x = pollFirst();
  if (x == null)
    throw new NoSuchElementException();
  return x;
}
```
```java
public E pollFirst() {
  int h = head;
  @SuppressWarnings("unchecked")
  E result = (E) elements[h];
  // Element is null if deque empty
  if (result == null)
    return null;
  elements[h] = null;     // Must null out slot
  head = (h + 1) & (elements.length - 1);
  return result;
}
```

#### 尾部删除
```java
public E removeLast() {
  E x = pollLast();
  if (x == null)
    throw new NoSuchElementException();
  return x;
}
```
```java
public E pollLast() {
  int t = (tail - 1) & (elements.length - 1);
  @SuppressWarnings("unchecked")
  E result = (E) elements[t];
  if (result == null)
    return null;
  elements[t] = null;
  tail = t;
  return result;
}
```

#### 查看长度
```java
public int size() {
  return (tail - head) & (elements.length - 1);
}
```

#### 检查元素是否存在
```java
public boolean contains(Object o) {
  if (o == null)
    return false;
  int mask = elements.length - 1;
  int i = head;
  Object x;

  // ArrayDeque 中有效元素不允许为 null
  while ( (x = elements[i]) != null) {
    if (o.equals(x))
      return true;
    i = (i + 1) & mask;
  }
  return false;
}
```

#### toArray
```java
public Object[] toArray() {
  return copyElements(new Object[size()]);
}

@SuppressWarnings("unchecked")
public <T> T[] toArray(T[] a) {
  int size = size();
  if (a.length < size)
    a = (T[])java.lang.reflect.Array.newInstance(
              a.getClass().getComponentType(), size);
  copyElements(a);
  if (a.length > size)
    a[size] = null;
  return a;
}
```
```java
private <T> T[] copyElements(T[] a) {
  if (head < tail) {
    System.arraycopy(elements, head, a, 0, size());
  } else if (head > tail) {
    int headPortionLen = elements.length - head;
    System.arraycopy(elements, head, a, 0, headPortionLen);
    System.arraycopy(elements, 0, a, headPortionLen, tail);
  }
  return a;
}
```