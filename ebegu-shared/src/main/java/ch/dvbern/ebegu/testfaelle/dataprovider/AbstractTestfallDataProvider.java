package ch.dvbern.ebegu.testfaelle.dataprovider;

import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

public abstract class AbstractTestfallDataProvider {

	public abstract Familiensituation createVerheiratet();
	public abstract Familiensituation createAlleinerziehend();

	public abstract FinanzielleSituationTyp getFinanzielleSituationTyp();

	protected Familiensituation createDefaultFieldsOfFamiliensituation() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setSozialhilfeBezueger(false);
		familiensituation.setVerguenstigungGewuenscht(true); // by default verguenstigung gewuenscht
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(true);
		familiensituation.setAuszahlungsdaten(createDefaultAuszahlungsdaten());
		return familiensituation;
	}

	protected Auszahlungsdaten createDefaultAuszahlungsdaten() {
		Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		auszahlungsdaten.setIban(new IBAN("CH2089144969768441935"));
		auszahlungsdaten.setKontoinhaber("kiBon Test");
		return auszahlungsdaten;
	}

}

