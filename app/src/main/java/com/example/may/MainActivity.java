package com.example.may;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    private String question3;
    String question;

    Button login_mobilenumber;
    MessageAdapter messageAdapter;

    public static final MediaType JSON = MediaType.get("application/json");
    OkHttpClient client = new OkHttpClient();

    private ImageView iv_mic;



    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private TextView tv_Speech_to_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        messageList=new ArrayList<>();

        recyclerView=findViewById(R.id.recycler_view);
        welcomeTextView=findViewById(R.id.welcomea_text);
        messageEditText=findViewById(R.id.message_edit_text);
        sendButton=findViewById(R.id.send_btn);

        iv_mic = findViewById(R.id.iv_mic);
//        tv_Speech_to_text = findViewById(R.id.tv_speech_to_text);


//set the recycler
        messageAdapter=new MessageAdapter(messageList);

        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm=new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question =messageEditText.getText().toString().trim();
                addToChat(question,Message.SENT_BY_ME);
          messageEditText.setText("");
            callAPI(question);
         welcomeTextView.setVisibility(View.GONE);
            }
        });




        iv_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to input");

                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
//                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//
//                // Retrieve the converted text
//                String convertedText = Objects.requireNonNull(result).get(0);
//
//                // Set the text to TextView
//                tv_Speech_to_text.setText(convertedText);
//
//                // Save the converted text to a variable
//                question = convertedText; // 'question' is a String variable declared manually
//                messageEditText.setText("");
//                callAPI("");
//                welcomeTextView.setVisibility(View.GONE);
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String speechText = result.get(0);

                    // Display recognized text in a Toast
                    //Toast.makeText(this, "Recognized Speech: " + speechText, Toast.LENGTH_SHORT).show();
                      question=  speechText;
                      callAPI(question);


                }

            }
        }
    }


    void addToChat(String message,String sentBy)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }

    void addResponse(String response)
    {
//   messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }

    public  void callAPI(String question) {
//        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));

        // Create JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("inputs", question); // Add the user's question dynamically
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Prepare the request
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill") // Model URL
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer <<place api key>>") // Your API key
                .post(body)
                .build();



//                .url("https://api-inference.huggingface.co/models/humarin/chatgpt_paraphraser_on_T5_base") // Use the new model URL
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer <<api key here>>") // Use the provided API key
//                .post(body)
//                .build();





        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Parse the response
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        String result = jsonArray.getJSONObject(0).getString("generated_text");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        addResponse("Failed to parse response: " + e.getMessage());
                    } finally {
                        response.close(); // Ensure the response body is closed
                    }
                } else {
                    addResponse("Failed to load response. Error: " + response.code());
                }
            }
        });
    }



//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
//            if (resultCode == RESULT_OK && data != null) {
//                ArrayList<String> result = data.getStringArrayListExtra(
//                        RecognizerIntent.EXTRA_RESULTS);
//                tv_Speech_to_text.setText( Objects.requireNonNull(result).get(0));
//            }
//        }
//    }





