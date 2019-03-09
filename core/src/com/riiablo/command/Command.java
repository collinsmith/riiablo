package com.riiablo.command;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.riiablo.serializer.SerializeException;
import com.riiablo.validator.ValidationException;
import com.riiablo.validator.Validator;

import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Command implements Validator {

  private static final String[]    EMPTY_ARGS   = new String[0];
  private static final Parameter[] EMPTY_PARAMS = new Parameter[0];

  private final Set<AssignmentListener> ASSIGNMENT_LISTENERS = new CopyOnWriteArraySet<>();

  final String      ALIAS;
  final String      DESCRIPTION;
  final Parameter[] PARAMS;
  final Action      ACTION;

  private final int MINIMUM_ARGS;

  Set<String> aliases;

  Command(Builder builder) {
    Preconditions.checkArgument(builder.alias != null, "Commands must have at least one alias");
    ALIAS        = builder.alias;
    DESCRIPTION  = Strings.nullToEmpty(builder.description);
    PARAMS       = MoreObjects.firstNonNull(builder.params, EMPTY_PARAMS);
    ACTION       = MoreObjects.firstNonNull(builder.action, Action.DO_NOTHING);
    MINIMUM_ARGS = PARAMS == EMPTY_PARAMS ? 0 : calculateMinimumArgs(PARAMS);
    aliases      = builder.aliases;
  }

  private int calculateMinimumArgs(Parameter[] params) {
    int minimumParams = 0;
    boolean forceOptional = false;
    for (Parameter param : params) {
      if (!(param instanceof OptionalParameter)) {
        minimumParams++;
      } else if (forceOptional) {
        throw new IllegalArgumentException("no required parameters may appear after the first optional parameter");
      } else {
        forceOptional = true;
      }
    }

    return minimumParams;
  }

  public String getAlias() {
    return ALIAS;
  }

  public String getDescription() {
    return DESCRIPTION;
  }

  public Set<String> getAliases() {
    if (aliases == null) {
      return Collections.emptySet();
    }

    return ImmutableSet.<String>builder().add(ALIAS).addAll(aliases).build();
  }

  public Parameter getParam(int index) {
    return PARAMS[index];
  }

  public boolean hasParam(int index) {
    return 0 <= index && index < PARAMS.length;
  }

  public int numArgs() {
    return PARAMS.length;
  }

  public int minArgs() {
    return MINIMUM_ARGS;
  }

  private String getParametersHint() {
    StringBuilder sb = new StringBuilder();
    for (Parameter param : PARAMS) {
      sb.append(param);
      sb.append(' ');
    }

    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
  }

  @NonNull
  @Override
  public String toString() {
    if (MINIMUM_ARGS == 0) {
      return ALIAS;
    }

    return ALIAS + " " + getParametersHint();
  }

  public Command addAlias(String alias) {
    Preconditions.checkArgument(!alias.isEmpty(), "alias cannot be empty");
    if (aliases == null) aliases = new CopyOnWriteArraySet<>();
    aliases.add(alias);
    for (AssignmentListener l : ASSIGNMENT_LISTENERS) l.onAssigned(this, alias);
    return this;
  }

  public boolean removeAlias(@NonNull String alias) {
    if (alias == null) return false;
    Preconditions.checkArgument(!alias.equals(ALIAS), "The primary alias cannot be removed");
    boolean unassigned = aliases.remove(alias);
    if (unassigned) {
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) l.onUnassigned(this, alias);
    }

    return unassigned;
  }

  public boolean addAssignmentListener(AssignmentListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    boolean added = ASSIGNMENT_LISTENERS.add(l);
    if (added) {
      l.onAssigned(this, ALIAS);
      if (aliases != null) {
        for (String alias : aliases) l.onAssigned(this, alias);
      }
    }

    return added;
  }

  public boolean containsAssignmentListener(@Nullable Object l) {
    return l != null && ASSIGNMENT_LISTENERS.contains(l);
  }

  public boolean removeAssignmentListener(@Nullable Object l) {
    return l != null && ASSIGNMENT_LISTENERS.remove(l);
  }

  @Override
  public boolean isValid(@Nullable Object obj) {
    try {
      validate(obj);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void validate(@Nullable Object obj) {
    if (obj == null) {
      throw new ValidationException("obj cannot be null");
    } else if (!(obj instanceof Instance)) {
      throw new ValidationException("obj is not a subclass of Command.Instance");
    }

    Instance instance = (Instance) obj;
    if (instance.numArgs() < MINIMUM_ARGS) {
      throw new ValidationException("Bad syntax, expected: " + this);
    }

    int numArgs = Math.min(instance.numArgs(), PARAMS.length);
    for (int i = 0; i < numArgs; i++) {
      Parameter param = PARAMS[i];
      if (param.canValidate()) {
        param.validate(instance.getArg(i));
      }
    }
  }

  public boolean isAlias(@Nullable String alias) {
    return Objects.equal(ALIAS, alias) ||
        (alias != null && aliases != null && aliases.contains(alias));
  }

  public interface AssignmentListener {
    void onAssigned(Command command, String alias);
    void onUnassigned(Command command, String alias);
  }

  public Instance newInstance(String alias) {
    return new Instance(alias);
  }

  public Instance newInstance(String alias, @Nullable String... args) {
    return new Instance(alias, args);
  }

  public Instance newInstance(String[] args) {
    return new Instance(args);
  }

  public class Instance implements Iterable<String> {
    private final boolean  compressed;
    private final String   ALIAS;
    private final String[] ARGS;

    Instance(String alias) {
      this(alias, EMPTY_ARGS);
    }

    Instance(String alias, @Nullable String... args) {
      Preconditions.checkArgument(!alias.isEmpty(), "alias cannot be empty");
      this.ALIAS = alias;
      this.ARGS = MoreObjects.firstNonNull(args, EMPTY_ARGS);
      this.compressed = false;
    }

    Instance(String[] args) {
      Preconditions.checkArgument(args.length >= 1,
          "args should at least contain the alias of the command instance as index 0");
      Preconditions.checkArgument(!args[0].isEmpty(), "alias cannot be empty");
      this.ALIAS = args[0];
      this.ARGS = args;
      this.compressed = true;
    }

    public String getAlias() {
      return ALIAS;
    }

    public String getArg(int i) {
      return ARGS[compressed ? i + 1 : i];
    }

    @SuppressWarnings("unchecked")
    public <T> T deserializeArg(int i) {
      try {
        String arg = getArg(i);
        return (T) PARAMS[i].deserialize(arg);
      } catch (Throwable t) {
        Throwables.propagateIfPossible(t, SerializeException.class);
        throw new SerializeException(t);
      }
    }

    public int numArgs() {
      return compressed ? ARGS.length - 1 : ARGS.length;
    }

    @NonNull
    @Override
    public Iterator<String> iterator() {
      return new ArrayIterator<>(ARGS, compressed ? 1 : 0);
    }

    public void execute() {
      validate(this);
      ACTION.onExecuted(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    @Nullable
    String      alias;
    @Nullable
    String      description;
    @Nullable
    Set<String> aliases;
    @Nullable
    Parameter[] params;
    @Nullable
    Action      action;

    Builder() {}

    public Builder alias(String alias) {
      Preconditions.checkArgument(!alias.isEmpty(), "alias cannot be empty");
      if (this.alias == null) {
        this.alias = alias;
      } else {
        if (aliases == null) aliases = new CopyOnWriteArraySet<>();
        aliases.add(alias);
      }

      return this;
    }

    public Builder description(@NonNull String description) {
      Preconditions.checkArgument(description != null, "description cannot be null");
      this.description = description;
      return this;
    }

    public Builder params(@NonNull Parameter... params) {
      Preconditions.checkArgument(params != null, "params cannot be null");
      this.params = params;
      return this;
    }

    public Builder action(@NonNull Action action) {
      Preconditions.checkArgument(action != null, "action cannot be null");
      this.action = action;
      return this;
    }

    public Command build() {
      return new Command(this);
    }
  }

}
