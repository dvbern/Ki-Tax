package ch.dvbern.ebegu.rechner;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import org.apache.commons.lang.NotImplementedException;

import javax.annotation.Nonnull;

public class MittagstischRechner extends AbstractRechner {
	@Override
	public void calculate(@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt, @Nonnull BGRechnerParameterDTO parameterDTO) {
		throw new NotImplementedException();
	}
}
