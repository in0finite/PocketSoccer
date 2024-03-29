package com.example.pocketsoccer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {

    public static int randomInt(int min, int max) {
        return min + (int) Math.round( Math.random() * (max - min) );
    }

    public static byte[] readFile(File file) throws IOException {
        byte[] data = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();
        return data;
    }

    public static void writeToFile(File file, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    public static MediaPlayer playSound(MediaPlayer mediaPlayer, int soundResId, Context context) {

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(context, soundResId);
        mediaPlayer.start();

        return mediaPlayer;
    }

    public static MediaPlayer playSameSound(MediaPlayer mediaPlayer, int soundResId, Context context) {

        //return Util.playSound(mediaPlayer, soundResId, context);

        if (null == mediaPlayer) {
            mediaPlayer = MediaPlayer.create(context, soundResId);
            mediaPlayer.start();
        } else {

//            if (mediaPlayer.isPlaying())
//                mediaPlayer.seekTo(0);
//            else
//                mediaPlayer.start();


//            mediaPlayer.reset();
//            try {
//                mediaPlayer.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mediaPlayer.start();

            mediaPlayer.seekTo(0);
            mediaPlayer.start();

        }

        return mediaPlayer;
    }

    public static Bitmap getBitmapClippedCircle(Bitmap bitmap, int width, int height) {

        //final int width = bitmap.getWidth();
        //final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float)(width / 2)
                , (float)(height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, width, height), null);
        return outputBitmap;
    }

}
