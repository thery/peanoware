package org.inria.peanoware.nd;

/**
 * Created by thery on 2/19/15.
 * A point with a modality
 */
class ModPoint {
    private int x;
    private int y;
    private int mod;

    /** Creates a new instance of ModPoint */
    public ModPoint() {
        x = 0;
        y = 0;
        mod = 0;
    }
    public void set(ModPoint p) {
        set(p.x, p.y, p.mod);
    }
    public void set(int x, int y, int mod) {
        this.x = x;
        this.y = y;
        this.mod = mod;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public void setMod(int mod) {
        this.mod = mod;
    }
    public int getMod() {
        return mod;
    }
    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }

}
