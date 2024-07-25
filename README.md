# CommandsAPI

CommandsAPI is a powerful and flexible Java library for creating and managing commands in Bukkit/Spigot plugins. It provides a robust framework for handling command arguments, permissions, subcommands, and auto-completion, making it easier to build complex command structures in your Minecraft plugins.

## Features

- **Customizable Commands:** Easily create commands with custom arguments, descriptions, usage, and permissions.
- **Subcommands Support:** Organize your commands with subcommands and handle complex command structures effortlessly.
- **Argument Handling:** Support for various argument types including custom types and auto-completion for a better user experience.
- **Permissions and Aliases:** Define permissions and aliases for your commands to control access and provide alternative command names.
- **In-Game/Console Command Support:** Specify whether a command can only be executed in-game or from the console.

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Paper/Spigot API** - Compatible with various Minecraft server versions
- **CommandsAPI Library** - Add it to your project dependencies

### Installation

To use CommandsAPI in your project, add the dependency to your build configuration. For Maven, include:

#### For Gradle include 
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.Traqueur-dev:CommandsAPI:VERSION'
}
```

#### For Maven include
```xml 
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Traqueur-dev</groupId>
        <artifactId>CommandsAPI</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```
Be sure to relocate commandsAPI in to prevent bugs with other plugins.

### Basic Usage

To get started with CommandsAPI, create a new command by extending the `Command<T extends JavaPlugin>` class. Here’s a simple example:

```java
public class HelloWorldCommand extends SimpleCommand {

    public HelloWorldCommand(JavaPlugin plugin) {
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

Register the command in your plugin's `onEnable` method:

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommands(new HelloWorldCommand(this));
    }
}
```

### Documentation

For detailed documentation and usage examples, visit me [Wiki](https://github.com/Traqueur-dev/CommandsAPI/wiki).

## Contributing

We welcome contributions to the CommandsAPI project! If you would like to contribute, please follow these steps:

1. Fork the repository.
2. Create a new branch for your changes.
3. Commit your changes with clear and concise commit messages.
4. Push your changes to your fork.
5. Create a pull request with a description of your changes.

## License

CommandsAPI is licensed under the [MIT License](LICENSE). See the LICENSE file for more details.

## Contact

For any questions or support, please open an issue on the [GitHub repository](https://github.com/Traqueur-dev/CommandsAPI/issues).

---

Happy coding and enjoy building your Minecraft plugins with CommandsAPI!