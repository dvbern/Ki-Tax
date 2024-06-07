package ch.dvbern.ebegu.outbox.verfuegungselbstbehaltgemeinde;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.verfuegungselbstbehaltgemeinde.GemeindeSelbstbehaltEventDTO;

@ApplicationScoped
public class GemeindeSelbstbehaltEventConverter {

	@Nonnull
	public GemeindeSelbstbehaltEvent of(@Nonnull AbstractPlatz platz, boolean keinSelbstbehaltFuerGemeinde) {
		String referenzNummer = platz.getReferenzNummer();
		GemeindeSelbstbehaltEventDTO dto = toGemeindeSelbstbehaltEventDTO(referenzNummer, keinSelbstbehaltFuerGemeinde);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new GemeindeSelbstbehaltEvent(UUID.randomUUID().toString(), payload, dto.getSchema());
	}

	@Nonnull
	private GemeindeSelbstbehaltEventDTO toGemeindeSelbstbehaltEventDTO(
		String referenzNummer,
		boolean keinSelbstbehaltFuerGemeinde
	) {
		return GemeindeSelbstbehaltEventDTO.newBuilder()
			.setKeinSelbstbehaltDurchGemeinde(keinSelbstbehaltFuerGemeinde)
			.setRefnr(referenzNummer)
			.build();
	}

}
