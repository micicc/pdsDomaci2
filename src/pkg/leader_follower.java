package pkg;

import org.apache.zookeeper.*;
import java.io.IOException;
import java.util.*;

public class leader_follower implements Watcher {

    String connectionString;

    ZooKeeper zk;
    boolean lider = false;
    String mojGlas = "";
    String id;

    public leader_follower(int id) {
        Random r = new Random();
        this.id = id + "-" + UUID.randomUUID().toString().split("-")[4] ;
        this.connectionString = "localhost:2181";
    }

    public void start() throws IOException {
        this.zk = new ZooKeeper(this.connectionString, 500, this);
    }

    public void join() throws InterruptedException, KeeperException {
        try {
            zk.create("/leader", "start".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println(this.id + ": Postao sam lider!");
            this.lider = true;

            List<String> glasovi = zk.getChildren("/votes", true);
            System.out.println(this.id + ": Ukupno glasova: " + glasovi.size());

        } catch (KeeperException e) {

            System.out.println(this.id + ": Lider vec postoji, čekam da glasam!" );
            String content = new String(zk.getData("/leader", true,null));

            if (content.equals("start")){

                Random r = new Random();
                String glas = "Ne";
                if(r.nextBoolean())
                    glas="Da";
                this.mojGlas = glas;
                try {
                    String path = zk.create("/votes/vote-", glas.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                    String vote = new String(zk.getData(path,false,null));
                    System.out.println(this.id + ": Glasao sam za  " + vote + "!");

                } catch (KeeperException e2) {

                }
            }
        }
    }

    public void stop() throws InterruptedException {
        zk.close();
        System.out.println(this.id + ": Odoh kuci!");
    }

    @Override
    public void process(WatchedEvent event) {

        if (event.getPath().equals("/leader") && !lider) {
            try {

                String winningVote = new String(zk.getData("/leader", false, null));

                String result = "pobedaio";
                if (!winningVote.equals(mojGlas)){
                    result = "izgubio";
                }
                System.out.println(this.id + ": Moj glas je: " + result + "  (Galsao sam za " + this.mojGlas + ")");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (event.getPath().contains("/votes") && lider) {
            try {
                List<String> children = zk.getChildren("/votes", true);
                System.out.println(this.id + ": Neko je glasao!  Ukupno glasova: " + children.size() );

                if (children.size() == 3) {
                    String pobeda = this.prebrojGlasove(children);
                    System.out.println(this.id + ": Pobednicki glas je:  " + pobeda + "!");
                    this.zk.setData("/leader", pobeda.toString().getBytes(), 0);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String prebrojGlasove(List<String> votingNodes) throws InterruptedException, KeeperException {

        int rez = 0;
        for (String node : votingNodes) {
            byte[] content = zk.getData("/votes/"+node, false, null);
            String vote = new String(content);
            if(vote.equals("Da"))
                rez++;
            else
                rez --;
        }
        if (rez > 0){
            return "Da";
        }
        return "Ne";
    }

}