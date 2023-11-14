/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.dto;

import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DTO für eine Verfügungsbemerkung
 */
public class VerfuegungsBemerkungDTOList {


	/**
	 * Wir schreiben alle Bemerkungen in ein Set. Damit stellen wir sicher, dass alle Varianten von Bemerkungen drin
	 * bleiben, also z.B. Restanspruch nach ASIV und Restanspruch nach GEMEINDE, so dass wir am Schluss entscheiden
	 * koennen, welche Bemerkungen wir genau benoetigen.
	 */
	@Nonnull
	private final Set<VerfuegungsBemerkungDTO> bemerkungenList = new HashSet<>();

	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(@Nullable VerfuegungsBemerkungDTOList other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		return Objects.equals(this.bemerkungenList, other.bemerkungenList);
	}

	public boolean isEmpty() {
		return bemerkungenList.isEmpty();
	}

	/**
	 * Gibt die Anzahl *unterschiedlicher* Messages zurueck, in Sinne von: Gleicher Key wird fuer ASIV und GEMEINDE
	 * nur einmal gezaehlt
	 */
	public int uniqueSize() {
		return toUniqueMap().size();
	}

	public void clear() {
		this.bemerkungenList.clear();
	}

	public boolean containsMsgKey(@Nonnull MsgKey msgKey) {
		return this.bemerkungenList.stream().anyMatch(bemerkung -> bemerkung.getMsgKey() == msgKey);
	}

	public void addAllBemerkungen(@Nonnull VerfuegungsBemerkungDTOList additionalBemerkungen) {
		for (VerfuegungsBemerkungDTO additionalBemerkung : additionalBemerkungen.bemerkungenList) {
			bemerkungenList.add(new VerfuegungsBemerkungDTO(additionalBemerkung));
		}
	}

	public void addBemerkung(@Nonnull RuleValidity ruleValidity, @Nonnull MsgKey msgKey, @Nonnull Locale locale, @Nullable Object... args) {
		bemerkungenList.add(new VerfuegungsBemerkungDTO(ruleValidity, msgKey, locale, args));
	}

	public void addBemerkung(@Nonnull VerfuegungsBemerkungDTO verfuegungsBemerkungDTO) {
		bemerkungenList.add(verfuegungsBemerkungDTO);
	}

	@Nonnull
	public Stream<VerfuegungsBemerkungDTO> getBemerkungenStream() {
		return this.bemerkungenList.stream();
	}


	@Nullable
	public VerfuegungsBemerkungDTO findFirstBemerkungByMsgKey(@Nonnull MsgKey msgKey) {
		return this.bemerkungenList
			.stream()
			.filter(bemerkung -> bemerkung.getMsgKey() == msgKey)
			.findFirst().orElse(null);
	}

	@Nonnull
	private List<VerfuegungsBemerkungDTO> findBemerkungenByMsgKey(@Nonnull MsgKey msgKey) {
		return this.bemerkungenList
			.stream()
			.filter(bemerkung -> bemerkung.getMsgKey() == msgKey)
			.collect(Collectors.toList());
	}

	public void removeBemerkungByMsgKey(@Nonnull MsgKey msgKey) {
		List<VerfuegungsBemerkungDTO> toRemoveList = findBemerkungenByMsgKey(msgKey);
		for (VerfuegungsBemerkungDTO verfuegungsBemerkungDTO : toRemoveList) {
			bemerkungenList.remove(verfuegungsBemerkungDTO);
		}
	}

	/**
	 * Fügt otherBemerkungen zur Liste hinzu
	 */
	public void mergeBemerkungenMap(@Nonnull VerfuegungsBemerkungDTOList otherList) {
		otherList.bemerkungenList
			.forEach(bemerkung -> this.bemerkungenList.add(new VerfuegungsBemerkungDTO(bemerkung)));
	}

	/**
	 * Erstellt eine Liste mit allen erforderlichen Bemerkungen. Bemerkungen, die sich gegenseitig ausschliessen (z.B.
	 * Anspruch FACHSTELLE ueberschreibt Anspruch ERWERBSPENSUM) werden entfernt.
	 */
	@Nonnull
	public List<VerfuegungsBemerkungDTO> getRequiredBemerkungen(boolean isTexteForFKJV) {
		// Wir muessen bei gleichem MsgKey dejenigen aus ASIV loeschen
		BemerkungenReplacer bemerkungenRemover = new BemerkungenReplacer(toUniqueMap());
		// Ab jetzt muessen wir die Herkunft (ASIV oder Gemeinde) nicht mehr beachten.
		return bemerkungenRemover.getRequiredBemerkungen(isTexteForFKJV);
	}

