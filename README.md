# ImageX

ImageX 2.0.3 是基于 ImageJ1 1.54u9 源码构建的图像处理软件，包含中文界面、独立应用图标和 Windows 便携版。

- GitHub：<https://github.com/jhy354/ImageX>
- 发布页：<https://github.com/jhy354/ImageX/releases>
- 问题反馈：<https://github.com/jhy354/ImageX/issues>

> 为兼容 ImageJ1 插件和宏，Java 包名 `ij`、主类 `ij.ImageJ` 以及 TIFF/ROI 文件格式标记保持不变；界面、程序名、jar、exe、图标和项目链接均已改为 ImageX。

## 环境要求

| 组件 | 要求 |
|---|---|
| 操作系统 | Windows 10 / 11 x64 |
| JDK | **JDK 11**（完整 JDK，必须包含 `javac.exe` 和 `jlink.exe`） |
| Maven | 3.9.x（3.6+ 通常可用） |
| PowerShell | Windows PowerShell 5.1+ |
| .NET Framework | 4.x（Windows 10/11 自带，打包 `ImageX.exe` 时需要） |

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
cd D:\ImageX
mvn clean package
```

输出：

```text
D:\ImageX\target\imagex-2.0.3.jar
```

运行：

```powershell
java -jar D:\ImageX\target\imagex-2.0.3.jar
```

## 打包为开箱即用的软件

```powershell
cd D:\ImageX
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\package.ps1
```

输出：

```text
D:\ImageX\dist\ImageX-2.0.3-windows-x64\
D:\ImageX\dist\ImageX-2.0.3-windows-x64.zip
```

便携包内容：

```text
ImageX-2.0.3-windows-x64\
├─ ImageX.exe              启动程序
├─ ImageX.jar              ImageX 主程序
├─ runtime\                自带的 Java 11 运行时
├─ IJ_Props.txt
├─ IJ_zh_CN.properties
├─ LICENSE.txt
├─ macros\
├─ plugins\
└─ luts\
```

解压后双击 `ImageX.exe` 即可运行，不需要安装 Java，也没有 `run.bat`。

插件放到 `plugins` 目录，LUT 放到 `luts` 目录。

## 许可证

ImageX 基于 ImageJ1 的 public domain 源码，详见 [LICENSE.txt](LICENSE.txt)。
