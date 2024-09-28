package pl.mikron.objectdetection.models.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pl.mikron.objectdetection.models.*
import pl.mikron.objectdetection.models.used.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ModelModule {

    @[Binds IntoSet Singleton]
    abstract fun mobilenetV1(model: MobilenetV1): ModelLifecycle

    @[Binds IntoSet Singleton]
    abstract fun mobilenetV2(model: MobilenetV2): ModelLifecycle

    @[Binds IntoSet Singleton]
    abstract fun mobilenetV3Large(model: MobilenetV3Large): ModelLifecycle

    @[Binds IntoSet Singleton]
    abstract fun mobilenetV3Small(model: MobilenetV3Small): ModelLifecycle

    @[Binds IntoSet Singleton]
    abstract fun yoloV5s(model: YoloV5s): ModelLifecycle
}
