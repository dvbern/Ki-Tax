package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

import java.util.Comparator;

/**
 * Comparator, der die VerfuegungsZeitabschnitte nach Datum-Von sortiert.
 */
public class VerfuegungZeitabschnittComparator implements Comparator<VerfuegungZeitabschnitt> {

	@Override
	public int compare(VerfuegungZeitabschnitt o1, VerfuegungZeitabschnitt o2) {
		int result = o1.getGueltigkeit().getGueltigAb().compareTo(o2.getGueltigkeit().getGueltigAb());
		if (result == 0) {
			result = o1.getGueltigkeit().getGueltigBis().compareTo(o2.getGueltigkeit().getGueltigBis());
		}
		return result;
	}
}
