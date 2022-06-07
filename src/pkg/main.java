package pkg;

import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;


public class main {

    public static void main(String[] args) throws Exception {

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(ch.qos.logback.classic.Level.OFF);

        List<leader_follower> leaderElections = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            leaderElections.add(new leader_follower(i));
        }

        for (leader_follower  le : leaderElections){
            le.start();
            le.join();
        }

        Thread.sleep(2000);

        for (leader_follower  le : leaderElections){
            le.stop();
        }

    }

}
