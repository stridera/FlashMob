package com.stridera.connectivitycreations.flashmob.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.stridera.connectivitycreations.flashmob.models.Flashmob;
import com.stridera.connectivitycreations.flashmob.R;
import com.stridera.connectivitycreations.flashmob.adapters.CommentsAdapter;

import org.w3c.dom.Comment;

import java.util.ArrayList;

public class CommentsActivity extends ActionBarActivity {

    private Flashmob flashmob;
    // TODO: Make sure to use our own custom model for Comment
    private ArrayList<Comment> comments;
    private CommentsAdapter aComments;

    private Button btnCommentSubmit;
    private EditText etComment;
    private ListView lvComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_comments);

        comments = new ArrayList<>();
        aComments = new CommentsAdapter(this, comments);
        lvComments = (ListView) findViewById(R.id.lvComments);
        lvComments.setAdapter(aComments);

        setupViews();
        // TODO: Fetch the data to populate the ListView
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_comments, menu);
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

    public void setupViews() {
        btnCommentSubmit = (Button) findViewById(R.id.btnSubmitComment);
        btnCommentSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Submit the comment
                etComment.setText("");
                aComments.notifyDataSetChanged();
            }
        });

        etComment = (EditText) findViewById(R.id.etComment);
    }
}
