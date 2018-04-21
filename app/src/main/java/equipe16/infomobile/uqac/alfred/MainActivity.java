package equipe16.infomobile.uqac.alfred;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.ContentResolver;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.Cursor;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.provider.ContactsContract;

import com.rivescript.RiveScript;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Logger;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.ui.SpeechProgressView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS =1 ;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE =2 ;
    private static final int MY_PERMISSIONS_REQUEST_USE_MICROPHONE = 3;

    RiveScript bot;
    EditText editText;
    TextView answer;
    TextView txtSpeechInput;
    SpeechProgressView speechProgressView;

    ArrayList<Contact> contactList = new ArrayList<>();

    TextToSpeech t1;

    String request_question;
    private boolean processing = false;

    // ------
    // UTIL FUNCTIONS
    // ------

    /**
     * Logs the string cast of an object.
     * @param object object (toString) to log.
     */
    public void log(Object object) {
        Log.i("ALF", object.toString());
    }

    /**
     * Returns the prefix of an array (i.e. the first
     * element of the array).
     * @param string string to extract the prefix
     * @return prefix of the array
     */
    public String prefix(String string) {
        // Conversion de la chaîne en liste
        String[] arr = string.split(";");

        // Récupération du préfixe
        return arr[0];
    }

    /**
     * Returns the content of an array (i.e. the concatenation
     * of all its elements except the first one).
     * @param string array to extract the content
     * @return content of the array
     */
    public String content(String string) {
        // Conversion de la chaîne en liste
        String[] arr = string.split(";");

        // On retire le premier élément de la liste (le préfixe)
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        list.remove(0);
        arr = list.toArray(new String[0]);

        // Concaténation de la liste pour reformer le contenu
        StringBuilder sb = new StringBuilder();
        for(String s:
                arr) {
            sb.append(s);
            sb.append(";");
        }

        // Récupération du contenu
        if (sb.length() > 0)
            return sb.substring(0, sb.length() - 1) + "";
        else
            return "";
    }

    /**
     * Sets up the main activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("*** Application started ***");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RiveScript bot initialization
        bot = new RiveScript();
        // bot.setHandler("javascript", new JavaScriptHandler());


        InputStream inputStream = getResources().openRawResource(R.raw.brain);
        String script = "";

        try {
            script = IOUtils.toString(inputStream);
        } catch(IOException e) {
            log(e.getMessage());
        }

        bot.stream(script);

        bot.sortReplies();

        //txtSpeechInput = findViewById(R.id.txtSpeechInput);
        speechProgressView = findViewById(R.id.progress);
        editText = findViewById(R.id.question);
        answer = findViewById(R.id.answer);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.FRENCH);
                }
            }
        });

        Speech.init(this, getPackageName());

        executePermission();
        getContactList();


        int[] colors = {
                ContextCompat.getColor(this, R.color.alf_blue),
                ContextCompat.getColor(this, R.color.alf_blue),
                ContextCompat.getColor(this, R.color.alf_darkgreen),
                ContextCompat.getColor(this, R.color.alf_red),
                ContextCompat.getColor(this, R.color.alf_red)
        };
        speechProgressView.setColors(colors);
    }

    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }

    /**
     * Triggered when @submit button is pressed.
     * @param view Button
     */
    public void submit(View view) {
        editText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        if(!processing){
            promptSpeechInput();
        }
        else {
            String question = request_question;//editText.getText().toString();
            String prefix;
            String content;
            if (question.length() == 0)
                answer.setText("Question vide !");
            else {
                // Récupération de la réponse du bot
                String reply = bot.reply("user", question);

                prefix = prefix(reply);
                content = content(reply);

                // Test du type de la réponse et exécution
                if (prefix.equals("REP"))
                    executeReply(content);
                else if (prefix.equals("CMD"))
                    executeCommand(content);
                processing = false;
            }
        }
    }

    private void promptSpeechInput() {
        try {
            // you must have android.permission.RECORD_AUDIO granted at this point
            Speech.getInstance().startListening(speechProgressView, new SpeechDelegate() {
                @Override
                public void onStartOfSpeech() {
                    log("speech recognition is now active");
                }

                @Override
                public void onSpeechRmsChanged(float value) {
                    log("rms is now: " + value);
                }

                @Override
                public void onSpeechPartialResults(List<String> results) {
                    StringBuilder str = new StringBuilder();
                    for (String res : results) {
                        str.append(res).append(" ");
                    }

                    log("partial result: " + str.toString().trim());
                }

                @Override
                public void onSpeechResult(String result) {
                    processing = true;
                    log("result: " + result);
                    request_question = result;
                    submit(editText);
                }
            });
        } catch (SpeechRecognitionNotAvailable exc) {
            log("Speech recognition is not available on this device!");
            // You can prompt the user if he wants to install Google App to have
            // speech recognition, and then you can simply call:
            //
            // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
            //
            // to redirect the user to the Google App page on Play Store
        } catch (GoogleVoiceTypingDisabledException exc) {
            log("Google voice typing must be enabled!");
        }
    }

    // ------
    // PREFIX HANDLING
    // ------

    /**
     * Executes a reply, i.e. if the prefix of the bot's
     * reply is "REP".
     * @param content content of the bot's reply
     */
    public void executeReply(String content) {
        log("TYPE: REPLY");
        answer.setText(content);
        t1.speak(content, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Executes a command, i.e. if the prefix of the bot's
     * reply is "CMD".
     * @param content content of the bot's reply
     */
    public void executeCommand(String content) {
        log("TYPE: COMMAND");
        answer.setText("Executing command..");
        t1.speak("Exécution de la commande", TextToSpeech.QUEUE_FLUSH, null);

        String command = prefix(content);
        content = content(content);

        log("command = " + command);
        log("content = " + content);

        switch(command) {
            case "OPEN":
                log("OPEN");
                cmdOpen(content);
                break;
            case "TEXT":
                log("TEXT");
                cmdText(content);
                break;
            case "CALL":
                log("CALL");
                cmdCall(content);
                break;
            case "WEB":
                log("WEB");
                cmdWeb(content);
                break;
            case "DATE":
                log("DATE");
                cmdDate();
                break;
            case "TIME":
                log("TIME");
                cmdTime();
            case "WEATHER":
                log("WEATHER");
                cmdWeather(content);
                break;
            case "POSITION":
                log("POSITION");
                cmdPosition(content);
                break;
            case "EVENT":
                log("EVENT");
                cmdEvent(content);
                break;
            case "REMINDER":
                log("REMINDER");
                cmdReminder(content);
                break;
            case "MATH":
                log("MATH");
                cmdMath(content);
                break;
        }
    }

    // ------
    // COMMANDS
    // ------

    /**
     * Opens the given application.
     * If application is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdOpen(String content) {
        // Conversion of the content to lowercase
        content = content.toLowerCase();

        // Creation of the app list hashmap
        HashMap<String, String> appList = new HashMap<>();

        // Gets all the packages stored in the device
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(main, 0);

        // Initialization of the lists
        ArrayList<String> appNameList = new ArrayList<>();
        ArrayList<String> packageNameList = new ArrayList<>();

        // Stores the names in the hashmap
        // under the form (k, v) = (app name, package name)
        for(ResolveInfo resolve_info:
                packages) {
            try {
                String packageName = resolve_info.activityInfo.packageName;
                String appName = (String)pm.getApplicationLabel(
                        pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                boolean same = false;
                for(int i = 0 ; i < appNameList.size() ; i++) {
                    if(packageName.equals(packageNameList.get(i)))
                        same = true;
                }
                if(!same) {
                    appList.put(appName.toLowerCase(), packageName);
                }
            } catch(Exception e) {
                log("exception");
            }
        }

        // Checks if the wanted application is in the hashmap's keys
        boolean found = false;
        for(String appName:
                appList.keySet()) {
            if(content.equals(appName))
                found = true;
        }

        // If the app is known, opens it
        if (found) {
            String packageName = appList.get(content);
            Intent mIntent = getPackageManager().getLaunchIntentForPackage(
                    packageName);
            if (mIntent != null) {
                try {
                    startActivity(mIntent);
                } catch (ActivityNotFoundException err) {
                    executeReply("App not found");
                }
            }
        }
        // Else, tels the user
        else
            executeReply("App not found");

    }

    /**
     * Sends a text message to a given receiver.
     * If the receiver is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdText(String content) {
        String message;
        String phoneNo;
        String[] arr = content.split(";");
        Contact contact = getContact(arr[0]);
        if (contact.getDisplayName() == null){
            Toast.makeText(getApplicationContext(), "Contact not found", Toast.LENGTH_LONG).show();
        }
        else{
            message = arr[1];
            phoneNo = contact.getNumero();
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS envoyé.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Calls a given contact.
     * If the contact is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdCall(String content) {
        log("ntml");
        Contact contact = getContact(content);
        log(contact.getDisplayName());
        if (contact.getDisplayName() == null){
            Toast.makeText(getApplicationContext(), "Contact not found", Toast.LENGTH_LONG).show();
        }
        else{
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+contact.getNumero()));
            startActivity(callIntent);
            Toast.makeText(getApplicationContext(), "Appel lancé.", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Runs a Web search and announces the result to the user.
     * @param content argument(s) of the command
     */
    private void cmdWeb(String content) {
        // Traitement : fait une recherche web
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        String keyword = content;
        intent.putExtra(SearchManager.QUERY, keyword);
        startActivity(intent);
    }

    /**
     * Tells the current date to the user.
     */
    private void cmdDate() {
        // Traitement : donne la date
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.CANADA_FRENCH);

        Date date = new Date();

        executeReply("Il est " + dateFormat.format(date) + ".");
    }

    /**
     * Tells the current time to the user.
     */
    private void cmdTime() {
        // Traitement : donne l'heure
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CANADA_FRENCH);

        Date date = new Date();

        executeReply("Il est " + dateFormat.format(date) + ".");
    }

    /**
     * Tells the current weather of a place to the user.
     * @param content argument(s) of the command
     */
    private void cmdWeather(String content) {
        // Traitement : donne la météo
    }

    /**
     * Tells the current user's position.
     * @param content argument(s) of the command
     */
    private void cmdPosition(String content) {
        // Traitement : donne la localisation
    }

    /**
     * Adds an event to the calendar.
     * @param content argument(s) of the command
     */
    private void cmdEvent(String content) {
        // Traitement : ajoute un évènement dans le calendrier
    }

    /**
     * Adds a new reminder.
     * @param content argument(s) of the command
     */
    private void cmdReminder(String content) {
        // Traitement : ajoute un rappel
    }

    /**
     * Runs a mathematical operation.
     * @param content argument(s) of the command
     */
    private void cmdMath(String content) {
        String[] arr = content.split(";");
        double operand_1 = Double.parseDouble(arr[0]);
        double operand_2 = Double.parseDouble(arr[2]);
        String operator = arr[1];
        switch(operator){
            case "+" :
                executeReply((operand_1+operand_2)+"");
                break;
            case "-" :
                executeReply((operand_1-operand_2)+"");
                break;
            case "x" :
                executeReply((operand_1*operand_2)+"");
                break;
            case "/" :
                executeReply((operand_1/operand_2)+"");
                break;
            case "modulo" :
                executeReply((operand_1%operand_2)+"");
                break;
            case "puissance" :
                executeReply(pow(operand_1,operand_2)+"");
                break;
        }

    }

    // ------
    // PERMISSIONS
    // ------

    /**
     * Return an existing contact from its name if it exists,
     * or returns a new null Contact.
     * @param name name of the contact
     * @return found contact or null Contact
     */
    private Contact getContact(String name){
        for(Contact contact:
                contactList){
            log(contact.getDisplayName());
            if(contact.getDisplayName().toLowerCase().equals(name.toLowerCase())){
                return contact;
            }
        }
        return new Contact(null,null);
    }

    public void getContactList(){
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String displayName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            //log(displayName + " ----- " + phoneNumber);
            contactList.add(new Contact(displayName, phoneNumber));
        }
        phones.close();
    }

    /**
     * Asks all the permissions needed to the user.
     */
    private void executePermission() {
        permission_CALL_PHONE();
        permission_READ_CONTACTS();
        permission_SEND_SMS();
        permission_USE_MICROPHONE();
    }

    /**
     * Asks the user the permission to send SMS.
     */
    protected void permission_SEND_SMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS))) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    /**
     * Asks the user the permission to manage phone calls.
     */
    protected void permission_CALL_PHONE() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE))) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
        }
    }

    /**
     * Asks the user the permission to read the phone contacts.
     */
    protected void permission_READ_CONTACTS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS))) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }

    protected void permission_USE_MICROPHONE() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO))) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_USE_MICROPHONE);
            }
        }
    }
}
