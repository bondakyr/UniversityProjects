$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot

Write-Host "Starting infrastructure..."
docker compose up -d

$services = @(
    @{ Name = 'user-service'; Module = 'user-service' },
    @{ Name = 'product-catalog-service'; Module = 'product-catalog-service' },
    @{ Name = 'scraper-service'; Module = 'scraper-service' },
    @{ Name = 'notification-service'; Module = 'notification-service' },
    @{ Name = 'bff'; Module = 'bff' }
)

foreach ($service in $services) {
    Write-Host "Starting $($service.Name)..."
    Start-Process -WindowStyle Hidden `
        -FilePath powershell.exe `
        -ArgumentList @(
            '-NoProfile',
            '-ExecutionPolicy', 'Bypass',
            '-Command', "Set-Location '$root'; .\mvnw.cmd -pl $($service.Module) spring-boot:run"
        )
}

Write-Host "Stack launch requested."
