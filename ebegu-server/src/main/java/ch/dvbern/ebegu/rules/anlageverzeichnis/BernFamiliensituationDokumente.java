package ch.dvbern.ebegu.rules.anlageverzeichnis;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.util.EbeguUtil;

public class BernFamiliensituationDokumente extends AbstractFamiliensituationDokumente {

	@Override
	protected boolean isUnterstuetzungsbestaetigungNeeded(Familiensituation familiensituation) {
		return !EbeguUtil.isNullOrFalse(familiensituation.getSozialhilfeBezueger());
	}
}
