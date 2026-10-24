# Build ImageX and create a portable Windows package with a bundled JRE.
# Usage:
#   .\package.ps1
#   .\package.ps1 -Version "2.0.1"
param(
    [string]$Version = "2.0.1"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME is not set. Install JDK 11 and set JAVA_HOME first."
}
$javaHome = $env:JAVA_HOME.TrimEnd('\')
$jlink = Join-Path $javaHome "bin\jlink.exe"
if (-not (Test-Path $jlink)) {
    throw "jlink.exe not found under $javaHome. A full JDK 11 is required."
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "mvn was not found in PATH. Install Maven and add its bin directory to PATH."
}

Write-Host "==> Building ImageX.jar" -ForegroundColor Cyan
mvn -B -DskipTests clean package
if ($LASTEXITCODE -ne 0) { throw "Maven build failed ($LASTEXITCODE)." }

$jar = Get-ChildItem -Path (Join-Path $PSScriptRoot "target") -Filter "imagex-*.jar" |
    Where-Object { $_.Name -notmatch "sources" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if ($null -eq $jar) { throw "Built jar not found in target." }

$distRoot = Join-Path $PSScriptRoot "dist"
$appDir = Join-Path $distRoot "ImageX-$Version-windows-x64"
if (Test-Path $appDir) { Remove-Item -LiteralPath $appDir -Recurse -Force }
New-Item -ItemType Directory -Path $appDir | Out-Null

Write-Host "==> Copying application files" -ForegroundColor Cyan
Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $appDir "ImageX.jar")
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "IJ_Props.txt") -Destination $appDir
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "IJ_zh_CN.properties") -Destination $appDir
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "LICENSE.txt") -Destination $appDir
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "macros") -Destination $appDir -Recurse
New-Item -ItemType Directory -Path (Join-Path $appDir "plugins") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $appDir "luts") | Out-Null

Write-Host "==> Creating bundled Java runtime with jlink" -ForegroundColor Cyan
$modules = "java.base,java.compiler,java.desktop,java.logging,java.prefs,java.rmi,java.scripting,jdk.scripting.nashorn"
& $jlink --add-modules $modules --output (Join-Path $appDir "runtime") --strip-debug --no-header-files --no-man-pages --compress=2
if ($LASTEXITCODE -ne 0) { throw "jlink failed ($LASTEXITCODE)." }

Write-Host "==> Building ImageX.exe launcher" -ForegroundColor Cyan
$csc = @(
    (Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"),
    (Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe")
) | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $csc) {
    throw ".NET Framework 4.x C# compiler (csc.exe) was not found."
}
$launcherSource = Join-Path $PSScriptRoot "launcher\ImageXLauncher.cs"
$launcherExe = Join-Path $appDir "ImageX.exe"
& $csc /nologo /target:winexe /platform:anycpu "/win32icon:$PSScriptRoot\images\ImageX.ico" /r:System.Management.dll "/out:$launcherExe" $launcherSource
if ($LASTEXITCODE -ne 0) { throw "C# launcher compilation failed ($LASTEXITCODE)." }

$readme = @"
ImageX $Version portable (Windows x64)
=====================================

Double-click ImageX.exe to start ImageX.

Contents
--------
ImageX.exe             launcher
ImageX.jar             ImageX application
runtime\                bundled Java 11 runtime
IJ_Props.txt            menu/plugin definitions
IJ_zh_CN.properties     Chinese labels
macros\                 startup macros and examples
plugins\                put extra ImageX plugins here
luts\                   put LUT files here

Requires Windows 10/11 x64. No separate Java installation is needed.
"@
[IO.File]::WriteAllText((Join-Path $appDir "README.txt"), $readme, [Text.Encoding]::UTF8)

Write-Host "==> Creating zip" -ForegroundColor Cyan
$zip = Join-Path $distRoot "ImageX-$Version-windows-x64.zip"
if (Test-Path $zip) { Remove-Item -LiteralPath $zip -Force }
Compress-Archive -Path (Join-Path $appDir "*") -DestinationPath $zip -CompressionLevel Optimal

Write-Host ""
Write-Host "Done:" -ForegroundColor Green
Write-Host "  Folder: $appDir"
Write-Host "  Zip:    $zip"
