package fr.traqueur.commands.impl.logging;

import fr.traqueur.commands.api.logging.MessageHandler;
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

    // --- v5.0.0 new tests ---

    @Test
    void testGetCommandDisabledMessage() {
        assertEquals(
                "&cThis command is currently disabled.",
                handler.getCommandDisabledMessage()
        );
    }

    @Test
    void testGetCommandDisabledMessage_notNull() {
        assertNotNull(handler.getCommandDisabledMessage());
    }

    @Test
    void testGetCommandDisabledMessage_notEmpty() {
        assertFalse(handler.getCommandDisabledMessage().isEmpty());
    }
}