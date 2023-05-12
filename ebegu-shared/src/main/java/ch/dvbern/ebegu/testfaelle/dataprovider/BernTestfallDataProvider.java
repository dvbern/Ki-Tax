package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;

public abstract class BernTestfallDataProvider extends AbstractTestfallDataProvider {

	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		return familiensituation;
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen) {
		FinanzielleSituation finanzielleSituation = createDefaultFinanzielleSituation();
		finanzielleSituation.setSteuerdatenZugriff(false);
		finanzielleSituation.setNettolohn(einkommen);
		finanzielleSituation.setBruttovermoegen(vermoegen);
		return finanzielleSituation;
	}

}
