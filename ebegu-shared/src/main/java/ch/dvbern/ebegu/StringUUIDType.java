package ch.dvbern.ebegu;

import java.util.UUID;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;

/**
 * A type mapping {@link java.sql.Types#BINARY} and {@link UUID}
 *
 */
public class StringUUIDType extends AbstractSingleColumnStandardBasicType<String> {

	public static final StringUUIDType INSTANCE = new StringUUIDType();

	private static final long serialVersionUID = 8299338720540799781L;

	public StringUUIDType() {
		super( BinaryTypeDescriptor.INSTANCE, StringUUIDTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "string-uuid-binary";
	}

	@Override
	protected boolean registerUnderJavaType() {
		return true;
	}
}
