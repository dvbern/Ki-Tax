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

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.ZahlungenSearchParamsDTO;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.ZahlungslaufUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.*;
import javax.ws.rs.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Zahlungen. Die Zahlungen werden folgendermassen generiert:
 * Wenn ein neuer Zahlungsauftrag erstellt wird, muss nur ein Faelligkeitsdatum mitgegeben werden. Dieses wird *nur*
 * fuer
 * das Abfuellen des XML Files (ISO-20022) verwendet. Fuer die Ermittlung der einzuschliessenden Zahlungsdetail wird
 * immer der aktuelle Timestamp verwendet. Dies, damit wir fuer den naechsten Zahlungsauftrag immer wissen, welche
 * Zahlungen bereits beruecksichtigt wurden.
 * Wir muessen mit 2 Zeitraeumen arbeiten:
 * |      Jan      |      Feb       |
 * |                  |
 * letzter            aktueller
 * Zahlungslauf		Zahlungslauf
 * Für die Ermittlung der "normalen" Zahlungen wird immer (mind.) ein ganzer Monat berücksichtigt, und zwar der aktuelle
 * Monat des Zahlungslaufes plus fruehere Monate, falls in diesen kein Zahlungslauf stattfand.
 * Für die Ermittlung der Korrektur-Zahlungen muessen alle Verfuegungen berücksichtigt werden, welche seit dem letzten
 * Zahlungslauf bis heute dazugekommen sind.
 */
@Stateless
@Local(ZahlungService.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "SpringAutowiredFieldsWarningInspection",
	"InstanceMethodNamingConvention", "PMD.NcssMethodCount" })
