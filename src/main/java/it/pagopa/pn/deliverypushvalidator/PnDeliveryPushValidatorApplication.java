package it.pagopa.pn.deliverypushvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PnDeliveryPushValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PnDeliveryPushValidatorApplication.class, args);
    }
}