package softmaticbd.com.bdgas.Profile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import softmaticbd.com.bdgas.MapActivity.CustomerMapsActivity;
import softmaticbd.com.bdgas.Model.Customer;
import softmaticbd.com.bdgas.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ProfileCustomer extends AppCompatActivity implements View.OnClickListener {

    private EditText eFullName, ePhone, eEmail, eAddress;
    private Button btnSubmit;
    private ImageView ivPro;

    //todo for firebase db
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;

    // todo utils
    private static final String TAG = "ProfileCustomer";
    private static final String REQUIRED = "Required";
    private Uri resultUri;
    private Bitmap bitmap = null;
    private String userID;
    private static final int GALLERY_INTENT = 1;
    private String name, phone, email, address, profileImage;
    private boolean isProfileExist = false;
    private ProgressDialog pDialog;
    private Customer customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_customer);

        eFullName = findViewById(R.id.cusFullNameId);
        ePhone = findViewById(R.id.cusPhoneId);
        eEmail = findViewById(R.id.cusEmailId);
        eAddress = findViewById(R.id.cusAddressId);
        ivPro = findViewById(R.id.ivCusProfile);
        btnSubmit = findViewById(R.id.btnCusSubmit);
        btnSubmit.setOnClickListener(this);
        ivPro.setOnClickListener(this);

        //todo for progress Dialog
        pDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data...");
        pDialog.setCancelable(false);

        // todo for databaseRef
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("BdGas").child("Customer").child("Profile").child(userID);
        storageReference = FirebaseStorage.getInstance().getReference().child("BdGas").child("Customer").child(userID);
        checkUserPermission();
        getUserInfo();

    }

    private void checkUserPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onClick(View v) {
        if (v == ivPro) {
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), GALLERY_INTENT);
//            isProfileExist = true;
        }
        if (v == btnSubmit) {
            pDialog.show();
            createCustomerProfile();
        }
    }

    private void getUserInfo() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    customer = dataSnapshot.getValue(Customer.class);
                    eFullName.setText(customer.getName());
                    ePhone.setText(customer.getPhone());
                    eEmail.setText(customer.getEmail());
                    eAddress.setText(customer.getAddress());
                    profileImage = customer.getProfileImage();
                    Glide.with(getApplication()).load(profileImage).into(ivPro);
                    isProfileExist = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void createCustomerProfile() {
        name = eFullName.getText().toString();
        phone = ePhone.getText().toString();
        email = eEmail.getText().toString();
        address = eAddress.getText().toString();

        if (name.isEmpty()) {
            pDialog.dismiss();
            eFullName.setError(REQUIRED);
            eFullName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            pDialog.dismiss();
            ePhone.setError(REQUIRED);
            ePhone.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            pDialog.dismiss();
            eEmail.setError(REQUIRED);
            eEmail.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            pDialog.dismiss();
            eAddress.setError(REQUIRED);
            eAddress.requestFocus();
            return;
        }
        if (resultUri != null) {

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);

            Task<Uri> tasks = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplication(), "Upload Unsuccessful...", Toast.LENGTH_SHORT).show();
                }

                return storageReference.getDownloadUrl();

            }).addOnCompleteListener(task -> {
                profileImage = task.getResult().toString();
                setCustomer();
                pDialog.dismiss();
            });

        }
        else if (isProfileExist) {
            setCustomer();
        }
        else {
            Log.e(TAG, "Image is " + REQUIRED);
            Toast.makeText(ProfileCustomer.this, "Please Again Upload Image", Toast.LENGTH_LONG).show();
            pDialog.dismiss();
        }
    }

    private void setCustomer() {
        customer = new Customer(name, phone, email, address, profileImage);
        reference.setValue(customer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileCustomer.this, "Upload Successful...", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ProfileCustomer.this, CustomerMapsActivity.class));
                })
                .addOnFailureListener(e -> {

                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            ivPro.setImageURI(imageUri);
        }
    }

}
