$ErrorActionPreference = "Stop"

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Zhibo Platform - Frontend Build" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$noCache = $false
$skipInstall = $false

foreach ($arg in $args) {
    if ($arg -eq "--no-cache") { $noCache = $true }
    if ($arg -eq "--skip-install") { $skipInstall = $true }
    if ($arg -eq "-h" -or $arg -eq "--help") {
        Write-Host "Usage: .\build-frontend.ps1 [options]"
        Write-Host ""
        Write-Host "Options:"
        Write-Host "  --skip-install  Skip npm install"
        Write-Host "  --no-cache      Build without Docker cache"
        Write-Host "  -h, --help      Show this help message"
        exit 0
    }
}

$startTime = Get-Date

Set-Location frontend

if (-not $skipInstall) {
    Write-Host ""
    Write-Host "[1/3] Installing dependencies..." -ForegroundColor Cyan
    npm install

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Frontend dependency install failed!" -ForegroundColor Red
        Set-Location $scriptPath
        exit 1
    }
} else {
    Write-Host ""
    Write-Host "[1/3] Skipping dependency install" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[2/3] Building frontend..." -ForegroundColor Cyan
npm run build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Frontend build failed!" -ForegroundColor Red
    Set-Location $scriptPath
    exit 1
}

Write-Host ""
Write-Host "[3/3] Building frontend Docker image..." -ForegroundColor Cyan
if ($noCache) {
    docker build --no-cache -t zhibo-frontend .
} else {
    docker build -t zhibo-frontend .
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Frontend Docker image build failed!" -ForegroundColor Red
    Set-Location $scriptPath
    exit 1
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Frontend Build Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  Start service:   docker-compose up -d frontend" -ForegroundColor Yellow
Write-Host "  View logs:       docker-compose logs -f frontend" -ForegroundColor Yellow

Set-Location $scriptPath
