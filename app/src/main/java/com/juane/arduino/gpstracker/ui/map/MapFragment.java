package com.juane.arduino.gpstracker.ui.map;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.gps.GPSDirection;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {
    private static final String TAG = "MapFragment";

    private GoogleMap mMap;

    private ArrayList<Marker> markers = new ArrayList<>();

    private TextView selectedDayTextView;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);


        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        selectedDayTextView = root.findViewById(R.id.daySelectedTextView);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map1);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume() {
        //Log.i(TAG, "Fragment resumed..");
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in default first location (random)
        LatLng myLocation = new LatLng(30.1809411, -3.8262913);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
    * Add markers to map, avoiding add similar or equals locations
    *
    * @param gpsReads: JSON array of locations
    */
    public void addMarkers(JSONArray gpsReads) throws JSONException {
        if(gpsReads != null && gpsReads.length()>0) {
            //JSONObject lastDirectionRAW, currentDirectionRAW;
            GPSDirection lastGpsRead, currentGpsRead;

            lastGpsRead = new GPSDirection(gpsReads.getJSONObject(0), getActivity().getApplicationContext());
            addMarker(lastGpsRead);

            if(gpsReads.length() > 1) {
                for (int i = 1; i < gpsReads.length() - 1; i++) {
                    currentGpsRead = new GPSDirection(gpsReads.getJSONObject(i), getActivity().getApplicationContext());

                    if (!currentGpsRead.isEqual(lastGpsRead)) {
                        addMarker(currentGpsRead);
                        lastGpsRead = new GPSDirection(gpsReads.getJSONObject(i), getActivity().getApplicationContext());
                    }


                }
            }else{
                addMarker(lastGpsRead);
            }
        }
    }

    /**
     * Add marker to map, avoiding add similar or equals locations
     *
     * @param gpsRead: {@link GPSDirection} location
     */
    public void addMarker(GPSDirection gpsRead) {
        if (gpsRead != null) {
            if (markers.size() > 0) {
                markers.get(markers.size() - 1).setIcon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            LatLng newMarker = new LatLng(gpsRead.getLatitude(), gpsRead.getLongitude());

            markers.add(mMap.addMarker(new MarkerOptions().position(newMarker).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Device location: " + gpsRead.getDate().toString())));

            // For zooming automatically to the location of the marker
            CameraPosition cameraPosition = new CameraPosition.Builder().target(newMarker).zoom(16).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public void clearMarkers() {
        if(markers != null) {
            if (markers.size() > 0) {
                for (Marker m : markers) {
                    m.remove();
                }

                markers.clear();
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(getContext(), "Moving to your location..", Toast.LENGTH_SHORT).show();

        return false;
    }

    public void setSelectedDayTextView(String daySelected){
        LocalDate date =  LocalDate.parse(daySelected, DateTimeFormatter.ofPattern("yyyyMMdd"));//LocalDate.parse("dd/mm/yyyy");
        selectedDayTextView.setText("Day selected: " + date.format(DateTimeFormatter.ofPattern("dd-MM-YYYY")));
    }
}