	private Map<MsgKey, List<VerfuegungsBemerkungDTO>> toUniqueMap() {
		// Zum jetzigen Zeitpunkt haben wir unter Umstaenden eine Bemerkung zweimal drin: Einmal fuer ASIV und einmal fuer die Gemeinde
		// z.B. "Da ihr Kind weitere Angebote ... bleibt ein Anspruch von 10%" aus ASIV
		// vs. "Da ihr Kind weitere Angebote ... bleibt ein Anspruch von 30%" von der Gemeinde, da dort z.B. Freiwilligenarbeit mitzaehlt
		// Wir muessen also bei gleichem MsgKey dejenigen aus ASIV loeschen
		// Bemerkungen mit demselben Key können unterschiedliche Gueltigkeiten haben, z.B. "Da ihr Kind....." 01.08-14.08 und "Da Ihr Kind...." 15.08.-31.08
		// Beim Prüfen, ob eine Rule zweimal drin ist (für ASIV und Gemeinde) muss zusätzlich zum Message-Key auch die Gültigkeit gleich sein.
		//Mutlimap<K1: MsgKey, K2: DateRange, V:VerfuegungsBemerkungDTO>
		MultiKeyMap messagesMap = new MultiKeyMap();
		for (VerfuegungsBemerkungDTO verfuegungsBemerkungDTO : bemerkungenList) {
			VerfuegungsBemerkungDTO maybeExistingMsg =
				(VerfuegungsBemerkungDTO) messagesMap.get(verfuegungsBemerkungDTO.getMsgKey(), verfuegungsBemerkungDTO.getGueltigkeit());

			if (maybeExistingMsg != null) {
				if (maybeExistingMsg.getRuleValidity() == RuleValidity.ASIV) {
					messagesMap.remove(verfuegungsBemerkungDTO.getMsgKey(), verfuegungsBemerkungDTO.getGueltigkeit());
					messagesMap.put(verfuegungsBemerkungDTO.getMsgKey(), verfuegungsBemerkungDTO.getGueltigkeit(), verfuegungsBemerkungDTO);
				}
			} else {
				messagesMap.put(verfuegungsBemerkungDTO.getMsgKey(), verfuegungsBemerkungDTO.getGueltigkeit(), verfuegungsBemerkungDTO);
			}
		}

		List<VerfuegungsBemerkungDTO> verfuegungBemerkungList = new ArrayList<>();
		verfuegungBemerkungList.addAll(messagesMap.values());
		return verfuegungBemerkungList.stream().collect(Collectors.groupingBy(VerfuegungsBemerkungDTO::getMsgKey));
	}

	private static class BemerkungenReplacer {

		private final Map<MsgKey, List<VerfuegungsBemerkungDTO>> messagesMap;

		private BemerkungenReplacer(Map<MsgKey, List<VerfuegungsBemerkungDTO>> messagesMap) {
			this.messagesMap = messagesMap;
		}

		protected List<VerfuegungsBemerkungDTO> getRequiredBemerkungen(boolean isFKJVText) {
			this.removeNotRequiredBemerkungen();

			if (isFKJVText) {
				this.overwriteASIVBemerkungenWithFKJVBemerkungen();
			}

			return messagesMap.values().stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
		}

