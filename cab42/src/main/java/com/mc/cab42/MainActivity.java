package com.mc.cab42;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity  implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private TextView mStatusTextView;
    private TextView mDetailTextView;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_item_change_password);
        if (menuItem != null) {
            menu.findItem(R.id.menu_item_change_password).setVisible(containsEmailProvider());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_about:
                showAboutDialog();
                return true;
            case R.id.menu_item_change_password:
                showResetPasswordAlert();
                return true;
            case R.id.menu_item_logout:
                mAuth.signOut();
                finish();
                Intent goToA = new Intent(this, LoginActivity.class);

                startActivity(goToA);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.email_password_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
        } else {
            finish();
        }
    }


    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("CAB42 - about")
                .setMessage("Be Happy !!!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setCancelable(true).show();
    }

    private void showResetPasswordAlert() {
        if (!containsEmailProvider()) {
            invalidateOptionsMenu();
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Values");
        EditText oldPassPosible = null;

        oldPassPosible = new EditText(MainActivity.this);
        oldPassPosible.setTransformationMethod(PasswordTransformationMethod.getInstance());
        oldPassPosible.setHint("Old Password");

        final EditText newPass = new EditText(MainActivity.this);
        final EditText confirmPass = new EditText(MainActivity.this);
        newPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        confirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

        newPass.setHint("New Password");
        confirmPass.setHint("Confirm Password");
        LinearLayout ll = new LinearLayout(MainActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        if (oldPassPosible != null) {
            ll.addView(oldPassPosible);
        }

        ll.addView(newPass);
        ll.addView(confirmPass);
        alertDialog.setView(ll);
        final EditText oldPass = oldPassPosible;
        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showProgressDialog();
                        changePassword(oldPass != null ? oldPass.getText().toString() : null, newPass.getText().toString(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                hideProgressDialog();
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Password set.",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
        alertDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = alertDialog.create();
        alert11.show();
    }

    private boolean containsEmailProvider() {
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getProviders() != null) {
            for (String name : mAuth.getCurrentUser().getProviders()) {
                if (name != null && name.equalsIgnoreCase("password")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void changePassword(String currentPassword, final String newPassword, final OnCompleteListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    listener.onComplete(task);
                                }
                            });
                        } else {
                            listener.onComplete(task);
                        }
                    }
                });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

}
