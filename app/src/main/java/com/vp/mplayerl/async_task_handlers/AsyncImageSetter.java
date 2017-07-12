package com.vp.mplayerl.async_task_handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.vp.mplayerl.R;
import com.vp.mplayerl.misc.Track;
import com.vp.mplayerl.misc.TrackAdapter;

/**
 * Created by Ville on 6.7.2017.
 */

public class AsyncImageSetter extends AsyncTask<Void, Void, Bitmap> {

    private TrackAdapter.ViewHolder mViewHolder;
    private TrackAdapter mAdapter;
    private Context mContext;
    private Track mTrack;
    private int mPosition;

    public AsyncImageSetter(TrackAdapter.ViewHolder viewHolder, int position, Context context, Track track, TrackAdapter adapter) {
        mContext = context;
        mTrack = track;
        mPosition = position;
        mViewHolder = viewHolder;
        mAdapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mViewHolder.imageView.setImageResource(R.mipmap.noimagefound);
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        return mTrack.getScaledBitmap(100,100);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null && mViewHolder.position == mPosition) {
            mViewHolder.imageView.setImageBitmap(bitmap);
            mAdapter.putBitmapToArrayAsync(mPosition, bitmap);
        } else {
            mViewHolder.imageView.setImageResource(R.mipmap.noimagefound);
        }
    }
}
