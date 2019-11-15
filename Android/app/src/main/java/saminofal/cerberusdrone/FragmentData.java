package saminofal.cerberusdrone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static saminofal.cerberusdrone.MainActivity.BluetoothDeviceState;


public class FragmentData extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener {


    /*
     * ********************************** *********** **********************************
     * ********************************** Declaration **********************************
     * ********************************** *********** **********************************
     */
    // Global variables
    public Context context;
    // As per Android Fragment documentation an empty constructor should be defined
    public FragmentData(){}
    public static FragmentData newInstance() {
        return new FragmentData();
    }
    // Layout elements
    public TextView RXTitle, MapTitle, MapExtraTxt,
            FragDataTxtMsg, FragDataTabSwitch,
            GraphTitle, GPSTitle, GraphMsg;
    public static TextView GPS_Fix_State;
    public static TextView RXData;
    public static Switch Graph_Switch;
    public static ImageButton GraphBtn1, GraphBtn2, GraphBtn3, GraphBtn4;
    public static RelativeLayout FragDataLayoutMsg, FragDataLayoutCont;
    public static RelativeLayout GraphMsgLayout;
    public String TAG;
    // Graph view elements
    public static GraphView GraphPlotter;
    public static  LineGraphSeries<DataPoint> GraphSeries;
    // Google API elements
    public Boolean mLocationPermissionsGranted = false,
            LocationServicesState = false;
    public GoogleMap mMap;
    public ImageButton GPS_FindLoc, Map_ClearMarkers, Map_Flyover;
    public AutoCompleteTextView MapSearch;
    public static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final float DEFAULT_ZOOM = 15f;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    public GoogleApiClient mGoogleApiClient;
    public FusedLocationProviderClient mFusedLocationProviderClient;
    public static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    public double[] MarkerLatLngVal = {0, 0};
    public double[] DeviceLatLngVal = {0, 0};





    /*
     * ********************************** ******************* **********************************
     * ********************************** Lifecycle Callbacks **********************************
     * ********************************** ******************* **********************************
     */
    /**
     * Since Fragment is Activity dependent you need Activity context in various cases
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = getActivity();
    }


    /**
     * Your entire fragment uses context instead of getActivity()
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     *  Inflate layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);
        // Do not populate activity related tasks here
        return view;
    }


    /**
     *  The code should be called on onViewCreated method
     * **/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            // Define elements here:
            RXTitle = view.findViewById(R.id.RXTitle);
            RXData = view.findViewById(R.id.RXData);
            GPSTitle = view.findViewById(R.id.GPSTitle);
            MapTitle = view.findViewById(R.id.MapTitle);
            GraphTitle = view.findViewById(R.id.GraphTitle);
            Graph_Switch = view.findViewById(R.id.Graph_Switch);
            GraphMsg = view.findViewById(R.id.GraphMsg);
            GraphPlotter = view.findViewById(R.id.GraphPlotter);
            GraphBtn1 = view.findViewById(R.id.GraphBtn1);
            GraphBtn2 = view.findViewById(R.id.GraphBtn2);
            GraphBtn3 = view.findViewById(R.id.GraphBtn3);
            GraphBtn4 = view.findViewById(R.id.GraphBtn4);
            GraphMsgLayout = view.findViewById(R.id.GraphMsgLayout);
            GPS_Fix_State = view.findViewById(R.id.GPS_Fix_State);
            RXData.setMovementMethod(new ScrollingMovementMethod());
            MapSearch = view.findViewById(R.id.Map_InputSrc);
            GPS_FindLoc = view.findViewById(R.id.Map_FindLocation);
            Map_ClearMarkers = view.findViewById(R.id.Map_ClearMarkers);
            Map_Flyover = view.findViewById(R.id.Map_Flyover);
            MapExtraTxt = view.findViewById(R.id.MapExtraTxt);
            FragDataTxtMsg = view.findViewById(R.id.FragDataTxtMsg);
            FragDataTabSwitch = view.findViewById(R.id.FragDataTabSwitch);
            FragDataLayoutCont = view.findViewById(R.id.FragDataLayoutCont);
            FragDataLayoutMsg = view.findViewById(R.id.FragDataLayoutMsg);

