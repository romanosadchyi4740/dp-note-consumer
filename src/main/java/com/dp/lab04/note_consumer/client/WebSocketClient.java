package com.dp.lab04.note_consumer.client;

import com.dp.lab04.note_consumer.model.Note;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class WebSocketClient {
    private final PublishSubject<Note> noteSubject = PublishSubject.create();

    public Observable<Note> connect(String url) {
        System.out.println("Trying to connect to " + url);

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("Successfully connected to WebSocket!");
                System.out.println("Subscribing to /topic/notes...");

                session.subscribe("/topic/notes", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return Note.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        if (payload instanceof Note) {
                            noteSubject.onNext((Note) payload);
                        }
                    }
                });

                System.out.println("Subscription active.\n");
            }

            @Override
            public void handleException(StompSession session, StompCommand command,
                                        StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("WebSocket error: " + exception.getMessage());
                exception.printStackTrace();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.out.println("Transport error occurred.");
                exception.printStackTrace();
            }
        };

        try {
            System.out.println("Waiting for a connection...");
            stompClient.connectAsync(url, sessionHandler)
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("Error connecting to WebSocket!");
            e.printStackTrace();
        }

        return noteSubject;
    }
}