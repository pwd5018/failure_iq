param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$InputDirectory = "sample-data"
)

$ErrorActionPreference = "Stop"

# This script reads generated JSON files and posts them to the backend.

if (-not (Test-Path $InputDirectory)) {
    throw "Input directory '$InputDirectory' was not found. Run the generator first."
}

$files = Get-ChildItem -Path $InputDirectory -Filter *.json | Sort-Object Name

if ($files.Count -eq 0) {
    throw "No JSON files were found in '$InputDirectory'. Run the generator first."
}

foreach ($file in $files) {
    $body = Get-Content -Path $file.FullName -Raw
    Write-Host "Importing $($file.Name)..."

    $response = Invoke-RestMethod `
        -Method Post `
        -Uri "$ApiBaseUrl/api/test-runs" `
        -ContentType "application/json" `
        -Body $body

    Write-Host "Created test run with id $($response.id) and name '$($response.runName)'"
}
