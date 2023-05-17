package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

public class SolothurnTestfallDataProvider extends AbstractTestfallDataProvider {

	protected SolothurnTestfallDataProvider(Gesuchsperiode gesuchsperiode) {
		super(gesuchsperiode);
	}

	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		return familiensituation;
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		return familiensituation;
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen) {
		FinanzielleSituation finanzielleSituation = createDefaultFinanzielleSituation();
		finanzielleSituation.setMomentanSelbststaendig(true);
		finanzielleSituation.setNettolohn(einkommen);
		finanzielleSituation.setSteuerbaresVermoegen(vermoegen);
		finanzielleSituation.setUnterhaltsBeitraege(BigDecimal.ZERO);
		finanzielleSituation.setAbzuegeKinderAusbildung(BigDecimal.ZERO);
		return finanzielleSituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.SOLOTHURN;
	}
}
