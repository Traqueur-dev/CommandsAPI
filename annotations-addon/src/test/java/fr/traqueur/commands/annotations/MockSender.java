package fr.traqueur.commands.annotations;

public interface MockSender {
    void sendMessage(String message);
    boolean hasPermission(String permission);
}