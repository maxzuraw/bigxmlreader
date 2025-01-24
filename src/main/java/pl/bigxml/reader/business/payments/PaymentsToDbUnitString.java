package pl.bigxml.reader.business.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.bigxml.reader.business.MappingsFileReader;
import pl.bigxml.reader.domain.ConfigurationMaps;
import pl.bigxml.reader.domain.MappingsConfig;
import pl.bigxml.reader.domain.Payment;
import pl.bigxml.reader.utils.PropertyGetter;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentsToDbUnitString {

    private final static int START_ID = 1000;
    private final MappingsFileReader mappingsFileReader;

    public void printAsDBUnitPayment(List<Payment> payment) {
        List<MappingsConfig> paymentsConfig = mappingsFileReader.readPaymentMappings();
        ConfigurationMaps maps = new ConfigurationMaps(paymentsConfig);
        int startIdx = START_ID;
        StringBuilder sb = new StringBuilder();
        for(Payment p : payment) {
            sb.append(printSingle(maps, startIdx, p));
            startIdx++;
        }
        log.info("\n{}\n", sb);
    }

    private String printSingle(ConfigurationMaps maps, int id, Payment payment) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ses_payment_default id=\"").append(id).append("\"");
        Map<String, MappingsConfig> configurationPerTargetField = maps.getConfigurationPerTargetField();
        for (Map.Entry<String, MappingsConfig> entry : configurationPerTargetField.entrySet()) {
            Object value = null;
            value = PropertyGetter.getProperty(payment, entry.getKey());
            sb.append(" ").append(entry.getKey()).append("=\"");
            assert value != null;
            sb.append(value).append("\"");
        }
        sb.append("/>\n");
        return sb.toString();
    }
}
