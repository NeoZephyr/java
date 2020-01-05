## Map
`Map` 接口表示键值对集合，根据键进行操作，它有两个主要的实现类，`HashMap` 和 `TreeMap`

## HashMap
`HashMap` 基于哈希表实现，要求键重写 `hashCode` 方法，操作效率很高，但元素没有顺序
```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

  private static final long serialVersionUID = 362498820763181265L;
  static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
  static final int MAXIMUM_CAPACITY = 1 << 30;
  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  
  // 判断是否需要将链表转换为红黑树的阈值
  static final int TREEIFY_THRESHOLD = 8;
  static final int UNTREEIFY_THRESHOLD = 6;
  static final int MIN_TREEIFY_CAPACITY = 64;

  static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    
    // 用于实现链表结构
    Node<K,V> next;

    Node(int hash, K key, V value, Node<K,V> next) {
      this.hash = hash;
      this.key = key;
      this.value = value;
      this.next = next;
    }

    public final K getKey()        { return key; }
    public final V getValue()      { return value; }
    public final String toString() { return key + "=" + value; }

    public final int hashCode() {
      return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    public final V setValue(V newValue) {
      V oldValue = value;
      value = newValue;
      return oldValue;
    }

    public final boolean equals(Object o) {
      if (o == this)
        return true;
      if (o instanceof Map.Entry) {
        Map.Entry<?,?> e = (Map.Entry<?,?>)o;
        if (Objects.equals(key, e.getKey()) &&
            Objects.equals(value, e.getValue()))
          return true;
      }
      return false;
    }
  }

  static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }

  static Class<?> comparableClassFor(Object x) {
    if (x instanceof Comparable) {
      Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
      if ((c = x.getClass()) == String.class) // bypass checks
        return c;
      if ((ts = c.getGenericInterfaces()) != null) {
        for (int i = 0; i < ts.length; ++i) {
          if (((t = ts[i]) instanceof ParameterizedType) &&
              ((p = (ParameterizedType)t).getRawType() ==
               Comparable.class) &&
              (as = p.getActualTypeArguments()) != null &&
              as.length == 1 && as[0] == c) // type arg is c
            return c;
        }
      }
    }
    return null;
  }

  @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
  static int compareComparables(Class<?> kc, Object k, Object x) {
    return (x == null || x.getClass() != kc ? 0 :
            ((Comparable)k).compareTo(x));
  }

  static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
  }

  transient Node<K,V>[] table;
  transient Set<Map.Entry<K,V>> entrySet;
  transient int size;
  transient int modCount;
  int threshold;
  final float loadFactor;
}
```

### 构造 `HashMap`
`HashMap` 安装 lazy-load 原则，在非拷贝构造函数中只是进行初始值设定，节点数组在首次使用时才进行初始化
```java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
    throw new IllegalArgumentException("Illegal initial capacity: " +
                                       initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                           loadFactor);
    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
}

public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}

public HashMap(Map<? extends K, ? extends V> m) {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    putMapEntries(m, false);
}
```

### Map
`keySet()`/`values()`/`entrySet()` 方法返回的都是视图，基于这些返回值的修改会直接修改 Map 自身
```java
public interface Map<K,V> {
  int size();
  boolean isEmpty();
  boolean containsKey(Object key);
  boolean containsValue(Object value);
  V get(Object key);
  V put(K key, V value);
  V remove(Object key);

  void putAll(Map<? extends K, ? extends V> m);

  void clear();
  Set<K> keySet();
  Collection<V> values();
  Set<Map.Entry<K, V>> entrySet();
  
  interface Entry<K,V> {
    K getKey();
    V getValue();
    V setValue(V value);
    boolean equals(Object o);
    int hashCode();
  }

  boolean equals(Object o);
  int hashCode();
}
```

