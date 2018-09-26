package com.itc539.malinga.snapeats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    Button btnCapture;
    ImageView imageView;
    Bitmap bitmap;
    TextView responseText;
    private final static int IMAGE_REQUEST_CODE = 100;
    private final String CLOUD_VISION_API_KEY = "AIzaSyCWMxtw-iyAhiMaH8qbulYYrJ7IiNSwZ5I";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.btnCapture);
        imageView = findViewById(R.id.imageView);
        responseText = findViewById(R.id.responseText);

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
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);

            Feature feature = new Feature();
            feature.setType("LABEL_DETECTION");
            feature.setMaxResults(10);
            getNutritionInfo("red apple");
            callCloudVision(bitmap, feature);
        }
    }

    private void callCloudVision(Bitmap bitmap, Feature feature) {
        AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
        annotateImageReq.setFeatures(Arrays.asList(feature));
        annotateImageReq.setImage(getEncodedImage(bitmap));
        final List<AnnotateImageRequest> annotateImageRequests = Arrays.asList(annotateImageReq);

        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();

                    return convertResponseToString(response);
                } catch (GoogleJsonResponseException e) {
                    Log.d("TAG", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d("TAG", "failed to make API request because of other IOException " + e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected String convertResponseToString(BatchAnnotateImagesResponse response){
                String message = "";
                float min, max;
                String finalDesc = "";

                AnnotateImageResponse imageResponses = response.getResponses().get(0);
                List<EntityAnnotation> entityAnnotations;

                entityAnnotations = imageResponses.getLabelAnnotations();
                if (entityAnnotations != null) {
                    for (EntityAnnotation entity : entityAnnotations) {
                        message = message + "    " + entity.getDescription() + " " + entity.getScore();
                        message += "\n";
                    }

                    for (EntityAnnotation entity : entityAnnotations) {
                        float score = entity.getScore();
                        min = max = score;

                        if (score < min) {
                            min = score;
                        }
                        if (score > max) {
                            max = score;
                            finalDesc = entity.getDescription();
                        }
                    }

                    message += finalDesc;

                } else {
                    message = "Nothing Found";
                }

                return message;
            }

            protected void onPostExecute(String result) {
                //responseText.setText(result);

                //imageUploadProgress.setVisibility(View.INVISIBLE);
            }
        }.execute();

        getNutritionInfo("1 large apple");
    }

    private void getNutritionInfo(String text) {
        RequestParams rp = new RequestParams();

        NutritionHttpClient.get(text, rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asd", "this is response : " + response);
                try {
                    responseText.setText(response.toString());
                    JSONObject serverResp = new JSONObject(response.toString());
                    //serverResp.
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline

            }
        });
    }

    @NonNull
    private Image getEncodedImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        // Convert the bitmap to a JPEG
        // Just in case it's a format that Android understands but Cloud Vision
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Base64 encode the JPEG
        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }
}
