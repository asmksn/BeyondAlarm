param([string]$Sdk = $(if (Test-Path 'D:\KS\App idea\.tools\android-sdk') { 'D:\KS\App idea\.tools\android-sdk' } else { Join-Path $PSScriptRoot '..\.tools\android-sdk' }))
$ErrorActionPreference = 'Stop'
$sdkPath = [IO.Path]::GetFullPath($Sdk)
$buildTools = Join-Path $sdkPath 'build-tools\35.0.0'
$androidJar = Join-Path $sdkPath 'platforms\android-36\android.jar'
$build = Join-Path $PSScriptRoot 'build'
$javaBin = Split-Path (Get-Command java.exe).Source
function Check-Exit { if ($LASTEXITCODE -ne 0) { throw "Build command failed with exit code $LASTEXITCODE" } }
New-Item -ItemType Directory -Force $build, "$build\compiled", "$build\generated", "$build\classes", "$build\dex", "$build\tests", "$PSScriptRoot\signing", "$PSScriptRoot\output" | Out-Null
& "$buildTools\aapt2.exe" compile --dir "$PSScriptRoot\res" -o "$build\resources.zip"
Check-Exit
& "$buildTools\aapt2.exe" link -o "$build\base.apk" --manifest "$PSScriptRoot\AndroidManifest.xml" -I $androidJar --java "$build\generated" -A "$PSScriptRoot\assets" "$build\resources.zip"
Check-Exit
$sources = @(Get-ChildItem "$PSScriptRoot\src", "$build\generated" -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& "$javaBin\javac.exe" -encoding UTF-8 --release 8 -classpath $androidJar -d "$build\classes" $sources
Check-Exit
& "$javaBin\jar.exe" cf "$build\classes.jar" -C "$build\classes" .
Check-Exit
& "$buildTools\d8.bat" --lib $androidJar --min-api 29 --output "$build\dex" "$build\classes.jar"
Check-Exit
Copy-Item "$build\base.apk" "$build\unsigned.apk" -Force
& "$javaBin\jar.exe" uf0 "$build\unsigned.apk" -C "$build\dex" classes.dex
Check-Exit
& "$buildTools\zipalign.exe" -f 4 "$build\unsigned.apk" "$build\aligned.apk"
Check-Exit
$key = "$PSScriptRoot\signing\personal-build.jks"
if (-not (Test-Path $key)) {
    & "$javaBin\keytool.exe" -genkeypair -keystore $key -storepass android -keypass android -alias alarm-next -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Alarm Next Personal Build, O=Personal, C=BD'
    Check-Exit
}
$alias = if (Test-Path $key) { 'alarm-next' } else { 'dynamic-alarm' }
# Check alias in keystore
$aliasList = & "$javaBin\keytool.exe" -list -keystore $key -storepass android
$signingAlias = if ($aliasList -match 'alarm-next') { 'alarm-next' } else { 'dynamic-alarm' }

$outApk = "$PSScriptRoot\output\Beyond-Alarm-Universal-2.0.apk"
& "$buildTools\apksigner.bat" sign --ks $key --ks-key-alias $signingAlias --ks-pass pass:android --key-pass pass:android --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true --out $outApk "$build\aligned.apk"
Check-Exit
& "$buildTools\apksigner.bat" verify --verbose $outApk
Check-Exit
& "$javaBin\javac.exe" -encoding UTF-8 -d "$build\tests" "$PSScriptRoot\src\com\kibria\dynamicalarm\Times.java" "$PSScriptRoot\tests\TimesTest.java"
Check-Exit
& "$javaBin\java.exe" -cp "$build\tests" TimesTest
Check-Exit
Copy-Item $outApk (Join-Path $PSScriptRoot "..\Beyond-Alarm-Universal-2.0.apk") -Force -ErrorAction SilentlyContinue
Get-FileHash $outApk -Algorithm SHA256





