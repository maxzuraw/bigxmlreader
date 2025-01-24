package pl.bigxml.reader.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.bigxml.reader.domain.Payment;

import java.lang.reflect.Method;

import static pl.bigxml.reader.utils.SnakeToCamelCase.toCamelCase;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyGetter {

    public static Object getProperty(Payment target, String propertyName) {
        try {
            String camelCasePropertyName = toCamelCase(propertyName);
            String getterName = "get" + Character.toUpperCase(camelCasePropertyName.charAt(0)) + camelCasePropertyName.substring(1);
            Method getter = null;
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(getterName) && method.getParameterCount() == 0) {
                    getter = method;
                    break;
                }
            }
            if (getter == null) {
                getterName = "is" + Character.toUpperCase(camelCasePropertyName.charAt(0)) + camelCasePropertyName.substring(1);
                for (Method method : target.getClass().getMethods()) {
                    if (method.getName().equals(getterName) && method.getParameterCount() == 0) {
                        getter = method;
                        break;
                    }
                }
            }
            if (getter == null) {
                throw new NoSuchMethodException("Getter for property '" + camelCasePropertyName + "' not found.");
            }
            return getter.invoke(target);
        } catch (Exception e) {
            log.error("Failed to get property '" + propertyName + "' from " + target, e);
            return null;
        }
    }
}
