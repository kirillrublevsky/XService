package com.kirillrublevsky;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Kirill on 21.06.2017.
 */
public class CounterActor extends AbstractActor {

    public static Props props(ActorRef supervisor, String fileName) {
        return Props.create(CounterActor.class, () -> new CounterActor(supervisor, fileName));
    }

    public static class Text {
        public Text() {
        }
    }

    public static class Contents {
        public final ActorRef counter;
        public final String words;

        public Contents(ActorRef counter, String words) {
            this.counter = counter;
            this.words = words;
        }
    }

    private final ActorRef supervisor;  //supervisor actor
    private final String fileName;  //file name to read

    public CounterActor(ActorRef supervisor, String fileName) {
        this.supervisor = supervisor;
        this.fileName = fileName;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Text.class, textFile -> {
                    String contents = new String(Files.readAllBytes(Paths.get(fileName)));  //reading contents of txt file
                    supervisor.tell(new Contents(getSelf(), contents), getSelf());  //sending contents to supervisor
                })  //receiving message from corresponding HttpActor with number of words fetched from remote server
                .match(HttpActor.Count.class, count -> System.out.println("File " + fileName + " has " + count.number + " words"))
                .build();
    }

}
