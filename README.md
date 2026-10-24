# ImageJ 中文版（ImageJ1 1.54u9）

这是基于 [imagej/ImageJ](https://github.com/imagej/ImageJ) 的 ImageJ1 源码构建的非官方中文版本。

- 上游版本：ImageJ 1.54u9
- 本地 Maven 版本：`1.x-SNAPSHOT`
- 当前汉化范围：主菜单、子菜单、插件菜单、图像右键菜单、通用对话框和部分常用标签
- 许可证：ImageJ 本身为 public domain，见 [LICENSE.txt](LICENSE.txt)

> 说明：菜单和复选框的 `ActionCommand` 仍然保留英文，因此 ImageJ 宏命令、插件命令和快捷键不受汉化影响。

---

## 1. 环境要求

### 1.1 推荐环境

本项目在以下环境实际编译、打包和启动验证通过：

| 组件 | 版本 / 要求 | 说明 |
|---|---|---|
| 操作系统 | Windows 10 / 11 x64 | 源码本身可跨平台，本文命令以 Windows 为例 |
| JDK | **JDK 11**（Temurin 11.0.32.1 已验证） | 必须使用完整 JDK，不能只用 JRE |
| Maven | 3.9.16（3.6+ 通常也可以） | 用于下载依赖、编译和打包 |
| Git | 2.46.2 | 拉取源码和管理提交 |
| PowerShell | Windows PowerShell 5.1+ 或 PowerShell 7+ | 运行 `package.ps1` |
| 磁盘空间 | 约 300 MB | 依赖缓存 + 编译产物 + 便携包 |

### 1.2 JDK 版本非常重要

请使用 **JDK 11**，不要使用 JDK 17、21 或更高版本。

原因：

- 本项目的 `pom.xml` 设置 `maven.compiler.source=9`、`maven.compiler.target=6`；
- JDK 12 以后逐步移除了对 `target 6` 的支持，使用 JDK 17+ 编译会报错；
- JDK 8 又无法编译 `source=9` 的代码。

上游 README 写的是 JDK 8 或 11，但以当前仓库的 `pom.xml` 为准，**实际请使用 JDK 11**。

检查 Java：

```powershell
java -version
javac -version
```

应看到类似：

```text
openjdk version "11.0.32.1"
javac 11.0.32.1
```

### 1.3 配置 `JAVA_HOME` 和 `PATH`

如果当前终端找不到 `java` 或 `mvn`，先执行：

```powershell
$env:JAVA_HOME = 'D:\Eclipse Adoptium\jdk-11.0.32.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;D:\apache-maven-3.9.16\bin;$env:Path"

java -version
mvn -version
```

如果要永久写入用户环境变量：

```powershell
[Environment]::SetEnvironmentVariable('JAVA_HOME', 'D:\Eclipse Adoptium\jdk-11.0.32.101-hotspot', 'User')
```

然后重新打开终端。

### 1.4 项目目录

```text
D:\ImageX
├─ ij\                     ImageJ Java 源码
├─ tests\                  单元测试源码和数据
├─ pom.xml                 Maven 构建文件
├─ build.xml               Ant 构建文件
├─ IJ_Props.txt            菜单和插件安装定义
├─ IJ_zh_CN.properties     中文翻译表
├─ package.ps1             便携版打包脚本
├─ LICENSE.txt             ImageJ 许可证
├─ target\                 编译输出（Git 忽略）
└─ dist\                   便携包输出（Git 忽略）
```

---

## 2. 编译

### 2.1 使用 Maven（推荐）

进入项目目录：

```powershell
cd D:\ImageX
```

完整构建：

```powershell
mvn clean package
```

只构建、不跑测试（本项目 `pom.xml` 默认已经跳过测试）：

```powershell
mvn -DskipTests package
```

首次构建会从 Maven 仓库下载父 POM 和插件，时间取决于网络，通常 1～3 分钟；之后通常十几秒。

构建成功后会看到：

```text
BUILD SUCCESS
```

产物：

```text
D:\ImageX\target\ij-1.x-SNAPSHOT.jar
D:\ImageX\target\ij-1.x-SNAPSHOT-sources.jar
```

其中 `ij-1.x-SNAPSHOT.jar` 是可运行的 ImageJ1 主程序。

### 2.2 直接运行编译结果

```powershell
java -jar D:\ImageX\target\ij-1.x-SNAPSHOT.jar
```

或者用 Maven 的 exec 配置：

```powershell
mvn -Pexec
```

### 2.3 生成 Javadoc

```powershell
mvn javadoc:javadoc
```

输出目录：

```text
D:\ImageX\target\apidocs
```

### 2.4 清理

```powershell
mvn clean
```

会删除 `target` 目录，不会影响源码和 Git 提交。

---

## 3. 使用 Ant 编译（可选）

仓库自带 `build.xml`，官方也支持 Ant。但本机没有安装 Ant，**Maven 是推荐方式**；如果确实需要 Ant：

1. 下载 Apache Ant（1.10.x）；
2. 解压到例如 `D:\apache-ant`；
3. 设置环境变量：

```powershell
$env:ANT_HOME = 'D:\apache-ant'
$env:Path = "$env:ANT_HOME\bin;$env:Path"
ant -version
```

编译：

```powershell
cd D:\ImageX
ant -f build.xml clean build
```

产物：

```text
D:\ImageX\ij.jar
```

运行：

```powershell
java -jar D:\ImageX\ij.jar
```

或者：

```powershell
ant -f build.xml run
```

注意：

- `build.xml` 使用 `javac source="1.6" target="1.6"`，在 JDK 11 下可能出现警告；
- 如果 Ant 编译失败，请改用 Maven；
- 根目录的 `ij.jar` 已被 `.gitignore` 忽略，不会误提交。

---

## 4. 使用 IDE 编译（可选）

### IntelliJ IDEA

1. `File → Open`，选择 `D:\ImageX\pom.xml`；
2. Project SDK 选择 JDK 11；
3. Maven 导入完成后，运行主类：

```text
ij.ImageJ
```

4. 工作目录设置为 `D:\ImageX`；
5. 确保 `IJ_Props.txt`、`IJ_zh_CN.properties` 在运行 classpath 中。

### Eclipse

1. `File → Import → Existing Maven Projects`；
2. 选择 `D:\ImageX`；
3. JRE 选择 JDK 11；
4. 运行 `ij.ImageJ`。

### 不推荐 `compile.bat`

仓库里的 `compile.bat` 只编译了少量目录：

```bat
javac ij\ImageJ.java
javac ij\plugin\*.java
javac ij\plugin\filter\*.java
javac ij\plugin\frame\*.java
java ij.ImageJ
```

它不会完整处理资源、依赖和全部源码，容易缺类。请使用 Maven 或 Ant。

---

## 5. 打包为开箱即用的软件

目标：生成一个文件夹或 ZIP，里面自带 Java 运行时，目标电脑**不需要安装 Java**，双击即可启动。

### 5.1 一键打包（推荐）

在项目根目录执行：

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\package.ps1
```

也可以指定版本名：

```powershell
.\package.ps1 -Version "1.54u9-zh"
```

脚本会自动完成：

1. `mvn clean package` 编译；
2. 复制 `ij.jar`、`IJ_Props.txt`、`IJ_zh_CN.properties`、`LICENSE.txt`、`macros`；
3. 创建 `plugins`、`luts` 目录；
4. 用 JDK 11 的 `jlink` 生成精简 Java 运行时；
5. 生成 `run.bat`；
6. 生成 ZIP。

输出：

```text
D:\ImageX\dist\ImageJ-1.54u9-zh-windows-x64\
D:\ImageX\dist\ImageJ-1.54u9-zh-windows-x64.zip
```

实测大小：

- 文件夹约 **52 MB**
- ZIP 约 **35 MB**

### 5.2 便携包目录结构

```text
ImageJ-1.54u9-zh-windows-x64\
├─ run.bat                     启动脚本
├─ ij.jar                      ImageJ 主程序
├─ runtime\                    自带的 Java 11 运行时
│  └─ bin\javaw.exe
├─ IJ_Props.txt                菜单/插件定义
├─ IJ_zh_CN.properties         中文翻译表
├─ LICENSE.txt                 ImageJ 许可证
├─ macros\                     启动宏和示例宏
├─ plugins\                    放第三方插件
├─ luts\                       放 LUT 文件
└─ README.txt                  使用说明
```

### 5.3 启动

双击：

```text
run.bat
```

或命令行：

```powershell
cd D:\ImageX\dist\ImageJ-1.54u9-zh-windows-x64
.\run.bat
```

`run.bat` 的内容是：

```bat
@echo off
setlocal
cd /d "%~dp0"
start "" "runtime\bin\javaw.exe" -Xmx1024m -jar "ij.jar" %*
```

说明：

- `javaw.exe` 启动时不会弹出黑色控制台窗口；
- `-Xmx1024m` 是最大内存，处理大图时可以改成 `2048m` 或更高；
- `cd /d "%~dp0"` 保证从任意位置双击都能正确找到 `IJ_Props.txt`、`macros` 等文件；
- 目标电脑不需要安装 Java。

### 5.4 分发

直接把 ZIP 发给别人：

1. 解压 ZIP；
2. 双击 `run.bat`；
3. 如果 Windows SmartScreen 提示，选择“更多信息 → 仍要运行”。

插件放到 `plugins` 目录，LUT 放到 `luts` 目录。

### 5.5 手动打包（不用脚本）

如果不想用 `package.ps1`，可以手动执行。

```powershell
cd D:\ImageX
mvn clean package

$app = 'D:\ImageX\dist\ImageJ-manual-windows-x64'
if (Test-Path $app) { Remove-Item $app -Recurse -Force }
New-Item -ItemType Directory -Path $app | Out-Null

Copy-Item 'D:\ImageX\target\ij-1.x-SNAPSHOT.jar' "$app\ij.jar"
Copy-Item 'D:\ImageX\IJ_Props.txt' $app
Copy-Item 'D:\ImageX\IJ_zh_CN.properties' $app
Copy-Item 'D:\ImageX\LICENSE.txt' $app
Copy-Item 'D:\ImageX\macros' $app -Recurse
New-Item -ItemType Directory -Path "$app\plugins" | Out-Null
New-Item -ItemType Directory -Path "$app\luts" | Out-Null
```

生成自带 Java 运行时：

```powershell
$jlink = "$env:JAVA_HOME\bin\jlink.exe"

& $jlink `
  --add-modules java.base,java.compiler,java.desktop,java.logging,java.prefs,java.rmi,java.scripting,jdk.scripting.nashorn `
  --output "$app\runtime" `
  --strip-debug `
  --no-header-files `
  --no-man-pages `
  --compress=2
```

创建 `$app\run.bat`：

```bat
@echo off
setlocal
cd /d "%~dp0"
start "" "runtime\bin\javaw.exe" -Xmx1024m -jar "ij.jar" %*
```

压缩：

```powershell
Compress-Archive -Path "$app\*" -DestinationPath "$app.zip" -CompressionLevel Optimal
```

### 5.6 为什么要用 `jlink`

ImageJ1 只依赖标准 Java 模块。可以用 `jdeps` 查看：

```powershell
jdeps --print-module-deps --ignore-missing-deps target\ij-1.x-SNAPSHOT.jar
```

输出类似：

```text
java.base,java.compiler,java.desktop,java.rmi,java.scripting
```

`package.ps1` 在此基础上额外加入：

- `java.logging`
- `java.prefs`
- `jdk.scripting.nashorn`（JavaScript 宏支持）

`jlink` 会把运行时裁剪到约 40 MB，比完整 JDK 小很多。

> `jlink` 必须来自完整 JDK，JRE 中没有这个工具。

### 5.7 生成单文件 EXE / 安装包（可选）

便携 ZIP 已经满足“解压即用”。如果一定要 `.exe` 安装包，可以考虑：

- **jpackage**：JDK 14+ 自带，可生成 `.exe` 安装包；本项目未在 JDK 17+ 上验证 ImageJ1 的完整兼容性；
- **Launch4j**：把 `ij.jar` 包装成 `.exe`，可配置使用自带 JRE；
- **Inno Setup**：把便携文件夹做成安装程序，最稳妥。

建议优先使用便携 ZIP，避免安装器和 Java 版本兼容问题。

---

## 6. 在 Fiji 中测试新 jar（可选）

如果只想让 Fiji 使用新编译的 ImageJ1 核心：

```powershell
# 先备份
Copy-Item D:\Fiji\jars\ij-1.54p.jar D:\Fiji\jars\ij-1.54p.jar.bak

# 替换
Copy-Item D:\ImageX\target\ij-1.x-SNAPSHOT.jar D:\Fiji\jars\ij-1.54p.jar
```

出问题就恢复：

```powershell
Copy-Item D:\Fiji\jars\ij-1.54p.jar.bak D:\Fiji\jars\ij-1.54p.jar
```

注意：Fiji 对 ImageJ1 版本有依赖，替换前务必备份。更推荐直接运行便携版，不要改动 Fiji。

---

## 7. 汉化文件说明

汉化入口：

- [ij/I18n.java](ij/I18n.java)
- [IJ_zh_CN.properties](IJ_zh_CN.properties)

工作方式：

1. `I18n.label("Open...")` 在 `IJ_zh_CN.properties` 中查找 `Open...`；
2. 找到就返回中文，找不到就返回英文；
3. 菜单项显示中文，但 `ActionCommand` 仍然设置成英文，宏和插件命令照常工作。

添加翻译：

```properties
# 英文=中文
Gaussian Blur...=高斯模糊...
```

带空格的英文键需要转义空格：

```properties
Image\ Sequence...=图像序列...
```

对话框标签末尾的 `:` 会自动去掉后再查找，所以：

```properties
Width=宽度
```

可以同时匹配 `Width` 和 `Width:`。

修改翻译后重新编译：

```powershell
mvn -DskipTests package
```

当前仍是英文的部分：

- 工具栏状态栏提示
- 各插件内部的专用参数和提示
- 部分结果表列名
- 外部插件 jar 自带的菜单文本

这些内容需要继续往 `IJ_zh_CN.properties` 里补，或者直接修改对应插件源码。

---

## 8. 常见问题

### `mvn` 不是内部或外部命令

Maven 的 `bin` 目录没有加入 `PATH`：

```powershell
$env:Path = "D:\apache-maven-3.9.16\bin;$env:Path"
```

### `JAVA_HOME` 指向 JRE

必须指向完整 JDK，目录下应有：

```text
bin\javac.exe
bin\jlink.exe
```

### `Source option 6 is no longer supported`

使用了 JDK 17+。请切换到 JDK 11：

```powershell
$env:JAVA_HOME = 'D:\Eclipse Adoptium\jdk-11.0.32.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### Maven 下载依赖很慢或失败

检查网络/代理。当前机器 Maven 使用阿里云镜像；如果配置了代理，确认代理端口可用。

临时跳过测试并重试：

```powershell
mvn -U -DskipTests clean package
```

### `jlink.exe` 找不到

说明 `JAVA_HOME` 指向的是 JRE。请安装完整 JDK 11，并让 `JAVA_HOME` 指向 JDK 根目录。

### `jlink` 报 `jdk.scripting.nashorn` 不存在

有些精简 JDK 不包含 Nashorn。删掉模块列表中的 `jdk.scripting.nashorn` 再执行，ImageJ 基础功能不受影响，但 JavaScript 宏可能不可用。

### 双击 `run.bat` 没反应

在命令行运行，查看错误：

```powershell
cd D:\ImageX\dist\ImageJ-1.54u9-zh-windows-x64
.\runtime\bin\java.exe -jar ij.jar
```

### 中文显示为方框

Windows 缺少中文字体，或 Java 使用了不支持中文的默认字体。安装中文字体，或在系统中设置中文区域后重试。

### 内存不足

修改 `run.bat`：

```bat
start "" "runtime\bin\javaw.exe" -Xmx2048m -jar "ij.jar" %*
```

---

## 9. Git 提交与发布

提交修改：

```powershell
cd D:\ImageX
git add .
git commit -m "Update README and packaging"
git push
```

打版本标签：

```powershell
git tag v1.54u9-zh
git push origin main --tags
```

发布时，把 `dist\ImageJ-1.54u9-zh-windows-x64.zip` 上传到 GitHub Releases 即可。

---

## 10. 上游与许可

- 上游项目：<https://github.com/imagej/ImageJ>
- ImageJ 官网：<https://imagej.net/ij/>
- ImageJ 为 public domain，详见 [LICENSE.txt](LICENSE.txt)

本仓库是非官方中文构建，与 NIH / ImageJ 官方无隶属关系。
