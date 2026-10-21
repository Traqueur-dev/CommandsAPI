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

> ℹ️ The text formatter works on plain `String`, because the `core` module also serves
> platforms without Adventure, such as JDA. To keep `click` and `hover` events, use a
> component formatter (below) on Paper and Velocity.

### Messages as `Component` (Paper, Velocity)

A **component formatter** builds each message as an Adventure `Component`, which is then sent
as a component — click and hover events reach the player intact:

```java
public interface ComponentMessageFormatter<S> {
    Component format(S receiver, MessageKey key, Map<String, String> placeholders);
}
```

The message comes by its `MessageKey` (`NO_PERMISSION`, `ONLY_IN_GAME`, `ARG_NOT_RECOGNIZED`,
`REQUIREMENT`, `DISABLED`, `TOO_LONG`, `INVALID_FORMAT`, `USAGE`, `INTERNAL_ERROR`), and its values
come apart, never substituted in the text: `arg` (the input), `requirement` (the requirement's
`errorMessage()`, or its `name()` when empty) and `usage`.

```java
// MiniMessage templates from the server's configuration; values become <arg>, <requirement>, <usage>
manager.setComponentFormatter(ComponentMessageFormatter.miniMessage(
        key -> getConfig().getString("messages." + key.name().toLowerCase())));

// Or keep the texts in a MessageHandler written in MiniMessage: %arg% is read as <arg>
manager.setComponentFormatter(ComponentMessageFormatter.miniMessage(myHandler));
```

Values are inserted as plain text: an argument typed as `<red>abc` is shown as typed. A formatter
returning `null` for a key sends that message as text, through the `MessageHandler` and the text
formatter, as before. With a component formatter set, the text formatter only sees those messages.

The formatter lives in the `adventure-addon` module, pulled in by the Paper and Velocity
platforms. On another Adventure platform, install it with `ComponentMessages.install(manager, formatter)`.

---

## 🧭 Paper platform (Brigadier)

`platform-paper` registers commands through Paper's Brigadier API (`LifecycleEvents.COMMANDS`)
instead of the Bukkit command map, so the client knows the shape of each command: arguments are
coloured, a wrong value is flagged before it is sent, and suggestions come from the tab completers.

```java
public final class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        CommandManager<MyPlugin> manager = new fr.traqueur.commands.paper.CommandManager<>(this);
        manager.command("give").arg("amount", Integer.class).executor((sender, args) -> { ... }).register();
    }
}
```

| CommandsAPI argument | Brigadier node |
|---|---|
| `int`/`Integer`, `long`, `double`, `float`, `byte` (library converter) | the matching number |
| `boolean`/`Boolean` (library converter) | boolean |
| `Infinite` | greedy string, to the end of the line |
| `String`, a type with a custom converter, a number with your own converter | word, with the tab completer's suggestions |

The permission and the requirements of a command become the `requires` of its node: a player who
cannot run a command does not see it. A command is executable once its required arguments are
given; running it goes through the usual invoker, so conversion, checks and messages are unchanged.
`setComponentFormatter` is available as on Velocity.

> ℹ️ A word only accepts `0-9 A-Z a-z _ - . +`, as in vanilla: the client refuses a `String`
> argument holding other characters (accents, `<`, `&`). Declare it `Infinite` when it must accept
> any text.

### When Paper builds the tree

Paper asks for the commands when it builds its command tree: **once at startup, after the plugins
are enabled, then on every `/minecraft:reload`**. The nodes are built from what the manager holds
at that moment. Tested on Paper 1.21.4 and 26.3:

| What happens | Result |
|---|---|
| Command registered in `onEnable` | Available at startup. |
| New label registered after startup | Unknown command until the next `/minecraft:reload` (a warning is logged), then available. |
| Command unregistered after startup | Its node stays, hidden by its `requires`: unknown command, and the clients' command lists are refreshed. |
| Same label registered again | Visible again, **with the arguments the node had when it was built**; a changed shape needs a reload. |
| `unregisterAll()` | Every command of the manager becomes unknown; other plugins' commands stay. |

`platform-spigot` stays available for Spigot, or for a Paper server that prefers the command map.

---

## 🧹 Removing every command of a manager

When a plugin stops, `unregisterAll()` removes every command its manager registered, with their
aliases and subcommands:

```java
@Override
public void onDisable() {
    manager.unregisterAll();
}
```

Only the commands of **this** manager go: those of other managers, other plugins and the
server stay in place, including a label another plugin held and this manager did not get.

---

## 🚧 Requirement messages

A requirement that fails sends its `errorMessage()` as-is. When it is empty, the generic
`MessageHandler#getRequirementMessage()` is sent instead, and its `%requirement%` placeholder
is filled with `Requirement#name()` — the simple name of the requirement's class by default.
Override `name()` to show something readable:

```java
@Override
public String name() {
    return "être dans le Nether";
}
```

---

## 🔄 Update check

Creating a `CommandManager` makes **no network call**. To be told about a newer version of the
library, ask for it:

```java
Updater.checkUpdates(); // asynchronous, logs once
```

A failed check, or a version that cannot be compared (a build named after a commit), announces
nothing. A server can turn the check off for every plugin with `-Dcommands.updater=false`.

---

## 🧱 Project Structure

```
traqueur-dev-commandsapi/
├── core/                    # Platform-agnostic command logic
├── spigot/                  # Spigot implementation
├── paper/                   # Paper implementation, on Brigadier
├── adventure-addon/         # Component messages, for Adventure platforms
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