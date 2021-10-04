package com.example.onlinecabbooking.passengerui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.onlinecabbooking.MainActivity;
import com.example.onlinecabbooking.R;
import com.example.onlinecabbooking.SettingActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PassengerMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;

    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;

    private Button btn_call_cab;
    private String passengerID;
    private LatLng PassengerPickUpLocation;
    private int radius = 1;
    private Boolean driverFound = false, requestType = false;
    private String driverFoundID;

    private FirebaseAuth mAuth;
    private FirebaseUser dCurrentUser;
    Marker DriverMarker, pickupMarker;
    GeoQuery geoQuery;

    private DatabaseReference PassengerDatabaseRef;
    private DatabaseReference DriverAvailableRef;
    private DatabaseReference DriverRef;
    private DatabaseReference DriverLocationRef;

    private ValueEventListener DriverLocationRefListener;


    private TextView txtname,txtphone,txtcarname;
    private CircleImageView profilepic;
    private RelativeLayout relativeLayout;

    //    for floating button
    FloatingActionButton main_passenger_fab, btn_passenger_setting, btn_passenger_logout,main_p_btn_payment;
    Animation passenger_fabopen, passenger_fabclose, passenger_fabRclockwise, passenger_fabRanticlockwise;
    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_maps);


        mAuth = FirebaseAuth.getInstance();
        dCurrentUser = mAuth.getCurrentUser();
        passengerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        PassengerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Passenger Request");
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        //floating button for passenger
        main_passenger_fab = findViewById(R.id.main_p_fab);
        btn_passenger_setting = findViewById(R.id.main_p_btn_setting);
        btn_passenger_logout = findViewById(R.id.main_p_btn_logout);
        main_p_btn_payment = findViewById(R.id.main_p_btn_payment);

        passenger_fabopen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        passenger_fabclose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        passenger_fabRclockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        passenger_fabRanticlockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anitclockwise);


        btn_call_cab = findViewById(R.id.btn_call_cab);


        txtname = findViewById(R.id.driver_name);
        txtphone = findViewById(R.id.driver_phone);
        txtcarname = findViewById(R.id.driver_car_name);
        profilepic = findViewById(R.id.driver_profile_image);
        relativeLayout = findViewById(R.id.rell);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        main_passenger_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    btn_passenger_logout.startAnimation(passenger_fabclose);
                    btn_passenger_setting.startAnimation(passenger_fabclose);
                    main_p_btn_payment.startAnimation(passenger_fabclose);

                    main_passenger_fab.startAnimation(passenger_fabRanticlockwise);


                    btn_passenger_logout.setClickable(false);
                    btn_passenger_setting.setClickable(false);
                    main_p_btn_payment.setClickable(false);
                    isOpen = false;
                } else {
                    btn_passenger_logout.startAnimation(passenger_fabopen);
                    btn_passenger_setting.startAnimation(passenger_fabopen);
                    main_p_btn_payment.startAnimation(passenger_fabopen);

                    main_passenger_fab.startAnimation(passenger_fabRclockwise);


                    btn_passenger_logout.setClickable(true);
                    btn_passenger_setting.setClickable(true);
                    main_p_btn_payment.setClickable(true);
                    isOpen = true;

                }

            }
        });

        btn_passenger_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PassengerMapsActivity.this, SettingActivity.class);
                intent.putExtra("type","Customers");
                startActivity(intent);
            }
        });

        btn_passenger_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LogoutPassenger();
            }
        });


        btn_call_cab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (requestType) {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListener);

                    if (driverFound != null){
                        DriverRef  = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("CustomerRideID");

                        DriverRef.removeValue();

                        driverFound = null;
                    }

                    driverFound = false;
                    radius = 1;

                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(PassengerDatabaseRef);
                    geoFire.removeLocation(customerId);
                    if (pickupMarker != null)
                    {
                        pickupMarker.remove();
                    }

                    if (DriverMarker != null)
                    {
                        DriverMarker.remove();
                    }

                    btn_call_cab.setText("Call A Cab");
                    relativeLayout.setVisibility(View.GONE);

                } else {

                    requestType = true;

                    GeoFire geoFire = new GeoFire(PassengerDatabaseRef);
                    geoFire.setLocation(passengerID, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));

                    PassengerPickUpLocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                   pickupMarker =  mMap.addMarker(new MarkerOptions().position(PassengerPickUpLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_user)));

                    btn_call_cab.setText("Getting Your Cab...");
                    GetClosestDriverCab();
                }


            }
        });


    }


    private void GetClosestDriverCab() {
        GeoFire geoFire = new GeoFire(DriverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(PassengerPickUpLocation.latitude, PassengerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundID = key;

                    DriverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", passengerID);
                    DriverRef.updateChildren(driverMap);

                    GettingDriverLocation();
                    btn_call_cab.setText("Looking For Drive Location");

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius = radius + 1;
                    GetClosestDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void GettingDriverLocation() {

        DriverLocationRefListener = DriverLocationRef.child(driverFoundID).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && requestType) {
                    List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 1;
                    btn_call_cab.setText("Driver Found");


                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedDriverInformation();

                    if (driverLocationMap.get(0) != null) {
                        LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());

                    }
                    if (driverLocationMap.get(1) != null) {
                        LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());

                    }

                    LatLng DriverLatLng = new LatLng(LocationLat, LocationLng);
                    if (DriverMarker != null) {
                        DriverMarker.remove();
                    }

                    Location Location1 = new Location("");
                    Location1.setLatitude(PassengerPickUpLocation.latitude);
                    Location1.setLongitude(PassengerPickUpLocation.longitude);

                    Location Location2 = new Location("");
                    Location2.setLatitude(DriverLatLng.latitude);
                    Location2.setLongitude(DriverLatLng.longitude);

                    float Distance = Location1.distanceTo(Location2);

                    if (Distance < 90)
                    {
                        btn_call_cab.setText("Driver's Arrived");
                    }
                    else {
                        btn_call_cab.setText("Driver Found : " + String.valueOf(Distance));
                    }
                    
                    DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Cab is Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_cab)));

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleAPIClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onLocationChanged(Location location) {
        lastlocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setZoomControlsEnabled(true);

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
    }

    private void LogoutPassenger() {
        Intent intent = new Intent(PassengerMapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }


    private void getAssignedDriverInformation(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverFoundID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){

                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();
                    String CarName = snapshot.child("CarName").getValue().toString();
                    txtname.setText(name);
                    txtphone.setText(phone);
                    txtcarname.setText(CarName);

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