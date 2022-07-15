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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.ejb.Asynchronous;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlung_;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsauftrag_;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.entities.Zahlungsposition_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.services.util.ZahlungslaufUtil;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.util.MathUtil.DEFAULT;
import static ch.dvbern.ebegu.util.MathUtil.isSame;

/**
 * Test-Service fuer Zahlungen. Es wird fuer alle Faelle die letzt gueltige Verfuegung verglichen mit den tatsaechlich erfolgten
 * Zahlungen.
 * Einige Gesuche haben bekanntermassen falsche Auszahlungen gehabt. Diese werden entsprechend behandelt.
 */
@Stateful
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "SpringAutowiredFieldsWarningInspection", "InstanceMethodNamingConvention" })
public class ZahlungUeberpruefungServiceBean extends AbstractBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZahlungUeberpruefungServiceBean.class.getSimpleName());


	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private MailService mailService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private Persistence persistence;

	@Inject
	private ZahlungService zahlungService;

	@Nonnull
	private ZahlungslaufHelper zahlungslaufHelper;

	private List<String> potentielleFehlerList = new ArrayList<>();
	private List<String> potenzielleFehlerListZusammenfassung = new ArrayList<>();


	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@TransactionTimeout(value = 360, unit = TimeUnit.MINUTES)
	public void pruefungZahlungen(
		@Nonnull Gemeinde gemeinde,
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String zahlungsauftragId,
		@Nonnull LocalDateTime datumLetzteZahlung,
		@Nonnull String beschrieb
	) {
		StopWatch stopWatch = logAndStartTimer(String.format(
			"Starte Zahlungsüberprüfung für %s",
			gemeinde.getName()));

		this.zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);
		Objects.requireNonNull(gemeinde);
		Objects.requireNonNull(zahlungsauftragId);
		Objects.requireNonNull(datumLetzteZahlung);
		resetAllData();

		// Alle Gesuchsperioden im Status AKTIV und INAKTIV muessen geprueft werden, da auch rueckwirkend Korrekturen gemacht werden koennen.
		Collection<Gesuchsperiode> aktiveGesuchsperioden = gesuchsperiodeService.getAllAktivUndInaktivGesuchsperioden();

		// IST- und SOLL- Zustand lesen, vergleichen
		Zahlungsauftrag zahlungsauftrag = zahlungService.findZahlungsauftrag(zahlungsauftragId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("findZahlungsauftrag", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
		Collection<Gesuchsperiode> containedGesuchsperioden =
			ZahlungslaufUtil.findGesuchsperiodenContainedInZahlungsauftrag(aktiveGesuchsperioden, zahlungsauftrag);
		ermittleIstAndSollAndCheckFuerGPs(gemeinde, containedGesuchsperioden, datumLetzteZahlung);

		logAndStopTimer(stopWatch, String.format(
			"Zahlungsüberprüfung für %s beendet: %s",
			gemeinde.getName(),
			(potentielleFehlerList.isEmpty() ? "OK" : "ERROR")));
		sendeMail(gemeinde, zahlungsauftragId, beschrieb);
		resetAllData();
	}

	private void ermittleIstAndSollAndCheckFuerGPs(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Collection<Gesuchsperiode> gesuchesperiodenToCheck,
		@Nonnull LocalDateTime datumLetzteZahlung
	) {
		for (Gesuchsperiode gesuchsperiode : gesuchesperiodenToCheck) {
			final String step = String.format(
				"Kontrolle für Gesuchsperiode %s für Gemeinde %s",
				gesuchsperiode.getGesuchsperiodeString(),
				gemeinde.getName());
			final StopWatch stopWatch = logAndStartTimer(step);
			final Map<String, List<Zahlungsposition>> zahlungenIstFuerGP = pruefeZahlungenIst(gemeinde, gesuchsperiode);
			pruefungZahlungenSollFuerGesuchsperiode(gesuchsperiode, gemeinde, datumLetzteZahlung, zahlungenIstFuerGP);
			logAndStopTimer(stopWatch, step);
		}
	}

	private void sendeMail(@Nonnull Gemeinde gemeinde, @Nonnull String zahlungsauftragId, @Nonnull String beschrieb) {
		Objects.requireNonNull(gemeinde);
		LOGGER.info("ZAHLUNGSUEBERPRUEFUNG: Sende Mail...");
		String administratorMail = ebeguConfiguration.getAdministratorMail();
		if (StringUtils.isEmpty(administratorMail)) {
			LOGGER.warn("ZAHLUNGSUEBERPRUEFUNG: Es ist keine Administrator-Email konfiguriert. Sende keine E-Mail ueber den Zahlungspruefungs-Status");
			return;
		}
		try {
			final String serverName = ebeguConfiguration.getHostname(gemeinde.getMandant().getMandantIdentifier());
			final String typ = ServerMessageUtil.translateEnumValue(zahlungslaufHelper.getZahlungslaufTyp(), Locale.GERMAN,
					Objects.requireNonNull(gemeinde.getMandant()));
			String auftragBezeichnung = "Zahlungslauf " + gemeinde.getName() + " (" + serverName + ", " + typ + ')';
			String autragResult = "Pending";
			if (potentielleFehlerList.isEmpty()) {
				mailService.sendMessage(auftragBezeichnung + ": Keine Fehler gefunden",
					"Bezeichnung: " + beschrieb + ": Keine Fehler gefunden", administratorMail);
				autragResult = "OK";
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Zusammenfassung: \n");
				for (String s : potenzielleFehlerListZusammenfassung) {
					sb.append(s);
					sb.append('\n');
				}
				sb.append("Zusammenfassung Ende\n");

				for (String s : potentielleFehlerList) {
					sb.append(s);
					sb.append("\n*************************************\n");
				}
				mailService.sendMessage(auftragBezeichnung+ ": Potentieller Fehler im Zahlungslauf",
					sb.toString(), administratorMail);
				autragResult = "Bezeichnung: " + beschrieb + ": Potentieller Fehler im Zahlungslauf: " + sb;
				autragResult = StringUtils.abbreviate(autragResult, Constants.DB_TEXTAREA_LENGTH);

			}
			// Erst jetzt den Zahlungsauftrag lesen bzw. updaten, wegen OptimisticLockExceptions,
			updateZahlungsauftragResult(zahlungsauftragId, autragResult);

		} catch (MailException e) {
			logExceptionAccordingToEnvironment(e, "Senden der Mail nicht erfolgreich", "");
		}
		LOGGER.info("ZAHLUNGSUEBERPRUEFUNG: ... sende Mail beendet");
	}

	private void updateZahlungsauftragResult(@Nonnull String zahlungsauftragId, @Nonnull String autragResult) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaUpdate<Zahlungsauftrag> criteriaUpdate = cb.createCriteriaUpdate(Zahlungsauftrag.class);
		Root<Zahlungsauftrag> employeeRoot = criteriaUpdate.from(Zahlungsauftrag.class);
		criteriaUpdate.set(employeeRoot.get(Zahlungsauftrag_.result), autragResult);
		criteriaUpdate.where(cb.equal(employeeRoot.get(Zahlungsauftrag_.id), zahlungsauftragId));
		persistence.getEntityManager().createQuery(criteriaUpdate).executeUpdate();
	}

	private void pruefungZahlungenSollFuerGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde,
		@Nonnull LocalDateTime datumLetzteZahlung,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap
	) {
		Objects.requireNonNull(gesuchsperiode);
		Objects.requireNonNull(gemeinde);
		Objects.requireNonNull(datumLetzteZahlung);

		final String step = String.format(
			"Prüfe SOLL für GP %s und Gemeinde %s",
			gesuchsperiode.getGesuchsperiodeString(),
			gemeinde.getName());
		final StopWatch stopWatch = logAndStartTimer(step);
		final Collection<Gesuch> neuesteVerfuegteGesuche =
			gesuchService.getNeuesteVerfuegtesGesuchProDossierFuerGemeindeUndGesuchsperiode(gesuchsperiode, gemeinde);
		for (Gesuch gesuch : neuesteVerfuegteGesuche) {
			pruefeZahlungenSollFuerGesuch(gesuch, datumLetzteZahlung, zahlungenIstMap);
		}
		logAndStopTimer(stopWatch, step);
	}

	private void pruefeZahlungenSollFuerGesuch(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDateTime datumLetzteZahlung,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap
	) {
		Objects.requireNonNull(gesuch);
		Objects.requireNonNull(datumLetzteZahlung);

		if (gesuch.getStatus() == AntragStatus.NUR_SCHULAMT) {
			return;
		}
		if (gesuch.getTimestampVerfuegt() == null) {
			LOGGER.error("ZAHLUNGSUEBERPRUEFUNG: timestampVerfuegt ist null beim Auszahlen: {} - {}", gesuch.getId(), gesuch.getJahrFallAndGemeindenummer());
			return;
		}
		// Nur Gesuche, die VOR der letzten Zahlung verfuegt wurden, sind relevant
		if (gesuch.getTimestampVerfuegt().isBefore(datumLetzteZahlung)) {
			LocalDate dateAusbezahltBis = datumLetzteZahlung.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
			for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
				pruefeZahlungenSollFuerBetreuung(betreuung, dateAusbezahltBis, zahlungenIstMap);
			}
		}
	}

	private void pruefeZahlungenSollFuerBetreuung(
		@Nonnull Betreuung betreuung,
		@Nonnull LocalDate dateAusbezahltBis,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap
	) {
		// Nur die "gueltige" Betreuung beachten und nur, wenn es KITA oder TAGESFAMILIEN ist
		if (betreuung.isAngebotAuszuzahlen()) {
			if (!betreuung.isGueltig()) {
				// Es gibt eine spätere Verfügung, deren Gesuch aber noch nicht (komplett) verfügt ist
				Optional<Betreuung> gueltigeBetreuungOptional =
				betreuungService.findSameBetreuungInDifferentGesuchsperiode(
					betreuung.extractGesuchsperiode(),
					betreuung.extractGesuch().getDossier(),
					betreuung.getBetreuungNummer(),
					betreuung.getKind().getKindNummer());
				if (gueltigeBetreuungOptional.isPresent()) {
					betreuung = gueltigeBetreuungOptional.get();
				} else {
					if (betreuung.getBetreuungsstatus() == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG) {
						LOGGER.warn("ZAHLUNGSUEBERPRUEFUNG: Die Betreuung war neu im letzten Antrag, wurde aber ohne Verfuegung geschlossen: {}",
							betreuung.getBGNummer());
					} else {
						potentielleFehlerList.add("Keine gueltige Betreuung gefunden fuer BG "
							+ betreuung.getBGNummer());
					}
				}
			}
			// Jetzt kann es immer noch sein, dass es zwar die gueltige Verfuegung, aber mit NICHT_EINTRETEN ist
			if (betreuung.getBetreuungsstatus() != Betreuungsstatus.NICHT_EINGETRETEN) {
				vergleicheSollIst(betreuung, dateAusbezahltBis, zahlungenIstMap);
			}
		}
	}

	private void vergleicheSollIst(
		@Nonnull Betreuung betreuung,
		@Nonnull LocalDate dateAusbezahltBis,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap
	) {
		BigDecimal betragSoll = getBetragSoll(betreuung, dateAusbezahltBis);
		BigDecimal betragIst = getBetragIst(betreuung, zahlungenIstMap);

		if (!isSame(betragSoll, betragIst)) {
			List<VerfuegungZeitabschnitt> ausbezahlteAbschnitte = getAusbezahlteZeitabschnitte(betreuung, dateAusbezahltBis);
			logPossibleError(betreuung, ausbezahlteAbschnitte, betragSoll, betragIst, zahlungenIstMap);
		}
	}

	private void logPossibleError(
		@Nonnull Betreuung betreuung,
		@Nonnull List<VerfuegungZeitabschnitt> ausbezahlteAbschnitte,
		@Nonnull BigDecimal betragSoll,
		@Nonnull BigDecimal betragIst,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap
	) {
		StringBuilder sb = new StringBuilder();
		BigDecimal differenz = DEFAULT.subtract(betragIst, betragSoll);
		sb.append("Soll und Ist nicht identisch: ").append(betreuung.getBGNummer()).append(" Soll: ").append(betragSoll).append(" Ist: ").append
			(betragIst).append('\n').append(" Differenz: ").append(differenz).append('\n');
		sb.append("Aktuell gueltige Betreuung: ").append(betreuung.getId()).append('\n');
		sb.append("Vergangene Zeitabschnitte").append('\n');
		ausbezahlteAbschnitte.sort(Comparator.comparing(o -> o.getGueltigkeit().getGueltigAb()));
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : ausbezahlteAbschnitte) {
			sb.append(verfuegungZeitabschnitt.getGueltigkeit().toRangeString()).append(", ");
			sb.append(zahlungslaufHelper.getAuszahlungsbetrag(verfuegungZeitabschnitt)).append(", ");
			sb.append(zahlungslaufHelper.getZahlungsstatus(verfuegungZeitabschnitt)).append('\n');
		}
		sb.append("Zahlungspositionen: \n");
		List<Zahlungsposition> zahlungspositions = zahlungenIstMap.get(betreuung.getBGNummer());
		if (zahlungspositions != null) {
			zahlungspositions.sort(Comparator.comparing(o -> o.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigAb()));
			for (Zahlungsposition zahlungsposition : zahlungspositions) {
				sb.append(zahlungsposition.getVerfuegungZeitabschnitt().getGueltigkeit().toRangeString()).append(", ");
				String trennzeichen = ", \t";
				sb.append(zahlungsposition.getBetrag()).append(trennzeichen);
				sb.append(zahlungsposition.getStatus()).append(trennzeichen);
				sb.append(zahlungslaufHelper.getZahlungsstatus(zahlungsposition.getVerfuegungZeitabschnitt())).append(trennzeichen);
				sb.append("Ausbezahlt am: ").append(zahlungsposition.getZahlung().getZahlungsauftrag().getDatumGeneriert()).append(trennzeichen);
				sb.append("ignoriert=").append(zahlungsposition.isIgnoriert()).append('\n');
			}
		}
		potentielleFehlerList.add(sb.toString());
		potenzielleFehlerListZusammenfassung.add(betreuung.getBGNummer() + ": " + differenz);
		LOGGER.warn("ZAHLUNGSUEBERPRUEFUNG: " + sb.toString());
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> getAusbezahlteZeitabschnitte(@Nonnull Betreuung betreuung, @Nonnull LocalDate dateAusbezahltBis) {
		List<VerfuegungZeitabschnitt> ausbezahlteAbschnitte = new ArrayList<>();
		if (betreuung.getVerfuegung() != null) {
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : betreuung.getVerfuegung().getZeitabschnitte()) {
				if (!verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(dateAusbezahltBis)) {
					// Dieser Zeitabschnitt muesste ausbezahlt sein
					ausbezahlteAbschnitte.add(verfuegungZeitabschnitt);
				}
			}
		}
		return ausbezahlteAbschnitte;
	}

	@Nonnull
	private BigDecimal getBetragSoll(@Nonnull Betreuung betreuung, @Nonnull LocalDate dateAusbezahltBis) {
		BigDecimal betragSoll = BigDecimal.ZERO;
		if (betreuung.getVerfuegung() != null) {
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : betreuung.getVerfuegung().getZeitabschnitte()) {
				if (zahlungslaufHelper.isAuszuzahlen(verfuegungZeitabschnitt, betreuung.extractGesuch())) {
					if (!verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(dateAusbezahltBis)) {
						// Dieser Zeitabschnitt muesste ausbezahlt sein
						betragSoll = DEFAULT.add(betragSoll, zahlungslaufHelper.getAuszahlungsbetrag(verfuegungZeitabschnitt));
					}
				}
			}
		}
		return betragSoll;
	}

	@Nonnull
	private BigDecimal getBetragIst(@Nonnull Betreuung betreuung, @Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap) {
		BigDecimal betragIst = BigDecimal.ZERO;
		if (zahlungenIstMap.containsKey(betreuung.getBGNummer())) {
			List<Zahlungsposition> zahlungspositionList = zahlungenIstMap.get(betreuung.getBGNummer());
			for (Zahlungsposition zahlungsposition : zahlungspositionList) {
				betragIst = DEFAULT.add(betragIst, zahlungsposition.getBetrag());
			}
		}
		return betragIst;
	}

	@Nonnull
	private Map<String, List<Zahlungsposition>> pruefeZahlungenIst(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		Map<String, List<Zahlungsposition>> zahlungenIst = new HashMap<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsposition> query = cb.createQuery(Zahlungsposition.class);

		Root<Zahlungsposition> root = query.from(Zahlungsposition.class);
		Join<Zahlungsposition, Zahlung> joinZahlung = root.join(Zahlungsposition_.zahlung);
		Join<Zahlung, Zahlungsauftrag> joinZahlungsauftrag = joinZahlung.join(Zahlung_.zahlungsauftrag);
		Join<Zahlungsposition, VerfuegungZeitabschnitt> joinZeitabschnitt = root.join(Zahlungsposition_.verfuegungZeitabschnitt);

		Predicate predicateGemeinde = cb.equal(joinZahlungsauftrag.get(Zahlungsauftrag_.gemeinde), gemeinde);
		Predicate predicateAuftragTyp = cb.equal(joinZahlungsauftrag.get(Zahlungsauftrag_.zahlungslaufTyp), zahlungslaufHelper.getZahlungslaufTyp());

		Predicate predicateStart = cb.greaterThanOrEqualTo(
			joinZeitabschnitt.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			gesuchsperiode.getGueltigkeit().getGueltigAb());
		Predicate predicateEnd = cb.lessThanOrEqualTo(
			joinZeitabschnitt.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			gesuchsperiode.getGueltigkeit().getGueltigBis());

		query.where(predicateGemeinde, predicateAuftragTyp, predicateStart, predicateEnd);
		Collection<Zahlungsposition> zahlungspositionList = persistence.getCriteriaResults(query);

		for (Zahlungsposition zahlungsposition : zahlungspositionList) {
			addToZahlungenList(zahlungenIst, zahlungsposition);
		}
		return zahlungenIst;
	}

	private void addToZahlungenList(
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIst,
		@Nonnull Zahlungsposition zahlungsposition
	) {
		Objects.requireNonNull(zahlungsposition.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung());
		String key = zahlungsposition.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung().getBGNummer();
		if (!zahlungenIst.containsKey(key)) {
			zahlungenIst.put(key, new ArrayList<>());
		}
		zahlungenIst.get(key).add(zahlungsposition);
	}

	private void resetAllData() {
		this.potentielleFehlerList = new ArrayList<>();
		this.potenzielleFehlerListZusammenfassung = new ArrayList<>();
	}

	@Nonnull
	private StopWatch logAndStartTimer(@Nonnull String info) {
		LOGGER.info("ZAHLUNGSUEBERPRUEFUNG: Starting Step '{}'", info);
		return StopWatch.createStarted();
	}

	private void logAndStopTimer(@Nonnull StopWatch stopWatch, @Nonnull String info) {
		stopWatch.stop();
		final long millis = stopWatch.getTime(TimeUnit.MILLISECONDS);
		String timeInHHMMSS = DurationFormatUtils.formatDuration(millis, "HH:mm:ss.SSS", true);
		LOGGER.info("ZAHLUNGSUEBERPRUEFUNG: Step '{}' took {}", info, timeInHHMMSS);
	}
}
