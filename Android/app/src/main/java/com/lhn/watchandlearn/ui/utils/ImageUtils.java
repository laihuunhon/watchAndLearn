package com.lhn.watchandlearn.ui.utils;

import android.annotation.SuppressLint;
import android.graphics.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

/**
 * Copyright (c) 2012 Blurr, LLC. All rights reserved.
 *
 * @author ak
 */
@SuppressLint("DefaultLocale")
public class ImageUtils {
    public static boolean isImageUrl(String aImageUrl) {
        final String loweredUrl = aImageUrl.toLowerCase();
        return (loweredUrl.startsWith("http") || loweredUrl.startsWith("https")) && (isJpeg(loweredUrl) || isPng(loweredUrl) || isTiff(loweredUrl) || isGif(loweredUrl));
    }

    public static boolean isJpeg(String aImageUrl) {
        String lowered = aImageUrl.toLowerCase();
        return lowered.contains(".jpg") || lowered.contains(".jpeg");
    }

    public static boolean isGif(String aImageUrl) {
        return aImageUrl.toLowerCase().endsWith(".gif");
    }

    public static boolean isTiff(String aImageUrl) {
        return aImageUrl.toLowerCase().endsWith(".tiff");
    }

    public static boolean isPng(String aImageUrl) {
        return aImageUrl.toLowerCase().endsWith(".png");
    }

    public static void unbindDrawables(View view){
        if(view.getBackground() != null){
            view.getBackground().setCallback(null);
        }
        if(view instanceof ImageView){
            ((ImageView) view).setImageDrawable(null);
        }
        if(view instanceof ViewGroup){
            for(int i = 0; i < ((ViewGroup) view).getChildCount(); i++){
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            if(!(view instanceof AdapterView)){
                ((ViewGroup) view).removeAllViews();
            }
        }
    }

    public static Bitmap cropCircle(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                                            bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                          bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

//        float scale = Math.max((float)aSize/(float)bitmap.getWidth(),
//                               (float)aSize/(float)bitmap.getHeight());
//
//        canvas.scale(scale, scale);
        return output;
    }
}
