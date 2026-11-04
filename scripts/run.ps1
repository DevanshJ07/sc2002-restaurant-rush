& "$PSScriptRoot/build.ps1"
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME/bin/java.exe")) { $java = "$env:JAVA_HOME/bin/java.exe" }
elseif (Get-Command java -ErrorAction SilentlyContinue) { $java = (Get-Command java).Source }
else { $java = 'C:/Program Files/BlueJ/jdk/bin/java.exe' }
& $java -cp out restaurantrush.Main
