package ch.dvbern.ebegu.services;

import java.util.List;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetail_;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Local(LastenausgleichDetailService.class)
public class LastenausgleichDetailServiceBean extends AbstractBaseService implements LastenausgleichDetailService {

	private static final Logger LOG = LoggerFactory.getLogger(LastenausgleichDetailServiceBean.class);


	@Inject
	private Persistence persistence;

	@Nonnull
	@Override
	public List<LastenausgleichDetail> getAllLastenausgleichDetailsForGemeinde(@Nonnull Gemeinde gemeinde) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichDetail> query = cb.createQuery(LastenausgleichDetail.class);
		Root<LastenausgleichDetail> root = query.from(LastenausgleichDetail.class);

		Predicate namePredicate = root.get(LastenausgleichDetail_.gemeinde).get(AbstractEntity_.id).in(gemeinde.getId());

		query.where(namePredicate);
		return persistence.getCriteriaResults(query);

	}

}
