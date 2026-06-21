package bw.alphadirect.pratdroid.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class AskRequest(val prompt: String)
data class AskResponse(val reply: String)

interface AssistantApi {
    @POST("ask")
    suspend fun ask(@Body request: AskRequest): AskResponse
}

/** Network layer for the personal assistant API. Set BASE_URL (and auth) to a real endpoint. */
object AssistantClient {
    // TODO: point this at your personal assistant endpoint.
    private const val BASE_URL = "https://assistant.example.com/"

    val api: AssistantApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AssistantApi::class.java)
    }
}
