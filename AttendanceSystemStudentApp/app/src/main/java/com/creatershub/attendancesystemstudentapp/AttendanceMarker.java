package com.creatershub.attendancesystemstudentapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AttendanceMarker extends AppCompatActivity {
    String notVerifiedText = "<font color=#000000>Status - </font> <font color=#ff0000>Not Verified</font>";
    String verifiedText = "<font color=#000000>Status - </font> <font color=#006400>Verified</font>";
    String url = "https://attendance-system-archdj.herokuapp.com/";
    TextView locationVerificationStatusTextView, identityIdentificationStatusTextView, expectedLocationTextView;
    Boolean locationVerified = false, identityVerified = false;
    String studentId, subjectId, classname;
    JSONObject classroomJson;
    JSONArray apList;
    RequestQueue requestQueue;
    WifiManager wifiManager;
    HashMap<String, Double> discoverableAPs;
    List<WifiDetails> validAPs;

    class WifiDetails {
        String bssid;
        double distance, x, y, z;

        WifiDetails(String bssid, double distance, double x, double y, double z) {
            this.bssid = bssid;
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_marker);
        discoverableAPs = new HashMap<>();
        validAPs = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        locationVerificationStatusTextView = findViewById(R.id.locationVerificationStatusTextView);
        identityIdentificationStatusTextView = findViewById(R.id.identityVerificationStatusTextView);
        expectedLocationTextView = findViewById(R.id.expectedLocationTextView);
        locationVerificationStatusTextView.setText(Html.fromHtml(notVerifiedText));
        identityIdentificationStatusTextView.setText(Html.fromHtml(notVerifiedText));
        Intent intent = getIntent();
        studentId = intent.getStringExtra("studentId");
        subjectId = intent.getStringExtra("subjectId");
        try {
            classroomJson = new JSONObject(intent.getStringExtra("classroomJson"));
            classname = classroomJson.getString("class_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        expectedLocationTextView.setText(String.format("Expected location - %s", classname));
    }

    public void onClickVerifyIdentity(View view) {
        if (locationVerified) {
            return;
        }

        else {
            Toast.makeText(this, "Please verify your location first!", Toast.LENGTH_SHORT).show();
        }
    }




    public void onClickVerifyLocation(View view) {
        final ProgressDialog dialog = new ProgressDialog(AttendanceMarker.this);
        dialog.setMessage("Fetching AP list...");
        dialog.setCancelable(false);
        dialog.show();
        String getApUrl = url + "accesspoint/";

        JsonArrayRequest apRequest = new JsonArrayRequest
                (Request.Method.GET, getApUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        apList = response;
                        dialog.dismiss();
                        scanAPs();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Toast.makeText(AttendanceMarker.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(AttendanceMarker.this, "Internal server error. Can't fetch students list!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        requestQueue.add(apRequest);
    }


    public static double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }



    void scanAPs() {
        discoverableAPs.clear();
        validAPs.clear();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiManager != null;
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wifi not enabled. Enabling...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        for (int i = 0; i < 10; i++) {
            wifiManager.startScan();
            List<android.net.wifi.ScanResult> list = wifiManager.getScanResults();

            for (android.net.wifi.ScanResult scanResult : list) {
                double freq = scanResult.frequency;
                String bssid = scanResult.BSSID;
                String ssid = scanResult.SSID;
                double level = scanResult.level;
                double distance = calculateDistance(level, freq);

                if (i == 0) {
                    discoverableAPs.put(bssid, distance);
                }

                else {
                    double dist = discoverableAPs.get(bssid);
                    dist += distance;
                    discoverableAPs.put(bssid, dist);
                }
            }
        }

        for (HashMap.Entry<String, Double> entry : discoverableAPs.entrySet()) {
            double dist = entry.getValue();
            dist /= 10;
            entry.setValue(dist);
        }

        
        for (int i = 0; i < apList.length(); i++) {
            try {
                JSONObject apJson = apList.getJSONObject(i);
                String bssid = apJson.getString("bssid");

                if (discoverableAPs.containsKey(bssid)) {
                    WifiDetails wifi = new WifiDetails(
                            bssid, discoverableAPs.get(bssid), apJson.getDouble("x"),
                            apJson.getDouble("y"), apJson.getDouble("z"));

                    validAPs.add(wifi);
                    Log.e("mila", "hai");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        calculateLocationAndVerify();

    }

    void calculateLocationAndVerify() {
        if (validAPs.size() < 3) {
            Toast.makeText(this, "Atleast 3 registered APs should be discoverable!", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Discovered = " + Integer.toString(validAPs.size()), Toast.LENGTH_SHORT).show();
            locationVerified = true;
            locationVerificationStatusTextView.setText(Html.fromHtml(verifiedText));
        }

        else {
            double[][] positions = new double[validAPs.size()][3];
            double[] distances = new double[validAPs.size()];

            for (int i = 0; i < validAPs.size(); i++) {
                positions[i][0] = validAPs.get(i).x;
                positions[i][1] = validAPs.get(i).y;
                positions[i][2] = validAPs.get(i).z;
                distances[i] = validAPs.get(i).distance;
            }

            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();
            double[] location = optimum.getPoint().toArray();

            double x = location[0];
            double y = location[1];
            double z = location[2];

            try {
                if (x > classroomJson.getDouble("xmin") &&  y > classroomJson.getDouble("ymin") &&
                        z > classroomJson.getDouble("zmin") &&  x < classroomJson.getDouble("xmax") &&
                        y < classroomJson.getDouble("ymax") &&  z < classroomJson.getDouble("zmax")) {

                    Toast.makeText(this, "Your location is verified!", Toast.LENGTH_SHORT).show();
                    locationVerified = true;
                    locationVerificationStatusTextView.setText(Html.fromHtml(verifiedText));
                }

                else {
                    Toast.makeText(this, "Sorry, your location was not verified! Try again!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }




    public void onClickSubmit(View view) {
        if (locationVerified && identityVerified) {
            return;
        }

        else {
            Toast.makeText(this, "Please verify your location and identity!", Toast.LENGTH_SHORT).show();
        }
    }
}
