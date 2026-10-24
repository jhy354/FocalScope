# FocalScope

FocalScope 2.0.5 是基于 ImageJ1 1.54u9 源码构建的图像处理软件，包含中文界面、独立应用图标和 Windows 便携版。

- GitHub：<https://github.com/jhy354/FocalScope>
- 发布页：<https://github.com/jhy354/FocalScope/releases>
- 问题反馈：<https://github.com/jhy354/FocalScope/issues>

> 为兼容 ImageJ1 插件和宏，Java 包名 `ij`、主类 `ij.ImageJ` 以及 TIFF/ROI 文件格式标记保持不变；界面、程序名、jar、exe、图标和项目链接均已改为 FocalScope。

## 环境要求

| 组件 | 要求 |
|---|---|
| 操作系统 | Windows 10 / 11 x64 |
| JDK | **JDK 11**（完整 JDK，必须包含 `javac.exe` 和 `jlink.exe`） |
| Maven | 3.9.x（3.6+ 通常可用） |
| PowerShell | Windows PowerShell 5.1+ |
| .NET Framework | 4.x（Windows 10/11 自带，打包 `FocalScope.exe` 时需要） |

必须使用 JDK 11：JDK 17 及以上不支持本项目的 `target 6`，JDK 8 不支持 `source 9`。

## 环境配置

```powershell
$env:JAVA_HOME = 'D:\Eclipse Adoptium\jdk-11.0.32.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;D:\apache-maven-3.9.16\bin;$env:Path"

java -version
mvn -version
```

## 编译

```powershell
cd <FocalScope 项目目录>
mvn clean package
```

输出：

```text
target\focalscope-2.0.5.jar
```

运行：

```powershell
java -jar target\focalscope-2.0.5.jar
```

## 打包为开箱即用的软件

```powershell
cd <FocalScope 项目目录>
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\package.ps1
```

输出：

```text
dist\FocalScope-2.0.5-windows-x64\
dist\FocalScope-2.0.5-windows-x64.zip
```

便携包内容：

```text
FocalScope-2.0.5-windows-x64\
├─ FocalScope.exe          启动程序
├─ FocalScope.jar          FocalScope 主程序
├─ runtime\                自带的 Java 11 运行时
├─ IJ_Props.txt
├─ IJ_zh_CN.properties
├─ LICENSE.txt
├─ macros\
├─ plugins\
└─ luts\
```

解压后双击 `FocalScope.exe` 即可运行，不需要安装 Java，也没有 `run.bat`。

插件放到 `plugins` 目录，LUT 放到 `luts` 目录。

## 许可证

FocalScope 基于 ImageJ1 的 public domain 源码，详见 [LICENSE.txt](LICENSE.txt)。
