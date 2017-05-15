package com.shawnaten.simpleweather.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shawnaten.simpleweather.R;
import com.shawnaten.simpleweather.lib.model.APIKeys;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.filepicker.Filepicker;
import io.filepicker.models.FPFile;

public class UploadPhotoActivity extends BaseActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;

    private static String[] services = {
            "GALLERY",
            "CAMERA",
            "DROPBOX",
            "INSTAGRAM",
            "GOOGLE_DRIVE",
    };

    private static String[] mimetypes = {"image/*"};

    private String imageKey = null;
    private String imageURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_upload);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Fragment fragment = UploadInitialFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, fragment)
                .commit();

        Filepicker.setKey(APIKeys.FILESTACK);
        Filepicker.setAppName(getString(R.string.app_name));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (imageURL != null) {
            Fragment fragment = UploadConfirmFragment.newInstance(imageKey, imageURL);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
        }
    }

    public void onSelectPhotoClick(View button) {
//      Toast.makeText(this, "Select photo clicked!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, Filepicker.class);
        intent.putExtra("services", services);
        intent.putExtra("mimetype", mimetypes);
        startActivityForResult(intent, Filepicker.REQUEST_CODE_GETFILE);
    }

    public void onSubmitClick(View button) {
        Toast.makeText(this, "Submit clicked!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Filepicker.REQUEST_CODE_GETFILE) {
            if(resultCode == RESULT_OK) {

                // Filepicker always returns array of FPFile objects
                ArrayList<FPFile> fpFiles = data.getParcelableArrayListExtra(Filepicker.FPFILES_EXTRA);

                // Option multiple was not set so only 1 object is expected
                FPFile file = fpFiles.get(0);

                Pattern pattern = Pattern.compile("\\Qhttps://cdn.filepicker.io/api/file/\\E(.+)");
                Matcher matcher = pattern.matcher(file.getUrl());
                matcher.find();

                imageKey = matcher.group(1);
                Log.d("key", imageKey);
                imageURL = file.getUrl();

                // Do something cool with the result
            } else {
                // Handle errors here
            }

        }
    }



}

