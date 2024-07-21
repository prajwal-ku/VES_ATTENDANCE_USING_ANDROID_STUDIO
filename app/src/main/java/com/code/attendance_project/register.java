package com.code.attendance_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textview;

    String[] yearItems = {"1st Year", "2nd Year", "3rd Year", "4th Year"};
    String[] branchItems = {"ECS", "CMPN", "AURO", "EXTC", "AIDS", "INFT"};
    String[] genderItems = {"Male", "Female"};

    AutoCompleteTextView yearAutoCompleteTextView;
    AutoCompleteTextView branchAutoCompleteTextView;
    AutoCompleteTextView genderAutoCompleteTextView;

    ArrayAdapter<String> yearAdapter;
    ArrayAdapter<String> branchAdapter;
    ArrayAdapter<String> genderAdapter;

    EditText divisionEditText;
    EditText rollNoEditText;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressbar);
        textview = findViewById(R.id.loginNow);

        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(register.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(register.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(register.this, "Account Created", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Initialize year dropdown
        yearAutoCompleteTextView = findViewById(R.id.auto_complete_txt);
        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, yearItems);
        yearAutoCompleteTextView.setAdapter(yearAdapter);
        yearAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(register.this, "Year: " + item, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize branch dropdown
        branchAutoCompleteTextView = findViewById(R.id.auto_Br_txt);
        branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, branchItems);
        branchAutoCompleteTextView.setAdapter(branchAdapter);
        branchAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(register.this, "Branch: " + item, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize gender dropdown
        genderAutoCompleteTextView = findViewById(R.id.auto_gender_txt);
        genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderItems);
        genderAutoCompleteTextView.setAdapter(genderAdapter);
        genderAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(register.this, "Gender: " + item, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize division and roll number EditTexts
        divisionEditText = findViewById(R.id.division);
        rollNoEditText = findViewById(R.id.roll_no);
    }
}
