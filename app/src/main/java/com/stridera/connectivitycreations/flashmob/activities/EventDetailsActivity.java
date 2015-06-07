package com.stridera.connectivitycreations.flashmob.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.stridera.connectivitycreations.flashmob.Models.Flashmob;
import com.stridera.connectivitycreations.flashmob.R;

public class EventDetailsActivity extends ActionBarActivity {

    protected ImageView ivEventDetailsImage;
    protected TextView tvEventName;

    protected RelativeLayout rlJoinViews;

    protected RelativeLayout rlAttendingViews;
    protected TextView tvAttendingCount;

    protected RelativeLayout rlCommentsViews;
    protected TextView tvCommentsCount;

    protected TextView tvEventTime;
    protected TextView tvEventLocation;

    protected Flashmob event;
    protected String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        getAllViews();

        eventId = getIntent().getStringExtra("event_id");
        Flashmob.getInBackground(eventId, new GetCallback<Flashmob>() {
            @Override
            public void done(Flashmob flashmob, ParseException e) {
                event = flashmob;
                setAllViews();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getAllViews()
    {
        ivEventDetailsImage = (ImageView) findViewById(R.id.ivEventDetailsImage);
        tvEventName = (TextView) findViewById(R.id.tvEventName);

        rlJoinViews = (RelativeLayout) findViewById(R.id.rlJoinViews);

        rlAttendingViews = (RelativeLayout) findViewById(R.id.rlAttendingViews);
        tvAttendingCount = (TextView) findViewById(R.id.tvAttendingCount);

        rlCommentsViews = (RelativeLayout) findViewById(R.id.rlCommentsViews);
        tvCommentsCount = (TextView) findViewById(R.id.tvCommentsCount);

        tvEventTime = (TextView) findViewById(R.id.tvEventTime);
        tvEventLocation = (TextView) findViewById(R.id.tvEventLocation);

        // TODO: Display the min and max number of attendees somewhere
    }

    private void setAllViews() {
        // TODO: Set the image for the event
        // ivEventDetailsImage = ...

        // TODO: Set the number of current attendees
        // tvAttendingCount.setText(flashmob.something());

        // TODO: Set the number of comments
        // tvCommentsCount.setText(flashmob.getTitle());

        tvEventName.setText(event.getTitle());
        tvEventTime.setText(event.getEventDate().toString());
        tvEventLocation.setText(event.getAddress());

        rlJoinViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Join the event
            }
        });

        rlAttendingViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EventDetailsActivity.this, AttendeesActivity.class);
                i.putExtra("event_id", event.getObjectId());
                startActivity(i);
            }
        });

        rlCommentsViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EventDetailsActivity.this, CommentsActivity.class);
                i.putExtra("event_id", event.getObjectId());
                startActivity(i);
            }
        });
    }



}