package com.firebase.marks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.Rating;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private TextView locationRide;
    private TextView distanceRide;
    private TextView dateRide;
    private TextView nameUser;
    private TextView phoneUser;
    private TextView ridePrice;
    private ImageView imageUser;
    private GoogleMap mMap;
    private LatLng destinationLatLng, pickupLatLng;
    private String rideId, currentUserId, passengerID, driverID, userDriverOrPassenger;
    private SupportMapFragment mMapFragment;
    private DatabaseReference historyRideinfoDb;
    private RatingBar ratingBar;
    private String distance;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        locationRide = findViewById(R.id.rideLocation);
        distanceRide= findViewById(R.id.rideDistance);
        dateRide = findViewById(R.id.rideDate);
        nameUser = findViewById(R.id.userName);
        phoneUser = findViewById(R.id.userPhone);
        imageUser = findViewById(R.id.userImage);
        ratingBar = findViewById(R.id.ratingBar);
        ridePrice = findViewById(R.id.ridePrice);
        rideId = getIntent().getExtras().getString("rideId");
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        polylines = new ArrayList<>();
        historyRideinfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideIdInformation();

        mMapFragment.getMapAsync(this);
    }

    private void getRideIdInformation() {
        historyRideinfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot child:dataSnapshot.getChildren()) {
                        if(child.getKey().equals("passenger")) {
                            passengerID = child.getValue().toString();
                            if(!passengerID.equals(currentUserId)) {
                                userDriverOrPassenger = "Drivers";
                                getUserInformation("Passengers", passengerID);
                            }
                        }
                        if(child.getKey().equals("driver")) {
                            driverID = child.getValue().toString();
                            if(!driverID.equals(currentUserId)) {
                                userDriverOrPassenger = "Passengers";
                                getUserInformation("Drivers", driverID);
                                displayCustomerRelatedObjects();
                            }
                        }
                        if(child.getKey().equals("timestamp")) {
                            dateRide.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if(child.getKey().equals("rating")) {
                            ratingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }
                        if(child.getKey().equals("ridePrice")) {
                            ridePrice.setText("Цена "+child.getValue().toString()+" сом");
                        }
                        if(child.getKey().equals("distance")) {
                            distance = child.getValue().toString();
                            distanceRide.setText(distance.substring(0, Math.min(distance.length(), 5)) + "км");
                        }
                        if(child.getKey().equals("destination")) {
                            locationRide.setText(child.getValue().toString());
                        }
                        if(child.getKey().equals("location")) {
                            pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destinationLatLng != new LatLng(0,0)){
                                getRouteToMarker();
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerRelatedObjects() {
        ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyRideinfoDb.child("rating").setValue(rating);
                DatabaseReference driverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("rating");
                driverRatingDb.child(rideId).setValue(rating);
            }
        });
    }

    private void getUserInformation(String otherUserDriverOrPassenger, String otherUserID) {
        DatabaseReference otherUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrPassenger).child(otherUserID);
        otherUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null) {
                        nameUser.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null) {
                        phoneUser.setText(map.get("phone").toString());
                    }if(map.get("profileImageUrl") != null) {
                        Glide.with(getApplication())
                                .load(map.get("profileImageUrl").toString())
                                .into(imageUser);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
        return date;
    }
    private String apiKey = "AIzaSyDGeRI8ZxuPUqSWsQpc74weaPhalo_uRh0";
    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .key(apiKey)
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng, destinationLatLng)
                .build();
        routing.execute();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorPrimary,R.color.colorAccent,R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Что-то пошло не так, попробуйте еще раз", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.05);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Точка посадки"));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Точка высадки"));

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines() {
        for (Polyline line: polylines) {
            line.remove();
        }
        polylines.clear();
    }
}