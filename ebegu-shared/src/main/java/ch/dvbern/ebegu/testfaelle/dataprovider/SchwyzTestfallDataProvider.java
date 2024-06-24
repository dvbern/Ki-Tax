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

package ch.dvbern.ebegu.testfaelle.dataprovider;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SchwyzTestfallDataProvider extends AbstractTestfallDataProvider {
	protected SchwyzTestfallDataProvider(Gesuchsperiode gesuchsperiode) {
		super(gesuchsperiode);
	}

	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setVerguenstigungGewuenscht(true);
		familiensituation.setSozialhilfeBezueger(false);
		familiensituation.setAuszahlungsdaten(createDefaultAuszahlungsdaten());
		familiensituation.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(true);
		return familiensituation;
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setVerguenstigungGewuenscht(true);
		familiensituation.setSozialhilfeBezueger(false);
		familiensituation.setAuszahlungsdaten(createDefaultAuszahlungsdaten());
		familiensituation.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(true);
		return familiensituation;
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen) {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		// required in all finsit
		finanzielleSituation.setSteuerveranlagungErhalten(true);
		// required in all finsit
		finanzielleSituation.setSteuererklaerungAusgefuellt(true);
		finanzielleSituation.setQuellenbesteuert(false);
		applyVerfuegt(finanzielleSituation, vermoegen, einkommen, BigDecimal.ZERO, BigDecimal.ZERO);

		return finanzielleSituation;
	}

	public static void applyVerfuegt(
		AbstractFinanzielleSituation finanzielleSituation,
		BigDecimal vermoegen,
		BigDecimal einkommen,
		BigDecimal abzuegeLiegenschaft,
		BigDecimal einkaeufeVorsorge
	) {
		finanzielleSituation.setSteuerbaresEinkommen(einkommen);
		finanzielleSituation.setSteuerbaresVermoegen(vermoegen);
		finanzielleSituation.setAbzuegeLiegenschaft(abzuegeLiegenschaft);
		finanzielleSituation.setEinkaeufeVorsorge(einkaeufeVorsorge);
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.SCHWYZ;
	}

	@Override
	public Kind createKind(
		Geschlecht geschlecht,
		String name,
		String vorname,
		LocalDate geburtsdatum,
		boolean is18GeburtstagBeforeGPEnds,
		Kinderabzug kinderabzug,
		boolean betreuung) {
		Kind kind = new Kind();
		setSchwyzKindData(TestKindParameter.builder()
			.kind(kind)
			.geschlecht(geschlecht)
			.name(name)
			.vorname(vorname)
			.geburtsdatum(geburtsdatum)
			.betreuung(betreuung)
			.build());
		return kind;
	}

	public static void setSchwyzKindData(TestKindParameter testKindParameter) {
		final Kind kind = testKindParameter.getKind();

		kind.setGeschlecht(testKindParameter.getGeschlecht());
		kind.setGeburtsdatum(testKindParameter.getGeburtsdatum());
		kind.setVorname(testKindParameter.getVorname());
		kind.setNachname(testKindParameter.getName());
		kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		kind.setSprichtAmtssprache(true);
		kind.setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		if (testKindParameter.isBetreuung()) {
			kind.setFamilienErgaenzendeBetreuung(true);
			kind.setUnterhaltspflichtig(true);
			kind.setLebtKindAlternierend(true);
			kind.setGemeinsamesGesuch(true);
		}
	}

	@Override
	public Gesuchsteller createGesuchsteller(String name, String vorname, int gesuchstellerNumber) {
		Gesuchsteller gesuchsteller = super.createGesuchsteller(name, vorname, gesuchstellerNumber);
		if (gesuchstellerNumber == 1) {
			gesuchsteller.setSozialversicherungsnummer("756.1234.5678.97");
		} else {
			gesuchsteller.setSozialversicherungsnummer("756.1238.5678.93");
		}
		return gesuchsteller;
	}
}
