package com.itc539.malinga.snapeats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button btnCapture;
    ImageView imageView;
    private final static int IMAGE_REQUEST_CODE = 100;
    private final String API_KEY = "AIzaSyCWMxtw-iyAhiMaH8qbulYYrJ7IiNSwZ5I";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.btnCapture);
        imageView = findViewById(R.id.imageView);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitMap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitMap);

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null
        );
        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer(API_KEY)
        );

        Vision vision = visionBuilder.build();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Convert photo to byte array
                try{

                    InputStream inputStream =
                            getResources().openRawResource(R.raw.apple);
                    byte[] photoData = IOUtils.toByteArray(inputStream);
                    inputStream.close();


                } catch (IOException e){
                    Log.e("Error", "run: " + e.getMessage());
                }


                // More code here
            }
        });


        Image inputImage = new Image();
        inputImage.encodeContent(photoData);

        Feature desiredFeature = new Feature();
        //desiredFeature.setType("FACE_DETECTION");
        desiredFeature.setType("TEXT_DETECTION");

        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(desiredFeature));

        BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
        batchRequest.setRequests(Arrays.asList(request));

        BatchAnnotateImagesResponse batchResponse = vision.images().annotate(batchRequest).execute();

        final TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
        Toast.makeText(getApplicationContext(), text.getText(), Toast.LENGTH_LONG).show();
    }
}
