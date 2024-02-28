package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.services.InstitutionStammdatenInitalizerService;
import com.sun.istack.NotNull;

import javax.annotation.Nullable;

public class InstitutionStammdatenInitalizerVisitor implements BetreuungsangebotTypVisitor<InstitutionStammdaten> {

	private final InstitutionStammdatenInitalizerService institutionStammdatenInitalizerService;
	private final String gemeindeId;

	public InstitutionStammdatenInitalizerVisitor(InstitutionStammdatenInitalizerService initalizerService, @Nullable String gemeindeId) {
		this.institutionStammdatenInitalizerService = initalizerService;
		this.gemeindeId = gemeindeId;
	}

	public InstitutionStammdaten initalizeInstiutionStammdaten(@NotNull BetreuungsangebotTyp betreuungsangebotTyp) {
		return betreuungsangebotTyp.accept(this);
	}

	@Override
	public InstitutionStammdaten visitKita() {
		return institutionStammdatenInitalizerService.initInstitutionStammdatenBetreuungsgutschein();
	}

	@Override
	public InstitutionStammdaten visitTagesfamilien() {
		return institutionStammdatenInitalizerService.initInstitutionStammdatenBetreuungsgutschein();
	}

	@Override
	public InstitutionStammdaten visitMittagtisch() {
		return institutionStammdatenInitalizerService.initInstitutionStammdatenBetreuungsgutschein();
	}

	@Override
	public InstitutionStammdaten visitTagesschule() {
		return institutionStammdatenInitalizerService.initInstitutionStammdatenTagesschule(gemeindeId);
	}

	@Override
	public InstitutionStammdaten visitFerieninsel() {
		return institutionStammdatenInitalizerService.initInstitutionStammdatenFerieninsel(gemeindeId);
	}
}
