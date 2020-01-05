### Joiner
```java
Map<String, String> map = Maps.newHashMap();
String result = Joiner.on("&").useForNull("").withKeyValueSeparator("=").join(map);
```
### Hash
```java
HashFunction FUNCTION = Hashing.md5();
HashCode hashCode =  FUNCTION.hashString(password + 'salt', Charset.forName("UTF-8"));
String result = hashCode.toString();
```
### Random
```java
String randomStr = RandomStringUtils.randomAlphabetic(10);
```
### Files
```java
MultipartFile uploadFile;
Files.write(uploadFile.getBytes(), newFile);
```
### Cache
```java
private final Cache<String, String> emailCache =
  CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterAccess(15, TimeUnit.MINUTES)
    .removalListener(new RemovalListener<String, String>() {
      @Override
      public void onRemoval(RemovalNotification<String, String> notification) {
        String email = notification.getValue();
      }
    }).build();

String email = emailCache.getIfPresent(key);
emailCache.invalidate(key);
```
