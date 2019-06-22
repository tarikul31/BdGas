package softmaticbd.com.bdgas.MapActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import softmaticbd.com.bdgas.Model.Customer;
import softmaticbd.com.bdgas.Model.CustomerReview;
import softmaticbd.com.bdgas.Model.Distributor;
import softmaticbd.com.bdgas.Model.OrderDetails;
import softmaticbd.com.bdgas.Model.Product;
import softmaticbd.com.bdgas.Operation.ProductsActivity;
import softmaticbd.com.bdgas.Operation.ProductList;
import softmaticbd.com.bdgas.Profile.ProfileDistributor;
import softmaticbd.com.bdgas.R;
import softmaticbd.com.bdgas.SignActivity.SignInActivity;

public class DistributorMapsActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;

    // todo for map integration
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest myLocationRequest;
    private Location myLocation;
    private Marker marker, providerMarker, cusMarker;
    private Distributor distributor;

    // todo App dependency
    private SupportMapFragment mapFragment;
    private static final int CALL = 1;
    private final int ACCESS_FINE_LOCATION = 1;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorBrown, R.color.colorPrimary, R.color.colorOrange, R.color.colorDeepAsh};


    // todo for firebase db
    private FirebaseAuth auth;
    private DatabaseReference reference, customerRef, cusProfileRef, cusLocationRef, cusReqRef;
    private DatabaseReference cusReqReff, findReqReff, deliveryRef;
    private String userId;
    private FirebaseUser user;

    // todo for navLoad
    private TextView nvName, nvEmail;
    private ImageView nvImage;
    private View view;

    // todo for assign customer
    private Customer customer;
    private LinearLayout cusReqLayout, acceptLayout, operationLayout;
    private String customerId;
    private Button btnAccept, btnCancel, btnOpComplete;
    private Product product;
    private String key;
    private OrderDetails orderDetails;

    //todo acceptReq
    private Button btnDelivery, btnShowUserDetails;
    private LinearLayout reqAcceptLayout, proDetailsLayout, userDetailsLayout;
    private TextView reqName, reqPhone, proName, unitPrice, totalPrice, reqQuantity, reqDeliveryAdd;
    private ImageView ivCusPhoneCall;
    private static final int REQUEST_CALL = 2;

    // todo review
    private CustomerReview customerReview;
    private TextView tvReview;
    private RatingBar ratingBar;
    private LinearLayout reviewLayout;
    private DatabaseReference reviewRef;
    private Button btnReviewClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_maps);
        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navViewDistributor);
        navigationView.setNavigationItemSelectedListener(this);
        view = navigationView.getHeaderView(0);
        //todo for nav load
        nvName = view.findViewById(R.id.nvNameId);
        nvEmail = view.findViewById(R.id.nvEmailId);
        nvImage = view.findViewById(R.id.nvProfileImageId);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            checkUsePermission();
        }

        // todo for assign Customer
        cusReqLayout = findViewById(R.id.cusReQLayoutID);
        acceptLayout = findViewById(R.id.acceptLayoutID);
        operationLayout = findViewById(R.id.operationLayoutID);
        btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(this);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        btnOpComplete = findViewById(R.id.btnOpComplete);
        btnOpComplete.setOnClickListener(this);

        //todo accept req
        reqAcceptLayout = findViewById(R.id.cReqAccLayoutID);
        proDetailsLayout = findViewById(R.id.proDetailsLayout);
        userDetailsLayout = findViewById(R.id.userDetailsLayout);
        reqName = findViewById(R.id.rCusNameID);
        proName = findViewById(R.id.rProductName);
        unitPrice = findViewById(R.id.rUnitPrice);
        reqQuantity = findViewById(R.id.rProductQuantity);
        totalPrice = findViewById(R.id.rTotalPrice);
        reqDeliveryAdd = findViewById(R.id.rDeliveryAddress);
        reqPhone = findViewById(R.id.rCusPhoneID);
        btnDelivery = findViewById(R.id.btnDeliveryReq);
        btnDelivery.setOnClickListener(this);
        btnShowUserDetails = findViewById(R.id.btnShowUserDetails);
        btnShowUserDetails.setOnClickListener(this);
        ivCusPhoneCall = findViewById(R.id.cusPhoneCallID);

        //todo for review
        reviewLayout = findViewById(R.id.reviewLayout);
        tvReview = findViewById(R.id.tvCusReviewId);
        ratingBar = findViewById(R.id.ratingBarId);
        btnReviewClose = findViewById(R.id.btnCloseRevId);
        btnReviewClose.setOnClickListener(this);

        // todo firebase auth
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference()
                .child("BdGas")
                .child("Distributor")
                .child("Profile")
                .child(userId);

        initMap();
        onNavBarLoad();
        getAssignCustomer();


    }

    private void initMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDis);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DistributorMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        } else {
            mapFragment.getMapAsync(this);
        }
    }

    public boolean checkUsePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CALL);
            }
            return false;
        } else {
            return true;
        }
    }

    private void getAssignCustomer() {
        customerRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(userId);
        customerRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    key = dataSnapshot.getKey();
                    orderDetails = dataSnapshot.getValue(OrderDetails.class);
                    cusReqLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(DistributorMapsActivity.this, "New Request Arrive", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DistributorMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        }
        builtGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    private synchronized void builtGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(1000);
        myLocationRequest.setFastestInterval(1000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DistributorMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, myLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DistributorMapsActivity.this);
                    builder.setTitle("Error ...!!!");
                    builder.setMessage("Permission invalid");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                    });
                    builder.show();
