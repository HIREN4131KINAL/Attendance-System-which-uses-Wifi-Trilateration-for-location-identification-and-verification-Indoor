package com.creatershub.attendancesystemstudentapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.rtt.WifiRttManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Dashboard extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    String url = "https://attendance-system-archdj.herokuapp.com/";
    String roomId, subjectId, subjectName;
    JSONObject classroomJson;
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        TextView dashboardWelcomeTextView = findViewById(R.id.dashboardWelcomeText);
        sharedPreferences = getApplicationContext().getSharedPreferences("StudentDetails", 0);
        String name = sharedPreferences.getString("name", "");
        String welcomeText = "Welcome " + name + "!";
        dashboardWelcomeTextView.setText(welcomeText);
    }

    public void onClickMarkAttendance(View view) {
        final ProgressDialog dialog = new ProgressDialog(Dashboard.this);
        dialog.setMessage("Checking attendance status...");
        dialog.setCancelable(false);
        dialog.show();
        requestQueue = Volley.newRequestQueue(this);
        String checkAttendanceurl = url + "ongoingattendance/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, checkAttendanceurl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        dialog.dismiss();
                        try {
                            boolean valid = false;
                            String subId;
                            JSONArray subjectList = new JSONArray(sharedPreferences.getString("subjects",""));

                            for (int i = 0; i < subjectList.length(); i++) {
                                JSONObject subject = subjectList.getJSONObject(i).getJSONObject("subject");
                                subId = subject.getString("_id");
                                subjectName = subject.getString("code") + " " + subject.getString("sub_name");

                                for (int j = 0; j < response.length(); j++) {
                                    String attendanceSubId = response.getJSONObject(j).getString("subject");
                                    if (subId.equals(attendanceSubId)) {
                                        subjectId = subId;
                                        valid = true;
                                        break;
                                    }
                                }

                                if (valid)
                                    break;
                            }

                            if (!valid) {
                                Toast.makeText(Dashboard.this, "Sorry no ongoing attendance for you now!", Toast.LENGTH_SHORT).show();
                            }

                            else {
                                findRoom();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Toast.makeText(Dashboard.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(Dashboard.this, "Internal server error. Please try again later!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        requestQueue.add(jsonArrayRequest);

    }


    void findRoom() {
        final ProgressDialog dialog = new ProgressDialog(Dashboard.this);
        dialog.setMessage("Fetching classroom info...");
        dialog.setCancelable(false);
        dialog.show();
        JSONArray subjects;
        String getClassroomInfoUrl = "";
        try {
            subjects = new JSONArray(sharedPreferences.getString("subjects", ""));
            for (int i = 0; i < subjects.length(); i++) {
                String subId = subjects.getJSONObject(i).getJSONObject("subject").getString("_id");

                if (subId.equals(subjectId)) {
                    roomId = subjects.getJSONObject(i).getJSONObject("subject").getString("room");
                    break;
                }
            }
            getClassroomInfoUrl = url + "classroominfo/" + roomId + "/";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest classRoomInfoRequest = new JsonObjectRequest
                (Request.Method.GET, getClassroomInfoUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        classroomJson = response;
                        Toast.makeText(Dashboard.this, "Attending - " + subjectName, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), AttendanceMarker.class);
                        intent.putExtra("studentId", sharedPreferences.getString("id", ""));
                        intent.putExtra("subjectId", subjectId);
                        intent.putExtra("classroomJson", classroomJson.toString());
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Toast.makeText(Dashboard.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(Dashboard.this, "Internal server error. Can't fetch classroom info!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        requestQueue.add(classRoomInfoRequest);
    }



    public void onClickViewAttendance(View view) {

    }

    public void onClickProfile(View view) {

    }

    public void onClickLogout(View view) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("StudentDetails", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }
}
