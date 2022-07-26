package ch.dvbern.ebegu.services.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.util.Constants;

public final class ZahlungslaufUtil {

	private ZahlungslaufUtil() {
	}

	@Nonnull
	public static List<Gesuchsperiode> findGesuchsperiodenContainedInZahlungsauftrag(
		@Nonnull Collection<Gesuchsperiode> allGesuchsperioden,
		@Nonnull Zahlungsauftrag zahlungsauftrag
	) {
		List<Gesuchsperiode> containedGesuchsperioden = new ArrayList<>();
		for (Gesuchsperiode currentGP : allGesuchsperioden) {
			boolean contains = isGesuchsperiodeContainedInZahlungsauftrag(currentGP, zahlungsauftrag);
			if (contains) {
				containedGesuchsperioden.add(currentGP);
			}
		}
		return containedGesuchsperioden;
	}

	private static boolean isGesuchsperiodeContainedInZahlungsauftrag(
		@Nonnull Gesuchsperiode currentGP,
		@Nonnull Zahlungsauftrag zahlungsauftrag
	) {
		final List<Zahlung> zahlungen = zahlungsauftrag.getZahlungen();
		for (Zahlung zahlung : zahlungen) {
			final List<Zahlungsposition> zahlungspositionen = zahlung.getZahlungspositionen();
			for (Zahlungsposition zahlungsposition : zahlungspositionen) {
				final Gesuchsperiode gesuchsperiode = zahlungsposition.extractGesuchsperiode();
				if (currentGP.equals(gesuchsperiode)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isZahlunglaufRepetition(
		@Nonnull LocalDate gueltigBisCurrentZahlungslauf,
		@Nullable Zahlungsauftrag lastZahlungslauf
	) {
		if (lastZahlungslauf == null) {
			return false;
		}
		final boolean repetition = !gueltigBisCurrentZahlungslauf.isAfter(lastZahlungslauf.getGueltigkeit().getGueltigBis());
		return repetition;
	}

	@Nonnull
	public static LocalDate ermittleZahlungslaufGueltigBis(
		@Nonnull Zahlungsauftrag currentZahlungsauftrag,
		int anzahlMonateInZukunft
	) {
		Objects.requireNonNull(currentZahlungsauftrag.getDatumGeneriert(), "Datum generiert muss jetzt gesetzt sein");
		return currentZahlungsauftrag.getDatumGeneriert().toLocalDate()
			.plusMonths(anzahlMonateInZukunft)
			.with(TemporalAdjusters.lastDayOfMonth());
	}

	@Nonnull
	public static LocalDate ermittleZahlungslaufGueltigVon(
		@Nonnull LocalDate gueltigBisCurrentZahlungslauf,
		@Nullable Zahlungsauftrag lastZahlungslauf
	) {
		if (lastZahlungslauf == null) {
			return Constants.START_OF_DATETIME.toLocalDate();
		}
		boolean isRepetition = ZahlungslaufUtil.isZahlunglaufRepetition(gueltigBisCurrentZahlungslauf, lastZahlungslauf);
		final LocalDate gueltigBisLastZahlungslauf = lastZahlungslauf.getGueltigkeit().getGueltigBis();
		if (isRepetition) {
			// Repetition, dh.der Monat ist schon ausgeloest. Wir nehmen den Monat der Gültigkeit des letzten Zahlungslaufs
			return gueltigBisLastZahlungslauf
				.with(TemporalAdjusters.firstDayOfMonth());
		}
		// Wir beginnen am Anfang des Folgemonats des letzten Auftrags
		return gueltigBisLastZahlungslauf
				.plusMonths(1)
				.with(TemporalAdjusters.firstDayOfMonth());
	}
}
