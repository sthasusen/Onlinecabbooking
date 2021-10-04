package com.example.onlinecabbooking.driverui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.onlinecabbooking.MainActivity;
import com.example.onlinecabbooking.R;
import com.example.onlinecabbooking.SettingActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;

    private FirebaseAuth mAuth;
    private FirebaseUser dCurrentUser;
    private Boolean CurrentLogoutDriverStatus = false;


    private DatabaseReference AssignedCustomerRef, AssignedCustomerPickUpRef;
    private String driverID;
    private String customerID = "";
    Marker pickupMaker;

    private ValueEventListener AssignedCustomerPickUpRefListener;

    private TextView txtname, txtphone;
    private CircleImageView profilepic;
    private RelativeLayout relativeLayout;

    //    for floating button
    FloatingActionButton main_fab, btn_driver_setting, btn_driver_logout, main_btn_payment;
    Animation fabopen, fabclose, fabRclockwise, fabRanticlockwise;
    boolean isOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);


        main_fab = findViewById(R.id.main_fab);
        btn_driver_setting = findViewById(R.id.main_btn_setting);
        btn_driver_logout = findViewById(R.id.main_btn_logout);
        main_btn_payment = findViewById(R.id.main_btn_payment);

        fabopen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabclose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fabRclockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        fabRanticlockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anitclockwise);


        mAuth = FirebaseAuth.getInstance();
        dCurrentUser = mAuth.getCurrentUser();
        driverID = mAuth.getCurrentUser().getUid();


        txtname = findViewById(R.id.customer_name);
        txtphone = findViewById(R.id.customer_phone);
        profilepic = findViewById(R.id.customer_profile_image);
        relativeLayout = findViewById(R.id.rel2);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        main_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    btn_driver_logout.startAnimation(fabclose);
                    btn_driver_setting.startAnimation(fabclose);
                    main_btn_payment.startAnimation(fabclose);

                    main_fab.startAnimation(fabRanticlockwise);
                    btn_driver_logout.setClickable(false);
                    btn_driver_setting.setClickable(false);
                    main_btn_payment.setClickable(false);
                    isOpen = false;
                } else {
                    btn_driver_logout.startAnimation(fabopen);
                    btn_driver_setting.startAnimation(fabopen);
                    main_btn_payment.startAnimation(fabopen);

                    main_fab.startAnimation(fabRclockwise);

                    btn_driver_logout.setClickable(true);
                    btn_driver_setting.setClickable(true);
                    main_btn_payment.setClickable(true);
                    isOpen = true;

                }

            }
        });


        btn_driver_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMapsActivity.this, SettingActivity.class);
                intent.putExtra("type", "Drivers");
                startActivity(intent);
            }
        });


        btn_driver_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CurrentLogoutDriverStatus = true;
                DissconnectTheDriver();

                mAuth.signOut();

                LogoutDriver();
            }
        });

        GetAssignedCustomerRequest();

    }

    private void GetAssignedCustomerRequest() {

        AssignedCustomerRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverID).child("CustomerRideID");

        AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    customerID = snapshot.getValue().toString();
                    GetAssignedCustomerPickUpLocation();

                    relativeLayout.setVisibility(View.VISIBLE);
                    GetAssignedPassengerInformation();


                } else {
                    customerID = "";
                    if (pickupMaker != null) {
                        pickupMaker.remove();
                    }
                    if (AssignedCustomerPickUpRefListener != null) {
                        AssignedCustomerPickUpRef.removeEventListener(AssignedCustomerPickUpRefListener);

                    }

                    relativeLayout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void GetAssignedCustomerPickUpLocation() {

        AssignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("Customer Request")
                .child(customerID).child("l");

        AssignedCustomerPickUpRefListener = AssignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    List<Object> customerLocationMap = (List<Object>) snapshot.getValue();

                    double LocationLat = 0;
                    double LocationLng = 1;


                    if (customerLocationMap.get(0) != null) {
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());

                    }
                    if (customerLocationMap.get(1) != null) {
                        LocationLng = Double.parseDouble(customerLocationMap.get(1).toString());

                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    pickupMaker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Passenger Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_cab)));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buildGoogleAPIClient();
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (getApplicationContext() != null) {
            lastlocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();


            DatabaseReference DriverAvailibilityReferences = FirebaseDatabase.getInstance().getReference().child("Driver Available");
            GeoFire geoFireAvailibility = new GeoFire(DriverAvailibilityReferences);

            DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
            GeoFire geoFireworking = new GeoFire(DriverWorkingRef);

            switch (customerID) {
                case "":
                    geoFireworking.removeLocation(userID);
                    geoFireAvailibility.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailibility.removeLocation(userID);
                    geoFireworking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!CurrentLogoutDriverStatus) {
            DissconnectTheDriver();
        }

    }

    private void DissconnectTheDriver() {

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriverAvailibilityReferences = FirebaseDatabase.getInstance().getReference().child("Driver Available");

        GeoFire geoFire = new GeoFire(DriverAvailibilityReferences);
        geoFire.removeLocation(userID);

    }

    private void LogoutDriver() {
        Intent intent = new Intent(DriverMapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void GetAssignedPassengerInformation() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();
                    txtname.setText(name);
                    txtphone.setText(phone);

                    if (snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilepic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}