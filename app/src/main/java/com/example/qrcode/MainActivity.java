package com.example.qrcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import yuku.ambilwarna.AmbilWarnaDialog;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int QR_CODE_WIDTH = 1000;
    private static final int QR_CODE_HEIGHT = 1000;
    private static final int BAR_CODE_WIDTH = 2000;
    private static final int BAR_CODE_HEIGHT = 700;
    private static int FOREGROUND_COLOR = Color.parseColor("#000000");
    private static int BACKGROUND_COLOR = Color.parseColor("#ffffff");

    private EditText editText;
    private TextView result;
    private ImageView img;

    private boolean IS_QR = false;
    private String resultString = "";
    private Bitmap bmp;
    private boolean hasBeenPaused = false;
    static private String InputText = "";

    static boolean isBitmapShowing = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (hasBeenPaused) {
            //onPause was called
            //May be because of home button press, recents opened, another activity opened or a call etc...
        } else {
            //this is a call just after the activity was created
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hasBeenPaused = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        result = findViewById(R.id.textViewResult);
        img = findViewById(R.id.imageView);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }


        if (savedInstanceState != null) {
            IS_QR = savedInstanceState.getBoolean("type");
            bmp = savedInstanceState.getParcelable("bitmap");
            FOREGROUND_COLOR = savedInstanceState.getInt("foreground");
            BACKGROUND_COLOR = savedInstanceState.getInt("background");
//            Log.d(TAG, "onCreate: "+InputText);
            if(isBitmapShowing){
                if(IS_QR){
                    QrBitmap();
                } else {
                    BarBitmap();
                }
                img.setImageBitmap(bmp);
            }
//            Log.d(TAG, "onCreate: "+isBitmapShowing);
            resultString = savedInstanceState.getString("result");
            if (resultString != null && !resultString.isEmpty()) {
                result.setText("Result: " + resultString);
                Linkify.addLinks(result, Linkify.ALL);
            }
        }
    }

    MultiFormatWriter writer = new MultiFormatWriter();

    public void generateBarCode(View view) {
        InputText = editText.getText().toString().trim();
        if (InputText.isEmpty()) {
            Toast.makeText(this, "Please Enter Some Text", Toast.LENGTH_SHORT).show();
            return;
        }
        BarBitmap();
        img.setImageBitmap(bmp);
        IS_QR = false;
    }

    private void BarBitmap() {
        try {
            BitMatrix bitMatrix = writer.encode(InputText, BarcodeFormat.CODE_128, BAR_CODE_WIDTH, BAR_CODE_HEIGHT, null);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bmp = barcodeEncoder.createBitmap(bitMatrix);

            isBitmapShowing = true;
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void generateQrCode(View view) {

        InputText = editText.getText().toString().trim();
        if (InputText.isEmpty()) {
            Toast.makeText(this, "Please Enter Some Text", Toast.LENGTH_SHORT).show();
            return;
        }
        QrBitmap();
        img.setImageBitmap(bmp);
        IS_QR = true;
    }

    private void QrBitmap() {
        try {
            BitMatrix bitMatrix = writer.encode(InputText, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bmp = barcodeEncoder.createBitmap(bitMatrix);

            isBitmapShowing = true;
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public void changeForegroundColour(View view) {
        if(isBitmapShowing) {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, FOREGROUND_COLOR, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    //   Toast.makeText(MainActivity.this, String.valueOf(Color.valueOf(color)), Toast.LENGTH_LONG).show();
                    img.setImageResource(0);
                    if (IS_QR) {
                        QrBitmap();
                    } else {
                        BarBitmap();
                    }
                    bmp = changeColour(bmp, FOREGROUND_COLOR, color, BACKGROUND_COLOR);
                    img.setImageBitmap(bmp);
                    //      FOREGROUND_COLOR = color;

                }
            });
            colorPicker.show();
        } else {
            Toast.makeText(MainActivity.this,"Generate QR or Bar Code to change foreground color!",Toast.LENGTH_SHORT).show();
        }
    }

    public void changeBackgroundColour(View view) {
        if(isBitmapShowing) {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, BACKGROUND_COLOR, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    //   Toast.makeText(MainActivity.this, String.valueOf(Color.valueOf(color)), Toast.LENGTH_LONG).show();
                    img.setImageResource(0);
                    if (IS_QR) {
                        QrBitmap();
                    } else {
                        BarBitmap();
                    }
                    bmp = changeColour(bmp, BACKGROUND_COLOR, color, FOREGROUND_COLOR);
                    img.setImageBitmap(bmp);
                    //  BACKGROUND_COLOR = color;

                }
            });
            colorPicker.show();
        } else {
            Toast.makeText(MainActivity.this,"Generate QR or Bar Code to change background color!",Toast.LENGTH_SHORT).show();
        }

    }

    private Bitmap changeColour(Bitmap scrBitmap, int referencecolour, int newcolour, int constantcolour) {
        Bitmap newBitmap = Bitmap.createBitmap(scrBitmap);
        for (int i = 0; i < scrBitmap.getWidth(); i++) {
            for (int j = 0; j < scrBitmap.getHeight(); j++) {
                int pixel = newBitmap.getPixel(i, j);
                if (Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel)) == referencecolour) {
                    newBitmap.setPixel(i, j, newcolour);
                } else {
                    newBitmap.setPixel(i, j, constantcolour);
                }
            }
        }
        return newBitmap;
    }

    public void copyToClipboard(View view) {
        if (resultString.isEmpty()) {
            Toast.makeText(MainActivity.this, "Scan First", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Text Copied", resultString);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("type", IS_QR);
        outState.putString("result", resultString);
// don't uncomment the below line as the size for parcelable is too long
//        outState.putParcelable("bitmap", bmp);
        outState.putInt("foreground", FOREGROUND_COLOR);
        outState.putInt("background", BACKGROUND_COLOR);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult.getContents() != null) {
            resultString = intentResult.getContents();
            result.setText("Result: " + resultString);
            Linkify.addLinks(result, Linkify.ALL);
/*
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            builder.setMessage(intentResult.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
*/
        } else {
            Toast.makeText(this, "Scan Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void scan(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("For Flash Use Volume Up Key");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setCaptureActivity(Capture.class);
        intentIntegrator.initiateScan();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_image:
                saveToGallery(item.getActionView());
                return true;
            case R.id.clear:
                clear(item.getActionView());
                return true;
            case R.id.forecolour:
                changeForegroundColour(item.getActionView());
                return true;
            case R.id.backcolour:
                changeBackgroundColour(item.getActionView());
                return true;
            case R.id.copy:
                copyToClipboard(item.getActionView());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void clear(View view) {
        editText.setText("");
        img.setImageResource(0);
        isBitmapShowing = false;
        result.setText("");
        FOREGROUND_COLOR = Color.parseColor("#000000");
        BACKGROUND_COLOR = Color.parseColor("#ffffff");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void saveToGallery(View view) {
        if(isBitmapShowing) {

            File dir = new File(Environment.getExternalStorageDirectory() + "/QRCode");
            if(!dir.exists()) {
                boolean success = dir.mkdir();
            }
            String type;
            if (IS_QR) {
                type = "QRCODE";
            } else {
                type = "BARCODE";
            }
            String filename = String.format("%s.jpg", type + "_" + editText.getText().toString().trim().substring(0, 5) + "_" + System.currentTimeMillis());

            File outfile = new File(dir.getAbsolutePath()+"/"+filename);
            Toast.makeText(this, "Saved at: " + outfile.getPath(), Toast.LENGTH_LONG).show();
            try {
                FileOutputStream outputStream = new FileOutputStream(outfile);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Cannot Save", Toast.LENGTH_SHORT).show();
        }
    }

}