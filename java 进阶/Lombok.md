## Lombok
[参考](https://projectlombok.org/)

### 安装
- Maven 安装
```xml
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.16.18</version>
  <scope>provided</scope>
</dependency>
```
- 安装 Idea 插件

### Lombok 注解
#### @Getter
为类中的成员变量生成 `get` 方法
```java
@Getter
class Order {
    private Integer orderNo;
}
```
#### @Setter
为类中的成员变量生成 `set` 方法
```java
@Setter(AccessLevel.PROTECTED)
class Order {
    private Integer orderNo;
}
```
#### @NoArgsConstructor
生成无参构造函数
#### @AllArgsConstructor
生成带所有参数的构造函数
#### @ToString
```java
// id 不加入 toString 方法
@ToString(exclude="id")
// 多个成员不加入 toString 方法
@ToString(exclude={"id", "name"})

// id 加入 toString 方法
@ToString(of="id")
// 多个成员加入 toString 方法
@ToString(of={"id", "name"})
```
#### @EqualsAndHashCode
生成 `equals` 与 `hashCode` 方法
#### @Slf4j
针对 logback, 直接使用 `log` 变量，不需要定义
#### @Log4j
针对 log4j
#### @Data
包含 `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode` 注解，一般不使用