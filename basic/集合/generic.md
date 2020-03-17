## 泛型
泛型就是类型参数化，处理的数据类型不是固定的，而是可以作为参数传入；泛型相对非泛型而言具有更好的安全性以及可读性
### 泛型类
- 单个类型参数
```java
class Pair<T> {
  T first;
  T second;

  Pair(T first, T second) {
    this.first = first;
    this.second = second;
  }

  public T getFirst() {
    return first;
  }

  public T getSecond() {
    return second;
  }

  public static void main(String[] args) {
    Pair<String> pair = new Pair<>("page", "pain");
    System.out.println(pair.getFirst());
    System.out.println(pair.getSecond());
  }
}
```
- 多个类型参数
```java
class Pair<U, V> {
  U first;
  V second;

  Pair(U first, V second) {
    this.first = first;
    this.second = second;
  }

  public U getFirst() {
    return first;
  }

  public V getSecond() {
    return second;
  }

  /**
   * 泛型方法
   */
  public static <U, V> Pair<U, V> makePair(U first, V second) {
    Pair<U, V> pair = new Pair<>(first, second);
    return pair;
  }

  public static void main(String[] args) {
    Pair<String, Integer> pair = new Pair<>("pain", 400);
    System.out.println(pair.getFirst());
    System.out.println(pair.getSecond());
  }
}
```
```java
class LinkedStack<T> {
  private class Node<U> {
    U item;
    Node<U> next;
    Node() {
      this.item = null;
      this.next = null;
    }
    Node(U item, Node<U> next) {
      this.item = item;
      this.next = next;
    }
    boolean end() {
      return null == item && null == next;
    }
  }

  private Node<T> top = new Node<>();

  public void push(T item) {
    top = new Node<>(item, top);
  }

  public T pop() {
    T result = top.item;
    if (!top.end()) {
      top = top.next;
    }
    return result;
  }
}

class TestLinkedStack {
  public static void main(String[] args) {
    LinkedStack<String> stack = new LinkedStack<>();
    for (String item : "hello pain gain".split(" ")) {
      stack.push(item);
    }

    String tmp;
    while (null != (tmp = stack.pop())) {
      System.out.println(tmp);
    }
  }
}
```
注意，泛型类的类型参数不能用于静态变量和静态方法

### 泛型接口
```java
interface Generator<T> {
  T next();
}

class Coffee {
  private static long counter = 0;
  private final long id = counter++;

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + id;
  }
}

class Moka extends Coffee {}
class Latte extends Coffee {}

class CoffeeGenerator implements Generator<Coffee>, Iterable<Coffee> {

  private Class[] types = {Moka.class, Latte.class};
  private static Random random = new Random(47);
  private int size;

  CoffeeGenerator() {}
  CoffeeGenerator(int size) {
    this.size = size;
  }

  @Override
  public Coffee next() {
    try {
      return (Coffee) types[random.nextInt(types.length)].newInstance();
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  private class CoffeeIterator implements Iterator<Coffee> {

    int count = size;

    @Override
    public boolean hasNext() {
        return count > 0;
    }

    @Override
    public Coffee next() {
        --count;
        return CoffeeGenerator.this.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<Coffee> iterator() {
    return new CoffeeIterator();
  }

  public static void main(String[] args) {
    CoffeeGenerator gen = new CoffeeGenerator();
    for (int i = 0; i < 3; ++i) {
      System.out.println(gen.next());
    }
    for (Coffee coffee : new CoffeeGenerator(3)) {
      System.out.println(coffee);
    }
  }
}
```

### 泛型方法
```java
class Tuple {
  public static <A, B> TwoTuple<A, B> tuple(A a, B b) {
    return new TwoTuple<>(a, b);
  }

  public static <A, B, C> ThreeTuple<A, B, C> tuple(A a, B b, C c) {
    return new ThreeTuple<>(a, b, c);
  }
}
```

