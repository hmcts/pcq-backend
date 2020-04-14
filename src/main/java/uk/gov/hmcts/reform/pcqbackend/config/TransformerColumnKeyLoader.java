package uk.gov.hmcts.reform.pcqbackend.config;

import com.gilecode.reflection.ReflectionAccessUtils;
import com.gilecode.reflection.ReflectionAccessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;

@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
@Slf4j
@Getter
public class TransformerColumnKeyLoader implements ApplicationListener<ApplicationPreparedEvent> {

    public static final String KEY_ANNOTATION_PROPERTY = "${encryption.key}";

    private static final String NO_PROPERTY = "No";
    private static final String READ_PROPERTY = "read";

    private String dbEncryptionKey;

    private String encryptionDisabled;

    private Class<?> clazz;

    public TransformerColumnKeyLoader() {
        super();
    }

    public TransformerColumnKeyLoader(Class<?> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        log.info("TransformerColumnKeyLoader invoked");
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        for (PropertySource propertySource : mutablePropertySources) {
            if (propertySource.containsProperty("security.db.backend-encryption-key")
                || propertySource.containsProperty("backend-encryption-key")) {
                log.info("TransformerColumnKeyLoader Properties Found {}", propertySource.getName());
                this.dbEncryptionKey = environment.getProperty("security.db.backend-encryption-key");
                this.encryptionDisabled = environment.getProperty("security.db.encryption-disabled");
                if (getClazz() == null) {
                    addKey(ProtectedCharacteristics.class);
                } else {
                    addKey(clazz);
                }
            }
        }
    }

    private void addKey(Class<?> clazz) {
        try {
            Field field = clazz.getDeclaredField("partyId");

            ColumnTransformer columnTransformer = field.getDeclaredAnnotation(ColumnTransformer.class);
            updateAnnotationValue(columnTransformer, "read");
            updateAnnotationValue(columnTransformer, "write");

        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(
                String.format("Encryption key cannot be loaded into %s,%s", clazz.getName(),
                              "partyId"), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void updateAnnotationValue(Annotation annotation, String annotationProperty) {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field memberValuesField;

        try {

            memberValuesField = handler.getClass().getDeclaredField("memberValues");

        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }

        ReflectionAccessor accessor = ReflectionAccessUtils.getReflectionAccessor();
        accessor.makeAccessible(memberValuesField);

        try {

            Map<String, Object> memberValues = (Map<String, Object>) memberValuesField.get(handler);
            String oldValueString = memberValues.get(annotationProperty).toString();
            if (oldValueString.contains(TransformerColumnKeyLoader.KEY_ANNOTATION_PROPERTY)) {
                if (dbEncryptionKey != null && NO_PROPERTY.equals(encryptionDisabled)) {
                    log.info("Replaced the values with key {}", dbEncryptionKey);
                    String newValueString = oldValueString.replace(
                        TransformerColumnKeyLoader.KEY_ANNOTATION_PROPERTY, dbEncryptionKey);
                    memberValues.put(annotationProperty, newValueString);
                } else {
                    log.info("Removed the encryption");
                    if (READ_PROPERTY.equals(annotationProperty)) {
                        memberValues.put(annotationProperty, "party_id");
                    } else {
                        memberValues.put(annotationProperty, "?");
                    }
                }
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

    }


}
