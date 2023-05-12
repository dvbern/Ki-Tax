package ch.dvbern.ebegu.testfaelle.dataprovider;

import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.jetbrains.annotations.NotNull;

public class LuzernTestfallDataProvider extends AbstractTestfallDataProvider {

	@Override
	public Familiensituation createVerheiratet() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		setAuszahlungsdatenInforma(familiensituation);
		return familiensituation;
	}

	@Override
	public Familiensituation createAlleinerziehend() {
		Familiensituation familiensituation = createDefaultFieldsOfFamiliensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		setAuszahlungsdatenInforma(familiensituation);
		return familiensituation;
	}

	private void setAuszahlungsdatenInforma(@NotNull Familiensituation familiensituation) {
		Auszahlungsdaten auszahlungsdatenInforma = new Auszahlungsdaten();
		auszahlungsdatenInforma.setIban(new IBAN("CH2089144969768441935"));
		auszahlungsdatenInforma.setKontoinhaber("kiBon Test");
		auszahlungsdatenInforma.setInfomaKreditorennummer("0010");
		auszahlungsdatenInforma.setInfomaBankcode("00-1-00");
		familiensituation.setAuszahlungsdaten(auszahlungsdatenInforma);
	}
}
