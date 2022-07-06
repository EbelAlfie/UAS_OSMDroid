package com.moop.uas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import org.osmdroid.config.Configuration ;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity implements MapEventsReceiver {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView = null ;
    private Double latitude = 0.0 ;
    private Double longitude = 0.0 ;
    private Scanner scan = null;
    private InputStream input = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context cont = getApplicationContext();
        Configuration.getInstance().load(cont, PreferenceManager.getDefaultSharedPreferences(cont));
        setContentView(R.layout.activity_main);

        /**Ambil longitude dan lattitude dari file text di folder res*/
        try{
            input = cont.getResources().openRawResource(R.raw.location) ;
            scan = new Scanner(input) ;
            scan.useDelimiter(":|\\n");
            getLongitudeLatitude(scan) ;
        }catch(Exception e){
            Toast.makeText(cont, "Error " + e.toString(), Toast.LENGTH_SHORT).show();
        }finally{
            scan.close() ;
        }

        mapView = (MapView) findViewById(R.id.peta) ;
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(cont, this) ;
        mapView.getOverlays().add(0, mapEventsOverlay);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        //Permissionsnya
        String[] permissionStrings = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissionsIfNecessary(permissionStrings); //permissions

        /**Seandainya latitude dan longitude tidak ada, maka tampilan defaultnya mengarah pada
          Longitude dan latitude Indonesia sebagai center*/
        mapView.getController().setZoom(5.5);
        GeoPoint g = new GeoPoint(5.00,120.00,0);
        mapView.getController().setCenter(g);

        /**Set tampilan map mengikuti longitude dan latitude yang telah dibaca dari file text*/
        if(longitude != 0.0 && latitude != 0.0){
            GeoPoint startPoin = new GeoPoint(latitude,longitude,0); //Ambil dari storage utk dijadikan starting point
            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoin); //set marker to be in posisi lat long start poin
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); //set anchornya

            /**TODO: Tampilkan alamatnya menggantikan Start pointo*/
            Geocoder placeNow = new Geocoder(getApplicationContext(), Locale.getDefault()) ;
            try {
                placeNow.getFromLocation(latitude, longitude, 1);
                startMarker.setTitle(placeNow.getFromLocation(latitude, longitude, 1).get(0).getAddressLine(0));
                mapView.getController().setCenter(startPoin); //Set center dari map sebagai posisi start point yang ada markernya sekarang
                mapView.getController().setZoom(19);
                mapView.getOverlays().add(startMarker); //Tampilkan markernya sebagai map overlay (keiket longitude latitude supaya ikut gerak juga kalau peta gerak)
                mapView.invalidate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(cont, "No latitude and longitude", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLongitudeLatitude(Scanner scan) {
        String longOrLat, longitudeOne, latitudeOne;
        while(scan.hasNextLine()){
            if(latitude == 0.0 || longitude == 0.0){
                longOrLat = scan.next();
                switch(longOrLat.toLowerCase()){
                    case "longitude":
                        longitudeOne = scan.next() ;
                        longitude = Double.parseDouble(longitudeOne) ;
                        break;
                    case "latitude":
                        latitudeOne = scan.next();
                        latitude = Double.parseDouble(latitudeOne) ;
                        break;
                    default:
                        Toast.makeText(this, "Wrong input", Toast.LENGTH_SHORT).show();
                        return;
                }
            }else{
                break;
            }
        }
    }

    private void requestPermissionsIfNecessary(String[] permissionStrings) {
        ArrayList<String> ungranted = new ArrayList<>()  ;
        for(String onePermission : permissionStrings){
            if(ContextCompat.checkSelfPermission(getApplicationContext(),onePermission) != PackageManager.PERMISSION_GRANTED){
                ungranted.add(onePermission) ;
            }
        }
        if(ungranted.size() > 0){
            ActivityCompat.requestPermissions(
                    this,
                    ungranted.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * @param p the position where the event occurred.
     * @return true if the event has been "consumed" and should not be handled by other objects.
     */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    /**
     * @param p the position where the event occurred.
     * @return true if the event has been "consumed" and should not be handled by other objects.
     */
    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}