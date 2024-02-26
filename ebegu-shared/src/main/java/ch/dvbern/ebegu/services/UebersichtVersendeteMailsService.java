package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.UebersichtVersendeteMails;

public interface UebersichtVersendeteMailsService {
	@Nonnull
	UebersichtVersendeteMails saveUebersichtVersendeteMails(@Valid @Nonnull UebersichtVersendeteMails uebersichtVersendeteMails);
}
