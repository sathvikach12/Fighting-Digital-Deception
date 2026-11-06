param(
    [string]$MySqlUser = 'root',
    [string]$MySqlHost = 'localhost',
    [string]$MySqlPort = '3306'
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $repoRoot 'target\truthchain-news\WEB-INF\lib\jbcrypt-0.4.jar'
if (-not (Test-Path -LiteralPath $jar)) {
    throw 'Build the WAR first with mvn clean package so the BCrypt library is available.'
}

$jshell = Get-Command jshell -ErrorAction SilentlyContinue
if (-not $jshell) { throw 'JDK 17 jshell was not found. Set JAVA_HOME to a JDK 17 installation.' }

$script = @'
import org.mindrot.jbcrypt.BCrypt;
System.out.println(BCrypt.hashpw("AdminDemo!2026", BCrypt.gensalt(12)));
System.out.println(BCrypt.hashpw("ReaderDemo!2026", BCrypt.gensalt(12)));
/exit
'@
$hashes = @($script | & $jshell.Source --class-path $jar - 2>$null | Where-Object { $_ -match '^\$2[aby]\$12\$[./A-Za-z0-9]{53}$' })
if ($hashes.Count -lt 2) { throw 'Could not generate BCrypt hashes. Run this script from PowerShell with JDK 17.' }

$seed = Get-Content -Raw -LiteralPath (Join-Path $repoRoot 'database\seed.sql')
$seed = $seed.Replace('{bcrypt-hash-admin}', $hashes[0]).Replace('{bcrypt-hash-reader}', $hashes[1])
$tempFile = Join-Path ([System.IO.Path]::GetTempPath()) ('truthchain-seed-' + [guid]::NewGuid().ToString('N') + '.sql')
try {
    Set-Content -LiteralPath $tempFile -Value $seed -Encoding utf8
    $mysqlArgs = @("--host=$MySqlHost", "--port=$MySqlPort", "--user=$MySqlUser", 'truthchain')
    $seedSql = Get-Content -Raw -LiteralPath $tempFile
    $seedSql | & mysql @mysqlArgs
    if ($LASTEXITCODE -ne 0) { throw 'MySQL rejected the seed script. Check the schema and credentials.' }
    Write-Host 'Demo seed accounts created.'
    Write-Host 'admin@truthchain.local / AdminDemo!2026'
    Write-Host 'reader@truthchain.local / ReaderDemo!2026'
    Write-Host 'pending@truthchain.local / ReaderDemo!2026'
} finally {
    Remove-Item -LiteralPath $tempFile -ErrorAction SilentlyContinue
}