### 内部实现
`table` 是一个 `Node` 类型数组，其中每个元素指向一个单链表；根据键存取值，通过键计算出 hash 值，取模得到数组中的索引位置 `idx`，然后操作 `table[idx]` 指向的单向链表；存取时依据键的 `hash` 值，这需要相同对象的 `hashCode()` 返回值相同

#### 保存键值对
```java
public V put(K key, V value) {
  return putVal(hash(key), key, value, false, true);
}
```
有些 `key` 的哈希值差异主要在高位，而 `HashMap` 里的哈希寻址是忽略容量以上的高位的，以下移位处理就可以有效避免哈希碰撞
```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```
```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    
    // 判断桶是否为空，若空就调用 resize 进行初始化
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    
    // 根据 hash 值获取索引位置
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k)))) // 找到 key
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value); //
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) { // 没有找到 key
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        // 转换为树节点
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))) // 找到 key
                    break;
                p = e;
            }
        }
        
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```
```java
Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
    return new Node<>(hash, key, value, next);
}
```
扩容
```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    
    if (oldCap > 0) {
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }

    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```
```java
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        if ((tab[index] = hd) != null)
        hd.treeify(tab);
    }
}
```

#### 获取元素
```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
```
```java
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    
    // 定位
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
            // 红黑树查找，复制度 O(logn)
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

#### 是否包含键
```java
public boolean containsKey(Object key) {
    return getNode(hash(key), key) != null;
}
```

#### 是否包含值
```java
public boolean containsValue(Object value) {
    Node<K,V>[] tab; V v;
    if ((tab = table) != null && size > 0) {
        for (int i = 0; i < tab.length; ++i) {
            for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                if ((v = e.value) == value ||
                    (value != null && value.equals(v)))
                return true;
            }
        }
    }
    return false;
}
```

#### 删除键值对
```java
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ?
        null : e.value;
}
```
```java
final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
  Node<K,V>[] tab; Node<K,V> p; int n, index;
  if ((tab = table) != null && (n = tab.length) > 0 &&
      (p = tab[index = (n - 1) & hash]) != null) {
    Node<K,V> node = null, e; K k; V v;
    if (p.hash == hash &&
        ((k = p.key) == key || (key != null && key.equals(k))))
      node = p;
    else if ((e = p.next) != null) {
      if (p instanceof TreeNode)
        node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
      else {
        do {
          if (e.hash == hash &&
              ((k = e.key) == key ||
               (key != null && key.equals(k)))) {
            node = e;
            break;
          }
          p = e;
        } while ((e = e.next) != null);
      }
    }
    if (node != null && (!matchValue || (v = node.value) == value ||
                         (value != null && value.equals(v)))) {
      if (node instanceof TreeNode)
        ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
      else if (node == p)
        tab[index] = node.next;
      else
        p.next = node.next;
      ++modCount;
      --size;
      afterNodeRemoval(node);
      return node;
    }
  }
  return null;
}
```

#### 遍历
```java
// 遍历时将 key, value 同时取出
Iterator<Map.Entry<String, Integer>> entryIterator = map.entrySet().iterator();
while (entryIterator.hasNext()) {
    Map.Entry<String, Integer> next = entryIterator.next();
    System.out.println("key=" + next.getKey() + " value=" + next.getValue());
}

// 需要通过 key 再一次取出 value，效率较低
Iterator<String> iterator = map.keySet().iterator();
while (iterator.hasNext()) {
    String key = iterator.next();
    System.out.println("key=" + key + " value=" + map.get(key));
}
```

## TreeMap
`TreeMap` 基于排序二叉树实现，要求键实现 `Comparable` 接口，或提供一个 `Comparator` 对象，操作效率稍低，但可以按键有序
```java
public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable {

    private final Comparator<? super K> comparator;
    private transient Entry<K,V> root;
    private transient int size = 0;
    private transient int modCount = 0;

    public TreeMap() {
        comparator = null;
    }

    public TreeMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    public TreeMap(Map<? extends K, ? extends V> m) {
        comparator = null;
        putAll(m);
    }

    public TreeMap(SortedMap<K, ? extends V> m) {
        comparator = m.comparator();
        try {
            buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
        } catch (java.io.IOException cannotHappen) {
        } catch (ClassNotFoundException cannotHappen) {
        }
    }
}
```

### 构造方法
- 默认构造方法
默认构造方法要求 `Map` 中的键实现 `Comparable` 接口
```java
Map<String, String> map = new TreeMap<>();
map.put("a", "abstract");
map.put("c", "class");
map.put("b", "boolean");
map.put("T", "this");

