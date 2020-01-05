“Serial” is a STW, copying collector which uses a single GC thread.
“Parallel Scavenge” is a STW, copying collector which uses multiple GC threads.
“ParNew” is a STW, copying collector which uses multiple GC threads. It differs from “Parallel Scavenge” in that it has enhancements that make it usable with CMS. For example, “ParNew” does the synchronization needed so that it can run during the concurrent phases of CMS.

“Serial Old” is a STW, mark-sweep-compact collector that uses a single GC thread.
“CMS” (Concurrent Mark Sweep) is a mostly concurrent, low-pause collector.
“Parallel Old” is a compacting collector that uses multiple GC threads.









内存屏障共分为四种类型：
LoadLoad屏障：
抽象场景：Load1; LoadLoad; Load2
Load1 和 Load2 代表两条读取指令。在Load2要读取的数据被访问前，保证Load1要读取的数据被读取完毕。

StoreStore屏障：
抽象场景：Store1; StoreStore; Store2
Store1 和 Store2代表两条写入指令。在Store2写入执行前，保证Store1的写入操作对其它处理器可见

LoadStore屏障：
抽象场景：Load1; LoadStore; Store2
在Store2被写入前，保证Load1要读取的数据被读取完毕。

StoreLoad屏障：
抽象场景：Store1; StoreLoad; Load2
在Load2读取操作执行前，保证Store1的写入对所有处理器可见。StoreLoad屏障的开销是四种屏障中最大的

在一个变量被volatile修饰后，JVM会为我们做两件事：
1.在每个volatile写操作前插入StoreStore屏障，在写操作后插入StoreLoad屏障。
2.在每个volatile读操作前插入LoadLoad屏障，在读操作后插入LoadStore屏障。

单个字符
普通字符
特殊字符，'\t' '\n' '\r'
八进制字符，以\0开头，后跟1到3位数字，比如\0141，即字符'a'
十六进制字符，以\x开头，后跟两位字符，比如\x6A，即字符'j'
Unicode编号字符，以\u开头，后跟四位字符，比如\u9A6C，即中文字符'马'，这只能表示编号在0xFFFF以下的字符，如果超出0XFFFF，使用\x{...}形式，比如对于字符'💎'，可以使用\x{1f48e}
元字符，\ . * ? +，要匹配这些元字符需要在前面加转义字符'\'，比如'\\'

字符组
点号字符 '.' 在默认模式下匹配除了换行符以外的任意字符
在单行匹配模式下，'.' 匹配任意字符，包括换行符
a.f

有两种方式指定匹配模式，一种是在正则表达式中，以(?s)开头，表示单行匹配模式，比如：
(?s)a.f
另外一种是在程序中指定，Java 中对应的模式常量是 Pattern.DOTALL

匹配组中的任意一个字符，用中括号[]表示
[abcd]

连字符 '-' 表示连续的多个字符
[0-9]
[a-z]
可以有多个连续空间，可以有其他普通字符，比如：
[0-9a-zA-Z_]

'-' 是元字符，如果要匹配它自身，可以使用转义，即'\-' 或者放在字符组的最前面，比如：
[-0-9]

