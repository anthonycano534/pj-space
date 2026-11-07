param([string]$JdkPath)
$ErrorActionPreference = 'Stop'
$workspacePath = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
if (-not $JdkPath) {
    $portablePath = Join-Path $workspacePath 'work/jdk17'
    if (Test-Path -LiteralPath $portablePath) {
        $JdkPath = (Get-ChildItem -LiteralPath $portablePath -Directory | Select-Object -First 1).FullName
    } elseif ($env:JAVA_HOME) { $JdkPath = $env:JAVA_HOME }
}
if (-not $JdkPath -or -not (Test-Path -LiteralPath (Join-Path $JdkPath 'bin/java.exe'))) { throw 'Indicá la carpeta de Java 17 con -JdkPath.' }
$env:JAVA_HOME = $JdkPath
$env:GRADLE_USER_HOME = Join-Path $workspacePath 'work/gradle-home'
& (Join-Path $PSScriptRoot 'gradlew.bat') -p $PSScriptRoot assembleDebug testDebugUnitTest --no-daemon --console=plain
exit $LASTEXITCODE
