package com.gmail.collinsmith70.command;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.collections4.set.UnmodifiableSet;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Command<T> {

private final String PRIMARY_ALIAS;
private final Set<String> ALIASES;
private final ParameterResolver[] PARAMETER_RESOLVERS;
private final Action<T> ACTION;

public Command(String alias, Action action, ParameterResolver... parameterResolvers) {
    this.PRIMARY_ALIAS = alias;
    this.ALIASES = new CopyOnWriteArraySet<String>();
    this.ACTION = action == null ? Action.EMPTY_ACTION : action;
    this.PARAMETER_RESOLVERS = parameterResolvers;

    if (PARAMETER_RESOLVERS != null && !isValidParameterResolversOrder(PARAMETER_RESOLVERS)) {
        throw new IllegalArgumentException(
                "OptionalParameterResolver instances must be after all normal " +
                "ParameterResolver instances");
    }

    addAlias(alias);
}

private boolean isValidParameterResolversOrder(ParameterResolver[] parameterResolvers) {
    for (Iterator<ParameterResolver> it = new ArrayIterator(parameterResolvers); it.hasNext(); ) {
        if (!(it.next() instanceof OptionalParameterResolver)) {
            continue;
        }

        while (it.hasNext()) {
            if (!(it.next() instanceof OptionalParameterResolver)) {
                return false;
            }
        }
    }

    return true;
}

public Command addAlias(String alias) {
    if (alias == null) {
        throw new NullPointerException("Command aliases should not be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("Command aliases should not be empty");
    }

    ALIASES.add(alias);
    return this;
}

public Set<String> getAliases() {
    return UnmodifiableSet.unmodifiableSet(ALIASES);
}

public String getAlias() {
    return PRIMARY_ALIAS;
}

public boolean isAlias(String alias) {
    return ALIASES.contains(alias);
}

public void execute(CommandInstance command) {
    execute(command, null);
}
public void execute(CommandInstance command, T obj) {
    ACTION.onActionExecuted(command, obj);
}

}
