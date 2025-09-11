package com.matejdro.micropebble.common.di

import android.content.BroadcastReceiver
import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

@MapKey
@Target(AnnotationTarget.CLASS)
annotation class ReceiverKey(val value: KClass<out BroadcastReceiver>)
