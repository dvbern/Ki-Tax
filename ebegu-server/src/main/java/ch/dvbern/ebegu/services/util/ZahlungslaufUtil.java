package ch.dvbern.ebegu.services.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsposition;

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
}
