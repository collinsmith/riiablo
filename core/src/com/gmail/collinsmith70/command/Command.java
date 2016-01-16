package com.gmail.collinsmith70.command;

import com.gmail.collinsmith70.util.AddRemoveStringListener;
import com.google.common.base.Preconditions;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.collections4.set.UnmodifiableSet;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Command<T> {

private final String PRIMARY_ALIAS;
private final String DESCRIPTION;
private final Set<String> ALIASES;
private final Parameter[] PARAMETER_RESOLVERS;
private final Class<T> TYPE;
private final Action<T> ACTION;

private final Set<AddRemoveStringListener<Command<T>>> ALIAS_LISTENERS;

public Command(String alias, String description, Class<T> type, Action<T> action, Parameter... parameters) {
    if (alias == null) {
        throw new NullPointerException("Command aliases cannot be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("Command aliases cannot be empty");
    }

    this.PRIMARY_ALIAS = alias;
    this.DESCRIPTION = description;
    this.ALIASES = new CopyOnWriteArraySet<String>();
    this.TYPE = Preconditions.checkNotNull(type, "Command type should not be null");
    this.ACTION = action == null ? Action.EMPTY_ACTION : action;
    this.PARAMETER_RESOLVERS = parameters;

    this.ALIAS_LISTENERS = new CopyOnWriteArraySet<AddRemoveStringListener<Command<T>>>();

    if (PARAMETER_RESOLVERS != null && !isValidParameterResolversOrder(PARAMETER_RESOLVERS)) {
        throw new IllegalArgumentException(
                "OptionalParameter instances must be after all normal " +
                "Parameter instances");
    }

    addAlias(alias);
}

private boolean isValidParameterResolversOrder(Parameter[] parameters) {
    for (Iterator<Parameter> it = new ArrayIterator(parameters); it.hasNext(); ) {
        if (!(it.next() instanceof OptionalParameter)) {
            continue;
        }

        while (it.hasNext()) {
            if (!(it.next() instanceof OptionalParameter)) {
                return false;
            }
        }
    }

    return true;
}

public String getAlias() { return PRIMARY_ALIAS; }
public String getDescription() { return DESCRIPTION; }
public Set<String> getAliases() { return UnmodifiableSet.unmodifiableSet(ALIASES); }
public Class<T> getType() { return TYPE; }

public Command<T> addAlias(String alias) {
    if (alias == null) {
        throw new NullPointerException("Command aliases should not be null");
    } else if (alias.isEmpty()) {
        throw new IllegalArgumentException("Command aliases should not be empty");
    }

    ALIASES.add(alias);
    for (AddRemoveStringListener<Command<T>> addRemoveStringListener : ALIAS_LISTENERS) {
        addRemoveStringListener.onAdded(alias, this);
    }

    return this;
}

public boolean containsAlias(String alias) {
    return ALIASES.contains(alias);
}

public boolean removeAlias(String alias) {
    if (alias.equals(PRIMARY_ALIAS)) {
        throw new IllegalArgumentException("Cannot remove primary alias from command!");
    }

    boolean removed = ALIASES.remove(alias);
    if (removed) {
        for (AddRemoveStringListener<Command<T>> addRemoveStringListener : ALIAS_LISTENERS) {
            addRemoveStringListener.onRemoved(alias, this);
        }
    }

    return removed;
}

public void execute(CommandInstance command) {
    execute(command, null);
}
public void execute(CommandInstance command, T obj) {
    ACTION.onActionExecuted(command, obj);
}

public void addAliasListener(AddRemoveStringListener<Command<T>> l) {
    ALIAS_LISTENERS.add(l);
    for (String alias : ALIASES) {
        l.onAdded(alias, this);
    }
}

public boolean containsAliasListener(AddRemoveStringListener<Command<T>> l) {
    return ALIAS_LISTENERS.contains(l);
}

public boolean removeAliasListener(AddRemoveStringListener<Command<T>> l) {
    return ALIAS_LISTENERS.remove(l);
}

}
