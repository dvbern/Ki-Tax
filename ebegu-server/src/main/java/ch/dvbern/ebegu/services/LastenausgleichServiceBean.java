/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetail_;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen_;
import ch.dvbern.ebegu.entities.Lastenausgleich_;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer den Lastenausgleich
 */
@Stateless
@Local(LastenausgleichService.class)
public class LastenausgleichServiceBean extends AbstractBaseService implements LastenausgleichService {

	private static final Logger LOG = LoggerFactory.getLogger(LastenausgleichServiceBean.class.getSimpleName());

	private static final BigDecimal SELBSTBEHALT = MathUtil.EXACT.fromNullSafe(0.20);
	private static final String NEWLINE = "\n";

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private MailService mailService;

	@Nonnull
	@Override
	public Collection<Lastenausgleich> getAllLastenausgleiche() {
		return criteriaQueryHelper.getAll(Lastenausgleich.class);
	}

	@Nonnull
	@Override
	public Collection<Lastenausgleich> getLastenausgleicheForGemeinden(@Nonnull Set<Gemeinde> gemeinden) {
		return this.getAllLastenausgleiche().stream().map(lastenausgleich -> {
			Lastenausgleich clone = new Lastenausgleich();
			// filter gemeinden that are not in the list
			clone.setLastenausgleichDetails(
				lastenausgleich.getLastenausgleichDetails()
					.stream()
					.filter(lastenausgleichDetail -> gemeinden.contains(lastenausgleichDetail.getGemeinde()))
					.collect(Collectors.toList()));
			// set total from filtered gemeinden
			clone.setTotalAlleGemeinden(clone.getLastenausgleichDetails().stream().reduce(
				BigDecimal.ZERO,
				(subtotal, lastenausgleichDetail) -> subtotal.add(lastenausgleichDetail.getBetragLastenausgleich()),
				BigDecimal::add));
			clone.setJahr(lastenausgleich.getJahr());
			clone.setId(lastenausgleich.getId());
			clone.setTimestampErstellt(lastenausgleich.getTimestampErstellt() != null ?
				lastenausgleich.getTimestampErstellt() :
				LocalDateTime.MIN);

			return clone;
		}).collect(Collectors.toSet());
	}

	@Override
	@Nonnull
	public Lastenausgleich createLastenausgleich(int jahr, @Nonnull BigDecimal selbstbehaltPro100ProzentPlatz) {
		// Ueberpruefen, dass es nicht schon einen Lastenausgleich oder LastenausgleichGrundlagen gibt fuer dieses Jahr
		assertUnique(jahr);

		StringBuilder sb = new StringBuilder();
		sb.append("Erstelle Lastenausgleich für Jahr ").append(jahr)
			.append(" bei einem Selbstbehalt pro 100% Platz von ").append(selbstbehaltPro100ProzentPlatz)
			.append(NEWLINE);

		BigDecimal kostenPro100ProzentPlatz =
			MathUtil.DEFAULT.divideNullSafe(selbstbehaltPro100ProzentPlatz, SELBSTBEHALT);
		sb.append("Kosten pro 100% Platz: ").append(kostenPro100ProzentPlatz)
			.append(NEWLINE);

		LastenausgleichGrundlagen grundlagenErhebungsjahr = new LastenausgleichGrundlagen();
		grundlagenErhebungsjahr.setJahr(jahr);
		grundlagenErhebungsjahr.setSelbstbehaltPro100ProzentPlatz(selbstbehaltPro100ProzentPlatz);
		grundlagenErhebungsjahr.setKostenPro100ProzentPlatz(kostenPro100ProzentPlatz);
		persistence.persist(grundlagenErhebungsjahr);

		Lastenausgleich lastenausgleich = new Lastenausgleich();
		lastenausgleich.setJahr(jahr);

		// Die regulare Abrechnung
		Collection<Gemeinde> aktiveGemeinden = gemeindeService.getAktiveGemeinden();
		for (Gemeinde gemeinde : aktiveGemeinden) {
			LastenausgleichDetail detailErhebung =
				createLastenausgleichDetail(gemeinde, lastenausgleich, grundlagenErhebungsjahr);
			if (detailErhebung != null) {
				lastenausgleich.addLastenausgleichDetail(detailErhebung);
				sb.append("Reguläre Abrechnung Gemeinde ").append(gemeinde.getName()).append(NEWLINE);
				sb.append(detailErhebung).append(NEWLINE);
			}
		}
		// Korrekturen frueherer Jahre: Wir gehen bis 10 Jahre retour
		for (int i = 1; i < 10; i++) {
			int korrekturJahr = jahr - i;
			Optional<LastenausgleichGrundlagen> grundlagenKorrekturjahr = findLastenausgleichGrundlagen(korrekturJahr);
			if (grundlagenKorrekturjahr.isPresent()) {
				sb.append("Korrekturen für Jahr ").append(korrekturJahr).append(NEWLINE);
				for (Gemeinde gemeinde : aktiveGemeinden) {
					handleKorrekturJahrFuerGemeinde(
						korrekturJahr,
						gemeinde,
						lastenausgleich,
						grundlagenKorrekturjahr.get(),
						sb);
				}
			}
			// Wir loggen dies mit WARN, damit wir es im Sentry sehen
			LOG.warn(sb.toString());
		}

		// Am Schluss das berechnete Total speichern
		BigDecimal totalGesamterLastenausgleich = BigDecimal.ZERO;
		for (LastenausgleichDetail lastenausgleichDetail : lastenausgleich.getLastenausgleichDetails()) {
			totalGesamterLastenausgleich = MathUtil.DEFAULT.addNullSafe(
				totalGesamterLastenausgleich,
				lastenausgleichDetail.getBetragLastenausgleich());
		}
		lastenausgleich.setTotalAlleGemeinden(totalGesamterLastenausgleich);

		Lastenausgleich storedLastenausgleich = persistence.merge(lastenausgleich);

		//Send Email
		lastenausgleich.getLastenausgleichDetails().stream().forEach(lastenausgleichDetail ->
			mailService.sendInfoLastenausgleichGemeinde(lastenausgleichDetail.getGemeinde())
		);
		return storedLastenausgleich;
	}

