package com.uniqademo.todolamda;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.*;

class TodoLamdaApplicationTest {

    TodoLamdaApplication todoLamdaApplication = new TodoLamdaApplication();

    @Test
    public void shouldSaveTodo() {
        // given:
        Message<TodoLamdaApplication.Todo> inputMessage = MessageBuilder.createMessage(new TodoLamdaApplication.Todo(null, "abc"), new MessageHeaders(null));

        // when
        Message<TodoLamdaApplication.Todo> result = todoLamdaApplication.createTodo().apply(inputMessage);

        // then
        Assertions.assertEquals("abc", result.getPayload().getText());
        Assertions.assertNotEquals(null, result.getPayload().getId());
        Assertions.assertEquals(201, result.getHeaders().get("statusCode"));
    }

}