# CommandsAPI

**CommandsAPI** is a modular, extensible Java library for building robust, typed command systems across multiple
platforms such as **Spigot** and **Velocity**.
As of version `4.0.0`, all core logic has been extracted into a dedicated `core` module, enabling seamless
multi-platform support.

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
* ✅ **Overriding of Existing Labels** (vanilla or another plugin)
* ✅ **Message Formatter** (prefix, MiniMessage, placeholders, per-sender translation)
* ✅ **Lightweight, Fast, and Fully Extensible**

---

## 🔁 Overriding an existing command

A label already registered on the platform — a vanilla command such as `/gamemode`, `/tp`, `/ban`,
or one owned by another plugin — is **left alone by default**: the command is not bound and the
existing one keeps answering. This is now logged as a warning, instead of failing silently.

To take the label over, mark the command as an override:

```java
@Command(name = "gamemode", permission = "admin.gamemode", override = true)
public void gamemode(Player sender, @Arg("mode") GameMode mode) { ... }

// or, with the builder
manager.command("gamemode").override().executor(...).register();
```

The previous command stays reachable through its namespace (`/minecraft:gamemode`,
`/otherplugin:home`) — only the plain label changes hands.

---

## 💬 Formatting command messages

The messages the library sends — no permission, only in-game, command disabled, unmet
requirement, usage, unrecognised argument — come from the `MessageHandler`. A **message
formatter** is the last step before one of them leaves, and unlike the handler it knows
**who** the message is being sent to:

```java
public interface MessageFormatter<S> {
    String format(S sender, String raw);
}
```

Register one on the manager:

```java
// A plugin prefix, without repeating it in every message of the handler
manager.setMessageFormatter((sender, raw) -> "&8[&bMyPlugin&8] " + raw);

// MiniMessage, serialized back to the legacy format
manager.setMessageFormatter((sender, raw) -> LegacyComponentSerializer.legacySection()
        .serialize(MiniMessage.miniMessage().deserialize(raw)));

// External placeholders, resolved for the sender the message is about to reach
manager.setMessageFormatter((sender, raw) -> sender instanceof Player player
        ? PlaceholderAPI.setPlaceholders(player, raw)
        : raw);
```

The formatter is called on every sending path, including the direct interaction replies of
the JDA platform, and it receives the message **after** the internal placeholders (`%arg%`,
`%requirement%`) have been substituted, so it always sees the final text. Setting it back to
`null` restores the default behaviour: with no formatter registered, messages are sent
exactly as the handler produced them.

> ⚠️ Messages are plain `String` in and out, because the `core` module also serves platforms
> without Adventure, such as JDA. A consumer working with `Component` has to serialize it,
> usually to the legacy `§` format, which drops `hover` and `click` events — command messages
> such as "you do not have permission" do not need them, and honouring them would mean
> pulling Adventure into `core`.

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
    maven { url 'https://repo.groupez.dev/<repository>' } // snapshots or releases
}

dependencies {
    implementation 'fr.traqueur.commands:platform-spigot:[version]' // or platform-velocity or platform-<your-platform>
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>groupez-releases</id>
        <url>https://repo.groupez.dev/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>fr.traqueur.commands</groupId>
        <artifactId>platform-spigot</artifactId> <!-- or platform-velocity or platform-<your-platform> -->
        <version>[version]</version>
    </dependency>
</dependencies>
```

> ⚠️ **Relocate** the library when shading it into your plugin to avoid version conflicts with other plugins.

---

## 🏷️ Annotations Addon

The `annotations-addon` module provides annotation-based command registration. When using `@Arg` implicitly (without annotation), parameter names are used as argument names. This requires the `-parameters` compiler flag.

### Maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <parameters>true</parameters>
    </configuration>
</plugin>
```

### Gradle (Groovy)

```groovy
tasks.withType(JavaCompile).configureEach {
    options.compilerArgs.add('-parameters')
}
```

### Gradle (Kotlin DSL)

```kotlin
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
```

> 💡 Without this flag, parameter names default to `arg0`, `arg1`, etc. You can always use `@Arg("name")` explicitly to avoid this requirement.

---

## 💡 Example (Spigot)

Be sure to extends all the classes from the platform you are using (Spigot, Velocity, etc.):
`fr.traqueur.commandsapi.spigot.CommandManager` for Spigot, `fr.traqueur.commandsapi.velocity.CommandManager` for
Velocity, etc.

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
./gradlew core:publishToMavenLocal spigot:publishToMavenLocal velocity:publishToMavenLocal
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