package com.vp.mplayerl.async_task_handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.vp.mplayerl.R;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.Track;

/**
 * Created by Ville on 6.7.2017.
 */

public class AsyncImageSetter extends AsyncTask<Void, Void, Bitmap> {

    private ImageView mImageView;
    private Context mContext;
    private Track mTrack;

    public AsyncImageSetter(ImageView imageView, Context context, Track track) {
        mImageView = imageView;
        mContext = context;
        mTrack = track;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        return mTrack.getScaledBitmap(100,100);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setImageResource(R.mipmap.noimagefound);
        }
    }
}
