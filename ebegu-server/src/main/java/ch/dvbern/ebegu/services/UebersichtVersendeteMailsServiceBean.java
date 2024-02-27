package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

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

	@Nonnull
	@Override
	public Collection<UebersichtVersendeteMails> getAll() {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<UebersichtVersendeteMails> query = builder.createQuery(UebersichtVersendeteMails.class);
		query.from(UebersichtVersendeteMails.class);

		return persistence.getEntityManager().createQuery(query)
			.getResultList();
	}
}
