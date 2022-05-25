package ch.dvbern.ebegu.services.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ZahlungslaufUtil {

	private ZahlungslaufUtil() {
	}

	@NonNull
	public static List<Gesuchsperiode> findGesuchsperiodenContainedInZahlungsauftrag(
		@NonNull Collection<Gesuchsperiode> allGesuchsperioden,
		@NonNull Zahlungsauftrag zahlungsauftrag
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
		@NonNull Gesuchsperiode currentGP,
		@NonNull Zahlungsauftrag zahlungsauftrag
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
}
