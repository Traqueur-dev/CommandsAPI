# Changelog

Every minor version ships one batch of changes. Nothing changes for code that does not use the
new APIs, except where a section says otherwise.

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
