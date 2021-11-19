package com.example.farfish.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.farfish.Module.util.FileUtil;
import com.example.farfish.Module.preferences.MessagesPreference;
import com.example.farfish.Module.dataclasses.Status;
import com.example.farfish.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import id.zelory.compressor.Compressor;

public class StatusRepository implements ValueEventListener {

    private static final String TAG = StatusRepository.class.getSimpleName();
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUserReference;
    private StorageReference mRootRef;
    private Set<String> mContact;
    private Context mContext;
    private List<List<Status>> mStatusLists = new ArrayList<>();
    private StatusInterface statusInterface;

    @Inject
    public StatusRepository(@ApplicationContext Context context) {
        mContext = context;
    }

    private void init() {
        mRootRef = FirebaseStorage.getInstance().getReference("stories");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("status");
        mUserReference = mDatabaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        mContact = MessagesPreference.getUserContacts(mContext);
    }

    public void setStatusInterface(StatusInterface statusInterface) {
        this.statusInterface = statusInterface;
    }

    public void loadAllStatuses() {
        init();
        mDatabaseReference.addValueEventListener(this);
    }

    public void removeListener(){

        if (mDatabaseReference != null) {
            mDatabaseReference.removeEventListener(this);
//            cleanUp();
        }
    }

    private void cleanUp() {
        mDatabaseReference = null;
        mRootRef = null;
        mContact.clear();
        mContact = null;
        mUserReference = null;
        statusInterface = null;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "onDataChange: generally");
        Iterable<DataSnapshot> iterable = snapshot.getChildren();
        List<List<Status>> allUsersStatues = new ArrayList<>();
        for (DataSnapshot dataSnapshot : iterable) {
            String rootUserStatusPhoneNumber = "";
            Iterator<DataSnapshot> childIterator = dataSnapshot.getChildren().iterator();
            List<Status> oneUserStatuses = new ArrayList<>();
            while (childIterator.hasNext()) {
                Status status = childIterator.next().getValue(Status.class);
                assert status != null;
                String phoneNum = status.getUploaderPhoneNumber();
                if (rootUserStatusPhoneNumber.equals(""))
                    rootUserStatusPhoneNumber = phoneNum;
                oneUserStatuses.add(status);
            }
            // just display contacts status
            if (mContact != null) {
                for (String contact : mContact) {
                    if (PhoneNumberUtils.compare(contact, rootUserStatusPhoneNumber))
                        allUsersStatues.add(oneUserStatuses);
                }
            }

        }
        mStatusLists.clear();
        mStatusLists.addAll(allUsersStatues);
        statusInterface.statusesAreReady();
    }

    public List<List<Status>> getStatusLists() {
        return mStatusLists;
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    public void compressAndUploadImage(Uri uri) {
        try {
            File galleryFile = FileUtil.from(mContext, uri);
            /*compress the file using a special library*/
            File compressedImageFile = new Compressor(mContext).compressToFile(galleryFile);
            /*take the file name as a unique identifier*/
            StorageReference imageRef = mRootRef.child(compressedImageFile.getName());
            // finally uploading the file to firebase storage.
            if (mContext == null)
                return;
            UploadTask uploadTask = imageRef.putFile(Uri.fromFile(compressedImageFile));

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...

                Toast.makeText(mContext, mContext.getResources().getString(R.string.sending_img_msg), Toast.LENGTH_SHORT).show();
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String downloadUrl = downloadUri.toString();
                    long dateFromDateClass = new Date().getTime();
                     /* if the image sent successfully to the firebase storage send its metadata as a message
                     to the firebase firestore */
                    if (mContext == null) return;
                    String uploaderName = MessagesPreference.getUserName(mContext);
                    String uploaderPhoneNumber = MessagesPreference.getUsePhoneNumber(mContext);
                    Status newStatus = new Status(uploaderName, uploaderPhoneNumber, downloadUrl, "", dateFromDateClass, 0);
                    uploadNewStatus(newStatus);
                });

            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadNewStatus(Status newStatus) {
        mUserReference.push().setValue(newStatus).addOnSuccessListener(listen -> {
        });
    }

    public interface StatusInterface {
        void statusesAreReady();
    }
}
