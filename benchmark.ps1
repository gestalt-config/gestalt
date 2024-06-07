$gestaltVersions = @("0.30.0", "0.29.0", "0.28.0", "0.27.0", "0.26.0", "0.25.3", "0.25.2", "0.25.1", "0.25.0", "0.24.6", "0.24.5", "0.24.4", "0.24.3", "0.24.2", "0.24.1", "0.24.0", "0.23.3", "0.23.2", "0.23.1", "0.23.0", "0.22.1", "0.21.0", "0.20.6", "0.20.5", "0.20.4", "0.20.3", "0.20.2", "0.20.1", "0.19.0", "0.18.0", "0.16.6", "0.16.5", "0.16.4", "0.16.3", "0.16.2", "0.16.1", "0.16.0", "0.15.0", "0.14.1", "0.14.0", "0.13.0", "0.12.0")
#$gestaltVersions = @("0.27.0", "0.26.0", "0.25.3", "0.25.2", "0.25.1", "0.25.0")
#jdkVersions = @(11 17 21)
$jdkVersions = @(11)

foreach ($jdkVersion in $jdkVersions)
{
  foreach ($gestaltVersion in $gestaltVersions)
  {
    $gradleCommand = "./gradlew jmh -PgestaltVersion=`"$gestaltVersion`" -PjdkVersion=`"$jdkVersion`""
    Invoke-Expression $gradleCommand
  }
}
