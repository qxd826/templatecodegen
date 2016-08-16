# 使用说明：

## 建工程
引入maven project到eclipse，修改pom.xml设置正确的swt库
> 
		<!-- For mac
		<dependency>
			<groupId>org.eclise.swt</groupId>
			<artifactId>cocoa</artifactId>
			<version>3.102.1</version>
			<scope>system</scope>
			<systemPath>${project.basedir}/lib/org.eclipse.swt.cocoa.macosx.x86_64_3.102.1.v20140206-1358.jar</systemPath>
		</dependency>
		-->
		<!--  For win
		<dependency>
			<groupId>org.eclise.swt</groupId>
			<artifactId>win32</artifactId>
			<version>3.5.2</version>
			<scope>system</scope>
			<systemPath>${project.basedir}/lib/swt-win32-win32-x86-3.5.2.jar</systemPath>
		</dependency>
		 -->
## 运行
    运行com.hive.codegen.Tcg

## 操作
    1. 左边窗口：按“>>”,将数据库对象建表脚本引入; 或粘贴内容进窗口
        * SQL格式
            * 建表脚本每行一个字段，不要包含除字段外的其他内容。参考demo目录下DemoSqlSrc.txt文件
        * 多列模式
            * 以空格分隔的多个列，连续空格当是一个处理。参考demo目录下DemoColsSrc.txt文件
    2. 勾选 “截掉表前缀”
    3. 选模板渲染：模板是Velocity语法格式
        * 单文件
            * 右边窗口选择“文件”, 按“>>”，载入模板，参考demo目录下DemoXxxSrc.txt文件
            * 点“确定”，渲染的结果在右下窗口内
        * 多文件
            * 右边窗口选择“目录”,按“>>”, 选定模板所在的目录
            * 右上窗口会列出所有模板文件
            * 点“确定”，渲染的结果和日志在与原模板目录同级，后缀“_target”的目录中
    4. 项目用的模板可以放在template目录下
