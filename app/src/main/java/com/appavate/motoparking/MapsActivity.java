package com.appavate.motoparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

public class MapsActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        ManageAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // Leave this alone - works
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            /*case R.id.action_add:
                Intent aIntent = new Intent(this, Add.class);
                startActivity(aIntent);
                break;*/


            case R.id.action_about:
                Intent bIntent = new Intent(this, About.class);
                startActivity(bIntent);
                break;


        }
        return true;

    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();


            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }
                @Override
                public View getInfoContents(Marker marker) {
                    View myContentView = getLayoutInflater().inflate(
                            R.layout.custommarker, null);
                    TextView tvTitle = ((TextView) myContentView
                            .findViewById(R.id.title));
                    tvTitle.setText(marker.getTitle());
                    TextView tvSnippet = ((TextView) myContentView
                            .findViewById(R.id.snippet));
                    tvSnippet.setText(marker.getSnippet());
                    return myContentView;
                }
            });


            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        SetLocation();
        try
        {
            InputStream is = getAssets().open("cbd.xml");
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(is, null);
            int eventType = xpp.getEventType();
            String name = "";
            String description = "";
           // String coordinates = "";
            double lat = 0;
            double lon = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if(eventType == XmlPullParser.START_TAG) {
                    //We look for "title" tag in XML response
                    if (xpp.getName().equalsIgnoreCase("name")) {
                        name = xpp.nextText();
                    }
                    if (xpp.getName().equalsIgnoreCase("description")) {
                        description = xpp.nextText();
                    }
                    if (xpp.getName().equalsIgnoreCase("coordinates")) {
                        String coordinates = xpp.nextText();
                        String[] separated = coordinates.split(",");
                        lon = Double.parseDouble(separated[0]);
                        lat = Double.parseDouble(separated[1]);
                    }
                }

                if(!name.equals("") && !description.equals("") && lat != 0 && lon != 0)
                {
                    mMap.addMarker(CreateMarker(lat, lon, name, description));
                }
                eventType = xpp.next();
            }
            // indicate app done reading the resource.
          //  xpp.close();
        }
        catch (Exception e)
        {
            Log.d("EXCEPT", "Exception in parsing " + e);
        }

        // This is where things are added
      //  mMap.addMarker(CreateMarker(-36.850868, 174.764414, "TEST", "TEST DESCRIPTION"));
    }

    // WORKS
    private MarkerOptions CreateMarker(double lat, double lon, String title, String description)
    {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lon));
        markerOptions.title(title);
        markerOptions.snippet(description);
        return markerOptions;

    }


    // WORKS
    private void SetLocation()
    {
        CameraUpdate point = CameraUpdateFactory.newLatLngZoom(new LatLng(-36.8508, 174.76), 15);
    // moves camera to coordinates
        mMap.moveCamera(point);
    // animates camera to coordinates
        mMap.animateCamera(point);
    }

    // JAY - Leave this alone - works
    private void ManageAd()
    {
        // Ads and Payment verification area
        AdView adView = (AdView) this.findViewById(R.id.adView);
       // ScrollView _scrollView = (ScrollView) this.findViewById(R.id._scrollview);
        SharedPreferences myPrefs = getSharedPreferences("Pref", MODE_PRIVATE);
        Boolean paid = myPrefs.getBoolean("paid", false);
       // Boolean paid = false;
        if(paid == false)
        {
            // Look up the AdView as a resource and load a request.
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setVisibility(AdView.VISIBLE);
            adView.loadAd(adRequest);
        }
        else if (paid == true)
        {
            adView.destroy();
            if (adView.getVisibility() == AdView.VISIBLE)
            {

                adView.setVisibility(AdView.GONE);
                //  _scrollView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
            }
        }
    }





}