	private void handleKorrekturJahrFuerGemeinde(
		int korrekturJahr,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagenKorrekturjahr,
		@Nonnull StringBuilder logBuilder
	) {
		logBuilder.append("Gemeinde ").append(gemeinde.getName()).append(NEWLINE);
		// Wir ermitteln für die Gemeinde und das Korrekurjahr den aktuell gültigen Wert
		LastenausgleichDetail detailAktuellesTotalKorrekturjahr =
			createLastenausgleichDetail(gemeinde, lastenausgleich, grundlagenKorrekturjahr);
		logBuilder.append("Aktuell berechnetes Total: ")
			.append(NEWLINE)
			.append(detailAktuellesTotalKorrekturjahr)
			.append(NEWLINE);

		if (detailAktuellesTotalKorrekturjahr != null) {
			// Dieses Detail ist jetzt aber das aktuelle Total für das Jahr. Uns interessiert aber die eventuelle
			// Differenz zu bereits ausgeglichenen Beträgen
			Collection<LastenausgleichDetail> detailsBereitsVerrechnetKorrekturjahr =
				findLastenausgleichDetailForKorrekturen(korrekturJahr, gemeinde);
			if (CollectionUtils.isNotEmpty(detailsBereitsVerrechnetKorrekturjahr)) {
				LastenausgleichDetail detailBisherigeWerte = new LastenausgleichDetail();
				for (LastenausgleichDetail detailBereitsVerrechnet : detailsBereitsVerrechnetKorrekturjahr) {
					detailBisherigeWerte.add(detailBereitsVerrechnet);
				}
				logBuilder.append("Davon bereits verrechnet: ")
					.append(NEWLINE)
					.append(detailBisherigeWerte)
					.append(NEWLINE);
				// Gibt es eine Differenz?
				if (detailBisherigeWerte.hasChanged(detailAktuellesTotalKorrekturjahr)) {
					// Es gibt eine Differenz (wobei wir nur den Betrag des Lastenausgleiches anschauen)
					// Wir rechnen das bisher verrechnete minus
					LastenausgleichDetail detailKorrektur = createLastenausgleichDetailKorrektur(detailBisherigeWerte);
					detailKorrektur.setLastenausgleich(lastenausgleich);
					lastenausgleich.addLastenausgleichDetail(detailKorrektur);
					logBuilder.append("Korrektur PLUS: ").append(NEWLINE).append(detailKorrektur).append(NEWLINE);
					// Und erstellen einen neuen Korrektur-Eintrag mit dem aktuell berechneten Wert
					lastenausgleich.addLastenausgleichDetail(detailAktuellesTotalKorrekturjahr);
					logBuilder.append("Korrektur MINUS: ")
						.append(NEWLINE)
						.append(detailAktuellesTotalKorrekturjahr)
						.append(NEWLINE);
				}
			}
		}
	}

