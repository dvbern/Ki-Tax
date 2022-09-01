package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.Month;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BELEGNUMMER_PRAEFIX;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BUCHUNGSKREIS;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DATE_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.INSTITUTIONELLE_GLIEDERUNG;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.NEWLINE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.SEPARATOR;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.STAMMDATEN_BELEGART;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.ZEILENART_STAMMDATEN;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaUtil.normalize;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaUtil.normalizeAndAbbreviate;

@SuppressWarnings("FieldCanBeLocal")
public abstract class InfomaStammdaten {

	private final String belegnummer;
	private final String externeNummer;
	private final String buchungsdatum;
	private final String kontoart;
	private final String kontonummer;
	private final String buchungstext;
	private final String dimensionswert3;
	private final String betrag;
	private final String faelligkeitsdatum;
	private final String bankcode;
	private final String kundenspezifischesFeld2;

	protected InfomaStammdaten(@NonNull Zahlung zahlung, long belegnummer, Locale locale) {
		this.belegnummer = BELEGNUMMER_PRAEFIX + belegnummer;
		this.externeNummer = getBgNummer(zahlung);
		this.buchungsdatum = DATE_FORMAT.format(zahlung.getZahlungsauftrag().getDatumFaellig());
		this.kontoart = getKontoart();
		this.kontonummer = getKontonummer(zahlung);
		this.buchungstext = getBuchungstext(zahlung);
		this.dimensionswert3 = getDimensionswert3();
		this.betrag = getBetrag(zahlung);
		this.faelligkeitsdatum = getFaelligkeitsdatum(zahlung);
		this.bankcode = getBankCode(zahlung);
		this.kundenspezifischesFeld2 = getKundenspezifischesFeld2(zahlung, locale);
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
	protected abstract String getBankCode(@Nonnull Zahlung zahlung);

	@Nonnull
	private String getBuchungstext(Zahlung zahlung) {
		return zahlung.getEmpfaengerName() + " - " + zahlung.getZahlungsauftrag().getBeschrieb();
	}

	@Nonnull
	private String getKundenspezifischesFeld2(@NotNull Zahlung zahlung, Locale locale) {
		final String kontoinhaber = zahlung.getAuszahlungsdaten().getKontoinhaber();
		final Month monthValueDatumGeneriert = zahlung.getZahlungsauftrag().getDatumGeneriert().getMonth();
		final int yearValueDatumGeneriert = zahlung.getZahlungsauftrag().getDatumGeneriert().getYear();

		// Just to keep IDE happy. auf Zahlungsauftrag ist Mandant NotNull im Interface nullable.
		Objects.requireNonNull(zahlung.getZahlungsauftrag().getMandant());

		String monthBezeichnung = ServerMessageUtil
			.translateEnumValue(monthValueDatumGeneriert, locale, zahlung.getZahlungsauftrag().getMandant());
		return kontoinhaber + ", Betreuungsgutscheine " + monthBezeichnung + ' ' + yearValueDatumGeneriert;
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
		args[0] = ZEILENART_STAMMDATEN;
		args[1] = STAMMDATEN_BELEGART;
		args[2] = normalizeAndAbbreviate(belegnummer, 20);
		args[3] = normalizeAndAbbreviate(externeNummer, 20);
		args[4] = buchungsdatum;
		args[6] = normalize(kontoart);
		args[7] = normalize(kontonummer);
		args[11] = normalizeAndAbbreviate(buchungstext, 120);
		args[12] = BUCHUNGSKREIS;
		args[13] = INSTITUTIONELLE_GLIEDERUNG;
		args[16] = normalizeAndAbbreviate(dimensionswert3, 20);
		args[30] = betrag;
		args[32] = faelligkeitsdatum;
		args[63] = bankcode;
		args[68] = normalize(kundenspezifischesFeld2);

		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}
}
