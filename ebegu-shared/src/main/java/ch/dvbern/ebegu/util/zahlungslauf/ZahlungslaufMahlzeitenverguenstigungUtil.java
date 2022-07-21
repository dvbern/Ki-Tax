package ch.dvbern.ebegu.util.zahlungslauf;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

public final class ZahlungslaufMahlzeitenverguenstigungUtil {

	private ZahlungslaufMahlzeitenverguenstigungUtil() {
	}

	@Nonnull
	public static BigDecimal getAuszahlungsbetrag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		BigDecimal auszahlungsbetrag = zeitabschnitt.getRelevantBgCalculationResult().getVerguenstigungMahlzeitenTotal();
		if (auszahlungsbetrag == null) {
			auszahlungsbetrag = BigDecimal.ZERO;
		}
		return auszahlungsbetrag;
	}

	public static boolean isSameAusbezahlterBetrag(
		@Nonnull BGCalculationResult resultNeu,
		@Nonnull BGCalculationResult resultBisher
	) {
		return MathUtil.isSame(resultNeu.getVerguenstigungMahlzeitenTotal(), resultBisher.getVerguenstigungMahlzeitenTotal());
	}

	public static boolean isSamePersistedValues(@Nonnull VerfuegungZeitabschnitt abschnitt, @Nonnull VerfuegungZeitabschnitt otherAbschnitt) {
		// Fuer die Antragsteller-Auszahlungen koennen wir nicht die normale isSamePersistedValues verwenden. Dort werden die
		// Mahlzeiten nicht verglichen
		boolean isSame = MathUtil.isSame(
			abschnitt.getBgCalculationResultAsiv().getVerguenstigungMahlzeitenTotal(),
			otherAbschnitt.getBgCalculationResultAsiv().getVerguenstigungMahlzeitenTotal())
			&& (!abschnitt.isHasGemeindeSpezifischeBerechnung()
			|| (abschnitt.getBgCalculationResultGemeinde() != null
			&& 	otherAbschnitt.getBgCalculationResultGemeinde() != null
			&& MathUtil.isSame(
			abschnitt.getBgCalculationResultGemeinde().getVerguenstigungMahlzeitenTotal(),
			otherAbschnitt.getBgCalculationResultGemeinde().getVerguenstigungMahlzeitenTotal())))
			&& abschnitt.getGueltigkeit().compareTo(otherAbschnitt.getGueltigkeit()) == 0;
		return isSame;
	}

	public static boolean isAuszuzahlen(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		// Nur auszuzahlen, wenn Mahlzeitenverguenstigung beantragt
		final Familiensituation familiensituation = zeitabschnitt.getVerfuegung().getPlatz().extractGesuch().extractFamiliensituation();
		return familiensituation != null && !familiensituation.isKeineMahlzeitenverguenstigungBeantragt();
	}
}
