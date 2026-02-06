$ErrorActionPreference = "Stop"

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Zhibo Platform - Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$buildBackend = $true
$buildFrontend = $true
$skipTests = $true
$noCache = $false

foreach ($arg in $args) {
    if ($arg -eq "--no-backend") { $buildBackend = $false }
    if ($arg -eq "--no-frontend") { $buildFrontend = $false }
    if ($arg -eq "--with-tests") { $skipTests = $false }
    if ($arg -eq "--no-cache") { $noCache = $true }
    if ($arg -eq "-h" -or $arg -eq "--help") {
        Write-Host "Usage: .\build-all.ps1 [options]"
        Write-Host ""
        Write-Host "Options:"
        Write-Host "  --no-backend    Skip backend build"
        Write-Host "  --no-frontend   Skip frontend build"
        Write-Host "  --with-tests    Run tests (default: skip)"
        Write-Host "  --no-cache      Build without Docker cache"
        Write-Host "  -h, --help      Show this help message"
        exit 0
    }
}

$startTime = Get-Date

if ($buildBackend) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "  Building Backend" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow

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

    Write-Host ""
    Write-Host "Backend build completed!" -ForegroundColor Green
    Set-Location $scriptPath
}

if ($buildFrontend) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "  Building Frontend" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow

    Set-Location frontend

    Write-Host ""
    Write-Host "[1/3] Checking dependencies..." -ForegroundColor Cyan
    if (-Not (Test-Path "node_modules")) {
        npm install
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Frontend dependency install failed!" -ForegroundColor Red
            Set-Location $scriptPath
            exit 1
        }
    } else {
        Write-Host "node_modules exists, skipping install" -ForegroundColor Gray
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

    Write-Host ""
    Write-Host "Frontend build completed!" -ForegroundColor Green
    Set-Location $scriptPath
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Build Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  Start all services:  docker-compose up -d" -ForegroundColor Yellow
Write-Host "  Check status:         docker-compose ps" -ForegroundColor Yellow
Write-Host "  View logs:            docker-compose logs -f" -ForegroundColor Yellow
Write-Host "  Stop all services:    docker-compose down" -ForegroundColor Yellow
