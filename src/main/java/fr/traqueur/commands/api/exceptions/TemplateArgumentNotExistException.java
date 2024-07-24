package fr.traqueur.commands.api.exceptions;

/**
 * Cette exception est levée lorsqu'un argument requis n'est pas trouvé dans les arguments fournis.
 */
public class TemplateArgumentNotExistException extends Exception {

    /**
     * Constructeur de la classe TemplateArgumentNotExistException.
     */
    public TemplateArgumentNotExistException() {
        super("Required argument not found");
    }
}