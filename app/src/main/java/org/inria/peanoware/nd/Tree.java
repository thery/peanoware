package org.inria.peanoware.nd;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import org.inria.peanoware.Resources;
import org.inria.peanoware.formula.Formula;
import org.inria.peanoware.geometry.TextLayout;

import java.util.Vector;

/**
 * @author Laurent Théry
 * @since 2/19/15.
 * Tree structure
 */
public class Tree {

    public static final int A_NONE = 0;
    private static final int A_LEFT = 1;
    private static final int A_LEFT_RIGHT = 3;
    private int saved_select = A_NONE;
    private int deltaX;
    private int deltaY;
    private static final int THICKNESS = 2;
    public static final boolean LEFT = false;
    public static final boolean RIGHT = true;
    private static final Rect empty = new Rect(0, 0, 0, 0);
    private Rect[] shapes;
    private final Point origin;
    private final Rect bar;
    private final Point conclusion;
    private final Rect size;
    private final Rect rec;
    private final boolean close;
    private Tree[] trees;
    Tree father;
    private final TextLayout text;
    private final Formula f;
    public Tree(Formula f, boolean close, Tree[] trees) {
        rec = new Rect();
        shapes = new Rect[0];
        origin = new Point(0, 0);
        bar = new Rect(empty);
        conclusion = new Point(0, 0);
        size = new Rect(empty);
        text = new TextLayout(f.toString());
        text.setTypeface(Resources.FONT_FORMULA);
        text.setSize(Resources.SIZE_FORMULA);
        deltaX = Resources.SIZE_FORMULA;
        deltaY = Resources.SIZE_FORMULA / 3;
        this.f = f;
        this.close = close;
        setSons(trees);
        father = null;
    }

    void setSelect(int select) {
        saved_select = select;
        if (select == A_LEFT) {
            shapes = new Rect[1];
            Formula f = getConclusion();
            String son = f.get(LEFT).toString();
            int i = f.toString().indexOf(son);
            shapes[0] = text.getBounds(i, i + son.length());
            shapes[0].set(shapes[0].left, shapes[0].top - deltaY,
                          shapes[0].right, shapes[0].bottom + deltaY);
            return;
        }
        if (select == A_LEFT_RIGHT) {
            Formula f = getConclusion();
            Point[] pt = new Point[f.toString().length()];
            f.putIndex(0, pt, false);
            shapes = new Rect[2];
            String son = f.get(LEFT).toString();
            int i = f.maxPoint(0, pt).x;
            shapes[0] = text.getBounds(i, i + son.length());
            shapes[0].set(shapes[0].left, shapes[0].top - deltaY,
                    shapes[0].right, shapes[0].bottom + deltaY);
            son = f.get(RIGHT).toString();
            i = f.maxPoint(pt.length - 1, pt).x;
            shapes[1] = text.getBounds(i, i + son.length());
            shapes[1].set(shapes[1].left, shapes[1].top - deltaY,
                    shapes[1].right, shapes[1].bottom + deltaY);
            return;
        }
        shapes = new Rect[0];
    }
    public Tree get(int i) {
        return trees[i];
    }

    public int getArity() {
        return trees.length;
    }

    void setFather(Tree father) {
        this.father = father;
    }
    public Tree getFather() {
        return father;
    }
    public Tree getRoot() {
        Tree res = this;
        while (res.father != null) {
            res = res.father;
        }
        return res;
    }
    public Tree(Formula f, boolean close) {
        this(f,  close, new Tree[0]);
    }
    public void setOrigin(int x, int y) {
        this.origin.x = x;
        this.origin.y = y;
    }
    public void setOrigin(Point origin) {
        setOrigin(origin.x, origin.y);
    }
    public Point getOrigin() {
        return origin;
    }
    public int getWidth() {
        return getSize().width();
    }
    int getBaseHeight() {
        update();
        return conclusion.y;
    }
    int getMaxBaseHeight() {
        int res = 0;
        for (Tree tree : trees) {
            res = Math.max(res, tree.getBaseHeight());
        }
        return res;
    }

    int getWidth(boolean b) {
        if (b == LEFT) {
            return getWidth() - conclusion.x;
        }
        update();
        return conclusion.x + text.getBounds().width();
    }

    public int getHeight() {
        return getSize().height();
    }
    int getBaseWidth() {
        switch (trees.length) {
            case 0:
                return 0;
            case 1:
                return trees[0].getConclusionBounds().width();
        }
        int res = trees[0].getWidth(LEFT)
                + trees[trees.length - 1].getWidth(RIGHT)
                + deltaX;
        for (int i = 1; i < trees.length - 1; i++) {
            res += trees[i].getWidth() + deltaX;
        }
        return res;
    }
    void modify() {
        size.set(empty);
        if (father != null) {
            father.modify();
        }
    }

