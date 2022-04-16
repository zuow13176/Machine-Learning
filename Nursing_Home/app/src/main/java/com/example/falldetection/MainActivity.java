package com.example.falldetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.stetho.Stetho;
import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity {
    //Location
    private LocationManager locationManager;
    private TextView textViewLa, textViewLo;
    private String stringLa, stringLo;

    //Sensor
    private TextView xValue_a, yValue_a, zValue_a;
    private TextView xValue_g, yValue_g, zValue_g;
    private SensorManager sensorManager;

    //Server
    private TextView textIn;
    private EditText patientID_editText;

    //SMS
    private EditText phone_Edittext;
    private SmsManager smsManager;

    //DB
    private SQLiteDB mySQLHelper;
    private List<DataBean> dataList;

    //KNN
    private int k_value;
    private String movementResult;
    private TextView movementView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        movementView = findViewById(R.id.movement_TextView);

        textIn = (TextView)findViewById(R.id.serverRes_textView);
        patientID_editText = findViewById(R.id.patienID_Edittext);
        phone_Edittext = findViewById(R.id.phone_Edittext);

        initDB();
        getLocation();
        getSensorValue();
        getDB();
        k_value = (int)Math.sqrt(dataList.size());
    }

    void getLocation() {
        textViewLa = (TextView) findViewById(R.id.latitute_textView);
        textViewLo = (TextView) findViewById(R.id.longitude_textView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                stringLa = String.valueOf(location.getLatitude());
                stringLo = String.valueOf(location.getLongitude());
                textViewLa.setText(stringLa);
                textViewLo.setText(stringLo);
            }
        });
    }

    void getSensorValue() {
        xValue_a = (TextView) findViewById(R.id.x_value);
        yValue_a = (TextView) findViewById(R.id.y_value);
        zValue_a = (TextView) findViewById(R.id.z_value);
        xValue_g = (TextView) findViewById(R.id.x_value2);
        yValue_g = (TextView) findViewById(R.id.y_value2);
        zValue_g = (TextView) findViewById(R.id.z_value2);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(myAccelerometerListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(myAccelerometerListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    final SensorEventListener myAccelerometerListener = new SensorEventListener(){
        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                xValue_a.setText(String.valueOf(X_lateral));
                yValue_a.setText(String.valueOf(Y_longitudinal));
                zValue_a.setText(String.valueOf(Z_vertical));
                isfalling_KNN(X_lateral,Y_longitudinal,Z_vertical);
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float x2 = sensorEvent.values[0];
                float y2 = sensorEvent.values[1];
                float z2 = sensorEvent.values[2];
                xValue_g.setText(String.valueOf(x2));
                yValue_g.setText(String.valueOf(y2));
                zValue_g.setText(String.valueOf(z2));
            }
        }
        public void onAccuracyChanged(Sensor sensor , int accuracy){
        }
    };

    void initDB() {
        mySQLHelper = new SQLiteDB(this);
        try {
            InputStreamReader file = null;
            file = new InputStreamReader(getAssets().open("fallingData.csv"));
            BufferedReader buffer = new BufferedReader(file);
            String line = "";
            while ((line = buffer.readLine()) != null) {
                Log.e("line", line);
                String[] str = line.split(",");
                float x = Float.parseFloat(str[0].toString());
                float y = Float.parseFloat(str[1].toString());
                float z = Float.parseFloat(str[2].toString());
                String c = str[3].toString();
                Log.e("line", x+y+z+c);
                mySQLHelper.insertRecord(x, y, z, c);
                Log.e("Import", "Successfully Updated Database.");
            }
        } catch (IOException e) {
            Log.e("SQLError", e.getMessage().toString());
        }
    }

    void getDB() {
        Log.e("ShowData", "making list");
        dataList = mySQLHelper.queryRecord();
        Log.e("ShowData", "done list");
    }

    double distance(float x1, float y1, float z1, float x2, float y2, float z2){
        double distance = 0.0;
        float temp = (x1-x2);
        distance += temp*temp;
        temp = (y1-y2);
        distance += temp*temp;
        temp = (z1-z2);
        distance += temp*temp;
        return Math.sqrt(distance);
    }

    void isfalling_KNN(float x2, float y2, float z2) {
        PriorityQueue<DistanceData> heap = new PriorityQueue<DistanceData>(new Comparator<DistanceData>() {
            public int compare(DistanceData a, DistanceData b) { return (int)(a.getDistance()-b.getDistance()); }
        });
        for (int i = 0; i < dataList.size(); i++) {
            float x1 = dataList.get(i).getX();
            float y1 = dataList.get(i).getY();
            float z1 = dataList.get(i).getZ();
            float distance_temp = (float) distance(x1,y1,z1,x2,y2,z2);
            heap.offer(new DistanceData(distance_temp, dataList.get(i).getClass_(),0));
        }

        HashMap<String, Integer> classcount = new HashMap<String, Integer>();
        for (int i = 0; i < k_value; i++) {
            DistanceData tempData = heap.poll();
            if(!classcount.containsKey(tempData.getClass_())){
                classcount.put(tempData.getClass_(), 1);
            } else {
                classcount.put(tempData.getClass_(), classcount.get(tempData.getClass_())+1);
            }
        }

        PriorityQueue<DistanceData> knn_return = new PriorityQueue<DistanceData>(new Comparator<DistanceData>() {
            public int compare(DistanceData a, DistanceData b) { return (int)(a.getCount_()-b.getCount_()); }
        });

        Iterator classcountIterator = classcount.entrySet().iterator();
        while (classcountIterator.hasNext()) {
            Map.Entry mapElement
                    = (Map.Entry)classcountIterator.next();
            DistanceData tempData = new DistanceData(0, String.valueOf(mapElement.getKey()),(int)mapElement.getValue());
            knn_return.offer(tempData);
        }
        movementResult = knn_return.poll().getClass_();
        movementView.setText(movementResult);
        Log.e("Movement", movementResult);
        sentToServer();
        if (movementResult.equals("lying")) {
            sentSMS();
        }
    }

    void sentToServer() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String P_ID = patientID_editText.getText().toString();
        String msg = "Patient:"+ P_ID + ", " + movementResult +", Location: "+ stringLa + "," +stringLo;
        Socket socket;
        DataOutputStream dataOutputStream;
        DataInputStream dataInputStream;
        try {
            socket = new Socket("192.168.1.72", 5050);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(msg);
            dataInputStream = new DataInputStream(socket.getInputStream());
            textIn.setText(dataInputStream.readUTF());
            dataOutputStream.close();
            dataOutputStream.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void sentSMS() {
        String P_ID = patientID_editText.getText().toString();
        String phoneNumber = phone_Edittext.getText().toString();
        String sms_msg = "Patient:"+ P_ID + ", " + movementResult +", Location: "+ stringLa + "," +stringLo;
        try {
            smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, sms_msg, null, null);
            Log.i("Send SMS", "");
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
        }
    }
}