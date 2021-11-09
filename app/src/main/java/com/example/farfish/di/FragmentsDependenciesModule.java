package com.example.farfish.di;

import android.content.Context;

import com.example.farfish.Adapters.MessagesListAdapter;
import com.example.farfish.Module.dataclasses.Message;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.android.scopes.ViewModelScoped;

@Module
@InstallIn(ViewModelComponent.class)
public abstract class FragmentsDependenciesModule {

    @Provides
    @ViewModelScoped
    public static MessagesListAdapter providesMessagesListAdapter(
            @ApplicationContext Context context
    ) {
        return new MessagesListAdapter(context);
    }
}
