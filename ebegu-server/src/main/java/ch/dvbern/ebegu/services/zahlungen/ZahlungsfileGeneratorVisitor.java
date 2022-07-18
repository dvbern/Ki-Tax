package ch.dvbern.ebegu.services.zahlungen;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

@Dependent
public class ZahlungsfileGeneratorVisitor implements MandantVisitor<IZahlungsfileGenerator> {

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private MandantService mandantService;

	@Inject
	private ZahlungsfileGeneratorPain painGenerator;

	@Inject
	private ZahlungsfileGeneratorInfoma infomaGenerator;

	public IZahlungsfileGenerator getZahlungsfileGenerator(
		@Nonnull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public IZahlungsfileGenerator visitBern() {
		return getZahlungsfileGeneratorForMandant(MandantIdentifier.BERN);
	}

	@Override
	public IZahlungsfileGenerator visitLuzern() {
		return getZahlungsfileGeneratorForMandant(MandantIdentifier.LUZERN);
	}

	@Override
	public IZahlungsfileGenerator visitSolothurn() {
		return getZahlungsfileGeneratorForMandant(MandantIdentifier.SOLOTHURN);
	}

	@Nonnull
	private IZahlungsfileGenerator getZahlungsfileGeneratorForMandant(@Nonnull MandantIdentifier mandantIdentifier) {
		if (isInfomaZahlungenActivatedForMandant(mandantIdentifier)) {
			return infomaGenerator;
		}
		return painGenerator;
	}

	private boolean isInfomaZahlungenActivatedForMandant(@Nonnull MandantIdentifier mandantIdentifier) {
		final Mandant mandant = mandantService
			.findMandantByIdentifier(mandantIdentifier)
			.orElseThrow(() -> new EbeguEntityNotFoundException("findMandantByIdentifier", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
		ApplicationProperty infomaZahlungen  =
			this.applicationPropertyService.readApplicationProperty(
					ApplicationPropertyKey.INFOMA_ZAHLUNGEN,
					mandant)
				.orElse(null);
		return infomaZahlungen != null && Boolean.parseBoolean(infomaZahlungen.getValue());
	}
}
