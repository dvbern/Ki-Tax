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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleKey;

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

	public void addAllBemerkungen(@Nonnull VerfuegungsBemerkungList bemerkungenList) {
		this.bemerkungenList.addAll(bemerkungenList.getBemerkungenList());
	}

	public void addBemerkung(@Nonnull VerfuegungsBemerkung bemerkung) {
		bemerkungenList.add(bemerkung);
	}

	public void addBemerkung(@Nonnull RuleKey ruleKey, @Nonnull MsgKey msgKey, @Nonnull Locale locale) {
		VerfuegungsBemerkung bemerkung = new VerfuegungsBemerkung(ruleKey, msgKey, locale);
		bemerkungenList.add(bemerkung);
	}

	@Nullable
	public VerfuegungsBemerkung findBemerkungByMsgKey(@Nonnull MsgKey msgKey) {
		return this.bemerkungenList.stream().filter(bemerkung -> bemerkung.getMsgKey() == msgKey).findFirst().orElse(null);
	}

	public void removeBemerkungByMsgKey(@Nonnull MsgKey msgKey) {
		this.bemerkungenList.stream().filter(bemerkung -> bemerkung.getMsgKey() == msgKey).forEach(bemerkungenList::remove);
	}

	/**
	 * Fügt otherBemerkungen zur Liste hinzu, falls sie noch nicht vorhanden sind
	 */
	public void mergeBemerkungenMap(@Nonnull VerfuegungsBemerkungList otherList) {
		for (VerfuegungsBemerkung otherBemerkung : otherList.getBemerkungenList()) {
			Optional<VerfuegungsBemerkung> bemerkungPresentOptional =
				this.getBemerkungenList().stream().filter(thisBemerkung -> thisBemerkung.getMsgKey() == otherBemerkung.getMsgKey()).findAny();
			if (!bemerkungPresentOptional.isPresent()) {
				this.addBemerkung(otherBemerkung);;
			}
		}
	}
}
