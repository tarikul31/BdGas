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
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
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
import java.util.HashMap;
import java.util.Map;

import softmaticbd.com.bdgas.MapActivity.CustomerMapsActivity;
import softmaticbd.com.bdgas.MapActivity.DistributorMapsActivity;
import softmaticbd.com.bdgas.Model.Customer;
import softmaticbd.com.bdgas.Model.Distributor;
import softmaticbd.com.bdgas.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ProfileDistributor extends AppCompatActivity implements View.OnClickListener {

    private EditText dFullName, dPhone, dEmail, dShopName, dShopAddress;
    private String name, phone, email, shopName, shopAddress;
    private Button btndSubmit;
    private ImageView ivDisPro;

    //todo for firebase db
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference, disLocationRef;
    private StorageReference storageReference;

    // todo for getuser Location
    private FusedLocationProviderClient fsClient;

    // todo utils
    private static final String TAG = "ProfileCustomer";
    private static final String REQUIRED = "Required";
    private Uri resultUri;
    private Bitmap bitmap = null;
    private String profileImage;
    private String userID;
    private static final int GALLERY_INTENT = 1;
    private boolean isProfileExist = false;
    private ProgressDialog pDialog;
    private GeoQuery geoQuery;
    private Distributor distributor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_distributor);

        dFullName = findViewById(R.id.disFullNameId);
        dPhone = findViewById(R.id.disPhoneId);
        dEmail = findViewById(R.id.disEmailId);
        dShopName = findViewById(R.id.disShopNameId);
        dShopAddress = findViewById(R.id.disShopAddressId);
        ivDisPro = findViewById(R.id.ivDisProfileId);
        ivDisPro.setOnClickListener(this);

        btndSubmit = findViewById(R.id.btnDisSubmit);
        btndSubmit.setOnClickListener(this);

        //todo for progress Dialog
        pDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data...");
        pDialog.setCancelable(false);

        fsClient = LocationServices.getFusedLocationProviderClient(this);

        // todo for databaseRef
        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();
        disLocationRef = FirebaseDatabase.getInstance().getReference().child("Location").child("Distributor");
        reference = FirebaseDatabase.getInstance().getReference().child("BdGas").child("Distributor").child("Profile").child(userID);
        storageReference = FirebaseStorage.getInstance().getReference().child("BdGas").child("Distributor").child(userID);
        checkUserPermission();
        getUserInfo();
    }

    private void checkUserPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onClick(View v) {
        if (v == ivDisPro) {
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), GALLERY_INTENT);
            isProfileExist = true;
        }

        if (v == btndSubmit) {
            pDialog.show();
            onDistributorReg();
            getDistributorLocation();
        }
    }

    // todo for distributor location
    private void getDistributorLocation() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fsClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    GeoFire geoFire = new GeoFire(disLocationRef);
                    geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    private void getUserInfo() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    distributor = dataSnapshot.getValue(Distributor.class);

                    dFullName.setText(distributor.getName());
                    dPhone.setText(distributor.getPhone());
                    dEmail.setText(distributor.getEmail());
                    dShopName.setText(distributor.getShopName());
                    dShopAddress.setText(distributor.getShopAddress());
                    profileImage = distributor.getProfileImage();
                    Glide.with(getApplication()).load(profileImage).into(ivDisPro);
                    isProfileExist = true;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onDistributorReg() {
        name = dFullName.getText().toString();
        phone = dPhone.getText().toString();
        email = dEmail.getText().toString();
        shopName = dShopName.getText().toString();
        shopAddress = dShopAddress.getText().toString();

        if (name.isEmpty()) {
            pDialog.dismiss();
            dFullName.setError("Enter the Full Name");
            dFullName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            pDialog.dismiss();
            dPhone.setError("Enter the phone Number");
            dPhone.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            pDialog.dismiss();
            dEmail.setError("Enter the address");
            dEmail.requestFocus();
            return;
        }
        if (shopName.isEmpty()) {
            pDialog.dismiss();
            dShopName.setError("Enter Father Name");
            dShopName.requestFocus();
            return;
        }
        if (shopAddress.isEmpty()) {
            pDialog.dismiss();
            dShopAddress.setError("Enter Father Name");
            dShopAddress.requestFocus();
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

            Task<Uri> tasks = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        pDialog.dismiss();
                        Toast.makeText(getApplication(), "Upload Unsuccessful...", Toast.LENGTH_SHORT).show();
                    }

                    return storageReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    profileImage = task.getResult().toString();
                    setDistributor();
                    pDialog.dismiss();
                }
            });

        }
        else if (isProfileExist) {
            setDistributor();
        }
        else {
            Log.e(TAG, "Image is " + REQUIRED);
            Toast.makeText(ProfileDistributor.this, "Please Again Upload Image", Toast.LENGTH_LONG).show();
            pDialog.dismiss();
        }
    }
    private void setDistributor(){
        distributor = new Distributor(name, phone, email,shopName,shopAddress,profileImage);
        reference.setValue(distributor)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileDistributor.this, "Upload Successful...", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ProfileDistributor.this, DistributorMapsActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            ivDisPro.setImageURI(imageUri);
        }
    }


}
