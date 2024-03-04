package com.alonso.chatgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 *  Alonso Martínez
 *  OKHttp
 *  OpenAI
 */

class MainActivity : AppCompatActivity() {
    // Cliente de OKHttp
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Edit Text de la pregunta
        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        // Botón de submit
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        // Text View de la respuesta
        val tvResponse = findViewById<TextView>(R.id.tvResponse)

        // Método al pulsar el botón de submit
        btnSubmit.setOnClickListener {
            // Pregunta
            val question = etQuestion.text.toString()

            // Metodo getResponse()
            // Recibe la pregunta y devuelve la respuesta
            getResponse(question) { response ->
                // Run on UI Thread es un hilo de Android responsable
                // de actualizar los elementos del layout de la app
                // En éste caso actualiza el Text View de la respuesta
                // Asignándole la respuesta de la API en su propiedad
                // de texto
                runOnUiThread {
                    tvResponse.text = response
                }
            }
        }
    }

    // Metodo getResponse()
    // Recibe la pregunta y devuelve la respuesta
    fun getResponse(question: String, callback: (String) -> Unit) {

        // Debes asignar como valor tu API Key de OpenAI
        val apiKey = "Tu API Key"

        // URL para hacer la petición a la API
        val url = "https://api.openai.com/v1/chat/completions"

        // El cuerpo de la petición a la API
        // Es un objeto JSON que incluye parámetros como el modelo
        // a utilizar, el máximo de tokens o el mensaje.
        //
        // El mensaje que le pasamos es el parámetro question
        // de la función
        val requestBody =
            """
            {
                "model": "gpt-3.5-turbo-0613",
                "messages": [{"role": "user", "content": "$question"}],
                "max_tokens": 500,
                "temperature": 0
            }
            """

        // Realizamos la petición con el objeto Request que nos
        // proporciona OKHttp
        val request = Request.Builder()
            // Le proporcionamos la url
            .url(url)
            // Cabecera del tipo de contenido
            .addHeader("Content-Type", "application/json")
            // Cabecera de autorización
            // Le proporcionamos la API key
            .addHeader("Authorization", "Bearer $apiKey")
            // Método post al que le pasamos el objeto JSON
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        // Nuestro cliente de OKHttp realiza una llamada a la API
        // Le pasamos la petición (request) con todos los datos
        // Nos devolverá un objeto con la respuesta
        client.newCall(request).enqueue(object : Callback {

            // En caso de fallo
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed", e)
            }

            // En caso de respuesta
            override fun onResponse(call: Call, response: Response) {
                // Cuerpo de la respuesta
                val body = response.body?.string()
                if(body!==null) {
                    Log.v("data", body)
                } else {
                    Log.v("data", "Empty")
                }

                // Objeto JSON con el cuerpo
                var jsonObject = JSONObject(body)
                // Array Choices que obtenemos del cuerpo
                var choices: JSONArray = jsonObject.getJSONArray("choices")
                // Obtenemos el contenido en formato String del
                // objeto Message en el Array Choices
                val content = choices.getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                // Devolvemos el contenido
                callback(content)
            }
        })
    }
}