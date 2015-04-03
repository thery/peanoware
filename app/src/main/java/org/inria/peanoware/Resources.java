package org.inria.peanoware;

import android.graphics.Color;
import android.graphics.Typeface;
/**
 *
 * @author  Laurent Théry
 * @date  2/19/15.
 */

public class Resources {
    public static final String AND = "And";
    public static final String OR = "Or";
    public static final String IMP = "Imp";
    public static final String NEG = "Neg";
    public static final String FOR_ALL = "ForAll";
    public static final String EXISTS = "Exist";
    public static final String APPLY = "Apply";
    public static final String QUANTIFIER_SEPARATION = ".";
    public static final int HYP = Color.rgb(149, 192, 214) ;
    public static final int TO = Color.rgb(255,122,115);
    public static final int ARC = Color.rgb(0, 163, 0);
    public static final String[] BUILD_PROPOSITION_VARIABLE = {"A", "B", "C", "D"};

    public static final Typeface FONT_FORMULA =
            Typeface.create(Typeface.SERIF, Typeface.NORMAL);
  //          Typeface.create(Typeface.SERIF, Typeface.NORMAL);
    public static int SIZE_FORMULA = 35;
    public static int MAX_SIZE_FORMULA = 70;
    private static final String COPY = "Copy";
    private static final String SPLIT = "Dissolve";
    private static final String[] COMMAND = {COPY, SPLIT, AND, OR, IMP, NEG, FOR_ALL, EXISTS, APPLY};
    public static final String FALSE = "\u22A5";
    public static final String BOX = "\u25A1";
    private static final String[] INFO =
            { null, null, " \u2227 ", " \u2228 ", " \u21d2 ", "\u00ac", "\u2200", "\u2203", ""};
    private static final int[] ARITY = {1, 1, 2, 2, 2, 1, 2, 2, 2};
    private static final int[] PRECEDENCE = {1, 1, 30, 20, 10, 40, 7, 7, 60};
    private static final int[] LEFT_PRECEDENCE = {1, 1, 29, 19, 9, 41, 8, 8, 60};
    private static final int[] RIGHT_PRECEDENCE = {1, 1, 31, 21, 11, 40, 8, 8, 60};
    public static final int BOX_COLOR = Color.rgb(0, 163, 0);

    public static String getString(String s) {
        for (int i = 0; i < COMMAND.length; i++) {
            if (s.equals(COMMAND[i])) {
                return INFO[i];
            }
        }
        return null;
    }

    public static int getArity(String s) {
        for (int i = 0; i < COMMAND.length; i++) {
            if (s.equals(COMMAND[i])) {
                return ARITY[i];
            }
        }
        return -1;
    }
    public static int getPrecedence(String s) {
        for (int i = 0; i < COMMAND.length; i++) {
            if (s.equals(COMMAND[i])) {
                return PRECEDENCE[i];
            }
        }
        return 60;
    }
    public static int getLeftPrecedence(String s) {
        for (int i = 0; i < COMMAND.length; i++) {
            if (s.equals(COMMAND[i])) {
                return LEFT_PRECEDENCE[i];
            }
        }
        return 60;
    }
    public static int getRightPrecedence(String s) {
        for (int i = 0; i < COMMAND.length; i++) {
            if (s.equals(COMMAND[i])) {
                return RIGHT_PRECEDENCE[i];
            }
        }
        return 60;
    }
}