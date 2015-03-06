package org.inria.peanoware.formula;

/**
 *
 * @author  Laurent Théry
 */
abstract class Type {
    public abstract boolean isProp();
    public abstract boolean isTerm();
}