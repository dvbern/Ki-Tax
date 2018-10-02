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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.FerieninselStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Gesuchsperiode
 */
@Stateless
@Local(GesuchsperiodeService.class)
@PermitAll
public class GesuchsperiodeServiceBean extends AbstractBaseService implements GesuchsperiodeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GesuchsperiodeServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private FallService fallService;

	@Inject
	private DossierService dossierService;

	@Inject
	private FerieninselStammdatenService ferieninselStammdatenService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public Gesuchsperiode saveGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		Objects.requireNonNull(gesuchsperiode);
		return persistence.merge(gesuchsperiode);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	@SuppressWarnings("PMD.CollapsibleIfStatements")
	public Gesuchsperiode saveGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GesuchsperiodeStatus statusBisher) {
		if (gesuchsperiode.isNew() && GesuchsperiodeStatus.ENTWURF != gesuchsperiode.getStatus()) {
			// Gesuchsperiode muss im Status ENTWURF erstellt werden
			throw new EbeguRuntimeException("saveGesuchsperiode", ErrorCodeEnum.ERROR_GESUCHSPERIODE_INVALID_STATUSUEBERGANG, "Neu", gesuchsperiode.getStatus());
		}
		// Überprüfen, ob der Statusübergang zulässig ist
		if (gesuchsperiode.getStatus() != statusBisher) {
			// Alle Statusuebergaenge werden geloggt
			logStatusChange(gesuchsperiode, statusBisher);
			// Superadmin darf alles
			if (!principalBean.isCallerInRole(UserRole.SUPER_ADMIN)) {
				if (!isStatusUebergangValid(statusBisher, gesuchsperiode.getStatus())) {
					throw new EbeguRuntimeException("saveGesuchsperiode", ErrorCodeEnum.ERROR_GESUCHSPERIODE_INVALID_STATUSUEBERGANG, statusBisher, gesuchsperiode.getStatus());
				}
			}
			// Falls es ein Statuswechsel war, und der neue Status ist AKTIV -> Mail an alle Gesuchsteller schicken
			// Nur, wenn die Gesuchsperiode noch nie auf aktiv geschaltet war.
			if (GesuchsperiodeStatus.AKTIV == gesuchsperiode.getStatus() && gesuchsperiode.getDatumAktiviert() == null) {
				Optional<Gesuchsperiode> lastGesuchsperiodeOptional = getGesuchsperiodeAm(gesuchsperiode.getGueltigkeit().getGueltigAb().minusDays(1));
				if (lastGesuchsperiodeOptional.isPresent()) {
					gesuchService.sendMailsToAllGesuchstellerOfLastGesuchsperiode(lastGesuchsperiodeOptional.get(), gesuchsperiode);
					gesuchsperiode.setDatumAktiviert(LocalDate.now());
				}
			}
			if (GesuchsperiodeStatus.GESCHLOSSEN == gesuchsperiode.getStatus()) {
				// Prüfen, dass ALLE Gesuche dieser Periode im Status "Verfügt" oder "Schulamt" sind. Sind noch
				// Gesuce in Bearbeitung, oder in Beschwerde etc. darf nicht geschlossen werden!
				if (!gesuchService.canGesuchsperiodeBeClosed(gesuchsperiode)) {
					throw new EbeguRuntimeException("saveGesuchsperiode", ErrorCodeEnum.ERROR_GESUCHSPERIODE_CANNOT_BE_CLOSED);
				}
			}
		}
		if (gesuchsperiode.isNew()) {
			gesuchsperiode = saveGesuchsperiode(gesuchsperiode);
			LocalDate stichtagInVorperiode = gesuchsperiode.getGueltigkeit().getGueltigAb().minusDays(1);
			Optional<Gesuchsperiode> lastGesuchsperiode = getGesuchsperiodeAm(stichtagInVorperiode);
			if (lastGesuchsperiode.isPresent()) {
				// we only copy the einstellung when there is a lastGesuchsperiode. In some cases, among others in some tests we won't have
				// a lastGesuchsperiode so we cannot copy the Einstellungen. In production if there is no lastGesuchsperiode there is also nothing to copy
				einstellungService.copyEinstellungenToNewGesuchsperiode(gesuchsperiode, lastGesuchsperiode.get());
			}
			// Wenn die Gesuchsperiode neu ist, muss das Datum Freischaltung Tagesschule gesetzt werden: Defaultmässig
			// erster Tag der Gesuchsperiode. Kann nach Aktivierung der Periode auf ein beliebiges Datum gesetzt werden
			gesuchsperiode.setDatumFreischaltungTagesschule(gesuchsperiode.getGueltigkeit().getGueltigAb());
		}
		return saveGesuchsperiode(gesuchsperiode);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Gesuchsperiode> findGesuchsperiode(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Gesuchsperiode gesuchsperiode = persistence.find(Gesuchsperiode.class, key);
		return Optional.ofNullable(gesuchsperiode);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Gesuchsperiode> getAllGesuchsperioden() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
		return persistence.getCriteriaResults(query);

	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed(SUPER_ADMIN)
	public void removeGesuchsperiode(@Nonnull String gesuchsPeriodeId) {
		Optional<Gesuchsperiode> gesuchsperiodeOptional = findGesuchsperiode(gesuchsPeriodeId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeOptional.orElseThrow(() -> new EbeguEntityNotFoundException("deleteGesuchsperiodeAndGesuche", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsPeriodeId));
		LOGGER.info("Handling Gesuchsperiode {}", gesuchsperiode.getGesuchsperiodeString());
		if (gesuchsperiode.getStatus() == GesuchsperiodeStatus.GESCHLOSSEN) {
			// Gesuche der Periode loeschen
			Collection<Gesuch> gesucheOfPeriode = criteriaQueryHelper.getEntitiesByAttribute(Gesuch.class, gesuchsperiode, Gesuch_.gesuchsperiode);
			for (Gesuch gesuch : gesucheOfPeriode) {
				Fall fall = gesuch.getFall();
				Dossier dossier = gesuch.getDossier();

				// Gesuch, WizardSteps, Mahnungen, Dokumente, AntragstatusHistory, Zahlungspositionen
				LOGGER.info("Deleting Gesuch of Fall {}", gesuch.getFall().getFallNummer());
				gesuchService.removeGesuch(gesuch.getId(), GesuchDeletionCause.BATCHJOB_DATENSCHUTZVERORDNUNG);

				removeDossierIfEmpty(dossier, GesuchDeletionCause.BATCHJOB_DATENSCHUTZVERORDNUNG);
				removeFallIfEmpty(fall, GesuchDeletionCause.BATCHJOB_DATENSCHUTZVERORDNUNG);
			}
			// FerieninselStammdaten dieser Gesuchsperiode loeschen
			Collection<FerieninselStammdaten> ferieninselStammdatenList = ferieninselStammdatenService.findFerieninselStammdatenForGesuchsperiode(gesuchsPeriodeId);
			for (FerieninselStammdaten ferieninselStammdaten : ferieninselStammdatenList) {
				ferieninselStammdatenService.removeFerieninselStammdaten(ferieninselStammdaten.getId());
			}
			// Einstellungen dieser Gesuchsperiode loeschen
			einstellungService.deleteEinstellungenOfGesuchsperiode(gesuchsperiode);
			// Gesuchsperiode
			LOGGER.info("Deleting Gesuchsperiode {}", gesuchsperiode.getGesuchsperiodeString());
			persistence.remove(gesuchsperiode);
		} else {
			throw new EbeguRuntimeException("removeGesuchsperiode", ErrorCodeEnum.ERROR_GESUCHSPERIODE_CANNOT_BE_REMOVED);
		}
	}

	private void removeFallIfEmpty(@Nonnull Fall fall, @Nonnull GesuchDeletionCause cause) {
		Collection<Dossier> dossiersByFall = dossierService.findDossiersByFall(fall.getId());
		if (dossiersByFall.isEmpty()) {
			LOGGER.info("This was the last Gesuch/Dossier of Fall, deleting Fall {}", fall.getFallNummer());
			fallService.removeFall(fall, cause);
		}
	}

	private void removeDossierIfEmpty(@Nonnull Dossier dossier, @Nonnull GesuchDeletionCause cause) {
		List<String> allGesuchIDsForDossier = gesuchService.getAllGesuchIDsForDossier(dossier.getId());
		if (allGesuchIDsForDossier.isEmpty()) {
			LOGGER.info("This was the last Gesuch of Dossier, deleting Dossier of Fall {} and Gemeinde {}", dossier.getFall(), dossier.getGemeinde());
			dossierService.removeDossier(dossier.getId(), cause);
		}
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Gesuchsperiode> getAllActiveGesuchsperioden() {
		return getGesuchsperiodenImStatus(GesuchsperiodeStatus.AKTIV);
	}

	/**
	 * @return all Gesuchsperioden that have a gueltigkeitBis Date that is in the future (compared to the current date)
	 */
	@Override
	@Nonnull
	@PermitAll
	public Collection<Gesuchsperiode> getAllNichtAbgeschlosseneGesuchsperioden() {
		return getGesuchsperiodenImStatus(GesuchsperiodeStatus.AKTIV, GesuchsperiodeStatus.INAKTIV);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Gesuchsperiode> getGesuchsperiodenAfterDate(@Nonnull LocalDate date) {
		return getGesuchsperiodenImStatus(GesuchsperiodeStatus.AKTIV).stream()
			.filter(periode -> periode.getGueltigkeit().startsSameDayOrAfter(date))
			.collect(Collectors.toList());
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Gesuchsperiode> getAllNichtAbgeschlosseneNichtVerwendeteGesuchsperioden(@Nullable String dossierId) {
		Dossier dossier = persistence.find(Dossier.class, dossierId);

		if (dossier == null) {
			throw new EbeguEntityNotFoundException("getAllNichtAbgeschlosseneNichtVerwendeteGesuchsperioden",
				ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, dossierId);
		}

		final Collection<Gesuchsperiode> nichtAbgeschlossenePerioden = getAllNichtAbgeschlosseneGesuchsperioden();
		if (!nichtAbgeschlossenePerioden.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

			Root<Gesuch> root = query.from(Gesuch.class);
			Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier), dossier);
			Predicate gesuchsperiodePredicate = root.get(Gesuch_.gesuchsperiode).in(nichtAbgeschlossenePerioden);
			// Es interessieren nur die Gesuche, die entweder Papier oder Online und freigegeben sind, also keine, die in Bearbeitung GS sind.

			Predicate gesuchStatus = root.get(Gesuch_.status).in(AntragStatus.getInBearbeitungGSStates()).not();

			query.where(dossierPredicate, gesuchsperiodePredicate, gesuchStatus);
			List<Gesuch> criteriaResults = persistence.getCriteriaResults(query);
			// Die Gesuchsperioden, die jetzt in der Liste sind, sind sicher besetzt (eventuell noch weitere, sprich Online-Gesuche)
			for (Gesuch criteriaResult : criteriaResults) {
				nichtAbgeschlossenePerioden.remove(criteriaResult.getGesuchsperiode());
			}
		}
		return nichtAbgeschlossenePerioden;
	}

	private Collection<Gesuchsperiode> getGesuchsperiodenImStatus(GesuchsperiodeStatus... status) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = builder.createQuery(Gesuchsperiode.class);
		final Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
		query.where(root.get(Gesuchsperiode_.status).in(status));
		query.orderBy(builder.desc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<Gesuchsperiode> getGesuchsperiodeAm(@Nonnull LocalDate stichtag) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);

		Predicate predicateStart = cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), stichtag);
		Predicate predicateEnd = cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), stichtag);

		query.where(predicateStart, predicateEnd);
		Gesuchsperiode criteriaSingleResult = persistence.getCriteriaSingleResult(query);
		return Optional.ofNullable(criteriaSingleResult);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Gesuchsperiode> getGesuchsperiodenBetween(@Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);

		Predicate predicateStart = cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), datumBis);
		Predicate predicateEnd = cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), datumVon);

		query.where(predicateStart, predicateEnd);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Optional<Gesuchsperiode> findNewestGesuchsperiode() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
		query.orderBy(cb.desc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis)));
		final List<Gesuchsperiode> results = persistence.getCriteriaResults(query, 1);
		if (results.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(results.get(0));
	}

	private boolean isStatusUebergangValid(GesuchsperiodeStatus statusBefore, GesuchsperiodeStatus statusAfter) {
		if (GesuchsperiodeStatus.ENTWURF == statusBefore) {
			return GesuchsperiodeStatus.AKTIV == statusAfter;
		}
		if (GesuchsperiodeStatus.AKTIV == statusBefore) {
			return GesuchsperiodeStatus.INAKTIV == statusAfter;
		}
		if (GesuchsperiodeStatus.INAKTIV == statusBefore) {
			return GesuchsperiodeStatus.GESCHLOSSEN == statusAfter;
		}
		return false;
	}

	private void logStatusChange(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GesuchsperiodeStatus statusBisher) {
		LOGGER.info("****************************************************");
		LOGGER.info("Status Gesuchsperiode wurde geändert:");
		LOGGER.info("Benutzer: {}", principalBean.getBenutzer().getUsername());
		LOGGER.info("Gesuchsperiode: {} ({}" + ')', gesuchsperiode.getGesuchsperiodeString(), gesuchsperiode.getId());
		LOGGER.info("Neuer Status: {}", gesuchsperiode.getStatus());
		LOGGER.info("Bisheriger Status: {}", statusBisher);
		LOGGER.info("****************************************************");
	}
}
