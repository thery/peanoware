package org.inria.peanoware.formula;

/**
 *
 * @author  Laurent Théry
 * @date 2/19/15
 */
abstract class Type {
    public abstract boolean isProp();
    public abstract boolean isTerm();
}