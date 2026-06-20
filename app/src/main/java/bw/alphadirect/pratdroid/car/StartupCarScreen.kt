package bw.alphadirect.pratdroid.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import bw.alphadirect.pratdroid.R

class StartupCarScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val photo = CarIcon.Builder(
            IconCompat.createWithResource(carContext, R.drawable.splash_photo)
        ).build()

        return MessageTemplate.Builder("Welcome, Prat.")
            .setTitle("Prat-Droid")
            .setIcon(photo)
            .addAction(
                Action.Builder()
                    .setTitle("Enter")
                    .setOnClickListener { screenManager.push(MainCarScreen(carContext)) }
                    .build()
            )
            .build()
    }
}
