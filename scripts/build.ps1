param([switch]$Test)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$javac = if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME/bin/javac.exe")) { "$env:JAVA_HOME/bin/javac.exe" } elseif (Get-Command javac -ErrorAction SilentlyContinue) { (Get-Command javac).Source } elseif (Test-Path 'C:/Program Files/BlueJ/jdk/bin/javac.exe') { 'C:/Program Files/BlueJ/jdk/bin/javac.exe' } else { throw 'Install JDK 17+ and set JAVA_HOME or add its bin directory to PATH.' }
$java = Join-Path (Split-Path $javac) 'java.exe'
New-Item -ItemType Directory -Force out | Out-Null
& $javac --release 17 -d out @(Get-ChildItem src/main/java/restaurantrush/*.java | ForEach-Object FullName)
if ($LASTEXITCODE -ne 0) { throw 'Production compilation failed' }
if ($Test) {
    New-Item -ItemType Directory -Force out-test | Out-Null
    & $javac --release 17 -cp out -d out-test @(Get-ChildItem src/test/java/restaurantrush/*.java | ForEach-Object FullName)
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
    foreach ($check in Get-ChildItem src/test/java/restaurantrush/*Checks.java) {
        & $java -cp 'out;out-test' "restaurantrush.$($check.BaseName)"
        if ($LASTEXITCODE -ne 0) { throw "$($check.BaseName) failed" }
    }
}
