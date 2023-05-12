package ch.dvbern.ebegu.testfaelle.dataprovider;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
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

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.LUZERN;
	}

	private void setAuszahlungsdatenInforma(@NotNull Familiensituation familiensituation) {
		if (familiensituation.getAuszahlungsdaten() == null) {
			familiensituation.setAuszahlungsdaten(createDefaultAuszahlungsdaten());
		}

		familiensituation.getAuszahlungsdaten().setInfomaKreditorennummer("0010");
		familiensituation.getAuszahlungsdaten().setInfomaBankcode("00-1-00");
	}
}