// 按键排序输出
for (Map.Entry<String, String> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
```
- 接受比较器对象的构造方法
如果 `comparator` 不为空，在内部对键进行比较时调用 `comparator` 的 `compare` 方法
```java
// 键的比较忽略大小写
Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
```
```java
Map<String, Integer> map = new TreeMap<>(new Comparator<String>() {
  @Override
  public int compare(String o1, String o2) {
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    return formatter.parseDateTime(o1).compareTo(formatter.parseDateTime(o2));
  }
});
map.put("2016-7-3", 100);
map.put("2016-7-10", 120);
map.put("2016-8-1", 90);

for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}
```
- 接受 `Map` 对象的构造方法
将已有的所有键值添加到当前 `TreeMap` 中，比较器 `comparator` 为空
- 接受 `SortedMap` 对象的构造方法
`SortedMap` 扩展了 `Map` 接口，当前 `TreeMap` 中的比较器设为跟 `SortedMap` 的比较器一样

### SortedMap
```java
public interface SortedMap<K,V> extends Map<K,V> {
  Comparator<? super K> comparator();

  // 返回视图，大于等于 fromKey 且小于 toKey 的所有键
  SortedMap<K,V> subMap(K fromKey, K toKey);

  // 返回视图，小于 toKey 的所有键
  SortedMap<K,V> headMap(K toKey);

  // 返回视图，大于等于 fromKey 的所有键
  SortedMap<K,V> tailMap(K fromKey);

  // 返回第一个键
  K firstKey();

  // 返回最后一个键
  K lastKey();
  Set<K> keySet();
  Collection<V> values();
  Set<Map.Entry<K, V>> entrySet();
}
```

### NavigableMap
扩展 `SortedMap` 增加了一些查找邻近键的方法

### 内部实现
每个节点一个颜色，非黑即红
```java
static final class Entry<K,V> implements Map.Entry<K,V> {
  K key;
  V value;
  Entry<K,V> left;
  Entry<K,V> right;
  Entry<K,V> parent;
  boolean color = BLACK;

  Entry(K key, V value, Entry<K,V> parent) {
      this.key = key;
      this.value = value;
      this.parent = parent;
  }

  public K getKey() {
      return key;
  }

  public V getValue() {
      return value;
  }

  public V setValue(V value) {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
  }

  public boolean equals(Object o) {
      if (!(o instanceof Map.Entry))
          return false;
      Map.Entry<?,?> e = (Map.Entry<?,?>)o;

      return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
  }

  public int hashCode() {
      int keyHash = (key==null ? 0 : key.hashCode());
      int valueHash = (value==null ? 0 : value.hashCode());
      return keyHash ^ valueHash;
  }

