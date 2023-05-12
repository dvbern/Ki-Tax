package ch.dvbern.ebegu.testfaelle.dataprovider;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

public class AppenzellTestfallDataProvider extends AbstractTestfallDataProvider {
	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.APPENZELL);
		familiensituation.setGeteilteObhut(Boolean.TRUE);
		familiensituation.setGemeinsamerHaushaltMitObhutsberechtigterPerson(Boolean.TRUE);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		return familiensituation;
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.APPENZELL);
		familiensituation.setGeteilteObhut(Boolean.FALSE);
		familiensituation.setGemeinsamerHaushaltMitPartner(Boolean.FALSE);
		return familiensituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.APPENZELL;
	}
}
