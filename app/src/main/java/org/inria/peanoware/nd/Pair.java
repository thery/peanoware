package org.inria.peanoware.nd;

/**
 * @author Laurent Théry
 * @date 2/15/15.
 Pair of a tree and its subtrees
**/
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.inria.peanoware.formula.Formula;
import org.inria.peanoware.Resources;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

class Pair {
    static private final int DELTA = 10;
    private Tree main;
    static private final Vector<Tree> emptyVector = new Vector<>();
    private Vector<Tree> hyp;

    public Pair() {
        init();
    }
    static private final String x = Resources.BUILD_PROPOSITION_VARIABLE[0];
    static private final String y = Resources.BUILD_PROPOSITION_VARIABLE[1];
    static private final String z = Resources.BUILD_PROPOSITION_VARIABLE[2];
    static private final String t = Resources.BUILD_PROPOSITION_VARIABLE[3];
    private static final Random rand = new Random();
    private static final int W_COLOR = Color.rgb(255, 122, 115);
    private static final Formula[] examples = new Formula[] {
            // ((x -> y) /\ (y -> z)) -> (x => z)
            Formula.makeImp(
                    Formula.makeAnd(
                            Formula.makeImp(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makeImp(Formula.makePropVar(y), Formula.makePropVar(z))),
                    Formula.makeImp(Formula.makePropVar(x), Formula.makePropVar(z))),
            // ((x \/ y) /\ (~y \/ z)) -> (x \/ z)
            Formula.makeImp(
                    Formula.makeAnd(
                            Formula.makeOr(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makeOr(
                                    Formula.makeNeg(Formula.makePropVar(y)),
                                    Formula.makePropVar(z))),
                    Formula.makeOr(Formula.makePropVar(x), Formula.makePropVar(z))),
            // ((x \/ y) /\ (~x\/ y)) -> y
            Formula.makeImp(
                    Formula.makeAnd(
                            Formula.makeOr(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makeOr(
                                    Formula.makeNeg(Formula.makePropVar(x)),
                                    Formula.makePropVar(y))),
                    Formula.makePropVar(y)),
            // ((x \/ y) /\ ~y) -> x
            Formula.makeImp(
                    Formula.makeAnd(
                            Formula.makeOr(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makeNeg(Formula.makePropVar(y))),
                    Formula.makePropVar(x)),
            // (~x \/ ~y) -> ~(x /\ y)
            Formula.makeImp(
                    Formula.makeOr(
                            Formula.makeNeg(Formula.makePropVar(x)),
                            Formula.makeNeg(Formula.makePropVar(y))),
                    Formula.makeNeg(
                            Formula.makeAnd(Formula.makePropVar(x), Formula.makePropVar(y)))),
            // (~x /\ ~y) -> ~(x \/ y)
            Formula.makeImp(
                    Formula.makeAnd(
                            Formula.makeNeg(Formula.makePropVar(x)),
                            Formula.makeNeg(Formula.makePropVar(y))),
                    Formula.makeNeg(
                            Formula.makeOr(Formula.makePropVar(x), Formula.makePropVar(y)))),
            // x -> ~~x
            Formula.makeImp(
                    Formula.makePropVar(x),
                    Formula.makeNeg(Formula.makeNeg(Formula.makePropVar(x)))),
            // ~~~x -> ~x
            Formula.makeImp(
                    Formula.makeNeg(
                            Formula.makeNeg(Formula.makeNeg(Formula.makePropVar(x)))),
                    Formula.makeNeg(Formula.makePropVar(x))),
            // (x -> y) -> ~y -> ~x
            Formula.makeImp(
                    Formula.makeImp(Formula.makePropVar(x), Formula.makePropVar(y)),
                    Formula.makeImp(
                            Formula.makeNeg(Formula.makePropVar(y)),
                            Formula.makeNeg(Formula.makePropVar(x)))),
            // (x -> y) -> ~y -> ~x
            Formula.makeImp(
                    Formula.makeImp(Formula.makePropVar(x), Formula.makePropVar(y)),
                    Formula.makeImp(
                            Formula.makeNeg(Formula.makePropVar(y)),
                            Formula.makeNeg(Formula.makePropVar(x)))),
            // (x -> y) -> ~y -> ~x
            Formula.makeImp(
                    Formula.makeImp(Formula.makePropVar(x), Formula.makePropVar(y)),
                    Formula.makeImp(
                            Formula.makeNeg(Formula.makePropVar(y)),
                            Formula.makeNeg(Formula.makePropVar(x)))),
            // ~~(x \/ x)
            Formula.makeNeg(
                    Formula.makeNeg(
                            Formula.makeOr(Formula.makePropVar(x),
                                    Formula.makeNeg(Formula.makePropVar(x))))),
            // (x /\ y) /\ z -> x /\ y /\ z
            Formula.makeImp(
                    Formula.makeImp(
                            Formula.makeAnd(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makePropVar(z)),
                    Formula.makeImp(
                            Formula.makePropVar(x),
                            Formula.makeImp(Formula.makePropVar(y), Formula.makePropVar(z)))),
            // (x -> y -> z) -> ((x /\ y) -> z)
            Formula.makeImp(
                    Formula.makeImp(Formula.makePropVar(x),
                            Formula.makeImp(Formula.makePropVar(y), Formula.makePropVar(z))),
                    Formula.makeImp(
                            Formula.makeAnd(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makePropVar(z))),
            // ((x -> y) -> z) -> (x -> z)
            Formula.makeImp(
                    Formula.makeImp(
                            Formula.makeImp(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makePropVar(z)),
                    Formula.makeImp(Formula.makePropVar(y), Formula.makePropVar(z))),
            // (x -> y -> z) -> ((x /\ y) -> z)
            Formula.makeImp(
                    Formula.makeImp(
                            Formula.makeAnd(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makePropVar(z)),
                    Formula.makeImp(Formula.makePropVar(x),
                            Formula.makeImp(Formula.makePropVar(y), Formula.makePropVar(z)))),
            // (x /\ y /\ z) -> ((z /\ y) /\ z
            Formula.makeImp(
                    Formula.makeAnd(Formula.makePropVar(x),
                            Formula.makeAnd(Formula.makePropVar(y),Formula.makePropVar(z))),
                    Formula.makeAnd(
                            Formula.makeAnd(Formula.makePropVar(z), Formula.makePropVar(y)),
                            Formula.makePropVar(x))),
            // ((x -> y) /\ (z -> t)) -> (x /\ z) -> (y /\ t)
            Formula.makeImp(
                    Formula.makeAnd(
                            Formula.makeImp(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makeImp(Formula.makePropVar(z), Formula.makePropVar(t))),
                    Formula.makeImp(
                            Formula.makeAnd(Formula.makePropVar(x), Formula.makePropVar(z)),
                            Formula.makeAnd(Formula.makePropVar(y), Formula.makePropVar(t)))),
            // (x /\ y) -> (y /\ x)
            Formula.makeImp(
                    Formula.makeAnd(Formula.makePropVar(x), Formula.makePropVar(y)),
                    Formula.makeAnd(Formula.makePropVar(y), Formula.makePropVar(x))),
            // (x \/ y) -> (y \/ x)
            Formula.makeImp(
                    Formula.makeOr(Formula.makePropVar(x), Formula.makePropVar(y)),
                    Formula.makeOr(Formula.makePropVar(y), Formula.makePropVar(x))),
            // (x \/ y \/ z) -> (x \/ y) \/ z
            Formula.makeImp(
                    Formula.makeOr(Formula.makePropVar(x),
                            Formula.makeOr(Formula.makePropVar(y), Formula.makePropVar(z))),
                    Formula.makeOr(
                            Formula.makeOr(Formula.makePropVar(x), Formula.makePropVar(y)),
                            Formula.makePropVar(z))),
            // (x \/ y \/ z) -> (z \/ y) \/ x
            Formula.makeImp(
                    Formula.makeOr(Formula.makePropVar(x),
                            Formula.makeOr(Formula.makePropVar(y), Formula.makePropVar(z))),
                    Formula.makeOr(
                            Formula.makeOr(Formula.makePropVar(z), Formula.makePropVar(y)),
                            Formula.makePropVar(x))),
    };

    public void init() {
        Resources.SIZE_FORMULA = Resources.PREFERED_SIZE_FORMULA;
        hyp = new Vector<>();

        Formula f = examples[rand.nextInt(examples.length)];
        main = new Tree(f, false);
    }

    public void draw(Canvas c, Paint p) {
         Paint.Style st = p.getStyle();
         int co = p.getColor();
         float w = p.getStrokeWidth();
         p.setColor(W_COLOR);
         p.setStyle(Paint.Style.STROKE);
         p.setStrokeWidth(10);
         Rect rec = main.getRectangle();
         c.drawRect(rec.left - 2 * DELTA, rec.top - 2 * DELTA,
                 rec.right + 2 * DELTA, rec.bottom + 2 * DELTA, p);
         p.setStyle(st);
         p.setColor(co);
         p.setStrokeWidth(w);
         main.draw(c, p, false, emptyVector);
    }
    public void draw(Canvas c, Paint p, Tree currentTree, Vector v) {
        if ( currentTree == main) {
            Paint.Style st = p.getStyle();
            int co = p.getColor();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.LTGRAY);
            Rect rec = currentTree.getRectangle();
            c.drawRect(rec.left -DELTA, rec.top - DELTA,
                    rec.right + DELTA, rec.bottom + DELTA, p);
            p.setStyle(st);
            p.setColor(co);
        }
        main.draw(c, p, currentTree == main, v);
    //    System.out.println("origin = " + main.origin);
        Enumeration<Tree> en = hyp.elements();
        Tree tmp;
        while (en.hasMoreElements()) {
            tmp = en.nextElement();
            if ( currentTree == tmp) {
                Paint.Style st = p.getStyle();
                int co = p.getColor();
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.LTGRAY);
                Rect rec = currentTree.getRectangle();
                c.drawRect(rec.left -DELTA, rec.top - DELTA,
                           rec.right + DELTA, rec.bottom + DELTA, p);
                p.setStyle(st);
                p.setColor(co);
            }
            tmp.draw(c, p, currentTree == tmp, v);
        }
    }

    public void setMain(Tree tree) {
        main = tree;
        tree.father = null;
    }
    public Tree getMain() {
        return main;
    }
    public void addHyp(Tree tree) {
        hyp.addElement(tree);
        tree.father = null;
    }
    public void addHyp(Tree tree, int i) {
        hyp.add(i,tree);
        tree.father = null;
    }
    public void removeHyp(Tree tree) {
        hyp.remove(tree);
    }

    public Tree inside(int x, int y, ModPoint p) {
        Tree t = main.inside(x, y, p);
        if (t != null) {
            return t;
        }
        Enumeration en = hyp.elements();
        Tree tmp;
        while (en.hasMoreElements()) {
            tmp = (Tree) en.nextElement();
            t = tmp.inside(x, y, p);
            if (t != null) {
                return t;
            }
        }
        return null;
    }
    public Tree findRoot(int x, int y) {
        if (main.getRectangle().contains(x, y)) {
            return main;
        }

        Enumeration en = hyp.elements();
        Tree tmp;
        while (en.hasMoreElements()) {
            tmp = (Tree) en.nextElement();
            if (tmp.getRectangle().contains(x, y)) {
                return tmp;
            }
        }
        return null;
    }
    public Tree findHyp(Formula f) {
        Enumeration en = hyp.elements();
        Tree tmp;
        while (en.hasMoreElements()) {
            tmp = (Tree) en.nextElement();
            if (tmp.getConclusion().equals(f)) {
                return tmp;
            }
        }
        return null;
    }
    public boolean hasWon() {
        return (numberOfHypothesis() == 0 && main.closed());
    }
    public int numberOfHypothesis() {
        return hyp.size();
    }
    public Enumeration getHypothesis() {
        return hyp.elements();
    }
    public int getIndex(Tree tree) {
        return hyp.indexOf(tree);
    }

    public void setFontSize(float sz) {
        main.setFontSize(sz);
        for (Tree tree : hyp) {
            tree.setFontSize(sz);
        }
    }

}
