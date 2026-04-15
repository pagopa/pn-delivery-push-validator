package it.pagopa.pn.deliverypushvalidator.service.impl;

import it.pagopa.pn.deliverypushvalidator.validation.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockMessageClientTest {

    private MockMessageClient client;

    @BeforeEach
    void setup() {
        client = new MockMessageClient();
    }

    @Test
    void crud_lifecycle() {
        MessageData msg = MessageData.builder()
                .messageId("M1").paId("PA").language("IT").subject("sub").longBody("body").build();

        // create
        client.create(msg);
        Optional<MessageData> found = client.getById("M1");
        assertTrue(found.isPresent());
        assertEquals("M1", found.get().getMessageId());

        // update
        MessageData updated = msg.toBuilder().subject("new sub").build();
        client.update(updated);
        assertEquals("new sub", client.getById("M1").orElseThrow().getSubject());

        // delete
        client.delete("M1");
        assertTrue(client.getById("M1").isEmpty());
    }

    @Test
    void getById_notFound() {
        assertTrue(client.getById("MISSING").isEmpty());
    }
}

