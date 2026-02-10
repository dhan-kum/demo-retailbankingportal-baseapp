package com.eviden.app.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Spy
    private ConsumerService consumerService;

    @Test
    void connect_shouldDeclareQueueAndCreateConsumer() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Channel mockChannel = mock(Channel.class);
        when(mockConnection.createChannel()).thenReturn(mockChannel);

        doReturn(mockConnection).when(consumerService).getConnection();

        consumerService.connect("test_queue");

        verify(mockConnection).createChannel();
        verify(mockChannel).queueDeclare("test_queue", false, false, false, null);
    }

    @Test
    void connect_shouldCloseChannelAndConnection() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Channel mockChannel = mock(Channel.class);
        when(mockConnection.createChannel()).thenReturn(mockChannel);

        doReturn(mockConnection).when(consumerService).getConnection();

        consumerService.connect("test_queue");

        verify(mockChannel).close();
        verify(mockConnection).close();
    }

    @Test
    void connect_shouldThrowExceptionWhenConnectionFails() throws Exception {
        doThrow(new IOException("Connection refused")).when(consumerService).getConnection();

        assertThrows(IOException.class, () -> consumerService.connect("test_queue"));
    }

    @Test
    void connect_shouldThrowExceptionWhenChannelCreationFails() throws Exception {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.createChannel()).thenThrow(new IOException("Channel creation failed"));

        doReturn(mockConnection).when(consumerService).getConnection();

        assertThrows(IOException.class, () -> consumerService.connect("test_queue"));

        verify(mockConnection).close();
    }

    @Test
    void connect_shouldThrowExceptionOnTimeout() throws Exception {
        doThrow(new TimeoutException("Connection timed out")).when(consumerService).getConnection();

        assertThrows(TimeoutException.class, () -> consumerService.connect("test_queue"));
    }

    @Test
    void connect_shouldHandleQueueDeclareFailing() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Channel mockChannel = mock(Channel.class);
        when(mockConnection.createChannel()).thenReturn(mockChannel);
        when(mockChannel.queueDeclare(anyString(), anyBoolean(), anyBoolean(), anyBoolean(), isNull()))
                .thenThrow(new IOException("Queue declare failed"));

        doReturn(mockConnection).when(consumerService).getConnection();

        assertThrows(IOException.class, () -> consumerService.connect("test_queue"));
    }

    @Test
    void connect_shouldWorkWithDifferentQueueNames() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Channel mockChannel = mock(Channel.class);
        when(mockConnection.createChannel()).thenReturn(mockChannel);

        doReturn(mockConnection).when(consumerService).getConnection();

        consumerService.connect("another_queue");

        verify(mockChannel).queueDeclare("another_queue", false, false, false, null);
    }

    @Test
    void consumerService_shouldBeInstantiable() {
        ConsumerService service = new ConsumerService();
        assertNotNull(service);
    }
}
