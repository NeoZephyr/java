## Map
Map 接口表示键值对集合，根据键进行操作，它有两个主要的实现类，HashMap 和 TreeMap


## HashMap
HashMap 基于哈希表实现，要求键重写 hashCode 方法，操作效率很高，但元素没有顺序

HashMap 是 Node 数组构成，每个 Node 包含了一个 key-value 键值对
```java
transient Node<K,V>[] table;

static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;
}
```

HashMap 有两个重要的属性：加载因子（loadFactor）和边界值（threshold）

加载因子用来间接设置 Entry 数组的内存空间大小，在初始 HashMap 不设置参数的情况下，默认加载因子为 0.75

加载因子越大，对空间的利用就越充分，这就意味着链表的长度越长，查找效率也就越低；加载因子太小，那么哈希表的数据将过于稀疏，对空间造成严重浪费

边界值通过初始容量和加载因子计算所得。如果 HashMap 中 Node 的数量超过边界值，HashMap 就会调用 resize() 方法重新分配 table 数组。这将会导致 HashMap 的数组复制，迁移到另一块内存中去，从而影响 HashMap 的效率


### 添加元素
有些 key 的哈希值差异主要在高位，而 HashMap 里的哈希寻址是忽略容量以上的高位的，以下移位处理就可以有效避免哈希碰撞
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
            ((k = p.key) == key || (key != null && key.equals(k)))) // key 值相同
            e = p;
        else if (p instanceof TreeNode) // 新增节点为红黑树节点
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) 
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // 链表长度足够转换为红黑树
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

### 获取元素
当 HashMap 中只存在数组，而数组中没有 Node 链表时，查询数据性能最好。一旦发生大量的哈希冲突，就会产生 Node 链表，这个时候每次查询元素都可能遍历 Node 链表，从而降低查询数据的性能。可以通过红黑树，使查询的平均复杂度降低到了 O(log(n))

### 扩容
在 JDK1.7 中，HashMap 整个扩容过程就是分别取出数组元素，一般该元素是最后一个放入链表中的元素，然后遍历以该元素为头的单向链表元素，依据每个被遍历元素的 hash 值计算其在新数组中的下标，然后进行交换。这样的扩容方式会将原来哈希冲突的单向链表尾部变成扩容后单向链表的头部

在 JDK 1.8 中，HashMap 对扩容操作做了优化。由于扩容数组的长度是 2 倍关系，所以对于假设初始 tableSize = 4 要扩容到 8 来说就是 0100 到 1000 的变化，在扩容中只用判断原来的 hash 值和左移动的一位按位与操作是 0 或 1 就行，0 的话索引不变，1 的话索引变成原索引加上扩容前数组

初始容量，一般得是 2 的整数次幂：
2 的幂次方减 1 后每一位都是 1，这样数组的每一个位置都能添加到元素。如果有一个位置为 0，那么无论 hash 值是多少，那一位总是 0，不利于均匀分布元素

### 遍历
```java
// 遍历时将 key, value 同时取出
Iterator<Map.Entry<String, Integer>> entryIterator = map.entrySet().iterator();

while (entryIterator.hasNext()) {
    Map.Entry<String, Integer> next = entryIterator.next();
    System.out.println("key = " + next.getKey() + " value = " + next.getValue());
}

// 需要通过 key 再一次取出 value，效率较低
Iterator<String> iterator = map.keySet().iterator();
while (iterator.hasNext()) {
    String key = iterator.next();
    System.out.println("key = " + key + " value = " + map.get(key));
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