[ 后紧跟字符 ^ 表示排除的概念，比如：
[^abcd] 表示匹配除了a, b, c, d以外的任意一个字符
[^0-9] 表示匹配一个非数字字符

'^' 只在字符组的开头才是元字符，如果不在开头，就是普通字符，匹配它自身，比如：
[a^b] 表示匹配字符a, ^或b

在字符组中，除了 ^ - [ ] \ 外，其他在字符组外的元字符不再具备特殊含义，变成了普通字符，比如'.'，[.*]就是匹配'.'或者'*'本身

字符组运算
[[abc][def]] 等同于 [abcdef]，内部多个字符组等同于并集运算

[a-z&&[^de]] 交集运算，匹配的字符是a到z，但不能是d或e

预定义的字符组
\d：匹配一个数字字符，等同于[0-9]
\w：匹配一个单词字符，等同于[a-zA-Z_0-9]
\s：匹配一个空白字符，等同于[ \t\n\x0B\f\r]

它们都有对应的排除型字符组，用大写表示，即：
\D：匹配一个非数字字符，即[^\d]
\W：匹配一个非单词字符，即[^\w]
\S：匹配一个非空白字符，即[^\s]

POSIX字符组是POSIX标准定义的一些字符组，Java 中这些字符组的形式是\p{...}，比如：
\p{Lower}：小写字母，等同于[a-z]
\p{Upper}：大写字母，等同于[A-Z]
\p{Digit}：数字，等同于[0-9]
\p{Punct} ：标点符号，匹配!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~中的一个

量词
指定出现次数的元字符
+：表示前面字符的一次或多次出现
ab+c: 既能匹配abc，也能匹配abbc，或abbbc
*: 表示前面字符的零次或多次出现
ab*c: 既能匹配abc，也能匹配ac，或abbbc
?: 表示前面字符可能出现，也可能不出现
ab?c，既能匹配abc，也能匹配ac，但不能匹配abbc

通用量词 {m,n} 表示出现次数从m到n，包括m和n，如果n没有限制，可以省略，如果m和n一样，可以写为{m}。语法必须是严格的{m,n}形式，逗号左右不能有空格
ab{1,10}c：b可以出现1次到10次
ab{3}c：b必须出现三次，即只能匹配abbbc
ab{1,}c：与ab+c一样
ab{0,}c：与ab*c一样
ab{0,1}c：与ab?c一样

?, *, +, { 是元字符，如果要匹配这些字符本身，需要使用'\'转义，比如
a\*b 匹配字符串"a*b"

这些量词出现在字符组中时，不是元字符，比如表达式
[?*+{] 就是匹配其中一个字符本身


量词默认匹配是贪婪
<a>.*</a>
如果要处理的字符串是：
<a>first</a><a>second</a>
目的是想得到两个匹配，一个匹配：
<a>first</a>
另一个匹配：
<a>second</a>

但默认情况下，得到的结果却只有一个匹配，匹配所有内容
因为.*可以匹配第一个<a>和最后一个</a>之间的所有字符，只要能匹配，.*就尽量往后匹配，它是贪婪的。如果希望在碰到第一个匹配时就停止应该使用懒惰量词，在量词的后面加一个符号'?'
<a>.*?</a>

所有量词都有对应的懒惰形式，比如：x??, x*?, x+?, x{m,n}?等

分组
表达式可以用括号()括起来，表示一个分组
比如a(bc)d，bc就是一个分组，分组可以嵌套，比如a(de(fg))

捕获分组
分组默认都有一个编号，按照括号的出现顺序，从1开始，从左到右依次递增，比如表达式：a(bc)((de)(fg))

字符串abcdefg匹配这个表达式，第1个分组为bc，第2个为defg，第3个为de，第4个为fg。分组0是一个特殊分组，内容是整个匹配的字符串，这里是abcdefg

分组量词：对分组使用量词，表示分组的出现次数
比如a(bc)+d，表示bc出现一次或多次

分组多选
中括号[]表示匹配其中的一个字符，括号()和元字符'|'一起，可以表示匹配其中的一个子表达式，比如
(http|ftp|file)匹配http或ftp或file

需要注意区分|和[]，|用于[]中不再有特殊含义，比如
[a|b] 含义不是匹配a或b，而是a或|或b

回溯引用
在正则表达式中，可以使用斜杠\加分组编号引用之前匹配的分组，这称之为回溯引用，比如：
<(\w+)>(.*)</\1>
\1匹配之前的第一个分组(\w+)，这个表达式可以匹配类似如下字符串：
<title>bc</title>
第一个分组是"title"

命名分组
使用数字引用分组，可能容易出现混乱，可以对分组进行命名，通过名字引用之前的分组，对分组命名的语法是(?<name>X)，引用分组的语法是\k<name>，比如，上面的例子可以写为：
<(?<tag>\w+)>(.*)</\k<tag>>

非捕获分组
默认分组都称之为捕获分组，即分组匹配的内容被捕获了，可以在后续被引用，实现捕获分组有一定的成本，为了提高性能，如果分组后续不需要被引用，可以改为非捕获分组，语法是(?:...)，比如：
(?:abc|def)

特殊边界匹配
常用的表示特殊边界的元字符有^, $, \A, \Z, \z和\b
默认情况下，^ 匹配整个字符串的开始，^abc 表示整个字符串必须以abc开始
需要注意的是^的含义，在字符组中它表示排除，但在字符组外，它匹配开始，比如表达式^[^abc]，表示以一个不是a,b,c的字符开始
默认情况下，$匹配整个字符串的结束，不过，如果整个字符串以换行符结束，$匹配的是换行符之前的边界，比如表达式abc$，表示整个表达式以abc结束，或者以abc\r\n或abc\n结束

多行匹配模式
以上^和$的含义是默认模式下的，可以指定另外一种匹配模式，多行匹配模式，在此模式下，会以行为单位进行匹配，^匹配的是行开始，$匹配的是行结束，比如表达式是^abc$，字符串是"abc\nabc\r\n"，就会有两个匹配

可以有两种方式指定匹配模式，一种是在正则表达式中，以(?m)开头，m表示multiline，即多行匹配模式，上面的正则表达式可以写为：
(?m)^abc$

另外一种是在程序中指定，Java 中对应的模式常量是Pattern.MULTILINE

单行模式影响的是字符'.'的匹配规则，使得'.'可以匹配换行符，多行模式影响的是^和$的匹配规则，使得它们可以匹配行的开始和结束，两个模式可以一起使用

边界 \A
\A与^类似，但不管什么模式，它匹配的总是整个字符串的开始边界

边界 \Z和\z

\Z和\z与$类似，但不管什么模式，它们匹配的总是整个字符串的结束，\Z与\z的区别是，如果字符串以换行符结束，\Z与$一样，匹配的是换行符之前的边界，而\z匹配的总是结束边界。在进行输入验证的时候，为了确保输入最后没有多余的换行符，可以使用\z进行匹配

单词边界 \b

\b匹配的是单词边界，比如\bcat\b，匹配的是完整的单词cat，它不能匹配category，\b匹配的不是一个具体的字符，而是一种边界，这种边界满足一个要求，即一边是单词字符，另一边不是单词字符。在Java中，\b识别的单词字符除了\w，还包括中文字符

环视边界匹配
对于边界匹配，除了使用上面介绍的边界元字符，还有一种更为通用的方式，那就是环视，环视的字面意思就是左右看看，需要左右符合一些条件，本质上，它也是匹配边界，对边界有一些要求，这个要求是针对左边或右边的字符串的，根据要求不同，分为四种环视：
肯定顺序环视，语法是(?=...)，要求右边的字符串匹配指定的表达式，比如表达式abc(?=def)，(?=def)在字符c右面，即匹配c右面的边界，对这个边界的要求是，它的右边有def，比如abcdef，如果没有，比如abcd，则不匹配；
否定顺序环视，语法是(?!...)，要求右边的字符串不能匹配指定的表达式，比如表达式s(?!ing)，匹配一般的s，但不匹配后面有ing的s；
肯定逆序环视，语法是(?<=...)，要求左边的字符串匹配指定的表达式，比如表达式(?<=\s)abc，(?<=\s)在字符a左边，即匹配a左边的边界，对这个边界的要求是，它的左边必须是空白字符；
否定逆序环视，语法是
```
(?<!...)，要求左边的字符串不能匹配指定的表达式，比如表达式(?<!\w)cat，(?<!\w)在字符c左边，即匹配c左边的边界，对这个边界的要求是，它的左边不能是单词字符
```
可以看出，环视也使用括号()，不过，它不是分组，不占用分组编号。

这些环视结构也被称为断言，断言的对象是边界，边界不占用字符，没有宽度，所以也被称为零宽度断言

否定顺序环视与排除型字符组
关于否定顺序环视，我们要避免与排除型字符组混淆，即区分s(?!ing)与s[^ing]，s[^ing]匹配的是两个字符，第一个是s，第二个是i, n, g以外的任意一个字符。还要注意，写法s(^ing)是不对的，^匹配的是起始位置。

出现在左边的顺序环视
顺序环视也可以出现在左边，比如表达式：
(?=.*[A-Z])\w+

\w+匹配多个单词字符，(?=.*[A-Z])匹配单词字符的左边界，这是一个肯定顺序环视，对这个边界的要求是，它右边的字符串匹配表达式：
.*[A-Z]
也就是说，它右边至少要有一个大写字母。
出现在右边的逆序环视
逆序环视也可以出现在右边，比如表达式：
```
[\w.]+(?<!\.)
```
[\w.]+匹配单词字符和字符'.'构成的字符串，比如"hello.ma"
```
(?<!\.)匹配字符串的右边界，这是一个逆序否定环视，对这个边界的要求是，它左边的字符不能是'.'，也就是说，如果字符串以'.'结尾，则匹配的字符串中不能包括这个'.'，比如，如果字符串是"hello.ma."，则匹配的子字符串是"hello.ma"
```
并行环视
环视匹配的是一个边界，里面的表达式是对这个边界左边或右边字符串的要求，对同一个边界，可以指定多个要求，即写多个环视，比如表达式：
(?=.*[A-Z])(?=.*[0-9])\w+
\w+的左边界有两个要求，(?=.*[A-Z])要求后面至少有一个大写字母，(?=.*[0-9])要求后面至少有一位数字。

转义与匹配模式
转义有两种：
把普通字符转义，使其具备特殊含义，比如'\t', '\n', '\d', '\w', '\b', '\A'等，也就是说，这个转义把普通字符变为了元字符；
把元字符转义，使其变为普通字符，比如'\.', '\*', '\?','\(', '\\'等

记住所有的元字符，并在需要的时候进行转义，这是比较困难的，有一个简单的办法，可以将所有元字符看做普通字符，就是在开始处加上\Q，在结束处加上\E，比如：
\Q(.*+)\E
\Q和\E之间的所有字符都会被视为普通字符

Java中，字符'\'也是字符串语法中的元字符，这使得正则表达式中的'\'，在Java字符串表示中，要用两个'\'，即'\\'，而要匹配字符'\'本身，在Java字符串表示中，要用四个'\'，即'\\\\'

匹配模式
前面提到了两种匹配模式，还有一种常用的匹配模式，就是不区分大小写的模式，指定方式也有两种，一种是在正则表达式开头使用(?i)，i为ignore，比如：
(?i)the
既可以匹配the，也可以匹配THE，还可以匹配The
也可以在程序中指定，Java中对应的变量是Pattern.CASE_INSENSITIVE
需要说明的是，匹配模式间不是互斥的关系，它们可以一起使用，在正则表达式中，可以指定多个模式，比如(?smi)



表示正则表达式
转义符 '\'
正则表达式由元字符和普通字符组成，字符'\'是一个元字符，要在正则表达式中表示'\'本身，需要使用它转义，即'\\'
在Java中需要用字符串表示正则表达式，而在字符串中，'\'也是一个元字符，为了在字符串中表示正则表达式的'\'，就需要使用两个'\'，即'\\'，而要匹配'\'本身，就需要四个'\'，即'\\\\'，比如说，如下表达式：
<(\w+)>(.*)</\1>
对应的字符串表示就是：
"<(\\w+)>(.*)</\\1>"
一个简单规则是，正则表达式中的任何一个'\'，在字符串中，需要替换为两个'\'

字符串表示的正则表达式可以被编译为一个Pattern对象，比如：
String regex = "<(\\w+)>(.*)</\\1>";
Pattern pattern = Pattern.compile(regex);
编译有一定的成本，而且Pattern对象只与正则表达式有关，与要处理的具体文本无关，它可以安全地被多线程共享，所以，在使用同一个正则表达式处理多个文本时，应该尽量重用同一个Pattern对象，避免重复编译

匹配模式
Pattern的compile方法接受一个额外参数，可以指定匹配模式：
public static Pattern compile(String regex, int flags)

三种匹配模式：单行模式(点号模式)、多行模式和大小写无关模式，它们对应的常量分别为：Pattern.DOTALL，Pattern.MULTILINE和Pattern.CASE_INSENSITIVE，多个模式可以一起使用，通过'|'连起来即可：
Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)

还有一个模式Pattern.LITERAL，在此模式下，正则表达式字符串中的元字符将失去特殊含义，被看做普通字符。Pattern有一个静态方法，它将s中的字符都看作普通字符
public static String quote(String s)
\Q和\E之间的字符会被视为普通字符。quote()基本上就是在字符串s的前后加了\Q和\E，比如，如果s为"\\d{6}"，则quote()的返回值就是"\\Q\\d{6}\\E"

切分
文本处理的一个常见需求是根据分隔符切分字符串，比如在处理CSV文件时，按逗号分隔每个字段，这个需求听上去很容易满足，因为String类有如下方法：
public String[] split(String regex)
String str = "abc,def,hello";
String[] fields = str.split(",");
System.out.println("field num: "+fields.length);
System.out.println(Arrays.toString(fields));

split将参数regex看做正则表达式，而不是普通的字符，如果分隔符是元字符，比如. $ | ( ) [ { ^ ? * + \，就需要转义，比如按点号'.'分隔，就需要写为：
String[] fields = str.split("\\.");
如果分隔符是用户指定的，程序事先不知道，可以通过Pattern.quote()将其看做普通字符串

将多个字符用作分隔符，可以将一个或多个空白字符或点号作为分隔符
String str = "abc  def      hello.\n   world";
String[] fields = str.split("[\\s.]+");
fields内容为：
[abc, def, hello, world]

空白字符串
需要说明的是，尾部的空白字符串不会包含在返回的结果数组中，但头部和中间的空白字符串会被包含在内，比如：
String str = ",abc,,def,,";
String[] fields = str.split(",");
System.out.println("field num: "+fields.length);
System.out.println(Arrays.toString(fields));
输出为：
field num: 4
[, abc, , def]

找不到分隔符
如果字符串中找不到匹配regex的分隔符，返回数组长度为1，元素为原字符串

切分数目限制
split方法接受一个额外的参数limit，用于限定切分的数目：
public String[] split(String regex, int limit) 

不带limit参数的split，其limit相当于0。关于limit的含义，我们通过一个例子说明下，比如字符串是"a:b:c:"，分隔符是":"，在limit为不同值的情况下，其返回数组不同


Pattern有两个split方法，与String方法的定义类似：
public String[] split(CharSequence input)
public String[] split(CharSequence input, int limit)
与String方法的区别是：
Pattern接受的参数是CharSequence，更为通用，我们知道String, StringBuilder, StringBuffer, CharBuffer等都实现了该接口；

如果regex长度大于1或包含元字符，String的split方法会先将regex编译为Pattern对象，再调用Pattern的split方法，这时，为避免重复编译，应该优先采用Pattern的方法；
如果regex就是一个字符且不是元字符，String的split方法会采用更为简单高效的实现，所以，这时，应该优先采用String的split方法

验证就是检验输入文本是否完整匹配预定义的正则表达式，经常用于检验用户的输入是否合法
String有如下方法：
public boolean matches(String regex)
比如：
String regex = "\\d{8}";
String str = "12345678";
System.out.println(str.matches(regex));

String的matches实际调用的是Pattern的如下方法：
public static boolean matches(String regex, CharSequence input)

这是一个静态方法，它的代码为：
public static boolean matches(String regex, CharSequence input) {
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(input);
    return m.matches();
}
就是先调用compile编译regex为Pattern对象，再调用Pattern的matcher方法生成一个匹配对象Matcher，Matcher的matches()返回是否完整匹配

查找就是在文本中寻找匹配正则表达式的子字符串
public static void find(){
    String regex = "\\d{4}-\\d{2}-\\d{2}";
    Pattern pattern = Pattern.compile(regex);
    String str = "today is 2017-06-02, yesterday is 2017-06-01";
    Matcher matcher = pattern.matcher(str);
    while(matcher.find()){
        System.out.println("find "+matcher.group()
            +" position: "+matcher.start()+"-"+matcher.end());
    }
}

代码寻找所有类似"2017-06-02"这种格式的日期，输出为：
find 2017-06-02 position: 9-19
find 2017-06-01 position: 34-44

Matcher的内部记录有一个位置，起始为0，find()方法从这个位置查找匹配正则表达式的子字符串，找到后，返回true，并更新这个内部位置，匹配到的子字符串信息可以通过如下方法获取：
//匹配到的完整子字符串
public String group()
//子字符串在整个字符串中的起始位置
public int start()
//子字符串在整个字符串中的结束位置加1
public int end()

group()其实调用的是group(0)，表示获取匹配的第0个分组的内容。我们在上节介绍过捕获分组的概念，分组0是一个特殊分组，表示匹配的整个子字符串。除了分组0，Matcher还有如下方法，获取分组的更多信息：

//分组个数
public int groupCount()
//分组编号为group的内容
public String group(int group) 
//分组命名为name的内容
public String group(String name)
//分组编号为group的起始位置
public int start(int group)
//分组编号为group的结束位置加1
public int end(int group)

比如：
public static void findGroup() {
    String regex = "(\\d{4})-(\\d{2})-(\\d{2})";
    Pattern pattern = Pattern.compile(regex);
    String str = "today is 2017-06-02, yesterday is 2017-06-01";
    Matcher matcher = pattern.matcher(str);
    while (matcher.find()) {
        System.out.println("year:" + matcher.group(1) 
            + ",month:" + matcher.group(2) 
            + ",day:" + matcher.group(3));
    }
}
输出为：
year:2017,month:06,day:02
year:2017,month:06,day:01


替换
replaceAll和replaceFirst
查找到子字符串后，一个常见的后续操作是替换。String有多个替换方法：
public String replace(char oldChar, char newChar)
public String replace(CharSequence target, CharSequence replacement)
public String replaceAll(String regex, String replacement)
public String replaceFirst(String regex, String replacement)

第一个replace方法操作的是单个字符，第二个是CharSequence，它们都是将参数看做普通字符。而replaceAll和replaceFirst则将参数regex看做正则表达式，它们的区别是，replaceAll替换所有找到的子字符串，而replaceFirst则只替换第一个找到的，看个简单的例子，将字符串中的多个连续空白字符替换为一个：
String regex = "\\s+";
String str = "hello    world       good";
System.out.println(str.replaceAll(regex, " "));
输出为：
hello world good

在replaceAll和replaceFirst中，参数replacement也不是被看做普通的字符串，可以使用美元符号加数字的形式，比如$1，引用捕获分组，我们看个例子：

String regex = "(\\d{4})-(\\d{2})-(\\d{2})";
String str = "today is 2017-06-02.";
System.out.println(str.replaceFirst(regex, "$1/$2/$3"));
输出为：
today is 2017/06/02.

这个例子将找到的日期字符串的格式进行了转换。所以，字符'$'在replacement中是元字符，如果需要替换为字符'$'本身，需要使用转义，看个例子：
String regex = "#";
String str = "#this is a test";
System.out.println(str.replaceAll(regex, "\\$"));

如果替换字符串是用户提供的，为避免元字符的的干扰，可以使用Matcher的如下静态方法将其视为普通字符串：
public static String quoteReplacement(String s)
String的replaceAll和replaceFirst调用的其实是Pattern和Matcher中的方法，比如，replaceAll的代码为：
public String replaceAll(String regex, String replacement) {
    return Pattern.compile(regex).matcher(this).replaceAll(replacement);
}

边查找边替换
replaceAll和replaceFirst都定义在Matcher中，除了一次性的替换操作外，Matcher还定义了边查找、边替换的方法：
public Matcher appendReplacement(StringBuffer sb, String replacement)
public StringBuffer appendTail(StringBuffer sb)

这两个方法用于和find()一起使用，我们先看个例子：
public static void replaceCat() {
    Pattern p = Pattern.compile("cat");
    Matcher m = p.matcher("one cat, two cat, three cat");
    StringBuffer sb = new StringBuffer();
    int foundNum = 0;
    while (m.find()) {
        m.appendReplacement(sb, "dog");
        foundNum++;
        if (foundNum == 2) {
            break;
        }
    }
    m.appendTail(sb);
    System.out.println(sb.toString());
}


在这个例子中，我们将前两个"cat"替换为了"dog"，其他"cat"不变，输出为：
one dog, two dog, three cat


StringBuffer类型的变量sb存放最终的替换结果，Matcher内部除了有一个查找位置，还有一个append位置，初始为0，当找到一个匹配的子字符串后，appendReplacement()做了三件事情：
将append位置到当前匹配之前的子字符串append到sb中，在第一次操作中，为"one "，第二次为", two ";
将替换字符串append到sb中；
更新append位置为当前匹配之后的位置。

appendTail将append位置之后所有的字符append到sb中。



模板引擎
利用Matcher的这几个方法，我们可以实现一个简单的模板引擎，模板是一个字符串，中间有一些变量，以{name}表示，如下例所示：
String template = "Hi {name}, your code is {code}.";

这里，模板字符串中有两个变量，一个是name，另一个是code。变量的实际值通过Map提供，变量名称对应Map中的键，模板引擎的任务就是接受模板和Map作为参数，返回替换变量后的字符串，示例实现为：

private static Pattern templatePattern = Pattern.compile("\\{(\\w+)\\}");

public static String templateEngine(String template, Map<String, Object> params) {
    StringBuffer sb = new StringBuffer();
    Matcher matcher = templatePattern.matcher(template);
    while (matcher.find()) {
        String key = matcher.group(1);
        Object value = params.get(key);
        matcher.appendReplacement(sb, value != null ? 
                Matcher.quoteReplacement(value.toString()) : "");
    }
    matcher.appendTail(sb);
    return sb.toString();
}

代码寻找所有的模板变量，正则表达式为：
\{(\w+)\}
'{'是元字符，所以要转义，\w+表示变量名，为便于引用，加了括号，可以通过分组1引用变量名

使用该模板引擎的示例代码为：
public static void templateDemo() {
    String template = "Hi {name}, your code is {code}.";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", "老马");
    params.put("code", 6789);
    System.out.println(templateEngine(template, params));
}
输出为：
Hi 老马, your code is 6789.




邮编比较简单，就是6位数字，首位不能是0，所以表达式可以为：
[1-9][0-9]{5}

public static Pattern ZIP_CODE_PATTERN = Pattern.compile("[1-9][0-9]{5}");
public static boolean isZipCode(String text) {
    return ZIP_CODE_PATTERN.matcher(text).matches();
}

但如果用于查找，这个表达式是不够的，看个例子：

public static void findZipCode(String text) {
    Matcher matcher = ZIP_CODE_PATTERN.matcher(text);
    while (matcher.find()) {
        System.out.println(matcher.group());
    }
}
public static void main(String[] args) {
    findZipCode("邮编 100013，电话18612345678");
}
文本中只有一个邮编，但输出却为：
100013
186123

可以使用环视边界匹配，对于左边界，它前面的字符不能是数字，环视表达式为：
```
(?<![0-9])
```
对于右边界，它右边的字符不能是数字，环视表达式为：
```
(?![0-9])
```
完整的表达式可以为：
```
(?<![0-9])[1-9][0-9]{5}(?![0-9])
```


手机号码
[0-9]{11}
目前手机号第1位都是1，第2位取值为3、4、5、7、8之一，所以更精确的表达式是
1[3|4|5|7|8|][0-9]{9}

为方便表达手机号，手机号中间经常有连字符(即减号'-')，形如：
186-1234-5678
为表达这种可选的连字符，表达式可以改为：
1[3|4|5|7|8|][0-9]-?[0-9]{4}-?[0-9]{4}

在手机号前面，可能还有0、+86或0086，和手机号码之间可能还有一个空格，比如：
018612345678
+86 18612345678
0086 18612345678
为表达这种形式，可以在号码前加如下表达式：
((0|\+86|0086)\s?)?
和邮编类似，如果为了抽取，也要在左右加环视边界匹配，左右不能是数字。所以，完整的表达式为：
```
(?<![0-9])((0|\+86|0086)\s?)?1[3|4|5|7|8|][0-9]-?[0-9]{4}-?[0-9]{4}(?![0-9])
```


固定电话
不考虑分机，中国的固定电话一般由两部分组成：区号和市内号码，区号是3到4位，市内号码是7到8位。区号以0开头，表达式可以为：
0[0-9]{2,3}
市内号码表达式为：
[0-9]{7,8}

区号可能用括号包含，区号与市内号码之间可能有连字符，如以下形式：
010-62265678
(010)62265678
整个区号是可选的，所以整个表达式为：
(\(?0[0-9]{2,3}\)?-?)?[0-9]{7,8}
再加上左右边界环视，完整的Java表示为：
```
public static Pattern FIXED_PHONE_PATTERN = Pattern.compile(
        "(?<![0-9])" // 左边不能有数字
        + "(\\(?0[0-9]{2,3}\\)?-?)?" // 区号
        + "[0-9]{7,8}"// 市内号码
        + "(?![0-9])"); // 右边不能有数字
```

日期的表示方式有很多种，我们只看一种，形如：
2017-06-21
2016-11-1
年月日之间用连字符分隔，月和日可能只有一位。
最简单的正则表达式可以为：
\d{4}-\d{1,2}-\d{1,2}
年一般没有限制，但月只能取值1到12，日只能取值1到31
对于月，有两种情况，1月到9月，表达式可以为：
0?[1-9]
10月到12月，表达式可以为：
1[0-2]
所以，月的表达式为：
(0?[1-9]|1[0-2])
对于日，有三种情况：
1到9号，表达式为：0?[1-9]
10号到29号，表达式为：[1-2][0-9]
30号和31号，表达式为：3[01]
所以，整个表达式为：
\d{4}-(0?[1-9]|1[0-2])-(0?[1-9]|[1-2][0-9]|3[01])
加上左右边界环视，完整的Java表示为：
```
public static Pattern DATE_PATTERN = Pattern.compile(
        "(?<![0-9])" // 左边不能有数字
        + "\\d{4}-" // 年
        + "(0?[1-9]|1[0-2])-" // 月
        + "(0?[1-9]|[1-2][0-9]|3[01])"// 日
        + "(?![0-9])"); // 右边不能有数字
```


时间，格式如下：
10:57
基本表达式为：
\d{2}:\d{2}
小时取值范围为0到23，更精确的表达式为：
([0-1][0-9]|2[0-3])
分钟取值范围为0到59，更精确的表达式为：
[0-5][0-9]
所以，整个表达式为：
([0-1][0-9]|2[0-3]):[0-5][0-9]
加上左右边界环视，完整的Java表示为：
```
public static Pattern TIME_PATTERN = Pattern.compile(
        "(?<![0-9])" // 左边不能有数字
        + "([0-1][0-9]|2[0-3])" // 小时
        + ":" + "[0-5][0-9]"// 分钟
        + "(?![0-9])"); // 右边不能有数字
```

身份证有一代和二代之分，一代是15位数字，二代是18位，都不能以0开头，对于二代身份证，最后一位可能为x或X，其他是数字
一代身份证表达式可以为：
[1-9][0-9]{14}
二代身份证可以为：
[1-9][0-9]{16}[0-9xX]
这两个表达式的前面部分是相同的，二代身份证多了如下内容：
[0-9]{2}[0-9xX]
所以，它们可以合并为一个表达式，即：
[1-9][0-9]{14}([0-9]{2}[0-9xX])?
加上左右边界环视，完整的Java表示为：
```
public static Pattern ID_CARD_PATTERN = Pattern.compile(
        "(?<![0-9])" // 左边不能有数字
        + "[1-9][0-9]{14}" // 一代身份证
        + "([0-9]{2}[0-9xX])?" // 二代身份证多出的部分
        + "(?![0-9])"); // 右边不能有数字
```


IP地址格式如下：
192.168.3.5 
点号分隔，4段数字，每个数字范围是0到255。最简单的表达式为：
(\d{1,3}\.){3}\d{1-3}
\d{1,3}太简单，没有满足0到255之间的约束，要满足这个约束，就要分多种情况考虑。
值是1位数，前面可能有0到2个0，表达式为：
0{0,2}[0-9]
值是两位数，前面可能有一个0，表达式为：
0?[0-9]{2}
值是三位数，又要分为多种情况。以1开头的，后两位没有限制，表达式为：
1[0-9]{2}
以2开头的，如果第二位是0到4，则第三位没有限制，表达式为：
2[0-4][0-9]
如果第二位是5，则第三位取值为0到5，表达式为：
25[0-5] 
所以，\d{1,3}更为精确的表示为：
(0{0,2}[0-9]|0?[0-9]{2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])
所以，加上左右边界环视，IP地址的完整Java表示为：
```
public static Pattern IP_PATTERN = Pattern.compile(
        "(?<![0-9])" // 左边不能有数字
        + "((0{0,2}[0-9]|0?[0-9]{2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"
        + "(0{0,2}[0-9]|0?[0-9]{2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])" 
        + "(?![0-9])"); // 右边不能有数字
```
URL
URL的格式比较复杂，其规范定义在https://tools.ietf.org/html/rfc1738，我们只考虑http协议，其通用格式是：

http://<host>:<port>/<path>?<searchpart>

开始是http://，接着是主机名，主机名之后是可选的端口，再之后是可选的路径，路径后是可选的查询字符串，以?开头。
一些例子：
http://www.example.com
http://www.example.com/ab/c/def.html
http://www.example.com:8080/ab/c/def?q1=abc&q2=def


主机名中的字符可以是字母、数字、减号和点号，所以表达式可以为：
[-0-9a-zA-Z.]+
端口部分可以写为：
(:\d+)?
路径由多个子路径组成，每个子路径以/开头，后跟零个或多个非/的字符，简单的说，表达式可以为：
(/[^/]*)*
更精确的说，把所有允许的字符列出来，表达式为：
(/[-\w$.+!*'(),%;:@&=]*)*
对于查询字符串，简单的说，由非空字符串组成，表达式为：
\?[\S]*

更精确的，把所有允许的字符列出来，表达式为：
\?[-\w$.+!*'(),%;:@&=]*

路径和查询字符串是可选的，且查询字符串只有在至少存在一个路径的情况下才能出现，其模式为：
(/<sub_path>(/<sub_path>)*(\?<search>)?)?

所以，路径和查询部分的简单表达式为：
(/[^/]*(/[^/]*)*(\?[\S]*)?)?

精确表达式为：
(/[-\w$.+!*'(),%;:@&=]*(/[-\w$.+!*'(),%;:@&=]*)*(\?[-\w$.+!*'(),%;:@&=]*)?)?

HTTP的完整Java表达式为：
public static Pattern HTTP_PATTERN = Pattern.compile(
        "http://" + "[-0-9a-zA-Z.]+" // 主机名
        + "(:\\d+)?" // 端口
        + "(" // 可选的路径和查询 - 开始
            + "/[-\\w$.+!*'(),%;:@&=]*" // 第一层路径
            + "(/[-\\w$.+!*'(),%;:@&=]*)*" // 可选的其他层路径
            + "(\\?[-\\w$.+!*'(),%;:@&=]*)?" // 可选的查询字符串
        + ")?"); // 可选的路径和查询 - 结束 


Email地址
完整的Email规范比较复杂，定义在https://tools.ietf.org/html/rfc822，我们先看一些实际中常用的。
比如新浪邮箱，它的格式如：
abc@sina.com

对于用户名部分，它的要求是：4-16个字符，可使用英文小写、数字、下划线，但下划线不能在首尾。
怎么验证用户名呢？可以为：
[a-z0-9][a-z0-9_]{2,14}[a-z0-9]

新浪邮箱的完整Java表达式为：
public static Pattern SINA_EMAIL_PATTERN = Pattern.compile(
        "[a-z0-9]"  
        + "[a-z0-9_]{2,14}" 
        + "[a-z0-9]@sina\\.com");

我们再来看QQ邮箱，它对于用户名的要求为：
3-18字符，可使用英文、数字、减号、点或下划线
必须以英文字母开头，必须以英文字母或数字结尾
点、减号、下划线不能连续出现两次或两次以上

如果只有第一条，可以为：
[-0-9a-zA-Z._]{3,18}

为满足第二条，可以改为：
[a-zA-Z][-0-9a-zA-Z._]{1,16}[a-zA-Z0-9]

怎么满足第三条呢？可以使用边界环视，左边加如下表达式：
(?![-0-9a-zA-Z._]*(--|\.\.|__))

完整表达式可以为：
(?![-0-9a-zA-Z._]*(--|\.\.|__))[a-zA-Z][-0-9a-zA-Z._]{1,16}[a-zA-Z0-9]

QQ邮箱的完整Java表达式为：
public static Pattern QQ_EMAIL_PATTERN = Pattern.compile(
        "(?![-0-9a-zA-Z._]*(--|\\.\\.|__))" // 点、减号、下划线不能连续出现两次或两次以上
        + "[a-zA-Z]" // 必须以英文字母开头
        + "[-0-9a-zA-Z._]{1,16}" // 3-18位 英文、数字、减号、点、下划线组成
        + "[a-zA-Z0-9]@qq\\.com"); // 由英文字母、数字结尾        

以上都是特定邮箱服务商的要求，一般的邮箱是什么规则呢？一般而言，以@作为分隔符，前面是用户名，后面是域名。
用户名的一般规则是：
由英文字母、数字、下划线、减号、点号组成
至少1位，不超过64位
开头不能是减号、点号和下划线

比如：
h_llo-abc.good@example.com

这个表达式可以为：
[0-9a-zA-Z][-._0-9a-zA-Z]{0,63}

域名部分以点号分隔为多个部分，至少有两个部分。最后一部分是顶级域名，由2到3个英文字母组成，表达式可以为：
[a-zA-Z]{2,3}

对于域名的其他点号分隔的部分，每个部分一般由字母、数字、减号组成，但减号不能在开头，长度不能超过63个字符，表达式可以为：
[0-9a-zA-Z][-0-9a-zA-Z]{0,62}

所以，域名部分的表达式为：
([0-9a-zA-Z][-0-9a-zA-Z]{0,62}\.)+[a-zA-Z]{2,3}

完整的Java表示为：
public static Pattern GENERAL_EMAIL_PATTERN = Pattern.compile(
        "[0-9a-zA-Z][-._0-9a-zA-Z]{0,63}" // 用户名
        + "@" 
        + "([0-9a-zA-Z][-0-9a-zA-Z]{0,62}\\.)+" // 域名部分
        + "[a-zA-Z]{2,3}"); // 顶级域名

中文字符
中文字符的Unicode编号一般位于\u4e00和\u9fff之间，所以匹配任意一个中文字符的表达式可以为：
[\u4e00-\u9fff]

Java表达式为：
public static Pattern CHINESE_PATTERN = Pattern.compile(
        "[\\u4e00-\\u9fff]");








避免死锁的技术：
加锁顺序
加锁时限
死锁检测

interrupted()：测试当前线程是否已经是中断状态，执行后具有状态标志清除为false的功能
isInterrupted()：测试线程Thread对象是否已经是中断状态，但不清除状态标志
```java
public static boolean interrupted() {
  return currentThread().isInterrupted(true);
}

public boolean isInterrupted() {
  return isInterrupted(false);
}

private native boolean isInterrupted(boolean ClearInterrupted);
```
终止正在运行的线程的三种方法：
使用退出标志
使用stop方法强行终止线程
使用interrupt方法中断线程

yield()方法的作用是放弃当前的CPU资源，将它让给其他的任务去占用CPU执行时间。但放弃时间不确定，有可能刚放弃，马上又获得CPU时间片。yield()方法和sleep方法一样，线程并不会让出锁，和wait不同

thread.setDaemon(true)必须在thread.start()之前设置，否则会报IllegalThreadStateException异常；在Daemon线程中产生的新线程也是Daemon的；在使用ExecutorSerice等多线程框架时，会把守护线程转换为用户线程，并且也会把优先级设置为Thread.NORM_PRIORITY，所以如果要使用后台线程就不能用java的线程池。在构建Daemon线程时，不能依靠finally块中的内容来确保执行关闭或清理资源的逻辑
```java
// Executors/DefaultThreadFactory
static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                      poolNumber.getAndIncrement() +
                     "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
```
声明为synchronized的父类方法A，在子类中重写之后并不具备synchronized的特性

ReentrantLock中的其余方法
int getHoldCount()：查询当前线程保持此锁定的个数，也就是调用lock()方法的次数。
int getQueueLength()：返回正等待获取此锁定的线程估计数。比如有5个线程，1个线程首先执行await()方法，那么在调用getQueueLength方法后返回值是4，说明有4个线程在等待lock的释放。
int getWaitQueueLength(Condition condition)：返回等待此锁定相关的给定条件Condition的线程估计数。比如有5个线程，每个线程都执行了同一个condition对象的await方法，则调用getWaitQueueLength(Condition condition)方法时返回的int值是5。
boolean hasQueuedThread(Thread thread)：查询指定线程是否正在等待获取此锁定。
boolean hasQueuedThreads()：查询是否有线程正在等待获取此锁定。
boolean hasWaiters(Condition condition)：查询是否有线程正在等待与此锁定有关的condition条件。
boolean isFair()：判断是不是公平锁。
boolean isHeldByCurrentThread()：查询当前线程是否保持此锁定。
boolean isLocked()：查询此锁定是否由任意线程保持。
void lockInterruptibly()：如果当前线程未被中断，则获取锁定，如果已经被中断则出现异常

ReentrantLock与synchonized区别
ReentrantLock可以中断地获取锁（void lockInterruptibly() throws InterruptedException）
ReentrantLock可以尝试非阻塞地获取锁（boolean tryLock()）
ReentrantLock可以超时获取锁。通过tryLock(timeout, unit)，可以尝试获得锁，并且指定等待的时间。
ReentrantLock可以实现公平锁。通过new ReentrantLock(true)实现。
ReentrantLock对象可以同时绑定多个Condition对象，而在synchronized中，锁对象的的wait(), notify(), notifyAll()方法可以实现一个隐含条件，如果要和多于一个的条件关联的对象，就不得不额外地添加一个锁，而ReentrantLock则无需这样做，只需要多次调用newCondition()方法即可

gc日志：-XX:PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamp -Xloggc:$CATALINA_BASE/logs/gc.log

观察内核状态的上下文切换(cs)次数
bi和bo这两个值，分别表示块设备每秒接收的块数量和块设备每秒发送的块数量，可以判定io繁忙状况
vmstat 1 5

String类型的常量池主要使用方法有两种：
直接使用双引号声明出来的String对象会直接存储在常量池中。
如果不是用双引号声明的String对象，可以使用String提供的intern方法。intern 方法会从字符串常量池中查询当前字符串是否存在，若不存在就会将当前字符串放入常量池中

异常
在Finally块中清理资源或者使用try-with-resource语句
不要捕获Throwable
不要忽略异常
不要记录并抛出异常，经常会给同一个异常输出多条日志
包装异常时不要抛弃原始的异常

vmstat 测试
us过高：
a. 代码问题。比如一个耗时的循环不加sleep，或者在一些cpu密集计算（如xml解析，加解密，加解压，数据计算）时没处理好
b. gc频繁。一个比较容易遗漏的问题就是gc频繁时us容易过高，因为垃圾回收属于大量计算的过程。gc频繁带来的cpu过高常伴有内存的大量波动，通过内存来判断并解决该问题更好
sy过高：
a. 上下文切换次数过多。通常是系统内线程数量较多，并且线程经常在切换，由于系统抢占相对切换时间和次数比较合理，所以sy过高通常都是主动让出cpu的情况，比如sleep或者lock wait, io wait。
wa过高：
a. 等待io的cpu占比较多。注意与上面情况的区别，io wait引起的sy过高指的是io不停的wait然后唤醒，因为数量较大，导致上下文切换较多，强调的是动态的过程；而io wait引起的wa过高指的是io wait的线程占比较多，cpu切换到这个线程是io wait，到那个线程也是io wait，于是总cpu就是wait占比较高。
id过高：
a. 很多人认为id高是好的，其实在性能测试中id高说明资源未完全利用，或者压测不到位，并不是好事

nc 127.0.0.1 6379

```java
String a = "a";
String b = "b";

String c = "a" + "b";

String c = a + b;

for (int i = 0; i < 5; i++) {
  a += b;
}
```
