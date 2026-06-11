$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projectDir

$libJar = "lib/mysql-connector-j-9.6.0.jar"
$srcDir = Join-Path $projectDir "src"
$jarName = "AplikasiKeuangan.jar"
$classesDir = "classes"
$manifestPath = Join-Path $projectDir "manifest.mf"

if (Test-Path $classesDir) {
    Remove-Item -Recurse -Force $classesDir
}
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

$sources = Get-ChildItem -Path $srcDir -Filter *.java | ForEach-Object { $_.FullName }
if ($sources.Count -eq 0) {
    Write-Error "Tidak ditemukan file .java di folder src."
    exit 1
}

Write-Host "Compiling sources..."
$javacArgs = @("-d", $classesDir, "-cp", $libJar) + $sources
& javac @javacArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Kompilasi gagal."
    exit $LASTEXITCODE
}

function Get-JarPath {
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin\jar.exe"
        if (Test-Path $candidate) { return $candidate }
    }
    $cmd = Get-Command jar -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $javacCmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($javacCmd) {
        $candidate = Join-Path (Split-Path $javacCmd.Source) "jar.exe"
        if (Test-Path $candidate) { return $candidate }
    }
    return $null
}

$jarPath = Get-JarPath
if (-not $jarPath) {
    Write-Error "Tool jar.exe tidak ditemukan. Pastikan JDK terinstal dan jar dapat diakses."
    exit 1
}

$manifest = @"
Manifest-Version: 1.0
Main-Class: LoginFrame
Class-Path: lib/mysql-connector-j-9.6.0.jar
"@
Set-Content -Path $manifestPath -Value $manifest -Encoding ASCII

if (Test-Path $jarName) {
    Remove-Item -Force $jarName
}

Write-Host "Membuat JAR executable $jarName..."
& "$jarPath" --create --file $jarName --manifest $manifestPath -C $classesDir .
if ($LASTEXITCODE -ne 0) {
    Write-Error "Pembuatan JAR gagal."
    exit $LASTEXITCODE
}

Remove-Item -Force $manifestPath
Write-Host "Selesai. Jalankan dengan:"
Write-Host "  java -jar $jarName"
Write-Host "Pastikan file lib/mysql-connector-j-9.6.0.jar tetap berada di samping JAR."
