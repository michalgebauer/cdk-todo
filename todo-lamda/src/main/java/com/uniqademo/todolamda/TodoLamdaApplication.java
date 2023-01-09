package com.uniqademo.todolamda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@SpringBootApplication
public class TodoLamdaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoLamdaApplication.class, args);
	}

	@Bean
	public Function<Message<Todo>, Message<Todo>> createTodo() {
		return todoMessage -> {
			Todo todo = todoMessage.getPayload();
			// save :-)
			todo.setId(UUID.randomUUID().toString());

			MessageHeaders messageHeaders = new MessageHeaders(Map.of(
					"statusCode", 201,
					"Content-Type", "application/json",
					"Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent",
					"Access-Control-Allow-Origin", "*",
					"Access-Control-Allow-Methods", "OPTIONS,POST,GET"));

			return MessageBuilder.createMessage(todo, messageHeaders);
		};
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Todo {
		private String id;
		private String text;
	}

}