public class ZahlungServiceBean extends AbstractBaseService implements ZahlungService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZahlungServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Inject
	private ZahlungUeberpruefungServiceBean zahlungUeberpruefungServiceBean;

	@Inject
	private Authorizer authorizer;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private MandantService mandantService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Override
	@Nonnull
	public Zahlungsauftrag zahlungsauftragErstellen(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung,
		@Nonnull Boolean auszahlungInZukunft,
		@Nonnull Mandant mandant
	) {
		return zahlungsauftragErstellen(
			zahlungslaufTyp,
			gemeindeId,
			datumFaelligkeit,
			beschreibung,
			auszahlungInZukunft,
			LocalDateTime.now(),
			mandant);
	}

	@Override
	@Nonnull
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@TransactionTimeout(value = Constants.MAX_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	public Zahlungsauftrag zahlungsauftragErstellen(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung,
		@Nonnull Boolean auszahlungInZukunft,
		@Nonnull LocalDateTime datumGeneriert,
		@Nonnull Mandant mandant
	) {
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("zahlungsauftragErstellen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));
		authorizer.checkWriteAuthorization(gemeinde);

		// Damit wir spaeter die Auszahlungsdaten valdieren koennen, muessen wir wissen, ob es eine PAIN oder
		// eine INFOMA Zahlung ist. Wir lesen es hier einmalig fuer den ganzen Auftrag.
		ApplicationProperty infomaZahlungen =
			this.applicationPropertyService.readApplicationProperty(
					ApplicationPropertyKey.INFOMA_ZAHLUNGEN,
					mandant)
				.orElse(null);
		boolean isInfomaZahlung = infomaZahlungen != null
			&& Boolean.parseBoolean(infomaZahlungen.getValue());

		// Es darf immer nur ein Zahlungsauftrag im Status ENTWURF sein
		Optional<Zahlungsauftrag> lastZahlungsauftragOptional = findLastZahlungsauftrag(zahlungslaufTyp, gemeinde);
		if (lastZahlungsauftragOptional.isPresent() && lastZahlungsauftragOptional.get().getStatus().isEntwurf()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.DEBUG,
				"zahlungsauftragErstellen",
				ErrorCodeEnum.ERROR_ZAHLUNG_ERSTELLEN);
		}

		LOGGER.info("Erstelle Zahlungsauftrag mit Faelligkeit: {}", Constants.DATE_FORMATTER.format(datumFaelligkeit));
		Zahlungsauftrag zahlungsauftrag = new Zahlungsauftrag();
		zahlungsauftrag.setZahlungslaufTyp(zahlungslaufTyp);
		zahlungsauftrag.setStatus(ZahlungauftragStatus.ENTWURF);
		zahlungsauftrag.setBeschrieb(beschreibung);
		zahlungsauftrag.setDatumFaellig(datumFaelligkeit);
		zahlungsauftrag.setDatumGeneriert(datumGeneriert);
		zahlungsauftrag.setGemeinde(gemeinde);
		zahlungsauftrag.setMandant(mandant);

		// Alle aktuellen (d.h. der letzte Antrag jedes Falles) Verfuegungen suchen, welche ein Kita-Angebot haben
		// Wir brauchen folgende Daten:
		// - Zeitraum, welcher fuer die (normale) Auszahlung gilt: Immer ganzer Monat, mindestens der Monat des
		// DatumFaellig,
		// 		jedoch seit Ende Monat des letzten Auftrags -> 1 oder mehrere ganze Monate
		// - Zeitraum, welcher fuer die Berechnung der rueckwirkenden Korrekturen gilt: Zeitpunkt der letzten
		// Zahlungserstellung bis aktueller Zeitpunkt
		// 		(Achtung: Es ist *nicht* das Faelligkeitsdatum relevant, sondern das Erstellungsdatum des letzten
		// 		Auftrags!)
		// Den letzten Zahlungsauftrag lesen
		LocalDateTime lastZahlungErstellt = Constants.START_OF_DATETIME; // Default, falls dies der erste Auftrag ist
		// Auf dem Front End gibt es z.B. bei Luzern eine Checkbox, die definiert, ob der Folgemonat auch ausbezahlt werden soll.
		// Falls die Checkbox aktiv ist, wird der Folgemonat auch ausbezahlt. z.B. ausloesen am 15.8. ergibt eine
		// Zahlung bis 30.09.
		int anzahlMonateInZukunft = auszahlungInZukunft ? 1 : 0;

		// Falls es eine Wiederholung des Auftrags ist, muessen nur noch die Korrekturen beruecksichtigt werden, welche
		// seit dem letzten Auftrag erstellt wurden
		boolean isRepetition = false;
		// Falls fuer denselben Zeitraum (oder den letzten Teil davon) schon ein Auftrag vorhanden ist, kann das
		// DatumVon nicht
		// einfach an den letzten Auftrag anschliessen
		LocalDate zeitabschnittVon;
		LocalDate zeitabschnittBis = ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(zahlungsauftrag, anzahlMonateInZukunft);

		if (lastZahlungsauftragOptional.isPresent()) {
			final Zahlungsauftrag lastZahlungsauftrag = lastZahlungsauftragOptional.get();
			lastZahlungErstellt = lastZahlungsauftrag.getDatumGeneriert();
			zeitabschnittVon = ZahlungslaufUtil.ermittleZahlungslaufGueltigVon(zeitabschnittBis, lastZahlungsauftrag);
			isRepetition = ZahlungslaufUtil.isZahlunglaufRepetition(zeitabschnittBis, lastZahlungsauftrag);
		} else {
			zeitabschnittVon = Constants.START_OF_DATETIME.toLocalDate(); // Default, falls dies der erste Auftrag ist
		}

		zahlungsauftrag.setGueltigkeit(new DateRange(zeitabschnittVon, zeitabschnittBis));

		if (!zahlungsauftrag.getGueltigkeit().isValid()) {
			throw new EbeguRuntimeException(
				"zahlungsauftragErstellen",
				ErrorCodeEnum.ERROR_ZAHLUNGSAUFTRAG_GENERIERT_BEFORE_LAST_ZAHLUNGSAUFTRAG_GENERIERT,
				datumGeneriert.toLocalDate(),
				zeitabschnittVon
			);
		}

		Map<String, Zahlung> zahlungProInstitution = new HashMap<>();
		ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);

		// "Normale" Zahlungen
		if (!isRepetition) {
			LOGGER.info("Ermittle normale Zahlungen im Zeitraum {}", zahlungsauftrag.getGueltigkeit().toRangeString());
			Collection<VerfuegungZeitabschnitt> gueltigeVerfuegungZeitabschnitte =
				getGueltigeVerfuegungZeitabschnitte(gemeinde, zeitabschnittVon,
					zeitabschnittBis);
			for (VerfuegungZeitabschnitt zeitabschnitt : gueltigeVerfuegungZeitabschnitte) {
				if (zahlungslaufHelper.isAuszuzahlen(zeitabschnitt) && zahlungslaufHelper.getZahlungsstatus(
					zeitabschnitt).isNeu()) {
					createZahlungsposition(
						zahlungslaufHelper,
						zeitabschnitt,
						zahlungsauftrag,
						zahlungProInstitution,
						isInfomaZahlung);
				}
			}
		}
		// Korrekturen und Nachzahlungen
		// Stichtag: Falls es eine Wiederholung des Auftrags ist, wurde der aktuelle Monat bereits ausbezahlt.
		LocalDate stichtagKorrekturen =
			isRepetition ? zeitabschnittBis : zeitabschnittBis.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		// Die Korrekturzahlungen werden seit dem letzten Zahlungsauftrag beruecksichtigt. Falls wir im TEST-Mode sind
		// und ein fiktives "DatumGeneriert" gewaehlt haben, nehmen wir als Datum des letzten Auftrags das
		// timestampErstellt
		// und nicht das (eventuell ebenfalls fiktive) datumGeneriert.
		boolean isTestMode = ebeguConfiguration.getIsZahlungenTestMode();
		if (isTestMode) {
			lastZahlungErstellt = Constants.START_OF_DATETIME;
		}
		Collection<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte =
			getVerfuegungsZeitabschnitteNachVerfuegungDatum(gemeinde, lastZahlungErstellt,
				zahlungsauftrag.getDatumGeneriert(), stichtagKorrekturen);
		for (VerfuegungZeitabschnitt zeitabschnitt : verfuegungsZeitabschnitte) {
			// Zu behandeln sind alle, die NEU, VERRECHNEND oder IGNORIEREND sind
			if (zahlungslaufHelper.isAuszuzahlen(zeitabschnitt)
				&& zahlungslaufHelper.getZahlungsstatus(zeitabschnitt).isZuBehandelnInZahlungslauf()) {
				createZahlungspositionenKorrekturUndNachzahlung(
					zahlungslaufHelper,
					zeitabschnitt,
					zahlungsauftrag,
					zahlungProInstitution,
					isInfomaZahlung);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Zahlungsauftrag generiert: ").append(zahlungsauftrag.getGueltigkeit().toRangeString());
		if (isRepetition) {
			sb.append(" [Repetition]");
		}
		LOGGER.info(sb.toString());
		calculateZahlungsauftrag(zahlungsauftrag);
		Zahlungsauftrag persistedAuftrag = persistence.merge(zahlungsauftrag);

		return persistedAuftrag;
	}

	/**
	 * Zahlungsauftrag wird einmalig berechnet. Danach koennen nur noch die Stammdaten der Institutionen
	 * geaendert werden.
	 */
	private void calculateZahlungsauftrag(Zahlungsauftrag zahlungsauftrag) {
		BigDecimal totalAuftrag = BigDecimal.ZERO;
		boolean hasAnyZahlungWithNegativTotal = false;
		for (Zahlung zahlung : zahlungsauftrag.getZahlungen()) {
			BigDecimal totalZahlung = BigDecimal.ZERO;
			for (Zahlungsposition zahlungsposition : zahlung.getZahlungspositionen()) {
				if (!zahlungsposition.isIgnoriert()) {
					totalZahlung = MathUtil.DEFAULT.add(totalZahlung, zahlungsposition.getBetrag());
				}
			}
			if (MathUtil.isNegative(totalZahlung)) {
				hasAnyZahlungWithNegativTotal = true;
			}
			zahlung.setBetragTotalZahlung(totalZahlung);
			totalAuftrag = MathUtil.DEFAULT.add(totalAuftrag, totalZahlung);
		}
		zahlungsauftrag.setHasNegativeZahlungen(hasAnyZahlungWithNegativTotal);
		zahlungsauftrag.setBetragTotalAuftrag(totalAuftrag);
	}

	/**
	 * Ermittelt die aktuell gueltigen Verfuegungszeitabschnitte fuer die normale monatliche Zahlung (keine
	 * Korrekturen).
	 */
	@Nonnull
	private Collection<VerfuegungZeitabschnitt> getGueltigeVerfuegungZeitabschnitte(
		@Nonnull Gemeinde gemeinde, @Nonnull LocalDate zeitabschnittVon,
		@Nonnull LocalDate zeitabschnittBis) {
		requireNonNull(zeitabschnittVon, "zeitabschnittVon muss gesetzt sein");
		requireNonNull(zeitabschnittBis, "zeitabschnittBis muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(Betreuung_.kind);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier);

		List<Predicate> predicates = new ArrayList<>();

		// Datum Von
		Predicate predicateStart = cb.lessThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			zeitabschnittBis);
		predicates.add(predicateStart);
		// Datum Bis
		Predicate predicateEnd = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			zeitabschnittVon);
		predicates.add(predicateEnd);
		// Nur Angebot Betreuungsgutschein
		Predicate predicateAngebot =
			joinBetreuung.get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.betreuungsangebotTyp)
				.in(BetreuungsangebotTyp.getBetreuungsgutscheinTypes());
		predicates.add(predicateAngebot);
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = cb.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicates.add(predicateGueltig);
		// Status der Betreuung muss VERFUEGT sein
		Predicate predicateStatus = joinBetreuung.get(Betreuung_.betreuungsstatus).in(Betreuungsstatus.VERFUEGT);
		predicates.add(predicateStatus);
		// Das Dossier muss der uebergebenen Gemeinde zugeordnet sein
		Predicate predicateGemeinde = cb.equal(joinDossier.get(Dossier_.gemeinde), gemeinde);
		predicates.add(predicateGemeinde);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	/**
	 * Ermittelt alle Zeitabschnitte, welche zu Antraegen gehoeren, die seit dem letzten Zahlungslauf verfuegt wurden.
	 */
	@Nonnull
	private Collection<VerfuegungZeitabschnitt> getVerfuegungsZeitabschnitteNachVerfuegungDatum(
		@Nonnull Gemeinde gemeinde,
		@Nonnull LocalDateTime datumVerfuegtVon,
		@Nonnull LocalDateTime datumVerfuegtBis,
		@Nonnull LocalDate zeitabschnittBis
	) {
		requireNonNull(datumVerfuegtVon, "datumVerfuegtVon muss gesetzt sein");
		requireNonNull(datumVerfuegtBis, "datumVerfuegtBis muss gesetzt sein");
		requireNonNull(zeitabschnittBis, "zeitabschnittBis muss gesetzt sein");

		LOGGER.info("Ermittle Korrekturzahlungen:");
		LOGGER.info("Zeitabschnitt endet vor: {}", Constants.DATE_FORMATTER.format(zeitabschnittBis));
		LOGGER.info(
			"Gesuch verfuegt zwischen: {} - {}",
			Constants.DATE_FORMATTER.format(datumVerfuegtVon),
			Constants.DATE_FORMATTER.format(datumVerfuegtBis));

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(Betreuung_.kind);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier);

		List<Predicate> predicates = new ArrayList<>();

		// Datum Bis muss VOR dem regulaeren Auszahlungszeitraum sein (sonst ist es keine Korrektur und schon im
		// obigen Statement enthalten)
		Predicate predicateStart = cb.lessThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			zeitabschnittBis);
		predicates.add(predicateStart);
		// Nur Angebot KITA und TAGESFAMILIEN

		Predicate predicateAngebot =
			joinBetreuung.get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.betreuungsangebotTyp)
				.in(BetreuungsangebotTyp.KITA, BetreuungsangebotTyp.TAGESFAMILIEN);
		predicates.add(predicateAngebot);
		// Gesuche, welche seit dem letzten Zahlungslauf verfuegt wurden. Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateDatum = cb.between(
			joinGesuch.get(Gesuch_.timestampVerfuegt),
			cb.literal(datumVerfuegtVon),
			cb.literal(datumVerfuegtBis));
		predicates.add(predicateDatum);
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = cb.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicates.add(predicateGueltig);
		// Status der Betreuung muss VERFUEGT sein
		Predicate predicateStatus = joinBetreuung.get(Betreuung_.betreuungsstatus).in(Betreuungsstatus.VERFUEGT);
		predicates.add(predicateStatus);
		// Das Dossier muss der uebergebenen Gemeinde zugeordnet sein
		Predicate predicateGemeinde = cb.equal(joinDossier.get(Dossier_.gemeinde), gemeinde);
		predicates.add(predicateGemeinde);

		query.orderBy(cb.asc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	/**
	 * Erstellt eine Zahlungsposition fuer den uebergebenen Zeitabschnitt. Normalfall bei "Erstbuchung"
	 */
	private void createZahlungsposition(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution,
		boolean isInfomaZahlung) {
		Zahlungsposition zahlungsposition = new Zahlungsposition();
		zahlungsposition.setVerfuegungZeitabschnitt(zeitabschnitt);
		zahlungsposition.setBetrag(helper.getAuszahlungsbetrag(zeitabschnitt));
		zahlungsposition.setStatus(ZahlungspositionStatus.NORMAL);
		Zahlung zahlung = findZahlungForEmpfaengerOrCreate(
			helper,
			zeitabschnitt,
			zahlungsauftrag,
			zahlungProInstitution,
			isInfomaZahlung);
		zahlungsposition.setZahlung(zahlung);
		zahlung.getZahlungspositionen().add(zahlungsposition);
		helper.setZahlungsstatus(zeitabschnitt, getZahluntsstatusVerrechnet(zeitabschnitt));
	}

	private VerfuegungsZeitabschnittZahlungsstatus getZahluntsstatusVerrechnet(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.hasBetreuungspensum() ?
			VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET :
			VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET_KEINE_BETREUUNG;
	}

	/**
	 * Erstellt alle notwendigen Zahlungspositionen fuer die Korrektur des uebergebenen Zeitabschnitts.
	 * Bisherige Positionen werden in Abzug gebracht und die neuen hinzugefuegt
	 */
	private void createZahlungspositionenKorrekturUndNachzahlung(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution,
		boolean isInfomaZahlung
	) {
		// Ermitteln, ob die Vollkosten geaendert haben, seit der letzten Verfuegung, die auch verrechnet wurde!
		List<VerfuegungZeitabschnitt> zeitabschnittOnVorgaengerVerfuegung = new ArrayList<>();
		final Verfuegung verfuegung = zeitabschnittNeu.getVerfuegung();
		Objects.requireNonNull(verfuegung);
		Objects.requireNonNull(verfuegung.getBetreuung());

		verfuegungService.findVerrechnetenOrIgnoriertenZeitabschnittOnVorgaengerVerfuegung(
			helper.getZahlungslaufTyp(),
			zeitabschnittNeu,
			verfuegung.getBetreuung(),
			zeitabschnittOnVorgaengerVerfuegung);

		// Korrekturen
		if (!zeitabschnittOnVorgaengerVerfuegung.isEmpty()) {
			Zahlung zahlung = findZahlungForEmpfaengerOrCreate(
				helper, zeitabschnittNeu, zahlungsauftrag, zahlungProInstitution, isInfomaZahlung);
			createZahlungspositionKorrekturNeuerWert(helper, zeitabschnittNeu, zahlung); // Dies braucht man immer
			for (VerfuegungZeitabschnitt vorgaengerZeitabschnitt : zeitabschnittOnVorgaengerVerfuegung) {
				// Fuer die "alten" Verfuegungszeitabschnitte muessen Korrekturbuchungen erstellt werden
				// Wenn die neuen Zeitabschnitte ignoriert sind, setzen wir die alten Zeitabschnitte auch als ignoriert
				// wir muessen noch aufpassen, das dieser Korrektur nicht schon drin ist falls man einen Monat
				// in mehrer Zeitabschnitten migriert haben.
				boolean alreadyCorrected = false;
				for (Zahlungsposition zahlungsposition : zahlung.getZahlungspositionen()) {
					if (zahlungsposition.getVerfuegungZeitabschnitt().equals(vorgaengerZeitabschnitt)) {
						alreadyCorrected = true;
						break;
					}
				}
				if (!alreadyCorrected) {
					createZahlungspositionKorrekturAlterWert(helper, vorgaengerZeitabschnitt, zahlung,
						helper.getZahlungsstatus(zeitabschnittNeu).isIgnoriertIgnorierend());
				}
			}
		} else { // Nachzahlungen bzw. Erstgesuche die rueckwirkend ausbezahlt werden muessen
			createZahlungsposition(
				helper,
				zeitabschnittNeu,
				zahlungsauftrag, zahlungProInstitution, isInfomaZahlung);
		}
	}

	private Zahlung findZahlungForEmpfaengerOrCreate(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution,
		boolean isInfomaZahlung
	) {
		final Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		Objects.requireNonNull(betreuung);
		if (helper.getZahlungslaufTyp().equals(ZahlungslaufTyp.GEMEINDE_INSTITUTION)) {
			return findZahlungForInstitutionOrCreate(betreuung, zahlungsauftrag, zahlungProInstitution, isInfomaZahlung);
		}

		Gesuch letztesGueltigesGesuch = betreuung.extractGesuch();
		if (!letztesGueltigesGesuch.isGueltig() && !letztesGueltigesGesuch.getStatus().equals(AntragStatus.VERFUEGEN)) {
			letztesGueltigesGesuch = gesuchService
				.getNeustesVerfuegtesGesuchFuerGesuch(
					letztesGueltigesGesuch.getGesuchsperiode(),
					letztesGueltigesGesuch.getDossier(),
					false)
				.orElseThrow(() ->
					new EbeguRuntimeException(
						"createZahlungsposition",
						"Zahlungposition hat keine gueltige Gesuch")
				);
		}

		Auszahlungsdaten auszahlungsdaten = getAuszahlungsdatenFromGesuchOrBetreuung(letztesGueltigesGesuch, betreuung);

		// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
		if (auszahlungsdaten == null || !auszahlungsdaten.isZahlungsinformationValid(isInfomaZahlung)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				"createZahlung",
				ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_ANTRAGSTELLER_INCOMPLETE,
				letztesGueltigesGesuch.getJahrFallAndGemeindenummer());
		}

		return findZahlungForAntragstellerOrCreate(
			letztesGueltigesGesuch,
			betreuung,
			zahlungsauftrag,
			zahlungProInstitution,
			auszahlungsdaten
		);

	}

	private Auszahlungsdaten getAuszahlungsdatenFromGesuchOrBetreuung(Gesuch gesuch, Betreuung betreuung) {
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation, "Die Familiensituation muessen zu diesem Zeitpunkt definiert sein");

		//Zuerste werden immer die Auszahlungsdaten aus dem Gesuch bevorzugt
		Auszahlungsdaten auszahlungsdaten = familiensituation.getAuszahlungsdaten();

		if (auszahlungsdaten == null) {
			// Falls auf dem Gesuch keine Auszahlungsdaten vorhanden sind, werden die Auszahlungsdaten von der Betreuung
			// genommen.
			// Beim Gesuch handelt es sich um das Aktuell gültige Gesuch, bei der Betreuung um die Betruung die Ausbezahltw
			// werden soll.
			// Es gibt Fälle auf welchen beim gültigen Gesuch keine Auszahlungsdaten vorhanden sind
			// -> Mutation 1 MZV gewünscht, Auszahlungsdaten vorhanden auf Betreuung (diese soll ausbezahlt werden)
			// -> Mutation 2 MZV nicht mehr gewünscht, keine Auszahlungdaten mehr auf dem gültigen Gesuch vorhanden,
			// also nehmen wir die Daten aus der Betreuung
			Familiensituation familiensituationBetreuung = betreuung.extractGesuch().extractFamiliensituation();
			Objects.requireNonNull(
				familiensituationBetreuung,
				"Die Familiensituation muessen zu diesem Zeitpunkt definiert sein");
			auszahlungsdaten = familiensituationBetreuung.getAuszahlungsdaten();
		}

		return auszahlungsdaten;
	}

	private Zahlung findZahlungForAntragstellerOrCreate(
		@Nonnull Gesuch letztesGueltigesGesuch,
		@Nonnull Betreuung betreuung,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution,
		@Nonnull Auszahlungsdaten auszahlungsdaten
	) {
		// Wir setzen als "Empfaenger-ID" die ID des Falles: In selben Zahlungslauf kann es zu Auszahlungen
		// von mehreren Mutation derselben Familie kommen, daher waere die Gesuch-ID oder die Gesuchsteller-ID
		// nicht geeignet. Da auch Korrekturzahlungen ueber die Periode hinaus moeglich sind, faellt auch
		// die Dossier-ID weg.
		String fallId = letztesGueltigesGesuch.getDossier().getFall().getId();
		if (zahlungProInstitution.containsKey(fallId)) {
			return zahlungProInstitution.get(fallId);
		}
		// Es gibt noch keine Zahlung fuer diesen Empfaenger, wir erstellen eine Neue
		Zahlung zahlung =
			createZahlungForAntragsteller(
				letztesGueltigesGesuch,
				betreuung.getBetreuungsangebotTyp(),
				fallId,
				zahlungsauftrag,
				auszahlungsdaten);
		zahlungProInstitution.put(fallId, zahlung);
		return zahlung;
	}

	@Nonnull
	private Zahlung createZahlungForAntragsteller(
		@Nonnull Gesuch letztesGueltigesGesuch,
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull String fallId,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Auszahlungsdaten auszahlungsdaten
	) {
		final Gesuchsteller gesuchsteller1 = letztesGueltigesGesuch.extractGesuchsteller1()
			.orElseThrow(() -> new EbeguRuntimeException(
				"createZahlung",
				"GS1 not found for Gesuch " + letztesGueltigesGesuch.getId()));
		Gesuchsteller gesuchsteller2 = null;

		if (letztesGueltigesGesuch.getGesuchsteller2() != null) {
			gesuchsteller2 = letztesGueltigesGesuch.getGesuchsteller2().getGesuchstellerJA();
		}

		Zahlung zahlung = new Zahlung();
		zahlung.setStatus(ZahlungStatus.ENTWURF);
		zahlung.setAuszahlungsdaten(auszahlungsdaten);
		zahlung.setEmpfaengerId(fallId);
		zahlung.setEmpfaengerName(gesuchsteller1.getFullName());
		if (gesuchsteller2 != null) {
			zahlung.setEmpfaenger2Name(gesuchsteller2.getFullName());
		}
		zahlung.setBetreuungsangebotTyp(betreuungsangebotTyp);
		zahlung.setZahlungsauftrag(zahlungsauftrag);
		zahlungsauftrag.getZahlungen().add(zahlung);
		return zahlung;
	}

	private Zahlung findZahlungForInstitutionOrCreate(
		@Nonnull Betreuung betreuung,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution,
		boolean isInfomaZahlung
	) {
		InstitutionStammdaten institution = betreuung.getInstitutionStammdaten();
		if (zahlungProInstitution.containsKey(institution.getId())) {
			return zahlungProInstitution.get(institution.getId());
		}
		// Es gibt noch keine Zahlung fuer diesen Empfaenger, wir erstellen eine Neue
		Zahlung zahlung = createZahlungForInstitution(institution, zahlungsauftrag, isInfomaZahlung);
		zahlungProInstitution.put(institution.getId(), zahlung);
		return zahlung;
	}

	@Nonnull
	private Zahlung createZahlungForInstitution(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		boolean isInfomaZahlung
	) {
		Zahlung zahlung = new Zahlung();
		zahlung.setStatus(ZahlungStatus.ENTWURF);
		final InstitutionStammdatenBetreuungsgutscheine stammdatenBG =
			institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine();
		Objects.requireNonNull(stammdatenBG, "Die Stammdaten muessen zu diesem Zeitpunkt definiert sein");
		final Auszahlungsdaten auszahlungsdaten = stammdatenBG.getAuszahlungsdaten();
		// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
		if (auszahlungsdaten == null || !auszahlungsdaten.isZahlungsinformationValid(isInfomaZahlung)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				"createZahlung",
				ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_INSTITUTION_INCOMPLETE,
				institutionStammdaten.getInstitution().getName());
		}

		Objects.requireNonNull(auszahlungsdaten);
		zahlung.setAuszahlungsdaten(auszahlungsdaten);
		zahlung.setEmpfaengerId(institutionStammdaten.getInstitution().getId());
		zahlung.setEmpfaengerName(institutionStammdaten.getInstitution().getName());
		zahlung.setBetreuungsangebotTyp(institutionStammdaten.getBetreuungsangebotTyp());
		if (institutionStammdaten.getInstitution().getTraegerschaft() != null) {
			zahlung.setTraegerschaftName(institutionStammdaten.getInstitution().getTraegerschaft().getName());
		}
		zahlung.setZahlungsauftrag(zahlungsauftrag);
		zahlungsauftrag.getZahlungen().add(zahlung);
		return zahlung;
	}

	/**
	 * Erstellt eine Zahlungsposition fuer eine Korrekturzahlung mit dem *neu gueltigen* Wert
	 */
	private void createZahlungspositionKorrekturNeuerWert(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Zahlung zahlung
	) {
		Zahlungsposition zahlungsposition = new Zahlungsposition();
		zahlungsposition.setVerfuegungZeitabschnitt(zeitabschnittNeu);
		zahlungsposition.setBetrag(helper.getAuszahlungsbetrag(zeitabschnittNeu));
		zahlungsposition.setZahlung(zahlung);
		zahlungsposition.setIgnoriert(helper.getZahlungsstatus(zeitabschnittNeu).isIgnoriertIgnorierend());
		ZahlungspositionStatus status = ZahlungspositionStatus.KORREKTUR;
		zahlungsposition.setStatus(status);
		if (helper.getZahlungsstatus(zeitabschnittNeu).isIgnoriertIgnorierend()) {
			helper.setZahlungsstatus(zeitabschnittNeu, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT);
		} else {
			helper.setZahlungsstatus(zeitabschnittNeu, getZahluntsstatusKorrigiert(zeitabschnittNeu));
		}
		zahlung.getZahlungspositionen().add(zahlungsposition);
	}

	private VerfuegungsZeitabschnittZahlungsstatus getZahluntsstatusKorrigiert(VerfuegungZeitabschnitt zeitabschnittNeu) {
		if (zeitabschnittNeu.getRelevantBgCalculationResult().getBetreuungspensumProzent().compareTo(BigDecimal.ZERO) == 0) {
			return VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET_KEINE_BETREUUNG;
		}

		return VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT;
	}

	/**
	 * Erstellt eine Zahlungsposition fuer eine Korrekturzahlung mit der Korrektur des *alten Wertes* (negiert)
	 */
	private void createZahlungspositionKorrekturAlterWert(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt vorgaengerZeitabschnitt,
		@Nonnull Zahlung zahlung,
		boolean ignoriert
	) {
		Zahlungsposition korrekturPosition = new Zahlungsposition();
		korrekturPosition.setVerfuegungZeitabschnitt(vorgaengerZeitabschnitt);
		korrekturPosition.setBetrag(helper.getAuszahlungsbetrag(vorgaengerZeitabschnitt).negate());
		korrekturPosition.setZahlung(zahlung);
		korrekturPosition.setIgnoriert(ignoriert); // ignoriert kommt vom neuen Zeitabschnitt
		ZahlungspositionStatus status = ZahlungspositionStatus.KORREKTUR;
		korrekturPosition.setStatus(status);
		zahlung.getZahlungspositionen().add(korrekturPosition);
		if (helper.getZahlungsstatus(vorgaengerZeitabschnitt).isIgnoriertIgnorierend()) {
			helper.setZahlungsstatus(
				vorgaengerZeitabschnitt,
				VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT);
		} else {
			helper.setZahlungsstatus(
				vorgaengerZeitabschnitt,
				getZahluntsstatusKorrigiert(vorgaengerZeitabschnitt));
		}
	}

	/**
	 * Ermittelt den zuletzt durchgefuehrten Zahlungsauftrag des entsprechenden Typs
	 */
	@Nonnull
	private Optional<Zahlungsauftrag> findLastZahlungsauftrag(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull Gemeinde gemeinde
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);
		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);

		// Der Zahlungsauftrag muss der uebergebenen Gemeinde zugeordnet sein
		Predicate predicateGemeinde = cb.equal(root.get(Zahlungsauftrag_.gemeinde), gemeinde);
		// und den richtigen Typ haben
		Predicate predicateTyp = cb.equal(root.get(Zahlungsauftrag_.zahlungslaufTyp), zahlungslaufTyp);
		query.where(predicateGemeinde, predicateTyp);

		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Zahlungsauftrag> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (!criteriaResults.isEmpty()) {
			return Optional.of(criteriaResults.get(0));
		}
		return Optional.empty();
	}

	@Override
	@Nonnull
	public Zahlungsauftrag zahlungsauftragAktualisieren(
		@Nonnull String auftragId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung) {
		requireNonNull(auftragId, "auftragId muss gesetzt sein");
		requireNonNull(datumFaelligkeit, "datumFaelligkeit muss gesetzt sein");
		requireNonNull(beschreibung, "beschreibung muss gesetzt sein");

		Zahlungsauftrag auftrag = findZahlungsauftrag(auftragId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"zahlungsauftragAktualisieren", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, auftragId));

		authorizer.checkWriteAuthorizationZahlungsauftrag(auftrag);
		// Auftrag kann nur im Status ENTWURF veraendert werden
		if (auftrag.getStatus().isEntwurf()) {
			auftrag.setBeschrieb(beschreibung);
			auftrag.setDatumFaellig(datumFaelligkeit);
			return persistence.merge(auftrag);
		}
		throw new IllegalStateException("Auftrag kann nicht mehr veraendert werden: " + auftragId);
	}

	@Override
	@Nonnull
	public Zahlungsauftrag zahlungsauftragAusloesen(@Nonnull String auftragId) {
		requireNonNull(auftragId, "auftragId muss gesetzt sein");

		Zahlungsauftrag zahlungsauftrag =
			findZahlungsauftrag(auftragId).orElseThrow(() -> new EbeguEntityNotFoundException(
				"zahlungsauftragAktualisieren", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, auftragId));

		authorizer.checkWriteAuthorizationZahlungsauftrag(zahlungsauftrag);

		zahlungsauftrag.setStatus(ZahlungauftragStatus.AUSGELOEST);
		// Jetzt muss noch das PAIN File erstellt werden. Nach dem Ausloesen kann dieses nicht mehr veraendert werden
		try {
			generatedDokumentService.createZahlungsFiles(zahlungsauftrag);
		} catch (MimeTypeParseException e) {
			throw new IllegalStateException("Pain-File konnte nicht erstellt werden: " + auftragId, e);
		}
		for (Zahlung zahlung : zahlungsauftrag.getZahlungen()) {
			if (ZahlungStatus.ENTWURF != zahlung.getStatus()) {
				throw new IllegalArgumentException(
					"Zahlung muss im Status ENTWURF sein, wenn der Auftrag ausgelöst wird: " + zahlung.getId());
			}
			zahlung.setStatus(ZahlungStatus.AUSGELOEST);
			persistence.merge(zahlung);
		}
		// Die nextBelegnummerInfoma hochzaehlen
		final Mandant mandant = zahlungsauftrag.getMandant();
		Objects.requireNonNull(mandant);
		final boolean infomaZahlungenAktiviert =
			Boolean.TRUE.equals(applicationPropertyService.findApplicationPropertyAsBoolean(
				ApplicationPropertyKey.INFOMA_ZAHLUNGEN,
				mandant));
		if (infomaZahlungenAktiviert) {
			final long oldNextNummer = mandant.getNextInofmaBelegnummer(zahlungsauftrag.getZahlungslaufTyp());
			final int anzahlNeueZahlungen = zahlungsauftrag.getZahlungen().size();
			mandantService.updateNextInfomaBelegnummer(
				mandant,
				zahlungsauftrag.getZahlungslaufTyp(),
				oldNextNummer + anzahlNeueZahlungen);
		}
		return persistence.merge(zahlungsauftrag);
	}

	@Override
	@Nonnull
	public Optional<Zahlungsauftrag> findZahlungsauftrag(@Nonnull String auftragId) {
		requireNonNull(auftragId, "auftragId muss gesetzt sein");
		Zahlungsauftrag zahlungsauftrag = persistence.find(Zahlungsauftrag.class, auftragId);
		authorizer.checkReadAuthorizationZahlungsauftrag(zahlungsauftrag);
		return Optional.ofNullable(zahlungsauftrag);
	}

	@Override
	@Nonnull
	public Optional<Zahlung> findZahlung(@Nonnull String zahlungId) {
		requireNonNull(zahlungId, "zahlungId muss gesetzt sein");
		Zahlung zahlung = persistence.find(Zahlung.class, zahlungId);
		authorizer.checkReadAuthorizationZahlung(zahlung);
		return Optional.ofNullable(zahlung);
	}

	@Override
	@Nonnull
	public Collection<Zahlungsauftrag> getAllZahlungsauftraege(
		@Nonnull ZahlungenSearchParamsDTO zahlungenSearchParamsDTO
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);
		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
		Join<Zahlungsauftrag, Gemeinde> joinGemeinde = root.join(Zahlungsauftrag_.gemeinde);

		List<Predicate> predicates = createPredicatesForZahlungen(zahlungenSearchParamsDTO, cb, root, joinGemeinde);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		query.distinct(true);

		setSortOrder(query, zahlungenSearchParamsDTO, root, joinGemeinde, cb);

		var zahlungen = persistence.getEntityManager()
			.createQuery(query)
			.setFirstResult(zahlungenSearchParamsDTO.getPage() * zahlungenSearchParamsDTO.getPageSize())
			.setMaxResults(zahlungenSearchParamsDTO.getPageSize())
			.getResultList();

		for (var z : zahlungen) {
			authorizer.checkReadAuthorizationZahlungsauftrag(z);
		}
		return zahlungen;
	}

	@Nonnull
	@Override
	public Long countAllZahlungsauftraege(ZahlungenSearchParamsDTO zahlungenSearchParamsDTO) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
		Join<Zahlungsauftrag, Gemeinde> joinGemeinde = root.join(Zahlungsauftrag_.gemeinde);

		query.select(cb.count(root));

		List<Predicate> predicates = createPredicatesForZahlungen(zahlungenSearchParamsDTO, cb, root, joinGemeinde);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		query.distinct(true);

		return persistence.getCriteriaSingleResult(query);
	}

	private List<Predicate> createPredicatesForZahlungen(
		ZahlungenSearchParamsDTO zahlungenSearchParamsDTO,
		CriteriaBuilder cb,
		Root<Zahlungsauftrag> root,
		Join<Zahlungsauftrag, Gemeinde> joinGemeinde) {
		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"createPredicatesForZahlungen", "Non logged in user should never reach this"));

		List<Predicate> predicates = new ArrayList<>();

		Objects.requireNonNull(principalBean.getMandant());
		Predicate mandantPredicate = cb.equal(root.get(Zahlungsauftrag_.mandant), principalBean.getMandant());
		predicates.add(mandantPredicate);

		// general
		if (zahlungenSearchParamsDTO.getGemeinde() != null) {
			predicates.add(cb.equal(root.get(Zahlungsauftrag_.gemeinde), zahlungenSearchParamsDTO.getGemeinde()));
		}
		predicates.add(cb.equal(
			root.get(Zahlungsauftrag_.zahlungslaufTyp),
			zahlungenSearchParamsDTO.getZahlungslaufTyp()));

		// institutionen
		if (currentBenutzer.getCurrentBerechtigung().getRole().isInstitutionRole()) {
			Join<Zahlungsauftrag, Zahlung> joinZahlung = root.join(Zahlungsauftrag_.zahlungen);
			Objects.requireNonNull(zahlungenSearchParamsDTO.getAllowedInstitutionIds());
			List<String> allowedInstitutionenIds = zahlungenSearchParamsDTO.getAllowedInstitutionIds();

			predicates.add(cb.notEqual(root.get(Zahlungsauftrag_.status), ZahlungauftragStatus.ENTWURF));
			predicates.add(joinZahlung.get(Zahlung_.empfaengerId).in(allowedInstitutionenIds));
		}
		// gemeinden
		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Collection<Gemeinde> gemeindenForUser = currentBenutzer.extractGemeindenForUser();
			Predicate inGemeinde = joinGemeinde.in(gemeindenForUser);
			predicates.add(inGemeinde);
		}
		return predicates;
	}

	private void setSortOrder(
		@Nonnull CriteriaQuery<Zahlungsauftrag> query,
		@Nonnull ZahlungenSearchParamsDTO zahlungenSearchParamsDTO,
		@Nonnull Root<Zahlungsauftrag> root,
		@Nonnull Join<Zahlungsauftrag, Gemeinde> joinGemeinde,
		@Nonnull CriteriaBuilder cb
	) {
		if (zahlungenSearchParamsDTO.getSortPredicate() == null) {
			return;
		}

		Expression<?> sortExpression;
		switch (zahlungenSearchParamsDTO.getSortPredicate()) {
		case "datumFaellig":
			sortExpression = root.get(Zahlungsauftrag_.datumFaellig);
			break;
		case "beschrieb":
			sortExpression = root.get(Zahlungsauftrag_.beschrieb);
			break;
		case "datumGeneriert":
			sortExpression = root.get(Zahlungsauftrag_.datumGeneriert);
			break;
		case "gemeinde":
			sortExpression = joinGemeinde.get(Gemeinde_.name);
			break;
		case "betragTotalAuftrag":
			sortExpression = root.get(Zahlungsauftrag_.betragTotalAuftrag);
			break;
		case "status": {
			Benutzer currentBenutzer =
				benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
					"setSortOrder", "Non logged in user should never reach this"));

			if (currentBenutzer.getCurrentBerechtigung().getRole().isInstitutionRole()) {
				sortExpression = createInstitutionStatusSortPredicate(query, zahlungenSearchParamsDTO, root, cb);
			} else {
				sortExpression = root.get(Zahlungsauftrag_.status);
			}
			break;
		}
		default:
			throw new BadRequestException("wrong sort predicate: " + zahlungenSearchParamsDTO.getSortPredicate());
		}

		if (zahlungenSearchParamsDTO.getSortReverse() != null && zahlungenSearchParamsDTO.getSortReverse()) {
			query.orderBy(cb.desc(sortExpression));
		} else if (zahlungenSearchParamsDTO.getSortReverse() != null && !zahlungenSearchParamsDTO.getSortReverse()) {
			query.orderBy(cb.asc(sortExpression));
		}
	}

	// if user is in institution role, we have to calculate the status from all zahlungen, user is allowed to see
	// we count the number of zahlungen which are not AUSGELOEST (not yet BESTAETIGT) and order it by them
	private Expression<?> createInstitutionStatusSortPredicate(
		@Nonnull CriteriaQuery<Zahlungsauftrag> query,
		@Nonnull ZahlungenSearchParamsDTO zahlungenSearchParamsDTO,
		@Nonnull Root<Zahlungsauftrag> root,
		@Nonnull CriteriaBuilder cb
	) {
		Expression<?> sortExpression;
		List<String> allowedInstitutionenIds = zahlungenSearchParamsDTO.getAllowedInstitutionIds();
		Join<Zahlungsauftrag, Zahlung> joinZahlung = root.join(Zahlungsauftrag_.zahlungen);
		query.groupBy(root.get(Zahlungsauftrag_.id));

		Predicate ausgeloestAndInstitutionId = cb.and(
			cb.equal(joinZahlung.get(Zahlung_.STATUS), ZahlungStatus.AUSGELOEST),
			joinZahlung.get(Zahlung_.empfaengerId).in(allowedInstitutionenIds)
		);

		sortExpression = cb.sum(
			cb.selectCase()
				.when(ausgeloestAndInstitutionId, 1)
				.otherwise(0)
				.as(Number.class)
		);
		return sortExpression;
	}

	@Override
	@Nonnull
	public Zahlung zahlungBestaetigen(@Nonnull String zahlungId) {
		requireNonNull(zahlungId, "zahlungId muss gesetzt sein");
		Zahlung zahlung =
			findZahlung(zahlungId).orElseThrow(() -> new EbeguEntityNotFoundException("zahlungBestaetigen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, zahlungId));
		zahlung.setStatus(ZahlungStatus.BESTAETIGT);
		Zahlung persistedZahlung = persistence.merge(zahlung);
		zahlungauftragBestaetigenIfAllZahlungenBestaetigt(zahlung.getZahlungsauftrag());
		return persistedZahlung;
	}

	@Nonnull
	@Override
	public Collection<Zahlungsauftrag> getZahlungsauftraegeInPeriode(@Nonnull LocalDate von, @Nonnull LocalDate bis) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);

		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
		List<Predicate> predicatesToUse = new ArrayList<>();

		// Zeitraum
		Predicate predicateZeitraum = cb.between(
			root.get(Zahlungsauftrag_.datumGeneriert),
			cb.literal(von.atStartOfDay()),
			cb.literal(bis.atTime(LocalTime.MAX)));
		predicatesToUse.add(predicateZeitraum);
		// Dieser Report betrifft nur Institutionszahlungen
		Predicate predicateAuftragTyp =
			cb.equal(root.get(Zahlungsauftrag_.zahlungslaufTyp), ZahlungslaufTyp.GEMEINDE_INSTITUTION);
		predicatesToUse.add(predicateAuftragTyp);

		Mandant mandant = principalBean.getMandant();

		// Mandant
		Predicate mandantPredicate = cb.equal(
			root.get(Zahlungsauftrag_.mandant),
			mandant
		);
		predicatesToUse.add(mandantPredicate);

		// Gemeinde
		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getBenutzersOfRole", "Non logged in user should never reach this"));

		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Join<Zahlungsauftrag, Gemeinde> joinGemeinde = root.join(Zahlungsauftrag_.gemeinde);

			Collection<Gemeinde> gemeindenForUser = currentBenutzer.extractGemeindenForUser();
			Predicate inGemeinde = joinGemeinde.in(gemeindenForUser);
			predicatesToUse.add(inGemeinde);
		}
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse));
		return persistence.getCriteriaResults(query);

	}

	@Override
	public void deleteZahlungspositionenOfGesuch(@Nonnull Gesuch gesuch) {
		requireNonNull(gesuch, "gesuch muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsposition> query = cb.createQuery(Zahlungsposition.class);

		Root<Zahlungsposition> root = query.from(Zahlungsposition.class);
		Predicate predicates = cb.equal(root.get(Zahlungsposition_.verfuegungZeitabschnitt)
			.get(VerfuegungZeitabschnitt_.verfuegung)
			.get(Verfuegung_.betreuung)
			.get(Betreuung_.kind)
			.get(KindContainer_.gesuch), gesuch);

		query.where(predicates);
		List<Zahlungsposition> zahlungspositionList = persistence.getCriteriaResults(query);

		//remove Zahlungspositionen
		Set<Zahlung> potenziellZuLoeschenZahlungenList = new HashSet<>();
		for (Zahlungsposition zahlungsposition : zahlungspositionList) {
			potenziellZuLoeschenZahlungenList.add(zahlungsposition.getZahlung()); // add the Zahlung to the set
			zahlungsposition.getZahlung().getZahlungspositionen().remove(zahlungsposition);
			persistence.remove(Zahlungsposition.class, zahlungsposition.getId());
		}
		Set<Zahlungsauftrag> zahlungsauftraegeList = removeAllEmptyZahlungen(potenziellZuLoeschenZahlungenList);
		removeAllEmptyZahlungsauftraege(zahlungsauftraegeList);
	}

	/**
	 * Goes through the given list and check whether the given Zahlungsauftrag is empty or not.
	 * All empty Zahlungsauftraege are removed.
	 */
	private void removeAllEmptyZahlungsauftraege(Set<Zahlungsauftrag> zahlungsauftraegeList) {
		for (Zahlungsauftrag zahlungsauftrag : zahlungsauftraegeList) {
			if (zahlungsauftrag.getZahlungen().isEmpty()) {
				removePAIN001FromZahlungsauftrag(zahlungsauftrag);
				persistence.remove(Zahlungsauftrag.class, zahlungsauftrag.getId());
			}
		}
	}

	/**
	 * Removes the Pain001Dokument that is linked with the given Zahlungsauftrag if it exists.
	 */
	private void removePAIN001FromZahlungsauftrag(Zahlungsauftrag zahlungsauftrag) {
		final Collection<Pain001Dokument> pain001Dokument =
			criteriaQueryHelper.getEntitiesByAttribute(
				Pain001Dokument.class,
				zahlungsauftrag,
				Pain001Dokument_.zahlungsauftrag);
		pain001Dokument.forEach(pain -> {
			fileSaverService.removeAllFromSubfolder(pain.getZahlungsauftrag().getId());
			persistence.remove(Pain001Dokument.class, pain.getId());
		});
	}

	/**
	 * Goes through the given list and check whether the given Zahlung is empty or not.
	 * All empty Zahlungen are removed and all corresponding Zahlungsauftraege are added to the
	 * Set that will be returned at the end of the function
	 */
	@Nonnull
	private Set<Zahlungsauftrag> removeAllEmptyZahlungen(Set<Zahlung> potenziellZuLoeschenZahlungenList) {
		Set<Zahlungsauftrag> potenziellZuLoeschenZahlungsauftraegeList = new HashSet<>();
		for (Zahlung zahlung : potenziellZuLoeschenZahlungenList) {
			if (zahlung.getZahlungspositionen().isEmpty()) {
				potenziellZuLoeschenZahlungsauftraegeList.add(zahlung.getZahlungsauftrag());
				zahlung.getZahlungsauftrag().getZahlungen().remove(zahlung);
				persistence.remove(Zahlung.class, zahlung.getId());
			}
		}
		return potenziellZuLoeschenZahlungsauftraegeList;
	}

	private void zahlungauftragBestaetigenIfAllZahlungenBestaetigt(@Nonnull Zahlungsauftrag zahlungsauftrag) {
		requireNonNull(zahlungsauftrag, "zahlungsauftrag darf nicht null sein");
		if (zahlungsauftrag.getZahlungen()
			.stream()
			.allMatch(zahlung -> zahlung.getStatus() == ZahlungStatus.BESTAETIGT)) {
			zahlungsauftrag.setStatus(ZahlungauftragStatus.BESTAETIGT);
			persistence.merge(zahlungsauftrag);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void zahlungenKontrollieren(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull Boolean auszahlungInZukunft
	) {
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("zahlungenKontrollieren",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));
		Optional<Zahlungsauftrag> lastZahlungsauftrag = findLastZahlungsauftrag(zahlungslaufTyp, gemeinde);
		lastZahlungsauftrag.ifPresent(zahlungsauftrag -> zahlungUeberpruefungServiceBean.pruefungZahlungen(
			gemeinde,
			zahlungslaufTyp,
			zahlungsauftrag.getId(),
			zahlungsauftrag.getDatumGeneriert(),
			zahlungsauftrag.getBeschrieb(),
			auszahlungInZukunft)
		);
	}
}


