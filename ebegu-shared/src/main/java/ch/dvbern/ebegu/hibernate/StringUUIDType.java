package ch.dvbern.ebegu.hibernate;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;

/**
 * A type mapping {@link java.sql.Types#BINARY} and to a String repesenting a UUID
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
