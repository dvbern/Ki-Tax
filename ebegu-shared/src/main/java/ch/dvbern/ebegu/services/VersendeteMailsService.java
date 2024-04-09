package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.VersendeteMail;

public interface VersendeteMailsService {
	@Nonnull
	VersendeteMail saveVersendeteMail(@Valid @Nonnull VersendeteMail versendeteMail);

	@Nonnull
	Collection<VersendeteMail> getAll();
}
