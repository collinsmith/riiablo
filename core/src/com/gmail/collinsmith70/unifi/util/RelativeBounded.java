package com.gmail.collinsmith70.unifi.util;

/**
 * Specialized implementation of a {@link Bounded} whose coordinates are specified in terms of
 * relativity to its parent. In this implementation, global screen points can still be obtained
 * via recursive operations (e.g., get the parent's left side recursively and add the left offset
 * of this {@code RelativeBounded} to it for the global {@linkplain #getLeft() left} side of this
 * {@code RelativeBounded}). This type of implementation is useful in complex hierarchical layouts
 * where children are laid out based on their parents {@link Bounded} coordinates.
 */
public interface RelativeBounded extends Bounded {

/**
 * @return {@code Bounded} of which this {@code RelativeBounded} is relative to
 */ Bounded getRelativeParent();

/**
 * @return distance from the {@linkplain #getBottom() bottom} side of the parent to the bottom side
 *         of this {@code RelativeBounded}
 *
 * @see #setRelativeBottom(int)
 * @see #setRelativeBounds(int, int, int, int)
 * @see #getBottom()
 */ int getRelativeBottom();
/**
 * @return distance from the {@linkplain #getLeft() left} side of the parent to the left side of
 *         this {@code RelativeBounded}
 *
 * @see #setRelativeLeft(int)
 * @see #setRelativeBounds(int, int, int, int)
 * @see #getLeft()
 */ int getRelativeLeft();
/**
 * @return distance from the {@linkplain #getLeft() left} side of the parent to the right side of
 *         this {@code RelativeBounded}
 *
 * @see #setRelativeRight(int)
 * @see #setRelativeBounds(int, int, int, int)
 * @see #getRight()
 */ int getRelativeRight();
/**
 * @return distance from the {@linkplain #getBottom() bottom} side of the parent to the top side of
 *         this {@code RelativeBounded}
 *
 * @see #setRelativeTop(int)
 * @see #setRelativeBounds(int, int, int, int)
 * @see #getTop()
 */ int getRelativeTop();

/**
 * @param bottom distance from the bottom side of the parent to the bottom side of this
 *               {@code RelativeBounded}
 *
 * @see #getRelativeBottom()
 * @see #setRelativeBounds(int, int, int, int)
 * @see #setBottom(int)
 */ void setRelativeBottom(int bottom);
/**
 *
 * @param left distance from the left side of the parent to the left side of this
 *             {@code RelativeBounded}
 *
 * @see #getRelativeLeft()
 * @see #setRelativeBounds(int, int, int, int)
 * @see #setLeft(int)
 */ void setRelativeLeft(int left);
/**
 * @param right distance from the left side of the parent to the right side of this
 *              @code RelativeBounded}
 *
 * @see #getRelativeRight()
 * @see #setRelativeBounds(int, int, int, int)
 * @see #setRight(int)
 */ void setRelativeRight(int right);
/**
 * @param top distance from the bottom side of the parent to the top side of this
 *               {@code RelativeBounded}
 *
 * @see #getRelativeTop()
 * @see #setRelativeBounds(int, int, int, int)
 * @see #setTop(int)
 */ void setRelativeTop(int top);

/**
 * Translates the position of this {@code RelativeBounded} so that its
 * {@linkplain #getRelativeBottom() bottom} offset matches the specified one.
 * <p>
 * Note: Similar to {@link #moveBottom(int)}, this operation does not effect the area of this
 *       {@code RelativeBounded}
 * </p>
 *
 * @param bottom distance from the {@linkplain #getBottom() bottom} side of the parent to the bottom
 *               side of this {@code RelativeBounded}
 *
 * @see #moveBottom(int)
 */ void moveRelativeBottom(int bottom);
/**
 * Translates the position of this {@code RelativeBounded} so that its
 * {@linkplain #getRelativeLeft() left} offset matches the specified one.
 * <p>
 * Note: Similar to {@link #moveLeft(int)}, this operation does not effect the area of this
 *       {@code RelativeBounded}
 * </p>
 *
 * @param left distance from the {@linkplain #getLeft() left} side of the parent to the left side of
 *             this {@code RelativeBounded}
 *
 * @see #moveLeft(int)
 */ void moveRelativeLeft(int left);
/**
 * Translates the position of this {@code RelativeBounded} so that its
 * {@linkplain #getRelativeRight() right} offset matches the specified one.
 * <p>
 * Note: Similar to {@link #moveRight(int)}, this operation does not effect the area of this
 *       {@code RelativeBounded}
 * </p>
 *
 * @param right distance from the {@linkplain #getLeft() left} side of the parent to the right side
 *              of this {@code RelativeBounded}
 *
 * @see #moveRight(int)
 */ void moveRelativeRight(int right);
/**
 * Translates the position of this {@code RelativeBounded} so that its
 * {@linkplain #getRelativeTop() top} offset matches the specified one.
 * <p>
 * Note: Similar to {@link #moveTop(int)}, this operation does not effect the area of this
 *       {@code RelativeBounded}
 * </p>
 *
 * @param top distance from the {@linkplain #getBottom() bottom} side of the parent to the top
 *               side of this {@code RelativeBounded}
 *
 * @see #moveBottom(int)
 */ void moveRelativeTop(int top);

/**
 * Sets the bounds of this {@code Bounded} to the specified coordinates which are offsets from the
 * parents'.
 * <p>
 * Note: The right side must be greater than or equal to the left side, and the top side must be
 *       greater than or equal to the bottom side.
 * </p>
 *
 * @param left   distance from the left side of the parent to the left side of this
 *               {@code RelativeBounded}
 * @param right  distance from the left side of the parent to the right side of this
 *               {@code RelativeBounded}
 * @param top    distance from the bottom side of the parent to the top side of this
 *               {@code RelativeBounded}
 * @param bottom distance from the bottom side of the parent to the bottom side of this
 *               {@code RelativeBounded}
 *
 * @see #setBounds(int, int, int, int)
 * @see #setRelativeBottom(int)
 * @see #setRelativeLeft(int)
 * @see #setRelativeRight(int)
 * @see #setRelativeTop(int)
 */ void setRelativeBounds(int left, int right, int top, int bottom);

}