    void setSons(Tree[] sons) {
        trees = sons;
        for (Tree tree : trees) {
            tree.setFather(this);
        }
        modify();
    }
    public void replace(Tree from, Tree to) {
        for (int i = 0; i < trees.length; i++) {
            if (trees[i] == from) {
                trees[i] = to;
                to.setFather(this);
            }
        }
        modify();
    }
    void update() {
        getSize();
    }

    public Rect getSize() {
        if (!empty.equals(size)) {
            return size;
        }
        size.set(0, 0, 0, 0);
        // Take care of all width:
        // sumWidth the real width of all sons
        // baseWidth the real width minus the initial bit of the first
        //            son and the final bit of the last won
        // textWidth the width of the conclusion
        int baseWidth = getBaseWidth();
        int textWidth = text.getBounds().width();
        int maxWidth = Math.max(baseWidth, textWidth);
        int rx, lDelta, rb, rc;
        if (maxWidth == baseWidth) {
            rx = 0;
            lDelta = 0;
            rb = trees[0].getWidth() - trees[0].getWidth(LEFT);
            rc = rb + (baseWidth - textWidth) / 2;
        } else {
            lDelta = (textWidth - baseWidth)/ (trees.length + 1);
            int u = 0;
            if (trees.length != 0) {
                u = (trees[0].getWidth() - trees[0].getWidth(LEFT)) - lDelta;
            }
            if (u > 0) {
                rx = 0;
                rb = u;
                rc = u;
            } else {
                rx = -u;
                rb = 0;
                rc = 0;
            }
        }
        lDelta += deltaX;
        int maxBaseHeight = getMaxBaseHeight();
        // Now we can place all the subTrees
        Rect tmp = new Rect();
        for (Tree tree : trees) {
            tree.setOrigin(rx, maxBaseHeight - tree.getBaseHeight());
            rx += tree.getWidth() + lDelta;
            tmp.set(tree.getSize());
            tmp.offset(tree.getOrigin().x, tree.getOrigin().y);
            size.union(tmp);
        }
        // Now we can take care of the bar
        int ry = size.bottom + deltaY;
        bar.set(rb, ry, rb + maxWidth, ry + THICKNESS);
        size.union(bar);
        // Finally the conclusion
        ry += THICKNESS + deltaY - text.getBounds().top;
        conclusion.x = rc;
        conclusion.y = ry;
        Rect rec = text.getBounds();
        rec.offset(conclusion.x, conclusion.y);
        size.union(rec);
        return size;
    }

