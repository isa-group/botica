package es.us.isa.botica.protocol.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.protocol.TestRequestPacket;
import es.us.isa.botica.protocol.TestResponsePacket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@SuppressWarnings({"rawtypes", "unchecked"})
@MockitoSettings
class QueryHandlerTest {
  @Mock private ScheduledExecutorService executorService;
  @Mock private ScheduledFuture mockFuture;
  @Mock private Consumer<TestResponsePacket> mockCallback;
  @Mock private Runnable mockTimeoutCallback;

  @Captor private ArgumentCaptor<Runnable> scheduledTaskCaptor;
  @Captor private ArgumentCaptor<Long> delayCaptor;
  @Captor private ArgumentCaptor<TimeUnit> timeUnitCaptor;

  private QueryHandler queryHandler;

  @BeforeEach
  void setUp() {
    queryHandler = new QueryHandler(executorService);
  }

  @Test
  @DisplayName("registerQuery should set requestId and schedule timeout")
  void registerQuery_setsRequestIdAndSchedulesTimeout() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("testData");
    long timeout = 1000L;
    TimeUnit unit = TimeUnit.MILLISECONDS;

    when(executorService.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(mockFuture);

    // Act
    queryHandler.registerQuery(requestPacket, mockCallback, mockTimeoutCallback, timeout, unit);

    // Assert
    assertThat(requestPacket.getRequestId()).isNotNull().hasSize(QueryHandler.REQUEST_ID_LENGTH);
    verify(executorService, times(1))
        .schedule(scheduledTaskCaptor.capture(), delayCaptor.capture(), timeUnitCaptor.capture());
    assertThat(delayCaptor.getValue()).isEqualTo(timeout);
    assertThat(timeUnitCaptor.getValue()).isEqualTo(unit);
  }

  @Test
  @DisplayName("scheduled timeout task should remove callbacks and invoke timeoutCallback")
  void scheduledTimeoutTask_removesCallbacksAndInvokesTimeoutCallback() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("testData");
    long timeout = 1000L;
    TimeUnit unit = TimeUnit.MILLISECONDS;

    when(executorService.schedule(scheduledTaskCaptor.capture(), anyLong(), any(TimeUnit.class)))
        .thenReturn(mockFuture);

    queryHandler.registerQuery(requestPacket, mockCallback, mockTimeoutCallback, timeout, unit);

    // Act - Simulate the scheduled task running (i.e., timeout occurs)
    Runnable timeoutTask = scheduledTaskCaptor.getValue();
    timeoutTask.run();

