package com.demos.fingerprinttest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class scale_activity extends AppCompatActivity {
    Bitmap bitmap=null;
    int inewWidth=0;
    int iorgW=0;
    int iorgH=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_activity);

        Intent intent = getIntent();
        if(intent==null)
            return;

        String fileName = intent.getStringExtra(CONSTANTS.EXTRA_BITMAP_FILE);
        try {
            bitmap = BitmapFactory.decodeStream(openFileInput(fileName));
        }catch (FileNotFoundException ex){}
        if(bitmap==null)
            return;
        final ImageView imageView=findViewById(R.id.imageView3);
        imageView.setImageBitmap(bitmap);
        iorgW=bitmap.getWidth();
        iorgH=bitmap.getHeight();

        final TextView textView=findViewById(R.id.textViewScaleActivity);

        SeekBar seekBar=findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                float scale = progress/100f;
                Log.d(CONSTANTS.TAG, "Progress= "+progress+", scale= "+scale);
                if(scale<0.1 || scale>2){
                    seekBar.setProgress(100);
                    return;
                }
                int newW= (int)(iorgW * scale);
                inewWidth=newW;
                Log.d(CONSTANTS.TAG, "old / new width = "+iorgW+" / "+inewWidth);
                bitmap=ImageTools.getResizedBitmap(bitmap, inewWidth);
                imageView.setImageBitmap(bitmap);
                textView.setText(inewWidth+" x " + (int)(iorgH*scale));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(CONSTANTS.EXTRA_BITMAP_WIDTH, inewWidth);
        intent.putExtra(CONSTANTS.EXTRA_BITMAP_FILE, saveBitmap(bitmap));
        setResult(RESULT_OK, intent);
        finish();
    }

    void returnWidth(int newWidth){
    }

    public String saveBitmap(Bitmap bitmap) {
        String fileName = "ImageName";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

}
