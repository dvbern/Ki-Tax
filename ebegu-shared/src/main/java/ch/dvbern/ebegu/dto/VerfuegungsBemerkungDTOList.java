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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleValidity;

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
			addBemerkung(additionalBemerkung);
		}
	}

	public void addBemerkung(@Nonnull VerfuegungsBemerkungDTO bemerkung) {
		bemerkungenList.add(bemerkung);
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
		for (VerfuegungsBemerkungDTO otherBemerkung : otherList.bemerkungenList) {
			this.addBemerkung(otherBemerkung);
		}
	}

	/**
	 * Erstellt eine Liste mit allen erforderlichen Bemerkungen. Bemerkungen, die sich gegenseitig ausschliessen (z.B.
	 * Anspruch FACHSTELLE ueberschreibt Anspruch ERWERBSPENSUM) werden entfernt.
	 */
	@Nonnull
	public List<VerfuegungsBemerkungDTO> getRequiredBemerkungen() {
		// Wir muessen bei gleichem MsgKey dejenigen aus ASIV loeschen
		Map<MsgKey, VerfuegungsBemerkungDTO> messagesMap = toUniqueMap();
		// Ab jetzt muessen wir die Herkunft (ASIV oder Gemeinde) nicht mehr beachten.

		// Einige Regeln "überschreiben" einander. Die Bemerkungen der überschriebenen Regeln müssen hier entfernt werden
		// Aktuell bekannt:
		// 1. Ausserordentlicher Anspruch
		// 2. Fachstelle
		// 3. Erwerbspensum
		if (messagesMap.containsKey(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG)) {
			messagesMap.remove(MsgKey.ERWERBSPENSUM_ANSPRUCH);
			messagesMap.remove(MsgKey.FACHSTELLE_MSG);
		}
		if (messagesMap.containsKey(MsgKey.FACHSTELLE_MSG)) {
			messagesMap.remove(MsgKey.ERWERBSPENSUM_ANSPRUCH);
		}
		// Es kann sein das, trotz eine eingewoehnung, die minimal Erwerbspenum nicht erreicht ist
		if(messagesMap.containsKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH)){
			messagesMap.remove(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG);
		}
		if(messagesMap.containsKey(MsgKey.KEINE_ERWEITERTE_BEDUERFNISSE_MSG)) {
			messagesMap.remove(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG);
		}

		return new ArrayList<>(messagesMap.values());
	}

	private Map<MsgKey, VerfuegungsBemerkungDTO> toUniqueMap() {
		// Zum jetzigen Zeitpunkt haben wir unter Umstaenden eine Bemerkung zweimal drin: Einmal fuer ASIV und einmal fuer die Gemeinde
		// z.B. "Da ihr Kind weitere Angebote ... bleibt ein Anspruch von 10%" aus ASIV
		// vs. "Da ihr Kind weitere Angebote ... bleibt ein Anspruch von 30%" von der Gemeinde, da dort z.B. Freiwilligenarbeit mitzaehlt
		// Wir muessen also bei gleichem MsgKey dejenigen aus ASIV loeschen
		Map<MsgKey, VerfuegungsBemerkungDTO> messagesMap = new HashMap<>();
		for (VerfuegungsBemerkungDTO verfuegungsBemerkungDTO : bemerkungenList) {
			VerfuegungsBemerkungDTO maybeExistingMsg = messagesMap.get(verfuegungsBemerkungDTO.getMsgKey());
			if (maybeExistingMsg != null) {
				if (maybeExistingMsg.getRuleValidity() == RuleValidity.ASIV) {
					messagesMap.remove(verfuegungsBemerkungDTO.getMsgKey());
					messagesMap.put(verfuegungsBemerkungDTO.getMsgKey(), verfuegungsBemerkungDTO);
				}
			} else {
				messagesMap.put(verfuegungsBemerkungDTO.getMsgKey(), verfuegungsBemerkungDTO);
			}
		}
		return messagesMap;
	}
}
