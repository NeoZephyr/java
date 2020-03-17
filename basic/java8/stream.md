```java
users.forEach(u -> {
  u.setAvatar(imgPrefix + u.getAvatar());
  u.setImageList(u.getImageList().stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
});
```