## 擦除与边界
Java 编译器会将泛型代码转换为普通的非泛型代码，将类型参数擦除替换为 Object，插入必要的类型转换；指定边界后类型擦除时就不会转换为 Object 而是转换为边界类型
### 擦除
运行时类型信息不适用与泛型
```java
class TestErase {
  public static void main(String[] args) {
    Class clazz1 = new ArrayList<Integer>().getClass();
    Class clazz2 = new ArrayList<String>().getClass();
    // output:
    // true
    // [E]
    // [E]
    System.out.println(clazz1 == clazz2);
    System.out.println(Arrays.toString(new ArrayList<Integer>().getClass().getTypeParameters()));
    System.out.println(Arrays.toString(new ArrayList<String>().getClass().getTypeParameters()));
  }
}
```
类型擦除导致 `Base` 与 `Child` 实现的接口是同一个
```java
class Base {}
class Child extends Base {}
class Base implements Comparable<Base> {}
class Child extends Base implements Comparable<Child> {}
```
类型擦除导致不能重载
```java
public static void test(MyArray<Integer> arr) {}
public static void test(MyArray<String> arr) {}
```
```java
/**
 * 模板方法创建类型实例
 */
abstract class CarFactory<T> {
  T car;
  CarFactory() {
    car = create();
  }

  abstract T create();
}

class LightCar {}

class LightCarFactory extends CarFactory<LightCar> {

  @Override
  LightCar create() {
    return new LightCar();
  }

  void f() {
    System.out.println(car.getClass().getSimpleName());
  }

  public static void main(String[] args) {
    LightCarFactory lightCarFactory = new LightCarFactory();
    lightCarFactory.f();
  }
}
```
### 上边界
```java
// Comparable 是一个泛型接口，也需要一个类型参数
// T 表示一种数据类型，T extends Comparable<T> 表示必须实现 Comparable 接口且必须可以与相同类型元素比较
public static <T extends Comparable<T>> T max(T[] arr) {
//public static <T extends Comparable> T max(T[] arr) {
  T max = arr[0];
  for (int i = 0; i < arr.length; ++i) {
    if (arr[i].compareTo(max) > 0) {
      max = arr[i];
    }
  }
  return max;
}
```

## 通配符
`?` 表示通配符，`<? extends E>` 表示有限定通配符，匹配 `E` 或 `E` 的某个子类型。`<T extends E>` 用于定义类型参数，`<? extends E>` 用于实例化类型参数，而这个类型参数具体类型是未知的，只知道是 `E` 或 `E` 的某个子类型
`<? super E>` 用于灵活写入或比较，使得对象可以写入父类型容器，且父类型的比较方法可以应用于子类对象；`<? extends E>` 用于灵活读取，使得方法可以读取 `E` 或者 `E` 的任意子类型的容器对象
`<? super E>` 不能用类型参数形式替代
### 无限定通配符
无限定通配符可以改写成类型参数形式
```java
public static int indexOf(MyArray<?> arr, Object elem) {
  for (int i = 0; i < arr.size(); ++i) {
    if (arr.get(i).equals(elem)) {
      return i;
    }
  }
  return -1;
}
```
```java
public static <T> int indexOf(MyArray<T> arr, Object elem) {
  for (int i = 0; i < arr.size(); ++i) {
    if (arr.get(i).equals(elem)) {
      return i;
    }
  }
  return -1;
}
```

### 只读性
```java
MyArray<Integer> arr1 = new MyArray<>();
MyArray<? extends Number> arr2 = arr1;
Integer num = 10;

// 如果允许写入则不能确保安全性
arr2.add(num);
arr2.add((Number)num);
arr2.add((Object)num);
```
Java 容器类中通常提供通配符形式 API，形式简单，在内部调用带类型参数的方法
```java
public static void swap(MyArray<?> arr, int i, int j) {
  swapInternal(arr, i, j);

  // 只读性
  // Object tmp = arr.get(i);
  // arr.set(i, arr.get(j));
  // arr.set(j, tmp);
}

private static <T> void swapInternal() {
  T tmp = arr.get(i);
  arr.set(i, arr.get(j));
  arr.set(j, tmp);
}
```

### 超类写入
```java
public void copyTo(MyArray<E> des) {
  for (int i = 0; i < size; ++i) {
    dest.add(get(i));
  }
}
```
以下使用方法会导致编译错误，因为 `copyTo` 方法期望的参数类型是 `MyArray<Integer>`
```java
MyArray<Integer> arr1 = new MyArray<>();
arr1.add(100);
arr1.add(200);
MyArray<Number> arr2 = new MyArray<>();
arr1.copyTo(arr2);
```
改为超类通配符则可以写入
```java
public void copyTo(MyArray<? super E> des) {
  for (int i = 0; i < size; ++i) {
    des.add(get(i));
  }
}
```

