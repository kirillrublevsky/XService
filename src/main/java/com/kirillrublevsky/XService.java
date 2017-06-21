package com.kirillrublevsky;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Backoff;
import akka.pattern.BackoffSupervisor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by Kirill on 20.06.2017.
 */
public class XService {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("XService");

        final Props httpProps = HttpActor.props();

        //props to create Supervisor with Backoff - to implement exponential backoff; creates Subordinate of HttpActor class
        final Props supervisorProps = BackoffSupervisor.props(
                Backoff.onStop(
                        httpProps,
                        "httpActor",
                        Duration.create(3, TimeUnit.SECONDS),
                        Duration.create(50, TimeUnit.SECONDS),
                        0.2));

        //creating four actors - supervisors
        final ActorRef firstSupervisor = system.actorOf(supervisorProps, "firstSupervisor");
        final ActorRef secondSupervisor = system.actorOf(supervisorProps, "secondSupervisor");
        final ActorRef thirdSupervisor = system.actorOf(supervisorProps, "thirdSupervisor");
        final ActorRef fourthSupervisor = system.actorOf(supervisorProps, "fourthSupervisor");

        //creating four parent actors which send massage to supervisors and accept massage from HttpActors
        //filecounter actor receive file names and corresponding supervisors as parameters
        final ActorRef firstFileCounter = system.actorOf(CounterActor.props(firstSupervisor, "file1.txt"), "firstCounter");
        final ActorRef secondFileCounter = system.actorOf(CounterActor.props(secondSupervisor, "file2.txt"), "secondCounter");
        final ActorRef thirdFileCounter = system.actorOf(CounterActor.props(thirdSupervisor, "file3.txt"), "thirdCounter");
        final ActorRef fourthFileCounter = system.actorOf(CounterActor.props(fourthSupervisor, "file4.txt"), "fourthCounter");

        //sending messages to filecounters
        firstFileCounter.tell(new CounterActor.Text(), ActorRef.noSender());
        secondFileCounter.tell(new CounterActor.Text(), ActorRef.noSender());
        thirdFileCounter.tell(new CounterActor.Text(), ActorRef.noSender());
        fourthFileCounter.tell(new CounterActor.Text(), ActorRef.noSender());


    }
}
