package com.gmail.collinsmith70.command;

import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.Iterator;

public class CommandInstance implements Iterable<String> {

private final String ALIAS;
private final String[] ARGS;

public CommandInstance(String alias, String... args) {
    if (alias == null) {
        throw new IllegalArgumentException("CommandInstance alias should not be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("CommandInstance alias should not be empty");
    }

    this.ALIAS = alias;
    this.ARGS = args == null ? new String[0] : args;
}

public String getAlias() {
    return ALIAS;
}

public String getArg(int i) {
    if (ARGS.length == 0) {
        throw new UnsupportedOperationException("This CommandInstance does not have any arguments");
    } else if (i < 0 || i >= ARGS.length) {
        throw new IllegalArgumentException(String.format(
                "Invalid index specified (%d), indeces must be between 0 and %d (inclusive)",
                i,
                ARGS.length-1));
    }

    return ARGS[i];
}

public int getNumArgs() {
    return ARGS.length;
}

@Override
public Iterator<String> iterator() {
    return new ArrayIterator<String>(ARGS);
}

}
