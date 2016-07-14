<!-- MDTOC maxdepth:6 firsth1:1 numbering:0 flatten:0 bullets:1 updateOnSave:1 -->

   - [使 ideaVim 插件可以访问系统剪切板](#使-ideavim-插件可以访问系统剪切板)   
   - [部署Tomcat/Jetty，并发布Web程序](#部署tomcatjetty，并发布web程序)   

<!-- /MDTOC -->
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

## 部署Tomcat/Jetty，并发布Web程序
* 配置Tomcat/Jetty

设置Tomcat/Jetty根目录
```
File-Settings-Application Servers-Add
```
![](/Image/IDEA/1.jpg)

* 创建Artifact
```
File-Project Structure-Artifacts(创建项目骨架，即Web程序发布时的输出目录。必须设置，否则Web程序无法运行)
```
![](/Image/IDEA/2.jpg)

在弹出的对话框中选择当前项目
![](/Image/IDEA/3.jpg)

这部分好像没什么需要改的，直接点“OK”
* 创建运行配置
```
Run-Edit Configurations，点上面的+为Tomcat添加配置
```
![](/Image/IDEA/4.jpg)

Server Tab的配置根据自己的需要修改，主要看Deployment Tab配置，还是点+
![](/Image/IDEA/5.jpg)

IDEA自动为我选择了前面创建的Artifact，直接点确定就可以了
![](/Image/IDEA/6.jpg)
