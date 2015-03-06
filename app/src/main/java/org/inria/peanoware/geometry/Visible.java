package org.inria.peanoware.geometry;

/*
 * Visible.java
 *
 */

import android.graphics.Point;
import android.graphics.Rect;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Compatibility class
 *
 * @author Laurent Théry
 */
public class Visible {

    private static final int DELTA = 20;
    private Vector<Rect> comp;
    private Vector<Rect> tmp;

    /**
     * Creates a new instance of Visible
     */
    public Visible(Dimension dim) {
        comp = new Vector<>(0);
        tmp = new Vector<>(0);
        comp.add(new Rect(0, 0, dim.getWidth(), dim.getHeight()));
    }

    void add(Rect rec) {
        //System.out.println("trying to add " + rec)
        if (rec.width() != 0 && rec.height() != 0) {
            tmp.add(rec);
        }
    }

    public void remove(Rect rec) {
        rec = new Rect(rec.left - DELTA, rec.top - DELTA,
                       rec.right + DELTA, rec.bottom + DELTA);
        //System.out.println("Before remove " + comp.size());
        //System.out.println(rec);
        Enumeration<Rect> e = comp.elements();
        Rect el, inter = new Rect();
        tmp.removeAllElements();
        while (e.hasMoreElements()) {
            el = e.nextElement();
            //System.out.println("Checking " + el);
            inter.set(el);
            if (!inter.intersect(rec)) {
                add(el);
                continue;
            }
            add(new Rect(el.left, el.top, el.right, inter.top));
            add(new Rect(el.left, inter.bottom, el.right, el.bottom));
            add(new Rect(el.left, el.top, inter.left, el.bottom));
            add(new Rect(inter.right, el.top,el.right, el.bottom));
        }
        Vector<Rect> tmp1 = comp;
        comp = tmp;
        tmp = tmp1;
        /* System.out.println("After remove " + comp.s ize()); */

    }

    public Rect bestChoice(Rect rec) {
        Enumeration<Rect> e = comp.elements();
        Rect el;
        Point best = null;
        int mLeft, mTop;
        while (e.hasMoreElements()) {
            el = e.nextElement();
            if (el.width() < rec.width() || el.height() < rec.height()) {
                continue;
            }
            // Compute mx
            if (rec.left < el.left) {
                mLeft = el.left;
            } else if (el.right < rec.left) {
                mLeft = el.right - rec.width();
            } else {
                mLeft = rec.left - Math.max(0, rec.right - el.right);
            }
            // Compute my
            if (rec.top < el.top) {
                mTop = el.top;
            } else if (el.bottom < rec.height()) {
                mTop = el.bottom - rec.height();
            } else {
                mTop = rec.top - Math.max(0, rec.bottom - el.bottom);
            }
            mLeft -= rec.left;
            mTop -= rec.top;
            if (best == null) {
                best = new Point(mLeft, mTop);
                continue;
            }
            if (mLeft * mLeft + mTop * mTop < best.x * best.x + best.y * best.y) {
                best = new Point(mLeft, mTop);
            }
        }
        if (best == null) {
            return null;
        }
        Rect res = new Rect(rec);
        res.offset(best.x, best.y);
        return res;
    }
}




