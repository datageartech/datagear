# DataGear

## 版本发布流程

1. 以当前分支新建版本标记，名称为：v[version]，描述为：version [version]；

2. 切换到版本标记，执行maven构建命令：`mvn clean package` ；

3. 将刚才新建的标记推送到仓库保存；

4. 将构建的程序包（`datagear-webappembd/target/datagear-[version]-packages/`目录内）发布到Gitee、网盘、官网；

5. 将当前分支合并至主干；

6. 以主干新建新版本分支；

7. 切换至新版本分支，修改`pom.xml`文件中的`version`标签内的版本号为下一个版本；

8. 执行统一修改版本号的maven命令：`mvn -N versions:update-child-modules antrun:run` ；

9. 提交并推送新版本分支。