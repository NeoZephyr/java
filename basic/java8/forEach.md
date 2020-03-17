```java
Map<String, String> newMap = Maps.newHashMap();
map.forEach((k, v) -> {
    if (v != null) {
        newMap.put(k, URLEncoder.encode(v,"utf-8"));
    }
});
```