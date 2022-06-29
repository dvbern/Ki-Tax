package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Zahlung;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BANKCODE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BUCHUNGSKREIS;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DATE_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.INSTITUTIONELLE_GLIEDERUNG;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.NEWLINE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.SEPARATOR;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.STAMMDATEN_BELEGART;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.ZEILENART_STAMMDATEN;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.decimalFormat;

public abstract class InfomaStammdaten {

	private final Zahlung zahlung;
	private final String belegnummer;

	public InfomaStammdaten(@NonNull Zahlung zahlung, @NonNull String belegnummer) {
		this.zahlung = zahlung;
		this.belegnummer = belegnummer; // TODO Unique ueber alle Zahlungen, startend bei BGR200001
	}

	@Nonnull
	public String toString() {
		String[] args = new String[72];
		args[0] = ZEILENART_STAMMDATEN;
		args[1] = STAMMDATEN_BELEGART;
		args[2] = belegnummer;
		args[3] = zahlung.getId();
		args[4] = DATE_FORMAT.format(zahlung.getZahlungsauftrag().getDatumFaellig());
		args[6] = getKontoart();
		args[7] = getKontonummer();
		args[11] = zahlung.getEmpfaengerName() + " - " + zahlung.getZahlungsauftrag().getBeschrieb();
		args[12] = BUCHUNGSKREIS;
		args[13] = INSTITUTIONELLE_GLIEDERUNG;
		args[16] = getDimensionswert3();
		args[30] = decimalFormat().format(zahlung.getBetragTotalZahlung());
		args[32] = getFaelligkeitsdatum() != null ? DATE_FORMAT.format(getFaelligkeitsdatum()) : null;
		args[63] = BANKCODE;
		args[68] = "BG " + zahlung.getZahlungsauftrag().getDatumGeneriert().getYear() + ", " + zahlung.getZahlungsauftrag().getDatumGeneriert().getMonthValue() + ", " + zahlung.getEmpfaengerName();

		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}

	@Nonnull
	protected Zahlung getZahlung() {
		return zahlung;
	}

	@Nonnull
	protected abstract String getKontoart();

	@Nonnull
	protected abstract String getKontonummer();

	@Nullable
	protected abstract String getDimensionswert3();

	@Nullable
	protected abstract LocalDate getFaelligkeitsdatum();
}