            // Settings switch tab
            FragDataTabSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).SwitchTabs(3);
                }
            });

            // Toggle graph data
            Graph_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((MainActivity)context).SendState(Graph_Switch, 'K', 'L');
                    if (isChecked) {
                        GraphMsgLayout.setVisibility(View.GONE);
                        MainActivity.GraphState = true;
                    }
                    else {
                        GraphMsgLayout.setVisibility(View.VISIBLE);
                        MainActivity.GraphState = false;
                        try {
                            GraphSeries.resetData(new DataPoint[]{});
                            GraphPlotter.getViewport().setXAxisBoundsManual(true);
                        }
                        catch (Exception e) {
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });

            // Switch between which data to plot
            GraphBtn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).SendChar('M');
                    GraphBtn1.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.DarkGray));
                    GraphBtn2.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn3.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn4.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                }
            });
            GraphBtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).SendChar('N');
                    GraphBtn1.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn2.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.DarkGray));
                    GraphBtn3.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn4.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                }
            });
            GraphBtn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).SendChar('O');
                    GraphBtn1.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn2.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn3.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.DarkGray));
                    GraphBtn4.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                }
            });
            GraphBtn4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity)context).SendChar('P');
                    GraphBtn1.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn2.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn3.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.Carbon));
                    GraphBtn4.setBackgroundColor(Objects.requireNonNull(getContext())
                            .getResources().getColor(R.color.DarkGray));
                }
            });

            // Initialize graph plotter
            GraphPlotter.getGridLabelRenderer().setHorizontalAxisTitleColor(Objects.
                    requireNonNull(getContext()).getResources().getColor(R.color.White));
            GraphPlotter.getGridLabelRenderer().setHorizontalLabelsColor(Objects.
                    requireNonNull(getContext()).getResources().getColor(R.color.White));
            GraphPlotter.getGridLabelRenderer().setVerticalAxisTitleColor(Objects.
                    requireNonNull(getContext()).getResources().getColor(R.color.White));
            GraphPlotter.getGridLabelRenderer().setVerticalLabelsColor(Objects.
                    requireNonNull(getContext()).getResources().getColor(R.color.White));
            GraphPlotter.getGridLabelRenderer().setGridColor(Objects.
                    requireNonNull(getContext()).getResources().getColor(R.color.TransWhite));
            GraphPlotter.getViewport().setXAxisBoundsManual(true);
            GraphPlotter.getViewport().setMinX(0);
            GraphPlotter.getViewport().setMaxX(100);
            GraphSeries = new LineGraphSeries<>();
            GraphPlotter.addSeries(GraphSeries);


            // Set search drop down list background color
            MapSearch.setDropDownBackgroundDrawable(new ColorDrawable(Objects
                    .requireNonNull(getContext()).getResources().getColor(R.color.White)));

            // Initialize Google Maps
            if(((MainActivity)context).isMapServicesOK()) {
                if(!mLocationPermissionsGranted) {
                    getLocationPermission();
                }
            }

            // Check connected device status to remove message
            if(BluetoothDeviceState) {
                FragDataLayoutMsg.setVisibility(View.GONE);
                FragDataLayoutCont.setVisibility(View.VISIBLE);
            }
            else {
                FragDataLayoutMsg.setVisibility(View.VISIBLE);
                FragDataLayoutCont.setVisibility(View.GONE);
            }

            // Font styling
            Typeface AileronsFont = Typeface.createFromAsset(context
                    .getAssets(), "fonts/Ailerons.ttf");
            RXTitle.setTypeface(AileronsFont);
            GPSTitle.setTypeface(AileronsFont);
            MapTitle.setTypeface(AileronsFont);
            GraphTitle.setTypeface(AileronsFont);
            GraphMsg.setTypeface(AileronsFont);
            FragDataTxtMsg.setTypeface(AileronsFont);
            FragDataTabSwitch.setTypeface(AileronsFont);

            setRetainInstance(true);
        }

        catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    /**
     *  Called to resume fragment
     */
    @Override
    public void onResume() {
        super.onResume();

    }





    /*
     * ********************************** *************** **********************************
     * ********************************** Google Maps API **********************************
     * ********************************** *************** **********************************
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Empty
    }


    /**
     * Google Map on-ready callback
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(context, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        // Set on long click listener for Google Map
        mMap.setOnMapLongClickListener(FragmentData.this);

        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json));
            if(!success) {
                Log.e(TAG, "Style parsing failed");
            }
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Get device location and initialize the map
        if (mLocationPermissionsGranted) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            MapInit();
        }
    }


    /**
     * Override on map long press - Drop a pin on the map
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        // Clear marker location array
        MarkerLatLngVal[0] = 0;
        MarkerLatLngVal[1] = 0;
        // Clear all markers
        mMap.clear();
        // Add a new marker
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Desired Location")
                .snippet("Latitude: " + latLng.latitude + '\n'
                        + "Longitude: " + latLng.longitude)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        MarkerLatLngVal[0] = latLng.latitude;
        MarkerLatLngVal[1] = latLng.longitude;
    }


    /**
     * Initialization of Google Map
     */
    public void MapInit() {
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(Objects.requireNonNull(getActivity()), FragmentData.this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(context, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        // Autocomplete text set list adapter
        MapSearch.setAdapter(mPlaceAutocompleteAdapter);

        MapSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });


        // Initialize map utilities on click listeners
        GPS_FindLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });
        Map_ClearMarkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                Toast.makeText(context
                        , "All markers are cleared."
                        , Toast.LENGTH_SHORT).show();
                // Clear marker location array
                MarkerLatLngVal[0] = 0;
                MarkerLatLngVal[1] = 0;
            }
        });
        Map_Flyover.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                // Calculate distance between two points
                double Haversine = Haversine(DeviceLatLngVal[0], MarkerLatLngVal[0]
                        , DeviceLatLngVal[1], MarkerLatLngVal[1]);
                if(isNaN(Haversine)) {
                    Toast.makeText(context
                            , "Touch and hold on the map to set a marker; additionally " +
                                    "make sure your location is determined."
                            , Toast.LENGTH_SHORT).show();
                    MapExtraTxt.setText("");
                }
                else if(Haversine < 1000) {
                    MapExtraTxt.setText("Distance: " + Haversine + " Meters");
                }
                else if(Haversine > 1000) {
                    DecimalFormat decimalFormat = new DecimalFormat(".####");
                    Haversine = Haversine / 1000;
                    decimalFormat.format(Haversine);
                    MapExtraTxt.setText("Distance: " + Haversine + " Kilometers");
                }

            }
        });

        // Hide keyboard
        hideSoftKeyboard();
    }


    /**
     * Calculate the distance between two points on the map using Haversine formula
     */
    public double Haversine(double startLat, double endLat,
                            double startLng, double endLng) {
        if (endLat == 0 || endLng == 0
                || startLat == 0 || startLng == 0) {
            return NaN;
        }
        else {
            int EarthRadius = 6371; // Approximate earth radius
            double dLat = Math.toRadians((endLat - startLat));
            double dLng = Math.toRadians((endLng - startLng));
            startLat = Math.toRadians(startLat);
            endLat = Math.toRadians(endLat);
            double Sigma = Math.pow(Math.sin(dLat/2), 2) + Math.cos(startLat) *
                    Math.cos(endLat) * Math.pow(Math.sin(dLng/2), 2);
            double Omega = 2 * Math.atan2(Math.sqrt(Sigma), Math.sqrt(1-Sigma));
            double Epsilon = EarthRadius * Omega;
            DecimalFormat decimalFormat = new DecimalFormat(".####");
            return Double.parseDouble(decimalFormat.format(
                    Epsilon * 1000));
        }
    }


    /**
     * Get location of searched places
     */
    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = MapSearch.getText().toString();

        Geocoder geocoder = new Geocoder(context);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }
        catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            // Clear all markers
            mMap.clear();
            // Clear marker location array
            MarkerLatLngVal[0] = 0;
            MarkerLatLngVal[1] = 0;
            Address address = list.get(0);
            //Toast.makeText(context, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
            MarkerLatLngVal[0] = address.getLatitude();
            MarkerLatLngVal[1] = address.getLongitude();
        }
    }


    /**
     * Get location of device
     */
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        LocationServicesState = ((MainActivity)context).checkGPS();

        if(LocationServicesState) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

            try{
                if(mLocationPermissionsGranted){

                    final Task location = mFusedLocationProviderClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: found location!");
                                // Clear device location array
                                DeviceLatLngVal[0] = 0;
                                DeviceLatLngVal[1] = 0;

                                Location currentLocation = (Location) task.getResult();

                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM, "My Location");

                                DeviceLatLngVal[0] = currentLocation.getLatitude();
                                DeviceLatLngVal[1] = currentLocation.getLongitude();

                            }else{
                                Log.d(TAG, "onComplete: current location is null");
                                Toast.makeText(context, "unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            catch (SecurityException e){
                Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
            }
        }
        else {
            Toast.makeText(context, "You need to enable location services first.", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Google Map move camera - Moves the map towards desired location
     */
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );

        LatLng coordinate = new LatLng(latLng.latitude, latLng.longitude);
        CameraUpdate locationCamera = CameraUpdateFactory
                .newLatLngZoom(coordinate, zoom);
        mMap.animateCamera(locationCamera);

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }


    /**
     * Hide keyboard
     */
    public void hideSoftKeyboard(){
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    /**
     * Request location permission
     */
    public void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(context.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                // Call BeginMap function
                BeginMap();
            }
            else {
                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    /**
     * Request map permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    // Call BeginMap function
                    BeginMap();
                }
            }
        }
    }


    /**
     * Begin Google Map and attach it to a map fragment
     */
    public void BeginMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);
    }


}