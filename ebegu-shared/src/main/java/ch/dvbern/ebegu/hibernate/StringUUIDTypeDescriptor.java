package ch.dvbern.ebegu.hibernate;

import java.util.UUID;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor.PassThroughTransformer;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor.ToBytesTransformer;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor.ToStringTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This descriptor defines how to map StrinUUIDType to the Database
 */
public class StringUUIDTypeDescriptor extends AbstractTypeDescriptor<String> {

	private static final long serialVersionUID = -394259331570702578L;
	public static final StringUUIDTypeDescriptor INSTANCE = new StringUUIDTypeDescriptor();

	private static final Logger LOG = LoggerFactory.getLogger(StringUUIDTypeDescriptor.class);

	public StringUUIDTypeDescriptor() {
		super(String.class);
	}

	@Override
	public String toString(String stringInput) {
		return stringInput;
	}

	@Override
	public String fromString(String uuidString) {
		return uuidString;
	}

	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(String uuidAsString, Class<X> type, WrapperOptions options) {

		if (uuidAsString == null) {
			return null;
		}
		final UUID uuid;
		try {

			uuid = UUID.fromString(uuidAsString);
		} catch (IllegalArgumentException ex) {
			LOG.error("Could not map value as uuid. Check that it is a valid uuid. Otherwise mapping");
			throw ex;
		}

		if (UUID.class.isAssignableFrom(type)) {
			return (X) PassThroughTransformer.INSTANCE.transform(uuid);
		}
		if (String.class.isAssignableFrom(type)) {
			return (X) ToStringTransformer.INSTANCE.transform(uuid);
		}
		if (byte[].class.isAssignableFrom(type)) {
			return (X) ToBytesTransformer.INSTANCE.transform(uuid);
		}
		throw unknownUnwrap(type);

	}

	public <X> String wrap(X value, WrapperOptions options) {

		if (value == null) {
			return null;
		}
		if (UUID.class.isInstance(value)) {
			final UUID parsed = PassThroughTransformer.INSTANCE.parse(value);
			return parsed.toString();
		}
		if (String.class.isInstance(value)) {
			final UUID parsed = ToStringTransformer.INSTANCE.parse(value);
			return parsed.toString();
		}
		if (byte[].class.isInstance(value)) {
			final UUID parsed = ToBytesTransformer.INSTANCE.parse(value);
			return parsed.toString();
		}
		throw unknownWrap(value.getClass());

	}
}
