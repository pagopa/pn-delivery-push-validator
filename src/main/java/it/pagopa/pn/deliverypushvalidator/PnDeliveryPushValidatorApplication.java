package it.pagopa.pn.deliverypushvalidator;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PnDeliveryPushValidatorApplication {
    public static void main(String[] args) {
        buildSpringApplicationWithListener().run(args);
    }
    static SpringApplication buildSpringApplicationWithListener() {
        SpringApplication app = new SpringApplication(PnDeliveryPushValidatorApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        return app;
    }
}