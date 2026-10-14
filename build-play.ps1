param([string]$Sdk = $(if (Test-Path 'D:\KS\App idea\.tools\android-sdk') { 'D:\KS\App idea\.tools\android-sdk' } else { Join-Path $PSScriptRoot '..\.tools\android-sdk' }))
$ErrorActionPreference = 'Stop'
& "$PSScriptRoot\build.ps1" -Sdk $Sdk
if ($LASTEXITCODE -ne 0) { throw 'APK compilation failed' }
$javaBin = Split-Path (Get-Command java.exe).Source
$tools = if (Test-Path 'D:\KS\App idea\.tools') { 'D:\KS\App idea\.tools' } else { [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\.tools')) }
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
$aab = "$release\Beyond-Alarm-Universal-2.0.aab"
& java -jar "$tools\bundletool.jar" build-bundle --modules=$module --output=$aab --overwrite
Check
$key = "$PSScriptRoot\signing\play-upload.p12"
$password = "$PSScriptRoot\signing\play-upload-password.txt"
if (-not (Test-Path $key)) {
  if (Test-Path $password) { throw 'Password already exists without upload key; inspect before replacing.' }
  $bytes = New-Object byte[] 32
  [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
  [IO.File]::WriteAllText($password, [Convert]::ToBase64String($bytes))
  & "$javaBin\keytool.exe" -genkeypair -keystore $key -storetype PKCS12 -storepass:file $password -keypass:file $password -alias play-upload -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Kibria, OU=Beyond Alarm, C=BD'
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

# Copy bundle and assets to D:\KS\Beyond Alarm\GPlayStore
$gplay = "D:\KS\Beyond Alarm\GPlayStore"
New-Item -ItemType Directory -Force $gplay | Out-Null
Copy-Item $aab (Join-Path $gplay "Beyond-Alarm-Universal-2.0.aab") -Force
Copy-Item "$release\icon-512.png" (Join-Path $gplay "icon-512.png") -Force -ErrorAction SilentlyContinue
Copy-Item "$release\feature-graphic-1024x500.png" (Join-Path $gplay "feature-graphic-1024x500.png") -Force -ErrorAction SilentlyContinue
Copy-Item "$release\privacy-policy.html" (Join-Path $gplay "privacy-policy.html") -Force -ErrorAction SilentlyContinue
Copy-Item "$release\store-listing.txt" (Join-Path $gplay "store-listing.txt") -Force -ErrorAction SilentlyContinue
if (Test-Path "$release\screenshots") {
  Copy-Item "$release\screenshots" $gplay -Recurse -Force
}

Get-FileHash $aab -Algorithm SHA256 | Format-List
Write-Host "Play Store Kit and AAB Bundle created successfully in $gplay"
