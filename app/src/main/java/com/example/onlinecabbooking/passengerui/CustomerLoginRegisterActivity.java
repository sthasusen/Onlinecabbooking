package com.example.onlinecabbooking.passengerui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinecabbooking.R;
import com.example.onlinecabbooking.SettingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private EditText input_customer_email, input_customer_password;
    private TextView link_customer_login, customer_Status;
    private Button btn_customer_signup, btn_customer_login;

    private FirebaseAuth mauth;
    private ProgressDialog dialog;

    private DatabaseReference CustomerDatabaseRef;
    private String onlineCustomerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        mauth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);

        input_customer_email = findViewById(R.id.input_customer_email);
        input_customer_password = findViewById(R.id.input_customer_password);
        
        //login system
        customer_Status = findViewById(R.id.customer_Status);
        link_customer_login = findViewById(R.id.link_customer_login);

        btn_customer_signup = findViewById(R.id.btn_customer_signup);
        btn_customer_login = findViewById(R.id.btn_customer_login);
        btn_customer_login.setVisibility(View.INVISIBLE);
        btn_customer_login.setEnabled(false);
        link_customer_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_customer_signup.setVisibility(View.INVISIBLE);
                customer_Status.setText("Customer login System");

                link_customer_login.setVisibility(View.INVISIBLE);
                btn_customer_login.setVisibility(View.VISIBLE);
                btn_customer_login.setEnabled(true);
            }
        });

        btn_customer_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Cemail = input_customer_email.getText().toString();
                String Cpassword = input_customer_password.getText().toString();

                RegisterCustomer(Cemail, Cpassword);
            }
        });

        btn_customer_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Cemail = input_customer_email.getText().toString();
                String Cpassword = input_customer_password.getText().toString();

                CustomerLogin(Cemail, Cpassword);
            }
        });


    }

    private void CustomerLogin(String cemail, String cpassword) {

        if (TextUtils.isEmpty(cemail)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cpassword)) {
            Toast.makeText(this, "Please enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            dialog.setIndeterminate(true);
            dialog.setTitle("Customer Login");
            dialog.setMessage("Loading...");
            dialog.show();

            mauth.signInWithEmailAndPassword(cemail, cpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(CustomerLoginRegisterActivity.this, PassengerMapsActivity.class));
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Successfully Login...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                Intent intent = new Intent(CustomerLoginRegisterActivity.this, SettingActivity.class);
                                intent.putExtra("type", "Customers");
                                startActivity(intent);

                            } else {
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Failed to Login Customer. Please Try Again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
        }
    }

    private void RegisterCustomer(String cemail, String cpassword) {
        if (TextUtils.isEmpty(cemail)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cpassword)) {
            Toast.makeText(this, "Please enter Password...", Toast.LENGTH_SHORT).show();
        } else {
            dialog.setIndeterminate(true);
            dialog.setTitle("Loading...");
            dialog.setMessage("Please Wait, Until completing Registration Finished ");
            dialog.show();

            mauth.createUserWithEmailAndPassword(cemail, cpassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                onlineCustomerID = mauth.getCurrentUser().getUid();
                                CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Customers").child(onlineCustomerID);
                                CustomerDatabaseRef.setValue(true);
                                Intent driverIntent = new Intent(CustomerLoginRegisterActivity.this, PassengerMapsActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Successfully Register Customer Information...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Failed to Register Details. Please Try Again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });
        }

    }
}