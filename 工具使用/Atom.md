### 需要安装的插件
1. pretty-json
2. vim-mode-plus
3. autosave
4. markdown-toc：生成markdown目录
5. markdown-preview-plus
6. markdown-scroll-sync: 只针对原生的 markdown-preview
7. markdown-themeable-pdf: markdown to pdf。[安装方式](http://blog.csdn.net/dream_an/article/details/51800523)

### 无法安装插件
1. 在github上搜索插件名，下载相应源代码压缩包（也可以在Atom中搜索相应的插件，里面有链接，可以直接进入Github）
2. 解压后，将文件夹移动到 .../.atom/packages/文件夹内
3. 进入对应的文件夹内，执行npm install. 等待安装结束
4. 假如遇到还是无法安装，可以参考[上述方式安装失败后，解决方案](https://www.zhihu.com/question/38098629)
    * 其实也就是重启Atom后，里面会提示相应的插件依赖包还是无法安装。此时只需要 npm install 插件名，即可完成安装。
    * 前提需要安装Node
