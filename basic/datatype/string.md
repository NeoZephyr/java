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


## 正则
### 贪婪模式
在数量匹配中，如果单独使用 +、?、* 或 {min,max} 等量词，正则表达式会匹配尽可能多的内容

```java
String text = "abbc";
String regex = "ab{1,3}c"
```
贪婪模式下，匹配 3 个 b 字符。匹配发生了一次失败，就引起了一次回溯

### 懒惰模式
正则表达式会尽可能少地重复匹配字符。如果匹配成功，它会继续匹配剩余的字符串

```java
String text = "abbc";
String regex = "ab{1,3}?c"
```
选择最小的匹配范围，只匹配 1 个 b 字符，避免了回溯问题

### 独占模式
独占模式一样会最大限度地匹配更多内容，不同的是，在独占模式下，匹配失败就会结束匹配，不会发生回溯问题
```java
String text = "abbc";
String regex = "ab{1,3}+bc"
```
结果是不匹配，结束匹配，不会发生回溯问题


正则表达式的优化
少用贪婪模式，多用独占模式
减少分支选择。如果要用，可以通过以下几种方式来优化：
1. 将比较常用的选择项放在前面，使它们可以较快地被匹配
2. 尝试提取共用模式，例如，将 "(abcd|abef)" 替换为 "ab(cd|ef)"

减少捕获嵌套
捕获组是指把正则表达式中，子表达式匹配的内容保存到以数字编号或显式命名的数组中，方便后面引用。一般一个 () 就是一个捕获组，捕获组可以进行嵌套

非捕获组则是指参与匹配却不进行分组编号的捕获组，其表达式一般由 (?:exp) 组成

在正则表达式中，每个捕获组都有一个编号，编号 0 代表整个匹配到的内容

```java
String text = "<input high=\"20\" weight=\"70\">test</input>";
String reg="(<input.*?>)(.*?)(</input>)";
Pattern pattern = Pattern.compile(reg);
Matcher matcher = pattern.matcher(text);

while (matcher.find()) {
    System.out.println(matcher.group(0));
    System.out.println(matcher.group(1));
    System.out.println(matcher.group(2));
    System.out.println(matcher.groupCount());
}
```

如果不需要获取某一个分组内的文本，那么就使用非捕获分组。减少不需要获取的分组，可以提高正则表达式的性能
```java
String text = "<input high=\"20\" weight=\"70\">test</input>";
String reg="(?:<input.*?>)(.*?)(?:</input>)";
Pattern pattern = Pattern.compile(reg);
Matcher matcher = pattern.matcher(text);

while (matcher.find()) {
    System.out.println(matcher.group(0));
    System.out.println(matcher.group(1));
    System.out.println(matcher.groupCount());
}
```
