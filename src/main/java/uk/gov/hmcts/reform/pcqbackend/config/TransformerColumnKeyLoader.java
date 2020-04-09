package uk.gov.hmcts.reform.pcqbackend.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;

@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
@Slf4j
@Getter
@Setter
public class TransformerColumnKeyLoader implements ApplicationListener<ApplicationPreparedEvent> {

    public static final String KEY_ANNOTATION_PROPERTY = "${encryption.key}";

    @Autowired
    private Environment environment;

    private String dbEncryptionKey;

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        log.info("TransformerColumnKeyLoader invoked");
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        Iterator<PropertySource<?>> sourcesIterator = mutablePropertySources.iterator();
        while (sourcesIterator.hasNext()) {
            PropertySource propertySource = sourcesIterator.next();
            if (propertySource.containsProperty("security.db.backend-encryption-key")
                || propertySource.containsProperty("backend-encryption-key")) {
                log.info("TransformerColumnKeyLoader Properties Found {}", propertySource.getName());
                this.dbEncryptionKey = environment.getProperty("security.db.backend-encryption-key");
                addKey(ProtectedCharacteristics.class, "partyId");
            }
        }
    }

    private void addKey(Class<?> clazz, String columnName) {
        try {
            Field field = clazz.getDeclaredField(columnName);

            ColumnTransformer columnTransformer = field.getDeclaredAnnotation(ColumnTransformer.class);
            updateAnnotationValue(columnTransformer, "read");
            updateAnnotationValue(columnTransformer, "write");

        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(
                String.format("Encryption key cannot be loaded into %s,%s", clazz.getName(), columnName), e);
        }
    }

    @SuppressWarnings({"unchecked", "PMD.CyclomaticComplexity"})
    private void updateAnnotationValue(Annotation annotation, String annotationProperty) {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field memberValuesField;
        try {
            memberValuesField = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        memberValuesField.setAccessible(true);
        Map<String, Object> memberValues;
        try {
            memberValues = (Map<String, Object>) memberValuesField.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(annotationProperty);
        if (oldValue == null || oldValue.getClass() != String.class) {
            throw new IllegalArgumentException(String.format(
                "Annotation value should be String. Current value is of type: %s", oldValue.getClass().getName()));
        }

        String oldValueString = oldValue.toString();
        if (oldValueString.contains(TransformerColumnKeyLoader.KEY_ANNOTATION_PROPERTY)) {
            log.info("Replaced the values with key {}", dbEncryptionKey);
            if (dbEncryptionKey != null) {
                String newValueString = oldValueString.replace(
                    TransformerColumnKeyLoader.KEY_ANNOTATION_PROPERTY, dbEncryptionKey);
                memberValues.put(annotationProperty, newValueString);
            }
        }


    }


}
