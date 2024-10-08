package com.example.chatbotfssai

import android.content.Intent
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatbotfssai.databinding.ActivityMainBinding
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.Response
//import okhttp3.Call
//import okhttp3.Callback
//import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val messages = mutableListOf<Message>()
    private lateinit var speechRecognizer: SpeechRecognizer
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val FILE_PICKER_REQUEST_CODE = 1000
    private val CAMERA_REQUEST_CODE = 1001
    private var imageUri: Uri? = null
    private lateinit var question1: TextView
    private lateinit var question2: TextView
    private lateinit var question3: TextView
//    private lateinit var chatBotApi: ChatBotApiService
//    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://r9xdds3m-8000.inc1.devtunnels.ms/") // Replace with your base URL
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        chatBotApi = retrofit.create(ChatBotApiService::class.java)
//        client = OkHttpClient()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)
        val adapter = ChatAdapter(messages)

        binding.recyclerViewChat.adapter = adapter

        question1 = binding.question1
        question2 = binding.question2
        question3 = binding.question3

        // Set initial recommended questions
        setRecommendedQuestions()

        setUpQuestionClickListeners()

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        var listeningToast: Toast? = null
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listeningToast = Toast.makeText(this@MainActivity, "Listening...", Toast.LENGTH_SHORT)
                listeningToast?.show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                listeningToast?.cancel()
            }
            override fun onError(error: Int) {
                listeningToast?.cancel()
                val errorToast = Toast.makeText(this@MainActivity, "Error: $error", Toast.LENGTH_SHORT)
                errorToast.show()

                // Remove the error message after 2 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    errorToast.cancel()
                }, 2300)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    binding.editTextMessage.text.clear()
                    binding.editTextMessage.setText(recognizedText)
//                    addMessage(recognizedText, true)
//                    addMessage(getChatbotResponse(recognizedText), false)
//                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // Set up Send Button click listener
        binding.imageViewSend.setOnClickListener {
            val userMessage = binding.editTextMessage.text.toString()
            if (userMessage.isNotBlank()) {
                addMessage(userMessage, true)
                addMessage(getChatbotResponse(userMessage), false)
                binding.editTextMessage.text.clear()
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)

            }
        }

        // Set up Mic Button click listener
        binding.imageViewMic.setOnClickListener {
            startSpeechToText()
        }
        binding.imageViewAttachments.setOnClickListener {
            showAttachmentOptions()
        }

        addMessage("Greetings! I am FSSAI's Virtual AI Assistant. I can help you with queries related to food safety, standards, licensing, or anything under FSSAI's domain in India.\n", false)
    }


