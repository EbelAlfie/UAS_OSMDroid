package com.moop.uas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.config.Configuration ;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 23;
    private MapView mapView = null ;
    private Double latitude = 0.0 ;
    private Double longitude = 0.0 ;
    private Scanner scan = null;
    private EditText inputLatitude, inputLongitude;
    private Button save;
    private File dir ;
    private FileOutputStream fileOutputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context cont = getApplicationContext();
        Configuration.getInstance().load(cont, PreferenceManager.getDefaultSharedPreferences(cont));
        setContentView(R.layout.activity_main);
        save = (Button) findViewById(R.id.submitBtn);
        inputLatitude = (EditText) findViewById(R.id.editTxtLatitude);
        inputLongitude = (EditText) findViewById(R.id.editTxtLongitude) ;

        /**Ambil longitude dan lattitude dari file text di folder res*/
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        try{
            File locTxt = new File(dir, "Location.txt");
            if(!locTxt.exists()){
                fileOutputStream = new FileOutputStream(locTxt);
                fileOutputStream.write("Longitude:115.168640\nLatitude:-8.719266".getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            scan = new Scanner(new File(String.valueOf(dir) + "/Location.txt")) ;
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
        if(-90 < latitude && latitude < 90 && -180 < longitude && longitude < 180){
            GeoPoint startPoin = new GeoPoint(latitude,longitude,0); //Ambil dari storage utk dijadikan starting point
            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoin); //set marker to be in posisi lat long start poin
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); //set anchornya

            /**Tampilkan alamatnya menggantikan Start pointo*/
            Geocoder placeNow = new Geocoder(getApplicationContext(), Locale.getDefault()) ;
            try {
                if(!placeNow.getFromLocation(latitude, longitude, 1).isEmpty()){
                    startMarker.setTitle(placeNow.getFromLocation(latitude, longitude, 1).get(0).getAddressLine(0));
                    mapView.getController().setCenter(startPoin); //Set center dari map sebagai posisi start point yang ada markernya sekarang
                    mapView.getController().setZoom(19);
                    mapView.getOverlays().add(startMarker); //Tampilkan markernya sebagai map overlay (keiket longitude latitude supaya ikut gerak juga kalau peta gerak)
                    mapView.invalidate();
                }else{
                    Toast.makeText(cont, "Location doesn't exist!\nRemove Location.txt from Documents folder", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(cont, "No latitude and longitude", Toast.LENGTH_SHORT).show();
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String willBePrinted = "" ;
                String inputedLatitude = String.valueOf(inputLatitude.getText());
                String inputedLongitude = String.valueOf(inputLongitude.getText());
                if (inputedLongitude.equals("")) {
                    inputLongitude.setError("Must not be null!");
                    inputLongitude.requestFocus();
                    return;
                }
                if(inputedLatitude.equals("")){
                   inputLatitude.setError("Must not be null!");
                   inputLatitude.requestFocus();
                   return;
                }
                if(Double.parseDouble(inputedLongitude) < -180 || Double.parseDouble(inputedLongitude)  > 180)
                {
                    inputLongitude.setError("Longitude not valid!");
                    inputLongitude.requestFocus() ;
                    return;
                }
                    if(Double.parseDouble(inputedLatitude) < -90 || Double.parseDouble(inputedLatitude) > 90)
                {
                    inputLatitude.setError("Latitude is not valid!");
                    inputLatitude.requestFocus();
                    return;
                }
                latitude = Double.parseDouble(inputedLatitude);
                longitude = Double.parseDouble(inputedLongitude);
                try {
                    File file = new File(dir,"Location.txt");
                    willBePrinted = "Latitude:" + inputedLatitude + "\nLongitude:" + inputedLongitude ;
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(willBePrinted.getBytes());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    Toast.makeText(getApplicationContext(), "Saved to" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }) ;
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