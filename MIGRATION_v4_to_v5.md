# Guide de Migration v4.x → v5.0.0

## 🚨 Breaking Changes

### 1. Migration du Repository

**JitPack supprimé** → Migration vers **repo.groupez.dev**

```xml
<!-- AVANT v4.x -->
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
<dependency>
    <groupId>com.github.Traqueur-dev.CommandsAPI</groupId>
    <artifactId>platform-spigot</artifactId>
    <version>4.x.x</version>
</dependency>

<!-- APRÈS v5.0.0 -->
<repository>
    <id>groupez-releases</id>
    <url>https://repo.groupez.dev/releases</url>
</repository>
<dependency>
    <groupId>fr.traqueur.commands</groupId>
    <artifactId>platform-spigot</artifactId>
    <version>5.0.0</version>
</dependency>
```

```groovy
// AVANT v4.x
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.Traqueur-dev.CommandsAPI:platform-spigot:4.x.x'
}

// APRÈS v5.0.0
repositories {
    maven { url 'https://repo.groupez.dev/releases' }
}
dependencies {
    implementation 'fr.traqueur.commands:platform-spigot:5.0.0'
}
```

---

## ✨ Nouvelles Fonctionnalités

### 2. ArgumentParser - Parsing Typé

Nouveau système de parsing avec gestion d'erreurs typée.

**Interface:**
```java
public interface ArgumentParser<T, S, C> {
    ParseResult parse(Command<T, S> command, C context);
}
```

**Implémentations fournies:**
- `DefaultArgumentParser<T, S>` - Pour Spigot/Velocity (String[])
- `JDAArgumentParser` - Pour JDA (SlashCommandInteractionEvent)

**Types de résultats:**
```java
public record ParseResult(Arguments arguments, ParseError error, boolean success, int consumed) {
    public static ParseResult success(Arguments args, int consumed) { ... }
    public static ParseResult error(ParseError error) { ... }
}

public record ParseError(Type type, String argument, String input, String message) {
    public enum Type {
        MISSING_REQUIRED,
        TYPE_NOT_FOUND,
        CONVERSION_FAILED,
        ARGUMENT_TOO_LONG
    }
}
```

---

### 3. CommandBuilder - API Fluent

**Nouvelle façon de créer des commandes sans héritage:**

```java
// Via le CommandManager
CommandManager<Plugin, CommandSender> manager = new CommandManager<>(platform);

manager.command("hello")
    .description("Commande hello")
    .usage("/hello <player>")
    .permission("myplugin.hello")
    .arg("player", Player.class)
    .executor((sender, args) -> {
        Player target = args.get("player");
        sender.sendMessage("Hello " + target.getName());
    })
    .register();
```

**API Complète:**
```java
CommandBuilder<T, S> builder = manager.command("name")
    .description("...")
    .usage("...")
    .permission("...")
    .gameOnly()                                    // Joueurs uniquement
    .alias("alias1")
    .aliases("alias2", "alias3")
    .arg("name", String.class)                     // Argument requis
    .arg("count", Integer.class, customCompleter)  // Avec completer
    .optionalArg("reason", String.class)           // Argument optionnel
    .requirement(new MyRequirement())              // Requirement custom
    .subcommand(subCmd)                            // Subcommand
    .executor((sender, args) -> { ... })           // Handler
    .register();                                   // Enregistre
```

**Méthodes:**
- `.build()` - Construit sans enregistrer
- `.register()` - Construit et enregistre

---

### 4. Cache pour PlayerArgument

**Optimisation automatique** pour les arguments Player (Spigot).

```java
public class PlayerArgument implements ArgumentConverter<Player>, TabCompleter<CommandSender> {
    private static final long CACHE_TTL_MS = 1000; // 1 seconde

    @Override
    public List<String> onCompletion(CommandSender sender, List<String> args) {
        // Cache automatique des noms de joueurs en ligne
        // Rafraîchi toutes les secondes
    }
}
```

**Avantages:**
- Réduit les appels à `Bukkit.getOnlinePlayers()`
- TTL de 1 seconde pour fraîcheur des données
- Thread-safe avec `volatile`

---

### 5. Optimisations de Performance

**CommandTree amélioré:**
- Validation stricte des labels (max 64 caractères par segment, max 10 niveaux)
- Recherche optimisée avec HashMap
- Gestion intelligente des subcommands

**Pattern precompile:**
```java
private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
// Utilisé pour split au lieu de String.split("\\.")
```

**Updater non-bloquant:**
```java
public static CompletableFuture<String> fetchLatestVersionAsync() {
    // Vérification async avec timeout de 5s
}
```

**Autres optimisations:**
- Cache pour les online players
- CommandInvoker simplifié
- Gestion optimisée des aliases

---

## 📝 Exemples de Migration

### Méthode Classique (Héritage)

```java
// Fonctionne toujours en v5.0.0
public class HelloCommand extends Command<MyPlugin, CommandSender> {

    public HelloCommand(MyPlugin plugin) {
        super(plugin, "hello");
        setDescription("Say hello");
        addArg("player", Player.class);
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        Player target = args.get("player");
        sender.sendMessage("Hello " + target.getName());
    }
}

// Enregistrement
manager.registerCommand(new HelloCommand(plugin));
```

### Nouvelle Méthode (Builder)

```java
// Nouveau en v5.0.0
manager.command("hello")
    .description("Say hello")
    .arg("player", Player.class)
    .executor((sender, args) -> {
        Player target = args.get("player");
        sender.sendMessage("Hello " + target.getName());
    })
    .register();
```

---

## ✅ Checklist de Migration

- [ ] Changer repository : `jitpack.io` → `repo.groupez.dev/releases`
- [ ] Changer groupId : `com.github.Traqueur-dev.CommandsAPI` → `fr.traqueur.commands`
- [ ] Version : `5.0.0`
- [ ] (Optionnel) Tester le nouveau CommandBuilder pour simplifier le code
- [ ] Rebuild et tests

---

## 🆕 Ce qui est Nouveau (non breaking)

✅ **ArgumentParser** - System de parsing extensible
✅ **CommandBuilder** - Alternative fluent à l'héritage
✅ **Cache PlayerArgument** - Autocomplétion optimisée
✅ **ParseResult/ParseError** - Gestion d'erreurs typée
✅ **Updater async** - Vérification non-bloquante
✅ **Tests** - Coverage améliorée

## 🔄 Ce qui Reste Compatible

✅ Création de commandes par héritage de `Command<T, S>`
✅ API `Arguments` (get, getOptional, has, add)
✅ `TabCompleter` interface
✅ `Requirement` system
✅ Converters customs
✅ Subcommands