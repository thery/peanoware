package org.inria.peanoware;

/**
 * @author Laurent Théry
 * @since  2/19/15
 * Natural Deduction Activity
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import org.inria.peanoware.nd.NdView;
import org.inria.peanoware.nd.Pair;

import java.util.Arrays;
import java.util.Random;


public class NdActivity extends ActionBarActivity {
    private static NdView view;
    private static SeekBar seek;
    public static final String PREFS_NAME = "PeanoPref";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nd);
        RelativeLayout rel = (RelativeLayout) findViewById(R.id.container);
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int n = Pair.EXAMPLES.length;
        char[] chars = new char[n];
        Arrays.fill(chars, 'O');
        String s = new String(chars);
        String aF = settings.getString("activeFormulae",s);
        int nF = settings.getInt("numberOfFormulae", n);
        int cI =  settings.getInt("currentIndex", new Random().nextInt(n));
        final NdView rootView = new NdView(this, rel.getContext(), null, aF, nF, cI);

        rootView.post(new Runnable() {
            @Override
            public void run() {
                rootView.init();
                rootView.setActive();
            }
        });
        NdActivity.view = rootView;
        rel.addView(rootView);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }


    @Override
    protected void onStop(){
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("activeFormulae", view.activeFormulae);
        editor.putInt("numberOfFormulae", view.numberOfFormulae);
        editor.putInt("currentIndex", view.currentIndex);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            view.next();
            return true;
        }
        if (id == R.id.action_reset) {
            view.reset();
            view.init();
            return true;
        }
        if (id == R.id.action_size) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(R.string.action_size);

            seek = new SeekBar(this);
            seek.setProgress(Resources.SIZE_FORMULA);
            seek.setMax(Resources.MAX_SIZE_FORMULA);
            alert.setView(seek);
            alert.setPositiveButton(R.string.seek_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    int value = seek.getProgress();
                    Resources.PREFERED_SIZE_FORMULA = value;
                    view.setFontSize(value);
                    if (view.redraw()) {
                        view.invalidate();
                        dialog.cancel();
                    }
                }
            });
            alert.setNegativeButton(R.string.seek_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}