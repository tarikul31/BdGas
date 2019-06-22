package softmaticbd.com.bdgas.SignActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import softmaticbd.com.bdgas.Profile.ProfileCustomer;
import softmaticbd.com.bdgas.Profile.ProfileDistributor;
import softmaticbd.com.bdgas.R;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText eEmail, ePassword, eConPassword;
    private Button btnSignUp;
    private TextView tvBack;
    private String email, pass, conPass;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private RadioGroup rgUser;
    private RadioButton rbCustomer, rbDistributor;
    private Boolean isCusUser = true;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        eEmail = findViewById(R.id.etEmail);
        ePassword = findViewById(R.id.etPassword);
        eConPassword = findViewById(R.id.etConfirmPass);

        tvBack = findViewById(R.id.tvBack);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvBack.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

        rbCustomer = findViewById(R.id.rbCustomerId);
        rbDistributor = findViewById(R.id.rbDistributorId);
        rbCustomer.setOnClickListener(this);
        rbDistributor.setOnClickListener(this);
        rgUser = findViewById(R.id.rgUserId);

        auth = FirebaseAuth.getInstance();
        //todo progress Bar
        pDialog = new ProgressDialog(SignUpActivity.this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data.....");
        pDialog.setCancelable(false);

    }

    @Override
    public void onClick(View v) {
        if (v == tvBack) {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        }
        if (v == btnSignUp) {
            pDialog.show();
            onUserRegistration();
        }
        if (v == rbCustomer) {
            isCusUser = true;
        }

        if (v == rbDistributor) {
            isCusUser = false;
        }
    }

    private void onUserRegistration() {
        email = eEmail.getText().toString();
        pass = ePassword.getText().toString();
        conPass = eConPassword.getText().toString();

        if (email.isEmpty()) {
            eEmail.setError("Email is Required");
            pDialog.dismiss();
            eEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            pDialog.dismiss();
            eEmail.setError("Invalid !! Use a Valid Email");
            eEmail.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            pDialog.dismiss();
            ePassword.setError("Password is Required");
            ePassword.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            pDialog.dismiss();
            ePassword.setError("Set a Password 6 or More Digit");
            ePassword.requestFocus();
            return;
        }

        if (conPass.isEmpty()) {
            pDialog.dismiss();
            eConPassword.setError("Confirm Password is Required");
            eConPassword.requestFocus();
            return;
        }
        if (!pass.equals(conPass)) {
            pDialog.dismiss();
            ePassword.setError("Password Does't Match !! Try Again");
            ePassword.requestFocus();
            return;
        }
        if (rgUser.getCheckedRadioButtonId() == -1) {
            pDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please select User Type", Toast.LENGTH_SHORT).show();
            rgUser.requestFocus();
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        user = auth.getCurrentUser();
                        user.sendEmailVerification().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                pDialog.dismiss();
                                checkEmailVerify();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Email Verification is Unsuccessful", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        pDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                        builder.setTitle("Email Already Exist");
                        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    }
                });
    }

    private void checkEmailVerify() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Alert");
        alert.setMessage("Please Check Your Email To Verify You Account");
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", (dialog, which) -> user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                Toast.makeText(SignUpActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                onCheckUser();
            } else {
                checkEmailVerify();
            }
        }));
        alert.setNegativeButton("Cancel", (dialog, which) -> {
            auth.signOut();
            dialog.dismiss();
        });
        alert.show();
    }

    private void onCheckUser() {
        String id;
        if (isCusUser) {
            id = auth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child("User")
                    .child("Customer")
                    .child(id).setValue(true);
            pDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), ProfileCustomer.class));
        } else {
            id = auth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child("User")
                    .child("Distributor")
                    .child(id).setValue(true);
            pDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), ProfileDistributor.class));
        }
    }
}
