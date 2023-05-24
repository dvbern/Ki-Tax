package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

public class FkjvBernTestfallDataProvider extends BernTestfallDataProvider {

	protected FkjvBernTestfallDataProvider(Gesuchsperiode gesuchsperiode) {
		super(gesuchsperiode);
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGeteilteObhut(Boolean.TRUE);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		return familiensituation;
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen) {
		FinanzielleSituation finanzielleSituation = super.createFinanzielleSituation(vermoegen, einkommen);
		finanzielleSituation.setNettoertraegeErbengemeinschaft(BigDecimal.ZERO);
		finanzielleSituation.setBruttoertraegeVermoegen(BigDecimal.ZERO);
		finanzielleSituation.setEinkommenInVereinfachtemVerfahrenAbgerechnet(Boolean.FALSE);
		finanzielleSituation.setAbzugSchuldzinsen(BigDecimal.ZERO);
		finanzielleSituation.setGewinnungskosten(BigDecimal.ZERO);
		return finanzielleSituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.BERN_FKJV;
	}
}
