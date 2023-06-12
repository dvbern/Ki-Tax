package ch.dvbern.ebegu.enums;

import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;

import javax.annotation.Nullable;
import java.util.Objects;

public enum MitteilungTyp {
	BETREUUNGSMITTEILUNG,
	NEUE_VERANLAGUNGS_MITTEILUNG;

	@Nullable
	public static MitteilungTyp getMitteilungTypByClass(Class<? extends Mitteilung> clazz) {
		if (Objects.equals(clazz, Betreuungsmitteilung.class)) {
			return BETREUUNGSMITTEILUNG;
		}

		if (Objects.equals(clazz, NeueVeranlagungsMitteilung.class)) {
			return NEUE_VERANLAGUNGS_MITTEILUNG;
		}

		return null;
	}
}
