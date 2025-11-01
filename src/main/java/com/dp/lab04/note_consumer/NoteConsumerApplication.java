package com.dp.lab04.note_consumer;

import com.dp.lab04.note_consumer.service.NoteProcessorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NoteConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoteConsumerApplication.class, args);
	}

    @Bean
    public CommandLineRunner run(NoteProcessorService processor) {
        return args -> {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("🎹 Note Consumer запущено!");
            System.out.println("🔌 Підключення до WebSocket сервера...");
            System.out.println("=".repeat(70) + "\n");

            processor.startProcessing();

            Thread.currentThread().join();
            processor.stopProcessing();
        };
    }

}
