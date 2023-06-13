package com.example.logicgame;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class GPSManager implements LocationListener {
    Context context;
    boolean isGPSEnabled = false;
    Location location = null; // location
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; // 10 metri
    private static final long MIN_TIME_BW_UPDATES = 200 * 10 * 1; // 2 secondi
    protected LocationManager locationManager;
    private String address = "";

    public GPSManager(Context context) {
        this.context = context;
        location = getLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        location = getLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGPSEnabled) {
                return null;
            }
            else {
                if (ActivityCompat.checkSelfPermission((Activity)context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity)context, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, 10);
                }
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        setGeoNames();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }
    public double getLatitude() {
        return location.getLatitude();
    }
    public double getLongitude() {
        return location.getLongitude();
    }

    private void setGeoNames() {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(getLatitude(), getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAddress(){
        return address;
    }

}
