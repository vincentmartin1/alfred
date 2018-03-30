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

public class MainActivity extends AppCompatActivity {

    RiveScript bot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ALF", "*** Application started ***");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // RiveScript bot initialization
        bot = new RiveScript();

        InputStream inputStream = getResources().openRawResource(R.raw.helloworld);
        String script = "";

        try {
            script = IOUtils.toString(inputStream);
        } catch(IOException e) {
            Log.i("ALF", e.getMessage());
        }

        Log.i("ALF", "Loaded script : \n" + script);
        bot.stream(script);

        bot.sortReplies();
    }

    public void submit(View view) {
        EditText editText = findViewById(R.id.question);
        TextView answer = findViewById(R.id.answer);

        String question = editText.getText().toString();
        if (question.length() == 0)
            answer.setText("Question vide !");
        else
            answer.setText(bot.reply("user", question));
    }

}
