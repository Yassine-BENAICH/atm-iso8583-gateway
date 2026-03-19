# Get the JDK path used by the user
$JavaHome = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
$env:JAVA_HOME = $JavaHome

Write-Host "Compiling and starting ISO 8583 Mock Switch..." -ForegroundColor Cyan

# Use Maven to run the class from src/main/java
mvn exec:java `
    "-Dexec.mainClass=com.atm.iso8583.simulator.Iso8583MockSwitch" `
    "-Dexec.args='9000'"
