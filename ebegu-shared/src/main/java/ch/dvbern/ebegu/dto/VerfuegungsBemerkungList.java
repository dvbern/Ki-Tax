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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.util.VerfuegungsBemerkungComparator;
import org.apache.commons.lang3.StringUtils;

/**
 * DTO für eine Verfügungsbemerkung
 */
public class VerfuegungsBemerkungList {


	@Nonnull
	private final List<VerfuegungsBemerkung> bemerkungenList = new ArrayList<>();

	@Nonnull
	public List<VerfuegungsBemerkung> getBemerkungenList() {
		return bemerkungenList;
	}

	public boolean isSame(@Nullable VerfuegungsBemerkungList other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		return Objects.equals(this.getBemerkungenList(), other.getBemerkungenList());
	}

	public boolean isEmpty() {
		return bemerkungenList.isEmpty();
	}

	public int size() {
		return bemerkungenList.size();
	}

	public void clear() {
		this.bemerkungenList.clear();
	}

	public boolean containsMsgKey(@Nonnull MsgKey msgKey) {
		return this.bemerkungenList.stream().anyMatch(bemerkung -> bemerkung.getMsgKey() == msgKey);
	}

	public void addAllBemerkungen(@Nonnull VerfuegungsBemerkungList additionalBemerkungen) {
		//  Auch hier muss sichergestellt werden, dass pro Key nur eine Bemerkung vorhanden ist. Wir loeschen die aeltere
		for (VerfuegungsBemerkung additionalBemerkung : additionalBemerkungen.bemerkungenList) {
			addBemerkung(additionalBemerkung);
		}
	}

	public void addBemerkung(@Nonnull VerfuegungsBemerkung bemerkung) {
		// Falls von einer frueheren Regel *dieselbe* Bemerkung schon vorhanden ist, diese loeschen (sie hat evtl. andere Argumente)
		removeBemerkungByMsgKey(bemerkung.getMsgKey());
		bemerkungenList.add(bemerkung);
	}

	public void addBemerkung(@Nonnull MsgKey msgKey, @Nonnull Locale locale) {
		VerfuegungsBemerkung bemerkung = new VerfuegungsBemerkung(msgKey, locale);
		this.addBemerkung(bemerkung);
	}

	public void addBemerkung(@Nonnull MsgKey msgKey, @Nonnull Locale locale, @Nonnull Object... args) {
		VerfuegungsBemerkung bemerkung = new VerfuegungsBemerkung(msgKey, locale, args);
		this.addBemerkung(bemerkung);
	}

	@Nullable
	public VerfuegungsBemerkung findFirstBemerkungByMsgKey(@Nonnull MsgKey msgKey) {
		return this.bemerkungenList.stream().filter(bemerkung -> bemerkung.getMsgKey() == msgKey).findFirst().orElse(null);
	}

	@Nonnull
	public List<VerfuegungsBemerkung> findBemerkungenByMsgKey(@Nonnull MsgKey msgKey) {
		return this.bemerkungenList.stream().filter(bemerkung -> bemerkung.getMsgKey() == msgKey).collect(Collectors.toList());
	}

	public void removeBemerkungByMsgKey(@Nonnull MsgKey msgKey) {
		List<VerfuegungsBemerkung> toRemoveList = findBemerkungenByMsgKey(msgKey);
		for (VerfuegungsBemerkung verfuegungsBemerkung : toRemoveList) {
			bemerkungenList.remove(verfuegungsBemerkung);
		}
	}

	/**
	 * Fügt otherBemerkungen zur Liste hinzu, falls sie noch nicht vorhanden sind
	 */
	public void mergeBemerkungenMap(@Nonnull VerfuegungsBemerkungList otherList) {
		for (VerfuegungsBemerkung otherBemerkung : otherList.getBemerkungenList()) {
			Optional<VerfuegungsBemerkung> bemerkungPresentOptional =
				this.getBemerkungenList().stream().filter(thisBemerkung -> thisBemerkung.getMsgKey() == otherBemerkung.getMsgKey()).findAny();
			if (!bemerkungPresentOptional.isPresent()) {
				this.addBemerkung(otherBemerkung);
			}
		}
	}

	/**
	 * Erstellt einen String mit allen Bemerkungen. Bemerkungen, die sich gegenseitig ausschliessen (z.B.
	 * Anspruch FACHSTELLE ueberschreibt Anspruch ERWERBSPENSUM) werden entfernt.
	 */
	@Nonnull
	public String bemerkungenToString() {
		StringBuilder sb = new StringBuilder();

		// Einige Regeln "überschreiben" einander. Die Bemerkungen der überschriebenen Regeln müssen hier entfernt werden
		// Aktuell bekannt:
		// 1. Ausserordentlicher Anspruch
		// 2. Fachstelle
		// 3. Erwerbspensum
		if (containsMsgKey(MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG)) {
			removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH);
			removeBemerkungByMsgKey(MsgKey.FACHSTELLE_MSG);
		}
		if (containsMsgKey(MsgKey.FACHSTELLE_MSG)) {
			removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH);
		}

		// Die Bemerkungen so sortieren, wie sie auf der Verfuegung stehen sollen
		bemerkungenList.sort(new VerfuegungsBemerkungComparator());

		for (VerfuegungsBemerkung verfuegungsBemerkung : bemerkungenList) {
			sb.append(verfuegungsBemerkung.getTranslated());
			sb.append('\n');
		}
		// Den letzten NewLine entfernen
		String bemerkungen = sb.toString();
		bemerkungen = StringUtils.removeEnd(bemerkungen, "\n");
		return bemerkungen;
	}
}
