package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.UebersichtVersendeteMails;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(UebersichtVersendeteMailsService.class)
public class UebersichtVersendeteMailsServiceBean extends AbstractBaseService implements UebersichtVersendeteMailsService {
	@Inject
	private Persistence persistence;

	@Override
	@Nonnull
	public UebersichtVersendeteMails saveUebersichtVersendeteMails(@Nonnull UebersichtVersendeteMails uebersichtVersendeteMails) {
		return persistence.persist(uebersichtVersendeteMails);
	}
}
