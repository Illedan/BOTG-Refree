import com.codingame.gameengine.runner.GameRunner;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.Properties;

public class Main {
    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("seed", "1337");
        //Use new GameRunner(props); // if you want custom game seed.
        GameRunner gameRunner = new GameRunner();

        // ++++++++++++++++++++++++++++++++++++++++++++++++++//
        //                                                   //
        //    Comment on or off the bots you want to use     //
        //                                                   //
        // ++++++++++++++++++++++++++++++++++++++++++++++++++//

        //WAIT BOTS
        gameRunner.addAgent(SimpleWaitBot.class);
        gameRunner.addAgent(SimpleWaitBot.class);

        // Example of running another language than Java. It would be the same arguments in front of the path used in a console.

        // C# (mono is needed on a mac)
        //gameRunner.addAgent("mono /Users/myuser/Projects/BotG/CG.BotG/bin/Debug/CG.BotG.exe");

        gameRunner.start();
    }
}
