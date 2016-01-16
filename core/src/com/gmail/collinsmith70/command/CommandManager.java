package com.gmail.collinsmith70.command;

import com.gmail.collinsmith70.util.AddRemoveStringListener;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.SortedMap;

public class CommandManager {

private final Trie<String, Command<?>> COMMANDS;

public CommandManager() {
    this.COMMANDS = new PatriciaTrie<Command<?>>();
}

public <T> Command<T> create(String alias, String description, Class<T> type, Action<T> action, Parameter... parameters) {
    Command<T> command = new Command<T>(alias, description, type, action, parameters);
    return add(command);
}

public <T> Command<T> add(Command<T> command) {
    if (isManaging(command)) {
        return command;
    } else {
        for (String alias : command.getAliases()) {
            if (!containsAlias(command.getAlias())) {
                continue;
            }

            throw new DuplicateCommandException(command, String.format(
                    "A command with the alias %s is already registered. Cvar aliases must be unique!",
                    command.getAlias()));
        }
    }

    command.addAliasListener(new AddRemoveStringListener<Command<T>>() {
        @Override
        public void onAdded(String alias, Command<T> command) {
            COMMANDS.put(alias.toLowerCase(), command);
        }

        @Override
        public void onRemoved(String alias, Command<T> command) {
            COMMANDS.remove(alias.toLowerCase());
        }
    });

    return command;
}

public <T> boolean remove(Command<T> command) {
    if (!isManaging(command)) {
        return false;
    }

    boolean removed = false;
    for (String alias : command.getAliases()) {
        if (!removed) {
            removed = COMMANDS.remove(alias.toLowerCase()) != null;
            continue;
        }

        COMMANDS.remove(alias.toLowerCase());
    }

    return removed;
}

public Command<?> get(String alias) {
    alias = alias.toLowerCase();
    return COMMANDS.get(alias);
}

public <T> Command<T> get(String alias, Class<T> type) {
    alias = alias.toLowerCase();
    Command<?> command = get(alias);
    if (!command.getType().isAssignableFrom(type)) {
        throw new IllegalArgumentException(String.format(
                "type should match command's type (%s)", command.getType().getName()));
    }

    return (Command<T>)command;
}

public SortedMap<String, Command<?>> search(String alias) {
    alias = alias.toLowerCase();
    return COMMANDS.prefixMap(alias);
}

public Collection<Command<?>> getCvars() {
    return COMMANDS.values();
}

private <T> void checkIfManaged(Command<T> command) throws UnmanagedCommandException {
    if (isManaging(command)) {
        return;
    }

    throw new UnmanagedCommandException(command, String.format(
            "Command %s is not managed by this %s",
            command.getAlias(),
            getClass().getSimpleName()));
}

public <T> boolean isManaging(Command<T> command) {
    Command value = COMMANDS.get(command.getAlias().toLowerCase());
    return command.equals(value);
}

public boolean containsAlias(String alias) {
    return COMMANDS.containsKey(alias.toLowerCase());
}

public static abstract class CommandException extends RuntimeException {

    public final Command COMMAND;

    private CommandException() {
        this(null, null);
    }

    private CommandException(Command command) {
        this(command, null);
    }

    private CommandException(String message) {
        this(null, message);
    }

    private CommandException(Command command, String message) {
        super(message);
        this.COMMAND = command;
    }

    private Command getCvar() {
        return COMMAND;
    }

}

public static class DuplicateCommandException extends CommandException {

    private DuplicateCommandException() {
        this(null, null);
    }

    private DuplicateCommandException(Command command) {
        this(command, null);
    }

    private DuplicateCommandException(String message) {
        this(null, message);
    }

    private DuplicateCommandException(Command command, String message) {
        super(command, message);
    }

}

public static class UnmanagedCommandException extends CommandException {

    private UnmanagedCommandException() {
        this(null, null);
    }

    private UnmanagedCommandException(Command command) {
        this(command, null);
    }

    private UnmanagedCommandException(String message) {
        this(null, message);
    }

    private UnmanagedCommandException(Command command, String message) {
        super(command, message);
    }

}

}
