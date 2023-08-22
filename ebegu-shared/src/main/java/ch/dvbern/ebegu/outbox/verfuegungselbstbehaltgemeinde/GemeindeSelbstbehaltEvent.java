package ch.dvbern.ebegu.outbox.verfuegungselbstbehaltgemeinde;

import ch.dvbern.ebegu.outbox.ExportedEvent;
import org.apache.avro.Schema;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class GemeindeSelbstbehaltEvent implements ExportedEvent {

	@Nonnull
	private final String platzId;

	@Nonnull
	private final byte[] gemeindeSelbstbehalt;

	@Nonnull
	private final Schema schema;


	public GemeindeSelbstbehaltEvent(
		@Nonnull String platzId,
		@Nonnull byte[] gemeindeSelbstbehalt,
		@Nonnull Schema schema
	) {
		this.platzId = platzId;
		this.gemeindeSelbstbehalt =  Arrays.copyOf(gemeindeSelbstbehalt, gemeindeSelbstbehalt.length);
		this.schema = schema;
	}

	@Nonnull
	@Override
	public String getAggregateType() {
		return "GemeindeSelbstbehalt";
	}

	@Nonnull
	@Override
	public String getAggregateId() {
		return platzId;
	}

	@Nonnull
	@Override
	public String getType() {
		return "GemeindeSelbstbehaltChanged";
	}

	@Nonnull
	@Override
	public byte[] getPayload() {
		return Arrays.copyOf(gemeindeSelbstbehalt, gemeindeSelbstbehalt.length);
	}

	@Nonnull
	@Override
	public Schema getSchema() {
		return schema;
	}
}
