package org.inria.peanoware.build;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

import org.inria.peanoware.formula.Formula;

import org.inria.peanoware.Resources;
import org.inria.peanoware.formula.Int;

import java.util.Random;

/**
 * @author Laurent Théry
 * @date 2/15/15.
 * Box that represents a formula
 */

/**  **/
public class Box {
    private static final Random rand = new Random();
    private static final int DELTA = 5;
    private int x;
    private int y;
    private String text;
    private final Formula f;
    private Paint p;
   // FontRenderContext frc;

    public Box(int x, int y, Formula f) {
        Paint p = new Paint();
        p.setTypeface(Resources.FONT_FORMULA);
        p.setTextSize(Resources.SIZE_FORMULA);
        this.f = f;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return f.toString();
    }

    public void draw(Canvas c) {
        c.drawText(text, x, y, p);
    }

    public void drawSelect(Canvas c, int col) {
        Rect b = new Rect();
        p.getTextBounds(text, 0, text.length(), b);
        c.drawText(text,x, y, p);
        int col1 = p.getColor();
        p.setColor(col);
        c.drawRect(x + b.left - DELTA,
                y + b.top - DELTA,
                x + b.right + DELTA,
                y + b.bottom + DELTA,
                p);
        p.setColor(col1);
        draw(c);
    }

    public void drawArc(Canvas c, Box f, int col) {
        Rect b1 = new Rect();
        p.getTextBounds(text, 0, text.length(), b1);
        Rect b2 = new Rect();
        p.getTextBounds(f.text, 0, text.length(), b2);
        int col1 = p.getColor();
        p.setColor(col);
        c.drawLine(b1.exactCenterX() + x, b1.exactCenterY() + y,
                b2.exactCenterX() + f.x, b2.exactCenterY() + f.y, p);
        p.setColor(col1);
    }

    public void drawArc(Canvas c, Point pt, int col) {
        Rect b1 = new Rect();
        p.getTextBounds(text, 0, text.length(), b1);
        int col1 = p.getColor();
        p.setColor(col);
        c.drawLine(b1.centerX() + x, b1.centerY() + y, pt.x, pt.y, p);
        p.setColor(col1);
    }


    public boolean inside(int x, int y, BoxPoint bp) {
        Rect b = new Rect();
        p.getTextBounds(text, 0, text.length(), b);
        if (b.contains(x - this.x, y - this.y)) {
            String s = f.toString();
            int index = s.indexOf(Resources.BOX);
            while (index != -1) {
                p.getTextBounds(text, index, index + 1, b);
                ShapeDrawable sh = new ShapeDrawable(new RectShape());
                sh.setBounds(b);
                if (b.contains(x - this.x, y - this.y)) {
                    bp.setIndex(index);
                    bp.setLocation(this.x, this.y);
                    bp.setShape(sh);
                    return true;
                }
                index = s.indexOf(Resources.BOX, index + 1);
            }
            return true;
        }
        return false;
    }

    public Box copy() {
        return new Box(x, y, f);
    }

    public Box[] cleanSplit() {
        int lx = x;
        Formula[] fs = f.cleanSplit();
        Box[] res = new Box[fs.length];
        for (int i = 0; i < fs.length; i++) {
            res[i] = new Box(lx, y, fs[i]);
            lx += res[i].getRectangle().width();
        }
        return res;
    }

    public Box[] split() {
        int lx = x;
        Formula[] fs = f.split();
        Box[] res = new Box[fs.length];
        for (int i = 0; i < fs.length; i++) {
            res[i] = new Box(lx, y, fs[i]);
            lx += res[i].getRectangle().width();
        }
        return res;
    }

    public Rect getRectangle() {
        Rect rect = new Rect();
        p.getTextBounds(text, 0, text.length(),rect);
        rect.offset(x,y);
        return rect;
    }

    public Formula getFormula() {
        return f;
    }

    public Box substBox(int i, Formula f1) {
        String s = f.toString();
        int index = s.indexOf(Resources.BOX);
        int j = 0;
        while (index != -1) {
            if (index == i) {
                return new Box(x, y, f.substBox(new Int(j), f1));
            }
            index = s.indexOf(Resources.BOX, index + 1);
            j++;
        }
        return this;
    }

    public Box merge(String name) {
        Formula res = Formula.make(f, name);
        if (res == null) {
            return null;
        }
        return new Box(x, y, res);
    }

    public Box merge(Box box, String name) {
        int mx, my;
        if (y < box.y) {
            mx = x;
            my = y;
        } else {
            mx = x;
            my = box.y;
        }
        Formula res = Formula.make(f, box.f, name);
        if (res == null) {
            return null;
        }
        return new Box(mx, my, res);
    }

    public void update(Rect rec) {
        Rect b = new Rect();
        p.getTextBounds(text, 0, text.length(), b);
        x = rec.left - b.left;
        y = rec.top - b.top;
    }

    private static Formula genFormula(String[] op, String[] var, int num) {
        if (num <= 0) {
            return Formula.makePropVar(var[rand.nextInt(var.length)]);
        }
        String sop = op[rand.nextInt(op.length)];
        int arity = Resources.getArity(sop);
        if (arity == 1) {
            return new Formula(sop, Resources.getString(sop),
                    genFormula(op, var, num - 1));
        }
        int split = rand.nextInt(num);
        Formula f1 = genFormula(op, var, split);
        Formula f2 = genFormula(op, var, num - split - 1);
        return new Formula(sop, Resources.getString(sop), f1, f2);
    }

}
