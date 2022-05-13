/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;

import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class TextRessource extends AbstractMutableEntity {

	public static final long serialVersionUID = -3510401542520028556L;

	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String textDeutsch;

	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String textFranzoesisch;


	public String getTextDeutsch() {
		return textDeutsch;
	}

	public void setTextDeutsch(String textDeutsch) {
		this.textDeutsch = textDeutsch;
	}

	public String getTextFranzoesisch() {
		return textFranzoesisch;
	}

	public void setTextFranzoesisch(String textFranzoesisch) {
		this.textFranzoesisch = textFranzoesisch;
	}

	@Nullable
	public String findTextByLocale(Locale locale) {
		switch (locale.getLanguage()) {

		case "de":
			return textDeutsch;
		case "fr":
			return textFranzoesisch;
		default:
			return null;
		}
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final TextRessource otherTextRessource = (TextRessource) other;
		return Objects.equals(getTextDeutsch(), otherTextRessource.getTextDeutsch()) &&
			Objects.equals(getTextFranzoesisch(), otherTextRessource.getTextFranzoesisch());
	}

	public TextRessource copyTextRessource() {
		TextRessource copy = new TextRessource();
		copy.setTextDeutsch(this.getTextDeutsch());
		copy.setTextFranzoesisch(this.getTextFranzoesisch());
		return copy;
	}
}
