package fr.traqueur.commands.api.arguments;

public class ArgumentValue {

    private Class<?> type;
    private Object value;

    public ArgumentValue(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Class<?> getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

}
