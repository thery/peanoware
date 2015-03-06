package org.inria.peanoware.formula;

/**
 * @author Laurent Théry
 * @date 3/3/15.
 * Integer
 */
public class Int {

    private int i;

    public Int(int i) {
        this.i = i;
    }
    public int get() {
        return i;
    }
    public void decrement() {
        i--;
    }
    public boolean isNull() {
        return i == 0;
    }
}
