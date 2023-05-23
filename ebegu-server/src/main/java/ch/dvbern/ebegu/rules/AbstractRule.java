package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

public abstract class AbstractRule {

	/**
	 * Erstellt aus der übergebenen Liste von VerfuegungsZeitabschnitten eine neue Liste, die keine Überschneidungen mehr
	 * enthält. Überschneiden sich zwei Entitäten in der Ursprungsliste, so werden daraus drei Zeiträume erstellt:
	 * <pre>
	 * |------------------------|
	 * 40
	 * 	           |-------------------------------------|
	 * 			   60
	 * ergibt:
	 * |-----------|------------|------------------------|
	 * 40          100                60
	 * </pre>
	 */
	@Nonnull
	protected List<VerfuegungZeitabschnitt> mergeZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> entitiesToMerge) {
		List<VerfuegungZeitabschnitt> result = new ArrayList<>();
		Set<LocalDate> setOfPotentialZeitraumGrenzen = createSetOfPotentialZeitraumGrenzen(entitiesToMerge);
		if (setOfPotentialZeitraumGrenzen.isEmpty()) {
			return result;
		}
		Iterator<LocalDate> iterator = setOfPotentialZeitraumGrenzen.iterator();
		LocalDate datumVon = iterator.next();
		while (iterator.hasNext()) {
			LocalDate datumBis = iterator.next().minusDays(1);   //wir haben bei den Bis Daten jeweils einen Tag hinzugefuegt
			VerfuegungZeitabschnitt mergedZeitabschnitt = new VerfuegungZeitabschnitt(new DateRange(datumVon, datumBis));
			// Alle Zeitabschnitte suchen, die mit  diesem Range ueberlappen
			boolean foundOverlapping = false;
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : entitiesToMerge) {
				Optional<DateRange> optionalOverlap = verfuegungZeitabschnitt.getGueltigkeit().getOverlap(mergedZeitabschnitt.getGueltigkeit());
				if (optionalOverlap.isPresent()) {
					mergedZeitabschnitt.add(verfuegungZeitabschnitt); //Zeitabschnitt hinzumergen
					foundOverlapping = true;
				}
			}
			if (foundOverlapping) {
				result.add(mergedZeitabschnitt);
			}
			datumVon = datumBis.plusDays(1); //naechstes vondatum
		}
		return result;
	}

	/**
	 * Sammelt alle Start und Enddaten aus der Liste der Zeitabschnitte zusammen.
	 * Dabei ist es so, dass das bis Datum der Zeitabschnitte inklusiv - inklusiv ist. Wir moechten aber die Daten jeweils
	 * inklusv - exklusive enddatum. Daher wird zum endtaum jeweils ein Tag hinzugezaehlt
	 *
	 * @param entitiesUnmerged liste der Abschnitte
	 * @return Liste aller Dates die potentiell als Zeitraumgrenze dienen werden
	 */
	private Set<LocalDate> createSetOfPotentialZeitraumGrenzen(@Nonnull List<VerfuegungZeitabschnitt> entitiesUnmerged) {
		Set<LocalDate> setOfDates = new TreeSet<>();
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : entitiesUnmerged) {
			setOfDates.add(verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb());
			setOfDates.add(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().plusDays(1));
		}
		return setOfDates;
	}
}
