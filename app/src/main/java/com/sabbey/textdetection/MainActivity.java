package com.sabbey.textdetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button button, imagePicker;
    final int CAMERA = 4;
    final int GALLERY = 3;
    FirebaseVisionImage image;
    FirebaseVisionTextRecognizer detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.openCamera);
        imagePicker = findViewById(R.id.imagePicker);

        detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        onCamera();
        onImagePicker();
    }

    public void onCamera(){

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA);
            }
        });
    }

    public void onImagePicker(){

        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            try {
                image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
                detect(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA && resultCode == RESULT_OK)
        {
            Bundle bundle = data.getExtras();
            Bitmap photo = (Bitmap) bundle.get("data");
            image = FirebaseVisionImage.fromBitmap(photo);
            detect(image);


        }
    }

    public void detect(FirebaseVisionImage image){

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {


                        String resultText = firebaseVisionText.getText();
                        if (resultText.isEmpty())
                        {
                            Toast.makeText(MainActivity.this, "No text detected", Toast.LENGTH_SHORT).show();
                        }

                        else
                        {
                            Intent intent = new Intent(getApplicationContext(),Result.class);
                            intent.putExtra("result", resultText);
                            startActivity(intent);
                        }

                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}
