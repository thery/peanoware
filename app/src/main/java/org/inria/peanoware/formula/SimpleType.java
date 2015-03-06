package org.inria.peanoware.formula;

/**
 * @author Laurent Théry
 * @date 2/15/15.
 * Simple Type
 */
/**
 *
 * @author  thery
 */
public class SimpleType extends Type {
    private static final String PROP = "Prop";
    private static final String TERM = "Term";
    private final String type;

    /** Creates a new instance of SimpleType */
    private SimpleType(String type) {
        this.type = type;
    }

    public boolean isProp() {
        return PROP.equals(type);
    }

    public static SimpleType makeProp() {
        return new SimpleType(PROP);
    }

    public boolean isTerm() {
        return TERM.equals(type);
    }

    public static SimpleType makeTerm() {
        return new SimpleType(TERM);
    }
    public boolean equals(Object o) {
        return o instanceof SimpleType && type.equals(((SimpleType) o).type);
    }

}
