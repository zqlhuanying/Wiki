### 常用正则表达式
1. 只能输入数字
```
private static final Pattern DIGITS = Pattern.compile("^\\d+$");
```

2. 只能输入大写英文字符/小写英文字符
```
private static final Pattern UPPER_CHAR_SEQUENCES = Pattern.compile("^[A-Z]+$");

private static final Pattern LOWER_CHAR_SEQUENCES = Pattern.compile("^[a-z]+$");
```
