package com.example.ta_firebaseauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

public class HomeManualActivity extends AppCompatActivity implements View.OnClickListener{

    //Initialize variables
    private ImageView imageViewLogout, imageViewProfile, imageViewHistory, imageViewHome;
    private ImageView heatLogo, coolLogo, humidLogo, dehumidLogo, moistLogo, lightLogo;
    private TextView ActTemp, ActHum, ActMoist, EndLight;
    private TextView tvControl;

    //Initialize actuator state
    private Integer heatState = 0;
    private Integer lightState = 0;
    private Integer moistState = 0;
    private Integer humidityState = 0;

    //Initialize Firebase Authentication & Database
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    //Initialize MQTT Client
    MqttAndroidClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_manual);

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
                    Toast.makeText(HomeManualActivity.this, "Let's Start Farming!", Toast.LENGTH_LONG).show();
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(HomeManualActivity.this, "Check your connection!", Toast.LENGTH_LONG).show();
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
                }
                else if(topic.equals("ActHum")){
                    ActHum.setText("Actual      : " + new String(message.getPayload())+ " %RH");
                }
                else if(topic.equals("ActMoist")){
                    ActMoist.setText("Actual      : " + new String(message.getPayload()) + " %");
                }
                else if(topic.equals("EndLight")){
                    EndLight.setText("End Time   : " + new String(message.getPayload()));
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
        }
        catch(MqttException e){
            e.printStackTrace();
        }
    }

    //Function for heater activation
    private void heaterActuator() {
        String topic = "heater";
        String payload;
        if(heatState == 0){ //Heater is on
            heatState = 1;
            Toast.makeText(this, "Heater is ON", Toast.LENGTH_SHORT).show();
            heatLogo.setImageDrawable(getResources().getDrawable(R.drawable.heater2_on));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "on";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
        else if (heatState == 1){
            heatState = 0;
            Toast.makeText(this, "Heater is OFF", Toast.LENGTH_SHORT).show();
            heatLogo.setImageDrawable(getResources().getDrawable(R.drawable.heater2));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "off";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }

    //Function for cooler activation
    private void coolerActuator() {
        String topic = "cooler";
        String payload;
        if(heatState == 0){
            heatState = 1;
            Toast.makeText(this, "Cooler is ON", Toast.LENGTH_SHORT).show();
            coolLogo.setImageDrawable(getResources().getDrawable(R.drawable.cooler2_on));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "on";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
        else if (heatState == 1){
            heatState = 0;
            Toast.makeText(this, "Cooler is OFF", Toast.LENGTH_SHORT).show();
            coolLogo.setImageDrawable(getResources().getDrawable(R.drawable.cooler2));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "off";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }

    //Function for humidifier activation
    private void humidifierActuator() {
        String topic = "humidifier";
        String payload;
        if(humidityState == 0){
            humidityState = 1;
            Toast.makeText(this, "Humidifier is ON", Toast.LENGTH_SHORT).show();
            humidLogo.setImageDrawable(getResources().getDrawable(R.drawable.humidifier_on));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "on";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
        else if (humidityState == 1){
            humidityState = 0;
            Toast.makeText(this, "Humidifier is OFF", Toast.LENGTH_SHORT).show();
            humidLogo.setImageDrawable(getResources().getDrawable(R.drawable.humidifier));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "off";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }

    //Function for dehumidifier activation
    private void dehumidifierActuator() {
        String topic = "dehumidifier";
        String payload;
        if(humidityState == 0){
            humidityState = 1;
            Toast.makeText(this, "Dehumidifier is ON", Toast.LENGTH_SHORT).show();
            dehumidLogo.setImageDrawable(getResources().getDrawable(R.drawable.dehumidifier_on));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "on";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
        else if (humidityState == 1){
            humidityState = 0;
            Toast.makeText(this, "Dehumidifier is OFF", Toast.LENGTH_SHORT).show();
            dehumidLogo.setImageDrawable(getResources().getDrawable(R.drawable.dehumidifier));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "off";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }

    //Function for water pump activation
    private void micropumpActuator() {
        String topic = "moisture";
        String payload;
        if(moistState == 0){
            moistState = 1;
            Toast.makeText(this, "Water Pump is ON", Toast.LENGTH_SHORT).show();
            moistLogo.setImageDrawable(getResources().getDrawable(R.drawable.pump_on));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "on";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
        else if (moistState == 1){
            moistState = 0;
            Toast.makeText(this, "Water Pump is OFF", Toast.LENGTH_SHORT).show();
            moistLogo.setImageDrawable(getResources().getDrawable(R.drawable.pump));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "off";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }

    }

    //Function for grow lights activation
    private void lightActuator() {
        String topic = "light";
        String payload;
        if(lightState == 0){
            lightState = 1;
            Toast.makeText(this, "Light is ON", Toast.LENGTH_SHORT).show();
            lightLogo.setImageDrawable(getResources().getDrawable(R.drawable.light_button_on));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "on";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
        else if (lightState == 1){
            lightState = 0;
            Toast.makeText(this, "Light is OFF", Toast.LENGTH_SHORT).show();
            lightLogo.setImageDrawable(getResources().getDrawable(R.drawable.light_button));
            byte[] encodedPayload = new byte[0];
            try {
                payload = "off";
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
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
        heatLogo = (ImageView) findViewById(R.id.ivHeater);
        coolLogo = (ImageView) findViewById(R.id.ivCooler);
        humidLogo = (ImageView) findViewById(R.id.ivHumidifier);
        dehumidLogo = (ImageView) findViewById(R.id.ivDehumidifier);
        moistLogo = (ImageView) findViewById(R.id.ivPump);
        lightLogo = (ImageView) findViewById(R.id.ivLight);
        EndLight = (TextView) findViewById(R.id.textViewEndLight);
        ActTemp = (TextView) findViewById(R.id.textViewActTemp);
        ActHum = (TextView) findViewById(R.id.textViewActHum);
        ActMoist = (TextView) findViewById(R.id.textViewActMoist);
        tvControl = (TextView) findViewById(R.id.tvControl);
    }

    private void setOnClick(){
        //Initialize button to do a function or open an activity
        imageViewLogout.setOnClickListener(this);
        imageViewProfile.setOnClickListener(this);
        imageViewHistory.setOnClickListener(this);
        imageViewHome.setOnClickListener(this);
        heatLogo.setOnClickListener(this);
        coolLogo.setOnClickListener(this);
        moistLogo.setOnClickListener(this);
        humidLogo.setOnClickListener(this);
        dehumidLogo.setOnClickListener(this);
        lightLogo.setOnClickListener(this);
        tvControl.setOnClickListener(this);

    }

    //Function when imageView is clicked
    @Override
    public void onClick(View v) {
        if (v == heatLogo){
            //Activate Heater
            heaterActuator();
        }
        if (v == coolLogo){
            //Activate Cooler
            coolerActuator();
        }
        if (v == humidLogo) {
            //Activate Humidifier
            humidifierActuator();
        }
        if (v == dehumidLogo) {
            //not done!
            dehumidifierActuator();
        }
        if (v == moistLogo){
            //Activate Pump
            micropumpActuator();
        }
        if (v == lightLogo){
            //Activate Grow Light
            lightActuator();
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
            startActivity(new Intent(this, HomeManualActivity.class));
        }
        if (v == tvControl){
            startActivity(new Intent(this, HomeActivity.class));
        }
    }
}
