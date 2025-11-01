package com.dp.lab04.note_consumer.service;

import com.dp.lab04.note_consumer.client.WebSocketClient;
import com.dp.lab04.note_consumer.model.Note;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class NoteProcessorService {
    private final Set<Disposable> disposables = new HashSet<>();
    private final WebSocketClient webSocketClient;
    private static final String WS_URL = "ws://localhost:8080/ws-notes/websocket";

    public NoteProcessorService(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public void startProcessing() {
        Observable<Note> noteStream = webSocketClient.connect(WS_URL);

        disposables.add(noteStream
                .skip(10)
                .map(note -> String.format("%s%d (velocity: %d)",
                        note.noteName(), note.octave(), note.velocity()))
                .subscribe(
                        fullName -> System.out.println("  MAP: " + fullName),
                        error -> System.err.println("MAP error: " + error.getMessage())
                ));

        disposables.add(noteStream
                .filter(note -> note.octave() >= 5)
                .subscribe(
                        note -> System.out.println("  FILTER (high notes): " + note.getFullNoteName()),
                        error -> System.err.println("FILTER error: " + error.getMessage())
                ));

        disposables.add(noteStream
                .flatMap(note -> Observable.fromArray(
                        "Name: " + note.noteName(),
                        "Octave: " + note.octave(),
                        "Velocity: " + note.velocity()
                ))
                .subscribe(
                        info -> System.out.println("  FLATMAP: " + info),
                        error -> System.err.println("FLATMAP error: " + error.getMessage())
                ));

        disposables.add(noteStream
                .take(20)
                .map(Note::velocity)
                .reduce(Integer::sum)
                .subscribe(
                        totalVelocity -> {
                            double avg = totalVelocity / 20.0;
                            System.out.println("  REDUCE: Average velocity (20 notes): " +
                                    String.format("%.2f", avg));
                        },
                        error -> System.err.println("REDUCE error: " + error.getMessage())
                ));

        Observable<Note> highNotes = noteStream.filter(n -> n.octave() >= 5);
        Observable<Note> lowNotes = noteStream.filter(n -> n.octave() < 3);

        disposables.add(Observable.merge(
                        highNotes.map(n -> "HIGH: " + n.getFullNoteName()),
                        lowNotes.map(n -> "LOW: " + n.getFullNoteName())
                )
                .subscribe(
                        msg -> System.out.println("  MERGE: " + msg),
                        error -> System.err.println("MERGE error: " + error.getMessage())
                ));

        disposables.add(noteStream
                .filter(note -> note.velocity() > 80)
                .map(Note::getFullNoteName)
                .buffer(3)
                .subscribe(
                        notes -> System.out.println("  Chord: " +
                                String.join(" + ", notes)),
                        error -> System.err.println("Complex error: " + error.getMessage())
                ));

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Listening for notes sequence...");
        System.out.println("=".repeat(70) + "\n");
    }

    public void stopProcessing() {
        disposables.forEach(Disposable::dispose);
    }
}
