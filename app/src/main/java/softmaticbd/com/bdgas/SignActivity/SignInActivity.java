package softmaticbd.com.bdgas.SignActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import softmaticbd.com.bdgas.MapActivity.CustomerMapsActivity;
import softmaticbd.com.bdgas.MapActivity.DistributorMapsActivity;
import softmaticbd.com.bdgas.Profile.ProfileCustomer;
import softmaticbd.com.bdgas.R;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUserName, etPassword;
    private TextView tvSignIn;
    private Button btnSignIn;
    private String userName,password;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ProgressDialog pDialog;

    // todo reset password
    private LinearLayout signInLayout,resetLayout;
    private EditText resetEmail;
    private TextView tvResetPass,tvBackReset;
    private Button btnResetSubmit;
    private String rEmail;
    private Boolean isResetLayoutExist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        etUserName = findViewById(R.id.etUserNameId);
        etPassword= findViewById(R.id.etPasswordID);
        tvSignIn =findViewById(R.id.tvCreateNew);
        btnSignIn = findViewById(R.id.btnSignInId);

        tvSignIn.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);

        //todo reset
        signInLayout = findViewById(R.id.signInLayout);
        resetLayout  =findViewById(R.id.resetLayout);
        resetEmail = findViewById(R.id.resetEmail);
        tvResetPass = findViewById(R.id.tvResetPass);
        tvResetPass.setOnClickListener(this);
        btnResetSubmit = findViewById(R.id.btnResetSubmit);
        btnResetSubmit.setOnClickListener(this);
        tvBackReset =findViewById(R.id.tvBackReset);
        tvBackReset.setOnClickListener(this);

        //todo progress Bar
        pDialog = new ProgressDialog(SignInActivity.this,R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data.....");
        pDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null){
            startUserWiseActivity();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnSignIn){
            pDialog.show();
            onUserLogin();
        }
        if (v == tvSignIn){
            startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        }
        if (v == tvResetPass){
            signInLayout.setVisibility(View.GONE);
            resetLayout.setVisibility(View.VISIBLE);
            isResetLayoutExist = true;
        }

        if (v == btnResetSubmit){
            pDialog.show();
            onPasswordReset();
        }
        if (v == tvBackReset){
            resetLayout.setVisibility(View.GONE);
            signInLayout.setVisibility(View.VISIBLE);
            isResetLayoutExist = false;
        }
    }

    private void onUserLogin(){
        userName = etUserName.getText().toString();
        password = etPassword.getText().toString();

        if (userName.isEmpty()){
            pDialog.dismiss();
            etUserName.setError("User Name is Required");
            etUserName.requestFocus();
            return;
        }
        if (password.isEmpty()){
            pDialog.dismiss();
            etPassword.setError("Password is Required");
            etPassword.requestFocus();
            return;
        }
        auth.signInWithEmailAndPassword(userName,password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        startUserWiseActivity();
                    }else {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Invalid !! Login is UnSuccessful",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void onPasswordReset(){
        rEmail = resetEmail.getText().toString().trim();
        if (TextUtils.isEmpty(rEmail)){
            resetEmail.setError("Required");
            resetEmail.requestFocus();
            pDialog.dismiss();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(rEmail).matches()) {
            pDialog.dismiss();
            resetEmail.setError("Invalid !! Use a verified Email");
            resetEmail.requestFocus();
            pDialog.dismiss();
            return;
        }
        auth.sendPasswordResetEmail(rEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        pDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                        builder.setTitle("Alert");
                        builder.setMessage("Please Check your Email to Reset Password");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok", (dialog, which) -> {
                            resetLayout.setVisibility(View.GONE);
                            signInLayout.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        builder.show();
                    }else {
                        pDialog.dismiss();
                        resetLayout.setVisibility(View.GONE);
                        Toast.makeText(SignInActivity.this,"Failed to send Reset Email",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void startUserWiseActivity(){
        String id = auth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User");

        Query mQuery = reference.orderByChild(id);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        String key = data.getKey();
                        if (key.equals("Customer")){
                            pDialog.dismiss();
                            startActivity(new Intent(SignInActivity.this, CustomerMapsActivity.class));
                        }else {
                            pDialog.dismiss();
                            startActivity(new Intent(SignInActivity.this, DistributorMapsActivity.class));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isResetLayoutExist){
            signInLayout.setVisibility(View.VISIBLE);
            isResetLayoutExist = false;
        }else {
            finishAffinity();
        }
    }
}
