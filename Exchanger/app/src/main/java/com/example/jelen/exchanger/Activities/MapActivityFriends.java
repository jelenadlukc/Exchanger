package com.example.jelen.exchanger.Activities;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.SearchView;
import android.widget.Spinner;

import com.example.jelen.exchanger.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.HashMap;

public class MapActivityFriends extends FragmentActivity implements OnMapReadyCallback {


    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int ADD_POINTS_NEW_PARKING = 5;

    public GoogleMap googleMap;
    private MapView mMapView;
    public static HashMap<String, Marker> mapParkingIdMarker = new HashMap<String, Marker>();
    public static HashMap<String, Marker> mapUserIdMarker = new HashMap<String, Marker>();
    public static HashMap<String, Marker> mapFriendIdMarker = new HashMap<String, Marker>();

    private Circle distanceCircle;
  //  private SearchStrategy searchStrategy = null;

    private Dialog dialog;
    private Polyline direction;

    private SearchView search;
    private Spinner sSearchType;
    private FloatingActionButton btnCancelDirections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_friends);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);







    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
