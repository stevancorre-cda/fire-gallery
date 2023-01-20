package com.damienetstevan.firegallery;

import static com.damienetstevan.firegallery.Utils.makeLongErrorToast;
import static com.damienetstevan.firegallery.Utils.makeLongToast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.damienetstevan.firegallery.models.ImageModel;
import com.facebook.login.LoginManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienetstevan.firegallery.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private StorageReference storage;
    private DatabaseReference database;
    private RecyclerView recyclerView;
    private RecyclerImageAdapter recyclerImageAdapter;
    private ArrayList<ImageModel> images;

    // activity result launcher for the image selection (used for the image upload)
    private final ActivityResultLauncher<Intent> imageSelectionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    final Intent data = result.getData();
                    assert data != null;
                    handleImageSelected(data.getData());
                }
            });

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if user is authenticatede
        final FirebaseUser user = FirebaseAuth
                .getInstance()
                .getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, FacebookLoginActivity.class));
            return;
        }

        // initialize storage & database
        storage = FirebaseStorage
                .getInstance()
                .getReference();
        database = FirebaseDatabase
                .getInstance("https://firegallery-4e769-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child("users")
                .child(user.getUid());

        // bind actions (like image upload when the user clicks the + button)
        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(view -> handleSelectImage());

        // initialize recycler view (images container)
        recyclerView = findViewById(R.id.imagesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
        recyclerView.setHasFixedSize(true);
        images = new ArrayList<>();

        // on data source update
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                // load all available image urls for the authenticated user
                images.clear();
                for (final DataSnapshot child : snapshot.getChildren()) {
                    final String imageUrl = child.getValue(String.class);
                    final ImageModel image = new ImageModel(imageUrl);

                    images.add(image);
                }

                // update the recycler view with the new data
                recyclerImageAdapter = new RecyclerImageAdapter(MainActivity.this, images);
                recyclerView.setAdapter(recyclerImageAdapter);
                recyclerImageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleError(error.toException());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // create three dots menu in the top right corner
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // handle sign out
        if (item.getItemId() == R.id.menu_sign_out) {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleSelectImage() {
        // open image selection intent
        final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imageSelectionLauncher.launch(intent);
    }

    private void handleImageSelected(final Uri uri) {
        startLoading();

        // upload the file, then retrieve its download url in order to store it in the realtime database
        final StorageReference upload = storage.child("images/" + uri.getLastPathSegment());
        upload.putFile(uri, new StorageMetadata())
                .addOnFailureListener(this::handleError)
                .addOnCompleteListener(e -> {
                    upload.getDownloadUrl()
                            .addOnFailureListener(this::handleError)
                            .addOnSuccessListener(this::handleImageUploaded);
                });
    }

    private void handleImageUploaded(final Uri uri) {
        // store newly uploaded image url to the authenticated user images
        final String hash = String.valueOf(uri.hashCode());
        database
                .child(hash)
                .setValue(uri.toString())
                .addOnFailureListener(this::handleError)
                .addOnSuccessListener(e -> {
                    makeLongToast(this, "Image uploadée!");
                    endLoading();
                });
    }

    private void handleError(final Exception e) {
        makeLongErrorToast(this, e.getMessage());
        endLoading();
    }

    private void startLoading() {
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
    }

    private void endLoading() {
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
    }
}