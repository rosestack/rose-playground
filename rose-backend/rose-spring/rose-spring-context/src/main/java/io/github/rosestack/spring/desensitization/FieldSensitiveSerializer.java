package io.github.rosestack.spring.desensitization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.github.rosestack.core.util.MaskUtils;
import io.github.rosestack.spring.annotation.FieldSensitive;
import io.github.rosestack.spring.expression.SpringExpressionResolver;

import java.io.IOException;
import java.util.Objects;

/**
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 0.0.1
 */
public class FieldSensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

	private FieldSensitive fieldSensitive;

	public FieldSensitiveSerializer(FieldSensitive fieldSensitive) {
		this.fieldSensitive = fieldSensitive;
	}

	public FieldSensitiveSerializer() {
	}

	private String handler(FieldSensitive fieldSensitive, String origin) {
		Object disable = SpringExpressionResolver.getInstance().resolve(fieldSensitive.expression());
		if (Boolean.TRUE.equals(disable)) {
			return origin;
		}

		return MaskUtils.mask(origin, fieldSensitive.type());
	}

	@Override
	public void serialize(
		final String origin, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider)
		throws IOException {
		jsonGenerator.writeString(handler(fieldSensitive, origin));
	}

	@Override
	public JsonSerializer<?> createContextual(
		final SerializerProvider serializerProvider, final BeanProperty beanProperty) throws JsonMappingException {
		FieldSensitive annotation = beanProperty.getAnnotation(FieldSensitive.class);
		if (Objects.nonNull(annotation)
			&& Objects.equals(String.class, beanProperty.getType().getRawClass())) {
			return new FieldSensitiveSerializer(annotation);
		}
		return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
	}
}
