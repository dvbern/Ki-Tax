package ch.dvbern.ebegu;

import java.util.UUID;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor.PassThroughTransformer;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor.ToBytesTransformer;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor.ToStringTransformer;

public class StringUUIDTypeDescriptor extends AbstractTypeDescriptor<String> {


	public static final StringUUIDTypeDescriptor INSTANCE = new StringUUIDTypeDescriptor();

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
		try{

			 uuid= UUID.fromString(uuidAsString);
		}catch (IllegalArgumentException ex){

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
