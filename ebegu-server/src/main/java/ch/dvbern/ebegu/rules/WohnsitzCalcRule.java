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

package ch.dvbern.ebegu.rules;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.enterprise.inject.spi.CDI;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Regel für Wohnsitz in Bern (Zuzug und Wegzug):
 * - Durch Adresse definiert
 * - Anspruch vom ersten Tag des Zuzugs
 * - Anspruch bis 2 Monate nach Wegzug, auf Ende Monat
 * - Kein Doppel-Anspruch bei Umzug mit Betreuung in derselben Institution (KIBON-1843)
 * Verweis 16.8 Der zivilrechtliche Wohnsitz
 */
public class WohnsitzCalcRule extends AbstractCalcRule {

	private final @Nonnull Supplier<GesuchService> gesuchServiceResolver;

	public WohnsitzCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.WOHNSITZ, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
		this.gesuchServiceResolver = WohnsitzCalcRule::resolveGesuchServiceFromCDI;
	}

	/**
	 * for testing only
	 * @param validityPeriod
	 * @param locale
	 * @param gesuchServcieResolver
	 */
	WohnsitzCalcRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale,
			@Nonnull Supplier<GesuchService> gesuchServcieResolver
	) {
		super(RuleKey.WOHNSITZ, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
		this.gesuchServiceResolver = gesuchServcieResolver;
	}


	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	protected void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		if (hasDoppelBetreuung(platz, inputData)) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.setAnspruchSinktDuringMonat(true);
			inputData.addBemerkung(MsgKey.UMZUG_BG_BEREITS_IN_ANDERER_GEMEINDE, getLocale());
			return;
		}

		if (inputData.isWohnsitzNichtInGemeindeGS1()) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkung(
				MsgKey.WOHNSITZ_MSG,
				getLocale(),
				platz.extractGesuch().getDossier().getGemeinde().getName());
		}
	}

	private boolean hasDoppelBetreuung(AbstractPlatz platz, BGCalculationInput inputData) {
		// KIBON_1843 2 Ative gesuche in unterschiedlichen gemeinden möglich
		if (!inputData.getPotentielleDoppelBetreuung()) {
			return false;
		}
		if (!(platz instanceof Betreuung)) {
			return false;
		}
		Betreuung betreuung = (Betreuung) platz;
		List<Gesuch> allGesucheForFallNummer = getAllGesucheForFallAndGesuchsperiode(
				platz.extractGesuch().getFall(), platz.extractGesuchsperiode(), betreuung.extractGemeinde());
		// Pro Kind im Kindcontainer(dossier)
		List<Betreuung> allRelevanteBetreuungen = getAllRelevantBetreuungenForKind(allGesucheForFallNummer, betreuung);
		return hasVerfuegteBetreuungInSamePeriode(allRelevanteBetreuungen);
	}

	private boolean hasVerfuegteBetreuungInSamePeriode(List<Betreuung> allBetreuungenProKind) {
		if (allBetreuungenProKind.size() <= 1) {
			return false;
		}
		return istEineBetreuungVerfuegt(allBetreuungenProKind);
	}

	private List<Betreuung> getAllRelevantBetreuungenForKind(List<Gesuch> gesuche, Betreuung relevantBetreuung) {
		List<Betreuung> alleBetreuungen = getAlleBetreuungen(gesuche);
		String relevantIdentifier = constructSortIdentifier(relevantBetreuung.getKind(), relevantBetreuung);

		List<Betreuung> relevanteBetreuungen = new ArrayList<>();
		for (Betreuung betreuung : alleBetreuungen) {
			KindContainer kc = betreuung.getKind();
			String identifier = constructSortIdentifier(kc, betreuung);
			if (relevantIdentifier.equals(identifier)) {
				relevanteBetreuungen.add(betreuung);
			}
		}
		return relevanteBetreuungen;
	}

	private List<Betreuung> getAlleBetreuungen(List<Gesuch> alleGesuche) {
		List<Betreuung> alleBetreuungen = new ArrayList<>();
		if (null == alleGesuche){
			return alleBetreuungen;
		}
		for (Gesuch gesuch : alleGesuche) {
			for (KindContainer kc : gesuch.getKindContainers()) {
				alleBetreuungen.addAll(kc.getBetreuungen());
			}
		}
		return alleBetreuungen;
	}

	private boolean istEineBetreuungVerfuegt(List<Betreuung> bList) {
		for (Betreuung b : bList) {
			if (b.getVerfuegung() != null) {
				return true;
			}
		}
		return false;
	}
	private String constructSortIdentifier(KindContainer kc, Betreuung betreuung) {
		final char SPACER = '-';
		//für die Lesbarkeit :-) nur ein String mit
		// <JAHR><Monat><Tag>-<Nachnmae>-<Vorname>-<InstitutionsID>
		return kc.getKindJA().getGeburtsdatum().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + SPACER
			+ kc.getKindJA().getNachname() + SPACER
			+ kc.getKindJA().getVorname() + SPACER
			+ betreuung.getInstitutionStammdaten().getInstitution().getId();
	}

	private List<Gesuch> getAllGesucheForFallAndGesuchsperiode(
			@Nonnull Fall fall,
			@Nonnull Gesuchsperiode gesuchsperiode,
			@Nonnull Gemeinde gemeinde) {
		GesuchService gesuchService = this.gesuchServiceResolver.get();
		return gesuchService.getAllGesuchForFallAndGesuchsperiodeInUnterschiedlichenGemeinden(fall, gesuchsperiode, gemeinde);
	}

	private static GesuchService resolveGesuchServiceFromCDI() {
		return CDI.current().select(GesuchService.class).get();
	}
}
