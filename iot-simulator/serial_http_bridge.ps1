param(
    [ValidatePattern('^COM\d+$')]
    [string]$Port = 'COM6',

    [ValidateRange(1200, 2000000)]
    [int]$BaudRate = 115200,

    [ValidateNotNullOrEmpty()]
    [string]$ApiUrl = 'http://127.0.0.1:8080/api/iot/telemetry'
)

$serial = [System.IO.Ports.SerialPort]::new(
    $Port,
    $BaudRate,
    [System.IO.Ports.Parity]::None,
    8,
    [System.IO.Ports.StopBits]::One
)
$serial.NewLine = "`n"
$serial.ReadTimeout = 1000
$serial.WriteTimeout = 1000
$jsonBuffer = New-Object System.Text.StringBuilder
$insideJson = $false

try {
    try {
        $serial.Open()
    }
    catch [System.UnauthorizedAccessException] {
        throw "A porta $Port está ocupada ou sem permissão. Feche o Monitor Serial/Arduino IDE e tente novamente."
    }

    Write-Host "Ponte ativa: $Port em $BaudRate baud"
    Write-Host "Backend: $ApiUrl"
    Write-Host 'Pressione Ctrl+C para encerrar.'
    Start-Sleep -Seconds 2

    while ($true) {
        try {
            $line = $serial.ReadLine().Trim()
        }
        catch [System.TimeoutException] {
            continue
        }

        if ($line.StartsWith('[COMANDO RECEBIDO]') -or $line.StartsWith('[ERRO]')) {
            Write-Host $line -ForegroundColor Cyan
            continue
        }

        if ($line.StartsWith('{')) {
            $jsonBuffer.Clear() | Out-Null
            $insideJson = $true
        }

        if (-not $insideJson) {
            continue
        }

        $jsonBuffer.AppendLine($line) | Out-Null
        if (-not $line.EndsWith('}')) {
            continue
        }

        $jsonText = $jsonBuffer.ToString()
        $jsonBuffer.Clear() | Out-Null
        $insideJson = $false

        try {
            $telemetry = ConvertFrom-Json -InputObject $jsonText -ErrorAction Stop
        }
        catch {
            Write-Host "[SERIAL ERRO] JSON inválido recebido: $($_.Exception.Message)" -ForegroundColor Red
            continue
        }

        $brightness = 0
        if ([string]$telemetry.codigoTorre -eq '' -or
            -not [int]::TryParse([string]$telemetry.luminosidadeLdr, [ref]$brightness) -or
            $brightness -lt 0 -or $brightness -gt 1023 -or
            $telemetry.pressaoBotaoCerracao -isnot [bool]) {
            Write-Host '[SERIAL ERRO] Telemetria inválida: confira código, luminosidade (0-1023) e botão.' -ForegroundColor Red
            continue
        }

        $payload = @{
            codigoTorre = [string]$telemetry.codigoTorre
            luminosidadeLdr = $brightness
            pressaoBotaoCerracao = [bool]$telemetry.pressaoBotaoCerracao
        }

        $battery = 0.0
        if ([double]::TryParse(
                [string]$telemetry.nivelBateria,
                [System.Globalization.NumberStyles]::Float,
                [System.Globalization.CultureInfo]::InvariantCulture,
                [ref]$battery
            )) {
            $payload.nivelBateria = $battery
        }

        try {
            $response = Invoke-RestMethod `
                -Method Post `
                -Uri $ApiUrl `
                -ContentType 'application/json' `
                -Body ($payload | ConvertTo-Json -Compress) `
                -TimeoutSec 5 `
                -ErrorAction Stop

            $command = switch ([string]$response.corLedSugerida) {
                'VERDE' { 'V'; break }
                'AMBAR' { 'A'; break }
                'AZUL' { 'Z'; break }
                'VERMELHO' { 'R'; break }
                'APAGADO' { 'O'; break }
                default {
                    throw "Cor de LED desconhecida recebida do backend: '$($response.corLedSugerida)'"
                }
            }
            $serial.WriteLine($command)
            Write-Host (
                '[OK] {0}: LDR={1}, botão={2} -> comando={3}, modo={4}' -f
                $payload.codigoTorre,
                $brightness,
                $payload.pressaoBotaoCerracao,
                $command,
                $response.modoLuz
            )
        }
        catch {
            Write-Host "[HTTP ERRO] Não foi possível enviar/retornar a telemetria: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}
finally {
    if ($serial.IsOpen) {
        $serial.Close()
    }
}
