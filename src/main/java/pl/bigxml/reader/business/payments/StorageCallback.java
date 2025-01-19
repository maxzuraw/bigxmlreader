package pl.bigxml.reader.business.payments;

import pl.bigxml.reader.domain.Payment;

import java.util.List;
import java.util.function.Function;

public class StorageCallback implements Function<List<Payment>, Void> {

    @Override
    public Void apply(List<Payment> payments) {
        return null;
    }
}
