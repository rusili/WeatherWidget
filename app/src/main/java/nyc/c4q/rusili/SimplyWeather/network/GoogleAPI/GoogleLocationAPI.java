package nyc.c4q.rusili.SimplyWeather.network.GoogleAPI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import nyc.c4q.rusili.SimplyWeather.utilities.Constants;

public class GoogleLocationAPI extends GoogleLocationAPIInterface{
	private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 6;

	private static GoogleLocationAPI googleLocationAPI;
	private static GoogleApiClient googleAPIClient;

	private GoogleLocationAPILIstener lIstener;
	private boolean locationPermissionGranted = false;
	private Context context;

	private GoogleLocationAPI(){}

	public static GoogleLocationAPI getInstance(){
		if (googleLocationAPI == null){
			googleLocationAPI = new GoogleLocationAPI();
		}
		return googleLocationAPI;
	}

	public void setRetrofitListener (GoogleLocationAPILIstener googleLocationAPILIstener) {
		this.lIstener = googleLocationAPILIstener;
	}

	private void startGoogleAPIClient (Context context) {
		if (googleAPIClient == null) {
			googleAPIClient = new GoogleApiClient.Builder(context)
				  .addConnectionCallbacks(this)
				  .addOnConnectionFailedListener(this)
				  .addApi(LocationServices.API)
				  .build();
		}

		if (isNetworkConnected(context)) {
			googleAPIClient.connect();
		} else {
			Toast.makeText(context, "No network detected", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isNetworkConnected (Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
			  activeNetwork.isConnectedOrConnecting();
		this.context = context;

		return isConnected;
	}

	@Override
	public void getZipCode (Context context) {
		startGoogleAPIClient(context);
	}

	private int getLocation(Location location){
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			return Integer.parseInt(addresses.get(0).getPostalCode());
		} catch (IOException e) {
			e.printStackTrace();
		}
		context = null;
		return Constants.DEFAULT.INT;
	}

	@Override
	public void onConnected (@Nullable Bundle bundle) {
		if (lIstener != null) {
			lIstener.onConnection(getLocation(checkPermissions()));
		}
	}

	private Location checkPermissions(){
		if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
			  android.Manifest.permission.ACCESS_FINE_LOCATION)
			  == PackageManager.PERMISSION_GRANTED) {
			locationPermissionGranted = true;
		} else {
			ActivityCompat.requestPermissions((Activity) context.getApplicationContext(),
				  new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
				  PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
		}
		return LocationServices.FusedLocationApi.getLastLocation(googleAPIClient);
	}

	public interface GoogleLocationAPILIstener{
		void onConnection(int zipCode);
	}
}