### 超类比较
```java
public static <T extends Comparable<T>> T max(MyArray<T> arr) {
  T max = arr.get(0);
  for (int i = 0; i < arr.size; ++i) {
    if (arr.get(i).compareTo(max) > 0) {
      max = arr.get(i);
    }
  }

  return max;
}
```

```java
class Phone implements Comparable<Phone> {
  double price;

  Phone(double price) {
    this.price = price;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  @Override
  public int compareTo(Phone o) {
    if (price < o.getPrice()) {
      return -1;
    } else if (price > o.getPrice()) {
      return 1;
    } else {
      return 0;
    }
  }
}

class XiaomiPhone extends Phone {
  XiaomiPhone(double price) {
    super(price);
  }
}
```
```java
MyArray<XiaomiPhone> phones = new MyArray<>();
phones.add(new XiaomiPhone(98));
phones.add(new XiaomiPhone(89));

// max 方法会将 max 方法参数 T 推断为 XiaomiPhone 类型，但类型 T 需要实现 Comparable<XiaomiPhone>，而实际实现的却是 Comparable<Phone>
XiaomiPhone maxPhone = MyArray.max(phones);
```
改为以下形式进行比较
```java
public static <T extends Comparable<? super T>> T max(MyArray<T> arr) {
  T max = arr.get(0);
  for (int i = 0; i < arr.size; ++i) {
    if (arr.get(i).compareTo(max) > 0) {
      max = arr.get(i);
    }
  }

  return max;
}
```

## 泛型与数组
泛型数组不可创建
```java
Pair<String, String>[] options = new Pair<String, String>[]{
  new Pair("name", "pain"),
  new Pair("name", "page")
};
Object[] objs = options;
objs[0] = new Pair<String, Integer>("age", 10);
```
如果要使用数组存放泛型对象，可以用原始类型的数组
```java
Pair[] options = new Pair[]{
  new Pair<String, String>("name", "pain"),
  new Pair<String, String>("name", "page")
};
```

## 泛型与容器类
```java
class MyArray<E> {
  private static final int DEFAULT_CAPACITY = 10;
  private int size;
  private Object[] data;

  MyArray() {
    this.data = new Object[DEFAULT_CAPACITY];
  }

  private void ensureCapacity(int minCapacity) {
    int oldCapacity = data.length;
    if (oldCapacity >= minCapacity)
      return;
    int newCapacity = oldCapacity * 2;
    if (newCapacity < minCapacity) {
      newCapacity = minCapacity;
    }
    data = Arrays.copyOf(data, newCapacity);
  }

  public void add(E item) {
    ensureCapacity(size + 1);
    data[size++] = item;
  }

  public E get(int index) {
    return (E) data[index];
  }

  public int size() {
    return size;
  }

  public E set(int index, E item) {
    E oldValue = get(index);
    data[index] = item;
    return oldValue;
  }

  public void addAll1(MyArray<E> arr) {
    for (int i = 0; i < arr.size(); ++i) {
      add(arr.get(i));
    }
  }

  public <T extends E> void addAll2(MyArray<T> arr) {
    for (int i = 0; i < arr.size(); ++i) {
      add(arr.get(i));
    }
  }

  public void addAll3(MyArray<? extends E> arr) {
    for (int i = 0; i < arr.size(); ++i) {
      add(arr.get(i));
    }
  }

  public E[] toArray(Class<E> type) {
    Object copy = Array.newInstance(type, size);
    System.arraycopy(data, 0, copy, 0, size);
    return (E[])copy;
  }

  @Override
  public String toString() {
    return Arrays.toString(data);
  }

  public static void main(String[] args) {
    MyArray<Pair<Integer, String>> arr = new MyArray<>();
  }
}
```
```java
MyArray<Number> arr1 = new MyArray<>();
MyArray<Double> arr2 = new MyArray<>();
// 虽然 Double 是 Number 的子类，但 MyArray<Double> 不是 MyArray<Number> 子类
// arr1.addAll1(arr2);

arr1.addAll2(arr2);
arr1.addAll3(arr2);
```