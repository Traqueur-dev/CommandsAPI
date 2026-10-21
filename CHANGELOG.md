# Changelog

Every minor version ships one batch of changes. Nothing changes for code that does not use the
new APIs, except where a section says otherwise.

## 5.6.0

### Added

- **`platform-paper`**: registers commands through Paper's Brigadier API (`LifecycleEvents.COMMANDS`).
  Typed arguments become Brigadier nodes (numbers, boolean, word, greedy string; a type with a
  custom converter becomes a word with its tab completer's suggestions), permission and
  requirements become the node's `requires`, and messages can be components
  (`setComponentFormatter`). Compiled against the Paper API 1.21.4 (Java 21), tested on Paper
  1.21.4 and 26.3. See the README for what happens to commands added or removed after startup.
- `BrigadierCommandTree`, the platform-neutral translation of a manager's commands into Brigadier
  nodes.
- `CommandManager#getConverter(Class)`.
- `paper-test-plugin`, a test plugin for the Paper platform.

### Fixed

- `ComponentMessageFormatter.miniMessage(...)`: the `<arg>`, `<requirement>` and `<usage>` tags are
  known for every key, empty when the key has no such value, instead of being shown as typed.

## 5.5.0

### Added

- **Messages as `Component`** on Paper and Velocity: `manager.setComponentFormatter(...)` with a
  `ComponentMessageFormatter<S>`, which receives the `MessageKey` and the values (`arg`,
  `requirement`, `usage`) apart, and whose component is sent as a component, click and hover
  events included. `ComponentMessageFormatter.miniMessage(...)` reads MiniMessage templates, from a
  function or from a `MessageHandler`.
- New `adventure-addon` module holding it (`ComponentMessages.install` for other Adventure
  platforms); the Velocity platform depends on it.
- Core: `MessageKey`, `MessageDispatcher` and `CommandManager#setMessageDispatcher` /
  `#sendMessage(receiver, key, text, values)`, the platform-neutral hook the component formatter
  uses. `MessageHandler#getMessage(MessageKey)` gives the text of a key.

Without a component formatter or a dispatcher, messages are sent exactly as in 5.4.0.

## 5.4.0

### Changed

- **No network call when a manager is created.** The `CommandManager` constructor no longer calls
  `Updater.checkUpdates()`; call it yourself to check for a newer version. Visible change: the
  "CommandsAPI is up to date" line is no longer logged at startup.
- `-Dcommands.updater=false` turns the update check off, even when a plugin calls it.
- The update check compares versions as versions: a local version newer than the published one
  (a snapshot, a build named after a commit) is no longer announced as outdated, and a failed
  check announces nothing.

### Added

- `CommandManager#unregisterAll()` removes every command the manager registered, with their
  aliases and subcommands, for the shutdown of a plugin. Commands of other managers stay.
- `Requirement#name()` fills `%requirement%` in the generic requirement message, instead of the
  class name. It defaults to the simple class name, so existing messages read the same. A
  non-empty `errorMessage()` is still sent as-is.

### Fixed

- Spigot: unregistering a command now removes it from the command map's known commands, under
  its label, aliases and namespaced forms. It used to be marked as unregistered but kept
  answering.
- Spigot and Velocity: unregistering a label that another plugin holds (because this manager did
  not get it) no longer removes that plugin's command.
