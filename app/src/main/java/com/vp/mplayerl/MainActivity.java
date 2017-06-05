package com.vp.mplayerl;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.vp.mplayerl.misc.TrackAdapter;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSIONS_READ_EXT_STORAGE = 1;
    private final static int PERMISSIONS_WRITE_EXT_STORAGE = 2;

    ListView listView;
    TrackAdapter trackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheckRead = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d("VP", "PERMISSION READ: " + permissionCheckRead);
        if (permissionCheckRead == PermissionChecker.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_READ_EXT_STORAGE);

        }
        if (permissionCheckWrite == PermissionChecker.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_WRITE_EXT_STORAGE);

        }

        trackAdapter = new TrackAdapter(this, getLayoutInflater());

        listView = (ListView) findViewById(R.id.main_listview);
        listView.setAdapter(trackAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        InitalizeTracksOnPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_playback) {
            final Intent intentOpenPlaybackActivity = new Intent(this, PlaybackActivity.class);
            startActivity(intentOpenPlaybackActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File[] PrintFilesFromDirectory(String dir) {
        try {
            File file = new File(dir);
            File[] files = file.listFiles();
            if (files.length == 0) {
                Toast.makeText(this, "DIRECTORY" + dir + " is EMPTY", Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "DIRECTORY" + dir + " is EMPTY");
            } else {
                for (File f : files) {
//                    trackAdapter.addTrack(new Track(f.getName(), f.getPath(), 100));
                    Log.d("DEBUG", "Track added! " + f.getName() + " " + f.getPath());
                }
            }
            return files;
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ERROR", "FROM: " + dir + " -> " + ex.getMessage());
            return null;
        }
    }

    private void InitalizeTracksOnPage() {
        boolean useExtStorage = Utils.isExtStorageReadable();
        if (!useExtStorage) {
            Toast.makeText(this, "Reading from external storage is not possible", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Reading from external storage is possible", Toast.LENGTH_SHORT).show();
        }
        File dev = null;
        dev = new File(Environment.DIRECTORY_MUSIC);
        PrintFilesFromDirectory(Environment.DIRECTORY_MUSIC);
        PrintFilesFromDirectory(Environment.getExternalStorageDirectory().getPath());
        PrintFilesFromDirectory("/storage/9016-4EF8/");
    }
}
