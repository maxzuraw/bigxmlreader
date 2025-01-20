package pl.bigxml.reader.business.payments;

import lombok.extern.slf4j.Slf4j;
import pl.bigxml.reader.domain.Payment;

import java.util.List;
import java.util.function.Function;

@Slf4j
public class StorageCallback implements Function<List<Payment>, Void> {

    @Override
    public Void apply(List<Payment> payments) {
        log.info("Received {} of payments to store", payments.size());
        return null;
    }
}
