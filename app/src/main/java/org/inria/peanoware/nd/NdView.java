package org.inria.peanoware.nd;

/**
 * @author Laurent Théry
 * @date 2/21/15.
 * View for Natural deduction
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.inria.peanoware.NdActivity;
import org.inria.peanoware.R;
import org.inria.peanoware.formula.Formula;
import org.inria.peanoware.geometry.Dimension;
import org.inria.peanoware.geometry.Visible;
import org.inria.peanoware.Resources;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

@SuppressWarnings("UnusedAssignment")
public class NdView extends View {
    private static final Random rand = new Random();
    private NdActivity nd;
    public String activeFormulae ;
    public int numberOfFormulae;
    public int currentIndex;
    public boolean bravo;
    private static int DELTA = 10;
    private final Pair pair;
    private Tree selectedFrom;
    private final ModPoint modPointFrom;
    private Tree selectedTo;
    private final ModPoint modPointTo;
    private Point drag;
    private final ModPoint mp;
    private Tree currentTree;
    private Tree attachTree;
    private final Vector<Tree> attachVector;
    private final Paint paint;
    private final AlertDialog.Builder aD;
    private boolean configurationChanged;
    private SoundPool soundPool;
    private boolean loaded;
    private int soundID1;
    private int soundID2;
    private int soundID3;
    private AudioManager audioManager;

    public NdView(NdActivity nD, Context context, AttributeSet attrs,
                  String aF, int nF, int cI) {
        super(context, attrs);
        audioManager =
                (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
        soundID1 = soundPool.load(context, R.raw.sound1, 1);
        soundID2 = soundPool.load(context, R.raw.sound2, 1);
        soundID3 = soundPool.load(context, R.raw.sound3, 1);
        nd = nD;
        activeFormulae = aF;
        numberOfFormulae = nF;
        currentIndex = cI;
        if (nF == 0 || (aF.length() != Pair.EXAMPLES.length)) {
            reset();
        }
        nd.setTitle("Peanoware" + "   " +
                (Pair.EXAMPLES.length - numberOfFormulae) + "/" + Pair.EXAMPLES.length);

        aD = new AlertDialog.Builder(context);
        paint = new Paint();

        paint.setAntiAlias(true);
        paint.setStrokeWidth(6f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        pair = new Pair(currentIndex);
        attachVector = new Vector<>();
        modPointFrom = new ModPoint();
        modPointTo = new ModPoint();
        mp = new ModPoint();
        configurationChanged = false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configurationChanged = true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (configurationChanged) {
            configurationChanged = false;
            setFontSize(Resources.PREFERED_SIZE_FORMULA);
            redraw();
        }
        paintComponent(canvas, paint);
    }
    private void playSound(int sid) {
        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        if (loaded) {
            soundPool.play(sid, volume, volume, 1, 0, 1f);
        }
    }
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();
        if (bravo) {
            return true;
        }
        switch (maskedAction) {

            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN: {
                Tree tree = pair.findRoot((int) event.getX(), (int) event.getY());
                if (tree != null && tree != getCurrentTree()) {
                    setCurrentTree(tree);
                    tree = pair.inside((int) event.getX(), (int) event.getY(), mp);
                    playSound(soundID1);
                    return true;
                }
                mp.set(0, 0, Tree.A_NONE);
                setSelectFrom(pair.inside((int) event.getX(), (int) event.getY(), mp), mp);
                setAttach(tree);
                redraw();
                invalidate();

                return true;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                mp.set(0, 0, Tree.A_NONE);
                setSelectTo(pair.inside((int)event.getX(), (int)event.getY(), mp), mp);
                setDragPoint((int)event.getX(), (int)event.getY());

                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP: {
                commitMerge();
                unSelectFrom();
                invalidate();
                return true;
            }

            case MotionEvent.ACTION_CANCEL: {
                // TODO use data
                break;
            }
        }

        // Schedules a repaint.
        invalidate();
        return true;
    }
    void setSelectFrom(Tree f, ModPoint p) {
        //System.out.println("sel " + f);
        //System.out.println("Mod " + p.getMod());
        selectedFrom = f;
        modPointFrom.set(p);
        selectedTo = null;
        drag = null;
    }
    void unSelectFrom() {
        selectedFrom = null;
        selectedTo = null;
        drag = null;
    }

    void setSelectTo(Tree f, ModPoint p) {
        if (selectedFrom != null) {
            selectedTo = f;
            modPointTo.set(p);
        }
    }

    void setDragPoint(int x, int y) {
        if (drag == null) {
            drag = new Point(x, y);
            return;
        }
        drag.x = x;
        drag.y = y;
    }

    void setCurrentTree(Tree tree) {
        if (currentTree != tree) {
            currentTree = tree;
            invalidate();
        }
    }
    Tree getCurrentTree() {
        return currentTree;
    }

    boolean isUp(Tree tree) {
        Tree root = tree.getRoot();
        if (root == pair.getMain()) {
            return true;
        }
        while (root.getArity() != 0) {
            if (root == tree) {
                return false;
            }
            root = root.get(0);
        }
        return root != selectedFrom;
    }

    /** Process the request of build **/
    void commitMerge() {
        //System.out.println("MERGE");
        //System.out.println(modPointFrom.getMod());
        if (selectedFrom == null) {
            return;
        }
        if (drag != null && selectedTo != selectedFrom) {
            // The user is trying to link two objects
            if (!attachVector.contains(selectedTo)) {
                aD.setMessage(R.string.attach_error);
                aD.show();
                return;
            }
            commitAttach(selectedFrom, selectedTo);
            playSound(soundID1);
            return;
        }
        if (selectedFrom.isClosedLeaf() && selectedFrom.getFather() == null) {
            if (!isInContext(selectedFrom.getConclusion())) {
                pair.removeHyp(selectedFrom);
                setCurrentTree(getMainTree());
                playSound(soundID1);
                invalidate();
                return;
            }
        }
        //System.out.println(isUp(selectedFrom));
        if (isUp(selectedFrom)) {
            /** The user is trying to develop an open leaf **/
            commitMergeUp();
            playSound(soundID1);
            return;
        }
        /** The user is trying to develop a conclusion (elimination rule) **/
        Formula f = selectedFrom.getConclusion();
        Tree father = selectedFrom.getFather();
        if (f.isAnd()) {
            if (father == null) {
                performModification(selectedFrom, selectedFrom.makeAndE(Tree.LEFT));
                playSound(soundID1);
                return;
            }
            if (father.isAndE(Tree.RIGHT)) {
                performModification(selectedFrom.getRoot(), selectedFrom);
                playSound(soundID1);
                return;
            }
            if (father.isAndE(Tree.LEFT)) {
                performModification(selectedFrom.getRoot(),
                        selectedFrom.makeAndE(Tree.RIGHT));
                playSound(soundID1);
                return;
            }
            return;
        }
        if (father != null) {
            performModification(selectedFrom.getRoot(), selectedFrom);
            playSound(soundID1);
            return;
        }
        if (f.isImp()) {
            performModification(selectedFrom, selectedFrom.makeImpE());
            playSound(soundID1);
            return;
        }
        if (f.isNeg()) {
            performModification(selectedFrom, selectedFrom.makeNegE());
            playSound(soundID1);
            return;
        }
        if (attachVector.size() == 1) {
            /** There is nothing else to do that try to attach it with
             the unique point of attachment
             **/
            commitAttach(attachVector.get(0), selectedFrom);
        }

    }
    /** Perform an attachment **/
    void commitAttach(Tree tree1, Tree tree2) {
        Tree up, down;
        if (tree2.getFather() == null) {
            up = tree2;
            down = tree1;
        } else {
            up = tree1;
            down = tree2;
        }
        if (up.getConclusion().equals(down.getConclusion())) {
            performModification(down, up, up);
            playSound(soundID1);
            return;
        }
        if (up.getConclusion().isFalse()) {
            performModification(down, up.makeFalseE(down.getConclusion()), up);
            playSound(soundID1);
            return;
        }
        if (up.getConclusion().isOr()) {
            performModification(down, up.makeOrE(down.getConclusion()), up);
            playSound(soundID1);
        }

    }
    /** Add a new rule on a open conclusion **/
    void commitMergeUp() {
        if (modPointFrom.getMod() == 0) {
            if (selectedFrom.isClosedLeaf()) {
                return;
            }
            /* We are fully selecting a conclusion **/
            Formula f = selectedFrom.getConclusion();
            if (f.isOr()) {
                if (selectedFrom.getArity() == 0) {
                    performModification(selectedFrom, Tree.makeOrI(f, Tree.LEFT));
                    return;
                }
                if (selectedFrom.isOrI(Tree.RIGHT)) {
                    performModification(selectedFrom, Tree.makeOpenLeaf(f));
                    return;
                }
                if (selectedFrom.isOrI(Tree.LEFT)) {
                    performModification(selectedFrom, Tree.makeOrI(f, Tree.RIGHT));
                    return;
                }
                performModification(selectedFrom, Tree.makeOpenLeaf(f));
                return;
            }
            /** If the selected rule has sons, cut them ! **/
            if (selectedFrom.getArity() != 0) {
                performModification(selectedFrom, Tree.makeOpenLeaf(f));
                return;
            }

            if (f.isAnd()) {
                performModification(selectedFrom, Tree.makeAndI(f));
                return;
            }
            if (f.isImp()) {
                performModification(selectedFrom, Tree.makeImpI(f));
                invalidate();
            }
            if (f.isNeg()) {
                performModification(selectedFrom, Tree.makeNegI(f));
                return;
            }
            /** If there is nothing else to do and there is only one possible
             *  attachment, do it!
             **/
            if (attachVector.size() == 1) {
                commitAttach(attachVector.get(0), selectedFrom);
            }
            return;
        }
        Formula f = selectedFrom.getConclusion().get(modPointFrom.getMod() - 1);
        Tree tree = pair.findHyp(f);
        /** If the assumption is already present in the board as a standalone
         *  hypothesis, cancel this assumption
         **/
        if (tree != null && tree.isClosedLeaf()) {
            performModification(tree, false);
            invalidate();
            return;
        }
        /** Otherwise create a copy of the assumption and make it a standalone one
         **/
        performModification(new Tree(f, true, new Tree[]{}));
    }

    /** Activate the proof board */
    public void setActive() {
        bravo = false;
        invalidate();
    }
    /** Disable the proof board */
    void setInactive() {
        numberOfFormulae--;
        activeFormulae = activeFormulae.substring(0, currentIndex) + "X" +
                         activeFormulae.substring(currentIndex + 1);
        if (numberOfFormulae != 0) {
            currentIndex = getNextCurrentIndex();
        }
        bravo = true;
        playSound(soundID2);
        nd.setTitle("Peanoware" + "   " +
                (Pair.EXAMPLES.length - numberOfFormulae) + "/" + Pair.EXAMPLES.length);
        invalidate();
    }

    public void init() {
        pair.init(currentIndex);
        bravo = false;
        centerMainTree();
        setCurrentTree(getMainTree());
    }

    /** Put a new formula **/
    public void next() {
        if ((numberOfFormulae == 0) || (!bravo && (numberOfFormulae == 1))) {
            return;
        }
        if (!bravo) {
            numberOfFormulae--;
            String s = activeFormulae;
            activeFormulae = s.substring(0, currentIndex) + "X" +
                    s.substring(currentIndex + 1);
            currentIndex = getNextCurrentIndex();
            numberOfFormulae++;
            activeFormulae = s;
        }
        init();
    }

    public int getNextCurrentIndex() {
        int n = rand.nextInt(numberOfFormulae);
        int i = 0;
        for (; i < activeFormulae.length(); i++) {
            if (activeFormulae.charAt(i) == 'O') {
                if (n == 0) {
                    return i;
                }
                n--;
            }
        }
        reset();
        return currentIndex;
    }

    /** Put a new formula **/
    public void reset() {
        int n = Pair.EXAMPLES.length;
        char[] chars = new char[n];
        playSound(soundID3);
        Arrays.fill(chars, 'O');
        activeFormulae = new String(chars);
        numberOfFormulae = n;
        currentIndex = rand.nextInt(n);
        nd.setTitle("Peanoware" + "   " +
                (Pair.EXAMPLES.length - numberOfFormulae) + "/" + Pair.EXAMPLES.length);
    }



    /** Check if we have reached the result **/
    void checkWin() {
        if (pair.hasWon()) {
            setInactive();
            invalidate();
        }
    }

    Tree getMainTree() {
        return pair.getMain();
    }

    void centerMainTree() {
        Tree tree = getMainTree();
        tree.setOrigin((getWidth()
                        - tree.getSize().width()) / 2,
                 (getHeight()
                        - tree.getSize().height()) - (2 + Resources.SIZE_FORMULA / 3));
    }

    // Compute the possible attach point
    void setAttach(Tree tree) {
        if (attachTree == tree) {
            return;
        }
        attachTree = tree;
        attachVector.removeAllElements();
        if (tree == null) {
            return;
        }
        if (tree.isOpenLeaf()) {
            boolean isMain = tree.getRoot() == pair.getMain();
            Enumeration en = pair.getHypothesis();
            Tree tmp, root = tree.getRoot();
            Vector<Formula> ctx = new Vector<>();
            Vector<Formula> hCtx = new Vector<>();
            tree.getContext(ctx);
            while (en.hasMoreElements()) {
                tmp = (Tree) en.nextElement();
                if (tmp == root) {
                    continue;
                }
                if (!tmp.getConclusion().equals(tree.getConclusion())) {
                    if (! (tmp.getConclusion().isOr()
                            || tmp.getConclusion().isFalse())) {
                        continue;
                    }
                }
                if (isMain) {
                    hCtx.removeAllElements();
                    tmp.getHypothesis(hCtx);
                    if (ctx.containsAll(hCtx)) {
                        attachVector.addElement(tmp);
                    }
                } else {
                    attachVector.addElement(tmp);
                }
            }
            invalidate();
            return;
        }
        if (tree.isConclusion() && tree != pair.getMain()) {
            Vector<Formula> ctx = new Vector<>();
            tree.getHypothesis(ctx);
            processHypothesis(pair.getMain(), tree.getConclusion(), attachVector, ctx);
            Enumeration en = pair.getHypothesis();
            Tree tmp;
            while (en.hasMoreElements()) {
                tmp = (Tree) en.nextElement();
                if (tmp != tree) {
                    processHypothesis(tmp, tree.getConclusion(), attachVector);
                }
            }
            invalidate();
            return;
        }
        invalidate();
    }
    /* Check all the possibility for attachment */
    private static void processHypothesis(Tree tree, Formula f,
                                          Vector<Tree> res) {
        Vector<Tree> hCtx = new Vector<>();
        tree.getOpenLeaves(hCtx);
        Enumeration en = hCtx.elements();
        Tree tmp;
        while (en.hasMoreElements()) {
            tmp = (Tree) en.nextElement();
            if (!tmp.getConclusion().equals(f)) {
                if (! (f.isOr() || f.isFalse())) {
                    continue;
                }
            }
            res.addElement(tmp);
        }
    }

    /* Check all the possibility for attachment */
    private static void processHypothesis(Tree tree, Formula f,
                                          Vector<Tree> res, Vector<Formula> ctx) {
        Vector<Tree> hCtx = new Vector<>();
        tree.getOpenLeaves(hCtx);
        Enumeration en = hCtx.elements();
        Tree tmp;
        Vector<Formula> tmpCtx = new Vector<>();
        while (en.hasMoreElements()) {
            tmp = (Tree) en.nextElement();
            if (!tmp.getConclusion().equals(f)) {
                if (! (f.isOr() || f.isFalse())) {
                    continue;
                }
            }
            tmpCtx.removeAllElements();
            tmp.getContext(tmpCtx);
            if (tmpCtx.containsAll(ctx)) {
                res.addElement(tmp);
            }
        }
    }
    /* Check all the possibility for attachment */
    private boolean isInContext(Formula f) {
        if (pair.getMain().isInContext(f)) {
            return true;
        }
        Enumeration en = pair.getHypothesis();
        while (en.hasMoreElements()) {
            if (((Tree) en.nextElement()).isInContext(f)) {
                return true;
            }
        }
        return false;
    }

    /** Remove a stand-alone assumption **/
    void performModification(Tree tree, boolean b) {
        if (b) {
            // Add
            pair.addHyp(tree);
            setCurrentTree(tree);
            if (!redraw()) {
                pair.removeHyp(tree);
            }
            return;
        }
        //Remove
        pair.removeHyp(tree);
        invalidate();
        checkWin();
    }

    /** Remove a stand-alone assumption **/
    void performModification(Tree tree) {
        performModification(tree, true);
    }
    /** Substitute **/
    void performModification(Tree from, Tree to) {
        performModification(from, to, null);
    }
    /** Perform a modification if it is not possible do nothing **/
    void performModification(Tree from, Tree to, Tree del) {

        Point locFrom = new Point(from.getOrigin()), locDel = new Point();
        boolean changeMain = false;
        boolean addOrElimination = false;
        int indexFrom = 0, indexDel = 0;
        if (del != null) {
            locDel = new Point(del.getOrigin());
            indexDel = pair.getIndex(del);
            //System.out.println("Index del " + indexDel);
            pair.removeHyp(del);
        }
        to.setOrigin(locFrom);
        Tree father = from.getFather();
        if (from == pair.getMain()) {
            // we change the main tree
            pair.setMain(to);
            setCurrentTree(to);
            changeMain = true;
        } else if ((from.father == to.father)
                || (from.getRoot() != pair.getMain()
                && from.getRoot() == to.getRoot())) {
            // We are in the case where we have added or removed an elimination rule
            indexFrom = pair.getIndex(from);
            //System.out.println("Index from " + indexFrom);
            pair.removeHyp(from);
            pair.addHyp(to, indexFrom);
            addOrElimination = true;
            setCurrentTree(to);
        } else {
            // we add a new tree to an open leaf
            father.replace(from, to);
            setCurrentTree(father.getRoot());
        }
        if (redraw()) {
            // Everything when fine ww can check if it is a win
            checkWin();
            return;
        }
        // Something went wrong we have to undo the change
        from.setOrigin(locFrom);
        if (changeMain) {
            // we change the main tree
            pair.setMain(from);
        } else if (addOrElimination) {
            pair.removeHyp(to);
            pair.addHyp(from, indexFrom);
        } else {
            // we add a new tree to an open leaf
            father.replace(to, from);
        }
        if (del != null) {
            del.setOrigin(locDel);
            pair.addHyp(del,indexDel);
        }
    }

    /** Recompute the layout **/
    public boolean tryRedraw() {
        Tree tree = getMainTree();
        Rect r = tree.getSize();
        Rect r1 = new Rect(r.left - 2 * DELTA, r.top - 2 * DELTA,
                r.right + 2 * DELTA, r.bottom + 2 * DELTA);
        Visible vis = new Visible(new Dimension(getWidth(), getHeight()));
        Rect mainRec = vis.bestChoice(r1);
        if (mainRec == null) {
            // aD.setMessage(R.string.alert_size);
            // aD.show();
            return false;
        }
        mainRec.offsetTo((getWidth() - tree.getWidth()) / 2,
                (getHeight() - tree.getHeight()) - (2 + Resources.SIZE_FORMULA / 3));
        vis.remove(mainRec);
        Enumeration en = pair.getHypothesis();
        Rect[] recs = new Rect[pair.numberOfHypothesis()];
        Tree tmp;
        for (int i = 0; i < recs.length; i++) {
            tmp = (Tree) en.nextElement();
            recs[i] = vis.bestChoice(tmp.getSize());
            if (recs[i] == null) {
                // aD.setMessage(R.string.alert_size);
                // aD.show();
                return false;
            }
            vis.remove(recs[i]);
        }
        en = pair.getHypothesis();
        getMainTree().update(mainRec);
        for (Rect rec : recs) {
            tmp = (Tree) en.nextElement();
            tmp.update(rec);
        }
        invalidate();
        return true;
    }

    public boolean redraw() {
        while ((Resources.SIZE_FORMULA > 0) && !tryRedraw() ) {
            Resources.SIZE_FORMULA--;
            pair.setFontSize(Resources.SIZE_FORMULA);
        }
        return Resources.SIZE_FORMULA > 0;
    }

    void paintComponent(Canvas c, Paint p) {
        if (bravo) {
            p.setColor(Color.RED);
            p.setStyle(Paint.Style.STROKE);
            pair.draw(c, p);
            return;
        }
        pair.draw(c, p, currentTree, attachVector);
        if (selectedFrom != null) {
            if (selectedTo != null) {
                selectedFrom.drawArc(c, p, modPointFrom,
                        selectedTo, modPointTo,
                        Resources.ARC);
            } else if (drag != null) {
                selectedFrom.drawArc(c, p, modPointFrom,
                        drag, Resources.ARC);
            }
        }
        if (selectedFrom != null) {
            c.translate(modPointFrom.getX(), modPointFrom.getY());
            selectedFrom.drawConclusion(c, p, modPointFrom.getMod());
            c.translate(-modPointFrom.getX(), -modPointFrom.getY());
        }
    }
    public void setFontSize(float sz) {
        Resources.SIZE_FORMULA=(int)sz;
        pair.setFontSize(sz);
    }
}
