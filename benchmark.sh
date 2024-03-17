#!/bin/sh

#gestalt_versions=(0.25.0 0.24.6 0.24.5 0.24.4 0.24.3 0.24.2 0.24.1  0.24.0  0.23.3  0.23.2  0.23.1  0.23.0  0.22.1 0.21.0)
gestalt_versions=(0.25.0 0.24.6)
#jdk_version=(11 17 21)
jdk_version=(11)

for jdk in ${jdk_version[@]}
do
for v in ${gestalt_versions[@]}
do
        ./gradlew jmh -DgestaltVersion=$v -DjdkVersion=$jdk
done
done
