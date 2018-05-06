package net.hockeyapp.android;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import com.google.android.gms.location.LocationStatusCodes;
import java.io.File;
import java.io.FileOutputStream;
import net.hockeyapp.android.views.PaintView;

public class PaintActivity extends Activity {
    private static final int MENU_CLEAR_ID = 3;
    private static final int MENU_SAVE_ID = 1;
    private static final int MENU_UNDO_ID = 2;
    private String imageName;
    private PaintView paintView;

    class C02671 implements OnClickListener {
        C02671() {
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    PaintActivity.this.finish();
                    return;
                default:
                    return;
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri imageUri = (Uri) getIntent().getExtras().getParcelable("imageUri");
        this.imageName = determineFilename(imageUri, imageUri.getLastPathSegment());
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        int displayHeight = getResources().getDisplayMetrics().heightPixels;
        int currentOrientation = displayWidth > displayHeight ? 0 : 1;
        int desiredOrientation = PaintView.determineOrientation(getContentResolver(), imageUri);
        setRequestedOrientation(desiredOrientation);
        if (currentOrientation != desiredOrientation) {
            Log.d("HockeyApp", "Image loading skipped because activity will be destroyed for orientation change.");
            return;
        }
        this.paintView = new PaintView(this, imageUri, displayWidth, displayHeight);
        LinearLayout vLayout = new LinearLayout(this);
        vLayout.setLayoutParams(new LayoutParams(-1, -1));
        vLayout.setGravity(17);
        vLayout.setOrientation(1);
        LinearLayout hLayout = new LinearLayout(this);
        hLayout.setLayoutParams(new LayoutParams(-1, -1));
        hLayout.setGravity(17);
        hLayout.setOrientation(0);
        vLayout.addView(hLayout);
        hLayout.addView(this.paintView);
        setContentView(vLayout);
        Toast.makeText(this, Strings.get(Strings.PAINT_INDICATOR_TOAST_ID), LocationStatusCodes.GEOFENCE_NOT_AVAILABLE).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, Strings.get(Strings.PAINT_MENU_SAVE_ID));
        menu.add(0, 2, 0, Strings.get(Strings.PAINT_MENU_UNDO_ID));
        menu.add(0, 3, 0, Strings.get(Strings.PAINT_MENU_CLEAR_ID));
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                makeResult();
                return true;
            case 2:
                this.paintView.undo();
                return true;
            case 3:
                this.paintView.clearImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 || this.paintView.isClear()) {
            return super.onKeyDown(keyCode, event);
        }
        OnClickListener dialogClickListener = new C02671();
        new Builder(this).setMessage(Strings.get(Strings.PAINT_DIALOG_MESSAGE_ID)).setPositiveButton(Strings.get(Strings.PAINT_DIALOG_POSITIVE_BUTTON_ID), dialogClickListener).setNegativeButton(Strings.get(Strings.PAINT_DIALOG_NEGATIVE_BUTTON_ID), dialogClickListener).show();
        return true;
    }

    private void makeResult() {
        File hockeyAppCache = new File(getCacheDir(), "HockeyApp");
        hockeyAppCache.mkdir();
        File result = new File(hockeyAppCache, this.imageName + ".jpg");
        int suffix = 1;
        while (result.exists()) {
            result = new File(hockeyAppCache, this.imageName + "_" + suffix + ".jpg");
            suffix++;
        }
        this.paintView.setDrawingCacheEnabled(true);
        final Bitmap bitmap = this.paintView.getDrawingCache();
        new AsyncTask<File, Void, Void>() {
            protected Void doInBackground(File... args) {
                try {
                    FileOutputStream out = new FileOutputStream(args[0]);
                    bitmap.compress(CompressFormat.JPEG, 100, out);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("HockeyApp", "Could not save image.", e);
                }
                return null;
            }
        }.execute(new File[]{result});
        Intent intent = new Intent();
        intent.putExtra("imageUri", Uri.fromFile(result));
        if (getParent() == null) {
            setResult(-1, intent);
        } else {
            getParent().setResult(-1, intent);
        }
        finish();
    }

    private String determineFilename(Uri uri, String fallback) {
        String path = null;
        Cursor metaCursor = getApplicationContext().getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    path = metaCursor.getString(0);
                }
                metaCursor.close();
            } catch (Throwable th) {
                metaCursor.close();
            }
        }
        return path == null ? fallback : new File(path).getName();
    }
}
