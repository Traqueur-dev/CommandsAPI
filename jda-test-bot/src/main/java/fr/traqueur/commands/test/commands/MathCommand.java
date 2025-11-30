package fr.traqueur.commands.test.commands;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.jda.Command;
import fr.traqueur.commands.jda.JDAArguments;
import fr.traqueur.commands.test.TestBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Math command with subcommands demonstrating the command tree structure.
 * Commands:
 * - /math add <a> <b>
 * - /math subtract <a> <b>
 * - /math multiply <a> <b>
 * - /math divide <a> <b>
 */
public class MathCommand extends Command<TestBot> {

    public MathCommand(TestBot bot) {
        super(bot, "math");
        this.setDescription("Perform mathematical operations");

        // Add subcommand
        this.addSubCommand(new AddCommand(bot));
        this.addSubCommand(new SubtractCommand(bot));
        this.addSubCommand(new MultiplyCommand(bot));
        this.addSubCommand(new DivideCommand(bot));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
        // This won't be called since we have subcommands
    }

    /**
     * Add subcommand: /math add <a> <b>
     */
    private static class AddCommand extends Command<TestBot> {
        public AddCommand(TestBot bot) {
            super(bot, "add");
            this.setDescription("Add two numbers");
            this.addArgs("a", Double.class, "b", Double.class);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            double a = arguments.getAsDouble("a").orElse(0.0);
            double b = arguments.getAsDouble("b").orElse(0.0);
            double result = a + b;

            jda(arguments).reply(String.format("%.2f + %.2f = %.2f", a, b, result));
        }
    }

    /**
     * Subtract subcommand: /math subtract <a> <b>
     */
    private static class SubtractCommand extends Command<TestBot> {
        public SubtractCommand(TestBot bot) {
            super(bot, "subtract");
            this.setDescription("Subtract two numbers");
            this.addArgs("a", Double.class, "b", Double.class);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            double a = arguments.getAsDouble("a").orElse(0.0);
            double b = arguments.getAsDouble("b").orElse(0.0);
            double result = a - b;

            jda(arguments).reply(String.format("%.2f - %.2f = %.2f", a, b, result));
        }
    }

    /**
     * Multiply subcommand: /math multiply <a> <b>
     */
    private static class MultiplyCommand extends Command<TestBot> {
        public MultiplyCommand(TestBot bot) {
            super(bot, "multiply");
            this.setDescription("Multiply two numbers");
            this.addArgs("a", Double.class, "b", Double.class);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            double a = arguments.getAsDouble("a").orElse(0.0);
            double b = arguments.getAsDouble("b").orElse(0.0);
            double result = a * b;

            jda(arguments).reply(String.format("%.2f × %.2f = %.2f", a, b, result));
        }
    }

    /**
     * Divide subcommand: /math divide <a> <b>
     */
    private static class DivideCommand extends Command<TestBot> {
        public DivideCommand(TestBot bot) {
            super(bot, "divide");
            this.setDescription("Divide two numbers");
            this.addArgs("a", Double.class, "b", Double.class);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            double a = arguments.getAsDouble("a").orElse(0.0);
            double b = arguments.getAsDouble("b").orElse(0.0);

            if (b == 0) {
                jda(arguments).replyEphemeral("Cannot divide by zero!");
                return;
            }

            double result = a / b;
            jda(arguments).reply(String.format("%.2f ÷ %.2f = %.2f", a, b, result));
        }
    }
}