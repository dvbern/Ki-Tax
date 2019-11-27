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
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer den Lastenausgleich
 */
@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
@Stateless
@Local(LastenausgleichService.class)
public class LastenausgleichServiceBean extends AbstractBaseService implements LastenausgleichService {

	private static final Logger LOG = LoggerFactory.getLogger(LastenausgleichServiceBean.class);
	private static final BigDecimal SELBSTBEHALT = MathUtil.DEFAULT.from(0.2d);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private VerfuegungService verfuegungService;


	@RolesAllowed(SUPER_ADMIN)
	@Override
	@Nonnull
	public Lastenausgleich createLastenausgleich(int jahr, @Nonnull BigDecimal kostenPro100ProzentPlatz) {
		// Ueberpruefen, dass es nicht schon einen Lastenausgleich oder LastenausgleichGrundlagen gibt fuer dieses Jahr
		assertUnique(jahr);

		LastenausgleichGrundlagen grundlagen = new LastenausgleichGrundlagen();
		grundlagen.setJahr(jahr);
		grundlagen.setKostenPro100ProzentPlatz(kostenPro100ProzentPlatz);
		persistence.persist(grundlagen);

		Lastenausgleich lastenausgleich = new Lastenausgleich();
		lastenausgleich.setJahr(jahr);

		// Die regulare Abrechnung
		Collection<Gemeinde> aktiveGemeinden = gemeindeService.getAktiveGemeinden();
		for (Gemeinde gemeinde : aktiveGemeinden) {
			LOG.info("Evaluating Gemeinde " + gemeinde.getName() + " and year " + jahr);
			LastenausgleichDetail detail = createLastenausgleichDetail(gemeinde, lastenausgleich, grundlagen);
			if (detail != null) {
				LOG.info("... found");
				lastenausgleich.addLastenausgleichDetail(detail);
			}
		}
		// Korrekturen frueherer Jahre: Wir gehen bis 10 Jahre retour
		for (int i = 1; i < 10; i++) {
			int korrekturJahr = jahr - i;
			Optional<LastenausgleichGrundlagen> grundlagenKorrekturjahr = findLastenausgleichGrundlagen(korrekturJahr);
			if (grundlagenKorrekturjahr.isPresent()) {
				for (Gemeinde gemeinde : aktiveGemeinden) {
					LOG.info("Evaluating Korrekturen for Gemeinde " + gemeinde.getName() + " and year " + korrekturJahr);
					LastenausgleichDetail detail = createLastenausgleichDetail(gemeinde, lastenausgleich, grundlagenKorrekturjahr.get());
					if (detail != null) {
						LOG.info("... found");
						lastenausgleich.addLastenausgleichDetail(detail);
					}
				}
			}
		}

		// Am Schluss das berechnete Total speichern
		BigDecimal totalGesamterLastenausgleich = BigDecimal.ZERO;
		for (LastenausgleichDetail lastenausgleichDetail : lastenausgleich.getLastenausgleichDetails()) {
			MathUtil.DEFAULT.addNullSafe(totalGesamterLastenausgleich, lastenausgleichDetail.getBetragLastenausgleich());
		}
		lastenausgleich.setTotalAlleGemeinden(totalGesamterLastenausgleich);

		return persistence.merge(lastenausgleich);
	}

	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	@Override
	@Nonnull
	public Optional<Lastenausgleich> findLastenausgleich(int jahr) {
		Optional<Lastenausgleich> optional = criteriaQueryHelper.getEntityByUniqueAttribute(Lastenausgleich.class, jahr,
			Lastenausgleich_.jahr);
		return optional;
	}

	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	@Override
	@Nonnull
	public Optional<LastenausgleichGrundlagen> findLastenausgleichGrundlagen(int jahr) {
		Optional<LastenausgleichGrundlagen> optional = criteriaQueryHelper.getEntityByUniqueAttribute(LastenausgleichGrundlagen.class, jahr,
			LastenausgleichGrundlagen_.jahr);
		return optional;
	}