	@Nonnull
	@Override
	public Lastenausgleich findLastenausgleich(@Nonnull String lastenausgleichId) {
		return persistence.find(Lastenausgleich.class, lastenausgleichId);
	}

	@Nonnull
	private Optional<Lastenausgleich> findLastenausgleichByJahr(int jahr) {
		Optional<Lastenausgleich> optional = criteriaQueryHelper.getEntityByUniqueAttribute(
			Lastenausgleich.class,
			jahr,
			Lastenausgleich_.jahr);
		return optional;
	}

	@Override
	@Nonnull
	public Optional<LastenausgleichGrundlagen> findLastenausgleichGrundlagen(int jahr) {
		Optional<LastenausgleichGrundlagen> optional =
			criteriaQueryHelper.getEntityByUniqueAttribute(LastenausgleichGrundlagen.class, jahr,
				LastenausgleichGrundlagen_.jahr);
		return optional;
	}

	@Override
	public void removeLastenausgleich(@Nonnull String lastenausgleichId) {
		// Lastenausgleich und -Grundlagen löschen
		requireNonNull(lastenausgleichId);
		Lastenausgleich lastenausgleichToRemove = findLastenausgleich(lastenausgleichId);
		Optional<LastenausgleichGrundlagen> lastenausgleichGrundlagen =
			findLastenausgleichGrundlagen(lastenausgleichToRemove.getJahr());
		lastenausgleichGrundlagen.ifPresent(lastenausgleichGrundlagen1 -> persistence.remove(lastenausgleichGrundlagen1));
		persistence.remove(lastenausgleichToRemove);
	}

	private Collection<LastenausgleichDetail> findLastenausgleichDetailForKorrekturen(
		int jahr,
		@Nonnull Gemeinde gemeinde) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichDetail> query = cb.createQuery(LastenausgleichDetail.class);
		Root<LastenausgleichDetail> root = query.from(LastenausgleichDetail.class);

		ParameterExpression<Integer> paramJahr = cb.parameter(Integer.class, "paramJahr");
		ParameterExpression<Gemeinde> paramGemeinde = cb.parameter(Gemeinde.class, "paramGemeinde");

		Predicate predicateJahr = cb.equal(root.get(LastenausgleichDetail_.jahr), paramJahr);
		Predicate predicateGemeinde = cb.equal(root.get(LastenausgleichDetail_.gemeinde), paramGemeinde);
		query.where(predicateJahr, predicateGemeinde);

		TypedQuery<LastenausgleichDetail> tq = persistence.getEntityManager().createQuery(query);

