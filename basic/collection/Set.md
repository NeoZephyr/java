## Set
`Set` 是 `Collection` 的子接口，没有增加新的方法，但保证不含重复元素。它有两个主要的实现类，`HashSet` 和 `TreeSet`

## HashSet
`HashSet` 基于哈希表实现，要求键重写 `hashCode` 方法，效率较高，但元素间没有顺序
```java
public class HashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable {

  private transient HashMap<E,Object> map;
  private static final Object PRESENT = new Object();
  public HashSet() {
    map = new HashMap<>();
  }

  public HashSet(Collection<? extends E> c) {
    map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
    addAll(c);
  }

  public HashSet(int initialCapacity, float loadFactor) {
    map = new HashMap<>(initialCapacity, loadFactor);
  }

  public HashSet(int initialCapacity) {
    map = new HashMap<>(initialCapacity);
  }

  HashSet(int initialCapacity, float loadFactor, boolean dummy) {
    map = new LinkedHashMap<>(initialCapacity, loadFactor);
  }
}
```

### Set
`Set` 扩展了 `Collection` 但并未定义任何新的方法
```java
public interface Set<E> extends Collection<E> {
  int size();
  boolean isEmpty();
  boolean contains(Object o);
  Iterator<E> iterator();
  Object[] toArray();
  <T> T[] toArray(T[] a);
  boolean add(E e);
  boolean remove(Object o);
  boolean containsAll(Collection<?> c);
  boolean addAll(Collection<? extends E> c);
  boolean retainAll(Collection<?> c);
  boolean removeAll(Collection<?> c);
  void clear();
  boolean equals(Object o);
  int hashCode();
}
```

### 内部实现
与 `HashMap` 类似，`HashSet` 要求元素重写 `hashCode` 和 `equals` 方法，且两个对象如果 `equals` 相同则 `hashCode` 也必须相同
`HashSet` 内部通过 `HashMap` 实现，`HashSet` 只有键，值都是相同的固定值

#### 添加元素
```java
public boolean add(E e) {
  return map.put(e, PRESENT)==null;
}
```

#### 是否包含元素
```java
public boolean contains(Object o) {
  return map.containsKey(o);
}
```

#### 删除元素
```java
public boolean remove(Object o) {
  return map.remove(o)==PRESENT;
}
```

## TreeSet
`TreeSet` 基于排序二叉树实现，元素按比较有序，元素需要实现 `Comparable` 接口，或者创建 `TreeSet` 时提供一个 `Comparator` 对象。`HashSet` 还有一个子类 `LinkedHashSet` 可以按插入有序。还有一个针对枚举类型的实现类 `EnumSet`，它基于位向量实现，效率很高
```java
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable {

  private transient NavigableMap<E,Object> m;

  // Dummy value to associate with an Object in the backing Map
  private static final Object PRESENT = new Object();

  TreeSet(NavigableMap<E,Object> m) {
    this.m = m;
  }

  public TreeSet() {
    this(new TreeMap<E,Object>());
  }

  public TreeSet(Comparator<? super E> comparator) {
    this(new TreeMap<>(comparator));
  }

  public TreeSet(Collection<? extends E> c) {
    this();
    addAll(c);
  }

  public TreeSet(SortedSet<E> s) {
    this(s.comparator());
    addAll(s);
  }
}
```

## EnumSet
```java
public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E>
    implements Cloneable, java.io.Serializable {

  final Class<E> elementType;
  final Enum<?>[] universe;

  EnumSet(Class<E>elementType, Enum<?>[] universe) {
    this.elementType = elementType;
    this.universe    = universe;
  }

  public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
    Enum<?>[] universe = getUniverse(elementType);
    if (universe == null)
      throw new ClassCastException(elementType + " not an enum");

    if (universe.length <= 64)
      return new RegularEnumSet<>(elementType, universe);
    else
      return new JumboEnumSet<>(elementType, universe);
  }
}
```
```java
class RegularEnumSet<E extends Enum<E>> extends EnumSet<E> {
  RegularEnumSet(Class<E>elementType, Enum<?>[] universe) {
    super(elementType, universe);
  }

  private long elements = 0L;

  public int size() {
    return Long.bitCount(elements);
  }
}
```
```java
class JumboEnumSet<E extends Enum<E>> extends EnumSet<E> {
  private long elements[];
  private int size = 0;

  JumboEnumSet(Class<E>elementType, Enum<?>[] universe) {
    super(elementType, universe);
    elements = new long[(universe.length + 63) >>> 6];
  }
}
```

