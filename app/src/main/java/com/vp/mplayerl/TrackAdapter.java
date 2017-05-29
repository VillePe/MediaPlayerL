package com.vp.mplayerl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vp.mplayerl.misc.Track;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Ville on 9.10.2016.
 */

public class TrackAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater;
    ArrayList<Track> tracks;

    public TrackAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.layoutInflater = inflater;
        this.tracks = new ArrayList<>();
    }

    public void addTrack(Track track) {
        tracks.add(track);
        notifyDataSetChanged();
    }

    public void clearTracks() {
        tracks.clear();
        notifyDataSetChanged();
    }

    public void removeTrack(int position) {
        tracks.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_track, null);

            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.row_track_img_thumbnail);
            holder.textView_artist = (TextView) convertView.findViewById(R.id.row_track_artist);
            holder.textView_title = (TextView) convertView.findViewById(R.id.row_track_title);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Track track = (Track) getItem(position);

        holder.imageView.setImageResource(R.mipmap.noimagefound);
        holder.textView_artist.setText(track.getArtist());
        holder.textView_title.setText(track.getTitle());

        return convertView;
    }

    public class ViewHolder {
        public TextView textView_title;
        public TextView textView_artist;
        public ImageView imageView;
    }
}
