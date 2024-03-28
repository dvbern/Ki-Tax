package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

public class SchwyzTestfallDataProvider extends AbstractTestfallDataProvider {

	protected SchwyzTestfallDataProvider(Gesuchsperiode gesuchsperiode) {
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
