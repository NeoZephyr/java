## List
Collection 表示的数据集合有基本的增、删、查、遍历等方法，但没有定义元素间的顺序或位置，也没有规定是否有重复元素。

List 是 Collection 的子接口，表示有顺序或位置的数据集合，增加了根据索引位置进行操作的方法。它有两个主要的实现类，ArrayList 和 LinkedList，ArrayList 基于数组实现，LinkedList 基于链表实现


## ArrayList
ArrayList 实现了 List 接口，继承了 AbstractList 抽象类，底层是数组实现的，并且实现了自增扩容数组大小

ArrayList 实现了 Cloneable 接口和 Serializable 接口，可以实现克隆和序列化

ArrayList 实现了 RandomAccess 接口，表示能快速随机访问

```java
private static final int DEFAULT_CAPACITY = 10;
transient Object[] elementData;
private int size;
```
由于 ArrayList 的数组是基于动态扩增的，所以并不是所有被分配的内存空间都存储了数据。如果采用外部序列化法实现数组的序列化，会序列化整个数组
ArrayList 为了避免这些没有存储数据的内存空间被序列化，内部提供了两个私有方法 writeObject 以及 readObject 来自我完成序列化与反序列化，从而在序列化与反序列化数组时节省了空间和时间
因此使用 transient 修饰数组，防止对象数组被其他外部方法序列化

当 ArrayList 新增元素时，如果所存储的元素已经超过其已有大小，它会计算元素大小后再进行动态扩容，数组的扩容会导致整个数组进行一次内存复制。因此，在初始化 ArrayList 时指定初始大小，有助于减少数组的扩容次数，从而提高系统性能

如果只是在数组末尾添加元素，那么在大量新增元素的场景下，ArrayList 的性能反而比其他 List 集合的性能要好

```java
List<String> list = new ArrayList<String>(Arrays.asList("kafka", "stream", "spark", "java"));

for (String word : list) {
    if (word.equals("java")) {
        list.remove(word);
    }
}
```
在这里，for 循环被解释为迭代器，在使用迭代器遍历时，ArrayList 内部创建了一个内部迭代器，在使用 next() 方法来取下一个元素时，会使用一个记录 List 修改次数的变量 modCount，与迭代器保存的 expectedModCount 变量进行比较，如果不相等则会抛出异常

foreach 循环中调用 List 中的 remove() 方法，只做了 modCount++，而没有同步到 expectedModCount。当再次遍历调用 next() 方法时，发现 modCount 和 expectedModCount 不一致，就抛出了 ConcurrentModificationException 异常

```java
for (int i = 0; i < list.size(); ++i) {
    if (list.get(i).equals("java")) {
        list.remove(i);
    }
}
```
```java
Iterator<String> iterator = list.iterator();

while (iterator.hasNext()) {
    String word = iterator.next();

    if (word.equals("java")) {
        iterator.remove();
    }
}
```

### subList
返回原列表指定区间视图，对子列表的改动也会反应到原列表上
```java
public List<E> subList(int fromIndex, int toIndex) {
    subListRangeCheck(fromIndex, toIndex, size);
    return new SubList(this, 0, fromIndex, toIndex);
}
```

### 与数组的转换
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
LinkedList 实现了 List 接口、Deque 接口，同时继承了 AbstractSequentialList 抽象类
LinkedList 实现了 Cloneable 和 Serializable 接口，可以实现克隆和序列化

LinkedList 也自行实现 readObject 和 writeObject 进行序列化与反序列化
```java
transient int size = 0;
transient Node<E> first;
transient Node<E> last;
```

### Queue 接口
先进后出，在尾部添加元素，从头部删除元素
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

- add 与 offer 在尾部添加元素。队列满时，add 抛出异常，offer 返回 false
- element 与 peek 返回头部元素，但不改变队列。队列空时，element 抛出异常，peek 返回 null
- remove 与 poll 返回头部元素，并从队列中删除。队列空时，remove 抛出异常，poll 返回 null

### Deque 接口
- xxxFirst 操作头部，xxxLast 操作尾部
- 队列为空时，getXXX/removeXXX 抛出异常
- 队列为空时，peekXXX/pollXXX 返回 null
- 队列满时，addXXX 抛出异常，offerXXX 返回 false

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

Deque 还具有 Stack 功能
```java
public interface Deque<E> extends Queue<E> {
  void push(E e);
  E pop();
  E peek();
}
```


## ArrayList vs LinkedList vs Vector
Vector 与 ArrayList 作为动态数组，内部元素以数组形式顺序存储，适合随机访问
LinkedList 进行节点插入、删除比较高效，但是随机访问性能比动态数组慢
ArrayList 与 LinkedList 为线程不安全的，Vector 为线程安全的，因此效率比 ArrayList 要低

