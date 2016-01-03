package com.gmail.collinsmith70.cvar;

/**
 * Interface for representing a {@link ValueValidator} which specifically validates that values lie
 * within some arbitrary range.
 *
 * @param <T> type of the object to be validated
 */
public interface RangeValueValidator<T extends Number> extends ValueValidator<T> {

/**
 * Returns the minimum {@link Number} of the accepted range
 *
 * @return minimum {@link Number} of the accepted range
 */
T getMin();

/**
 * Returns the maximum {@link Number} of the accepted range
 *
 * @return maximum {@link Number} of the accepted range
 */
T getMax();

/**
 * Sets the boundedness of this {@link RangeValueValidator}.
 *
 * @param r boundedness of this {@link RangeValueValidator}
 */
void setBoundedness(Boundedness r);

/**
 * Retrieves the boundedness of this {@link RangeValueValidator}
 *
 * @return boundedness of this {@link RangeValueValidator}
 */
Boundedness getBoundedness();

/**
 * A Boundedness is a constant used to represent the boundedness of a {@link RangeValueValidator}.
 */
public static enum Boundedness {
    /**
     * Soft bounded {@linkplain Boundedness}es are ranges which will adjust a value which is outside
     * of the range to be within that range instead of outright rejecting it, i.e., a value is
     * attempted to be validated which is higher than the max of the range, then it will be adjusted
     * to be the max value of the range instead.
     */
    SOFT,

    /**
     * Hard bounded {@linkplain Boundedness}es are ranges which will reject a value if it is outside
     * of the specified range and will not adjust it accordingly.
     */
    HARD
}

}