	private void assertUnique(int jahr) {
		if (findLastenausgleichGrundlagen(jahr).isPresent()) {
			throw new EbeguRuntimeException(KibonLogLevel.NONE, "assertUnique", ErrorCodeEnum.ERROR_LASTENAUSGLEICH_GRUNDLAGEN_EXISTS);
		}
		if (findLastenausgleich(jahr).isPresent()) {
			throw new EbeguRuntimeException(KibonLogLevel.NONE, "assertUnique", ErrorCodeEnum.ERROR_LASTENAUSGLEICH_EXISTS);
		}
	}

	@Nullable
	private LastenausgleichDetail createLastenausgleichDetail(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagen
	) {
		Collection<VerfuegungZeitabschnitt> abschnitteProGemeindeUndJahr = getZeitabschnitte(gemeinde, grundlagen.getJahr());
		if (abschnitteProGemeindeUndJahr.isEmpty()) {
			return null;
		}

		// Total Belegung = Totals aller Pensum * AnteilDesMonats / 12
		BigDecimal totalAnteilKalenderjahr = BigDecimal.ZERO;
		// Total Gutscheine: Totals aller aktuell gültigen Zeitabschnitte, die im Kalenderjahr liegen
		BigDecimal totalGutscheine = BigDecimal.ZERO;
		for (VerfuegungZeitabschnitt abschnitt : abschnitteProGemeindeUndJahr) {
			BigDecimal anteilKalenderjahr = getAnteilKalenderjahr(abschnitt);
			BigDecimal gutschein = abschnitt.getVerguenstigung();

			totalAnteilKalenderjahr = MathUtil.DEFAULT.addNullSafe(totalAnteilKalenderjahr, anteilKalenderjahr);
			totalGutscheine = MathUtil.DEFAULT.addNullSafe(totalGutscheine, gutschein);
		}
		// Selbstbehalt Gemeinde = Total Belegung * Kosten pro 100% Platz * 20%
		BigDecimal selbstbehaltGemeinde = MathUtil.DEFAULT.multiplyNullSafe(totalAnteilKalenderjahr, grundlagen.getKostenPro100ProzentPlatz(), SELBSTBEHALT);
		// Eingabe Lastenausgleich = Total Gutscheine * Selbstbehalt Gemeinde
		BigDecimal eingabeLastenausgleich = MathUtil.DEFAULT.multiplyNullSafe(totalGutscheine, selbstbehaltGemeinde);

		LastenausgleichDetail detail = new LastenausgleichDetail();
		detail.setJahr(grundlagen.getJahr());
		detail.setGemeinde(gemeinde);
		detail.setTotalBelegungen(totalAnteilKalenderjahr);
		detail.setTotalBetragGutscheine(totalGutscheine);
		detail.setSelbstbehaltGemeinde(selbstbehaltGemeinde);
		detail.setBetragLastenausgleich(eingabeLastenausgleich);
		detail.setLastenausgleich(lastenausgleich);
		detail.setKorrektur(lastenausgleich.getJahr().compareTo(grundlagen.getJahr()) != 0);
		return detail;

	}

	@Nonnull
	private BigDecimal getAnteilKalenderjahr(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		// Pensum * AnteilDesMonats / 12. Beispiel 80% ganzer Monat = 6.67% AnteilKalenderjahr
		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(zeitabschnitt.getGueltigkeit().getGueltigAb(),
			zeitabschnitt.getGueltigkeit().getGueltigBis());
		BigDecimal pensum = zeitabschnitt.getBgPensum();
		BigDecimal pensumAnteilMonat = MathUtil.DEFAULT.multiplyNullSafe(anteilMonat, pensum);
		return MathUtil.DEFAULT.divide(pensumAnteilMonat, MathUtil.DEFAULT.from(12d));
	}

	@Nonnull
	private Collection<VerfuegungZeitabschnitt> getZeitabschnitte(@Nonnull Gemeinde gemeinde, int jahr) {
		return verfuegungService.findZeitabschnitteByYear(jahr, gemeinde);
	}
}
