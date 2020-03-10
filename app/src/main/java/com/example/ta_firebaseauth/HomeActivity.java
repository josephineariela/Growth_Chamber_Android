package com.example.ta_firebaseauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    //Initialize variables
    private ImageView imageViewLogout, imageViewProfile, imageViewHistory, imageViewHome;
    private ImageView tempLogo, humLogo, moistLogo, lightLogo;
    private TextView OptTemp, OptHum, OptMoist, StartLight, EndLight;
    private TextView ActTemp, ActHum, ActMoist;
    private TextView tvControl;
    private Button buttonPublishTemp, buttonPublishHum, buttonPublishMoist, buttonPublishLight;
    private EditText editTextOptTemp, editTextOptHum, editTextOptMoist, editTextStartLight;

    private int tempState, humidState, moistState, lightState;
    private int tempAct, humidAct, moistAct, lightStart;
    private int tempOpt, humidOpt, moistOpt, lightEnd;


    //Initialize Firebase Authentication & Database
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    //Initialize MQTT Client
    MqttAndroidClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getTimeDate();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        findID();

        //Configuration Server MQTT
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://tailor.cloudmqtt.com:14131", clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("hhaqsitb");
        options.setPassword("MP7TFv0i040Q".toCharArray());



        //MQTT Connection test
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(HomeActivity.this, "Let's Start Farming!", Toast.LENGTH_LONG).show();
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(HomeActivity.this, "Check your connection!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        //Write subscribed messages on home activity
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals("ActTemp")){
                    //ActTemp.setText("Actual       : " + new String(message.getPayload()) + " C");

                    ActTemp.setText("Actual      : " + new String(message.getPayload()) + " C");
                    tempAct = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("ActHum")){
                    ActHum.setText("Actual      : " + new String(message.getPayload())+ " %RH");
                    humidAct = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("ActMoist")){
                    ActMoist.setText("Actual      : " + new String(message.getPayload()) + " %");
                    moistAct = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("OptTemp")){
                    //OptTemp.setText("Optimum  : " + new String(message.getPayload()) + " C");
                    OptTemp.setText("Optimum : " + new String(message.getPayload()) + " C");
                    tempOpt = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("OptHum")){
                    OptHum.setText("Optimum : " + new String(message.getPayload())+ " %RH");
                    humidOpt = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("OptMoist")){
                    OptMoist.setText("Optimum  : " + new String(message.getPayload())+ " %");
                    moistOpt = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("StartLight")){
                    StartLight.setText("Start Time :" + new String(message.getPayload()));
                    lightStart = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("EndLight")){
                    EndLight.setText("End Time   : " + new String(message.getPayload()));
                    lightEnd = Integer.parseInt(new String(message.getPayload()));
                }
                else if(topic.equals("tempState")){
                    tempState = Integer.parseInt(new String(message.getPayload()));
                    temperatureLogo();
                }
                else if(topic.equals("humidState")){
                    humidState = Integer.parseInt(new String(message.getPayload()));
                    humidityLogo();
                }
                else if(topic.equals("moistState")){
                    moistState = Integer.parseInt(new String(message.getPayload()));
                    moistureLogo();
                }
                else if(topic.equals("light")){
                    lightState= Integer.parseInt(new String(message.getPayload()));
                    lightLogo();
                }
                //else if(topic.equals("light")){
                //    statusLight.setText("Status : " + new String(message.getPayload()));
                //}
                //else if(topic.equals("update")){
                //    update.setText("Plant Image (Updated " + new String(message.getPayload())+ " Minutes Ago");
                //}
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        setOnClick();
    }



    //LoginActivity will open if user account is NULL
    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    //Function to subscribe a topic from CloudMQTT
    private void subscribeToTopic(){
        try {
            client.subscribe("ActTemp",1);
            client.subscribe("ActHum",1);
            client.subscribe("ActMoist",1);
            client.subscribe("OptHum",1);
            client.subscribe("OptTemp",1);
            client.subscribe("OptMoist",1);
            client.subscribe("StartLight",1);
            client.subscribe("EndLight",1);
            client.subscribe("Light",1);
            client.subscribe("Update",1);
            client.subscribe("tempState",1);
            client.subscribe("humidState",1);
            client.subscribe("moistState",1);
            client.subscribe("lightState",1);
        }
        catch(MqttException e){
            e.printStackTrace();
        }
    }

    private void temperatureLogo(){
        int HEAT= 0;
        int WAIT_TEMP = 1;
        int COOL = 2;
        int TEMP_THRESHOLD = 1;

        if (tempState == HEAT){
            tempLogo.setImageDrawable(getResources().getDrawable(R.drawable.temperature_heater_on));
            //mungkin perhitungan ini dilakukan di ESP32 aja gausah disini juga
            //jadi yang diterima android cuma status parameter saja gausa ikut bandingin nilai aktual&optimalnya
            if (tempAct >= tempOpt) {
                tempState = WAIT_TEMP;
            }
        }
        else if (tempState == WAIT_TEMP){
            tempLogo.setImageDrawable(getResources().getDrawable(R.drawable.temperature));
            if((tempOpt - tempAct)>= TEMP_THRESHOLD){
                tempState = HEAT;
            }
            else if((tempAct - tempOpt)>= TEMP_THRESHOLD){
                tempState = COOL;
            }
        }
        else if (tempState == COOL) {
            tempLogo.setImageDrawable(getResources().getDrawable(R.drawable.temperature_cooler_on));
            if (tempAct <= tempAct){
                tempState = WAIT_TEMP;
            }
        }
    }

    private void humidityLogo(){
        int HUMID = 0;
        int WAIT_HUMID = 1;
        int DEHUMID = 2;
        int HUMID_THRESHOLD = 1;

        if (humidState == HUMID){
            humLogo.setImageDrawable(getResources().getDrawable(R.drawable.humidity_on));
            if (humidAct >= humidOpt) {
                humidState = WAIT_HUMID;
            }
        }
        else if (humidState == WAIT_HUMID){
            humLogo.setImageDrawable(getResources().getDrawable(R.drawable.humidity));
            if((humidOpt - humidAct)>= HUMID_THRESHOLD){
                humidState = HUMID;
            }
            else if((humidAct - humidOpt)>= HUMID_THRESHOLD){
                humidState = DEHUMID;
            }
        }
        else if (humidState == DEHUMID) {
            humLogo.setImageDrawable(getResources().getDrawable(R.drawable.dehumidity_on));
            if (humidAct <= humidOpt){
                humidState = WAIT_HUMID;
            }
        }
    }

    private void moistureLogo(){
        int PUMP_ON = 0;
        int WAIT_ON = 1;
        int WAIT_MOIST = 2;

        if (moistState == PUMP_ON){
            moistLogo.setImageDrawable(getResources().getDrawable(R.drawable.soilhumidity_on));
            moistState = WAIT_ON;
        }
        else if (moistState == WAIT_ON){
            moistLogo.setImageDrawable(getResources().getDrawable(R.drawable.soilhumidity));
            moistState = WAIT_MOIST;
        }
        else if (moistState == WAIT_MOIST) {
            moistLogo.setImageDrawable(getResources().getDrawable(R.drawable.soilhumidity));
            moistState = PUMP_ON;
        }
    }

    //BELOM BERHASIL
    private void lightLogo(){
        int LIGHT_OFF = 0;
        int LIGHT_ON = 1;
        if (lightState == LIGHT_OFF) {
            lightLogo.setImageDrawable(getResources().getDrawable(R.drawable.light_button));
            lightState = LIGHT_ON;
        }
        else if (lightState == LIGHT_ON) {
            lightLogo.setImageDrawable(getResources().getDrawable(R.drawable.light_button_on));
            lightState = LIGHT_OFF;
        }
    }

    private void publishOptTemp() {
        String OptTemp = editTextOptTemp.getText().toString().trim();
        String topic = "OptTemp";
        String payload;

        Toast.makeText(this, "Optimum Temperature is set!", Toast.LENGTH_SHORT).show();
        byte[] encodedPayload = new byte[0];
        try {
            payload = OptTemp;
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
        editTextOptTemp.getText().clear();
        /*
        if (!OptTemp.isEmpty()) {
            editTextOptTemp.setVisibility(View.GONE);
        }
        */
    }

    private void publishOptHum() {
        String OptHum = editTextOptHum.getText().toString().trim();
        String topic = "OptHum";
        String payload;

        Toast.makeText(this, "Optimum Humidity is set!", Toast.LENGTH_SHORT).show();
        byte[] encodedPayload = new byte[0];
        try {
            payload = OptHum;
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
        editTextOptHum.getText().clear();
        /*
        if (!OptHum.isEmpty()) {
            editTextOptHum.setVisibility(View.GONE);
        }
        */
    }

    private void publishOptMoist() {
        String OptMoist = editTextOptMoist.getText().toString().trim();
        String topic = "OptMoist";
        String payload;

        Toast.makeText(this, "Optimum Soil Moisture is set!", Toast.LENGTH_SHORT).show();
        byte[] encodedPayload = new byte[0];
        try {
            payload = OptMoist;
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
        editTextOptMoist.getText().clear();
        /*
        if (!OptMoist.isEmpty()) {
            editTextOptMoist.setVisibility(View.GONE);
        }
        */
    }

    private void publishStartLight() {
        String startLight = editTextStartLight.getText().toString().trim();
        String topic = "StartLight";
        String payload;

        Toast.makeText(this, "Start Light Time is set!", Toast.LENGTH_SHORT).show();
        byte[] encodedPayload = new byte[0];
        try {
            payload = startLight;
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
        editTextStartLight.getText().clear();
        /*
        if (!startLight.isEmpty()) {
            editTextStartLight.setVisibility(View.GONE);
        }
        */
    }

    //Display current date and time
    private void getTimeDate(){
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    while (!isInterrupted()){
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView hdate = (TextView) findViewById(R.id.hDate);
                                long date = System.currentTimeMillis();
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
                                String dataString = sdf.format(date);
                                hdate.setText(dataString);
                            }
                        });
                    }
                } catch (InterruptedException e){

                }
            }
        };
        t.start();
    }

    private void findID(){
        //Syncing XML variables
        imageViewLogout = (ImageView) findViewById(R.id.imageViewLogout);
        imageViewProfile = (ImageView) findViewById(R.id.imageViewProfile);
        imageViewHistory = (ImageView) findViewById(R.id.imageViewHistory);
        imageViewHome = (ImageView) findViewById(R.id.imageViewHome);
        //tempLogo = (ImageView) findViewById(R.id.imageViewTempLOGO);
        tempLogo = (ImageView) findViewById(R.id.imageViewTempLOGO);
        humLogo = (ImageView) findViewById(R.id.imageViewHumLOGO);
        moistLogo = (ImageView) findViewById(R.id.imageViewMoistLOGO);
        lightLogo = (ImageView) findViewById(R.id.imageViewLightLOGO);
        OptTemp = (TextView) findViewById(R.id.textViewOptTemp);
        OptHum = (TextView) findViewById(R.id.textViewOptHum);
        OptMoist = (TextView) findViewById(R.id.textViewOptMoist);
        StartLight = (TextView) findViewById(R.id.textViewStartLight);;
        EndLight = (TextView) findViewById(R.id.textViewEndLight);
        ActTemp = (TextView) findViewById(R.id.textViewActTemp);
        ActHum = (TextView) findViewById(R.id.textViewActHum);
        ActMoist = (TextView) findViewById(R.id.textViewActMoist);
        tvControl = (TextView) findViewById(R.id.tvControl);
        buttonPublishTemp = (Button) findViewById(R.id.buttonPublishTemp);
        buttonPublishHum = (Button) findViewById(R.id.buttonPublishHum);
        buttonPublishMoist = (Button) findViewById(R.id.buttonPublishMoist);
        buttonPublishLight = (Button) findViewById(R.id.buttonPublishLight);
        editTextOptTemp = (EditText) findViewById(R.id.editTextOptTemp);
        editTextOptHum = (EditText) findViewById(R.id.editTextOptHum);
        editTextOptMoist = (EditText) findViewById(R.id.editTextOptMoist);
        editTextStartLight = (EditText) findViewById(R.id.editTextStartLight);
    }

    private void setOnClick(){
        //Initialize button to do a function or open an activity
        imageViewLogout.setOnClickListener(this);
        imageViewProfile.setOnClickListener(this);
        imageViewHistory.setOnClickListener(this);
        imageViewHome.setOnClickListener(this);
        buttonPublishTemp.setOnClickListener(this);
        buttonPublishHum.setOnClickListener(this);
        buttonPublishMoist.setOnClickListener(this);
        buttonPublishLight.setOnClickListener(this);
        tvControl.setOnClickListener(this);

    }

    //Function when imageView is clicked
    @Override
    public void onClick(View v) {
        if (v == buttonPublishTemp){
            publishOptTemp();
        }
        if (v == buttonPublishHum){
            publishOptHum();
        }
        if (v == buttonPublishMoist){
            publishOptMoist();
        }
        if (v == buttonPublishLight){
            publishStartLight();
        }
        if (v == imageViewLogout){
            //account logged out
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (v == imageViewProfile){
            //will open profile activity
            startActivity(new Intent(this, ProfileActivity.class));
        }
        if (v == imageViewHistory){
            // will open history activity
            startActivity(new Intent(this, HistoryActivity.class));
        }
        if (v == imageViewHome){
            startActivity(new Intent(this, HomeActivity.class));
        }
        if (v == tvControl){
            startActivity(new Intent(this, HomeManualActivity.class));
        }
    }


}
