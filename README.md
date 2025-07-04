# CommandsAPI

**CommandsAPI** is a modular, extensible Java library for building robust, typed command systems across multiple platforms such as **Spigot** and **Velocity**. 
As of version `4.0.0`, all core logic has been extracted into a dedicated `core` module, enabling seamless multi-platform support.

---

## ✨ Features

* ✅ **Multi-Platform Support** (Spigot, Velocity, etc.)
* ✅ **Typed Argument Parsing** with validation
* ✅ **Custom Argument Converters**
* ✅ **Subcommands & Hierarchical Command Trees**
* ✅ **Tab Completion Support**
* ✅ **Permission & Context Requirements**
* ✅ **Optional and Infinite Arguments**
* ✅ **Auto-Generated Usage Help**
* ✅ **Lightweight, Fast, and Fully Extensible**

---

## 🧱 Project Structure

```
traqueur-dev-commandsapi/
├── core/                    # Platform-agnostic command logic
├── spigot/                  # Spigot implementation
├── <platform>-test-plugin/  # The test plugin for the specified platform
└── velocity/                # Velocity implementation
```

---

## 🚀 Getting Started

### ✅ Requirements

* Java 21+
* A supported Minecraft platform (e.g., Spigot or Velocity)
* Build tool (Gradle/Maven) with JitPack

---

## 📦 Installation

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Traqueur-dev.CommandsAPI:platform-spigot:4.0.0' // or platform-velocity
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Traqueur-dev.CommandsAPI</groupId>
        <artifactId>platform-spigot</artifactId> <!-- or platform-velocity -->
        <version>4.0.0</version>
    </dependency>
</dependencies>
```

> ⚠️ **Relocate** the library when shading it into your plugin to avoid version conflicts with other plugins.

---

## 💡 Example (Spigot)

Be sure to extends all the classes from the platform you are using (Spigot, Velocity, etc.):
`fr.traqueur.commandsapi.spigot.CommandManager` for Spigot, `fr.traqueur.commandsapi.velocity.CommandManager` for Velocity, etc.

```java
public class HelloWorldCommand extends Command<MyPlugin> {

    public HelloWorldCommand(MyPlugin plugin) {
        super(plugin, "helloworld");
        setDescription("A simple hello world command");
        setUsage("/helloworld");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        sender.sendMessage("Hello, world!");
    }
}
```

Register the command:

```java
@Override
public void onEnable() {
    CommandManager<MyPlugin> manager = new CommandManager<>(/*args depending of the platform*/);
    manager.registerCommand(new HelloWorldCommand(this));
}
```

---

## 🧠 Add New Platform Support

You can create your own adapter by implementing:

```java
public interface CommandPlatform<T, S> {
    T getPlugin();
    void injectManager(CommandManager<T, S> manager);
    Logger getLogger();
    boolean hasPermission(S sender, String permission);
    void addCommand(Command<T, S> command, String label);
    void removeCommand(String label, boolean subcommand);
}
```

This allows support for new platforms like Fabric, Minestom, or BungeeCord.

---

## 🛠️ Local Development

To publish locally for development:

```bash
./gradlew core:publishToMavenLocal platform-spigot:publishToMavenLocal platform-velocity:publishToMavenLocal
```

---

## 📚 Documentation

Visit the [Wiki](https://github.com/Traqueur-dev/CommandsAPI/wiki) for:

* Tutorials
* Examples
* API Reference
* Extending with custom types and logic

---

## 🤝 Contributing

We welcome contributions!

1. Fork this repository
2. Create a new branch
3. Implement your feature or fix
4. Open a pull request with a clear description

---

## 📄 License

CommandsAPI is licensed under the [MIT License](LICENSE).

---

## 💬 Support

Need help or want to report a bug?
Open an issue on [GitHub](https://github.com/Traqueur-dev/CommandsAPI/issues)