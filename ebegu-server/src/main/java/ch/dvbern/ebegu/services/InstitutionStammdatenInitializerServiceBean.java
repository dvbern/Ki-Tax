package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.ModulTagesschuleTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
@Local(InstitutionStammdatenInitalizerService.class)
public class InstitutionStammdatenInitializerServiceBean implements InstitutionStammdatenInitalizerService {

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Override
	public InstitutionStammdaten initInstitutionStammdatenBetreuungsgutschein() {
		InstitutionStammdaten institutionStammdaten = new InstitutionStammdaten();
		InstitutionStammdatenBetreuungsgutscheine bgStammdaten = new InstitutionStammdatenBetreuungsgutscheine();
		institutionStammdaten.setInstitutionStammdatenBetreuungsgutscheine(bgStammdaten);
		return institutionStammdaten;
	}

	@Override
	public InstitutionStammdaten initInstitutionStammdatenTagesschule(@Nullable String gemeindeId) {
		InstitutionStammdaten institutionStammdaten = new InstitutionStammdaten();
		InstitutionStammdatenTagesschule stammdatenTS = new InstitutionStammdatenTagesschule();
		stammdatenTS.setGemeinde(getGemeindeOrThrowException(gemeindeId));

		Set<EinstellungenTagesschule> einstellungenTagesschuleSet =
			gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden().stream().map(
				gesuchsperiode -> {
					EinstellungenTagesschule einstellungenTagesschule = new EinstellungenTagesschule();
					einstellungenTagesschule.setInstitutionStammdatenTagesschule(stammdatenTS);
					einstellungenTagesschule.setGesuchsperiode(gesuchsperiode);
					einstellungenTagesschule.setModulTagesschuleTyp(ModulTagesschuleTyp.DYNAMISCH);
					return einstellungenTagesschule;
				}
			).collect(Collectors.toSet());

		stammdatenTS.setEinstellungenTagesschule(einstellungenTagesschuleSet);
		institutionStammdaten.setInstitutionStammdatenTagesschule(stammdatenTS);
		return institutionStammdaten;
	}

	@Override
	public InstitutionStammdaten initInstitutionStammdatenFerieninsel(@Nullable String gemeindeId) {
		InstitutionStammdaten institutionStammdaten = new InstitutionStammdaten();
		InstitutionStammdatenFerieninsel stammdatenFI = new InstitutionStammdatenFerieninsel();
		stammdatenFI.setGemeinde(getGemeindeOrThrowException(gemeindeId));

		Set<EinstellungenFerieninsel> einstellungenFerieninselSet =
			gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden().stream().map(
				gesuchsperiode -> {
					EinstellungenFerieninsel einstellungenFerieninsel = new EinstellungenFerieninsel();
					einstellungenFerieninsel.setInstitutionStammdatenFerieninsel(stammdatenFI);
					einstellungenFerieninsel.setGesuchsperiode(gesuchsperiode);
					return einstellungenFerieninsel;
				}
			).collect(Collectors.toSet());

		stammdatenFI.setEinstellungenFerieninsel(einstellungenFerieninselSet);
		institutionStammdaten.setInstitutionStammdatenFerieninsel(stammdatenFI);
		return institutionStammdaten;
	}

	@Nonnull
	private Gemeinde getGemeindeOrThrowException(@Nullable  String gemeindeId) {
		if (gemeindeId == null) {
			throw new EbeguRuntimeException("initInstitutionStammdaten()", "missing gemeindeId");
		}

		Gemeinde gemeinde =
			gemeindeService.findGemeinde(gemeindeId)
				.orElseThrow(() -> new EbeguEntityNotFoundException("initInstitutionStammdaten",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GemeindeId invalid: " + gemeindeId));
		return gemeinde;
	}
}
