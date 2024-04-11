package ch.dvbern.ebegu.finanzielleSituationRechner;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;

import javax.annotation.Nonnull;

public class FinanzielleSituationSchwyzRechner extends AbstractFinanzielleSituationRechner {
	@Override
	public void setFinanzielleSituationParameters(@Nonnull Gesuch gesuch, FinanzielleSituationResultateDTO finSitResultDTO, boolean hasSecondGesuchsteller) {

	}

	@Override
	public void setEinkommensverschlechterungParameters(@Nonnull Gesuch gesuch, int basisJahrPlus, FinanzielleSituationResultateDTO einkVerResultDTO, boolean hasSecondGesuchsteller) {

	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		return false;
	}
}
