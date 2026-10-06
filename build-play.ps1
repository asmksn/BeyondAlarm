param([string]$Sdk = (Join-Path $PSScriptRoot '..\.tools\android-sdk'))
$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\build.ps1" -Sdk $Sdk
if ($LASTEXITCODE -ne 0) { throw 'APK compilation failed' }
$javaBin = Split-Path (Get-Command java.exe).Source
$tools = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\.tools'))
$build = "$PSScriptRoot\build"
$release = "$PSScriptRoot\output\play-store"
New-Item -ItemType Directory -Force $release | Out-Null
function Check { if ($LASTEXITCODE -ne 0) { throw "Bundle build failed: $LASTEXITCODE" } }
& "$tools\aapt2-maven\aapt2.exe" link --proto-format -o "$build\proto.apk" --manifest "$PSScriptRoot\AndroidManifest.xml" -I "$Sdk\platforms\android-36\android.jar" -A "$PSScriptRoot\assets" "$build\resources.zip"
Check
Add-Type -AssemblyName System.IO.Compression.FileSystem
$module = "$build\base-module.zip"
if (Test-Path $module) { Remove-Item -LiteralPath $module }
$inputZip = [IO.Compression.ZipFile]::OpenRead("$build\proto.apk")
$outputZip = [IO.Compression.ZipFile]::Open($module, 'Create')
try {
  foreach ($entry in $inputZip.Entries) {
    $name = $entry.FullName
    if ($name -eq 'AndroidManifest.xml') { $name = 'manifest/AndroidManifest.xml' }
    $target = $outputZip.CreateEntry($name)
    $sourceStream = $entry.Open(); $targetStream = $target.Open()
    try { $sourceStream.CopyTo($targetStream) } finally { $sourceStream.Dispose(); $targetStream.Dispose() }
  }
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($outputZip, "$build\dex\classes.dex", 'dex/classes.dex') | Out-Null
} finally { $inputZip.Dispose(); $outputZip.Dispose() }
$aab = "$release\Dynamic-Alarm-1.3.aab"
& java -jar "$tools\bundletool.jar" build-bundle --modules=$module --output=$aab --overwrite
Check
$key = "$PSScriptRoot\signing\play-upload.p12"
$password = "$PSScriptRoot\signing\play-upload-password.txt"
if (-not (Test-Path $key)) {
  if (Test-Path $password) { throw 'Password already exists without upload key; inspect before replacing.' }
  $bytes = New-Object byte[] 32
  [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
  [IO.File]::WriteAllText($password, [Convert]::ToBase64String($bytes))
  & "$javaBin\keytool.exe" -genkeypair -keystore $key -storetype PKCS12 -storepass:file $password -keypass:file $password -alias play-upload -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Kibria, OU=Dynamic Alarm, C=BD'
  Check
}
& "$javaBin\jarsigner.exe" -keystore $key -storepass:file $password -digestalg SHA-256 -sigalg SHA256withRSA $aab play-upload
Check
& "$javaBin\jarsigner.exe" -verify $aab
Check
& java -jar "$tools\bundletool.jar" validate --bundle=$aab
Check
& java -jar "$tools\bundletool.jar" dump manifest --bundle=$aab | Set-Content "$release\bundle-manifest.xml"
Check
Get-FileHash $aab -Algorithm SHA256 | Format-List
