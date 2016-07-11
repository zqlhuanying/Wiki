## 使 ideaVim 插件可以访问系统剪切板
安装Vim插件后，会在用户目录下生成 .ideavimrc 文件(若此文件不存在可以新建一个)。
* Windows
```
C:\Users\{$username}\.ideavimrc
```
编辑文件，同时保证此文件是utf-8编码格式
```
set clipboard=unnamed
set clipboard=unnamedplus
```
* Linux
```
~/.ideavimrc
```
编辑文件
```
set clipboard+=unnamed
```
