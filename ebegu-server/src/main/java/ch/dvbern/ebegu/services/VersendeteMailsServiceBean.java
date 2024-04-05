package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import ch.dvbern.ebegu.entities.VersendeteMails;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(VersendeteMailsService.class)
public class VersendeteMailsServiceBean extends AbstractBaseService implements VersendeteMailsService {
	@Inject
	private Persistence persistence;

	@Override
	@Nonnull
	public VersendeteMails saveVersendeteMails(@Nonnull VersendeteMails VersendeteMails) {
		return persistence.persist(VersendeteMails);
	}

	@Nonnull
	@Override
	public Collection<VersendeteMails> getAll() {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VersendeteMails> query = builder.createQuery(VersendeteMails.class);
		query.from(VersendeteMails.class);

		return persistence.getEntityManager().createQuery(query)
			.getResultList();
	}
}