		tq.setParameter("paramJahr", jahr);
		tq.setParameter("paramGemeinde", gemeinde);
		return tq.getResultList();
	}

	private void assertUnique(int jahr) {
		if (findLastenausgleichGrundlagen(jahr).isPresent()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"assertUnique",
				ErrorCodeEnum.ERROR_LASTENAUSGLEICH_GRUNDLAGEN_EXISTS);
		}
		if (findLastenausgleichByJahr(jahr).isPresent()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"assertUnique",
				ErrorCodeEnum.ERROR_LASTENAUSGLEICH_EXISTS);
		}
	}

	@Nullable
	private LastenausgleichDetail createLastenausgleichDetail(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagen
	) {
		Collection<VerfuegungZeitabschnitt> abschnitteProGemeindeUndJahr =
			getZeitabschnitte(gemeinde, grundlagen.getJahr());
		if (abschnitteProGemeindeUndJahr.isEmpty()) {
			return null;
		}

		// Total Belegung = Totals aller Pensum * AnteilDesMonats / 12
		BigDecimal totalBelegungInProzent = BigDecimal.ZERO;
		// Total Gutscheine: Totals aller aktuell gültigen Zeitabschnitte, die im Kalenderjahr liegen
		BigDecimal totalGutscheine = BigDecimal.ZERO;
		for (VerfuegungZeitabschnitt abschnitt : abschnitteProGemeindeUndJahr) {
			BigDecimal anteilKalenderjahr = getAnteilKalenderjahr(abschnitt);
			BigDecimal gutschein = abschnitt.getBgCalculationResultAsiv().getVerguenstigung();

			totalBelegungInProzent = MathUtil.EXACT.addNullSafe(totalBelegungInProzent, anteilKalenderjahr);
			totalGutscheine = MathUtil.EXACT.addNullSafe(totalGutscheine, gutschein);
		}
		// Selbstbehalt Gemeinde = Total Belegung * Kosten pro 100% Platz * 20%
		BigDecimal totalBelegung = MathUtil.EXACT.divide(totalBelegungInProzent, MathUtil.EXACT.from(100));
		BigDecimal selbstbehaltGemeinde =
			MathUtil.EXACT.multiplyNullSafe(totalBelegung, grundlagen.getSelbstbehaltPro100ProzentPlatz());
		// Eingabe Lastenausgleich = Total Gutscheine - Selbstbehalt Gemeinde
		BigDecimal eingabeLastenausgleich = MathUtil.EXACT.subtractNullSafe(totalGutscheine, selbstbehaltGemeinde);

		// Total anrechenbar = total belegung * Kosten pro 100% Platz
		BigDecimal totalAnrechenbar =
			MathUtil.EXACT.multiplyNullSafe(totalBelegung, grundlagen.getKostenPro100ProzentPlatz());

		LastenausgleichDetail detail = new LastenausgleichDetail();
		detail.setJahr(grundlagen.getJahr());
		detail.setGemeinde(gemeinde);
		detail.setTotalBelegungen(MathUtil.toTwoKommastelle(totalBelegungInProzent));
		detail.setTotalAnrechenbar(MathUtil.toTwoKommastelle(totalAnrechenbar));
		detail.setTotalBetragGutscheine(MathUtil.toTwoKommastelle(totalGutscheine));
		detail.setSelbstbehaltGemeinde(MathUtil.toTwoKommastelle(selbstbehaltGemeinde));
		detail.setBetragLastenausgleich(MathUtil.toTwoKommastelle(eingabeLastenausgleich));
		detail.setLastenausgleich(lastenausgleich);
		detail.setKorrektur(lastenausgleich.getJahr().compareTo(grundlagen.getJahr()) != 0);
		return detail;

	}

	@Nonnull
	private LastenausgleichDetail createLastenausgleichDetailKorrektur(
		@Nonnull LastenausgleichDetail detail
	) {
		detail.setTotalBelegungen(detail.getTotalBelegungen().negate());
		detail.setTotalAnrechenbar(detail.getTotalAnrechenbar().negate());
		detail.setTotalBetragGutscheine(detail.getTotalBetragGutscheine().negate());
		detail.setSelbstbehaltGemeinde(detail.getSelbstbehaltGemeinde().negate());
		detail.setBetragLastenausgleich(detail.getBetragLastenausgleich().negate());
		detail.setKorrektur(true);
		return detail;
	}

	@Nonnull
	private BigDecimal getAnteilKalenderjahr(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		// Pensum * AnteilDesMonats / 12. Beispiel 80% ganzer Monat = 6.67% AnteilKalenderjahr
		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(
			zeitabschnitt.getGueltigkeit().getGueltigAb(),
			zeitabschnitt.getGueltigkeit().getGueltigBis());
		BigDecimal pensum = zeitabschnitt.getBgCalculationResultAsiv().getBgPensumProzent();
		BigDecimal pensumAnteilMonat = MathUtil.EXACT.multiplyNullSafe(anteilMonat, pensum);
		return MathUtil.EXACT.divide(pensumAnteilMonat, MathUtil.EXACT.from(12d));
	}

	@Nonnull
	private Collection<VerfuegungZeitabschnitt> getZeitabschnitte(@Nonnull Gemeinde gemeinde, int jahr) {
		return verfuegungService.findZeitabschnitteByYear(jahr, gemeinde);
	}
}
