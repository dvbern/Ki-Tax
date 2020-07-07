/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den EbeguParametern gelesen werden.
 */
public final class KitaxUebergangsloesungParameter {

	private static final MathUtil MATH = MathUtil.DEFAULT;

	private final @Nonnull Map<String, KitaxUebergangsloesungInstitutionOeffnungszeiten> oeffnungszeitenMap = new HashMap<>();

	private final @Nonnull BigDecimal beitragKantonProTag = MATH.from(111.15);
	private final @Nonnull BigDecimal beitragStadtProTagJahr = MATH.from(8.00);

	private final @Nonnull BigDecimal maxTageKita = MATH.from(244);
	private final @Nonnull BigDecimal maxStundenProTagKita = MATH.from(11.5);

	private final @Nonnull BigDecimal kostenProStundeMaximalKitaTagi = MATH.from(12.35);
	private final @Nonnull BigDecimal kostenProStundeMaximalTageseltern = MATH.from(9.49);
	private final @Nonnull BigDecimal kostenProStundeMinimal = MATH.from(0.79);

	private int minEWP = 0; // Gilt fuer alle Schulstufen. Zuschlaege/Rundungen werden im Korrekturmodus gemacht

	private BigDecimal babyFaktor = MathUtil.DEFAULT.from(1.5);

	private final @Nonnull BigDecimal maxMassgebendesEinkommen = MATH.from(160000);
	private final @Nonnull BigDecimal minMassgebendesEinkommen = MATH.from(43000);

	private LocalDate stadtBernAsivStartDate = null;
	private boolean isStadtBernAsivConfiguered = false;


	public KitaxUebergangsloesungParameter(
		@Nonnull LocalDate stadtBernAsivStartDate,
		boolean isStadtBernAsivConfiguered,
		@Nonnull Collection<KitaxUebergangsloesungInstitutionOeffnungszeiten> oeffnungszeiten
	) {
		this.stadtBernAsivStartDate = stadtBernAsivStartDate;
		this.isStadtBernAsivConfiguered = isStadtBernAsivConfiguered;
		for (KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeit : oeffnungszeiten) {
			oeffnungszeitenMap.put(oeffnungszeit.getNameKibon().toLowerCase(Locale.GERMAN).trim(), oeffnungszeit);
		}
	}

	@Nonnull
	public KitaxUebergangsloesungInstitutionOeffnungszeiten getOeffnungszeiten(@Nonnull String kitaName) {
		KitaxUebergangsloesungInstitutionOeffnungszeiten dto =
			oeffnungszeitenMap.get(kitaName.toLowerCase(Locale.GERMAN).trim());

		if (dto == null) {
			throw new EbeguRuntimeException("getOeffnungszeiten", ErrorCodeEnum.ERROR_OEFFNUNGSZEITEN_NOT_FOUND, kitaName);
		}
		return dto;
	}

	@Nonnull
	public BigDecimal getBeitragKantonProTag() {
		return beitragKantonProTag;
	}

	@Nonnull
	public BigDecimal getBeitragStadtProTagJahr() {
		return beitragStadtProTagJahr;
	}

	@Nonnull
	public BigDecimal getMaxTageKita() {
		return maxTageKita;
	}

	@Nonnull
	public BigDecimal getMaxStundenProTagKita() {
		return maxStundenProTagKita;
	}

	@Nonnull
	public BigDecimal getKostenProStundeMaximalKitaTagi() {
		return kostenProStundeMaximalKitaTagi;
	}

	@Nonnull
	public BigDecimal getKostenProStundeMaximalTageseltern() {
		return kostenProStundeMaximalTageseltern;
	}

	@Nonnull
	public BigDecimal getKostenProStundeMinimal() {
		return kostenProStundeMinimal;
	}

	@Nonnull
	public BigDecimal getMaxMassgebendesEinkommen() {
		return maxMassgebendesEinkommen;
	}

	@Nonnull
	public BigDecimal getMinMassgebendesEinkommen() {
		return minMassgebendesEinkommen;
	}

	@Nonnull
	public BigDecimal getBabyFaktor() {
		return babyFaktor;
	}

	public int getMinEWP() {
		return minEWP;
	}

	public LocalDate getStadtBernAsivStartDate() {
		return stadtBernAsivStartDate;
	}

	public void setStadtBernAsivStartDate(LocalDate stadtBernAsivStartDate) {
		this.stadtBernAsivStartDate = stadtBernAsivStartDate;
	}

	public boolean isStadtBernAsivConfiguered() {
		return isStadtBernAsivConfiguered;
	}

	public void setStadtBernAsivConfiguered(boolean stadtBernAsivConfiguered) {
		isStadtBernAsivConfiguered = stadtBernAsivConfiguered;
	}
}
