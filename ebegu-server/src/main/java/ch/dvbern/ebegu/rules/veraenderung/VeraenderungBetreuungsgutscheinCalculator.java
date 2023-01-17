package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;

public class VeraenderungBetreuungsgutscheinCalculator extends VeraenderungCalculator {

	@Override
	public BigDecimal calculateVeraenderung(
		@NotNull List<VerfuegungZeitabschnitt> zeitabschnitte,
		@NotNull Verfuegung vorgaengerVerfuegung) {

		BigDecimal totalVerguenstigungVorgaenger = addUpVerguenstigung(vorgaengerVerfuegung.getZeitabschnitte());
		BigDecimal totalVerguenstigungAktuell = addUpVerguenstigung(zeitabschnitte);

		return totalVerguenstigungAktuell.subtract(totalVerguenstigungVorgaenger);
	}

	@Override
	protected boolean isVerfuegungIgnorable(BigDecimal veraenderung) {
		//Wenn der Betreuungsgutschein sinkt, darf die Verfügung ignoriert werden (BG Veränderung zu Ungusten der Eltern)
		return veraenderung.compareTo(BigDecimal.ZERO) <= 0;
	}

	@Override
	public void calculateKorrekturAusbezahlteVerguenstigung(AbstractPlatz platz) {
		if (platz.getVerfuegungOrVerfuegungPreview() == null || platz.getVorgaengerVerfuegung() == null) {
			return;
		}

		Verfuegung vorgaenger = platz.getVorgaengerVerfuegung();
		Verfuegung aktuell = platz.getVerfuegungOrVerfuegungPreview();

		BigDecimal korrekturInstitutionen = calculateKorrekturAusbazahlteVerguenstigung(aktuell, vorgaenger, ZahlungslaufTyp.GEMEINDE_INSTITUTION);
		BigDecimal korrekturEltern = calculateKorrekturAusbazahlteVerguenstigung(aktuell, vorgaenger, ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER);

		aktuell.setKorrekturAusbezahltEltern(korrekturEltern);
		aktuell.setKorrekturAusbezahltInstitution(korrekturInstitutionen);
	}

	private BigDecimal calculateKorrekturAusbazahlteVerguenstigung(Verfuegung aktuell, Verfuegung vorgaenger, ZahlungslaufTyp zahlungslaufTyp) {
		ZahlungslaufHelper helper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);

		//Im Vorgänger nach ausbezahlten Zeitabschnitten suchen
		List<VerfuegungZeitabschnitt> ausbezahlteZeitabschnitte = findAusbazahlteZeitabschnitte(vorgaenger, helper);

		//Wenn keine gefunden, gibt es keine Korrektur
		if (ausbezahlteZeitabschnitte.isEmpty()) {
			return BigDecimal.ZERO;
		}

		BigDecimal betragAusbezahlt = calculateAuszahlungsbetrag(ausbezahlteZeitabschnitte, helper);

		//In der aktuellen Verfügung die Zeitabchnitte suchen, welche im Vorgänger ausbezahlt sind
		List<VerfuegungZeitabschnitt> relevanteZeitabschnitteAktuelle = findRelevanteZAsInAktuellerVerfugeung(aktuell, ausbezahlteZeitabschnitte);
		BigDecimal betragAktuelleVerfuegung = calculateAuszahlungsbetrag(relevanteZeitabschnitteAktuelle, helper);
		return MathUtil.EXACT.subtract(betragAusbezahlt, betragAktuelleVerfuegung);
	}

	private List<VerfuegungZeitabschnitt> findRelevanteZAsInAktuellerVerfugeung(
		Verfuegung aktuell,
		List<VerfuegungZeitabschnitt> ausbezahlteZeitabschnitte) {
		List<Month> ausbazahlteMonate = ausbezahlteZeitabschnitte.stream()
			.map(zeitabschnitt -> zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth())
			.collect(Collectors.toList());

		return aktuell.getZeitabschnitte().stream()
			.filter(zeitabschnitt -> ausbazahlteMonate.contains(zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth()))
			.collect(Collectors.toList());
	}

	private BigDecimal calculateAuszahlungsbetrag(List<VerfuegungZeitabschnitt> zeitabschnitte, ZahlungslaufHelper helper) {
		return zeitabschnitte.stream()
			.map(zeitabschnitt -> helper.getAuszahlungsbetrag(zeitabschnitt))
			.reduce(BigDecimal.ZERO, MathUtil.EXACT::add);
	}

	private List<VerfuegungZeitabschnitt> findAusbazahlteZeitabschnitte(Verfuegung verfuegung, ZahlungslaufHelper helper) {
		return verfuegung.getZeitabschnitte().stream()
			.filter(zeitabschnitt -> !helper.getZahlungsstatus(zeitabschnitt).isNeu())
			.collect(Collectors.toList());
	}

	private BigDecimal addUpVerguenstigung(List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte.stream()
			.map(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().getVerguenstigung())
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
