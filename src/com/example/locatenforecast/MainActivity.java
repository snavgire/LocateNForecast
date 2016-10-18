package com.example.locatenforecast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener
{

	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	private Location mCurrentLocation;
	private String mLastUpdateTime;
	private TextView mWeather2TextView;
	private TextView mTemperatureTextView;
	private TextView mLastUpdateTimeTextView;
	private Button mButtonExit;

	//OnStart Function
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		mGoogleApiClient.connect();    
	}
	
	//OnCreate Function
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (mGoogleApiClient == null) {
		    mGoogleApiClient = new GoogleApiClient.Builder(this)
		        .addConnectionCallbacks(this)
		        .addOnConnectionFailedListener(this)
		        .addApi(LocationServices.API)
		        .build();
		}
		mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mWeather2TextView=(TextView) findViewById(R.id.textView_weather2);
		mTemperatureTextView=(TextView) findViewById(R.id.textView_temperature);
		mLastUpdateTimeTextView=(TextView) findViewById(R.id.textView_datetime);	
		mButtonExit = (Button) findViewById(R.id.button_exit);
	    
		mButtonExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
	            System.exit(0);
			}
	    });	
	}
	
	//CreateOptionsMenu Function
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	//onOptionsItemSelected Function
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	//OnLocationChanged Function
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
	}
	
	//UpdateUI Function
	private void updateUI() {
        mLastUpdateTimeTextView.setText("Last Updated: "+mLastUpdateTime);
        
        //URLStringBuilder
        String url=new String("https://api.darksky.net/forecast/3daa933804f77d06a18efca64fbfae9a/"+ mCurrentLocation.getLatitude() + "," +  mCurrentLocation.getLongitude() + "?exclude=minutely,hourly,daily,alerts,flags");
		
        //Call to LongOperation Class
		new LongOperation().execute(url);    
    }
	
	//Class LongOperation for Internet Communication
	class LongOperation extends AsyncTask<String,Void, String>
	{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			HttpClient client=new DefaultHttpClient();
			String result="";
			HttpGet get=new HttpGet(params[0]);
			HttpResponse response = null;
			try {
				response = client.execute(get);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {

				BufferedReader reader=new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuffer buffer=new StringBuffer("");
				String line="";
				while((line=reader.readLine())!=null)
					buffer.append(line);
				reader.close();
				result=buffer.toString();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		//Post the execution of LongOperation
		@Override
		protected void onPostExecute(String result) 
		{
			// TODO Auto-generated method stub
			Log.d("post","onpost" );
			Log.d("response",result);
			try{
				// Convert String to json object
				JSONObject obj=new JSONObject(result);
				
				// get LL json sub-object
				JSONObject json_LL = obj.getJSONObject("currently");
				
				// get value from currently.summary
				String wupdate=json_LL.getString("summary");
				String tupdate=json_LL.getString("temperature");
				
				Log.d("wupdate value", wupdate);
				
				//Update UI
				mWeather2TextView.setText("Weather: "+wupdate);
				mTemperatureTextView.setText("Temperature: "+tupdate);
			
			} catch (JSONException e) {
	            Log.e("JSON Parser", "Error parsing data " + e.toString());
			}
		}
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.d("onconnectd", "inside on connected");
		startLocationUpdates();
	}
	
	protected void startLocationUpdates() {
		
	    LocationServices.FusedLocationApi.requestLocationUpdates(
	            mGoogleApiClient, mLocationRequest,  new com.google.android.gms.location.LocationListener() {
					
					@Override
					public void onLocationChanged(Location arg0) {
						// TODO Auto-generated method stub
						mCurrentLocation = arg0;
				        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
				        updateUI();
					}
				});
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
