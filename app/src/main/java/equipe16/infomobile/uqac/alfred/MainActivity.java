package equipe16.infomobile.uqac.alfred;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
            Log.i("ALF", e.getMessage());
        }

        //Log.i("ALF", "Loaded script : \n" + script);
        bot.stream(script);

        bot.sortReplies();

        editText = findViewById(R.id.question);
        answer = findViewById(R.id.answer);
    }

    public void submit(View view) {
        String question = editText.getText().toString();
        String prefix;
        String content;
        if (question.length() == 0)
            answer.setText("Question vide !");
        else {
            String reply = bot.reply("user", question);
            String[] arr = reply.split(";");

            prefix = arr[0];

            // On retire le premier élément de la liste (le préfixe)
            List<String> list = new ArrayList<>(Arrays.asList(reply));
            list.remove(0);
            arr = list.toArray(new String[0]);

            // Concaténation de la liste pour reformer le contenu
            StringBuilder sb = new StringBuilder();
            for(String s:
                    arr) {
                sb.append(s);
            }
            content = sb.substring(0, sb.length() - 1);

            Log.i("ALF", "Prefix : " + prefix);
            Log.i("ALF", "Content : " + prefix);

            // Test du type de la réponse et exécution
            if (prefix.equals("REP"))
                executeReply(content);
            else if (prefix.equals("CMD"))
                executeCommand(content);

            answer.setText(bot.reply("user", question));
        }
    }

    public void executeReply(String reply) {
        Log.i("ALF", "Reply");
        answer.setText(reply);
    }

    public void executeCommand(String cmd) {

    }

}