  public String toString() {
      return key + "=" + value;
  }
}
```

添加元素
```java
public V put(K key, V value) {
  Entry<K,V> t = root;
  if (t == null) {
    compare(key, key); // type (and possibly null) check
    root = new Entry<>(key, value, null);
    size = 1;
    modCount++;
    return null;
  }
  int cmp;
  Entry<K,V> parent;
  // split comparator and comparable paths
  Comparator<? super K> cpr = comparator;
  if (cpr != null) {
    do {
      parent = t;
      cmp = cpr.compare(key, t.key);
      if (cmp < 0)
        t = t.left;
      else if (cmp > 0)
        t = t.right;
      else
        return t.setValue(value);
    } while (t != null);
  } else {
    if (key == null)
      throw new NullPointerException();
    @SuppressWarnings("unchecked")
    Comparable<? super K> k = (Comparable<? super K>) key;
    do {
      parent = t;
      cmp = k.compareTo(t.key);
      if (cmp < 0)
        t = t.left;
      else if (cmp > 0)
        t = t.right;
      else
        return t.setValue(value);
    } while (t != null);
  }
  Entry<K,V> e = new Entry<>(key, value, parent);
  if (cmp < 0)
    parent.left = e;
  else
    parent.right = e;

  // 调整树结构
  fixAfterInsertion(e);
  size++;
  modCount++;
  return null;
}
```

根据 `key` 获取元素
```java
final Entry<K,V> getEntry(Object key) {
  // Offload comparator-based version for sake of performance
  if (comparator != null)
    return getEntryUsingComparator(key);

  if (key == null)
    throw new NullPointerException();

  @SuppressWarnings("unchecked")
  Comparable<? super K> k = (Comparable<? super K>) key;
  Entry<K,V> p = root;

  while (p != null) {
    int cmp = k.compareTo(p.key);
    if (cmp < 0)
      p = p.left;
    else if (cmp > 0)
      p = p.right;
    else
      return p;
  }
  return null;
}
```
```java
final Entry<K,V> getEntryUsingComparator(Object key) {
  @SuppressWarnings("unchecked")
  K k = (K) key;

  Comparator<? super K> cpr = comparator;
  if (cpr != null) {
    Entry<K,V> p = root;
    while (p != null) {
      int cmp = cpr.compare(k, p.key);
      if (cmp < 0)
        p = p.left;
      else if (cmp > 0)
        p = p.right;
      else
        return p;
    }
  }
  return null;
}
```

是否包含某个值
```java
public boolean containsValue(Object value) {
  for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e))
    if (valEquals(value, e.value))
      return true;
  return false;
}
```
```java
final Entry<K,V> getFirstEntry() {
  Entry<K,V> p = root;
  if (p != null)
    while (p.left != null)
      p = p.left;
  return p;
}
```
查找后继
```java
static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
  if (t == null)
    return null;
  else if (t.right != null) {
    Entry<K,V> p = t.right;
    while (p.left != null)
      p = p.left;
    return p;
  } else {
    Entry<K,V> p = t.parent;
    Entry<K,V> ch = t;
    while (p != null && ch == p.right) {
      ch = p;
      p = p.parent;
    }
    return p;
  }
}
```

删除元素
```java
public V remove(Object key) {
  Entry<K,V> p = getEntry(key);
  if (p == null)
    return null;

  V oldValue = p.value;
  deleteEntry(p);
  return oldValue;
}
```
```java
private void deleteEntry(Entry<K,V> p) {
  modCount++;
  size--;

  // If strictly internal, copy successor's element to p and then make p
  // point to successor.
  if (p.left != null && p.right != null) {
    Entry<K,V> s = successor(p);
    p.key = s.key;
    p.value = s.value;
    p = s;
  } // p has 2 children

  // Start fixup at replacement node, if it exists.
  Entry<K,V> replacement = (p.left != null ? p.left : p.right);

  if (replacement != null) {
    // Link replacement to parent
    replacement.parent = p.parent;
    if (p.parent == null)
      root = replacement;
    else if (p == p.parent.left)
      p.parent.left  = replacement;
    else
      p.parent.right = replacement;

    // Null out links so they are OK to use by fixAfterDeletion.
    p.left = p.right = p.parent = null;

    // Fix replacement
    if (p.color == BLACK)
      fixAfterDeletion(replacement);
  } else if (p.parent == null) { // return if we are the only node.
    root = null;
  } else { //  No children. Use self as phantom replacement and unlink.
    if (p.color == BLACK)
      fixAfterDeletion(p);

    if (p.parent != null) {
      if (p == p.parent.left)
        p.parent.left = null;
      else if (p == p.parent.right)
        p.parent.right = null;
      p.parent = null;
    }
  }
}
```

## LinkedHashMap
`LinkedHashMap` 支持两种顺序，一种是插入顺序，另外一种是访问顺序
```java
public class LinkedHashMap<K,V>
    extends HashMap<K,V>
    implements Map<K,V> {

  static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after;
    Entry(int hash, K key, V value, Node<K,V> next) {
      super(hash, key, value, next);
    }
  }

  transient LinkedHashMap.Entry<K,V> head;
  transient LinkedHashMap.Entry<K,V> tail;
  final boolean accessOrder;

  public LinkedHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    accessOrder = false;
  }

  public LinkedHashMap(int initialCapacity) {
    super(initialCapacity);
    accessOrder = false;
  }

  public LinkedHashMap() {
    super();
    accessOrder = false;
  }

  public LinkedHashMap(Map<? extends K, ? extends V> m) {
    super();
    accessOrder = false;
    putMapEntries(m, false);
  }

  public LinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder) {
    super(initialCapacity, loadFactor);
    this.accessOrder = accessOrder;
  }
}
```

### 插入顺序
希望的数据模型是一个 `Map`，但希望保持添加的顺序，比如一个购物车，键为购买项目，值为购买数量，按用户添加的顺序保存；另外一种场景是希望 `Map` 能够按键有序，但在添加到 `Map` 前，键已经通过其他方式排好序了，就没有必要使用 `TreeMap` 因为 `TreeMap` 的开销要大一些。比如从数据库查询数据放到内存时，可以使用 `order by` 语句让数据库对数据排序

### 访问顺序
对一个键执行 `get/put` 操作后，其对应的键值对会移到链表末尾，可以非常容易的实现 LRU 缓存
```java
protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return false;
}
```
在添加元素到 `LinkedHashMap` 后，`LinkedHashMap` 会调用这个方法，传递的参数是最久没被访问的键值对，如果这个方法返回 `true`，则这个最久的键值对就会被删除。`LinkedHashMap` 的实现总是返回 `false`，所有容量没有限制，但子类可以重写该方法，在满足一定条件的情况，返回 `true`
```java
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
  private int maxEntries;
  
  public LRUCache(int maxEntries){
    super(16, 0.75f, true);
    this.maxEntries = maxEntries;
  }
  
  @Override
  protected boolean removeEldestEntry(Entry<K, V> eldest) {
    return size() > maxEntries;
  }
}
```

### 内部实现
`LinkedHashMap` 是 `HashMap` 的子类，内部还有一个双向链表维护键值对的顺序，每个键值对既位于哈希表中，也位于这个双向链表中

### 根据键获取
```java
public V get(Object key) {
  Node<K,V> e;
  if ((e = getNode(hash(key), key)) == null)
    return null;
  if (accessOrder)
    afterNodeAccess(e);
  return e.value;
}
```

### 是否包含某值
```java
public boolean containsValue(Object value) {
  for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {
    V v = e.value;
    if (v == value || (value != null && value.equals(v)))
      return true;
  }
  return false;
}
```

## EnumMap
`EnumMap` 是保证顺序的，输出是按照键在枚举中的顺序
```java
public class EnumMap<K extends Enum<K>, V> extends AbstractMap<K, V>
    implements java.io.Serializable, Cloneable {

  private final Class<K> keyType;
  private transient K[] keyUniverse;
  private transient Object[] vals;
  private transient int size = 0;

  // 初始化键数组，最终调用了枚举类型的 values 方法
  private static <K extends Enum<K>> K[] getKeyUniverse(Class<K> keyType) {
    return SharedSecrets.getJavaLangAccess()
                        .getEnumConstantsShared(keyType);
  }

  public EnumMap(Class<K> keyType) {
    this.keyType = keyType;
    keyUniverse = getKeyUniverse(keyType);
    vals = new Object[keyUniverse.length];
  }

  public EnumMap(EnumMap<K, ? extends V> m) {
    keyType = m.keyType;
    keyUniverse = m.keyUniverse;
    vals = m.vals.clone();
    size = m.size;
  }

  public EnumMap(Map<K, ? extends V> m) {
    if (m instanceof EnumMap) {
      EnumMap<K, ? extends V> em = (EnumMap<K, ? extends V>) m;
      keyType = em.keyType;
      keyUniverse = em.keyUniverse;
      vals = em.vals.clone();
      size = em.size;
    } else {
      if (m.isEmpty())
        throw new IllegalArgumentException("Specified map is empty");
      keyType = m.keySet().iterator().next().getDeclaringClass();
      keyUniverse = getKeyUniverse(keyType);
      vals = new Object[keyUniverse.length];
      putAll(m);
    }
  }
}
```

### 内部实现
`EnumMap` 内部有两个数组，长度相同，一个表示所有可能的键，一个表示对应的值，值为 `null` 表示没有该键值对，键都有一个对应的索引，根据索引可直接访问和操作其键和值，效率很高
#### 保存元素
```java
public V put(K key, V value) {
  typeCheck(key);

  int index = key.ordinal();
  Object oldValue = vals[index];
  vals[index] = maskNull(value);
  if (oldValue == null)
    size++;
  return unmaskNull(oldValue);
}
```
```java
private void typeCheck(K key) {
  Class<?> keyClass = key.getClass();
  if (keyClass != keyType && keyClass.getSuperclass() != keyType)
    throw new ClassCastException(keyClass + " != " + keyType);
}
```
```java
private Object maskNull(Object value) {
  return (value == null ? NULL : value);
}

