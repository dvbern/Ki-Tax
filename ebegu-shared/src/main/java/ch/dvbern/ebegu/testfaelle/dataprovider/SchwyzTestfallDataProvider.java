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

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

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
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		familiensituation.setGemeinsameSteuererklaerung(false);
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
		finanzielleSituation.setSteuerbaresEinkommen(einkommen);
		finanzielleSituation.setSteuerbaresVermoegen(vermoegen);
		finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ZERO);
		finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ZERO);
		return finanzielleSituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.SCHWYZ;
	}
}
