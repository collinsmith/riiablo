package com.gmail.collinsmith70.cvar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.util.Validatable;
import com.gmail.collinsmith70.util.Validator;
import com.gmail.collinsmith70.util.ValidationException;
import com.google.common.base.Preconditions;

/**
 * Basic implementation of a {@link SimpleCvar} which performs {@link #isValid validations} upon
 * {@linkplain #setValue assigned} values.
 *
 * @param <T> {@linkplain Class type} of the {@linkplain #getValue variable} which this SimpleCvar
 *            represents
 */
public class ValidatableCvar<T> extends SimpleCvar<T> implements Validatable {

/**
 * {@link Validator} used to validate values of this {@link Cvar} when {@linkplain #setValue
 * setting}.
 *
 * @see #setValue(Object)
 * @see #isValid(Object)
 */
@NonNull
private final Validator VALIDATOR;

/**
 * Constructs a new {@link ValidatableCvar} instance with a {@link Validator} that accepts all
 * non-null values.
 *
 * @param alias        {@link String} representation of the {@linkplain #getAlias name}
 * @param description  Brief {@linkplain #getDescription description} explaining the function and
 *                     values it expects
 * @param type         {@link Class} instance for the {@linkplain T type} of the {@linkplain
 *                     #getValue variable}
 * @param defaultValue {@linkplain #getDefaultValue Default value} which will be assigned to the
 *                     {@link Cvar} now and whenever it is {@linkplain #reset reset}. This value
 *                     will not be validated when {@linkplain #reset resetting} the {@link Cvar}
 *
 * @see Validator#ACCEPT_NON_NULL
 */
public ValidatableCvar(@Nullable final String alias, @Nullable final String description,
                       @NonNull final Class<T> type, @Nullable final T defaultValue) {
    this(alias, description, type, defaultValue, Validator.ACCEPT_NON_NULL);
}

/**
 * Constructs a new {@link ValidatableCvar} instance.
 *
 * @param alias        {@link String} representation of the {@linkplain #getAlias name}
 * @param description  Brief {@linkplain #getDescription description} explaining the function and
 *                     values it expects
 * @param type         {@link Class} instance for the {@linkplain T type} of the {@linkplain
 *                     #getValue variable}
 * @param defaultValue {@linkplain #getDefaultValue Default value} which will be assigned to the
 *                     {@link Cvar} now and whenever it is {@linkplain #reset reset}. This value
 *                     will not be validated when {@linkplain #reset resetting} the {@link Cvar}
 * @param validator    {@link Validator} to use when performing {@linkplain #isValid validations} on
 *                     {@linkplain #setValue assignments}
 */
public ValidatableCvar(@Nullable final String alias, @Nullable final String description,
                       @NonNull final Class<T> type, @Nullable final T defaultValue,
                       @NonNull final Validator validator) {
    super(alias, description, type, defaultValue);
    Preconditions.checkArgument(validator != null, "validator is not allowed to be null");

    this.VALIDATOR = validator;
}

/**
 * {@inheritDoc}
 * <p>
 * Note: This operation will {@linkplain #isValid validate} the specified {@code value} and may
 *       throw a {@link ValidationException} even if it is the {@link #getDefaultValue
 *       default value} (i.e., in that case that {@code isValid(getDefaultValue()) == false}). If
 *       setting to the default value is required, then use {@link #reset()} instead.
 * </p>
 *
 * @throws ValidationException if the passed value is {@linkplain #isValid not valid} according to
 *                             the {@link Validator} used by this {@link Cvar}
 *
 * @see #reset()
 */
@Override
public void setValue(@Nullable final T value) {
    VALIDATOR.validate(value);
    super.setValue(value);
}

/**
 * @param obj object to validate
 *
 * @return {@code true} if the {@linkplain Validator} used by this {@link Cvar} determines that the
 *         value is {@linkplain Validatable#isValid valid}, otherwise {@code false}
 */
@Override
public boolean isValid(@Nullable final Object obj) {
    return VALIDATOR.isValid(obj);
}

/**
 * {@inheritDoc}
 * <p>
 * Note: This operation will not {@linkplain #isValid validate} the {@link #getDefaultValue default
 *       value}
 * </p>
 */
@Override
public void reset() {
    super.setValue(getDefaultValue());
}

}
