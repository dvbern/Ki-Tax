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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den EbeguParametern gelesen werden.
 */
public final class KitaxUebergangsloesungParameter {

	private static final MathUtil MATH = MathUtil.DEFAULT;

	private Map<String, KitaxUebergangsloesungInstitutionOeffnungszeiten> oeffnungszeitenMap = new HashMap<>();

	private BigDecimal beitragKantonProTag = MATH.from(111.15);
	private BigDecimal beitragStadtProTagJahr = MATH.from(8.00);

	private BigDecimal maxTageKita = MATH.from(244);
	private BigDecimal maxStundenProTagKita = MATH.from(11.5);

	private BigDecimal kostenProStundeMaximalKitaTagi = MATH.from(12.35);
	private BigDecimal kostenProStundeMaximalTageseltern = MATH.from(9.49);
	private BigDecimal kostenProStundeMinimal = MATH.from(0.79);

	private BigDecimal maxMassgebendesEinkommen = MATH.from(160000);
	private BigDecimal minMassgebendesEinkommen = MATH.from(43000);

	private BigDecimal babyFaktor = MATH.from(1.5);

	private LocalDate stadtBernAsivStartDate = null;
	private boolean isStadtBernAsivConfiguered = false;


	public KitaxUebergangsloesungParameter(@Nonnull LocalDate stadtBernAsivStartDate, boolean isStadtBernAsivConfiguered) {
		this.stadtBernAsivStartDate = stadtBernAsivStartDate;
		this.isStadtBernAsivConfiguered = isStadtBernAsivConfiguered;
		initOeffnungszeitenMap();
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void initOeffnungszeitenMap() {
		oeffnungszeitenMap.put("kita dängelibänz", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita matahari weissenbühl", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita sandburg", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita bachmätteli", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("roti zora", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(9), MATH.from(225)));
		oeffnungszeitenMap.put("kita wombat zieglerstrasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("tagi libelle", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("matahari kirchenfeld", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita breitsch", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita loryplatz", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita murifeld mindstrasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita weissenstein", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("globegarden parkterrasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita morillon", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita wirbelwind", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("coccodrillo", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita murifeld weltpost", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("chindertroum waldkita", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11), MATH.from(238)));
		oeffnungszeitenMap.put("kita burgunder", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita himugüegeli", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita holenacker", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("strampolino", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita taka tuka", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kids & co wankdorf", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita krokofant", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita altenberg", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("private kindertagesstätte mattenhof", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita balou kirchenfeld", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("globegarden thunstrasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita tscharnergut", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("wyleregg", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita eigerplatz", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita wombat buchserstrasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("brünnen", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("villa tagi", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita lorraine", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita yeladim", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(237)));
		oeffnungszeitenMap.put("kita länggasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita fantasia", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kids & co viktoriastrasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita wyler", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("röseligarte", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita brünnengut", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita breitenrain", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita stöckacker", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita steckgut", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita weissenbühl", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("montessori kh lorraine", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(242)));
		oeffnungszeitenMap.put("kita wundertüte", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(10), MATH.from(224)));
		oeffnungszeitenMap.put("kita smallworld", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(237)));
		oeffnungszeitenMap.put("hagebutte", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("elfenau", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("mixmax kindertagegstätte schönegg", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(242)));
		oeffnungszeitenMap.put("kita farfallina", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11), MATH.from(235)));
		oeffnungszeitenMap.put("kita im favorite", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita spittel", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita bümpliz", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kinderort", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.25), MATH.from(230)));
		oeffnungszeitenMap.put("montessori a. d. aare", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("angelie hirt", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita làpurzel", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita publica", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita rosenweg", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita aaregg", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("topolina", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita spitalacker", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("sputnik (prokids ag)", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(237)));
		oeffnungszeitenMap.put("montessori kindertagesbetreuung viki", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita piccolino", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita crescendo", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("waldkita murifeld", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita ausserholligen", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita lindenhof", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita dählhölzli", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita tiefenau", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita murtenstrasse", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita rappard", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita bitzius", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("pop e poppa", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita ottilotti", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("tartaruga", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("ginkgo", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(12), MATH.from(244)));
		oeffnungszeitenMap.put("kirchenfeld", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita firlifanz", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita pop e poppa forsthaus", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
		oeffnungszeitenMap.put("kita matte", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(240)));
		oeffnungszeitenMap.put("kita falkennest", new KitaxUebergangsloesungInstitutionOeffnungszeiten(MATH.from(11.5), MATH.from(244)));
	}

	@Nullable
	public KitaxUebergangsloesungInstitutionOeffnungszeiten getOeffnungszeiten(@Nonnull String kitaName) {
		KitaxUebergangsloesungInstitutionOeffnungszeiten dto =
			oeffnungszeitenMap.get(kitaName.trim().toLowerCase());
		return dto;
	}

	public BigDecimal getBeitragKantonProTag() {
		return beitragKantonProTag;
	}

	public BigDecimal getBeitragStadtProTagJahr() {
		return beitragStadtProTagJahr;
	}

	public BigDecimal getMaxTageKita() {
		return maxTageKita;
	}

	public BigDecimal getMaxStundenProTagKita() {
		return maxStundenProTagKita;
	}

	public BigDecimal getKostenProStundeMaximalKitaTagi() {
		return kostenProStundeMaximalKitaTagi;
	}

	public BigDecimal getKostenProStundeMaximalTageseltern() {
		return kostenProStundeMaximalTageseltern;
	}

	public BigDecimal getKostenProStundeMinimal() {
		return kostenProStundeMinimal;
	}

	public BigDecimal getMaxMassgebendesEinkommen() {
		return maxMassgebendesEinkommen;
	}

	public BigDecimal getMinMassgebendesEinkommen() {
		return minMassgebendesEinkommen;
	}

	public BigDecimal getBabyFaktor() {
		return babyFaktor;
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

	public boolean isGemeindeWithKitaxUebergangsloesung(@Nonnull Gemeinde gemeinde) {
		// Zum Testen behandeln wir Paris wie Bern
		long bfsNummer = gemeinde.getBfsNummer();
		return bfsNummer == 351 || bfsNummer == 99998;
	}
}
