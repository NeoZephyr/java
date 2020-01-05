`Scanner` 作为简单的文本扫描器，能够分析基本类型和字符串，它需要一个分隔符来将不同数据区分开来，默认是使用空白符，可以通过 `useDelimiter` 方法进行指定
`Scanner` 的构造方法可以接受 `File`、`InputStream`、`Reader` 作为参数，它也可以将字符串作为参数，这时，它会创建一个 `StringReader`

```java
Student s = new Student();
Scanner scanner = new Scanner(line).useDelimiter(",");
s.setName(scanner.next());
s.setAge(scanner.nextInt());
s.setScore(scanner.nextDouble());
```