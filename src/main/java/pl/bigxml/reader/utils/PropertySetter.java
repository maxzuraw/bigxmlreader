package pl.bigxml.reader.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.bigxml.reader.domain.Payment;
import pl.bigxml.reader.exceptions.PropertySetterException;

import java.lang.reflect.Method;

import static pl.bigxml.reader.utils.SnakeToCamelCase.toCamelCase;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertySetter {

    public static void setProperty(Payment target, String propertyName, Object value) {
        try {
            String camelCasePropertyName = toCamelCase(propertyName);
            String setterName = "set" + Character.toUpperCase(camelCasePropertyName.charAt(0)) + camelCasePropertyName.substring(1);
            Method setter = null;
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                    setter = method;
                    break;
                }
            }
            if (setter == null) {
                throw new NoSuchMethodException("Setter for property '" + camelCasePropertyName + "' not found.");
            }
            setter.invoke(target, value);
        } catch (Exception e) {
            // log error silently, but try to map as much as possible
            log.error("Failed to set property '" + propertyName + "' on " + target, e);
        }
    }
}
