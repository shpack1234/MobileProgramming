/*
    ServiceModule - MPTeamProject
    Copyright (C) 2024-2025 Coppermine-SP - <https://github.com/Coppermine-SP>.
 */
package corp.cloudint.fridgeplus.Services;

import android.content.Context;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ServiceModule {

    @Provides
    @Singleton
    public ServerContext provideServerContext(@ApplicationContext Context context){
        return new ServerContext(context);
    }

    @Provides
    @Singleton
    public GoogleSignInManager provideGoogleSignInManager(@ApplicationContext Context context){
        return new GoogleSignInManager(context);
    }
}
