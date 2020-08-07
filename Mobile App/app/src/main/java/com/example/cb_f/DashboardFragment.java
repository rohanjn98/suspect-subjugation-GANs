package com.example.cb_f;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;


public class DashboardFragment extends Fragment {

    private FirebaseAuth mAuth;
    private ImageView profileImage,profileEdit;
    private TextView profileName,resetPassword;
    private String userId,profileImageUrl;
    private Uri uriProfileImage;
    private Bitmap bitmap;
    private ProgressBar progressBar;
    private Button verifyButton ,removeAccount;
    private EditText emailEdit,phoneEdit,aadharEdit;
    private static final int CHOOSE_IMAGE = 101;
    private ContextWrapper cw;
    private File path, directory;
    private FirebaseUser user;
    DatabaseReference userRef, mRef;


    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (Throwable t){}
        super.onCreate(savedInstanceState);
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();;
        userRef = mFirebaseDatabase.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user==null){
            Intent I = new Intent(getActivity(), LoginActivity.class);
            startActivity(I);
            //Todo Toast
        }
        assert user != null;
        userId = user.getUid();

        //Path to profile picture
        cw = new ContextWrapper(getContext());
        directory = cw.getDir("profile", Context.MODE_PRIVATE);
        path = new File(directory, "profile.png");

        if(user!=null){
            new Thread(new Runnable() {
                @Override
                public void run() {

                    if(user.getPhotoUrl()!=null) {
                        //Log.d("ProfilePic", "onCreate: ProfileActivity "+ user.getPhotoUrl().toString());
                        mHandler.sendEmptyMessage(1);
                    }
                    mRef = userRef.child(user.getUid());
                    mRef.keepSynced(true);
                    mRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Log.d("Mref", "onDataChange: "+mRef.toString());
                                Log.d("Mref","onDataChange: "+dataSnapshot.toString());
                                showData(dataSnapshot);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }).start();
        }
        else {
            //TODO
            //User not signed in
        }

    }

  /*  private void loadImage() {
        try{
            bitmap = BitmapFactory.decodeStream(new FileInputStream(path));
            mHandler.sendEmptyMessage(0);

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
    }*/

    private void showData(DataSnapshot dataSnapshot) {
        Log.d("data", "showData: "+dataSnapshot.toString());
        user userOne = dataSnapshot.getValue(user.class);
        if(userOne!=null && userOne.getFirstName()!=null && userOne.getEmailId()!=null)
        {
            Log.d("data", "showData: "+userOne.getFirstName());
            profileName.setText(userOne.getFirstName().trim());
            phoneEdit.setText(userOne.getPhoneNo().trim());
            emailEdit.setText(userOne.getEmailId().trim());
            aadharEdit.setText(userOne.getAadhar());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        profileName =view.findViewById(R.id.display_name);
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.display_name);
        progressBar = view.findViewById(R.id.progress_bar_circular);
        profileEdit = view.findViewById(R.id.profileEdit);
        emailEdit =view. findViewById(R.id.email);
        phoneEdit = view.findViewById(R.id.phone_no);
        aadharEdit = view.findViewById(R.id.aadhar_edit);
        verifyButton = view.findViewById(R.id.verifyButton);
        resetPassword = view.findViewById(R.id.resetPassword);
        removeAccount = view.findViewById(R.id.RemoveAccount);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogResetPassword(getActivity(),
                        "Reset Password",
                        "A password reset link will be sent to your registered email "+ user.getEmail());
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) profileImage.getDrawable();
                    bitmap = bitmapDrawable.getBitmap();
                }catch (Exception e){
                    Log.d(TAG, "onClick: "+ e.toString());
                    Toast.makeText(getContext(), "Upload a Profile Pic", Toast.LENGTH_SHORT).show();
                }finally {
                    if(bitmap!=null) {
                        Intent intent = new Intent(getActivity(), ProfilePhotoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), Pair.create((View) profileImage, "photoTransition"));//, Pair.create((View)logo,"logoTransition"));
                        intent.putExtra("photo",bitmap);
                        startActivity(intent, options.toBundle());
                    }
                }
            }
        });

        removeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveAccountMethode(v);
            }
        });

        profileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showImageChooser();
                    }
                }).start();
            }
        });

        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Confirm Logout");
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseAuth.getInstance().signOut();
                        getActivity().finish();
                        Intent I = new Intent(getActivity(),LoginActivity.class);
                        I.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(I);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });

        return view;
    }


    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            /*if(msg.what==0){
                Glide.with(getActivity())
                        .load(bitmap)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            }*/
            if(msg.what == 1){

                String s = mAuth.getCurrentUser().getPhotoUrl().toString();
                Glide.with(getActivity())
                            .load(s)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(profileImage);


            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK  && data!=null && data.getData()!=null){

            uriProfileImage = data.getData();

            try {
                Log.d("Bitmap", "onActivityResult: BitmapGenerated");
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uriProfileImage);
                //saveToInternalStorage(bitmap);
                /*mHandler.sendEmptyMessage(0);*/
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Bitmap", "onActivityResult: Bitmap not generated");
            }
        }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    /*private String saveToInternalStorage(Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }*/

    private void saveInfoToFirbase(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null && profileImageUrl!=null){
            UserProfileChangeRequest profile =  new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile Pic Uploaded", Toast.LENGTH_SHORT).show();
                        mHandler.sendEmptyMessage(1);
                    }
                    else{
                        Log.d("ProfileUpdated", "onComplete: Error Occured while uploading image");
                    }
                }
            });

        }
    }

    private void uploadImageToFirebaseStorage() {
        final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/"+System.currentTimeMillis()+".jpg");
        if(uriProfileImage != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25,baos);
            byte[] data_1 = baos.toByteArray();
            UploadTask uploadTask = profileImageRef.putBytes(data_1);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return profileImageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        profileImageUrl = downloadUri.toString();
                        saveInfoToFirbase();
                        Log.d("ProfileUploaded", "onSuccess: Profile picture uploaded "+ profileImageUrl);
                    } else {
                        Toast.makeText(getContext(),"Error Occured",Toast.LENGTH_SHORT).show();
                        Log.d("ProfileUploaded", "onFailure: Profile not uploaded");
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Image"),CHOOSE_IMAGE);
    }


    public void showDialogResetPassword(Activity activity, String title, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (title != null) builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {    //Todo Why User.delete?
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getContext(),"Password reset link sent",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    public void RemoveAccountMethode(View view){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        LayoutInflater factory = LayoutInflater.from(getContext());

//text_entry is an Layout XML file containing two text field to display in alert dialog
        final View textEntryView = factory.inflate(R.layout.reauthenticate, null);

        final EditText email = (EditText) textEntryView.findViewById(R.id.email);
        final EditText passsword = (EditText) textEntryView.findViewById(R.id.password);


        email.setHint("Enter Email");
        passsword.setHint("Enter Password");
        passsword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Re-Authenticate:").setView(textEntryView).setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        progressBar.setVisibility(View.VISIBLE);
                        Log.d("password", "onClick: " + passsword.getText().toString());
                        AuthCredential credential = EmailAuthProvider.getCredential(email.getText().toString(), passsword.getText().toString());
                        // Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(getContext(),"Account Deleted Successfully",Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(getActivity(),LoginActivity.class);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            i.putExtra("UID",userId);
                                            startActivity(i);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        dialog.cancel();
                        /*
                         * User clicked cancel so do some stuff
                         */
                    }
                });
        alert.show();

        //showDialogRemove(ProfileActivity.this,"Remove Account","Its hard to see you go!");
    }

    @Override
    public void onResume() {
        super.onResume();
        user.reload();
        boolean b = user.isEmailVerified();
        if(b){
            verifyButton.setText("Verified");
            verifyButton.setTextColor(Color.parseColor("#1B5E20"));
            verifyButton.setOnClickListener(null);
        }
        else{
            verifyButton.setText("Verify!");
            verifyButton.setTextColor(Color.rgb(255,0,0));
            verifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(getActivity(),"Email Verification","Resend verification email to "+user.getEmail()+" ");
                }
            });
        }
    }


    public void showDialog(Activity activity, String title, CharSequence message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (title != null) builder.setTitle(title);

        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendVerificationEmail();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void sendVerificationEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Verification email sent to " + user.getEmail(),Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.e("Verification", "sendEmailVerification", task.getException());
                    Toast.makeText(getContext(),
                            "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