//    public void callAPI(String question) {
////        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));
//
//        // Create JSON request body
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("inputs", question); // Add the user's question dynamically
//
//         } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        // Prepare the request body
//        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
//
//        // Prepare the request with the Hugging Face API details
//        Request request = new Request.Builder()
//                .url("https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill") // Model URL
//                .header("Authorization", "Bearer <<api key here>") // Your API key
//                .header("Content-Type", "application/json")
//                .post(body)
//                .build();
//
//        // Execute the request asynchronously
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                addResponse("Failed to load response due to: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    try {
//                        // Parse the response
//                        JSONArray jsonArray = new JSONArray(response.body().string());
//                        String result = jsonArray.getJSONObject(0).getString("generated_text");
//                        addResponse(result.trim());
//
//
//
//                    } catch (JSONException e) {
//                        addResponse("Failed to parse response: " + e.getMessage());
//                    } finally {
//                        response.close(); // Ensure the response body is closed
//                    }
//                } else {
//                    addResponse("Failed to load response. Error: " + response.code());
//                }
//            }
//        });
//    }






    //    void callAPI(String question) {
//        messageList.add(new Message("Typing...",Message.SENT_BY_BOT));
//        // Create JSON request body
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("model", "gpt-4o-mini-2024-07-18"); // Updated model name
//
//            // Create messages array
//            JSONArray messages = new JSONArray();
//
//            // Add system message
//            JSONObject systemMessage = new JSONObject();
//            systemMessage.put("role", "system");
//            systemMessage.put("content", "You are a helpful assistant.");
//            messages.put(systemMessage);
//
//            // Add user message dynamically
//            JSONObject userMessage = new JSONObject();
//            userMessage.put("role", "user");
//            userMessage.put("content", question); // Add the user's question from EditText
//            messages.put(userMessage);
//
//            // Add messages array to the body
//            jsonBody.put("messages", messages);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        // Prepare the request
//        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
//        Request request = new Request.Builder()
//                .url("https://api.openai.com/v1/chat/completions")
//                .header("Content-Type", "application/json" )
//                .header("Authorization", "Bearer <<api key here>>") // Replace with secure handling
//                .post(body)
//                .build();
//
//        // Execute the request
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                addResponse("Failed to load response due to: " + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    try {
//                        // Parse response JSON
//                        JSONObject jsonObject = new JSONObject(response.body().string());
//                        JSONArray choices = jsonObject.getJSONArray("choices");
//                        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
//                        String result = message.getString("content");
//                        addResponse(result.trim());
//                    } catch (JSONException e) {
//                        addResponse("Failed to parse response: " + e.getMessage());
//                    } finally {
//                        response.close(); // Ensure response body is closed
//                    }
//                } else {
//                    addResponse("Failed to load response. Error: " + response.code());
//                }
//            }
//        });
//    }

//    mmm


//
    //    void callAPI(String question)
    //    {
    //        //okhttp
    //        JSONObject jsonBody = new JSONObject();
    //        try {
    //            jsonBody.put("model", "o1-preview-2024-09-12");
    //
    //            // Create the messages array
    //            JSONArray messages = new JSONArray();
    //
    //            // Add system message
    //            JSONObject systemMessage = new JSONObject();
    //            systemMessage.put("role", "system");
    //            systemMessage.put("content", "You are a helpful assistant.");
    //            messages.put(systemMessage);
    //
    //            // Add user message dynamically
    //            JSONObject userMessage = new JSONObject();
    //            userMessage.put("role", "user");
    //            userMessage.put("content", question); // Add the user's question from EditText
    //            messages.put(userMessage);
    //
    //            // Add messages array to the body
    //            jsonBody.put("messages", messages);
    //
    //        } catch (JSONException e) {
    //            e.printStackTrace();
    //        }
    //
    //        RequestBody body =RequestBody.create(jsonBody.toString(),JSON);
    //        Request request= new Request.Builder()
    //
    //                .url("https://api.openai.com/v1/chat/completions")
    //                .header("Authorization","Bearer <<api key here>>")
    //                .post(body)
    //                .build();
    //
    //        client.newCall(request).enqueue(new Callback() {
    //            @Override
    //            public void onFailure(@NonNull Call call, @NonNull IOException e) {
    //      addResponse("Failed to load response due to "+e.getMessage());
    //            }
    //
    //            @Override
    //            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
    //                if(response.isSuccessful())
    //                {
    //                    JSONObject jsonObject= null;
    //                    try {
    //                        jsonObject = new JSONObject(response.body().string());
    //                        JSONArray jsonArray=jsonObject.getJSONArray("choices");
    //                        String result=jsonArray.getJSONObject(0).getString("content");
    //                        addResponse(result.trim());
    //                    } catch (JSONException e) {
    //                        throw new RuntimeException(e);
    //                    }
    //                }
    //                else {
    //                    addResponse("Failed to load response due to the "+response.body().toString());
    //                }
    //            }
    //        });
    //
    //
    //
    //    }

}