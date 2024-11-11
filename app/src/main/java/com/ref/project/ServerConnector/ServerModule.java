package com.ref.project.ServerConnector;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ServerModule {
    @Provides
    @Singleton
    public ServerAdapter provideServerAdapter(){
        return new ServerAdapter();
    }
}
