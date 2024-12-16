package com.firebase.marks;

import static com.firebase.marks.MainActivity.DRIVER_OR_PASSENGER;
import static com.firebase.marks.MainActivity.SHARED_PREFS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginPage extends AppCompatActivity {
    //Объявление переменных
    private EditText LoginDriverInp, PasswordDriverInp;
    private Button LoginButton, RegisterButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_page);
        //Вводы логина и пароля
        LoginDriverInp = findViewById(R.id.LoginDriverIn);
        PasswordDriverInp = findViewById(R.id.PasswordDriverIn);
        //Кнопки входа и регистрации
        LoginButton = findViewById(R.id.LoginButton);
        RegisterButton = findViewById(R.id.RegisterButton);
        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null) {
                    Intent i = new Intent(DriverLoginPage.this, DriverMapsActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };



        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginDriverInp.getText().toString().equals("") && PasswordDriverInp.getText().toString().equals("")){
                    Toast.makeText(DriverLoginPage.this, "Введите данные для регистрации", Toast.LENGTH_SHORT).show();
                } else {
                    final String email = LoginDriverInp.getText().toString();
                    final String password = PasswordDriverInp.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginPage.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id).child("name");
                                user_db.setValue(email);
                            }else {
                                Toast.makeText(DriverLoginPage.this, "Что-то пошло не так"+task.getResult(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginDriverInp.getText().toString().equals("") && PasswordDriverInp.getText().toString().equals("")){
                    Toast.makeText(DriverLoginPage.this, "Введите данные для авторизации", Toast.LENGTH_SHORT).show();
                } else {
                    final String email = LoginDriverInp.getText().toString();
                    final String password = PasswordDriverInp.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginPage.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent i = new Intent(DriverLoginPage.this, DriverMapsActivity.class);
                                startActivity(i);
                                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(DRIVER_OR_PASSENGER, 1);
                                editor.apply();
                                finish();
                            }else {
                                Toast.makeText(DriverLoginPage.this, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}