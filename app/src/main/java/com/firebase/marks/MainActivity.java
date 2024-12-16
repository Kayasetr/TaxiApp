package com.firebase.marks;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    //Объявление переменных
    private Button mDriver, mCustomer;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String DRIVER_OR_PASSENGER = "driverOrPassenger";
    private int choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDriver = findViewById(R.id.driver);
        mCustomer = findViewById(R.id.customer);
        startService(new Intent(MainActivity.this, OnAppKilled.class));
        //getChoice();
        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DriverLoginPage.class);
                startActivity(i);
                finish();
            }
        });
        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PassengerLoginPage.class);
                startActivity(i);
                finish();
            }
        });
    }
    public void getChoice() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        choice = sharedPreferences.getInt(DRIVER_OR_PASSENGER, 0);
        Intent intent;
        switch (choice) {
            case 1:
                intent = new Intent(MainActivity.this, DriverLoginPage.class);
                startActivity(intent);
                finish();
                break;
            case 2:
                intent = new Intent(MainActivity.this, PassengerLoginPage.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }
}
