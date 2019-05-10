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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
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
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static ch.dvbern.ebegu.util.MathUtil.DEFAULT;
import static ch.dvbern.ebegu.util.MathUtil.isSame;

/**
 * Test-Service fuer Zahlungen. Es wird fuer alle Faelle die letzt gueltige Verfuegung verglichen mit den tatsaechlich erfolgten
 * Zahlungen.
 * Einige Gesuche haben bekanntermassen falsche Auszahlungen gehabt. Diese werden entsprechend behandelt.
 */
@Stateful
@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "SpringAutowiredFieldsWarningInspection", "InstanceMethodNamingConvention" })
public class ZahlungUeberpruefungServiceBean extends AbstractBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZahlungUeberpruefungServiceBean.class.getSimpleName());


	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private DossierService dossierService;

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


	private Map<String, List<Zahlungsposition>> zahlungenIstMap = null;
	private final List<String> potentielleFehlerList = new ArrayList<>();
	private final List<String> potenzielleFehlerListZusammenfassung = new ArrayList<>();


	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@TransactionTimeout(value = 360, unit = TimeUnit.MINUTES)
	public void pruefungZahlungen(@Nonnull Gemeinde gemeinde, @Nonnull LocalDateTime datumLetzteZahlung) {
		LOGGER.info("Pruefe Zahlungen fuer Gemeinde {}", gemeinde.getName());
		zahlungenIstMap = pruefeZahlungenIst(gemeinde);
		// Alle Gesuchsperioden im Status AKTIV und INAKTIV muessen geprueft werden, da auch rueckwirkend Korrekturen gemacht werden koennen.
		Collection<Gesuchsperiode> aktiveGesuchsperioden = gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden();
		for (Gesuchsperiode gesuchsperiode : aktiveGesuchsperioden) {
			pruefungZahlungenSollFuerGesuchsperiode(gesuchsperiode, gemeinde, datumLetzteZahlung);
		}
		LOGGER.info("Pruefung der Zahlungen beendet: {}", potentielleFehlerList.isEmpty() ? "OK" : "ERROR");
		sendeMail();
	}

	private void sendeMail() {
		LOGGER.info("Sende Mail...");
		String administratorMail = ebeguConfiguration.getAdministratorMail();
		if (StringUtils.isEmpty(administratorMail)) {
			LOGGER.warn("Es ist keine Administrator-Email konfiguriert. Sende keine E-Mail ueber den Zahlungspruefungs-Status");
			return;
		}
		try {
			final String serverName = ebeguConfiguration.getHostname();
			if (potentielleFehlerList.isEmpty()) {
					mailService.sendMessage("Zahlungslauf: Keine Fehler gefunden (" + serverName + ')',
						"Keine Fehler gefunden", administratorMail);
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
				mailService.sendMessage("Potentieller Fehler im Zahlungslauf (" + serverName + ')',
					sb.toString(), administratorMail);
			}
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(e, "Senden der Mail nicht erfolgreich", "");
		}
		LOGGER.info("... beendet");
	}

	private void pruefungZahlungenSollFuerGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde, @Nonnull LocalDateTime datumLetzteZahlung) {
		LOGGER.info("Pruefe Gesuchsperiode {}", gesuchsperiode.toString());
		Collection<Dossier> allDossiers = dossierService.findDossiersByGemeinde(gemeinde.getId());
		for (Dossier dossier : allDossiers) {
			Optional<Gesuch> gesuchOptional = gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuchsperiode, dossier, false);
			gesuchOptional.ifPresent(gesuch -> pruefeZahlungenSollFuerGesuch(gesuch, datumLetzteZahlung));
		}
		LOGGER.info("... done");
	}

	private void pruefeZahlungenSollFuerGesuch(@Nonnull Gesuch gesuch, @Nonnull LocalDateTime datumLetzteZahlung) {
		if (gesuch.getStatus() == AntragStatus.NUR_SCHULAMT) {
			return;
		}
		if (gesuch.getTimestampVerfuegt() == null) {
			LOGGER.error("timestampVerfuegt ist null beim Auszahlen: {} - {}", gesuch.getId(), gesuch.getJahrFallAndGemeindenummer());
			return;
		}
		// Nur Gesuche, die VOR der letzten Zahlung verfuegt wurden, sind relevant
		if (gesuch.getTimestampVerfuegt().isBefore(datumLetzteZahlung)) {
			LocalDate dateAusbezahltBis = datumLetzteZahlung.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
			for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
				pruefeZahlungenSollFuerBetreuung(betreuung, dateAusbezahltBis);
			}
		}
	}

	private void pruefeZahlungenSollFuerBetreuung(@Nonnull Betreuung betreuung, @Nonnull LocalDate dateAusbezahltBis) {
		// Nur die "gueltige" Betreuung beachten und nur, wenn es KITA ist
		if (betreuung.isAngebotKita()) {
			if (!betreuung.isGueltig()) {
				// Es gibt eine spätere Verfügung, deren Gesuch aber noch nicht (komplett) verfügt ist
				Optional<Betreuung> gueltigeBetreuungOptional = betreuungService.findGueltigeBetreuungByBGNummer(betreuung.getBGNummer());
				if (gueltigeBetreuungOptional.isPresent()) {
					betreuung = gueltigeBetreuungOptional.get();
				}
			}
			// Jetzt kann es immer noch sein, dass es zwar die gueltige Verfuegung, aber mit NICHT_EINTRETEN ist
			if (betreuung.getBetreuungsstatus() != Betreuungsstatus.NICHT_EINGETRETEN) {
				vergleicheSollIst(betreuung, dateAusbezahltBis);
			}
		}
	}

	private void vergleicheSollIst(@Nonnull Betreuung betreuung, @Nonnull LocalDate dateAusbezahltBis) {
		BigDecimal betragSoll = getBetragSoll(betreuung, dateAusbezahltBis);
		BigDecimal betragIst = getBetragIst(betreuung);

		if (!isSame(betragSoll, betragIst)) {
			List<VerfuegungZeitabschnitt> ausbezahlteAbschnitte = getAusbezahlteZeitabschnitte(betreuung, dateAusbezahltBis);
			logPossibleError(betreuung, ausbezahlteAbschnitte, betragSoll, betragIst);
		}
	}

	private void logPossibleError(@Nonnull Betreuung betreuung, List<VerfuegungZeitabschnitt>
		ausbezahlteAbschnitte, BigDecimal betragSoll, BigDecimal betragIst) {

		StringBuilder sb = new StringBuilder();
		BigDecimal differenz = DEFAULT.subtract(betragIst, betragSoll);
		sb.append("Soll und Ist nicht identisch: ").append(betreuung.getBGNummer()).append(" Soll: ").append(betragSoll).append(" Ist: ").append
			(betragIst).append('\n').append(" Differenz: ").append(differenz).append('\n');
		sb.append("Aktuell gueltige Betreuung: ").append(betreuung.getId()).append('\n');
		sb.append("Vergangene Zeitabschnitte").append('\n');
		ausbezahlteAbschnitte.sort(Comparator.comparing(o -> o.getGueltigkeit().getGueltigAb()));
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : ausbezahlteAbschnitte) {
			sb.append(verfuegungZeitabschnitt.getGueltigkeit().toRangeString()).append(", ");
			sb.append(verfuegungZeitabschnitt.getVerguenstigung()).append(", ");
			sb.append(verfuegungZeitabschnitt.getZahlungsstatus()).append('\n');
		}
		sb.append("Zahlungspositionen: \n");
		List<Zahlungsposition> zahlungspositions = zahlungenIstMap.get(betreuung.getBGNummer());
		if (zahlungspositions != null) {
			zahlungspositions.sort(Comparator.comparing(o -> o.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigAb()));
			for (Zahlungsposition zahlungsposition : zahlungspositions) {
				sb.append(zahlungsposition.getVerfuegungZeitabschnitt().getGueltigkeit().toRangeString()).append(", ");
				sb.append(zahlungsposition.getBetrag()).append(", ");
				sb.append(zahlungsposition.getStatus()).append(", ");
				sb.append("Ausbezahlt am: ").append(zahlungsposition.getZahlung().getZahlungsauftrag().getDatumGeneriert()).append(", ");
				sb.append(zahlungsposition.isIgnoriert()).append('\n');
			}
		}
		potentielleFehlerList.add(sb.toString());
		potenzielleFehlerListZusammenfassung.add(betreuung.getBGNummer() + ": " + differenz);
		LOGGER.warn(sb.toString());
	}

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

	private BigDecimal getBetragSoll(@Nonnull Betreuung betreuung, @Nonnull LocalDate dateAusbezahltBis) {
		BigDecimal betragSoll = BigDecimal.ZERO;
		if (betreuung.getVerfuegung() != null) {
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : betreuung.getVerfuegung().getZeitabschnitte()) {
				if (!verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(dateAusbezahltBis)) {
					// Dieser Zeitabschnitt muesste ausbezahlt sein
					betragSoll = DEFAULT.add(betragSoll, verfuegungZeitabschnitt.getVerguenstigung());
				}
			}
		}
		//noinspection ConstantConditions
		return betragSoll;
	}

	private BigDecimal getBetragIst(@Nonnull Betreuung betreuung) {
		BigDecimal betragIst = BigDecimal.ZERO;
		if (zahlungenIstMap.containsKey(betreuung.getBGNummer())) {
			List<Zahlungsposition> zahlungspositionList = zahlungenIstMap.get(betreuung.getBGNummer());
			for (Zahlungsposition zahlungsposition : zahlungspositionList) {
				betragIst = DEFAULT.add(betragIst, zahlungsposition.getBetrag());
			}
		}
		//noinspection ConstantConditions
		return betragIst;
	}

	private Map<String, List<Zahlungsposition>> pruefeZahlungenIst(@Nonnull Gemeinde gemeinde) {
		Map<String, List<Zahlungsposition>> zahlungenIst = new HashMap<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsposition> query = cb.createQuery(Zahlungsposition.class);

		Root<Zahlungsposition> root = query.from(Zahlungsposition.class);
		Join<Zahlungsposition, Zahlung> joinZahlung = root.join(Zahlungsposition_.zahlung);
		Join<Zahlung, Zahlungsauftrag> joinZahlungsauftrag = joinZahlung.join(Zahlung_.zahlungsauftrag);

		Predicate predicate = cb.equal(joinZahlungsauftrag.get(Zahlungsauftrag_.gemeinde), gemeinde);
		query.where(predicate);
		Collection<Zahlungsposition> zahlungspositionList = persistence.getCriteriaResults(query);

		for (Zahlungsposition zahlungsposition : zahlungspositionList) {
			addToZahlungenList(zahlungenIst, zahlungsposition);
		}
		return zahlungenIst;
	}

	private void addToZahlungenList(Map<String, List<Zahlungsposition>> zahlungenIst, Zahlungsposition zahlungsposition) {
		String key = zahlungsposition.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung().getBGNummer();
		if (!zahlungenIst.containsKey(key)) {
			zahlungenIst.put(key, new ArrayList<>());
		}
		zahlungenIst.get(key).add(zahlungsposition);
	}
}
