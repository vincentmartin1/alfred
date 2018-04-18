package equipe16.infomobile.uqac.alfred;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rivescript.RiveScript;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
     * @param arr array to extract the prefix
     * @return prefix of the array
     */
    public String prefix(String[] arr) {
        // Récupération du préfixe
        String prefix = arr[0];

        return prefix;
    }

    /**
     * Returns the content of an array (i.e. the concatenation
     * of all its elements except the first one).
     * @param arr array to extract the content
     * @return content of the array
     */
    public String content(String[] arr) {
        // On retire le premier élément de la liste (le préfixe)
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        list.remove(0);
        arr = list.toArray(new String[0]);

        // Concaténation de la liste pour reformer le contenu
        StringBuilder sb = new StringBuilder();
        for(String s:
                arr) {
            sb.append(s);
        }

        // Récupération du contenu
        String content = sb.substring(0, sb.length());

        return content;
    }

    /**
     * Sets up the main activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ALF", "*** Application started ***");
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
        String question = editText.getText().toString();
        String prefix;
        String content;
        if (question.length() == 0)
            answer.setText("Question vide !");
        else {
            // Récupération de la réponse du bot
            String reply = bot.reply("user", question);

            // Split de la réponse par des ';'
            String[] arr = reply.split(";");

            prefix = prefix(arr);
            content = content(arr);

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
     * Runs dispatch() function.
     * @param content content of the bot's reply
     */
    public void executeCommand(String content) {
        log("TYPE: COMMAND");
        answer.setText("Executing command..");

        String[] arr = content.split(";");

        String command = prefix(arr);
        content = content(arr);

        switch(command) {
            case "OPEN":
                cmdOpen(content);
                break;
            case "TEXT":
                cmdText(content);
                break;
            case "MAIL":
                cmdMail(content);
                break;
            case "CALL":
                cmdCall(content);
                break;
            case "WEB":
                cmdWeb(content);
                break;
            case "DATE":
                cmdDate(content);
                break;
            case "WEATHER":
                cmdWeather(content);
                break;
            case "POSITION":
                cmdPosition(content);
                break;
            case "EVENT":
                cmdEvent(content);
                break;
            case "REMINDER":
                cmdReminder(content);
                break;
            case "MATH":
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
    }

    /**
     * Calls a given contact.
     * If the contact is not known, tells the user.
     * @param content argument(s) of the command
     */
    private void cmdCall(String content) {
    }

    /**
     * Runs a Web search and announces the result to the user.
     * @param content argument(s) of the command
     */
    private void cmdWeb(String content) {
    }

    /**
     * Tells the current date to the user.
     * @param content argument(s) of the command
     */
    private void cmdDate(String content) {
    }

    /**
     * Tells the current weather of a place to the user.
     * @param content argument(s) of the command
     */
    private void cmdWeather(String content) {
    }

    /**
     * Tells the current user's position.
     * @param content argument(s) of the command
     */
    private void cmdPosition(String content) {
    }

    /**
     * Adds an event to the calendar.
     * @param content argument(s) of the command
     */
    private void cmdEvent(String content) {
    }

    /**
     * Adds a new reminder.
     * @param content argument(s) of the command
     */
    private void cmdReminder(String content) {
    }

    /**
     * Runs a mathematical operation.
     * @param content argument(s) of the command
     */
    private void cmdMath(String content) {
        String[] arr;
    }
}