    // Assert
    verify(mockTimeoutCallback, times(1)).run();
    assertThat(queryHandler.getCallbacks()).doesNotContainKey(requestPacket.getRequestId());
    assertThat(queryHandler.getTimeoutFutures()).doesNotContainKey(requestPacket.getRequestId());
  }

  @Test
  @DisplayName("acceptResponse should invoke callback for registered requestId")
  void acceptResponse_invokesCallbackForRegisteredRequestId() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("testData");
    TestResponsePacket responsePacket = new TestResponsePacket("response");
    long timeout = 1000L;
    TimeUnit unit = TimeUnit.MILLISECONDS;

    when(executorService.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(mockFuture);

    queryHandler.registerQuery(requestPacket, mockCallback, mockTimeoutCallback, timeout, unit);

    // Set the requestId on the response to match the registered one
    responsePacket.setRequestId(requestPacket.getRequestId());

    // Act
    queryHandler.acceptResponse(responsePacket);

    // Assert
    verify(mockCallback, times(1)).accept(eq(responsePacket));
    verify(mockFuture, times(1)).cancel(false);
    verify(mockTimeoutCallback, never()).run();
    assertThat(queryHandler.getCallbacks()).doesNotContainKey(requestPacket.getRequestId());
    assertThat(queryHandler.getTimeoutFutures()).doesNotContainKey(requestPacket.getRequestId());
  }

  @Test
  @DisplayName("acceptResponse should do nothing for unregistered requestId")
  void acceptResponse_doesNothingForUnregisteredRequestId() {
    // Arrange
    TestResponsePacket responsePacket = new TestResponsePacket("response", "unknownId");

    // Act
    queryHandler.acceptResponse(responsePacket);

    // Assert
    verify(mockCallback, never()).accept(any(TestResponsePacket.class));
    verify(executorService, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  @DisplayName("acceptResponse should throw IllegalArgumentException for null requestId")
  void acceptResponse_throwsExceptionForNullRequestId() {
    // Arrange
    TestResponsePacket responsePacket =
        new TestResponsePacket("response"); // RequestId is null by default

    // Act & Assert
    assertThatThrownBy(() -> queryHandler.acceptResponse(responsePacket))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "received %s packet with null request ID", TestResponsePacket.class.getName());

    verify(mockCallback, never()).accept(any(TestResponsePacket.class));
    verify(executorService, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  @DisplayName("acceptResponse should not invoke callback if timeout occurred first")
  void acceptResponse_notInvokedIfTimeoutOccurredFirst() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("testData");
    TestResponsePacket responsePacket = new TestResponsePacket("response");
    long timeout = 1000L;
    TimeUnit unit = TimeUnit.MILLISECONDS;

    when(executorService.schedule(scheduledTaskCaptor.capture(), anyLong(), any(TimeUnit.class)))
        .thenReturn(mockFuture);

    queryHandler.registerQuery(requestPacket, mockCallback, mockTimeoutCallback, timeout, unit);

    // Set the requestId on the response to match the registered one
    responsePacket.setRequestId(requestPacket.getRequestId());

    // Act - Response after timeout
    scheduledTaskCaptor.getValue().run();
    queryHandler.acceptResponse(responsePacket);

    // Assert
    verify(mockCallback, never())
        .accept(any(TestResponsePacket.class)); // Callback should not be called
    verify(mockTimeoutCallback, times(1)).run(); // Timeout should have run
    verify(mockFuture, never()).cancel(anyBoolean()); // Future was not cancelled by acceptResponse
  }

  @Test
  @DisplayName("registerQuery with multiple calls should handle unique request IDs")
  void registerQuery_multipleCalls_handlesUniqueRequestIds() {
    // Arrange
    TestRequestPacket requestPacket1 = new TestRequestPacket("data1");
    TestRequestPacket requestPacket2 = new TestRequestPacket("data2");
    long timeout = 100L;
    TimeUnit unit = TimeUnit.MILLISECONDS;

    ScheduledFuture mockFuture2 = mock();
    when(executorService.schedule(any(Runnable.class), eq(timeout), eq(unit)))
        .thenReturn(mockFuture) // First call
        .thenReturn(mockFuture2); // Second call

    Consumer<TestResponsePacket> callback1 = mock();
    Consumer<TestResponsePacket> callback2 = mock();
    Runnable timeoutCallback1 = mock();
    Runnable timeoutCallback2 = mock();

    // Act
    queryHandler.registerQuery(requestPacket1, callback1, timeoutCallback1, timeout, unit);
    queryHandler.registerQuery(requestPacket2, callback2, timeoutCallback2, timeout, unit);

    // Assert
    assertThat(requestPacket1.getRequestId()).isNotNull();
    assertThat(requestPacket2.getRequestId())
        .isNotNull()
        .isNotEqualTo(requestPacket1.getRequestId());

    // Verify two scheduled tasks
    verify(executorService, times(2))
        .schedule(scheduledTaskCaptor.capture(), eq(timeout), eq(unit));

    // Simulate response for first packet
    TestResponsePacket response1 = new TestResponsePacket("resp1", requestPacket1.getRequestId());
    queryHandler.acceptResponse(response1);
    verify(callback1, times(1)).accept(eq(response1));
    verify(callback2, never()).accept(any());
    verify(mockFuture, times(1)).cancel(false);
    verify(timeoutCallback1, never()).run();

    // Simulate timeout for second packet
    scheduledTaskCaptor.getAllValues().get(1).run(); // Run the second scheduled task
    verify(timeoutCallback2, times(1)).run();
    verify(timeoutCallback1, never()).run(); // Ensure first timeout didn't run
  }
}
