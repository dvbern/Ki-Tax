package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.VersendeteMail;
import ch.dvbern.ebegu.entities.VersendeteMail_;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(VersendeteMailsService.class)
public class VersendeteMailsServiceBean extends AbstractBaseService implements VersendeteMailsService {
	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

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
		final Root<VersendeteMail> root = query.from(VersendeteMail.class);
		Predicate mandantPredicate = builder.equal(root.get(VersendeteMail_.MANDANT_IDENTIFIER), principalBean.getMandant().getMandantIdentifier());
		query.where(mandantPredicate);

		return persistence.getEntityManager().createQuery(query)
			.getResultList();
	}
}
