package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import ch.dvbern.ebegu.entities.VersendeteMail;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(VersendeteMailsService.class)
public class VersendeteMailsServiceBean extends AbstractBaseService implements VersendeteMailsService {
	@Inject
	private Persistence persistence;

	@Override
	@Nonnull
	public VersendeteMail saveVersendeteMail(@Nonnull VersendeteMail versendeteMail) {
		return persistence.persist(versendeteMail);
	}

	@Nonnull
	@Override
	public Collection<VersendeteMail> getAll() {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VersendeteMail> query = builder.createQuery(VersendeteMail.class);
		query.from(VersendeteMail.class);

		return persistence.getEntityManager().createQuery(query)
			.getResultList();
	}
}
