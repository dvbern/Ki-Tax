/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer_;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse_;
import ch.dvbern.ebegu.entities.Gesuchsteller_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer Adresse
 */
@Stateless
@Local(GesuchstellerAdresseService.class)
public class GesuchstellerAdresseServiceBean extends AbstractBaseService implements GesuchstellerAdresseService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	public GesuchstellerAdresseContainer createAdresse(@Nonnull GesuchstellerAdresseContainer gesuchstellerAdresse) {
		Objects.requireNonNull(gesuchstellerAdresse);
		return persistence.persist(gesuchstellerAdresse);
	}

	@Nonnull
	@Override
	public GesuchstellerAdresseContainer updateAdresse(@Nonnull GesuchstellerAdresseContainer gesuchstellerAdresse) {
		Objects.requireNonNull(gesuchstellerAdresse);
		return persistence.merge(gesuchstellerAdresse);
	}

	@Nonnull
	@Override
	public Optional<GesuchstellerAdresseContainer> findAdresse(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		GesuchstellerAdresseContainer a = persistence.find(GesuchstellerAdresseContainer.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public Collection<GesuchstellerAdresseContainer> getAllAdressen() {
		return new ArrayList<>(criteriaQueryHelper.getAll(GesuchstellerAdresseContainer.class));
	}

	@Override
	public void removeAdresse(@Nonnull GesuchstellerAdresseContainer gesuchstellerAdresse) {
		Objects.requireNonNull(gesuchstellerAdresse);
		GesuchstellerAdresseContainer adresseToRemove = findAdresse(gesuchstellerAdresse.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("removeAdresse", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchstellerAdresse));
		persistence.remove(adresseToRemove);
	}

	/**
	 * Erstellt ein query gegen die Adresse mit den gegebenen parametern
	 *
	 * @param gesuchstellerID gesuchsteller fuer die Adressen gesucht werden
	 * @param typ typ der Adresse der gesucht wird
	 * @param maximalDatumVon datum ab dem gesucht wird (incl)
	 * @param minimalDatumBis datum bis zu dem gesucht wird (incl)
	 */
	private TypedQuery<GesuchstellerAdresseContainer> getAdresseQuery(@Nonnull String gesuchstellerID, @Nonnull AdresseTyp typ, @Nullable LocalDate maximalDatumVon, @Nullable LocalDate minimalDatumBis) {
		Objects.requireNonNull(gesuchstellerID);
		Objects.requireNonNull(typ);
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		ParameterExpression<String> gesuchstellerIdParam = cb.parameter(String.class, "gesuchstellerID");
		ParameterExpression<AdresseTyp> typParam = cb.parameter(AdresseTyp.class, "adresseTyp");
		ParameterExpression<LocalDate> gueltigVonParam = cb.parameter(LocalDate.class, "gueltigVon");
		ParameterExpression<LocalDate> gueltigBisParam = cb.parameter(LocalDate.class, "gueltigBis");

		CriteriaQuery<GesuchstellerAdresseContainer> query = cb.createQuery(GesuchstellerAdresseContainer.class);
		Root<GesuchstellerAdresseContainer> root = query.from(GesuchstellerAdresseContainer.class);
		Predicate gesuchstellerPred = cb.equal(root.get(GesuchstellerAdresseContainer_.gesuchstellerContainer).get(Gesuchsteller_.id), gesuchstellerIdParam);

		Predicate typePredicate;
		if (AdresseTyp.KORRESPONDENZADRESSE == typ || AdresseTyp.RECHNUNGSADRESSE == typ) {
			final Join<GesuchstellerAdresseContainer, GesuchstellerAdresse> joinGS = root.join(GesuchstellerAdresseContainer_.gesuchstellerAdresseGS, JoinType.LEFT);
			final Join<GesuchstellerAdresseContainer, GesuchstellerAdresse> joinJA = root.join(GesuchstellerAdresseContainer_.gesuchstellerAdresseJA, JoinType.LEFT);
			typePredicate = cb.or(cb.equal(joinGS.get(GesuchstellerAdresse_.adresseTyp), typParam),
				cb.equal(joinJA.get(GesuchstellerAdresse_.adresseTyp), typParam));
		} else {
			typePredicate = cb.equal(root.get(GesuchstellerAdresseContainer_.gesuchstellerAdresseJA).get(GesuchstellerAdresse_.adresseTyp), typParam);
		}
		List<Predicate> predicatesToUse = new ArrayList<>();

		predicatesToUse.add(gesuchstellerPred);
		predicatesToUse.add(typePredicate);
		//noinspection VariableNotUsedInsideIf
		if (maximalDatumVon != null) {
			Predicate datumVonLessThanPred = cb.lessThanOrEqualTo(root.get(GesuchstellerAdresseContainer_.gesuchstellerAdresseJA)
				.get(GesuchstellerAdresse_.gueltigkeit).get(DateRange_.gueltigAb), gueltigVonParam);
			predicatesToUse.add(datumVonLessThanPred);

		}
		//noinspection VariableNotUsedInsideIf
		if (minimalDatumBis != null) {
			Predicate datumBisGreaterThanPRed = cb.greaterThanOrEqualTo(root.get(GesuchstellerAdresseContainer_.gesuchstellerAdresseJA)
				.get(GesuchstellerAdresse_.gueltigkeit).get(DateRange_.gueltigBis), gueltigBisParam);
			predicatesToUse.add(datumBisGreaterThanPRed);

		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse));

		TypedQuery<GesuchstellerAdresseContainer> typedQuery = persistence.getEntityManager().createQuery(query);

		typedQuery.setParameter("gesuchstellerID", gesuchstellerID);
		typedQuery.setParameter("adresseTyp", typ);
		if (maximalDatumVon != null) {
			typedQuery.setParameter("gueltigVon", maximalDatumVon);
		}
		if (minimalDatumBis != null) {
			typedQuery.setParameter("gueltigBis", minimalDatumBis);
		}
		return typedQuery;
	}

	@Nonnull
	@Override
	public Optional<GesuchstellerAdresseContainer> getKorrespondenzAdr(@Nonnull String gesuchstellerID) {
		Objects.requireNonNull(gesuchstellerID);
		List<GesuchstellerAdresseContainer> results = getAdresseQuery(gesuchstellerID, AdresseTyp.KORRESPONDENZADRESSE, null, null).getResultList();
		if (results.isEmpty()) {
			return Optional.empty();
		}
		if (results.size() > 1) {
			throw new EbeguRuntimeException("getKorrespondenzAdr", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS, gesuchstellerID);
		}
		return Optional.of(results.get(0));
	}

	@Nonnull
	@Override
	public Optional<GesuchstellerAdresseContainer> getRechnungsAdr(@Nonnull String gesuchstellerID) {
		Objects.requireNonNull(gesuchstellerID);
		List<GesuchstellerAdresseContainer> results = getAdresseQuery(gesuchstellerID, AdresseTyp.RECHNUNGSADRESSE, null, null).getResultList();
		if (results.isEmpty()) {
			return Optional.empty();
		}
		if (results.size() > 1) {
			throw new EbeguRuntimeException("getRechnungsAdr", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS, gesuchstellerID);
		}
		return Optional.of(results.get(0));
	}
}
