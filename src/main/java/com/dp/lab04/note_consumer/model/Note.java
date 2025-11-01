package com.dp.lab04.note_consumer.model;

public record Note(String noteName, int octave, long timestamp, int velocity) {
    public String getFullNoteName() {
        return noteName + octave;
    }
}
