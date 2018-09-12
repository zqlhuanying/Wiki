### 1.分支A中的文件或者文件夹(config.xml),不想要合并到分支B中，如何操作？
步骤(在分支B中新建 .gitattributes 文件)
1. 参考文件：https://stackoverflow.com/questions/15232000/git-ignore-files-during-merge
1. Add a **.gitattributes** file at the root level of repository
2. You can set up an attribute for confix.xml in .gitattributes file
```
config.xml merge=ours
/trip-adapter-jsf-sxy/src/test/**/* merge=ours (忽略文件夹)
*pom.xml merge=ours
```
3. And then define a dummy ours merge strategy with:
```
git config --global merge.ours.driver true
```

```
  注意：
  该步骤无法解决A分支中将某个文件删除的操作（即A分支中删除了某个文件，即使该文件处于.gitattributes文件中，分支B中相应的文件也会被删除）
  ```
