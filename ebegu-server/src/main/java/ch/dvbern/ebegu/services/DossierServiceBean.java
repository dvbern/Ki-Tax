/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherBGValidationGroup;
import ch.dvbern.ebegu.validationgroups.ChangeVerantwortlicherTSValidationGroup;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer Dossier
 */
@Stateless
@Local(DossierService.class)
public class DossierServiceBean extends AbstractBaseService implements DossierService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private FallService fallService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private GemeindeService gemeindeService;

	@Nonnull
	@Override
	public Optional<Dossier> findDossier(@Nonnull String id) {
		return findDossier(id, true);
	}

	@Nonnull
	@Override
	public Optional<Dossier> findDossier(@Nonnull String id, boolean doAuthCheck) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Dossier dossier = persistence.find(Dossier.class, id);
		if (doAuthCheck && dossier != null) {
			authorizer.checkReadAuthorizationDossier(dossier);
		}
		return Optional.ofNullable(dossier);
	}

	@Nonnull
	@Override
	public Collection<Dossier> findDossiersByFall(@Nonnull String fallId) {
		final Fall fall =
			fallService.findFall(fallId).orElseThrow(() -> new EbeguEntityNotFoundException("findDossiersByFall",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, fallId));

		Collection<Dossier> dossiers = criteriaQueryHelper.getEntitiesByAttribute(Dossier.class, fall, Dossier_.fall);

		return dossiers.stream()
			.filter(authorizer::isReadCompletelyAuthorizedDossier).
				collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public Collection<Dossier> findDossiersByGemeinde(@Nonnull String gemeindeId) {
		final Gemeinde gemeinde =
			gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException("findDossiersByGemeinde",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));

		Collection<Dossier> dossiers = criteriaQueryHelper.getEntitiesByAttribute(Dossier.class, gemeinde, Dossier_.gemeinde);

		return dossiers.stream()
			.filter(authorizer::isReadCompletelyAuthorizedDossier).
				collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public Optional<Dossier> findDossierByGemeindeAndFall(@Nonnull String gemeindeId, @Nonnull String fallId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Dossier> query = cb.createQuery(Dossier.class);

		Root<Dossier> root = query.from(Dossier.class);

		Predicate predicateFall = cb.equal(root.get(Dossier_.fall).get(AbstractEntity_.id), fallId);
		Predicate predicateGemeinde = cb.equal(root.get(Dossier_.gemeinde).get(AbstractEntity_.id), gemeindeId);

		query.where(predicateFall, predicateGemeinde);
		final Dossier criteriaSingleResult = persistence.getCriteriaSingleResult(query);
		return Optional.ofNullable(criteriaSingleResult);
	}

	@Nonnull
	@Override
	public Dossier saveDossier(@Nonnull Dossier dossier) {
		Objects.requireNonNull(dossier);
		authorizer.checkWriteAuthorizationDossier(dossier);
		return persistence.merge(dossier);
	}

	@Override
	public void removeDossier(@Nonnull String dossierId, @Nonnull GesuchDeletionCause deletionCause) {
		Objects.requireNonNull(dossierId);

		final Optional<Dossier> optDossier = findDossier(dossierId);
		final Dossier dossierToRemove = optDossier.orElseThrow(()
			-> new EbeguEntityNotFoundException("removeDossier", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dossierId));

		authorizer.checkWriteAuthorizationDossier(dossierToRemove);

		gesuchService.getAllGesuchIDsForDossier(dossierToRemove.getId())
			.forEach(gesuch -> gesuchService.removeGesuch(gesuch, deletionCause));

		persistence.remove(dossierToRemove);
	}

	@Nonnull
	@Override
	public Collection<Dossier> getAllDossiers(boolean doAuthCheck) {
		List<Dossier> dossiers = new ArrayList<>(criteriaQueryHelper.getAll(Dossier.class));
		if (doAuthCheck) {
			authorizer.checkReadAuthorizationDossiers(dossiers);
		}
		return dossiers;
	}

	@Nonnull
	@Override
	public Dossier getOrCreateDossierAndFallForCurrentUserAsBesitzer(@Nonnull String gemeindeId) {
		Optional<Fall> fallOptional = fallService.findFallByCurrentBenutzerAsBesitzer();
		if (!fallOptional.isPresent()) {
			fallOptional = fallService.createFallForCurrentGesuchstellerAsBesitzer();
		}

		Optional<Gemeinde> gemeindeOptional = gemeindeService.findGemeinde(gemeindeId);
		Gemeinde gemeinde = gemeindeOptional.orElseThrow(() -> new EbeguEntityNotFoundException(
			"getOrCreateDossierAndFallForCurrentUserAsBesitzer",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			gemeindeId));
		//noinspection ConstantConditions

		authorizer.checkReadAuthorization(gemeinde);

		Objects.requireNonNull(fallOptional.get());
		Optional<Dossier> dossierOptional = findDossierByGemeindeAndFall(gemeinde.getId(), fallOptional.get().getId());
		if (dossierOptional.isPresent()) {
			return dossierOptional.get();
		}

		Dossier dossier = new Dossier();
		dossier.setFall(fallOptional.get());
		dossier.setGemeinde(gemeinde);

		return saveDossier(dossier);
	}

	@Override
	public boolean hasDossierAnyMitteilung(@Nonnull Dossier dossier) {
		authorizer.checkReadAuthorizationDossier(dossier);

		final Collection<Mitteilung> mitteilungenForCurrentRolle =
			mitteilungService.getMitteilungenForCurrentRolle(dossier);
		return !mitteilungenForCurrentRolle.isEmpty();
	}

	@Nonnull
	@Override
	public Dossier setVerantwortlicherBG(@Nonnull String dossierId, @Nullable Benutzer benutzer) {
		final Dossier dossier =
			findDossier(dossierId).orElseThrow(() -> new EbeguEntityNotFoundException("setVerantwortlicherBG",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dossierId));
		authorizer.checkWriteAuthorizationDossier(dossier);
		dossier.setVerantwortlicherBG(benutzer);

		// Die Validierung bezüglich der Rolle des Verantwortlichen darf nur hier erfolgen, nicht bei jedem Speichern
		// des Falls
		validateVerantwortlicher(dossier, ChangeVerantwortlicherBGValidationGroup.class);
		return saveDossier(dossier);
	}

	@Nonnull
	@Override
	public Dossier setVerantwortlicherTS(@Nonnull String dossierId, @Nullable Benutzer benutzer) {
		final Dossier dossier =
			findDossier(dossierId).orElseThrow(() -> new EbeguEntityNotFoundException("setVerantwortlicherTS",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dossierId));
		authorizer.checkWriteAuthorizationDossier(dossier);
		dossier.setVerantwortlicherTS(benutzer);

		// Die Validierung bezüglich der Rolle des Verantwortlichen darf nur hier erfolgen, nicht bei jedem Speichern
		// des Falls
		validateVerantwortlicher(dossier, ChangeVerantwortlicherTSValidationGroup.class);
		return saveDossier(dossier);
	}

	@Nonnull
	@Override
	public LocalDate getErstesEinreichungsdatum(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		authorizer.checkWriteAuthorizationDossier(dossier);
		LocalDate erstesEinreichungsdatum = null;
		List<Gesuch> gesuchList =
			gesuchService.getAllGesucheForDossierAndPeriod(dossier, gesuchsperiode);
		for (Gesuch gesuch : gesuchList) {
			if (gesuch.getRegelStartDatum() != null &&
					(erstesEinreichungsdatum == null || gesuch.getRegelStartDatum().isBefore(erstesEinreichungsdatum))) {
				erstesEinreichungsdatum = gesuch.getRegelStartDatum();
			}
		}
		return erstesEinreichungsdatum != null ? erstesEinreichungsdatum : LocalDate.now();
	}

	private void validateVerantwortlicher(@Nonnull Dossier dossier, @Nonnull Class validationGroup) {
		Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Dossier>> constraintViolations = validator.validate(dossier, validationGroup);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}
}
