package com.example.ta_firebaseauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageViewHome, imageViewLogout, imageViewProfile, imageViewHistory;
    private FirebaseAuth mAuth;

    private DatabaseReference ref, ref2;
    private FirebaseUser user;
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
    GraphView tempGraph, humGraph, moistGraph, lightGraph;
    LineGraphSeries tempSeries, humSeries, moistSeries, lightSeries;
    TextView nameTv, statusTv;

    public String name, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getTimeDate();

        mAuth = FirebaseAuth.getInstance();
        //databaseReference = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();

        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        castAndAdding();
        firebase();
        graphFormatter();

        ref = FirebaseDatabase.getInstance().getReference("Administrator");

        findID();
        setOnClick();
    }

    private void castAndAdding() {
        //Casting Graph
        tempGraph = (GraphView) findViewById(R.id.tempGraph);
        humGraph = (GraphView) findViewById(R.id.humGraph);
        moistGraph = (GraphView) findViewById(R.id.moistGraph);
        lightGraph = (GraphView) findViewById(R.id.lightGraph);

        //Adding Data Series
        tempSeries = new LineGraphSeries();
        humSeries = new LineGraphSeries();
        moistSeries = new LineGraphSeries();
        lightSeries = new LineGraphSeries();

        tempGraph.addSeries(tempSeries);
        humGraph.addSeries(humSeries);
        moistGraph.addSeries(moistSeries);
        lightGraph.addSeries(lightSeries);

    }

    private void graphFormatter() {
        tempGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(new Date((long) value));
                }
                return super.formatLabel(value, isValueX);
            }
        });
        tempGraph.getGridLabelRenderer().setNumHorizontalLabels(3);

        humGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(new Date((long) value));
                }
                return super.formatLabel(value, isValueX);
            }
        });
        humGraph.getGridLabelRenderer().setNumHorizontalLabels(3);

        moistGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(new Date((long) value));
                }
                return super.formatLabel(value, isValueX);
            }
        });
        moistGraph.getGridLabelRenderer().setNumHorizontalLabels(3);

        lightGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(new Date((long) value));
                }
                return super.formatLabel(value, isValueX);
            }
        });
        lightGraph.getGridLabelRenderer().setNumHorizontalLabels(3);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataPoint[] dp = new DataPoint[(int) dataSnapshot.getChildrenCount()];

                int index = 0;
                for (DataSnapshot myDataSnapShot : dataSnapshot.getChildren()) {
                    PointValue pointValue = myDataSnapShot.getValue(PointValue.class);
                    dp[index] = new DataPoint(pointValue.getTime(), pointValue.getTemp());
                    index++;
                }
                tempSeries.resetData(dp);

                int index2 = 0;
                for (DataSnapshot myDataSnapShot : dataSnapshot.getChildren()) {
                    PointValue pointValue = myDataSnapShot.getValue(PointValue.class);
                    dp[index2] = new DataPoint(pointValue.getTime(), pointValue.getHum());
                    index2++;
                }
                humSeries.resetData(dp);

                int index3 = 0;
                for (DataSnapshot myDataSnapShot : dataSnapshot.getChildren()) {
                    PointValue pointValue = myDataSnapShot.getValue(PointValue.class);
                    dp[index3] = new DataPoint(pointValue.getTime(), pointValue.getMoist());
                    index3++;
                }
                moistSeries.resetData(dp);

                int index4 = 0;
                for (DataSnapshot myDataSnapShot : dataSnapshot.getChildren()) {
                    PointValue pointValue = myDataSnapShot.getValue(PointValue.class);
                    dp[index4] = new DataPoint(pointValue.getTime(), pointValue.getLight());
                    index4++;
                }
                lightSeries.resetData(dp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void firebase() {
        ref2 = FirebaseDatabase.getInstance().getReference("Dataset");
        user = FirebaseAuth.getInstance().getCurrentUser();

        Query query = ref2.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    status = "" + ds.child("status").getValue();
                    nameTv.setText(name);
                    statusTv.setText(status);

                    if (status.equals("admin")) {
                        statusTv.setText("Administrator");
                    } else {
                        statusTv.setText("Member");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void findID() {
        imageViewLogout = (ImageView) findViewById(R.id.imageViewLogout);
        imageViewHome = (ImageView) findViewById(R.id.imageViewHome);
        imageViewHistory = (ImageView) findViewById(R.id.imageViewHistory);
        imageViewProfile = (ImageView) findViewById(R.id.imageViewProfile);
    }

    private void setOnClick() {
        imageViewLogout.setOnClickListener(this);
        imageViewHome.setOnClickListener(this);
        imageViewProfile.setOnClickListener(this);
        imageViewHistory.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == imageViewLogout) {
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (v == imageViewHome) {
            startActivity(new Intent(this, HomeActivity.class));
        }
        if (v == imageViewProfile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        if (v == imageViewHistory) {
            startActivity(new Intent(this, HistoryActivity.class));
        }
    }
}
