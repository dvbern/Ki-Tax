package ch.dvbern.ebegu.testfaelle.dataprovider;

import ch.dvbern.ebegu.entities.Familiensituation;

public abstract class AbstractTestfallDataProvider {

	public abstract Familiensituation createVerheiratet();
	public abstract Familiensituation createAlleinerziehend();

	protected Familiensituation createDefaultFieldsOfFamiliensituation() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setSozialhilfeBezueger(false);
		familiensituation.setVerguenstigungGewuenscht(true); // by default verguenstigung gewuenscht
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(true);
		return familiensituation;
	}

}

