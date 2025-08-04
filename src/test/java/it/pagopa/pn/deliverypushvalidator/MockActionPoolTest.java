package it.pagopa.pn.deliverypushvalidator;


import it.pagopa.pn.deliverypushvalidator.middleware.queue.producer.abstractions.actionspool.ActionsPool;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class MockActionPoolTest {
    @MockitoBean
    private ActionsPool actionsPool;
}
