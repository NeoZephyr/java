## String
不可变对象，天生就是线程安全的，适用于少量的字符串操作的情况

### 字符串拼接
由多个字符串常量连接的字符串也属于字符串常量
```java
String s0 = "PainPage";
String s1 = "PainPage";

String part0 = "Pain";
String part1 = "Page";

String s2 = "Pain" + "Page";
String s3 = part0 + part1;

// result: true
System.out.println(s0 == s1);

// result: true
System.out.println(s0 == s2);

// result: false
System.out.println(s0 == s3);
```
构建超大字符串时，通过 StringBuilder 来提升系统性能

### intern
intern 方法会从字符串常量池中查询当前字符串是否存在，若不存在就会将当前字符串放入常量池中，并返回该字符串的引用；若存在就直接返回常量池中的字符串引用
```java
String s0 = "PainPage";
String s1 = new String("PainPage");
String s2 = s1.intern();

// result: false
System.out.println(s0 == s1);

// result: true
System.out.println(s0 == s2);
```
使用 intern 方法需要结合实际场景。因为常量池的实现是类似于一个 HashTable 的实现方式，存储的数据越大，遍历的时间复杂度就会增加。如果数据过大，会增加整个字符串常量池的负担

总之，如果对空间要求高于时间要求，且存在大量重复字符串时，可以考虑使用常量池存储。如果对查询速度要求很高，且存储字符串数量很大，重复率很低的情况下，不建议存储在常量池中

jdk6 中的常量池放在 Perm 区，Perm 区和正常的 JAVA Heap 区域是完全分开的。使用引号声明的字符串都是会直接在字符串常量池中生成，而 new 出来的字符串对象是放在 JAVA Heap 区域。所以 JAVA Heap 区域的对象地址和字符串常量池的对象地址肯定是不相同的

jdk7 版本中，字符串常量池已经从 Perm 区移到正常的 Java Heap 区域。调用 intern 方法后，若常量池中不存在该字符串，会直接保存成该对象的引用，而不会重新创建对象
```java
String s0 = new String("Pain") + new String("Page");
s0.intern();
String s1 = "PainPage";

// jdk6, result: false
// jdk7, result: true
System.out.println(s0 == s1);
```

### 字符串截取
JDK 6 的 substring 在进行截取时，还会引用原来的字符串，可能会导致内存泄露。通过如下方式修正：
```java
str.substring(5) + "";
```

### toString 无限递归
```java
class Infinite {
  @Override
  public String toString() {
    return "Infinite: " + this;
  }
}

class Finite {
  @Override
  public String toString() {
    return "Finite: " + super.toString();
  }
}

class ToStringTest {
  public static void main(String[] args) {
    //System.out.println(new Infinite());
    System.out.println(new Finite());
  }
}
```

### 格式化输出
```java
class PrintTest {
  public static void main(String[] args) {
    int x = 10;
    double y = 3.14;
    System.out.format("format, x: %d, y: %f\n", x, y);
    System.out.printf("printf, x: %d, y: %f\n", x, y);

    Formatter formatter = new Formatter(System.out);
    formatter.format("formatter, x: %d, y: %f\n", x, y);
    System.out.println(String.format("string format, x: %d, y: %f\n", x, y));
  }
}
```

### 正则
```java
class RegTest {
  public static void main(String[] args) {
    System.out.println("-1234".matches("-?\\d+"));
    System.out.println("1234".matches("-?\\d+"));
    System.out.println("1234".matches("(-|\\+)?\\d+"));

    String title = "Then, when you have found pain, you must";
    System.out.println(Arrays.toString(title.split(" ")));
    System.out.println(Arrays.toString(title.split("\\W+")));
    System.out.println(title.replaceFirst("f\\w+", "@"));
  }
}
```


## StringBuffer vs StringBuilder
StringBuffer: 线程安全，适用多线程下在字符缓冲区进行大量操作的情况
StringBuilder: 线程不安全，适用于单线程下在字符缓冲区进行大量操作的情况