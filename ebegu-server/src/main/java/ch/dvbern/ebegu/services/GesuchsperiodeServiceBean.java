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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.EinstellungenFerieninsel;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiode;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Gesuchsperiode
 */
@Stateless
@Local(GesuchsperiodeService.class)
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
	private EinstellungService einstellungService;

	@Inject
	private ModulTagesschuleService modulTagesschuleService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private FerieninselStammdatenService ferieninselStammdatenService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private GesuchsperiodeEmailService gesuchsperiodeEmailService;

	@Inject
	private MandantService mandantService;

	@Nonnull
	@Override
	public Gesuchsperiode saveGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		requireNonNull(gesuchsperiode);
		return persistence.merge(gesuchsperiode);
	}

	@Nonnull
	@Override
	@SuppressWarnings("PMD.CollapsibleIfStatements")
	public Gesuchsperiode saveGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull GesuchsperiodeStatus statusBisher) {
		if (gesuchsperiode.isNew() && GesuchsperiodeStatus.ENTWURF != gesuchsperiode.getStatus()) {
			// Gesuchsperiode muss im Status ENTWURF erstellt werden
			throw new EbeguRuntimeException(
				"saveGesuchsperiode",
				ErrorCodeEnum.ERROR_GESUCHSPERIODE_INVALID_STATUSUEBERGANG,
				"Neu",
				gesuchsperiode.getStatus());
		}
		// Überprüfen, ob der Statusübergang zulässig ist
		if (gesuchsperiode.getStatus() != statusBisher) {
			handleStatusUebergang(gesuchsperiode, statusBisher);
		}
		if (gesuchsperiode.isNew()) {
			gesuchsperiode = saveGesuchsperiode(gesuchsperiode);
			LocalDate stichtagInVorperiode = gesuchsperiode.getGueltigkeit().getGueltigAb().minusDays(1);
			Optional<Gesuchsperiode> lastGesuchsperiodeOptional = getGesuchsperiodeAm(stichtagInVorperiode, mandantService.getDefaultMandant());
			if (lastGesuchsperiodeOptional.isPresent()) {
				Gesuchsperiode lastGesuchsperiode = lastGesuchsperiodeOptional.get();
				// we only copy the einstellung when there is a lastGesuchsperiode. In some cases, among others in
				// some tests we won't have a lastGesuchsperiode so we cannot copy the Einstellungen. In production
				// if there is no lastGesuchsperiode there is also nothing to copy
				einstellungService.copyEinstellungenToNewGesuchsperiode(gesuchsperiode, lastGesuchsperiode);

				// Die Module der Tagesschulen sollen ebenfalls für die neue Gesuchsperiode übernommen werden
				modulTagesschuleService.copyModuleTagesschuleToNewGesuchsperiode(gesuchsperiode, lastGesuchsperiode);

				// Die Einstellungen der Ferieninseln sollen ebenfalls für die neue Gesuchsperiode übernommen werden
				ferieninselStammdatenService.copyEinstellungenFerieninselToNewGesuchsperiode(gesuchsperiode, lastGesuchsperiode);

				//Die Gemeinde Gesuchsperiode Stammdaten sollen auch für die neue Gesuchsperiode übernommen werden
				gemeindeService.copyGesuchsperiodeGemeindeStammdaten(gesuchsperiode, lastGesuchsperiode);

				//copy erlaeuterung verfuegung from previos Gesuchperiode
				gesuchsperiode.setVerfuegungErlaeuterungenDe(lastGesuchsperiode.getVerfuegungErlaeuterungenDe());
				gesuchsperiode.setVerfuegungErlaeuterungenFr(lastGesuchsperiode.getVerfuegungErlaeuterungenFr());
				// Merkblatt Tagesschulen kopieren
				gesuchsperiode.setVorlageMerkblattTsDe(lastGesuchsperiode.getVorlageMerkblattTsDe());
				gesuchsperiode.setVorlageMerkblattTsFr(lastGesuchsperiode.getVorlageMerkblattTsFr());
				// Vorlage Verfügung Lats kopieren
				gesuchsperiode.setVorlageVerfuegungLatsDe(lastGesuchsperiode.getVorlageVerfuegungLatsDe());
				gesuchsperiode.setVorlageVerfuegungLatsFr(lastGesuchsperiode.getVorlageVerfuegungLatsFr());
				// Vorlage Verfügung Ferienbetreuung kopieren
				gesuchsperiode.setVorlageVerfuegungFerienbetreuungDe(lastGesuchsperiode.getVorlageVerfuegungFerienbetreuungDe());
				gesuchsperiode.setVorlageVerfuegungFerienbetreuungFr(lastGesuchsperiode.getVorlageVerfuegungFerienbetreuungFr());
			}
		}
		return saveGesuchsperiode(gesuchsperiode);
	}

	private void handleStatusUebergang(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull GesuchsperiodeStatus statusBisher
	) {
		// Alle Statusuebergaenge werden geloggt
		logStatusChange(gesuchsperiode, statusBisher);
		// Superadmin darf alles
		if (!principalBean.isCallerInRole(UserRole.SUPER_ADMIN)
			&& !isStatusUebergangValid(statusBisher, gesuchsperiode.getStatus())) {
			throw new EbeguRuntimeException(
				"saveGesuchsperiode",
				ErrorCodeEnum.ERROR_GESUCHSPERIODE_INVALID_STATUSUEBERGANG,
				statusBisher,
				gesuchsperiode.getStatus());
		}
		// Falls es ein Statuswechsel war, und der neue Status ist AKTIV -> Mail an alle Gesuchsteller schicken
		// Nur, wenn die Gesuchsperiode noch nie auf aktiv geschaltet war.
		if (GesuchsperiodeStatus.AKTIV == gesuchsperiode.getStatus()
			&& gesuchsperiode.getDatumAktiviert() == null) {
			Optional<Gesuchsperiode> lastGesuchsperiodeOptional =
				getGesuchsperiodeAm(gesuchsperiode.getGueltigkeit().getGueltigAb().minusDays(1), mandantService.getDefaultMandant());
			if (lastGesuchsperiodeOptional.isPresent()) {
				gesuchsperiodeEmailService.getAndSaveGesuchsperiodeEmailCandidates(
					lastGesuchsperiodeOptional.get(),
					gesuchsperiode);
				gesuchsperiode.setDatumAktiviert(LocalDate.now());
			}
		}
		// Prüfen, dass ALLE Gesuche dieser Periode im Status "Verfügt" oder "Schulamt" sind. Sind noch
		// Gesuce in Bearbeitung, oder in Beschwerde etc. darf nicht geschlossen werden!
		if (GesuchsperiodeStatus.GESCHLOSSEN == gesuchsperiode.getStatus()
			&& !gesuchService.canGesuchsperiodeBeClosed(gesuchsperiode)) {
			throw new EbeguRuntimeException(
				"saveGesuchsperiode",
				ErrorCodeEnum.ERROR_GESUCHSPERIODE_CANNOT_BE_CLOSED);
		}
	}

	@Nonnull
	@Override
	public Optional<Gesuchsperiode> findGesuchsperiode(@Nonnull String key) {
		requireNonNull(key, "id muss gesetzt sein");
		Gesuchsperiode gesuchsperiode = persistence.find(Gesuchsperiode.class, key);
		return Optional.ofNullable(gesuchsperiode);
	}

	@Nonnull
	@Override
	public Collection<Gesuchsperiode> getAllGesuchsperioden() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
		return persistence.getCriteriaResults(query);

	}

	@Nullable
	@Override
	public Collection<Gesuchsperiode> findThisAndFutureGesuchsperioden(@Nonnull String key) {
		List<Gesuchsperiode> gesuchsperioden = null;
		Optional<Gesuchsperiode> gesuchsperiode = findGesuchsperiode(key);
		if (gesuchsperiode.isPresent()) {
			LocalDate datumVon = gesuchsperiode.get().getGueltigkeit().getGueltigAb();
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
			Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
			Path<DateRange> dateRangePath = root.get(AbstractDateRangedEntity_.gueltigkeit);
			Predicate predicateVon = cb.greaterThanOrEqualTo(dateRangePath.get(DateRange_.gueltigAb), datumVon);
			query.where(predicateVon);
			query.orderBy(cb.desc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
			gesuchsperioden = persistence.getCriteriaResults(query);
		}
		return gesuchsperioden;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void removeGesuchsperiode(@Nonnull String gesuchsPeriodeId) {
		Optional<Gesuchsperiode> gesuchsperiodeOptional = findGesuchsperiode(gesuchsPeriodeId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeOptional.orElseThrow(() -> new EbeguEntityNotFoundException(
			"deleteGesuchsperiodeAndGesuche",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			gesuchsPeriodeId));
		LOGGER.info("Handling deleton of Gesuchsperiode {}", gesuchsperiode.getGesuchsperiodeString());
		if (gesuchsperiode.getStatus() == GesuchsperiodeStatus.GESCHLOSSEN) {
			// Gesuche der Periode loeschen
			Collection<Gesuch> gesucheOfPeriode =
				criteriaQueryHelper.getEntitiesByAttribute(Gesuch.class, gesuchsperiode, Gesuch_.gesuchsperiode);
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
			Collection<GemeindeStammdatenGesuchsperiodeFerieninsel> ferieninselStammdatenList =
				ferieninselStammdatenService.findGesuchsperiodeFerieninselByGemeindeAndPeriode(null, gesuchsPeriodeId);
			for (GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdaten : ferieninselStammdatenList) {
				ferieninselStammdatenService.removeFerieninselStammdaten(ferieninselStammdaten.getId());
			}

			// EinstellungenFerieninsel dieser Gesuchsperiode loeschen
			Collection<EinstellungenFerieninsel> einstellungenFerieninselList =
				ferieninselStammdatenService.findEinstellungenFerieninselByGesuchsperiode(gesuchsperiode);
			for (EinstellungenFerieninsel einstellungenFerieninsel : einstellungenFerieninselList) {
				persistence.remove(einstellungenFerieninsel);
			}

			// EinstellungenTagesschule dieser Gesuchsperiode loeschen
			Collection<EinstellungenTagesschule> einstellungenTagesschuleList =
				modulTagesschuleService.findEinstellungenTagesschuleByGesuchsperiode(gesuchsperiode);
			for (EinstellungenTagesschule einstellungenTagesschule : einstellungenTagesschuleList) {
				persistence.remove(einstellungenTagesschule);
			}

			// GemeindeGesuchsperiodeStammdaten dieser Gesuchsperiode loeschen
			Collection<GemeindeStammdatenGesuchsperiode> gemeindeStammdatenGesuchsperiodeList =
				gemeindeService.findGemeindeStammdatenGesuchsperiode(gesuchsperiode);
			for (GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode :
				gemeindeStammdatenGesuchsperiodeList) {
				persistence.remove(gemeindeStammdatenGesuchsperiode);
			}

			// Einstellungen dieser Gesuchsperiode loeschen
			einstellungService.deleteEinstellungenOfGesuchsperiode(gesuchsperiode);
			// Gesuchsperiode
			LOGGER.info("Deleting Gesuchsperiode {}", gesuchsperiode.getGesuchsperiodeString());
			persistence.remove(gesuchsperiode);
		} else {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"removeGesuchsperiode",
				ErrorCodeEnum.ERROR_GESUCHSPERIODE_CANNOT_BE_REMOVED);
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
			LOGGER.info(
				"This was the last Gesuch of Dossier, deleting Dossier of Fall {} and Gemeinde {}",
				dossier.getFall(),
				dossier.getGemeinde());
			dossierService.removeDossier(dossier.getId(), cause);
		}
	}

	@Override
	@Nonnull
	public Collection<Gesuchsperiode> getAllActiveGesuchsperioden() {
		return getGesuchsperiodenImStatus(GesuchsperiodeStatus.AKTIV);
	}

	@Override
	@Nonnull
	public Collection<Gesuchsperiode> getAllNichtAbgeschlosseneGesuchsperioden() {
		return getGesuchsperiodenImStatus(GesuchsperiodeStatus.AKTIV, GesuchsperiodeStatus.INAKTIV,
			GesuchsperiodeStatus.ENTWURF);
	}

	/**
	 * @return all Gesuchsperiode who are in status Aktiv or Inaktiv
	 */
	@Override
	@Nonnull
	public Collection<Gesuchsperiode> getAllAktivUndInaktivGesuchsperioden() {
		return getGesuchsperiodenImStatus(GesuchsperiodeStatus.AKTIV, GesuchsperiodeStatus.INAKTIV);
	}

	@Override
	@Nonnull
	public Collection<Gesuchsperiode> getAllAktivInaktivNichtVerwendeteGesuchsperioden(
		@Nonnull String dossierId
	) {
		Dossier dossier = dossierService.findDossier(dossierId).orElseThrow(() ->
			new EbeguEntityNotFoundException("getAllAktivInaktivNichtVerwendeteGesuchsperioden",
				ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, dossierId)
		);
		final Collection<Gesuchsperiode> nichtAbgeschlossenePerioden = getAllAktivUndInaktivGesuchsperioden();

		filterAllGesuchperiodenForDossier(dossier, nichtAbgeschlossenePerioden);
		return nichtAbgeschlossenePerioden;
	}

	@Nonnull
	@Override
	public Collection<Gesuchsperiode> getAllAktiveNichtVerwendeteGesuchsperioden(@Nonnull String dossierId) {
		Dossier dossier = dossierService.findDossier(dossierId).orElseThrow(() ->
			new EbeguEntityNotFoundException("getAllAktiveNichtVerwendeteGesuchsperioden",
				ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, dossierId)
		);
		final Collection<Gesuchsperiode> aktivePerioden = getAllActiveGesuchsperioden();

		filterAllGesuchperiodenForDossier(dossier, aktivePerioden);
		return aktivePerioden;
	}

	private void filterAllGesuchperiodenForDossier(
		@Nonnull Dossier dossier,
		@Nonnull Collection<Gesuchsperiode> perioden) {
		if (!perioden.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

			Root<Gesuch> root = query.from(Gesuch.class);
			Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier), dossier);
			Predicate gesuchsperiodePredicate = root.get(Gesuch_.gesuchsperiode).in(perioden);
			// Es interessieren nur die Gesuche, die entweder Papier oder Online und freigegeben sind, also keine, die
			// in Bearbeitung GS sind.

			Predicate gesuchStatus = root.get(Gesuch_.status).in(AntragStatus.getInBearbeitungGSStates()).not();

			query.where(dossierPredicate, gesuchsperiodePredicate, gesuchStatus);
			List<Gesuch> criteriaResults = persistence.getCriteriaResults(query);
			// Die Gesuchsperioden, die jetzt in der Liste sind, sind sicher besetzt (eventuell noch weitere, sprich
			// Online-Gesuche)
			for (Gesuch criteriaResult : criteriaResults) {
				perioden.remove(criteriaResult.getGesuchsperiode());
			}
		}
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
	public Optional<Gesuchsperiode> getGesuchsperiodeAm(@Nonnull LocalDate stichtag, @Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);

		Predicate predicateStart =
			cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), stichtag);
		Predicate predicateEnd = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			stichtag);
		Predicate mandantPredicate =
				cb.equal(root.get(Gesuchsperiode_.mandant), mandant);

		query.where(predicateStart, predicateEnd, mandantPredicate);
		Gesuchsperiode criteriaSingleResult = persistence.getCriteriaSingleResult(query);
		return Optional.ofNullable(criteriaSingleResult);
	}

	@Override
	@Nonnull
	public Collection<Gesuchsperiode> getGesuchsperiodenBetween(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);

		Predicate predicateStart =
			cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), datumBis);
		Predicate predicateEnd = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			datumVon);

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

	@Nonnull
	@Override
	public Gesuchsperiode uploadGesuchsperiodeDokument(
		@Nonnull String gesuchsperiodeId,
		@Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp,
		@Nonnull byte[] content) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(content);

		final Gesuchsperiode gesuchsperiode = findGesuchsperiode(gesuchsperiodeId).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"uploadErlaeuterungenVerfuegung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId)
		);

		if (dokumentTyp.equals(DokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVerfuegungErlaeuterungenDe(content);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVerfuegungErlaeuterungenFr(content);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't overwrite accidentaly
				return gesuchsperiode;
			}
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_MERKBLATT_TS)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVorlageMerkblattTsDe(content);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVorlageMerkblattTsFr(content);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't overwrite accidentaly
				return gesuchsperiode;
			}
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_LATS)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVorlageVerfuegungLatsDe(content);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVorlageVerfuegungLatsFr(content);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't overwrite accidentaly
				return gesuchsperiode;
			}
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVorlageVerfuegungFerienbetreuungDe(content);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVorlageVerfuegungFerienbetreuungFr(content);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't overwrite accidentaly
				return gesuchsperiode;
			}
		} else {
			return gesuchsperiode;
		}
		return saveGesuchsperiode(gesuchsperiode);
	}

	@Override
	public Gesuchsperiode removeGesuchsperiodeDokument(@Nonnull String gesuchsperiodeId, @Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);

		final Gesuchsperiode gesuchsperiode = findGesuchsperiode(gesuchsperiodeId).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"uploadErlaeuterungenVerfuegung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId)
		);
		if (dokumentTyp.equals(DokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVerfuegungErlaeuterungenDe(null);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVerfuegungErlaeuterungenFr(null);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't remove accidentaly
				return gesuchsperiode;
			}
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_MERKBLATT_TS)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVorlageMerkblattTsDe(null);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVorlageMerkblattTsFr(null);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't remove accidentaly
				return gesuchsperiode;
			}
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_LATS)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVorlageVerfuegungLatsDe(null);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVorlageVerfuegungLatsFr(null);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't remove accidentaly
				return gesuchsperiode;
			}
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG)) {
			if (sprache == Sprache.DEUTSCH) {
				gesuchsperiode.setVorlageVerfuegungFerienbetreuungDe(null);
			} else if (sprache == Sprache.FRANZOESISCH) {
				gesuchsperiode.setVorlageVerfuegungFerienbetreuungFr(null);
			} else {
				// in case we don't recognize the language we don't do anything, so we don't remove accidentaly
				return gesuchsperiode;
			}
		} else {
			return gesuchsperiode;
		}

		return saveGesuchsperiode(gesuchsperiode);
	}

	@Override
	public boolean existDokument(@Nonnull String gesuchsperiodeId, @Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);

		final Gesuchsperiode gesuchsperiode = findGesuchsperiode(gesuchsperiodeId).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"existDokument",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId)
		);
		if (dokumentTyp.equals(DokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG)) {
			return gesuchsperiode.getVerfuegungErlaeuterungWithSprache(sprache).length != 0;
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_MERKBLATT_TS)) {
			return gesuchsperiode.getVorlageMerkblattTsWithSprache(sprache).length != 0;
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_LATS)) {
			return gesuchsperiode.getVorlageVerfuegungLatsWithSprache(sprache).length != 0;
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG)) {
			return gesuchsperiode.getVorlageVerfuegungFerienbetreuungWithSprache(sprache).length != 0;
		}

		return false;
	}

	@Nullable
	@Override
	public byte[] downloadGesuchsperiodeDokument(@Nonnull String gesuchsperiodeId, @Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp) {
		final Optional<Gesuchsperiode> gesuchsperiode = findGesuchsperiode(gesuchsperiodeId);
		if (dokumentTyp.equals(DokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG)) {
			return gesuchsperiode
				.map(gesuchsperiode1 -> gesuchsperiode1.getVerfuegungErlaeuterungWithSprache(sprache))
				.orElse(null);
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_MERKBLATT_TS)) {
			return gesuchsperiode
				.map(gesuchsperiode1 -> gesuchsperiode1.getVorlageMerkblattTsWithSprache(sprache))
				.orElse(null);
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_LATS)) {
			return gesuchsperiode
				.map(gesuchsperiode1 -> gesuchsperiode1.getVorlageVerfuegungLatsWithSprache(sprache))
				.orElse(null);
		} else if (dokumentTyp.equals(DokumentTyp.VORLAGE_VERFUEGUNG_FERIENBETREUUNG)) {
			return gesuchsperiode
				.map(gesuchsperiode1 -> gesuchsperiode1.getVorlageVerfuegungFerienbetreuungWithSprache(sprache))
				.orElse(null);
		}
		return new byte[0];
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
