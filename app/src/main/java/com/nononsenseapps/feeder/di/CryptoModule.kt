package com.nononsenseapps.feeder.di

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.nononsenseapps.feeder.crypto.Alan
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val cryptoModule = DI.Module(name = "crypto") {
    bind<LazySodiumAndroid>() with singleton { LazySodiumAndroid(SodiumAndroid()) }
    bind<Alan>() with singleton { Alan(di) }
}
