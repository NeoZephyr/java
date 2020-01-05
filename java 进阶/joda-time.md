## 日历
### 创建日历对象
```java
// 当前时间
DateTime dateTime = new DateTime();

dateTime = new DateTime(2016, 11, 11, 15, 20);
dateTime = new DateTime(2016, 11, 11, 15, 20, 47);
dateTime = new DateTime(2017, 11, 11, 15, 20, 47, 345);
```

### 获取日历信息
```java
System.out.println(dateTime.getYear());
System.out.println(dateTime.getMonthOfYear());
System.out.println(dateTime.getDayOfMonth());
System.out.println(dateTime.getHourOfDay());
System.out.println(dateTime.getMinuteOfHour());
System.out.println(dateTime.getSecondOfMinute());
System.out.println(dateTime.getMillisOfSecond());
System.out.println(dateTime.getDayOfWeek());
```

### 日历信息格式化
```java
DateTime dateTime = new DateTime(2017, 11, 11, 15, 20, 47, 345);
System.out.println(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
```
Joda-time 的 `DateTimeFormatter` 是线程安全的
```java
DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
System.out.println(formatter.print(dateTime));
```
```java
DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
DateTime dateTime = formatter.parseDateTime("2017-09-11 11:11:11");
```

### 修改日历信息
```java
// 下午 6:30
DateTime dateTime = dateTime.withHourOfDay(18).withMinuteOfHour(30);

// 2.5 小时之后
dateTime = new DateTime().plusHours(2).plusMinutes(30);

// 今日 0 点
dateTime = new DateTime().withMillisOfDay(0);

// 下周三上午 10 点
dateTime = new DateTime().plusWeeks(1).withDayOfWeek(3).withMillisOfDay(0).withHourOfDay(10);

// 明天最后时间
dateTime = new DateTime().plusDays(1).millisOfDay().withMaximumValue();

// 本月最后一天最后时间
dateTime = new DateTime().dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue();

// 下个月第一个周一上午 10 点
dateTime = new DateTime().plusMonths(1).dayOfMonth()
              .withMinimumValue()
              .plusDays(6)
              .withDayOfWeek(1)
              .withMillisOfDay(0)
              .withHourOfDay(10);
```

### 日历信息比较
```java
DateTime start = new DateTime(2017, 11, 11, 11, 11);
DateTime end = new DateTime(2017, 12, 12, 12, 12);
Period period = new Period(start, end);
System.out.println(period.getMonths() + ", " + period.getDays() + ", " + period.getHours());
```
```java
DateTime bornDate = new DateTime(1991, 11, 24, 11, 29);
System.out.println(Years.yearsBetween(bornDate, new DateTime()).getYears());

System.out.println(Minutes.minutesBetween(
    DateTime.now().withMillisOfDay(0).withHourOfDay(9),
    DateTime.now()).getMinutes());
```

## 日期
```java
LocalDate born = new LocalDate(1991, 11, 24);
System.out.println(Years.yearsBetween(born, LocalDate.now()).getYears());

LocalDate date = new DateTime().toLocalDate();
```

## 时间
```java
System.out.println(Minutes.minutesBetween(new LocalTime(9, 30), LocalTime.now()).getMinutes());

LocalTime time = new DateTime().toLocalTime();
```

## Joda 与 JDK date
### JDK to Joda
```java
DateTime dateTime1 = new DateTime(new Date());
DateTime dateTime2 = new DateTime(Calendar.getInstance());
```
```java
LocalDate.fromDateFields(new Date());
LocalDate.fromCalendarFields(Calendar.getInstance());
LocalTime.fromDateFields(new Date());
LocalDate.fromCalendarFields(Calendar.getInstance());
```

### Joda to JDK
```java
DateTime dateTime = new DateTime();
Date date = dateTime.toDate();

Calendar calendar = dateTime.toCalendar(Locale.CHINA);

Date date = new LocalDate().toDate();
```