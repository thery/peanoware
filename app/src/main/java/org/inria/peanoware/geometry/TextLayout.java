package org.inria.peanoware.geometry;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * @author Laurent Théry
 * @date 2/20/15.
 * Text with an associated layout
 */
public class TextLayout {
    private final Paint p;
    private final String s;
    public TextLayout(String s) {
        p = new Paint();
        this.s = s;
    }

    public void setTypeface(Typeface tf) {
        p.setTypeface(tf);
    }

    public void setSize(float sz) {
        p.setTextSize(sz);
    }

    public void draw(Canvas c, int x, int y) {
     c.drawText(s, x, y, p);
    }
    public Rect getBounds() {
        Rect rec = new Rect();
        p.getTextBounds(s, 0, s.length(), rec);
        return rec;
    }
    public Rect getBounds(int i, int j) {
        Rect rec = new Rect();
        float[] fs = new float[i];
        p.getTextWidths(s,0, i, fs);
        float dx = 0;
        for (float f : fs) {
            dx += f;
        }
        p.getTextBounds(s, i, j, rec);
        rec.offset((int)dx, 0);
        return rec;
    }

}

