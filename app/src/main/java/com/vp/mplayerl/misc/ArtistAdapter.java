package com.vp.mplayerl.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vp.mplayerl.R;

import java.util.ArrayList;

/**
 * Created by Ville on 9.10.2016.
 */

public class ArtistAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater;
    ArrayList<Artist> artists;

    public ArtistAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.layoutInflater = inflater;
        this.artists = new ArrayList<>();
    }

    public void addArtist(Artist artist) {
        artists.add(artist);
        notifyDataSetChanged();
    }

    public void clearTracks() {
        artists.clear();
        notifyDataSetChanged();
    }

    public void removeTrack(int position) {
        artists.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int position) {
        return artists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.row_artist, null);

            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.row_artist_img_thumbnail);
            holder.textView_artist = (TextView) convertView.findViewById(R.id.row_artist_artist);
            holder.textView_album_count = (TextView) convertView.findViewById(R.id.row_artist_album_count);
            holder.textView_track_count = (TextView) convertView.findViewById(R.id.row_artist_track_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Artist artist = (Artist) getItem(position);

        holder.imageView.setImageResource(R.mipmap.noimagefound);
        holder.textView_artist.setText(CutLongStrings(artist.getName(), 25));
        holder.textView_album_count.setText(CutLongStrings("Albumeja: " + artist.getAlbums().size(), 25));
        holder.textView_track_count.setText(CutLongStrings("Kappaleita: " + artist.getTracks().size(), 25));
        return convertView;
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

    public class ViewHolder {
        public TextView textView_artist;
        public TextView textView_album_count;
        public TextView textView_track_count;
        public ImageView imageView;
    }
}