    public void draw(Canvas c, Paint p, boolean b, Vector v) {
        update();
        c.translate(origin.x, origin.y);
        if (b) {
            int c1 = p.getColor();
            p.setColor(Resources.HYP);
            c.translate(conclusion.x, conclusion.y);
            Paint.Style s = p.getStyle();
            p.setStyle(Paint.Style.FILL);
            for (Rect shape : shapes) {
                c.drawRect(shape.left, shape.top, shape.right, shape.bottom, p);
            }
            c.translate(-conclusion.x, -conclusion.y);
            p.setColor(c1);
            p.setStyle(s);
        }
        if (v.contains(this)) {
            int c1 = p.getColor();
            p.setColor(Resources.TO);
            c.translate(conclusion.x, conclusion.y);
            Paint.Style s = p.getStyle();
            p.setStyle(Paint.Style.FILL);
            c.drawRect(getConclusionBounds().left, getConclusionBounds().top,
                    getConclusionBounds().right, getConclusionBounds().bottom, p);
            c.translate(-conclusion.x, -conclusion.y);
            p.setColor(c1);
            p.setStyle(s);
        }
        text.draw(c, conclusion.x, conclusion.y);
        if (trees.length != 0 || close) {
            Paint.Style s = p.getStyle();
            int cc = p.getColor();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.BLACK);
            c.drawRect(bar.left,bar.top, bar.right, bar.bottom, p);
            p.setStyle(s);
            p.setColor(cc);
        }
        for (Tree tree : trees) {
            tree.draw(c, p, b, v);
        }
        c.translate(-origin.x, -origin.y);
    }
    public void drawConclusion(Canvas c, Paint p, int mod) {
        update();
        c.translate(conclusion.x, conclusion.y);
        Rect sh;
        if (mod != 0) {
            sh = shapes[mod - 1];
        } else {
            sh = text.getBounds();
        }
        int c1 = p.getColor();
        p.setColor(Resources.TO);
        Paint.Style s = p.getStyle();
        p.setStyle(Paint.Style.FILL);
        c.drawRect(sh.left, sh.top, sh.right, sh.bottom, p);
        p.setColor(c1);
        p.setStyle(s);
        text.draw(c, 0, 0);
        c.translate(-conclusion.x, -conclusion.y);
    }
    public Tree inside(int x, int y, ModPoint p) {
        x = x - origin.x;
        y = y - origin.y;
        if (!getSize().contains(x, y)) {
            return null;
        }
        p.translate(origin.x, origin.y);
        Tree res;
        for (Tree tree : trees) {
            res = tree.inside(x, y, p);
            if (res != null) {
                return res;
            }
        }
        boolean b;
        for (int i = 0; i < shapes.length; i++) {
            shapes[i].offset(conclusion.x, conclusion.y);
            b = shapes[i].contains(x, y);
            shapes[i].offset(-conclusion.x, -conclusion.y);
            if (b) {
                p.setMod(i + 1);
                break;
            }
        }
        if (bar.contains(x, y)
                || text.getBounds().contains(x - conclusion.x, y - conclusion.y)) {
            return this;
        }
        return null;
    }
    public Formula getConclusion() {
        return f;
    }

    public boolean closed() {
        if (trees.length == 0) {
            return close;
        }
        for (Tree tree : trees) {
            if (!(tree.closed())) {
                return false;
            }
        }
        return true;
    }
    public void update(Rect rec) {
        origin.x = rec.left;
        origin.y = rec.top;
    }
    public Rect getRectangle() {
        rec.set(getSize());
        rec.offset(origin.x, origin.y);
        return rec;
    }
    public void drawArc(Canvas c, Paint p, ModPoint p1, Tree f,ModPoint p2, int cl) {
        Rect b1 = getConclusionBounds(p1.getMod());
        Rect b2 = f.getConclusionBounds(p2.getMod());
        int cl1 = p.getColor();
        p.setColor(cl);
        int x = conclusion.x + p1.getX();
        int y = conclusion.y + p1.getY();
        int fx = f.conclusion.x + p2.getX();
        int fy = f.conclusion.y + p2.getY();
        c.drawLine(x + b1.centerX(), y + b1.centerY(),
                fx + b2.centerX(), fy + b2.centerY(),p);
        p.setColor(cl1);
    }
    public void drawArc(Canvas c, Paint p, ModPoint p1, Point p2, int cl) {
        Rect b1 = getConclusionBounds(p1.getMod());
        int cl1 = p.getColor();
        int x = conclusion.x + p1.getX();
        int y = conclusion.y + p1.getY();
        p.setColor(cl);
        c.drawLine(x + b1.centerX(), y + b1.centerY(),
                p2.x, p2.y, p);
        p.setColor(cl1);
    }
    Rect getConclusionBounds() {
        return text.getBounds();
    }
    Rect getConclusionBounds(int mod) {
        if (mod == 0) {
            return text.getBounds();
        }
        return shapes[mod - 1];
    }
    void getContext(Vector<Formula> ctx) {
        if (father == null) {
            return;
        }
        switch (father.trees.length) {
            case 3: {
                //it is a Or elimination
                Formula f = father.get(0).getConclusion();
                ctx.addElement(f.get(this == father.get(1) ? 0 : 1));
                break;
            }
            case 2: {
                break;
            }
            case 1: {
                Formula f = father.getConclusion();
                if (f.isImp() || f.isNeg()) {
                    ctx.addElement(f.get(0));
                }
            }
        }
        father.getContext(ctx);
    }
    boolean isInContext(Formula f) {
        if (isImpI() || isNegI()) {
            if (getConclusion().get(LEFT).equals(f)) {
                return true;
            }
        }
        if (isOrE()) {
            if (get(0).getConclusion().get(LEFT).equals(f)) {
                return true;
            }
            if (get(0).getConclusion().get(RIGHT).equals(f)) {
                return true;
            }
        }
        for (int i = 0; i < getArity(); i++) {
            if (get(i).isInContext(f)) {
                return true;
            }
        }
        return false;
    }

    void getHypothesis(Vector<Formula> res) {
        if (getArity() == 0) {
            if (close) {
                res.addElement(getConclusion());
            }
            return;
        }
        if (isOrE()) {
            get(0).getHypothesis(res);
            if (res.contains(get(0).getConclusion().get(LEFT))) {
                get(1).getHypothesis(res);
            } else {
                get(1).getHypothesis(res);
                res.remove(get(0).getConclusion().get(LEFT));
            }
            if (res.contains(get(0).getConclusion().get(RIGHT))) {
                get(2).getHypothesis(res);
            } else {
                get(2).getHypothesis(res);
                res.remove(get(0).getConclusion().get(RIGHT));
            }
            return;
        }
        if (isImpI() || isNegI()) {
            if (res.contains(getConclusion().get(LEFT))) {
                get(0).getHypothesis(res);
            } else {
                get(0).getHypothesis(res);
                res.remove(getConclusion().get(LEFT));
            }
            return;
        }

        for (int i = 0; i < getArity(); i++) {
            get(i).getHypothesis(res);
        }
    }
    void getOpenLeaves(Vector<Tree> res) {
        if (trees.length == 0) {
            if (!close) {
                res.addElement(this);
            }
            return;
        }
        for (Tree tree : trees) {
            tree.getOpenLeaves(res);
        }
    }


    boolean isConclusion() {
        return father == null;
    }

    public boolean isClosedLeaf() {
        return getArity() == 0 && close;
    }
    public static Tree makeOpenLeaf(Formula f) {
        return new Tree(f, false);
    }
    public boolean isOpenLeaf() {
        return getArity() == 0 && !close;
    }
    public static Tree makeAndI(Formula f) {
        //System.out.println("makeAndI");
        return new Tree(f, false,
                new Tree[] {
                        makeOpenLeaf(f.get(LEFT)),
                        makeOpenLeaf(f.get(RIGHT))});
    }

    public static Tree makeOrI(Formula f, boolean b) {
        //System.out.println("makeOrI");
        return new Tree(f, false,
                new Tree[] {
                        makeOpenLeaf(f.get(b))});
    }

    public boolean isOrI(boolean b) {
        return getConclusion().isOr()
                && getArity() == 1
                && getConclusion().get(b).equals(get(0).getConclusion());
    }
    public static Tree makeImpI(Formula f) {
        //System.out.println("makeImpI");
        Tree tree = new Tree(f, false,
                new Tree[] {
                        makeOpenLeaf(f.get(RIGHT))});
        tree.setSelect(A_LEFT);
        return tree;
    }
    boolean isImpI() {
        return getConclusion().isImp()
                && getArity() == 1
                && getConclusion().get(RIGHT).equals(get(0).getConclusion());
    }
    public static Tree makeNegI(Formula f) {
        //System.out.println("makeNegI");

        Tree tree = new Tree(f, false,
                new Tree[] {
                        makeOpenLeaf(Formula.makeFalse())});
        tree.setSelect(A_LEFT);
        return tree;
    }
    boolean isNegI() {
        return getConclusion().isNeg()
                && getArity() == 1
                && get(0).getConclusion().isFalse();
    }

    public Tree makeAndE(boolean b) {
        //System.out.println("makeAndE");

        return new Tree(getConclusion().get(b), false,
                new Tree[] {this});
    }

    public boolean isAndE(boolean b) {
        return getArity() == 1
                && get(0).getConclusion().isAnd()
                && getConclusion().equals(get(0).getConclusion().get(b));
    }
    public Tree makeOrE(Formula f) {
        //System.out.println("makeOrE");

        Tree t = new Tree(f, false,
                new Tree[] {this, makeOpenLeaf(f), makeOpenLeaf(f)});
        t.get(0).setSelect(A_LEFT_RIGHT);
        return t;
    }
    boolean isOrE() {
        return getArity() == 3
                && get(0).getConclusion().isOr()
                && getConclusion().equals(get(1).getConclusion())
                && getConclusion().equals(get(2).getConclusion());
    }
    public Tree makeImpE() {
        //System.out.println("makeImpE");

        return
                new Tree(getConclusion().get(RIGHT), false,
                        new Tree[] {
                                this,
                                makeOpenLeaf(getConclusion().get(LEFT))});
    }

    public Tree makeNegE() {
        //System.out.println("makeNegE");

        return new Tree(Formula.makeFalse(), false,
                new Tree[] {
                        this,
                        makeOpenLeaf(getConclusion().get(0))});
    }

    public Tree makeFalseE(Formula f) {
        //System.out.println("makFalseE");

        return new Tree(f, false, new Tree[] {this});
    }

    public void setFontSize(float sz) {
        deltaX = (int)sz;
        deltaY = (int)(sz / 3);
        text.setSize(sz);
        size.set(0, 0, 0, 0);
        setSelect(saved_select);
        for (Tree tree : trees) {
            tree.setFontSize(sz);
        }
    }

}
