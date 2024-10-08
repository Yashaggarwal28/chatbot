// ChatBotApiService.kt
package com.example.chatbotfssai

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Data class for the request payload
data class ChatRequest(val query: String)

// Data class for the response payload
data class ChatResponse(val response: String)

// Retrofit API interface
interface ChatBotApiService {
    @POST("/chatbot") // Adjust the endpoint according to your API
    fun getChatbotResponse(@Body request: ChatRequest): Call<ChatResponse>
}
