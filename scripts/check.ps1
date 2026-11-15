param([string] $Connector, [switch] $Gui)
$ErrorActionPreference='Stop'
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    New-Item -ItemType Directory -Force build,build/without,build/with | Out-Null
    $src = @(Get-ChildItem Task*/source/*.java | ForEach-Object FullName)
    javac -d build $src scripts/GuiEvidence.java
    if ($LASTEXITCODE) { throw 'Compilation failed' }
    java -cp build FeeServiceTest
    if ($LASTEXITCODE) { throw 'Fee test failed' }
    javac -d build/without Task04/variants/no-annotation/DrawPad.java
    if ($LASTEXITCODE) { throw 'Unannotated variant should compile' }
    & javac -d build/with Task04/variants/with-annotation/DrawPad.java 2>&1 | ForEach-Object ToString
    if ($LASTEXITCODE -eq 0) { throw 'Annotated misspelling should fail' }
    if ($Gui) {
        if (!$Connector) { throw 'Pass the path to the MySQL Connector/J jar with -Connector' }
        java -cp "build;$Connector" GuiEvidence
        if ($LASTEXITCODE) { throw 'GUI evidence process failed' }
    }
} finally {
    Pop-Location
}
