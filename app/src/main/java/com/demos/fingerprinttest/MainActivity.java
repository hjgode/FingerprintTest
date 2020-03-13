package com.demos.fingerprinttest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.askjeffreyliu.floydsteinbergdithering.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    Bitmap bitmap=null;
    Bitmap bitmap2=null;
    ImageView imageView;
    ImageView imageView2;
    TextView txtInfo;
    NumberPicker numberPicker;
    int iWidthOrg=0;
    int iHeightOrg=0;
    TextView textViewWHnew;

    Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtInfo=findViewById(R.id.txtInfo);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);

        textViewWHnew=findViewById(R.id.textViewWidthHeightNewImage);

        SeekBar seekBarTreshold=findViewById(R.id.seekBarTreshold);
        seekBarTreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                ColorMatrix matrix = new ColorMatrix( new float[]{
                        85f, 85f, 85f, 0f, (progress - 255)*255f,
                        85f, 85f, 85f, 0f, (progress - 255)*255f,
                        85f, 85f, 85f, 0f, (progress - 255)*255f,
                        0f, 0f, 0f, 1f, 0f});

                if(bitmap2!=null)
                    bitmap2.recycle();
                bitmap2=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
                Canvas canvas=new Canvas(bitmap2);
                Paint paint=new Paint();
                paint.setColorFilter(new ColorMatrixColorFilter(matrix));
                canvas.drawBitmap(bitmap, 0,0,paint);
                imageView2.setImageBitmap(bitmap2);


//                imageView2.setColorFilter(new ColorMatrixColorFilter(matrix));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    int getBWimage(int preset){
        setUserInput(""+preset);
        // Set an EditText view to get user input
        final int result;
        //alert.setView(input);
        // load the dialog_promt_user.xml layout and inflate to view
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        View promptUserView = layoutinflater.inflate(R.layout.dialog_prompt_user, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setView(promptUserView);

        final EditText userAnswer = (EditText) promptUserView.findViewById(R.id.userinput);
        userAnswer.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertDialogBuilder.setTitle("Please define treshold for B&W conversion");

        // prompt for input
        alertDialogBuilder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // and display the username on main activity layout
                setUserInput(userAnswer.getText().toString());
                try {
                    int treshold = Integer.parseInt(userAnswer.getText().toString());
                    if (treshold > 0 && treshold < 255) {
                        imageView2.setImageBitmap(ImageTools.toMonochrome(bitmap, treshold));
                    }
                }catch(Exception ex){}
            }
        });

        // all set and time to build and show up!
        AlertDialog alertDialog = alertDialogBuilder.create();
//        userAnswer.setText(preset);
        alertDialog.show();
        int iRes=preset;
        try{
            iRes=Integer.parseInt(userInput);
        }catch(Exception ex){}
        return iRes;
    }
    String userInput="";
    void setUserInput(String s){
        userInput=s;
    }

    public void pickImage(View View) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CONSTANTS.REQUEST_CODE);
    }

    Bitmap getImageViewBitmap(ImageView imgView){
        Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String filename="";
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == CONSTANTS.REQUEST_CODE) {
                // We need to recyle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                // The result data contains a URI for the document or directory that
                // the user selected.
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                    //filename = uri.getPath();
                    bitmap = ImageTools.decodeSampledBitmapFromFile(context, uri, imageView.getWidth(), imageView.getHeight());
                    imageView.setImageBitmap(bitmap);
                    imageView2.setImageBitmap(bitmap);
                    Size size = ImageTools.getSize(bitmap);
                    iWidthOrg = size.getWidth();
                    iHeightOrg = size.getHeight();
                    txtInfo.setText(size.getWidth() + "x" + size.getHeight());
                }
            } else if (requestCode == CONSTANTS.REQUEST_CODE_GETSCALE) {
                final int newWidth = data.getIntExtra(CONSTANTS.EXTRA_BITMAP_WIDTH, iWidthOrg);
                //load scaled image
                String fileName = data.getStringExtra(CONSTANTS.EXTRA_BITMAP_FILE);
                try {
                    bitmap2 = BitmapFactory.decodeStream(openFileInput(fileName));
                }catch (FileNotFoundException ex){}
                if(bitmap2==null)
                    return;
                imageView2.setImageBitmap(bitmap2); //ImageTools.getResizedBitmap(bitmap2, newWidth);

                textViewWHnew.setText(""+newWidth); //otherwise crash as looks for a resource id newWidth
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_load:
                View view=findViewById(R.id.action_load);
                pickImage(view);
                return true;
            case R.id.action_print:
                return true;
            case R.id.action_scale:
                Intent intent = new Intent(this, scale_activity.class);
                intent.putExtra(CONSTANTS.EXTRA_BITMAP_FILE, saveBitmap(getImageViewBitmap(imageView2)));
                startActivityForResult(intent, CONSTANTS.REQUEST_CODE_GETSCALE);
                return true;
            case R.id.action_get_RLL:
                byte[] bytes=ImageTools.getBitmapAsRLL(bitmap2);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
