package ch.dvbern.ebegu.outbox.verfuegungselbstbehaltgemeinde;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.verfuegungselbstbehaltgemeinde.GemeindeSelbstbehaltEventDTO;


import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class GemeindeSelbstbehaltEventConverter {

	@Nonnull
	public GemeindeSelbstbehaltEvent of(@Nonnull AbstractPlatz platz, boolean keinSelbstbehaltFuerGemeinde) {
		GemeindeSelbstbehaltEventDTO dto = toGemeindeSelbstbehaltEventDTO(platz.getBGNummer(), keinSelbstbehaltFuerGemeinde);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new GemeindeSelbstbehaltEvent(UUID.randomUUID().toString(), payload, dto.getSchema());
	}

	@Nonnull
	private GemeindeSelbstbehaltEventDTO toGemeindeSelbstbehaltEventDTO(
		String refNr,
		boolean keinSelbstbehaltFuerGemeinde) {
		//noinspection ConstantConditions
		return GemeindeSelbstbehaltEventDTO.newBuilder()
			.setKeinSelbstbehaltDurchGemeinde(keinSelbstbehaltFuerGemeinde)
			.setRefnr(refNr)
			.build();
	}

}