//                    Toast.makeText(this, "Permission invalid", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        if (marker != null) {
            marker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14), 2000, null);
        marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(latLng).title("Distributor")
        );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.navDisProfileId: {
                startActivity(new Intent(DistributorMapsActivity.this, ProfileDistributor.class));
            }
            break;
            case R.id.navDisProductId: {
                startActivity(new Intent(DistributorMapsActivity.this, ProductsActivity.class));
            }
            break;

            case R.id.navDisProductListId: {
                startActivity(new Intent(DistributorMapsActivity.this, ProductList.class));
            }
            break;
//            case R.id.navDisQuery: {
//                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DistributorMapsActivity.this);
//                builder.setTitle("Notification");
//                builder.setMessage("Frequent Ask questions will Published later");
//                builder.setCancelable(false);
//                builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
//                builder.show();
//            }
//            break;
            case R.id.navDisLogoutId: {
                AlertDialog.Builder builder = new AlertDialog.Builder(DistributorMapsActivity.this);
                builder.setTitle("Alert ...!!!");
                builder.setMessage("Are You Sure to Logout??");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    auth.signOut();
                    startActivity(new Intent(DistributorMapsActivity.this, SignInActivity.class));
                });
                builder.setNegativeButton("NO", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();

            }
            break;
        }
        return false;
    }

    private void onNavBarLoad() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    distributor = dataSnapshot.getValue(Distributor.class);
                    nvName.setText(distributor.getName());
                    nvEmail.setText(distributor.getEmail());
                    Glide.with(DistributorMapsActivity.this).load(distributor.getProfileImage()).into(nvImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnAccept) {
            reqAcceptLayout.setVisibility(View.VISIBLE);
            cusReqLayout.setVisibility(View.GONE);
            acceptRequest(key);
        }
        if (v == btnShowUserDetails) {
            userDetailsLayout.setVisibility(View.VISIBLE);
            proDetailsLayout.setVisibility(View.GONE);
        }

        if (v == btnDelivery) {
            reqAcceptLayout.setVisibility(View.GONE);
            FirebaseDatabase.getInstance().getReference()
                    .child("Activity").child("Distributor").child(userId)
                    .child(key).setValue(true)
                    .addOnSuccessListener(aVoid -> {
//                        Toast.makeText(DistributorMapsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder alert = new AlertDialog.Builder(DistributorMapsActivity.this);
                        alert.setTitle("Success");
                        alert.setMessage("Your Service Delivery Has Been Successful");
                        alert.setCancelable(false);
                        alert.setPositiveButton("Ok", (dialog, which) -> {
                            getCustomerReview();
                            dialog.dismiss();
                        });
                        alert.show();
                    });
        }

        if (v == btnReviewClose){
            reviewRef = FirebaseDatabase.getInstance().getReference().child("Review");
            reviewRef.removeValue();
            reviewLayout.setVisibility(View.GONE);
            removeCustomer();

        }

        if (v == btnCancel) {
            Toast.makeText(getApplicationContext(), "Clear All data", Toast.LENGTH_LONG).show();
            cancelRequest();
        }
        if (v == btnOpComplete) {
            Toast.makeText(getApplicationContext(), "Button Clear Working Later", Toast.LENGTH_LONG).show();
            //clearCustomer();
        }
        if (v == ivCusPhoneCall) {
            makePhoneCall();
        }
    }

    private void getCustomerReview() {
        reviewRef = FirebaseDatabase.getInstance().getReference()
                .child("Review");
        reviewRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    reviewLayout.setVisibility(View.VISIBLE);
                    customerReview = dataSnapshot.getValue(CustomerReview.class);
                    tvReview.setText("Customer Review : "+customerReview.getReview());
                    ratingBar.setRating(Float.valueOf(customerReview.getUserRating()));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void acceptRequest(String key) {
        cusReqRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest")
                .child(userId).child(key);
        cusReqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    orderDetails = dataSnapshot.getValue(OrderDetails.class);
                    reqName.setText("Customer Name : " + orderDetails.getCusName());
                    proName.setText("Product Name : " + orderDetails.getProName());
                    unitPrice.setText("Unit Price : " + orderDetails.getUnitPrice());
                    reqQuantity.setText("Product Quantity : " + orderDetails.getQuantity());
                    totalPrice.setText("Total Price : " + orderDetails.getTotalBill());
                    reqDeliveryAdd.setText("Delivery Address : " + orderDetails.getAddress());
                    reqPhone.setText(orderDetails.getContact());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makePhoneCall() {
        String number = reqPhone.getText().toString();
        if (number.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DistributorMapsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(DistributorMapsActivity.this, "Phone Number Empty ", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCustomer() {
        reqName.setText("");
        reqPhone.setText("");
        proName.setText("");
        unitPrice.setText("");
        reqQuantity.setText("");
        reqDeliveryAdd.setText("");
        totalPrice.setText("");
        cancelRequest();
    }

    private void cancelRequest() {
        userDetailsLayout.setVisibility(View.GONE);
        proDetailsLayout.setVisibility(View.VISIBLE);
        cusReqLayout.setVisibility(View.GONE);
        cusReqReff = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(userId);
        cusReqReff.removeValue();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }
}