@SuppressWarnings("unchecked")
private V unmaskNull(Object value) {
  return (V)(value == NULL ? null : value);
}

private static final Object NULL = new Object() {
  public int hashCode() {
    return 0;
  }

  public String toString() {
    return "java.util.EnumMap.NULL";
  }
};
```

#### 根据键获取值
```java
public V get(Object key) {
  return (isValidKey(key) ?
          unmaskNull(vals[((Enum<?>)key).ordinal()]) : null);
}
```

#### 是否包含某值
```java
public boolean containsValue(Object value) {
  value = maskNull(value);

  for (Object val : vals)
    if (value.equals(val))
      return true;

  return false;
}
```

#### 按键删除
```java
public V remove(Object key) {
  if (!isValidKey(key))
    return null;
  int index = ((Enum<?>)key).ordinal();
  Object oldValue = vals[index];
  vals[index] = null;
  if (oldValue != null)
    size--;
  return unmaskNull(oldValue);
}
```

## `LinkedHashMap` `TreeMap` `HashMap`
1. `HashMap` 不保证顺序，具有很快的访问速度；`TreeMap` 实现 `SortMap` 接口，默认情况下，将保存的记录按照键升序排序
2. `HashMap` 与 `TreeMap` 不支持线程的同步；`HashTable` 支持线程同步，因此 `HashMap` 要比 `HashTable` 效率高
3. `HashMap` 最多只允许一条记录的键为 `null`，允许多条记录的值为 `null`；`HashTable` 不支持 null 键和值
4. `LinkedHashMap` 可以保证 `HashMap` 集合有序，存入的顺序和取出的顺序一致

## JDK7 vs JDK8
1. JDK7 中的 `HashMap` 底层维护一个数组，数组中的每一项都是一个 `Map.Entry`
2. JDK8 中的 `HashMap` 采用的是位桶 + 链表/红黑树的方式，当某个位桶的链表的长度达到某个阀值的时候，这个链表就将转换成红黑树