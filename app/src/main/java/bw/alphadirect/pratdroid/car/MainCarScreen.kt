package bw.alphadirect.pratdroid.car

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
            .build()

        return ListTemplate.Builder()
            .setSingleList(list)
            .setTitle("Prat-Droid Controls")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
