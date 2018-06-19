package com.example.user.splash;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import org.json.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private TextView fanStatus;
    private TextView lightStatus;
    private TextView shelterStatus;
    private Button button;
    private TextToSpeech textToSpeech;
    private TextView busStatus;
    private String[] lightsWords;
    private String[] fanOffWords;
    private String[] fanOnWords;
    private String[] shelterWords;
    private String[] busWords;
    private static final int CAMERA_REQUEST = 50;
    private boolean flashLightStatus = false;
    public String testMain() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=90061")
                .get()
                .addHeader("AccountKey", "vpvO2GtSRmaadVHTh6uc3Q==")
                .addHeader("accept", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "420167da-8bab-4d69-9f68-4dd066658af7")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch(Exception e) { }
        return "";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        fanStatus = findViewById(R.id.fanStatus);
        shelterStatus = findViewById(R.id.shelterStatus);
        lightStatus = findViewById(R.id.lightStatus);
        busStatus = findViewById(R.id.busStatus);
        lightsWords = new String[]{"lights", "light"};
        fanOffWords = new String[]{"cold"};
        fanOnWords = new String[]{"hot", "fans", "warm"};
        shelterWords = new String[]{"raining", "rain", "shelter"};
        busWords = new String[]{"bus"};
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);
        setupTts();
    }
    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
        } catch (CameraAccessException e) {
            System.out.println(e);
        }
    }
    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
        } catch (CameraAccessException e) {
            System.out.println(e);
        }
    }
    public void getSpeechInput (View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        textView.setText("Listening...");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Speech input is unsupported", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final MediaPlayer beepSound = MediaPlayer.create(this, R.raw.beep6);
        final MediaPlayer beepSound2 = MediaPlayer.create(this, R.raw.beep8);
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textView.setText(result.get(0));
                    String[] results = result.get(0).split(" ");
                    for (int i = 0; i < lightsWords.length; i++) {
                        if (Arrays.asList(results).contains(lightsWords[i])) {
                            if (lightStatus.getText() == "Lights: Off") {
                                lightStatus.setText("Lights: On");
                                beepSound.start();
                                startTts("Lights are now on");
                            } else {
                                lightStatus.setText("Lights: Off");
                                beepSound2.start();
                                startTts("Lights are now off");
                            }
                        }
                    }
                    for (int i = 0; i < fanOnWords.length; i++) {
                        if (Arrays.asList(results).contains(fanOnWords[i])) {
                            fanStatus.setText("Fans: On");
                            flashLightOn();
                            startTts("Fans are now on");
                        }
                    }
                    for (int i = 0; i < fanOffWords.length; i++) {
                        if (Arrays.asList(results).contains(fanOffWords[i])) {
                            fanStatus.setText("Fans: Off");
                            flashLightOff();
                            startTts("Fans are now off");
                        }
                    }
                    for (int i = 0; i < shelterWords.length; i++) {
                        if (Arrays.asList(results).contains(shelterWords[i])) {
                            if (shelterStatus.getText() == "Shelter: Off") {
                                shelterStatus.setText("Shelter: On");
                                beepSound.start();
                                flashLightOn();
                                startTts("Shelter is now extended");
                            } else {
                                shelterStatus.setText("Shelter: Off");
                                beepSound2.start();
                                flashLightOff();
                                startTts("Shelter is no longer extended");
                            }
                        }
                    }
                    for (int i = 0; i < busWords.length; i++) {
                        if (Arrays.asList(results).contains(busWords[i])) {
                            try {
                                busStatus.setText("Here we go");
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url("http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=90061")
                                        .get()
                                        .addHeader("AccountKey", "vpvO2GtSRmaadVHTh6uc3Q==")
                                        .addHeader("accept", "application/json")
                                        .addHeader("Cache-Control", "no-cache")
                                        .addHeader("Postman-Token", "420167da-8bab-4d69-9f68-4dd066658af7")
                                        .build();
                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {
                                        e.printStackTrace();
                                    }
                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        try (ResponseBody responseBody = response.body()) {
                                            if (!response.isSuccessful())
                                                throw new IOException("Unexpected code " + response);
                                            final String a = responseBody.string();
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //Handle UI here
                                                    JSONObject nextBus2 = reader.getJSONObjectx(“nextBus2”);
                                                    String estArrival = nextBus2.getString(“EstimatedArrival”);
                                                    //DateFormat inputDF = DateFormat.getDateInstance("'Bus 158 arriving on' yyyy/MM/dd 'on' EEE, 'at' HH:mm:ss");
                                                    SimpleDateFormat format = new SimpleDateFormat("'Bus 158 arriving on' yyyy/MM/dd 'on' EEE, 'at' HH:mm:ss");
                                                    //Date myDate = inputDF.parse(estArrival);
                                                    Date date = format.parse(estArrival);
                                                    //DateFormat outputDF = DateFormat.getDateInstance("'Bus 158 arriving on' yyyy/MM/dd 'on' EEE, 'at' HH:mm:ss");
                                                    //String timeStr = outputDF.format(myDate);
                                                    ((TextView) findViewById(R.id.busStatus)).setText(date);
                                                    //((TextView) findViewById(R.id.busStatus)).setText(a);
                                                }
                                            });
                                        }
                                    }
                                });
                            } catch (Exception ex){ }
                            break;
                        }
                    }
                }
                break;
        }
    }
    private void setupTts() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.ENGLISH);
                textToSpeech.setSpeechRate(1.0f);
            }
        });
    }
    public void startTts(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}