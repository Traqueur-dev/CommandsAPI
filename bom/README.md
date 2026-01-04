# CommandsAPI BOM (Bill of Materials)

This module provides a BOM (Bill of Materials) for CommandsAPI, making it easier to manage consistent versions across all CommandsAPI modules.

## Usage

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    // Import the BOM
    implementation(platform("fr.traqueur.commands:bom:VERSION"))

    // Then add dependencies without specifying versions
    implementation("fr.traqueur.commands:core")
    implementation("fr.traqueur.commands:platform-spigot")
    implementation("fr.traqueur.commands:platform-velocity")
    implementation("fr.traqueur.commands:platform-jda")
    implementation("fr.traqueur.commands:annotations-addon")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    // Import the BOM
    implementation platform('fr.traqueur.commands:bom:VERSION')

    // Then add dependencies without specifying versions
    implementation 'fr.traqueur.commands:core'
    implementation 'fr.traqueur.commands:platform-spigot'
    implementation 'fr.traqueur.commands:platform-velocity'
    implementation 'fr.traqueur.commands:platform-jda'
    implementation 'fr.traqueur.commands:annotations-addon'
}
```

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>fr.traqueur.commands</groupId>
            <artifactId>bom</artifactId>
            <version>VERSION</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Then add dependencies without specifying versions -->
    <dependency>
        <groupId>fr.traqueur.commands</groupId>
        <artifactId>core</artifactId>
    </dependency>
    <dependency>
        <groupId>fr.traqueur.commands</groupId>
        <artifactId>platform-spigot</artifactId>
    </dependency>
    <dependency>
        <groupId>fr.traqueur.commands</groupId>
        <artifactId>platform-velocity</artifactId>
    </dependency>
    <dependency>
        <groupId>fr.traqueur.commands</groupId>
        <artifactId>platform-jda</artifactId>
    </dependency>
    <dependency>
        <groupId>fr.traqueur.commands</groupId>
        <artifactId>annotations-addon</artifactId>
    </dependency>
</dependencies>
```

## Benefits

Using the BOM provides several advantages:

1. **Version Consistency**: All CommandsAPI modules will use compatible versions
2. **Simplified Dependency Management**: No need to specify versions for each module
3. **Easier Updates**: Update all modules by changing only the BOM version
4. **Reduced Conflicts**: Ensures all modules work together correctly

## Available Modules

The BOM manages versions for the following modules:

- `core` - Core functionality and API
- `platform-spigot` - Spigot/Bukkit platform support
- `platform-velocity` - Velocity proxy platform support
- `platform-jda` - JDA (Discord) platform support
- `annotations-addon` - Annotation-based command registration