package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import org.apache.commons.lang.NotImplementedException;

import javax.annotation.Nullable;

public class MinPensumEinstellungKeyBetreuungsTypVisitor implements BetreuungsangebotTypVisitor<EinstellungKey> {

	@Nullable
	public EinstellungKey getEinstellungenKey(BetreuungsangebotTyp betreuungsangebotTyp) {
		if (betreuungsangebotTyp == null) {
			return null;
		}

		return betreuungsangebotTyp.accept(this);
	}

	@Override
	public EinstellungKey visitKita() {
		return EinstellungKey.PARAM_PENSUM_KITA_MIN;
	}

	@Override
	public EinstellungKey visitTagesfamilien() {
		return EinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN;
	}

	@Override
	public EinstellungKey visitMittagtisch() {
		throw new NotImplementedException();
	}

	@Override
	public EinstellungKey visitTagesschule() {
		return EinstellungKey.PARAM_PENSUM_TAGESSCHULE_MIN;
	}

	@Nullable
	@Override
	public EinstellungKey visitFerieninsel() {
		return null;
	}
}
