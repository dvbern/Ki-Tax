package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class BGParameterVisitor implements MandantVisitor<BGRechnerParameterDTO> {

	public BGRechnerParameterDTO getBGParameterForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}
	@Override
	public BGRechnerParameterDTO visitBern() {
		return TestUtils.getParameter();
	}

	@Override
	public BGRechnerParameterDTO visitLuzern() {
		return TestUtils.getRechnerParameterLuzern();
	}

	@Override
	public BGRechnerParameterDTO visitSolothurn() {
		return TestUtils.getParameter();
	}

	@Override
	public BGRechnerParameterDTO visitAppenzellAusserrhoden() {
		return TestUtils.getRechnerParamterAppenzell();
	}
}
