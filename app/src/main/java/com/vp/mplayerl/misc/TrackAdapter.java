package com.vp.mplayerl.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vp.mplayerl.R;
import com.vp.mplayerl.Utils;
import com.vp.mplayerl.async_task_handlers.AsyncImageSetter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Ville on 9.10.2016.
 */

public class TrackAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Track> mTracks;
    private final SparseArray<Bitmap> mBitmapSparseArray = new SparseArray<>();

    public TrackAdapter(Context context, LayoutInflater inflater) {
        this.mContext = context;
        this.mLayoutInflater = inflater;
        this.mTracks = new ArrayList<>();
    }

    public void addTrack(final Track track) {
        mTracks.add(track);
        notifyDataSetChanged();
    }

    public void clearTracks() {
        mTracks.clear();
        notifyDataSetChanged();
    }

    public void removeTrack(int position) {
        mTracks.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTracks.size();
    }

    @Override
    public Object getItem(int position) {
        return mTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.row_track, null);

            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.row_track_img_thumbnail);
            holder.textView_artist = (TextView) convertView.findViewById(R.id.row_track_artist);
            holder.textView_title = (TextView) convertView.findViewById(R.id.row_track_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Track track = (Track) getItem(position);
        if (mBitmapSparseArray.get(position) != null) {
            Bitmap bitmap = getBitmapFromArrayAsync(position);
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(R.mipmap.noimagefound);
            }
        } else {
            AsyncImageSetter imageSetter = new AsyncImageSetter(holder, position, mContext, track, this);
            imageSetter.execute();
        }
        holder.position = position;
        holder.textView_artist.setText(CutLongStrings(track.getArtist(), 25));
        holder.textView_title.setText(CutLongStrings(track.getTitle(), 25));
        return convertView;
    }

    public void putBitmapToArrayAsync(int pos, Bitmap bMap) {
        if (this.mBitmapSparseArray.get(pos) != null) return;
        synchronized (mBitmapSparseArray) {
            this.mBitmapSparseArray.put(pos, bMap);
        }
    }

    public Bitmap getBitmapFromArrayAsync(int pos) {
        synchronized (mBitmapSparseArray) {
            return mBitmapSparseArray.get(pos);
        }
    }

    public void fillWithArtistsList(ArrayList<Artist> artists) {
        ArrayList<Track> tracksSorted = new ArrayList<>();
        for (Artist a : artists) {
            for (Track t : a.getTracks()) {
                tracksSorted.add(t);
            }
        }
        Collections.sort(tracksSorted, new Utils.TrackComparator());
        for (Track t : tracksSorted) {
            addTrack(t);
        }
    }

    public static void putTrackImagesAsync(final TrackAdapter adapter) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Track> tracks = adapter.getTracks();
                for (int i = 0; i < tracks.size(); i++) {
                    adapter.putBitmapToArrayAsync(adapter.getTracks().indexOf(tracks.get(i)), tracks.get(i).getScaledBitmap(100, 100));
                }
            }
        });
        thread.start();
    }

    private String CutLongStrings(String string, int length) {
        if (string == null) {
            return "NULL";
        }
        if (string.length() > length) {
            return string.substring(0, length) + "...";
        } else {
            return string;
        }
    }

    public ArrayList<Track> getTracks() {
        return mTracks;
    }

    public class ViewHolder {
        public int position;
        public TextView textView_title;
        public TextView textView_artist;
        public ImageView imageView;
    }
}
