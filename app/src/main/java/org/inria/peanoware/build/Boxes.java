package org.inria.peanoware.build;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.inria.peanoware.formula.Formula;
import org.inria.peanoware.geometry.Visible;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Laurent Théry
 * @date 2/15/15.
 * List of Boxes
 */

class Boxes {
    private final Vector<Box> list = new Vector<>();

    public void init(Formula f) {
        list.removeAllElements();
        Vector<Formula> v = new Vector<>();
        f.collectVars(v);
        Enumeration<Formula> en = v.elements();
        int i = 0;
        while (en.hasMoreElements()) {
            list.add(new Box(20, 20 + i * 40, en.nextElement()));
            i++;
        }
        list.add(new Box(100,100,Formula.makeTermVar("x")));
    }

    public void draw(Canvas c, Paint p) {
        Enumeration en = list.elements();
        while (en.hasMoreElements()) {
            ((Box) en.nextElement()).draw(c);
        }
    }
    public void add(Box bf) {
        list.add(bf);
    }
    public void delete(Box bf) {
        list.remove(bf);
    }
    Box inside1(int x, int y, BoxPoint mod) {
        Enumeration en = list.elements();
        Box bx;
        while (en.hasMoreElements()) {
            bx = ((Box) en.nextElement());
            if (bx.inside(x, y, mod)) {
                return bx;
            }
        }
        return null;
    }
    public Box inside(int x, int y, BoxPoint mod) {
        return inside1(x, y, mod);
    }

    public void complete (Visible vis) {
        complete(vis, new Box[0]);
    }
    void complete(Visible vis, Box[] bs) {
        Enumeration en = list.elements();
        Box bx;
        boolean in;
        while (en.hasMoreElements()) {
            bx = ((Box) en.nextElement());
            in = false;
            for (Box b : bs) {
                if (b == bx) {
                    in = true;
                    break;
                }
            }
            if (!in) {
                vis.remove(bx.getRectangle());
            }
        }
    }
    public Enumeration elements() {
        return list.elements();
    }
    public boolean hasWon(Formula f) {
        return (list.size() == 1) && (f.toString()).equals(list.get(0).toString());
    }
}