### 应用
```java
class Worker {
  String name;
  Set<Day> availableDays;
  
  public Worker(String name, Set<Day> availableDays) {
    this.name = name;
    this.availableDays = availableDays;
  }
  
  public String getName() {
    return name;
  }
  
  public Set<Day> getAvailableDays() {
    return availableDays;
  }
}

Worker[] workers = new Worker[]{
  new Worker("张三", EnumSet.of(
          Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.FRIDAY)),
  new Worker("李四", EnumSet.of(
          Day.TUESDAY, Day.THURSDAY, Day.SATURDAY)),
  new Worker("王五", EnumSet.of(
          Day.TUESDAY, Day.THURSDAY)),
};
```
```java
// 哪些天一个人都不会来

// 初始化为所有值
Set<Day> days = EnumSet.allOf(Day.class);
for(Worker worker : workers){
  days.removeAll(worker.getAvailableDays());
}
System.out.println(days);
```
```java
// 哪些天至少会有一个人来
Set<Day> days = EnumSet.noneOf(Day.class);
for(Worker worker : workers){
  days.addAll(worker.getAvailableDays());
}
System.out.println(days);
```
```java
// 哪些天所有人都会来
Set<Day> days = EnumSet.allOf(Day.class);
for(Worker worker : workers){
  // 交集
  days.retainAll(worker.getAvailableDays());
}
System.out.println(days);
```
```java
// 哪些人周一和周二都会来
Set<Worker> availableWorkers = new HashSet<Worker>();
for(Worker worker : workers){
  if(worker.getAvailableDays().containsAll(
        EnumSet.of(Day.MONDAY, Day.TUESDAY))){
    availableWorkers.add(worker);
  }
}

for(Worker worker : availableWorkers){
  System.out.println(worker.getName());
}
```
```java
Map<Day, Integer> countMap = new EnumMap<>(Day.class);
for(Worker worker : workers){
  for(Day day : worker.getAvailableDays()){
    Integer count = countMap.get(day);
    countMap.put(day, count == null ? 1 : count + 1);
  }
}

Set<Day> days = EnumSet.noneOf(Day.class);
for(Map.Entry<Day, Integer> entry : countMap.entrySet()){
  if(entry.getValue() >= 2){
    days.add(entry.getKey());
  }
}
System.out.println(days);
```

### 内部实现
`EnumSet` 是使用位向量实现的，一个位表示一个元素的状态，用一组位表示一个集合的状态，每个位对应一个元素，而状态只可能有两种

`EnumSet` 是一个抽象类，没有定义使用的向量长度，它有两个子类，`RegularEnumSet` 和 `JumboEnumSet`，`RegularEnumSet` 使用一个 `long` 类型的变量作为位向量，`long` 类型的位长度是 64，而 `JumboEnumSet` 使用一个 `long` 类型的数组。如果枚举值个数小于等于 64，则静态工厂方法中创建的就是 `RegularEnumSet`，大于 64 的话就是 `JumboEnumSet`

#### 添加元素
```java
// RegularEnumSet
public boolean add(E e) {
  typeCheck(e);

  long oldElements = elements;
  elements |= (1L << ((Enum)e).ordinal());
  return elements != oldElements;
}
```
```java
// JumboEnumSet
public boolean add(E e) {
  typeCheck(e);

  int eOrdinal = e.ordinal();
  int eWordNum = eOrdinal >>> 6;

  long oldElements = elements[eWordNum];
  elements[eWordNum] |= (1L << eOrdinal);
  boolean result = (elements[eWordNum] != oldElements);
  if (result)
    size++;
  return result;
}
```

#### 添加所有
```java
// RegularEnumSet
public boolean addAll(Collection<? extends E> c) {
  if (!(c instanceof RegularEnumSet))
    return super.addAll(c);

  RegularEnumSet<?> es = (RegularEnumSet<?>)c;
  if (es.elementType != elementType) {
    if (es.isEmpty())
      return false;
    else
      throw new ClassCastException(
          es.elementType + " != " + elementType);
  }

  long oldElements = elements;
  elements |= es.elements;
  return elements != oldElements;
}
```

#### 删除元素
```java
// RegularEnumSet
public boolean remove(Object e) {
  if (e == null)
    return false;
  Class<?> eClass = e.getClass();
  if (eClass != elementType && eClass.getSuperclass() != elementType)
    return false;

  long oldElements = elements;
  elements &= ~(1L << ((Enum<?>)e).ordinal());
  return elements != oldElements;
}
```

#### 是否包含元素
```java
// RegularEnumSet
public boolean contains(Object e) {
  if (e == null)
    return false;
  Class<?> eClass = e.getClass();
  if (eClass != elementType && eClass.getSuperclass() != elementType)
    return false;

  return (elements & (1L << ((Enum<?>)e).ordinal())) != 0;
}
```

#### 是否包含所有元素
```java
// RegularEnumSet
public boolean containsAll(Collection<?> c) {
  if (!(c instanceof RegularEnumSet))
    return super.containsAll(c);

  RegularEnumSet<?> es = (RegularEnumSet<?>)c;
  if (es.elementType != elementType)
    return es.isEmpty();

  return (es.elements & ~elements) == 0;
}
```

#### 保留集合中的元素
```java
// RegularEnumSet
public boolean retainAll(Collection<?> c) {
  if (!(c instanceof RegularEnumSet))
    return super.retainAll(c);

  RegularEnumSet<?> es = (RegularEnumSet<?>)c;
  if (es.elementType != elementType) {
    boolean changed = (elements != 0);
    elements = 0;
    return changed;
  }

  long oldElements = elements;
  elements &= es.elements;
  return elements != oldElements;
}
```

#### 集合补集
`elements` 是 64 位的，当前枚举类可能没有用那么多位，取反后高位部分都变为了 1，需要将超出 `universe.length` 的部分设为 0
```java
void complement() {
  if (universe.length != 0) {
    elements = ~elements;

    // elements &= -1L >>> (64-universe.length);
    elements &= -1L >>> -universe.length;  // Mask unused bits
  }
}
```