// src/test/java/fr/traqueur/commands/api/logging/InternalMessageHandlerTest.java
package fr.traqueur.commands.api.logging;

import fr.traqueur.commands.impl.logging.InternalMessageHandler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InternalMessageHandlerTest {

    private final MessageHandler handler = new InternalMessageHandler();

    @Test
    void testGetNoPermissionMessage() {
        assertEquals(
                "&cYou do not have permission to use this command.",
                handler.getNoPermissionMessage()
        );
    }

    @Test
    void testGetOnlyInGameMessage() {
        assertEquals(
                "&cYou can only use this command in-game.",
                handler.getOnlyInGameMessage()
        );
    }

    @Test
    void testGetArgNotRecognized() {
        assertEquals(
                "&cArgument &e%arg% &cnot recognized.",
                handler.getArgNotRecognized()
        );
    }

    @Test
    void testGetRequirementMessage() {
        assertEquals(
                "The requirement %requirement% was not met",
                handler.getRequirementMessage()
        );
    }
}
