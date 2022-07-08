package ch.dvbern.ebegu.services.zahlungen.infoma;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Zahlung;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BANKCODE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BELEGNUMMER_PRAEFIX;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BUCHUNGSKREIS;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DATE_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.INSTITUTIONELLE_GLIEDERUNG;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.NEWLINE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.SEPARATOR;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.STAMMDATEN_BELEGART;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.ZEILENART_STAMMDATEN;

public abstract class InfomaStammdaten {

	private final String zeilenart = ZEILENART_STAMMDATEN;
	private final String belegart = STAMMDATEN_BELEGART;
	private final String belegnummer;
	private final String externeNummer;
	private final String buchungsdatum;
	private final String kontoart;
	private final String kontonummer;
	private final String buchungstext;
	private final String buchungskreis = BUCHUNGSKREIS;
	private final String institutionelleGliederung = INSTITUTIONELLE_GLIEDERUNG;
	private final String dimensionswert3;
	private final String betrag;
	private final String faelligkeitsdatum;
	private final String bankcode = BANKCODE;
	private final String kundenspezifischesFeld2;

	protected InfomaStammdaten(@NonNull Zahlung zahlung, long belegnummer) {
		this.belegnummer = BELEGNUMMER_PRAEFIX + belegnummer;
		this.externeNummer = getBgNummer(zahlung);
		this.buchungsdatum = DATE_FORMAT.format(zahlung.getZahlungsauftrag().getDatumFaellig());
		this.kontoart = getKontoart();
		this.kontonummer = getKontonummer(zahlung);
		this.buchungstext = getBuchungstext(zahlung);
		this.dimensionswert3 = getDimensionswert3();
		this.betrag = getBetrag(zahlung);
		this.faelligkeitsdatum = getFaelligkeitsdatum(zahlung);
		this.kundenspezifischesFeld2 = getKundenspezifischesFeld2(zahlung);
	}

	@Nonnull
	protected abstract String getKontoart();

	@Nonnull
	protected abstract String getKontonummer(@Nonnull Zahlung zahlung);

	@Nullable
	protected abstract String getDimensionswert3();

	@Nonnull
	protected abstract String getBetrag(@Nonnull Zahlung zahlung);

	@Nullable
	protected abstract String getFaelligkeitsdatum(@Nonnull Zahlung zahlung);

	@Nonnull
	private String getBuchungstext(Zahlung zahlung) {
		return zahlung.getEmpfaengerName() + " - " + zahlung.getZahlungsauftrag().getBeschrieb();
	}

	@Nonnull
	private String getKundenspezifischesFeld2(Zahlung zahlung) {
		return "BG "
			+ zahlung.getZahlungsauftrag().getDatumGeneriert().getYear()
			+ ", "
			+ zahlung.getZahlungsauftrag().getDatumGeneriert().getMonthValue()
			+ ", "
			+ zahlung.getEmpfaengerName();
	}

	@Nonnull
	private String getBgNummer(@Nonnull Zahlung zahlung) {
		return zahlung
			.getZahlungspositionen()
			.stream()
			.findFirst()
			.get()
			.getVerfuegungZeitabschnitt()
			.getVerfuegung()
			.getPlatz()
			.getBGNummer();
	}

	@Nonnull
	public String toString() {
		String[] args = new String[72];
		args[0] = zeilenart;
		args[1] = belegart;
		args[2] = StringUtils.abbreviate(belegnummer, 20);
		args[3] = StringUtils.abbreviate(externeNummer, 20);
		args[4] = buchungsdatum;
		args[6] = kontoart;
		args[7] = kontonummer;
		args[11] = StringUtils.abbreviate(buchungstext, 120);
		args[12] = StringUtils.abbreviate(buchungskreis, 20);
		args[13] = StringUtils.abbreviate(institutionelleGliederung, 20);
		args[16] = StringUtils.abbreviate(dimensionswert3, 20);
		args[30] = betrag;
		args[32] = faelligkeitsdatum;
		args[63] = bankcode;
		args[68] = kundenspezifischesFeld2;

		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}
}
