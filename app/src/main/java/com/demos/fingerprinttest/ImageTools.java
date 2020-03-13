package com.demos.fingerprinttest;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Size;

import androidx.annotation.ColorInt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageTools {

    public static Size getSize(Bitmap bitmap){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Size size = new Size(bitmap.getWidth(), bitmap.getHeight());
        return size;
    }

    private static int calculateInSampleSize(Context context, Uri uri, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {

            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            //BitmapFactory.decodeFile(filename, options);// decodeResource(getResources(), R.id.myimage, options);
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            parcelFileDescriptor.close();
        }catch(FileNotFoundException ex){}
        catch (Exception ex){}

        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(Context context, Uri uri, int reqWidth, int reqHeight) {
        FileDescriptor fileDescriptor=null;
        ParcelFileDescriptor parcelFileDescriptor=null;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        }
        catch (FileNotFoundException ex){}
        catch (Exception ex){}finally {}

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        //BitmapFactory.decodeFile(filename, options);// decodeResource(res, resId, options);

        // Calculate inSampleSize
        //options.inSampleSize = calculateInSampleSize(filename, reqWidth, reqHeight);
        options.inSampleSize = calculateInSampleSize(context, uri, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap=BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);// BitmapFactory.decodeFile(filename, options);// decodeResource(res, resId, options);
        try {
            parcelFileDescriptor.close();
        }catch(IOException ex){}
        return bitmap;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
//        bmpOriginal.recycle();
        return bmpGrayscale;
    }


    public static Bitmap toMonochrome(Bitmap bmpOriginal, int treshold)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);

        //ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
/*
          [ a, b, c, d, e,
            f, g, h, i, j,
            k, l, m, n, o,
            p, q, r, s, t ]
        Then the new color [R’G'B'A'] applied to the image is a mathematic operation on the original color [RGBA] as below:
        R’ = a*R + b*G + c*B + d*A + e;
        G’ = f*R + g*G + h*B + i*A + j;
        B’ = k*R + l*G + m*B + n*A + o;
        A’ = p*R + q*G + r*B + s*A + t;
*/
        
        // Setup the float array.
        float[] mx1 = {
                -1.0f, 0, 0, 0, 255,  // red
                0, -1.0f, 0, 0, 255,  // green
                0, 0, -1.0f, 0, 255,  // blue
                0, 0, 0, -1.0f, 0     // alpha
        };
        int weight=treshold; //=85 //ie 127*255/3 =>
        float[] mx ={
                treshold, treshold, treshold, 0, -(treshold)*255,
                treshold, treshold, treshold, 0, -(treshold)*255,
                treshold, treshold, treshold, 0, -(treshold)*255,
                0, 0, 0, 1, 0
        };
        //The reason I use 85 is because of this: 85*3=255. I just want to make it balance with 128*255 in the formula.

        paint.setColorFilter(new ColorMatrixColorFilter(mx));

        c.drawBitmap(bmpOriginal, 0, 0, paint);
        //bmpOriginal.recycle();
        return bmpGrayscale;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scale = ((float) newWidth) / width;

/*
            int newHeight=width/newWidth*height;
            float scaleHeight = ((float) newHeight) / height;
*/
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scale, scale);

            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(
                    bm, 0, 0, width, height, matrix, false);
            //bm.recycle();
            return resizedBitmap;
    }

    static byte[] getBitmapBytes(Bitmap bmp){
        int w = bmp.getWidth();
        int h=bmp.getHeight();
        byte[] bytes=new byte[w*h];
        int[] pixels = new int[w]; //preserve one row (width)

        for (int row = 0; row < h; row++) {
            bmp.getPixels(pixels, 0, w, 0, row, w, 1);
            for (int column = 0; column < w; column++) {
                //only not pure white
                if ((pixels[column] & 0x00FFFFFF) != 0x00FFFFFF) {
                    //Log.d(TAG, "analyseImage - found object pixel: " + pixels[hPos] + ", " + hPos + ", " + vPos);
                    bytes[row * column]=(byte)0x01;
                }else{
                    bytes[row * column]=(byte)0x00;
                }
            }
        }
        return bytes;
    }

    public static byte[] getBitmapAsRLL(Bitmap bitmap){
        byte[] pixels=getBitmapBytes(bitmap);
        int w=bitmap.getWidth();
        int h=bitmap.getHeight();
        byte[] outRLL=getRLL(pixels, w, h);
        return outRLL;
    }

    static byte[] getRLL(byte[] bInput, int width, int height){
        byte[] bRow=new byte[width];
        int row=0;
        ByteArrayOutputStream bos=new ByteArrayOutputStream();

        try {
            //write header
            bos.write(new byte[]{0x40, 0x02});
            //write width and height, high byte first
            bos.write(new byte[]{(byte)(width>>8), (byte)(width&0xFF), (byte)(height>>8), (byte)(height&0xFF)});
            while (row<height) {
                bRow = Arrays.copyOfRange(bInput, row, row+width);
                bos.write(getRLLrow(bRow, width));
                row++;
            }
        }catch (IOException ex){}
        return bos.toByteArray();
    }

    static byte[] getRLLrow(byte[] bInput, int max){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        int pos=0;
        byte runcount=1;
        byte maxRuncount=127;
        int bBitSet=0;
        try {
            if (bInput[0] == 1) {
                bBitSet = 1;
                bos.write(new byte[]{0x00});
            }
            while(pos<bInput.length){

                if(bInput[pos]==bBitSet){
                    runcount++;
                    if(runcount>maxRuncount){
                        bos.write((byte)runcount);
                        runcount=1;
                        bos.write((byte)0);
                    }
                }else if(bInput[pos]!=bBitSet){
                    //write runcount
                    bos.write((byte)runcount);
                    runcount=1;
                    bBitSet=bInput[pos];
                }
                pos++;
            }
        }catch (IOException ex){

        }
        return bos.toByteArray();
    }
}
