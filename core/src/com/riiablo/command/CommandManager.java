package com.riiablo.command;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.SortedMap;

public class CommandManager implements Command.AssignmentListener {
  private final Trie<String, Command> COMMANDS = new PatriciaTrie<>();

  public Collection<Command> getCommands() {
    return ImmutableSet.copyOf(COMMANDS.values());
  }

  public OrderedMapIterator<String, Command> mapIterator() {
    return COMMANDS.mapIterator();
  }

  public boolean add(Command command) {
    final Command queriedCommand = COMMANDS.get(command.ALIAS);
    if (Objects.equal(command, queriedCommand)) {
      return false;
    } else if (queriedCommand != null) {
      throw new IllegalArgumentException(String.format(
          "A command with the alias %s is already being managed by this command manager", command.getAlias()));
    }

    if (command.aliases != null) {
      for (String alias : command.aliases) {
        if (COMMANDS.containsKey(alias)) {
          throw new DuplicateCommandException(command,
              "A command with the alias %s is already registered. Aliases must be unique!", alias);
        }
      }
    }

    return command.addAssignmentListener(this);
  }

  @Override
  public void onAssigned(Command command, String alias) {
    COMMANDS.put(alias, command);
  }

  @Override
  public void onUnassigned(Command command, String alias) {
    unassign(alias);
  }

  private boolean unassign(String alias) {
    return COMMANDS.remove(alias) != null;
  }

  private boolean unassign(Command command, String alias) {
    Command queriedCommand = COMMANDS.get(alias);
    if (Objects.equal(queriedCommand, command)) {
      COMMANDS.remove(alias);
    }

    return true;
  }

  public boolean remove(@Nullable Command command) {
    if (command == null) return false;
    boolean unassigned = false;
    for (String alias : command.aliases) unassigned = unassigned || unassign(command, alias);
    return unassigned;
  }

  @Nullable
  public Command get(@Nullable String alias) {
    return COMMANDS.get(alias);
  }

  public SortedMap<String, Command> prefixMap(String alias) {
    return COMMANDS.prefixMap(alias);
  }

  public boolean isManaging(@Nullable String alias) {
    return alias != null && COMMANDS.containsKey(alias);
  }

  public boolean isManaging(@Nullable Command command) {
    if (command == null) return false;
    Command value = COMMANDS.get(command.getAlias());
    return command.equals(value);
  }

  protected final void checkIfManaged(@NonNull Command command) {
    if (!isManaging(command)) {
      throw new UnmanagedCommandException(command,
          "Command %s is not managed by this command manager", command.getAlias());
    }
  }

  public static abstract class CommandManagerException extends RuntimeException {
    public final Command COMMAND;

    CommandManagerException() {
      this(null, null);
    }

    CommandManagerException(@Nullable Command command) {
      this(command, null);
    }

    CommandManagerException(@Nullable String message) {
      this(null, message);
    }

    CommandManagerException(@Nullable Command command, @Nullable String message) {
      super(message);
      this.COMMAND = command;
    }

    public Command getCommand() {
      return COMMAND;
    }
  }

  public static class DuplicateCommandException extends CommandManagerException {
    DuplicateCommandException(@Nullable Command command, @Nullable String format, @Nullable Object... args) {
      super(command, String.format(format, args));
    }
  }

  @SuppressWarnings("unused")
  public static class UnmanagedCommandException extends CommandManagerException {
    UnmanagedCommandException(@Nullable Command command, @Nullable String format, @Nullable Object... args) {
      super(command, String.format(format, args));
    }
  }
}
