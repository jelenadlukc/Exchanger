package com.example.jelen.exchanger.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.jelen.exchanger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Registration extends AppCompatActivity {

    ImageView ImgUserPhoto;
    static int PReqCode = 1;
    Uri pickedImgUri;


    private EditText userName, userLastname, userEmail, userPassword, userNumber;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUESCODE && data != null){
            //korisnik je uspesno izabrao sliku, ali cuvam referencu uri var
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
    }

    static int REQUESCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //ini views
        userName = findViewById(R.id.regName);
        userLastname = findViewById(R.id.regLastname);
        userEmail = findViewById(R.id.regEmail);
        userPassword = findViewById(R.id.regPass);
        userNumber = findViewById(R.id.regNumber);
        loadingProgress =findViewById(R.id.regProgressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("stickerexchanger");

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.INVISIBLE);
                final String name = userName.getText().toString();
                final String lastName = userLastname.getText().toString();
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String number = userNumber.getText().toString();

                if(name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || number.isEmpty()){
                        //ostalo prazno polje
                        showMessage("Please Verify all fields!");
                        regBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);

                }else{
                    //sve je ok sad pravimo nalog
                    //ako je email validan pavimo nalog
                    CreateUserAccount(name, lastName, email, password, number);
                    regBtn.setVisibility(View.INVISIBLE);
                    loadingProgress.setVisibility(View.VISIBLE);
                    

                }

            }
        });


        ImgUserPhoto = findViewById(R.id.regUserPhoto);
        ImgUserPhoto.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){
                if(Build.VERSION.SDK_INT >= 22){
                    checkAndRequestForPermission();
                }
                else{
                    openGallery();
                }
            }
        });

    }

    private void CreateUserAccount(final String name, final String lastName, final String email, final String password, final String number) {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    showMessage("Account created");
                    //nakon kreiranja naloga moramo da posaljemo sliku i ostale pofatke
                    updateUserInfo(name, lastName, number, pickedImgUri, mAuth.getCurrentUser());
                    user information = new user(
                            name,
                            lastName,
                            email,
                            password,
                            number
                    );

                    FirebaseDatabase.getInstance().getReference("User")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });


                }else{
                    showMessage("Account creation failed" + task.getException().getMessage());
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
    }

    private void openGallery(){
        //otvori galeriju i ceka da izaberes sliku
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(Registration.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(Registration.this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(Registration.this,
                        "Please accept for required permission", Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(Registration.this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        }else{
            openGallery();
        }
    }

    private void updateUserInfo(final String name, final String lastName, final String number, Uri pickedImgUri, final FirebaseUser currentUser){
        //prvo slika
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //prosalo ucitavanje sad nam treba url
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //uri
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            showMessage("Register Complete");
                                            updateUI();
                                        }

                                    }
                                });
                    }
                });
            }
        });


    }

    private void updateUI() {
        Intent Home = new Intent(getApplicationContext(),HomeActivity.class);
        startActivity(Home);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            //vec je ulogovan prebacujemo ga na login page
            updateUI();
        }
    }
}

