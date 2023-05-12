package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

public abstract class AbstractTestfallDataProvider {

	public abstract Familiensituation createVerheiratet();
	public abstract Familiensituation createAlleinerziehend();

	public abstract FinanzielleSituation createFinanzielleSituation(BigDecimal vermoegen, BigDecimal einkommen);

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

	protected FinanzielleSituation createDefaultFinanzielleSituation() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setSteuerveranlagungErhalten(true);
		finanzielleSituation.setSteuererklaerungAusgefuellt(true);
		setFinSitDefaultValues(finanzielleSituation);
		return finanzielleSituation;
	}

	/**
	 * Schreibt in alle Felder der finanziellenSituation, die nicht Null sein dürfen, eine 0. Diese kann später in den
	 * Testfällen überschrieben werden.
	 */
	private void setFinSitDefaultValues(@Nonnull FinanzielleSituation finanzielleSituation) {
		finanzielleSituation.setFamilienzulage(BigDecimal.ZERO);
		finanzielleSituation.setErsatzeinkommen(BigDecimal.ZERO);
		finanzielleSituation.setErhalteneAlimente(BigDecimal.ZERO);
		finanzielleSituation.setGeleisteteAlimente(BigDecimal.ZERO);
		finanzielleSituation.setNettolohn(BigDecimal.ZERO);
		finanzielleSituation.setBruttovermoegen(BigDecimal.ZERO);
		finanzielleSituation.setSchulden(BigDecimal.ZERO);
	}

}

