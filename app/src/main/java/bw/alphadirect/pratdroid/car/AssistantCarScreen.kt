package bw.alphadirect.pratdroid.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import bw.alphadirect.pratdroid.net.AskRequest
import bw.alphadirect.pratdroid.net.AssistantClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Calls the assistant API off the main thread and shows the reply in a car-safe template. */
class AssistantCarScreen(carContext: CarContext) : Screen(carContext) {
    private var message = "Asking your assistant…"
    private var started = false

    override fun onGetTemplate(): Template {
        if (!started) {
            started = true
            lifecycleScope.launch {
                message = try {
                    val res = withContext(Dispatchers.IO) {
                        AssistantClient.api.ask(AskRequest("Hello from the Raptor"))
                    }
                    res.reply
                } catch (e: Exception) {
                    "Assistant unavailable. Set the API endpoint in AssistantClient."
                }
                invalidate()
            }
        }
        return MessageTemplate.Builder(message)
            .setTitle("Assistant")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
