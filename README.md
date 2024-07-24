# CommandsAPI

CommandsAPI is a Java library designed to simplify the creation and management of commands in Spigot plugins. It provides a robust framework for defining commands, subcommands, and custom arguments with automatic validation and conversion.

## Features

- **Easy Command Creation**: Quickly define commands with arguments, permissions, and descriptions.
- **Subcommand Management**: Organize your commands into subcommands for better structure.
- **Argument Conversion**: Automatically convert and validate command arguments.
- **Auto-Completion**: Add auto-completion for commands and their arguments.

## Getting Started

### Adding CommandsAPI to Your Project

Ensure CommandsAPI is included in your project. If you're using Maven or Gradle, add the corresponding dependency to your configuration file.

#### For Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.Traqueur-dev:CommandsAPI:VERSION'
}
```

#### For Maven
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
By sure to relocate commandsAPI in to prevent bugs with other plugins.

### Instantiate the CommandManager

In your plugin's `onEnable` method, create a new instance of the `CommandManager` class and register your commands.

```java
public class MyPlugin extends JavaPlugin {

    private final CommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new CommandManager(this);
    }
}
```

### Creating a Command

To create a command, extend the `Command` class and implement the `execute` method. If you want simple command with no specific JavaPlugin subclass, you can use the `SimpleCommand` class instead.
Here's a simple example:

```java
public class HelloCommand extends Command<JavaPlugin> {

    public HelloCommand(JavaPlugin plugin) {
        super(plugin, "hello");
        setDescription("A greeting command");
        setUsage("/hello");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        sender.sendMessage("Hello, world!");
    }
}
```

```java
public class HelloCommand extends SimpleCommand {

    public HelloCommand(JavaPlugin plugin) {
        super(plugin, "hello");
        setDescription("A greeting command");
        setUsage("/hello");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        sender.sendMessage("Hello, world!");
    }
}
```

### Registering Subcommands

To create a subcommand, extend the `Command` or `SimpleCommand` class and implement the `execute` method. Then, add the subcommand to the parent command using the `addSubcommand` method.
The framework will automatically handle the subcommand routing for you.
Exemple: name of the subcommand is "sub" and the parent command is "hello".

```java
public class SubCommand extends Command<JavaPlugin> {

    public SubCommand(JavaPlugin plugin) {
        super(plugin, "sub");
        setDescription("A subcommand");
        setUsage("/hello sub");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        sender.sendMessage("This is a subcommand!");
    }
}
```

```java
public HelloCommand(JavaPlugin plugin) {
        /* Some code */
        addSubcommand(new SubCommand(plugin));
}
```

With this exemple you can run `/hello` or `/hello sub` and it will display the message.

If you just want `/hello sub` and not `/hello`, you can use register command like this:
    
```java
public class SubWithDotCommand extends Command<JavaPlugin> {

    public SubCommand(JavaPlugin plugin) {
        super(plugin, "hello.sub");
        setDescription("A subcommand");
        setUsage("/hello sub");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        sender.sendMessage("This is a subcommand!");
    }
}
```

The framework will automatically parse all dot on the command name and create the subcommand for you.
You must register only the parent command, the framework will automatically register the subcommands.
For our exemple:

```java
public class MyPlugin extends JavaPlugin {

    private final CommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new CommandManager(this);
        commandManager.registerCommands(new HelloCommand(this));
    }
}
```

or

```java
public class MyPlugin extends JavaPlugin {

    private final CommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new CommandManager(this);
        commandManager.registerCommands(new SubWithDotCommand(this));
    }
}
```

### Adding Arguments

Argument can be add with two methods `addArgs` and `addOptionalArgs`. The first one is for required argument and the second one is for optional argument.
Argument must be have a name and a type and the framework will automatically convert the argument to the type you want with this syntax `name:type`.

```java
public class GreetCommand extends Command<JavaPlugin> {

    public GreetCommand(JavaPlugin plugin) {
        super(plugin, "greet");
        setDescription("A greeting command");
        setUsage("/greet <name>");
        addArgs("name:string");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        String name = args.get("name");
        sender.sendMessage("Hello, " + name + "!");
    }
}
```

#### All available types:

| Type           | Identifier     | Description                                           |
|----------------|----------------|-------------------------------------------------------|
| `String`       | `string`       | Accepts any string without conversion.                |
| `Integer`      | `int`          | Converts input to an integer.                         |
| `Double`       | `double`       | Converts input to a double.                           |
| `Long`         | `long`         | Converts input to a long.                             |
| `Player`       | `player`       | Converts input to a `Player` object if the player is online. |
| `OfflinePlayer`| `offlineplayer`| Converts input to an `OfflinePlayer` object, allowing access to players not currently online. |
| `String`       | `infinite`     | Accepts an infinite amount of strings as a single argument. |

#### Custom Argument Types

You can create custom argument types by implements the `ArgumentConverter` class and implementing the `apply`methods. Then, register the custom argument type with the `CommandManager` instance.

```java
public class CustomArgument implements ArgumentConverter<CustomType> {

    @Override
    public CustomType apply(String input) {
        return /*something custom*/;
    }
}
```

```java
public class MyPlugin extends JavaPlugin {

    private final CommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new CommandManager(this);
        commandManager.registerArgumentType(CustomType.class, "custom", new CustomArgument());
    }
}
```

### Auto-Completion

You can add auto-completion for commands and their arguments by implementing the `TabConverter` interface on custom `ArgumentConverter` and implements `onCompletion` method.
You can also add auto-completion for specific arguments by using the methods `add(Optional)Args(String name, TabConverter converter)` when you register yours args.

```java
public class CustomArgument implements ArgumentConverter<CustomType>, TabConverter {

    @Override
    public CustomType apply(String input) {
        return /*something custom*/;
    }

    @Override
    public List<String> onCompletion(CommandSender sender, String input) {
        return /*list of completion*/;
    }
}
```

```java
public class GreetCommand extends Command<JavaPlugin> {

    public GreetCommand(JavaPlugin plugin) {
        super(plugin, "greet");
        setDescription("A greeting command");
        setUsage("/greet <name>");
        addArgs("name:custom");
        addArgs("number:int", () -> Arrays.asList("1", "2", "3"));
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        CustomType name = args.get("name");
        int number = args.get("number");
        for (int i = 0; i < number; i++) {
            sender.sendMessage("Hello, " + name + "!");
        }
    }
}
```