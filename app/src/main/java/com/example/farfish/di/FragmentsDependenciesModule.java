package com.example.farfish.di;

import android.content.Context;

import com.example.farfish.Adapters.ContactsListAdapter;
import com.example.farfish.Adapters.MessagesListAdapter;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.android.scopes.FragmentScoped;

@Module
@InstallIn(FragmentComponent.class)
public abstract class FragmentsDependenciesModule {

    @Provides
    @FragmentScoped
    public static MessagesListAdapter providesMessagesListAdapter(
            @ApplicationContext Context context
    ) {
        return new MessagesListAdapter(context);
    }

    @Provides
    @FragmentScoped
    public static ContactsListAdapter providesContactsListAdapter() {
        return new ContactsListAdapter();
    }
}
