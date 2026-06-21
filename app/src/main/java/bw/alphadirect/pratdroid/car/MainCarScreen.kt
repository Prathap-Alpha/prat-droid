package bw.alphadirect.pratdroid.car

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class MainCarScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val list = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Quick Message")
                    .addText("I'm driving the Raptor, will call soon.")
                    .setOnClickListener {
                        CarToast.makeText(carContext, "Message Sent", CarToast.LENGTH_SHORT).show()
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Navigate: Tapologo Estates")
                    .addText("Open turn-by-turn in Maps")
                    .setOnClickListener {
                        // Sanctioned car-app navigation handoff to the head-unit nav app.
                        try {
                            carContext.startCarApp(
                                Intent(
                                    CarContext.ACTION_NAVIGATE,
                                    Uri.parse("geo:0,0?q=" + Uri.encode("Tapologo Estates"))
                                )
                            )
                        } catch (e: Exception) {
                            CarToast.makeText(carContext, "Navigation not available", CarToast.LENGTH_SHORT).show()
                        }
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Ask Assistant")
                    .addText("Get a reply from your assistant")
                    .setOnClickListener { screenManager.push(AssistantCarScreen(carContext)) }
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .setSingleList(list)
            .setTitle("Prat-Droid Controls")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
