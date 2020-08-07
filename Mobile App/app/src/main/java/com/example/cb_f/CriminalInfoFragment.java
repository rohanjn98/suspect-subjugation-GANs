package com.example.cb_f;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Looper;
import android.provider.Settings;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Reference;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class CriminalInfoFragment extends Fragment{
    public static final int PERMISSION_ID= 44;
    private LocationManager locationManager;
    FusedLocationProviderClient mFusedLocationClient;

    private Button yes_button, no_button;
    private ProgressBar progressBar;
    private TextView nameInfo;
    private View view;


    private DatabaseReference criminalRef;
    private String criminalId;

    public CriminalInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        criminalId = getArguments().getString("Id");
        criminalRef = FirebaseDatabase.getInstance().getReference("Criminals").child(criminalId);
        Log.d(TAG, "onCreate: "+ criminalRef);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_criminal_info, container, false);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        final Toolbar toolbar = view.findViewById(R.id.tool_bar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        nameInfo = view.findViewById(R.id.name_info);

        criminalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot.toString());
                if(dataSnapshot!=null && dataSnapshot.child("name").getValue()!=null){
                    String name = dataSnapshot.child("name").getValue().toString();
                    nameInfo.setText(name);
                    toolbar.setTitle(name);
                    toolbar.setTitleTextColor(Color.WHITE);
                }
                if(dataSnapshot!=null &&dataSnapshot.child("photo").getValue()!=null){
                    Uri uri = Uri.parse(dataSnapshot.child("photo").getValue().toString());
                    Log.d(TAG, "onCreateView: "+ uri);
                    ImageView i = view.findViewById(R.id.crimImageView);
                    Glide.with(getActivity())
                            .load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressBar = view.findViewById(R.id.progressBar);
        yes_button = view.findViewById(R.id.yes);
        no_button = view.findViewById(R.id.no);

        yes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                getLastLocation();

            }
        });

        no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }



    /*  public void requestLocation(){
          if(checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                  != PackageManager.PERMISSION_GRANTED){
              if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)){
                  *//*new AlertDialog.Builder(getContext())
                        .setTitle("");*//*
            }
            else{

                Log.d(TAG, "requestLocation: RP");
                requestPermissions(new String[]{Manifest.permission
                        .ACCESS_COARSE_LOCATION},MY_PERMISSON_LOCATION_REQUEST);
                
            }
        }
        else{
            Log.d(TAG, "getLocation: Already Granted");
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2*60*1000){
                Log.d(TAG, "requestLocation: last known location "+ location.getLatitude() + " "+ location.getLongitude());
                uploadLocationToFirebase(location);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
                Log.d(TAG, "requestLocation: else");
            }
        }
    }
*/
    public void uploadLocationToFirebase(Location location){
        if(location!= null){
            MyLocation locationPair = new MyLocation(location.getLatitude(), location.getLongitude());
            criminalRef.child("locations").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(locationPair)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "Location is uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getContext(), task.getResult().toString(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    getActivity().onBackPressed();
                }
            });
        }
    }

   /* @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            Log.d(TAG, "onLocationChanged: " + location.getLatitude() + "Latitude   " + location.getLongitude()+"Logi");
            uploadLocationToFirebase(location);
            locationManager.removeUpdates(this);
        }
        else {
            Log.d(TAG, "onLocationChanged: Location not available");
        }
    }*/
/*
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onProviderDisabled(String provider) {}*/

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
        switch (requestCode){
            case MY_PERMISSON_LOCATION_REQUEST:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "onRequestPermissionsResult: permission Granted");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, this);
                    }
                }
                else{
                    Log.d(TAG, "onRequestPermissionsResult: Perission Denied");
                    //Todo Action to be performed if permission to access location is Denied.
                }
            }
        }
    }*/


    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    /*latTextView.setText(location.getLatitude()+"");
                                    lonTextView.setText(location.getLongitude()+"");*/
                                    uploadLocationToFirebase(location);
                                }
                            }
                        }
                );
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            /*latTextView.setText(mLastLocation.getLatitude()+"");
            lonTextView.setText(mLastLocation.getLongitude()+"");*/
            uploadLocationToFirebase(mLastLocation);
        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    public static class  MyLocation{
        private Double latitude, longitude;

        public MyLocation(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
