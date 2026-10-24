# ImageJ 中文版（ImageJ1 1.54u9）

基于 [imagej/ImageJ](https://github.com/imagej/ImageJ) ，包含中文菜单和基础对话框汉化。

## 环境要求

| 组件 | 要求 |
|---|---|
| JDK | **JDK 11**（完整 JDK，必须包含 `javac.exe` 和 `jlink.exe`） |
| Maven | 3.9.x（3.6+ 通常可用） |
| .NET Framework | 4.x（Windows 10/11 自带，打包 `ImageJ.exe` 时需要） |

> 必须使用 JDK 11。JDK 17 及以上不支持本项目的 `target 6` 编译；JDK 8 不支持 `source 9`。

## 检查环境

```powershell
java -version
mvn -version
```

## 编译

```powershell
mvn clean package
```

运行：

```powershell
java -jar ./target/ij-1.x-SNAPSHOT.jar
```

## 打包为开箱即用的软件

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
./package.ps1
```

输出：

```text
./dist/ImageJ-1.54u9-zh-windows-x64/
./dist/ImageJ-1.54u9-zh-windows-x64.zip
```

便携包内容：

```text
ImageJ-1.54u9-zh-windows-x64\
├─ ImageJ.exe              启动程序
├─ ij.jar                  ImageJ 主程序
├─ runtime\                自带的 Java 11 运行时
├─ IJ_Props.txt
├─ IJ_zh_CN.properties
├─ LICENSE.txt
├─ macros\
├─ plugins\
└─ luts\
```

解压后双击 `ImageJ.exe` 即可运行，不需要安装 Java，也没有 `run.bat`。

插件放到 `plugins` 目录，LUT 放到 `luts` 目录。

## 许可证

详见 [LICENSE.txt](LICENSE.txt)。
