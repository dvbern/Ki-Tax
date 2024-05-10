/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.rules.MonatsRule;
import ch.dvbern.ebegu.test.TestDataUtil;

import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;

public final class MutationsMergerTestUtil {

	private static final int DEFAULT_PENSUM = 80;

	private static final MonatsRule MONATS_RULE = new MonatsRule(false);

	private MutationsMergerTestUtil() {
	}

	static Verfuegung prepareErstGesuchVerfuegung(int pbPensum, BigDecimal massgegebenesEinkommenVorAbzug) {
		Betreuung erstgesuchBetreuung = prepareData(massgegebenesEinkommenVorAbzug, AntragTyp.ERSTGESUCH, pbPensum, START_PERIODE);
		return prepareVerfuegungForBetreuung(erstgesuchBetreuung);
	}

	static Verfuegung prepareVerfuegungForBetreuung(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(betreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch =
			EbeguRuleTestsHelper.runSingleAbschlussRule(MONATS_RULE, betreuung, zabetrErtgesuch);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		betreuung.setVerfuegung(verfuegungErstgesuch);
		betreuung.extractGesuch().setTimestampVerfuegt(LocalDateTime.now());
		verfuegungErstgesuch.setBetreuung(betreuung);
		return verfuegungErstgesuch;
	}

	static Betreuung prepareData(BigDecimal massgebendesEinkommen, AntragTyp antragTyp) {
		return prepareData(massgebendesEinkommen, antragTyp, DEFAULT_PENSUM, START_PERIODE);
	}

	static Betreuung prepareData(
		BigDecimal massgebendesEinkommen,
		AntragTyp antragTyp,
		int bpPensum,
		LocalDate aenderungsDatumBpPensum) {
		Betreuung betreuung =
			EbeguRuleTestsHelper.createBetreuungWithPensum(START_PERIODE, TestDataUtil.ENDE_PERIODE,
				BetreuungsangebotTyp.KITA, 100, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(antragTyp);
		Set<KindContainer> kindContainers = new LinkedHashSet<>();
		final KindContainer kindContainer = betreuung.getKind();
		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		kindContainer.setBetreuungen(betreuungen);
		kindContainers.add(betreuung.getKind());
		gesuch.setKindContainers(kindContainers);

		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.getFinanzDatenDTO().setMassgebendesEinkBjVorAbzFamGr(massgebendesEinkommen);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
			aenderungsDatumBpPensum, TestDataUtil.ENDE_PERIODE, bpPensum));

		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettolohn(massgebendesEinkommen);
		return betreuung;
	}
}
