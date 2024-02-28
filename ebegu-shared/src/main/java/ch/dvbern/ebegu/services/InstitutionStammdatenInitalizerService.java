package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;

import javax.annotation.Nullable;

public interface InstitutionStammdatenInitalizerService {

	InstitutionStammdaten initInstitutionStammdatenBetreuungsgutschein();

	InstitutionStammdaten initInstitutionStammdatenTagesschule(@Nullable String gemeindeId);

	InstitutionStammdaten initInstitutionStammdatenFerieninsel(@Nullable String gemeindeId);
}
