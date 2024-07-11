package com.myapp.otterofferings;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private final int FINE_PERMISSION_CODE = 1;
    private GoogleMap myMap;
    private SearchView mapSearchView;
    private Marker searchMarker;
    Location currentLocation;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        }

        mapSearchView = findViewById(R.id.mapSearch);
        Button saveButton = findViewById(R.id.saveButton); // Assuming the id of your save button

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocation(); // Call method to save location
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;

                Geocoder geocoder = new Geocoder(Maps.this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    // Remove previous marker if exists
                    removeSearchMarker();

                    // Add new marker and move camera
                    searchMarker = myMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    // Initialize currentLocation if null
                    if (currentLocation == null) {
                        currentLocation = new Location("");
                    }

                    // Set latitude and longitude
                    currentLocation.setLongitude(address.getLongitude());
                    currentLocation.setLatitude(address.getLatitude());

                } else {
                    Toast.makeText(Maps.this, "Location not found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            // Prompt user to enable location services
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d("MapsActivity", "Location found: " + location.getLatitude() + ", " + location.getLongitude());
                    currentLocation = location;
                    updateMapWithLocation(location);
                } else {
                    Log.d("MapsActivity", "Location is null");
                    Toast.makeText(Maps.this, "Could not get location", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("MapsActivity", "Failed to get location", e);
            Toast.makeText(Maps.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showLocationEnableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Services Not Enabled")
                .setMessage("Please enable location services to use this feature")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open settings to enable location services
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle if user cancels
                        Toast.makeText(Maps.this, "Location services disabled", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void updateMapWithLocation(Location location) {
        if (myMap != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            myMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

            // Convert the location to an address using Geocoder
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    // Save the address to the user profile and sync with the database
                    saveAddressToProfile(address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng malaysia = new LatLng(3.140853, 101.693207);
        myMap.addMarker(new MarkerOptions().position(malaysia).title("Malaysia"));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(malaysia));

        // Enable the "My Location" button on the map UI
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Set a listener for the My Location button
        myMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Handle the My Location button click here
                // Check if location permission is granted
                if (ActivityCompat.checkSelfPermission(Maps.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(Maps.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Maps.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
                    return true;
                }

                // Get current location from fusedLocationProviderClient
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(Maps.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        } else {
                            Toast.makeText(Maps.this, "Current location is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                return true; // Return true to indicate that the click event is handled
            }
        });

        // Set listener for map click events to place marker
        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Remove previous marker if exists
                myMap.clear();

                // Add marker at the clicked location
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Selected Location");
                myMap.addMarker(markerOptions);

                // Update currentLocation with marker's location
                currentLocation = new Location("");
                currentLocation.setLatitude(latLng.latitude);
                currentLocation.setLongitude(latLng.longitude);
            }
        });

        // Call getLastLocation after the map is ready
        getLastLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location-related operations
                getLastLocation();
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveLocation() {
        if (currentLocation != null) {
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addressList = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    String addressString = address.getAddressLine(0);

                    // Update current user's address locally
                    User currentUser = FirebaseHandler.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.setSavedAddress(addressString);
                        FirebaseHandler.setCurrentUser(currentUser);
                        saveAddressToProfile(address);
                        Toast.makeText(this, "Location saved: " + addressString, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No location available to save", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check location services again when the activity resumes
        if (!isLocationEnabled()) {
            showLocationEnableDialog();
        }
    }

    private void saveAddressToProfile(Address address) {
        FirebaseHandler.USERS_REF.child(FirebaseHandler.getCurrentUser().getUserID()).setValue(FirebaseHandler.getCurrentUser());
    }

    private void removeSearchMarker() {
        if (searchMarker != null) {
            searchMarker.remove();
            searchMarker = null; // Clear the reference
        }
    }

    public void backButtonHandler(View v) {
        finish();
    }
}
