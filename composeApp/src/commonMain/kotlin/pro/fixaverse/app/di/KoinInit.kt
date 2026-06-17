package pro.fixaverse.app.di

import com.arkivanov.decompose.ComponentContext
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform.getKoin
import pro.fixaverse.di.sharedModule
import pro.fixaverse.presentation.root.RootComponent

fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}

fun createRootComponent(componentContext: ComponentContext): RootComponent =
    getKoin().get { parametersOf(componentContext) }
