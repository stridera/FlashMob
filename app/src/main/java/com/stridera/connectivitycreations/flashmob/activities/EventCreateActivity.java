package com.stridera.connectivitycreations.flashmob.activities;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.stridera.connectivitycreations.flashmob.FlashmobApplication;
import com.stridera.connectivitycreations.flashmob.R;
import com.stridera.connectivitycreations.flashmob.models.Flashmob;
import com.stridera.connectivitycreations.flashmob.utils.CameraHelper;
import com.stridera.connectivitycreations.flashmob.utils.LocationHelper;
import com.stridera.connectivitycreations.flashmob.utils.TimeHelper;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class EventCreateActivity extends AppCompatActivity {

  public static final String EVENT_ID = "event_id";
  public static final String SHOW_DETAILS_POST_SAVE = "show_details_post_save";

  private static final int PICK_PHOTO_CODE = 1;
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
  private static final String TAG = EventCreateActivity.class.getSimpleName();

  private GoogleMap googleMap;
  private EditText locationEditText;
  private TextView startTimeTextView;
  private TextView endTimeTextView;
  private EditText nameEditText;
  private EditText minAttendeesEditText;
  private EditText maxAttendeesEditText;
  private ImageView photoImageView;
  private Marker locationMarker;
  private MenuItem progressItem;
  private EventCreateData data = new EventCreateData();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.event_create_activity);

    // find all the views we use later
    locationEditText = (EditText) findViewById(R.id.locationEditText);
    startTimeTextView = (TextView) findViewById(R.id.startTimeTextView);
    endTimeTextView = (TextView) findViewById(R.id.endTimeTextView);
    nameEditText = (EditText) findViewById(R.id.nameEditText);
    minAttendeesEditText = (EditText) findViewById(R.id.minAttendeesEditText);
    maxAttendeesEditText = (EditText) findViewById(R.id.maxAttendeesEditText);
    photoImageView = (ImageView) findViewById(R.id.photoImageView);

    // init all the things
    initLocation();
    initMap();
    initLocationEditText();
    initData();
  }

  private void initData() {
    String eventId = getIntent().getStringExtra(EVENT_ID);
    if (eventId == null) {
      return;
    }

    Flashmob.getInBackground(eventId, new GetCallback<Flashmob>() {
      @Override
      public void done(Flashmob flashmob, ParseException e) {
        if (e == null) {
          setData(new EventCreateData(flashmob, data.userLocation));
          ParseFile imageFile = flashmob.getImage();
          if (imageFile != null) {
            setEventImage(imageFile.getUrl());
          }
          nameEditText.setText(flashmob.getTitle());
          setTextView(minAttendeesEditText, flashmob.getMinAttendees());
          setTextView(maxAttendeesEditText, flashmob.getMaxAttendees());
        } else {
          Log.e(TAG, "Error retrieving the event", e);
          Toast.makeText(EventCreateActivity.this, "Unable to load your event", Toast.LENGTH_LONG);
          finish();
        }
      }
    });
  }

  private void initLocationEditText() {
    locationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        onLocationEditTextChanged(locationEditText.getText().toString());
        return false;
      }
    });
  }

  private void initMap() {
    SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
    mapFragment.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(GoogleMap googleMap) {
        EventCreateActivity.this.googleMap = googleMap;
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(15);
        googleMap.animateCamera(cameraUpdate);
        updateEventLocation();
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
      }
    });
  }

  private void initLocation() {
    LocationManager locationManager = (LocationManager) getSystemService(Activity.LOCATION_SERVICE);
    locationManager.requestSingleUpdate(new Criteria(), new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        data.userLocation = latLng;
        if (data.getEventLatLng() == null) {
          setEventLocation(latLng);
        }
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
    }, null);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_event_create, menu);
    progressItem = menu.findItem(R.id.actionProgress);
    ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(progressItem);
    return true;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    this.data.saveState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    setData(new EventCreateData(savedInstanceState));
  }

  private void updateTimeTextViews() {
    setTextView(startTimeTextView, data.startTime);
    setTextView(endTimeTextView, data.endTime);
  }

  private void updateEventLocation() {
    if ((data.getEventAddress() == null) || (data.getEventLatLng() == null)) {
      return;
    }
    updateLocationEditText();
    updateGoogleMap();
  }

  private void updateGoogleMap() {
    if (googleMap == null) {
      return;
    }

    // marker
    LatLng latLng = data.getEventLatLng();
    if (this.locationMarker == null) {
      BitmapDescriptor defaultMarker =
          BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
      locationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(defaultMarker));
    } else {
      locationMarker.setPosition(latLng);
    }

    // map camera
    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
    googleMap.animateCamera(cameraUpdate);
  }

  private void updateLocationEditText() {
    locationEditText.setText(data.getEventAddress());
  }

  private void updatePhotoImageView() {
    // Load the selected image into a preview
    photoImageView.setImageBitmap(data.eventImage);
  }

  public void setData(EventCreateData data) {
    this.data = data;
    updatePhotoImageView();
    updateTimeTextViews();
    updateEventLocation();
  }

  private void setEventLocation(Address address, LatLng latLng) {
    if ((address == null) || (latLng == null)) {
      return;
    }
    data.setLocation(latLng, address);
    updateEventLocation();
  }

  private boolean setEventLocation(LatLng latLng) {
    if (latLng == null) {
      return false;
    }
    try {
      Geocoder geocoder = new Geocoder(this, Locale.getDefault());
      List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
      if (addresses.isEmpty()) {
        return false;
      }
      setEventLocation(addresses.get(0), latLng);
      return true;
    } catch (IOException e) {
      Log.e(TAG, "Geocoding failed", e);
    }

    return false;
  }

  private void setEventImage(String url) {
    Log.d(TAG, "Loading image from URL: " + url);
    Target target = (Target) photoImageView.getTag();
    if (target == null) {
      target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
          setEventImage(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
          Toast.makeText(EventCreateActivity.this, "Error fetching the image", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
      };
      photoImageView.setTag(target);
    }
    Picasso.with(EventCreateActivity.this).load(url).into(target);
  }

  public void setEventImage(Bitmap bitmap) {
    Log.d(TAG, "Setting event image to: " + bitmap);
    data.eventImage = bitmap;
    updatePhotoImageView();
  }

  private void setStartTime(Calendar time) {
    data.startTime = time;
    setTextView(startTimeTextView, time);
  }

  private void onLocationEditTextChanged(final String locationName) {
    new AsyncTask<Void, Void, Address>() {
      @Override
      protected void onPreExecute() {
        progressItem.setVisible(true);
      }

      @Override
      protected Address doInBackground(Void... params) {
        final double SEARCH_BOUNDS_PRECISION = .2;
        final int MAX_RESULTS = 5;
        Geocoder geocoder = new Geocoder(EventCreateActivity.this, Locale.getDefault());
        LatLng target = (data.userLocation == null) ? data.getEventLatLng() : data.userLocation;
        return LocationHelper.findAddress(geocoder, locationName, target, SEARCH_BOUNDS_PRECISION, MAX_RESULTS);
      }

      @Override
      protected void onPostExecute(Address address) {
        if (address == null) {
          Toast.makeText(EventCreateActivity.this, "Location could not be found", Toast.LENGTH_LONG).show();
          data.setLocation(null, null);
          if (locationMarker != null) {
            locationMarker.remove();
          }
          locationMarker = null;
        } else {
          LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
          setEventLocation(address, latLng);
        }
        progressItem.setVisible(false);
      }
    }.execute();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_save) {
      onSave();
    }

    return super.onOptionsItemSelected(item);
  }

  private void onSave() {
    // required
    String title = nameEditText.getText().toString();
    String minStr = minAttendeesEditText.getText().toString();
    Integer minAttendees = minStr.isEmpty() ? null : Integer.valueOf(minStr);
    String maxStr = maxAttendeesEditText.getText().toString();
    Integer maxAttendees = maxStr.isEmpty() ? null : Integer.valueOf(maxStr);
    progressItem.setVisible(true);
    data.saveFlashmob(title, minAttendees, maxAttendees, new EventCreateData.SaveCallback() {
      @Override
      public void onSuccess(Flashmob event) {
        progressItem.setVisible(false);
        finish();
        if (getIntent().getBooleanExtra(SHOW_DETAILS_POST_SAVE, true)) {
          Intent i = new Intent(EventCreateActivity.this, EventDetailsActivity.class);
          i.putExtra(EventDetailsActivity.EVENT_ID, event.getObjectId());
          startActivity(i);
        }
      }

      @Override
      public void onFailure(ParseException ex, String userError) {
        progressItem.setVisible(false);
        if (ex != null) {
          Log.e(TAG, "Error saving model", ex);
        }
        Toast.makeText(EventCreateActivity.this, userError, Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent activityData) {
    if (resultCode != RESULT_OK) {
      Log.d(TAG, "Ignoring activity result with code: " + resultCode);
      return;
    }

    if (requestCode == PICK_PHOTO_CODE) {
      onPhotoPicked(activityData);
    } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
      onPhotoTaken();
    } else {
      Log.w(TAG, "Unhandled result code: " + resultCode);
    }
  }

  public void onAttachPhoto(View btn) {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, PICK_PHOTO_CODE);
  }

  public void onTakePhoto(View view) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, CameraHelper.getPhotoFileUri(FlashmobApplication.APP_TAG));
    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
  }

  private void onPhotoPicked(Intent activityData) {
    if (activityData != null) {
      Uri photoUri = activityData.getData();
      try {
        setEventImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri));
      } catch (IOException ex) {
        Log.e(TAG, "Error loading image", ex);
      }
    }
  }

  private void onPhotoTaken() {
    new AsyncTask<Void,Void,String>() {
      @Override
      protected String doInBackground(Void... params) {
        final String imagePath = CameraHelper.getPhotoFileUri(FlashmobApplication.APP_TAG).getPath();
        try {
          return CameraHelper.getRightAngleImage(imagePath);
        } catch (Throwable e){
          Log.e(TAG, e.getMessage(), e);
        }
        return imagePath;
      }

      @Override
      protected void onPostExecute(String imagePath) {
        super.onPostExecute(imagePath);
        setEventImage(CameraHelper.decodeFile(imagePath));
      }
    }.execute();
  }

  public void onClickStartTimeTextView(View view) {
    final Calendar now = Calendar.getInstance();
    Calendar initialDisplayTime = data.startTime == null ? now : data.startTime;
    int hour = initialDisplayTime.get(Calendar.HOUR_OF_DAY);
    int minute = initialDisplayTime.get(Calendar.MINUTE);

    showTimeDialog(hour, minute, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar time = TimeHelper.createAfter(now, hourOfDay, minute);
        setStartTime(time);
      }
    });
  }

  public void onClickEndTimeTextView(View view) {
    final TextView timeTextView = (TextView)view;
    final Calendar reference = data.startTime == null ? Calendar.getInstance() : data.startTime;
    int hour = reference.get(Calendar.HOUR_OF_DAY) + (Flashmob.DEFAULT_DURATION / 60);
    int minute = reference.get(Calendar.MINUTE) + (Flashmob.DEFAULT_DURATION % 60);

    showTimeDialog(hour, minute, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar time = TimeHelper.createAfter(reference, hourOfDay, minute);
        if (data.startTime == null) {
          setStartTime(reference);
        }
        setTextView(timeTextView, time);
        data.endTime = time;
      }
    });
  }

  private void showTimeDialog(int hour, int minute, TimePickerDialog.OnTimeSetListener listener) {
    new TimePickerDialog(this, listener, hour, minute, DateFormat.is24HourFormat(this)).show();
  }

  private void setTextView(TextView timeTextView, Calendar time) {
    if (time == null) {
      timeTextView.setText("");
    }
    else {
      String timeStr = DateFormat.getTimeFormat(this).format(time.getTime());
      timeTextView.setText(timeStr);
    }
  }

  private void setTextView(TextView numberTextView, Integer number) {
    numberTextView.setText(number == null ? "" : number + "");
  }
}
