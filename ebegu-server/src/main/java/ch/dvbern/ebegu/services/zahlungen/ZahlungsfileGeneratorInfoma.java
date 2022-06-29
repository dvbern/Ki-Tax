package ch.dvbern.ebegu.services.zahlungen;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaFooter;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaHeader;

@Dependent
public class ZahlungsfileGeneratorInfoma implements IZahlungsfileGenerator {

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private EbeguConfiguration ebeguConfiguration;


	@Override
	@Nonnull
	public byte[] generateZahlungfile(
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Locale locale
	) {
		final String currentUsername = principalBean.getBenutzer().getUsername();
		final boolean isDevmode = ebeguConfiguration.getIsDevmode();

		StringBuilder sb = new StringBuilder();
		sb.append(InfomaHeader.with(isDevmode, currentUsername));
		// TODO Je eine Zeile fuer Zahlung und Finanzbuchhaltung
		sb.append(InfomaFooter.with(zahlungsauftrag.getZahlungen().size(), zahlungsauftrag.getBetragTotalAuftrag()));
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}

	@Override
	@Nonnull
	public GeneratedDokumentTyp getGeneratedDokumentTyp() {
		return GeneratedDokumentTyp.INFOMA;
	}
}