		private void removeNotRequiredBemerkungen() {
			// Einige Regeln "überschreiben" einander. Die Bemerkungen der überschriebenen Regeln müssen hier entfernt werden
			// Aktuell bekannt:
			// 1. Ausserordentlicher Anspruch
			// 2. Fachstelle
			// 3. Erwerbspensum
			// 4. Erweiterte Beduefrnisse
			// 5. Zusatzgutschein
			// 6. Beenden von Antrag bei Konkubinat
			if (messagesMap.containsKey(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG)) {
				removeBemerkungForPeriodes(MsgKey.ERWERBSPENSUM_ANSPRUCH, getGueltigkeitenByMessageKey(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG));
				removeBemerkungForPeriodes(MsgKey.FACHSTELLE_MSG, getGueltigkeitenByMessageKey(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG));
			}
			if (messagesMap.containsKey(MsgKey.FACHSTELLE_MSG)) {
				removeBemerkungForPeriodes(MsgKey.ERWERBSPENSUM_ANSPRUCH, getGueltigkeitenByMessageKey(MsgKey.FACHSTELLE_MSG));
			}
			if (messagesMap.containsKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG)) {
				removeBemerkungForPeriodes(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH, getGueltigkeitenByMessageKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));
			}
			if (messagesMap.containsKey(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG)) {
				removeBemerkungForPeriodes(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG, getGueltigkeitenByMessageKey(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG));
				// Die Key KEINE_ERWEITERTE_BEDUERFNISSE_MSG soll nicht angezeigt werden, er wird nur verwendet,
				// um den ERWEITERTE_BEDUERFNISSE_MSG zu überschreiben, desshalb löschen wir in auch
				removeBemerkungForPeriodes(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG, getGueltigkeitenByMessageKey(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG));
			}
			if (messagesMap.containsKey(MsgKey.ZUSATZGUTSCHEIN_NEIN_SOZIALHILFE)) {
				removeBemerkungForPeriodes(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG_NEIN, getGueltigkeitenByMessageKey(MsgKey.ZUSATZGUTSCHEIN_NEIN_SOZIALHILFE));
				removeBemerkungForPeriodes(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS_NEIN, getGueltigkeitenByMessageKey(MsgKey.ZUSATZGUTSCHEIN_NEIN_SOZIALHILFE));
			}
			if (messagesMap.containsKey(MsgKey.SOZIALHILFEEMPFAENGER_HABEN_KEINEN_ANSPRUCH)) {
				removeBemerkungForPeriodes(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG, getGueltigkeitenByMessageKey(MsgKey.SOZIALHILFEEMPFAENGER_HABEN_KEINEN_ANSPRUCH));
			}
			if (messagesMap.containsKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST)) {
				removeBemerkungForPeriodes(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG, getGueltigkeitenByMessageKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			}
			if (messagesMap.containsKey(MsgKey.FAMILIENSITUATION_X_JAHRE_KONKUBINAT_MSG)) {
				removeBemerkungForPeriodes(MsgKey.FAMILIENSITUATION_KONKUBINAT_MSG, getGueltigkeitenByMessageKey(MsgKey.FAMILIENSITUATION_X_JAHRE_KONKUBINAT_MSG));
			}
			Optional<MsgKey> msgKeySchulstufe = getOptionalMsgKeySchulstufe();
			msgKeySchulstufe.ifPresent(msgKey -> removeBemerkungForPeriodes(MsgKey.FACHSTELLE_MSG, getGueltigkeitenByMessageKey(msgKey)));
		}

		private Optional<MsgKey> getOptionalMsgKeySchulstufe() {
			if (messagesMap.containsKey(MsgKey.SCHULSTUFE_VORSCHULE_MSG)) {
				return Optional.of(MsgKey.SCHULSTUFE_VORSCHULE_MSG);
			}

			if (messagesMap.containsKey(MsgKey.SCHULSTUFE_KINDERGARTEN_1_MSG)) {
				return Optional.of(MsgKey.SCHULSTUFE_KINDERGARTEN_1_MSG);
			}

			if (messagesMap.containsKey(MsgKey.SCHULSTUFE_FREIWILLIGER_KINDERGARTEN_MSG)) {
				return Optional.of(MsgKey.SCHULSTUFE_FREIWILLIGER_KINDERGARTEN_MSG);
			}

			if (messagesMap.containsKey(MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG)) {
				return Optional.of(MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG);
			}

			return Optional.empty();
		}

		/**
		 * Entfernt den MessageKey {@param messageKeyToRemove} für sämtliche Zeiträume {@param periodes}
		 * Gültigkeit MessageKey {@param messageKeyToRemove} vor remove 01.08 - 31.08
		 * Zeiträume {@param periodes}: 01.08-10.08 und 20.08-25.08
		 * Gültigkeit MessageKey {@param messageKeyToRemove} nach remove: 11.08-19.08 und 26.08-31.08
		 */
		private void removeBemerkungForPeriodes(MsgKey messageKeyToRemove, List<DateRange> periodes) {
			List<VerfuegungsBemerkungDTO> messagesToRemove = messagesMap.get(messageKeyToRemove);

			if(CollectionUtils.isEmpty(messagesToRemove)) {
				return;
			}

			List<VerfuegungsBemerkungDTO> gueltigeBemerkungen = periodes.stream()
				.flatMap(gueltigkeit -> getGueltigeBemerkungen(messagesToRemove, gueltigkeit).stream())
				.collect(Collectors.toList());

			messagesMap.put(messageKeyToRemove, gueltigeBemerkungen);
		}



		private List<VerfuegungsBemerkungDTO> getGueltigeBemerkungen(List<VerfuegungsBemerkungDTO> messagesToRemove, DateRange dateRangeNotGueltig) {
			return messagesToRemove.stream()
				.flatMap(verfuegungsBemerkungDTO -> getGueltigeBemerkung(verfuegungsBemerkungDTO, dateRangeNotGueltig).stream())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		}


		private List<VerfuegungsBemerkungDTO> getGueltigeBemerkung(VerfuegungsBemerkungDTO verfuegungsBemerkungDTO, DateRange dateRangeNotGueltig) {
			if(verfuegungsBemerkungDTO.getGueltigkeit() == null) {
				return Collections.emptyList();
			}

			//Return: kein gültiges Resultat, wenn Gültigkeit der Bemerkung komplet innerhalb der notGueltigRange ist
			if(dateRangeNotGueltig.contains(verfuegungsBemerkungDTO.getGueltigkeit())) {
				return Collections.emptyList();
			}

			Optional<DateRange> overlap = verfuegungsBemerkungDTO.getGueltigkeit().getOverlap(dateRangeNotGueltig);

			if(overlap.isEmpty()) {
				return Collections.singletonList(verfuegungsBemerkungDTO);
			}

			return createGueltigeBemerkungen(verfuegungsBemerkungDTO, overlap.get());
		}

		private List<VerfuegungsBemerkungDTO> createGueltigeBemerkungen(VerfuegungsBemerkungDTO verfuegungsBemerkungDTO, DateRange overlap) {
			List<VerfuegungsBemerkungDTO> result = new ArrayList<>();
			result.add(createGueltigeBemerkungFirstRange(verfuegungsBemerkungDTO, overlap));
			result.add(createGueltigeBemerkungenLastRange(verfuegungsBemerkungDTO, overlap));
			return result.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		}

		@Nullable
		private VerfuegungsBemerkungDTO createGueltigeBemerkungenLastRange(
			VerfuegungsBemerkungDTO verfuegungsBemerkungDTO,
			DateRange overlap) {
			assert verfuegungsBemerkungDTO.getGueltigkeit() != null;

			if(verfuegungsBemerkungDTO.getGueltigkeit().endsBefore(overlap) ||
				verfuegungsBemerkungDTO.getGueltigkeit().endsSameDay(overlap)) {
				return null;
			}

			VerfuegungsBemerkungDTO result = new VerfuegungsBemerkungDTO(verfuegungsBemerkungDTO);
			result.setGueltigkeit(new DateRange(overlap.getGueltigBis().plusDays(1), verfuegungsBemerkungDTO.getGueltigkeit().getGueltigBis()));
			return result;
		}

		@Nullable
		private VerfuegungsBemerkungDTO createGueltigeBemerkungFirstRange(
			VerfuegungsBemerkungDTO verfuegungsBemerkungDTO,
			DateRange overlap) {

			assert verfuegungsBemerkungDTO.getGueltigkeit() != null;

			if(overlap.startsBefore(verfuegungsBemerkungDTO.getGueltigkeit()) ||
			overlap.startsSameDay(verfuegungsBemerkungDTO.getGueltigkeit())) {
				return null;
			}

			VerfuegungsBemerkungDTO result = new VerfuegungsBemerkungDTO(verfuegungsBemerkungDTO);
			result.setGueltigkeit(new DateRange(verfuegungsBemerkungDTO.getGueltigkeit().getGueltigAb(), overlap.getGueltigAb().minusDays(1)));
			return result;
		}

		private List<DateRange> getGueltigkeitenByMessageKey(MsgKey messageKey) {
			return messagesMap.get(messageKey).stream()
				.map(VerfuegungsBemerkungDTO::getGueltigkeit)
				.collect(Collectors.toList());
		}


		private void overwriteASIVBemerkungenWithFKJVBemerkungen() {
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH, MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG, MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG, MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG, MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG, MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG, MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.UNBEZAHLTER_URLAUB_MSG, MsgKey.UNBEZAHLTER_URLAUB_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.ERWERBSPENSUM_ANSPRUCH, MsgKey.ERWERBSPENSUM_ANSPRUCH_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG, MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.WOHNSITZ_MSG, MsgKey.WOHNSITZ_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.FACHSTELLE_MSG, MsgKey.FACHSTELLE_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINREICHUNGSFRIST_MSG, MsgKey.EINREICHUNGSFRIST_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.REDUCKTION_RUECKWIRKEND_MSG, MsgKey.REDUCKTION_RUECKWIRKEND_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.ANSPRUCHSAENDERUNG_MSG, MsgKey.ANSPRUCHSAENDERUNG_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG, MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG, MsgKey.ERWEITERTE_BEDUERFNISSE_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.VERFUEGUNG_MIT_ANSPRUCH, MsgKey.VERFUEGUNG_MIT_ANSPRUCH_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINKOMMEN_MAX_MSG, MsgKey.EINKOMMEN_MAX_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG, MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.ABWESENHEIT_MSG, MsgKey.ABWESENHEIT_MSG_FKJV);
			overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG, MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG_FKJV);
		}

		private void overwriteASIVBemerkungenWithFKJVBemerkungen(MsgKey bemerkungToReplace, MsgKey replaceWithBemerkung) {
			if (!messagesMap.containsKey(bemerkungToReplace)) {
				return;
			}

			messagesMap
				.get(bemerkungToReplace)
				.forEach(verfuegungsBemerkungDTO -> verfuegungsBemerkungDTO.setMsgKey(replaceWithBemerkung));
		}
	}
}
