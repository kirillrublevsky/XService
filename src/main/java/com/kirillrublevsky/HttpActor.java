package com.kirillrublevsky;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.util.Optional;

/**
 * Created by Kirill on 20.06.2017.
 */
public class HttpActor extends AbstractActor {

    public static Props props() {
        return Props.create(HttpActor.class, HttpActor::new);
    }

    public static class Count {
        public final long number;

        public Count(long number) {
            this.number = number;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder() //sending 'get' request to remote server in order to count words in text
                .match(CounterActor.Contents.class, contents -> {
                    try (final AsyncHttpClient client = new AsyncHttpClient()) {
                        final Response response = client.prepareGet("http://localhost:4567/count?str=" + contents.words)
                                .execute().get();
                        final Long number = Long.valueOf(response.getResponseBody());   //parsing http response, may receive error
                        contents.counter.tell(new Count(number), getSelf());    //sending message to 'grandparent' - CounterActor
                    }
                })
                .build();
    }

    //resending messages to new instance of HttpActor after failure
    @Override
    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        message.ifPresent(m -> getSelf().tell(m, getSender()));
    }
}
