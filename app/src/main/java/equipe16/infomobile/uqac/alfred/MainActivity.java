package equipe16.infomobile.uqac.alfred;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rivescript.RiveScript;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RiveScript bot;
    EditText editText;
    TextView answer;

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
        return sb.substring(0, sb.length() - 1);
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

        //Log.i("ALF", "Loaded script : \n" + script);
        bot.stream(script);

        bot.sortReplies();

        editText = findViewById(R.id.question);
        answer = findViewById(R.id.answer);
    }

    /**
     * Triggered when @submit button is pressed.
     * @param view Button
     */
    public void submit(View view) {
        editText.onEditorAction(EditorInfo.IME_ACTION_DONE);

        String question = editText.getText().toString();
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
        }
    }

    /**
     * Executes a reply, i.e. if the prefix of the bot's
     * reply is "REP".
     * @param content content of the bot's reply
     */
    public void executeReply(String content) {
        log("TYPE: REPLY");
        answer.setText(content);
    }

    /**
     * Executes a command, i.e. if the prefix of the bot's
     * reply is "CMD".
     * @param content content of the bot's reply
     */
    public void executeCommand(String content) {
        log("TYPE: COMMAND");
        answer.setText("Executing command..");

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
            case "MAIL":
                log("MAIL");
                cmdMail(content);
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
                cmdDate(content);
                break;
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

    /**
     * Opens the given application.
     * If application is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdOpen(String content) {
        content = content.toLowerCase();

        HashMap<String, String> appList = new HashMap<>();

        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(main, 0);

        ArrayList<String> appNameList = new ArrayList<>();
        ArrayList<String> packageNameList = new ArrayList<>();

        for(ResolveInfo resolve_info : packages) {
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
                log("<" + appName + ">");
            } catch(Exception e) {
                log("exception");
            }
        }

        boolean found = false;
        for(String appName:
                appList.keySet()) {
            if(content.equals(appName))
                found = true;
        }

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
        else
            executeReply("App not found");

    }

    /**
     * Sends a text message to a given receiver.
     * If the receiver is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdText(String content) {
    }

    /**
     * Sends a mail to a given receiver.
     * If the receiver is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdMail(String content) {
        // Traitement : envoie un mail
    }

    /**
     * Calls a given contact.
     * If the contact is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdCall(String content) {
        // Traitement : passe un appel
    }

    /**
     * Runs a Web search and announces the result to the user.
     * @param content argument(s) of the command
     */
    private void cmdWeb(String content) {
        // Traitement : fait une recherche web
    }

    /**
     * Tells the current date to the user.
     * @param content argument(s) of the command
     */
    private void cmdDate(String content) {
        // Traitement : donne la date
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
        String[] arr;
        // Traitement : effectue une opération mathématique
    }
}
