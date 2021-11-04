package com.example.farfish.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.scopes.ViewModelScoped;

@Module
@InstallIn(ViewModelComponent.class)
public abstract class AppModule {

    @Provides
    @ViewModelScoped
    public static FirebaseFirestore providesFirestoreInstance() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @ViewModelScoped
    public static FirebaseAuth providesFirebaseAuthInstance() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @ViewModelScoped
    public static FirebaseStorage providesFirebaseStorageInstance() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    @ViewModelScoped
    public static FirebaseDatabase providesFirebaseDatabaseInstance() {
        return FirebaseDatabase.getInstance();
    }
}

