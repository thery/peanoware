package org.inria.peanoware.build;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;

import org.inria.peanoware.Resources;

/**
 * @author Laurent Théry
 * @date 2/15/15.
 * Point in a Box
 */

class BoxPoint {
    private int i;
    private int x;
    private int y;
    private ShapeDrawable s;

    public void set(BoxPoint p) {
        x = p.x;
        y = p.y;
        i = p.i;
        s = p.s;
    }
    public void setIndex(int i) {
        this.i = i;
    }
    public int getIndex() {
        return i;
    }
    public void setShape(ShapeDrawable s) {
        this.s = s;
    }
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void invalidate() {
        s = null;
    }
    public boolean isValid() {
        return s != null;
    }
    public boolean diff(BoxPoint p) {
        if ((p.s == null & s != null) || (p.s != null && s == null)) {
            return true;
        }
        return p.x != x || p.y != y;
    }
    public void draw(Canvas c) {
        if (s != null) {
            Paint p = s.getPaint();
            int col1 = p.getColor();
            c.translate(x, y);
            p.setColor(Resources.BOX_COLOR);
            s.draw(c);
            c.translate(-x, -y);
            p.setColor(col1);
        }
    }
}
