package org.inria.peanoware.formula;

 import android.graphics.Point;

 import org.inria.peanoware.Resources;
 import java.util.Vector;

/**
 * @author Laurent Théry
 * @date 2/15/15.
 * Formula
 */

public class Formula {
    private static final boolean LEFT = false;
    private static final Formula FALSE = new Formula(Resources.FALSE,
            Resources.FALSE);
    private Formula[] sons;
    private final String op;
    private String sep;
    private String init;
    private String end;
    private boolean select;
    private Type type;
    final int precedence() {
        return Resources.getPrecedence(op);
    }

    final int leftPrecedence() {
        return Resources.getLeftPrecedence(op);
    }

    final int rightPrecedence() {
        return Resources.getRightPrecedence(op);
    }
    Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }
    void setProp() {
        type = SimpleType.makeProp();
    }
    void setTerm() {
        type = SimpleType.makeTerm();
    }
    boolean isProp() {
        return !type.isProp();
    }
    boolean isTerm() {
        return type.isTerm();
    }
    private Formula(String op, String value) {
        this.op = op;
        sons = new Formula[0];
        init = value;
        end = "";
        sep = "";
    }
    public Formula(String op, String value, Formula f) {
        this.op = op;
        sons = new Formula[]{f};
        init = value;
        end = "";
        sep = "";
    }
    public Formula(String op, String value, Formula left, Formula right) {
        this.op = op;
        sons = new Formula[]{left, right};
        init = "";
        end = "";
        sep = value;
    }

    String getVarValue() {
        return op;
    }
    String getOp() {
        return op;
    }
    public Formula get(int i) {
        return sons[i];
    }
    public Formula get(boolean b) {
        return sons[b == LEFT ? 0 : 1];
    }

    // A split with not box;
    public Formula[] cleanSplit() {
        int count = 0;
        for (Formula son : sons) {
            if (son.isBox()) {
                count++;
            }
        }
        if (count == 0) {
            return sons;
        }
        Formula[] res = new Formula[sons.length - count];
        count = 0;
        for (Formula son : sons) {
            if (!son.isBox()) {
                res[count++] = son;
            }
        }
        return res;
    }
    public Formula[] split() {
        return sons;
    }
    String toStringApply() {
        if (Resources.APPLY.equals(getOp())) {
            return sons[0].toStringApply()  +
                    (Resources.APPLY.equals(sons[0].getOp()) ? "," : "(")
                    + sons[1];
        }
        return toString();
    }
    public String toString() {
        if (Resources.APPLY.equals(getOp())) {
            return toStringApply() + ")";
        }
        int precedence = precedence();
        int sonPrecedence;
        String res = init;
        for (int i = 0; i < sons.length - 1; i++) {
            if (i == 0) {
                sonPrecedence = sons[i].leftPrecedence();
            } else {
                sonPrecedence = sons[i].rightPrecedence();
            }
            if (sonPrecedence < precedence
        ) {
                res += "(" + sons[i].toString() + ")" + sep;
            } else {
                res += sons[i].toString() + sep;
            }
        }
        if (sons.length != 0) {
            int i = sons.length - 1;
            if (sons.length == 1) {
                sonPrecedence = sons[i].leftPrecedence();
            } else {
                sonPrecedence = sons[i].rightPrecedence();
            }
            if (sonPrecedence < precedence
        ) {
                res += "(" + sons[i].toString() + ")";
            } else {
                res += sons[i].toString();
            }
        }
        res += end;
        return res;
    }
    public Point maxPoint(int index, Point[] ref) {
        int length = toString().length();
        Point res = null;
        for (Point aRef : ref) {
            if (aRef.x == 0 && aRef.y == length) {
                continue;
            }
            if (aRef.x <= index && index <= aRef.y) {
                if (res == null || (aRef.x <= res.x && res.y <= aRef.y)) {
                    res = aRef;
                }
            }
        }
        return res;
    }
    public int putIndex(int index, Point[] ref, boolean par) {
        Point p = new Point(index, index);
        int precedence = precedence();
        int sonPrecedence;
        if (par) {
            ref[index++] = p;
        }
        for (int i = 0; i < init.length(); i++) {
            ref[index++] = p;
        }
        for (int i = 0; i < sons.length - 1; i++) {
            if (i == 0) {
                sonPrecedence = sons[i].leftPrecedence();
            } else {
                sonPrecedence = sons[i].rightPrecedence();
            }
            if (sonPrecedence < precedence) {
                index = sons[i].putIndex(index, ref, true);
                for (int j = 0; j < sep.length(); j++) {
                    ref[index++] = p;
                }
            } else {
                index = sons[i].putIndex(index, ref, false);
                for (int j = 0; j < sep.length(); j++) {
                    ref[index++] = p;
                }
            }
        }
        if (sons.length != 0) {
            int i = sons.length - 1;
            if (sons.length == 1) {
                sonPrecedence = sons[i].leftPrecedence();
            } else {
                sonPrecedence = sons[i].rightPrecedence();
            }
            if (sonPrecedence < precedence) {
                index = sons[i].putIndex(index, ref, true);
            } else {
                index = sons[i].putIndex(index, ref, false);
            }
        }
        for (int j = 0; j < end.length(); j++) {
            ref[index++] = p;
        }
        if (par) {
            ref[index++] = p;
        }
        p.y = index;
        return index;
    }
    public void collectVars(Vector<Formula> v) {
        if (sons.length == 0) {
            if (!v.contains(this)) {
                v.addElement(this);
            }
            return;
        }
        for (Formula son : sons) {
            son.collectVars(v);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Formula) {
            Formula f = (Formula) o;

            return this.toString().equals(f.toString());
        }
        return false;
    }
    public static Formula makeFalse() {
        return FALSE;
    }
    public boolean isFalse() {
        return FALSE == this;
    }
    public static Formula makeAnd(Formula left, Formula right) {
        if (!(!left.isProp() && !right.isProp())) {
            return null;
        }
        Formula res =
                new Formula(Resources.AND,
                        Resources.getString(Resources.AND), left, right);
        res.setProp();
        return res;
    }
    public boolean isAnd() {
        return Resources.AND.equals(getOp());
    }
    public static Formula makeOr(Formula left, Formula right) {
        if (!(!left.isProp() && !right.isProp())) {
            return null;
        }
        Formula res =
                new Formula(Resources.OR,
                        Resources.getString(Resources.OR), left, right);
        res.setProp();
        return res;
    }
    public boolean isOr() {
        return Resources.OR.equals(getOp());
    }
    public static Formula makeImp(Formula left, Formula right) {
        if (!(!left.isProp() && !right.isProp())) {
            return null;
        }
        Formula res =
                new Formula(Resources.IMP,
                        Resources.getString(Resources.IMP), left, right);
        res.setProp();
        return res;
    }
    public boolean isImp() {
        return Resources.IMP.equals(getOp());
    }
    public static Formula makeNeg(Formula body) {
        if (body.isProp()) {
            return null;
        }
        Formula res =
                new Formula(Resources.NEG,
                        Resources.getString(Resources.NEG), body);
        res.setProp();
        return res;
    }
    public boolean isNeg() {
        return Resources.NEG.equals(getOp());
    }
    private static Formula makeForAll(Formula left, Formula right) {
        if (!(left.isTermVar() && !right.isProp())) {
            return null;
        }
        Formula res =
                new Formula(Resources.FOR_ALL, Resources.QUANTIFIER_SEPARATION, left, right);
        res.init = Resources.getString(Resources.FOR_ALL);
        res.setProp();
        return res;
    }

    private static Formula makeApply(Formula left, Formula right) {
        if (!((left.isVar() ||  left.isApply()) & right.isTerm())) {
            return null;
        }
        Formula res =
                new Formula(Resources.APPLY, " ", left, right);
        res.init = Resources.getString(Resources.APPLY);
        res.setProp();
        res.setType(left.getType());
        return res;
    }
    boolean isApply() {
        return Resources.APPLY.equals(getOp());
    }

    public static Formula makePropVar(String name) {
        Formula res = new Formula(name, name.toUpperCase());
        res.setProp();
        return res;
    }

    public static Formula makeTermVar(String name) {
        Formula res = new Formula(name, name.toLowerCase());
        res.setTerm();
        return res;
    }

    boolean isTermVar() {
        return isTerm() && isVar();
    }
    boolean isVar() {
        return !isFalse() && sons.length == 0;
    }

    boolean isBox() {
        return Resources.BOX.equals(getVarValue());
    }

    public static Formula make(Formula f1, Formula f2, String value) {
        if (Resources.IMP.equals(value)) {
            return makeImp(f1, f2);
        }
        if (Resources.AND.equals(value)) {
            return makeAnd(f1, f2);
        }
        if (Resources.OR.equals(value)) {
            return makeOr(f1, f2);
        }
        if (Resources.FOR_ALL.equals(value)) {
            return makeForAll(f1, f2);
        }
        if (Resources.EXISTS.equals(value)) {
            return makeForAll(f1, f2);
        }
        if (Resources.APPLY.equals(value)) {
            return makeApply(f1, f2);
        }
        return null;
    }
    public static Formula make(Formula f, String value) {
        if (Resources.NEG.equals(value)) {
            return makeNeg(f);
        }
        return null;
    }
    public Formula substBox(Int i, Formula f) {
        if (isBox()) {
            if (i.isNull()) {
                i.decrement();
                return f;
            }
            i.decrement();
            return this;
        }
        Formula[] nsons = new Formula[sons.length];
        for (int j = 0; j < sons.length; j++) {
            nsons[j] = sons[j].substBox(i, f);
        }
        Formula res = new Formula("", "");
        res.sons = nsons;
        res.sep = sep;
        res.init = init;
        res.end = end;
        res.select = select;
        return res;
    }
}