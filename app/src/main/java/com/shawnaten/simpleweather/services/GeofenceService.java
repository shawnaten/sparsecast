package com.shawnaten.simpleweather.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.shawnaten.simpleweather.App;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GeofenceService extends IntentService {
    private static final List<String> GEOFENCES = new ArrayList<>();
    static {
        GEOFENCES.add("geofence1");
    }
    private static final float RADIUS = 10000;

    private static final String TAG = "GeofenceControlService";

    @Inject
    Context context;
    @Inject
    GoogleApiClient googleApiClient;

    public GeofenceService() {
        super("GeofenceControlService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ((App) getApplication()).getServiceComponent().injectGeofenceControlService(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        googleApiClient.blockingConnect();

        LocationServices.GeofencingApi.removeGeofences(googleApiClient, GEOFENCES);

        Intent locationUpdaterIntent = new Intent(this, LocationReportService.class);
        startService(locationUpdaterIntent);

        LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        );

    }

    private GeofencingRequest getGeofencingRequest() {

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofence(new Geofence.Builder()
                        .setRequestId(GEOFENCES.get(0))
                        .setCircularRegion(
                                location.getLatitude(),
                                location.getLongitude(),
                                RADIUS
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()
        );
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