//    private fun getChatbotResponse(input: String) {
//        val url = "https://r9xdds3m-8000.inc1.devtunnels.ms/ask?input=$input"
//
//        val request = Request.Builder()
//            .url(url)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    val responseData = response.body?.string()
//                    runOnUiThread {
//                        addMessage(responseData ?: "I'm sorry, I don't understand that.", false)
//                        binding.recyclerViewChat.scrollToPosition(messages.size - 1)
//                    }
//                } else {
//                    runOnUiThread {
//                        addMessage("Error: Unable to reach the server.", false)
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call, e: IOException) {
//                runOnUiThread {
//                    addMessage("Error: ${e.message}", false)
//                }
//            }
//        })
//    }

    private fun setRecommendedQuestions() {
        question1.text = "How to apply for an FSSAI license  "
        question2.text = "what are the FSSAI's food regulations  "
        question3.text = "What are the food safety standards  "
    }

    private fun setUpQuestionClickListeners() {
        val questionClickListener = View.OnClickListener { view ->
            val questionText = (view as TextView).text.toString()
            binding.editTextMessage.setText(questionText) // Set the clicked question as user input
            hideRecommendedQuestions() // Hide other questions
        }

        question1.setOnClickListener(questionClickListener)
        question2.setOnClickListener(questionClickListener)
        question3.setOnClickListener(questionClickListener)
    }

    // Function to hide recommended questions
    private fun hideRecommendedQuestions() {
        question1.visibility = View.GONE
        question2.visibility = View.GONE
        question3.visibility = View.GONE
    }

    //to select attachment type
    private fun showAttachmentOptions() {
        val options = arrayOf("Camera", "Files")
        AlertDialog.Builder(this)

            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera() // Camera option selected
                    1 -> openFilePicker() // Files option selected
                }
            }
            .show()
    }

    // open the camera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    //open device files
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Set MIME type to all files
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                FILE_PICKER_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        handleFileUri(uri)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val imageUri = data?.data ?: this.imageUri
                    handleImageUri(imageUri)
                }
            }
        }
    }

    private fun handleFileUri(uri: Uri) {
        // Handle the file URI here (e.g., display file info, attach to message, etc.)
        val fileName = getFileName(uri)
        // You can add logic to attach this file name or content with the message
        addMessage("Attached file: $fileName", true)
    }

    private fun handleImageUri(uri: Uri?) {
        uri?.let {
            // Handle the image URI here (e.g., display image, attach to message, etc.)
            addMessage("Attached image", true)
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex("_display_name")
            if (nameIndex >= 0 && it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return null
    }

    private fun startSpeechToText() {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        speechRecognizer.startListening(intent)
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(Message(text, isUser))
        binding.recyclerViewChat.adapter?.notifyDataSetChanged()
    }

    private fun getChatbotResponse(input: String): String {

        // Convert input to lowercase and split it into individual words
        val words = input.lowercase().split(" ")

        return when {
            words.contains("hello") -> "Hello there! How can I assist you?"
            words.contains("how") && words.contains("are") && words.contains("you") -> "I'm just a bunch of code, but I'm doing fine! How about you?"
            words.contains("fine") -> "Great to know. What takes you here today?"
            words.contains("your") && words.contains("name") -> "I'm FSSAI's AI assistant, I resolve your issues regarding FSSAI."
            words.contains("bye") -> "Goodbye! Have a great day!"
            words.contains("license") -> "The Food Safety and Standards Authority of India (FSSAI) is responsible for licensing food businesses in India. Food businesses are required to obtain a license from FSSAI before they can operate. The type of license required depends on the size and nature of the food business.\n" +
                    "\n" +
                    "To obtain a license, food businesses must submit an application to FSSAI. The application must include information about the business, such as its name, address, and the type of food it produces or sells. FSSAI will review the application and issue a license if the business meets the necessary requirements.\n" +
                    "\n" +
                    "More information on Food Licensing\n" +
                    "\n" +
                    "You can find more information about food licensing on the FSSAI website at: https://fssai.gov.in/food-licensing/ "
            words.contains("licensing") -> "The Food Safety and Standards Authority of India (FSSAI) is responsible for licensing food businesses in India. Food businesses are required to obtain a license from FSSAI before they can operate. The type of license required depends on the size and nature of the food business.\n" +
                    "\n" +
                    "To obtain a license, food businesses must submit an application to FSSAI. The application must include information about the business, such as its name, address, and the type of food it produces or sells. FSSAI will review the application and issue a license if the business meets the necessary requirements.\n" +
                    "\n" +
                    "More information on Food Licensing\n" +
                    "\n" +
                    "You can find more information about food licensing on the FSSAI website at: https://fssai.gov.in/food-licensing/ "
            words.contains("regulations") -> "The FSSAI has specific regulations for food labeling in India, as outlined in the Food Safety and Standards (Packaging and Labeling) Regulations, 2011. These regulations aim to ensure that consumers have access to accurate and complete information about the food they are purchasing.\n" +
                    "\n" +
                    "Key Requirements of FSSAI Food Labeling Regulations:\n" +
                    "\n" +
                    "Mandatory Information: Food labels must prominently display specific information, including:\n" +
                    "\n" +
                    "Name of the food (common or proprietary)\n" +
                    "List of ingredients in descending order of weight\n" +
                    "Net quantity\n" +
                    "Name and complete address of the manufacturer/packer/importer\n" +
                    "Best before or use by date\n" +
                    "Instructions for use, if necessary\n" +
                    "Country of origin, for imported foods\n" +
                    "Nutritional Information Panel: Packaged foods must display a nutritional information panel providing details on calories, fat, saturated fat, carbohydrates, sugars, protein, and sodium per serving.\n" +
                    "\n" +
                    "Allergen Labeling: Foods containing common allergens (e.g., peanuts, milk, soy) must clearly label the presence of these allergens.\n" +
                    "\n" +
                    "Health Claims: Food labels cannot make unsubstantiated health claims or imply that a food has medicinal properties.\n" +
                    "\n" +
                    "Front-of-Pack Labeling (FoPL): The FSSAI has introduced FoPL guidelines to help consumers make healthier choices. FoPL labels provide key nutritional information in an easy-to-understand format.\n" +
                    "\n" +
                    "Warning Statements: Certain foods, such as those high in sugar or salt, may require warning statements on the label.\n" +
                    "\n" +
                    "Exceptions and Exemptions:\n" +
                    "\n" +
                    "Some food categories, such as fresh fruits and vegetables, may be exempt from certain labeling requirements. The FSSAI website provides a comprehensive list of exemptions and exceptions.\n" +
                    "\n" +
                    "Enforcement:\n" +
                    "\n" +
                    "The FSSAI is responsible for enforcing food labeling regulations in India. Food businesses must comply with these regulations to avoid penalties and ensure consumer safety.\n" +
                    "\n" +
                    "Additional Information:\n" +
                    "\n" +
                    "For more detailed information on FSSAI food labeling regulations, please visit the FSSAI website: Food Safety and Standards (Packaging and Labeling) Regulations, 2011"
            words.contains("standards") -> "Safety Standards for Packaged Food in India\n" +
                    "\n" +
                    "The Food Safety and Standards Authority of India (FSSAI) has established comprehensive safety standards for packaged food to ensure the health and safety of consumers. These standards cover various aspects of food safety, including:\n" +
                    "\n" +
                    "Packaging:\n" +
                    "\n" +
                    "The packaging material must be food-grade and approved by FSSAI.\n" +
                    "It must protect the food from contamination, spoilage, and damage.\n" +
                    "The packaging should provide adequate information about the food, including ingredients, nutritional value, shelf life, and storage instructions.\n" +
                    "Hygiene:\n" +
                    "\n" +
                    "Food must be processed and packaged in hygienic conditions to prevent contamination.\n" +
                    "Equipment and facilities used for packaging must be cleaned and sanitized regularly.\n" +
                    "Employees involved in food handling must follow good hygiene practices.\n" +
                    "Ingredients:\n" +
                    "\n" +
                    "All ingredients used in packaged food must be safe for consumption and meet FSSAI specifications.\n" +
                    "The presence of food additives must be within permissible limits and clearly labeled on the packaging.\n" +
                    "Genetically modified ingredients must be labeled as per regulations.\n" +
                    "Nutrition:\n" +
                    "\n" +
                    "Packaged food must meet specific nutritional standards set by FSSAI.\n" +
                    "Nutrition information must be clearly displayed on the packaging, including energy value, protein, carbohydrates, fat, and other essential nutrients.\n" +
                    "Labeling:\n" +
                    "\n" +
                    "All packaged food products must have clear and accurate labeling that complies with the FSSAI Labeling Regulations.\n" +
                    "The label must include the following information:\n" +
                    "Name of the food\n" +
                    "List of ingredients\n" +
                    "Nutritional information\n" +
                    "Shelf life\n" +
                    "Storage instructions\n" +
                    "Manufacturer's name and address\n" +
                    "FSSAI license number\n" +
                    "Food Additives:\n" +
                    "\n" +
                    "Food additives used in packaged food must be approved by FSSAI and used within permissible limits.\n" +
                    "FSSAI maintains a Positive List of food additives that are safe for use in India.\n" +
                    "Inspection and Enforcement:\n" +
                    "\n" +
                    "FSSAI conducts regular inspections of food businesses to ensure compliance with food safety regulations.\n" +
                    "Non-compliant businesses may face penalties, including fines or license cancellation.\n" +
                    "Consumer Safety:\n" +
                    "\n" +
                    "FSSAI provides consumers with information and resources to promote food safety and healthy eating habits.\n" +
                    "Consumers can report any food safety concerns or complaints to FSSAI through various channels.\n" +
                    "Additional Information:\n" +
                    "\n" +
                    "For more detailed information on food safety standards for packaged food in India, please visit the FSSAI website (https://fssai.gov.in/) or contact the FSSAI helpline at 1800-11-2100."
            words.contains("inspection") -> "Inspections help ensure food safety compliance."
            else -> "I'm sorry, I don't understand that. Can you try asking something else?"
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}

data class Message(val text: String, val isUser: Boolean)
