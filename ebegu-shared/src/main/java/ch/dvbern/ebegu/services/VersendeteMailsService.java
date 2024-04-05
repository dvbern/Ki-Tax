package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.VersendeteMails;

public interface VersendeteMailsService {
	@Nonnull
	VersendeteMails saveVersendeteMails(@Valid @Nonnull VersendeteMails VersendeteMails);

	@Nonnull
	Collection<VersendeteMails> getAll();
}
