package softmaticbd.com.bdgas.MapActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import softmaticbd.com.bdgas.Model.Customer;
import softmaticbd.com.bdgas.Model.Product;
import softmaticbd.com.bdgas.Operation.CustomerOrder;
import softmaticbd.com.bdgas.Operation.FeedbackActivity;
import softmaticbd.com.bdgas.Profile.ProfileCustomer;
import softmaticbd.com.bdgas.R;
import softmaticbd.com.bdgas.SignActivity.SignInActivity;

public class CustomerMapsActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ProgressDialog pDialog;

    // todo for firebase db
    private FirebaseAuth auth;
    private DatabaseReference reference, deliveryRef;
    private DatabaseReference findRef;
    private DatabaseReference findDistributorRef;
    private DatabaseReference cusReqRef;
    private DatabaseReference productRef;
    private Task<Void> assignDisRef;
    private DatabaseReference cusOperationRef;
    private String userId;
    private FirebaseUser user;
    private ValueEventListener disLocationVEL;

    // todo for map integration
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest myLocationRequest;
    private Location myLocation;
    private Marker cusMarker, disMarker;
    private SupportMapFragment mapFragment;

    //TODO GeoFire
    private GeoFire geoFire;
    private GeoQuery geoQuery;

    // todo App dependency
    private Customer customer;
    private String distributorId;
    private static final int CALL = 1;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorBrown, R.color.colorPrimary, R.color.colorOrange, R.color.colorDeepAsh};

    // todo for navLoad
    private TextView nvName, nvEmail;
    private ImageView nvImage;
    private View view;

    // todo utils
    private String TAG = "Response";
    private double radius = 5;
    private final int ACCESS_FINE_LOCATION = 1;
    private boolean customerRequest = false;
    private boolean foundDistributor = false;
    private List<String> keys = new ArrayList<>();
    private Button btnFindOption;
    private List<Marker> markers = new ArrayList<>();

    //todo for operation
    private LinearLayout productLayout;
    private Product product;
    private String disKey;
    private int disIndex;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);

        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
        view = navigationView.getHeaderView(0);

        btnFindOption = findViewById(R.id.btnFindOption);
        btnFindOption.setOnClickListener(this);
        //todo for nav load
        nvName = view.findViewById(R.id.navNameId);
        nvEmail = view.findViewById(R.id.navEmailId);
        nvImage = view.findViewById(R.id.navProfileImageId);

        //todo progress Bar
        pDialog = new ProgressDialog(CustomerMapsActivity.this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data.....");
        pDialog.setCancelable(false);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            checkUsePermission();
        }


        // todo firebase db
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("BdGas").child("Customer").child("Profile").child(userId);

        initMap();
        onNavBarLoad();
    }

    private void initMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        builtGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
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
    public void onLocationChanged(Location location) {
        myLocation = location;
        if (cusMarker != null) {
            cusMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14), 1000, null);
        cusMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng).title("Customer")
        );
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(1000);
        myLocationRequest.setFastestInterval(1000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapsActivity.this);
                    builder.setTitle("Error ...!!!");
                    builder.setMessage("Permission invalid");
                    builder.setPositiveButton("OK", (dialog, which) -> {
                    });
                    builder.show();
                }
            }
            break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.navProfileId: {
                startActivity(new Intent(getApplicationContext(), ProfileCustomer.class));
            }
            break;
//            case R.id.navReviewId: {
//                startActivity(new Intent(CustomerMapsActivity.this, FeedbackActivity.class));
//            }
//            break;
            case R.id.navQueryId: {
                displayDialogMessage("Notify","Frequently Ask Questions will published Soon..");
            }
            break;

            case R.id.navLogoutId: {
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapsActivity.this);
                builder.setTitle("Alert ...!!!");
                builder.setMessage("Are You Sure to Logout??");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    auth.signOut();
                    startActivity(new Intent(CustomerMapsActivity.this, SignInActivity.class));
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
                    customer = dataSnapshot.getValue(Customer.class);
                    nvName.setText(customer.getName());
                    nvEmail.setText(customer.getEmail());
                    Glide.with(CustomerMapsActivity.this).load(customer.getProfileImage()).into(nvImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(final View v) {

        if (v == btnFindOption) {
            customerRequest = true;
            pDialog.show();
            findRef = FirebaseDatabase.getInstance().getReference().child("FindRequest").child("Customer");
            geoFire = new GeoFire(findRef);
            geoFire.setLocation(userId, new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), (key, error) ->
                    getAllDistributor());
        }
    }

    private void getAllDistributor() {
        findDistributorRef = FirebaseDatabase.getInstance().getReference().child("Location").child("Distributor");
        geoFire = new GeoFire(findDistributorRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                distributorId = key;
                double latitude = location.latitude;
                double longitude = location.longitude;
                keys.add(distributorId);
                addMarker(new LatLng(latitude, longitude));
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void addMarker(LatLng latLng) {
        disMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .position(latLng).title("Distributor")
        );
        markers.add(disMarker);
        pDialog.dismiss();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        disIndex = markers.indexOf(marker);
        if (!marker.equals(this.cusMarker)) {
            startActivity(new Intent(this, CustomerOrder.class).putExtra("disKey", keys.get(disIndex)));
        }
        return false;
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

    private void displayDialogMessage (String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapsActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
