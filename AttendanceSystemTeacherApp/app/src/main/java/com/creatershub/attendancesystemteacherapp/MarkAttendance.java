package com.creatershub.attendancesystemteacherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.MalformedJsonException;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MarkAttendance extends AppCompatActivity {
    final String url = "https://attendance-system-archdj.herokuapp.com/";
    String subjectId;
    RequestQueue requestQueue;
    JSONArray studentListJsonArray;
    List<String> studentList;
    ListView studentListView;
    HashMap<String, Integer> studentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        studentMap = new HashMap<>();
        requestQueue = Volley.newRequestQueue(this);
        Intent intent = getIntent();
        subjectId = intent.getStringExtra("subjectId");
        try {
            studentListJsonArray = new JSONArray(intent.getStringExtra("studentList"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        studentList = new ArrayList<>();

        for (int i = 0; i < studentListJsonArray.length(); i++) {
            try {
                JSONObject student = studentListJsonArray.getJSONObject(i);
                studentList.add(student.getString("roll") + " " + student.getString("name"));
                studentMap.put(student.getString("_id"), i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        studentListView = findViewById(R.id.studentListView);
        studentListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        studentListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, studentList));
        getPresentStudents();
    }

    void getPresentStudents() {
        for (int i = 0; i < studentListView.getCount(); i++) {
            studentListView.setItemChecked(i, false);
        }

        final ProgressDialog dialog = new ProgressDialog(MarkAttendance.this);
        dialog.setMessage("Fetching list of present students...");
        dialog.setCancelable(false);
        dialog.show();
        String getPresentStudentsUrl = url + "tempattendance/" + subjectId + "/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, getPresentStudentsUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        dialog.dismiss();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject student = response.getJSONObject(i).getJSONObject("student");
                                String studentId = student.getString("_id");
                                int position = studentMap.get(studentId);
                                studentListView.setItemChecked(position, true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Log.e("Error hai", error.getMessage());
                            Toast.makeText(MarkAttendance.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(MarkAttendance.this, "Internal server error. Can't fetch students list!", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    public void onClickRefreshAttendance(View view) {
        getPresentStudents();
    }

    public void onClickSubmitAttendance(View view) {
        String updateTotalClassUrl = url + "updatetotalclass/" + subjectId + "/";
        String updateAttendanceUrl = url + "updateattendance/";
        String deleteTempAttendanceUrl = url + "tempattendance/" + subjectId + "/";

        List<String> finalPresentStudents = new ArrayList<>();
        SparseBooleanArray sparseBooleanArray = studentListView.getCheckedItemPositions();

        for (int i = 0; i < studentListView.getCount(); i++) {
            if(sparseBooleanArray.get(i)) {
                try {
                    JSONObject student = studentListJsonArray.getJSONObject(i);
                    finalPresentStudents.add(student.getString("_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject studentsJson = new JSONObject();
        try {
            studentsJson.put("subject", subjectId);
            studentsJson.put("ids", new JSONArray(finalPresentStudents));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final ProgressDialog dialog = new ProgressDialog(MarkAttendance.this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();

        final JsonObjectRequest tempDataRemoveRequest =
                new JsonObjectRequest(Request.Method.DELETE, deleteTempAttendanceUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        Toast.makeText(MarkAttendance.this, "Attendance updated!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Toast.makeText(MarkAttendance.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(MarkAttendance.this, "Internal server error. Please try again later!", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                });


        final JsonObjectRequest totalClassUpdateRequest =
                new JsonObjectRequest(Request.Method.PATCH, updateTotalClassUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        requestQueue.add(tempDataRemoveRequest);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Toast.makeText(MarkAttendance.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(MarkAttendance.this, "Internal server error. Please try again later!", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                });

        JsonObjectRequest updateAttendanceRequest =
                new JsonObjectRequest(Request.Method.PATCH, updateAttendanceUrl, studentsJson, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        requestQueue.add(totalClassUpdateRequest);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse == null) {
                            Toast.makeText(MarkAttendance.this, "Internet Service not available!", Toast.LENGTH_SHORT).show();
                        }

                        else if (error.networkResponse.statusCode == 500) {
                            Toast.makeText(MarkAttendance.this, "Internal server error. Please try again later!", Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                });

                requestQueue.add(updateAttendanceRequest);
    }

}
