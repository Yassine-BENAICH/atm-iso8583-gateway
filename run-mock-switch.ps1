# ISO 8583 Mock Switch Runner
# This script compiles and runs the mock payment switch simulator

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ISO 8583 Mock Switch Starter" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is installed
Write-Host "Checking Maven installation..." -ForegroundColor Yellow
$mavenVersion = mvn -version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven from: https://maven.apache.org/download.cgi" -ForegroundColor Red
    exit 1
}
Write-Host "Maven found: OK" -ForegroundColor Green
Write-Host ""

# Check if Java is installed
Write-Host "Checking Java installation..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 17 or higher" -ForegroundColor Red
    exit 1
}
Write-Host "Java found: OK" -ForegroundColor Green
Write-Host ""

# Compile the project
Write-Host "Compiling project..." -ForegroundColor Yellow
mvn clean compile -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Compilation failed" -ForegroundColor Red
    exit 1
}
Write-Host "Compilation successful" -ForegroundColor Green
Write-Host ""

# Run the Mock Switch
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Mock Switch on port 9000..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop the Mock Switch" -ForegroundColor Yellow
Write-Host ""

mvn exec:java
