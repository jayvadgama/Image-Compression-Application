package com.example.projectsquash;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class activityCompress extends AppCompatActivity {

    TextView txtImageSize, txtApproxSize, txtSeekBar;
    Button btnBasicCompression, btnStrongCompression;
    Button btnSave;
    SeekBar seekBarQuality;
    String imageUri;

    Bitmap bitmap, bitmapResized;
    OutputStream fOut;

    private static String filepath;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myCompressor");

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void verifyStoragePermissions(Activity activity) {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_compress);

        this.verifyStoragePermissions(this);

        // Initialize elements
        txtImageSize = findViewById(R.id.txtImageSize);
        txtApproxSize = findViewById(R.id.txtApproxSize);
        btnBasicCompression = findViewById(R.id.btnBasicCompression);
        btnStrongCompression = findViewById(R.id.btnStrongCompression);
        btnSave = findViewById(R.id.btnSave);
        seekBarQuality = findViewById(R.id.seekBarQuality);
        txtSeekBar = findViewById(R.id.txtSeekBar);

        // Retrieve the image URI from Intent extras
        imageUri = getIntent().getStringExtra("imageUri");

        //Retrieve the ImageView from the layout file
        ImageView imgImport = findViewById(R.id.imgImport);

        //Load the image onto the ImageView using Glide
        Glide.with(this).load(Uri.parse(imageUri)).into(imgImport);

        // Get the content resolver
        ContentResolver contentResolver = getContentResolver();

        // Get the file descriptor for the ImageUri
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = contentResolver.openFileDescriptor(Uri.parse(imageUri), "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Get the original file size in bytes from the file descriptor
        long fileSize = parcelFileDescriptor.getStatSize();

        // Close the file descriptor
        try {
            parcelFileDescriptor.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Convert the file size to kilobytes
        float kilobytes = fileSize / 1024f;
        // Convert the file size to megabytes
        float megabytes = fileSize / (1024f * 1024f);

        // Format the file size as a string
        String fileSizeString = String.format("%.2f MB", megabytes);

        // Set the txtImageSize onto drawable, centering the text horizontally and vertically
        txtImageSize.setCompoundDrawablesWithIntrinsicBounds(R.drawable.start_button, 0, 0, 0);
        txtImageSize.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        // Set the txtApproxSize onto drawable, centering the text horizontally and vertically
        txtApproxSize.setCompoundDrawablesWithIntrinsicBounds(R.drawable.start_button, 0, 0, 0);
        txtApproxSize.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        // Set the file size as text on a TextView
        txtImageSize.setText("Size: " + fileSizeString);
        txtApproxSize.setText("Approx: ");

        // Basic Compression compresses an Image by 50% of its actual file size.
        btnBasicCompression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(view.getContext().getContentResolver(), Uri.parse(imageUri)));
                    bitmapResized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth() * 0.8), (int)(bitmap.getHeight() * 0.8), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                getApproxFileSize();
            }
        });

        // Strong Compression compresses an Image by 80% of its actual file size.
        btnStrongCompression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(view.getContext().getContentResolver(), Uri.parse(imageUri)));
                    bitmapResized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth() * 0.3), (int)(bitmap.getHeight() * 0.3), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                getApproxFileSize();
            }
        });

        // SeekBar percentage is what the approximate compression value is set to.
        seekBarQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                txtSeekBar.setText("Custom Quality: " + (i + 1) + "%");

//                // Drop in quality dependent on SeekBar Value
//                String fileSizeFinder = String.format("%.2f MB", megabytes * ((i + 1) / 100.0f));

                try {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(seekBar.getContext().getContentResolver(), Uri.parse(imageUri)));
                    bitmapResized = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth() * ((i + 1) / 100.0f)), (int)(bitmap.getHeight() * ((i + 1) / 100.0f)), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                getApproxFileSize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Save Button to Compress Image and Save it to Gallery
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(activityCompress.this, "Saving Image...", Toast.LENGTH_SHORT).show();

                try {
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File myDir = new File(root + "/myCompressor");
                    myDir.mkdirs();

                    File file = new File (myDir, "compressed_image.jpg");
                    OutputStream fOut = new FileOutputStream(file);

                    // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                    bitmapResized.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush(); // Not really required
                    fOut.close(); // do not forget to close the stream

                } catch (IOException e) {
                    e.printStackTrace();
                }

                goToExport();
                Toast.makeText(activityCompress.this, "Image Compressed & Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToExport() {
        Intent intent = new Intent(this, activityExport.class);
        startActivity(intent);
    }

    private void getApproxFileSize() {
        // Get the dimensions of the image
        int height = bitmapResized.getHeight();
        int width = bitmapResized.getWidth();

        Log.v("myLog2 - H", String.valueOf(bitmapResized.getHeight()));
        Log.v("myLog2 - W", String.valueOf(bitmapResized.getWidth()));

        double imageSize = (height * width);

        // Calculate the bytes per pixel based on the color depth
        // int bytesPerPixel = (int) (bitmapResized.getByteCount() / imageSize);
        double bytesPixel = imageSize * 3;
        Log.v("myLog2 - bps", String.valueOf(bytesPixel));

        // Calculate the file size in bytes
        double fileSizeInMegabytes = (((bytesPixel)/8)/1024)/1024;

        String formattedFileSize = String.format("%.2f", fileSizeInMegabytes);

        // Print the result
        txtApproxSize.setText("Approx: " + formattedFileSize + "MB");
    }
}