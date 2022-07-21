package ch.dvbern.ebegu.services.zahlungen;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaFooter;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaHeader;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaStammdatenFinanzbuchhaltung;
import ch.dvbern.ebegu.services.zahlungen.infoma.InfomaStammdatenZahlung;

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
		Objects.requireNonNull(zahlungsauftrag.getMandant());
		// Die nextInfomaBelegnummer darf erst bei der Freigabe der Zahlung auf dem Mandant
		// hochgezaehlt werden!
		long nextInfomaBelegnummer = zahlungsauftrag.getMandant().getNextInfomaBelegnummer();

		StringBuilder sb = new StringBuilder();
		sb.append(InfomaHeader.with(isDevmode, currentUsername));
		final List<Zahlung> zahlungenSorted = zahlungsauftrag.getZahlungen()
			.stream()
			.filter(zahlung -> zahlung.getBetragTotalZahlung().signum() == 1)
			.sorted()
			.collect(Collectors.toList());
		for (Zahlung zahlung : zahlungenSorted) {
			// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
			if (!zahlung.getAuszahlungsdaten().isZahlungsinformationValid(true)) {
				throw new EbeguRuntimeException(KibonLogLevel.INFO,
					"wrapZahlungsauftrag",
					zahlungsauftrag.getZahlungslaufTyp() == ZahlungslaufTyp.GEMEINDE_INSTITUTION
						? ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_INSTITUTION_INCOMPLETE
						: ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_ANTRAGSTELLER_INCOMPLETE,
					zahlung.getEmpfaengerName());
			}

			sb.append(InfomaStammdatenZahlung.with(zahlung, nextInfomaBelegnummer));
			sb.append(InfomaStammdatenFinanzbuchhaltung.with(zahlung, nextInfomaBelegnummer));
			nextInfomaBelegnummer++;
		}
		sb.append(InfomaFooter.with(zahlungenSorted.size(), zahlungsauftrag.getBetragTotalAuftrag()));
		return sb.toString().getBytes(StandardCharsets.US_ASCII);
	}

	@Override
	@Nonnull
	public GeneratedDokumentTyp getGeneratedDokumentTyp() {
		return GeneratedDokumentTyp.INFOMA;
	}
}
