$ErrorActionPreference = "Stop"

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Zhibo Platform - Backend Build" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$skipTests = $true
$noCache = $false

foreach ($arg in $args) {
    if ($arg -eq "--with-tests") { $skipTests = $false }
    if ($arg -eq "--no-cache") { $noCache = $true }
    if ($arg -eq "-h" -or $arg -eq "--help") {
        Write-Host "Usage: .\build-backend.ps1 [options]"
        Write-Host ""
        Write-Host "Options:"
        Write-Host "  --with-tests    Run tests (default: skip)"
        Write-Host "  --no-cache      Build without Docker cache"
        Write-Host "  -h, --help      Show this help message"
        exit 0
    }
}

$startTime = Get-Date

Set-Location backend

Write-Host ""
Write-Host "[1/2] Building backend with Maven..." -ForegroundColor Cyan
if ($skipTests) {
    mvn clean package -DskipTests
} else {
    mvn clean package
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Backend build failed!" -ForegroundColor Red
    Set-Location $scriptPath
    exit 1
}

Write-Host ""
Write-Host "[2/2] Building backend Docker image..." -ForegroundColor Cyan
if ($noCache) {
    docker build --no-cache -t zhibo-backend .
} else {
    docker build -t zhibo-backend .
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Backend Docker image build failed!" -ForegroundColor Red
    Set-Location $scriptPath
    exit 1
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Backend Build Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  Start service:   docker-compose up -d backend" -ForegroundColor Yellow
Write-Host "  View logs:       docker-compose logs -f backend" -ForegroundColor Yellow

Set-Location $scriptPath
