package ru.stuff.chess.entity.jsonutils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by mark on 27.12.14.
 */
public class SimpleJsonSerializer extends JsonSerializer<Object> {
    private static final Logger log = Logger.getLogger(SimpleJsonSerializer.class);

    @Override
    public void serialize(Object targetObj,
                          JsonGenerator jgen,
                          SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        Field[] fields = targetObj.getClass().getFields();
        for (Field field : fields) {
            try {

                if (!field.isAnnotationPresent(OptionalField.class)
                        || field.get(targetObj) != null) { //annotation present here
                    jgen.writeObjectField(field.getName(), field.get(targetObj));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (!targetObj.getClass().isAnnotationPresent(WithoutEventType.class))
            jgen.writeObjectField("event_type", targetObj.getClass().getSimpleName());
        jgen.writeEndObject();
    }
}
