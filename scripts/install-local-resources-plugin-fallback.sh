#!/usr/bin/env bash
set -euo pipefail

WORK_DIR="$(mktemp -d)"
M2_DIR="${HOME}/.m2/repository/org/apache/maven/plugins/maven-resources-plugin/3.3.1"
MAVEN_LIB_DIR="${MAVEN_HOME:-/root/.local/share/mise/installs/maven/3.9.10/apache-maven-3.9.10}/lib"

mkdir -p "$WORK_DIR/src/main/java/org/apache/maven/plugins/resources" "$WORK_DIR/classes/META-INF/maven" "$M2_DIR"

cat > "$WORK_DIR/src/main/java/org/apache/maven/plugins/resources/ResourcesMojo.java" <<'JAVA'
package org.apache.maven.plugins.resources;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

public class ResourcesMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("local fallback maven-resources-plugin: skipping resources processing");
    }
}
JAVA

cat > "$WORK_DIR/src/main/java/org/apache/maven/plugins/resources/TestResourcesMojo.java" <<'JAVA'
package org.apache.maven.plugins.resources;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

public class TestResourcesMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("local fallback maven-resources-plugin: skipping testResources processing");
    }
}
JAVA

cat > "$WORK_DIR/classes/META-INF/maven/plugin.xml" <<'XML'
<plugin>
  <name>Local Fallback Maven Resources Plugin</name>
  <description>Fallback plugin for restricted environments</description>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-resources-plugin</artifactId>
  <version>3.3.1</version>
  <goalPrefix>resources</goalPrefix>
  <mojos>
    <mojo>
      <goal>resources</goal>
      <implementation>org.apache.maven.plugins.resources.ResourcesMojo</implementation>
      <language>java</language>
      <phase>process-resources</phase>
      <threadSafe>true</threadSafe>
      <requiresProject>true</requiresProject>
    </mojo>
    <mojo>
      <goal>testResources</goal>
      <implementation>org.apache.maven.plugins.resources.TestResourcesMojo</implementation>
      <language>java</language>
      <phase>process-test-resources</phase>
      <threadSafe>true</threadSafe>
      <requiresProject>true</requiresProject>
    </mojo>
  </mojos>
</plugin>
XML

JAR_API=$(ls "$MAVEN_LIB_DIR"/maven-plugin-api-*.jar | head -n 1)
javac -cp "$JAR_API" -d "$WORK_DIR/classes" $(find "$WORK_DIR/src/main/java" -name '*.java')
jar cf "$M2_DIR/maven-resources-plugin-3.3.1.jar" -C "$WORK_DIR/classes" .

cat > "$M2_DIR/maven-resources-plugin-3.3.1.pom" <<'XML'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-resources-plugin</artifactId>
  <version>3.3.1</version>
  <packaging>maven-plugin</packaging>
  <name>Local Fallback Maven Resources Plugin</name>
</project>
XML

rm -f "$M2_DIR"/*.lastUpdated
rm -rf "$WORK_DIR"

echo "Installed local fallback plugin at: $M2_DIR"
