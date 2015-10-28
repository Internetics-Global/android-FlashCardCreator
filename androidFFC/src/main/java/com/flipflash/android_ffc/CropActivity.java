package com.flipflash.android_ffc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.flipflash.util.UIHelper;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by BourneWang on 27/10/2015.
 */
public class CropActivity extends Activity {

    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_edit);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Uri imageUri = (Uri) bundle.get("uri");

        cropImageView = (CropImageView)findViewById(R.id.cropImageView);
        cropImageView.setCropMode(CropImageView.CropMode.RATIO_FREE);

        // Set image for cropping
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            cropImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crop_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_crop_edit_save: {
                Bitmap bitmap = cropImageView.getCroppedBitmap();

                String path = Environment.getExternalStorageDirectory().toString();
                File filename = new File(path, "cropped_cached.jpg");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(filename);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                Uri uri = Uri.fromFile(filename);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("cropped_image_uri",uri);

                setResult(Activity.RESULT_OK,returnIntent);
                finish();

                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
