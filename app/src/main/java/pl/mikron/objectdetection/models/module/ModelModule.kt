package pl.mikron.objectdetection.models.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pl.mikron.objectdetection.models.MobilenetV1
import pl.mikron.objectdetection.models.MobilenetV2
import pl.mikron.objectdetection.models.ModelLifecycle
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ModelModule {

    @[Binds IntoSet Singleton]
    abstract fun mobilenetV1(model: MobilenetV1): ModelLifecycle

    @[Binds IntoSet Singleton]
    abstract fun mobilenetV2(model: MobilenetV2): ModelLifecycle
}
