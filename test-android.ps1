param([string]$Sdk = (Join-Path $PSScriptRoot '..\.tools\android-sdk'), [string]$Runner = 'SmokeTest')
$ErrorActionPreference='Stop'
$sdkPath=[IO.Path]::GetFullPath($Sdk)
$bt=Join-Path $sdkPath 'build-tools\35.0.0'
$androidJar=Join-Path $sdkPath 'platforms\android-36\android.jar'
$build=Join-Path $PSScriptRoot 'build'
$testBuild=Join-Path $build 'instrumentation'
$javaBin=Split-Path (Get-Command java.exe).Source
function Check-Exit { if($LASTEXITCODE -ne 0){throw "Test command failed: $LASTEXITCODE"} }
New-Item -ItemType Directory -Force $testBuild,"$testBuild\classes","$testBuild\dex" | Out-Null
& "$bt\aapt2.exe" link -o "$testBuild\base.apk" --manifest "$PSScriptRoot\tests\android\AndroidManifest.xml" -I $androidJar
Check-Exit
$testSources = @(Get-ChildItem "$PSScriptRoot\tests\android" -Filter '*.java' | ForEach-Object FullName)
& "$javaBin\javac.exe" --release 8 -encoding UTF-8 -classpath "$androidJar;$build\classes" -d "$testBuild\classes" $testSources
Check-Exit
& "$javaBin\jar.exe" cf "$testBuild\classes.jar" -C "$testBuild\classes" .
Check-Exit
& "$bt\d8.bat" --lib $androidJar --classpath "$build\classes.jar" --min-api 29 --output "$testBuild\dex" "$testBuild\classes.jar"
Check-Exit
& "$javaBin\jar.exe" uf "$testBuild\base.apk" -C "$testBuild\dex" classes.dex
Check-Exit
& "$bt\zipalign.exe" -f 4 "$testBuild\base.apk" "$testBuild\aligned.apk"
Check-Exit
& "$bt\apksigner.bat" sign --ks "$PSScriptRoot\signing\personal-build.jks" --ks-pass pass:android --out "$testBuild\test.apk" "$testBuild\aligned.apk"
Check-Exit
$adb=Join-Path $sdkPath 'platform-tools\adb.exe'
$device=& $adb -s emulator-5554 get-state
Check-Exit
if($device.Trim() -ne 'device'){throw 'Start the disposable emulator on port 5554 first.'}
& $adb -s emulator-5554 install -r "$PSScriptRoot\output\Dynamic-Alarm-1.3.apk"
Check-Exit
& $adb -s emulator-5554 install -r "$testBuild\test.apk"
Check-Exit
& $adb -s emulator-5554 shell pm grant com.kibria.dynamicalarm android.permission.POST_NOTIFICATIONS
& $adb -s emulator-5554 shell appops set com.kibria.dynamicalarm SCHEDULE_EXACT_ALARM allow
& $adb -s emulator-5554 shell appops set com.kibria.dynamicalarm USE_FULL_SCREEN_INTENT allow
$testOutput = & $adb -s emulator-5554 shell am instrument -w "com.kibria.dynamicalarm.test/.$Runner"
Write-Output $testOutput
Check-Exit
if (($testOutput -join "`n") -notmatch 'runtime checks passed' -or ($testOutput -join "`n") -match 'FAILED:') { throw 'Android runtime tests failed.' }


