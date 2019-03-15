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

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.envers.Audited;

@Audited
@Entity
public class TextRessourceContainer extends AbstractMutableEntity {

	public static final long serialVersionUID = -8987361980678317485L;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_text_ressource_container_text_ressource_deutsch"))
	private TextRessource deutsch;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_text_ressource_container_text_ressource_franzoesisch"))
	private TextRessource franzoesisch;




	@Nullable
	public TextRessource getDeutsch() {
		return deutsch;
	}

	public void setDeutsch(@Nullable TextRessource deutsch) {
		this.deutsch = deutsch;
	}

	@Nullable
	public TextRessource getFranzoesisch() {
		return franzoesisch;
	}

	public void setFranzoesisch(@Nullable TextRessource franzoesisch) {
		this.franzoesisch = franzoesisch;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		// TODO KIBN-387
		return false;
	}

	@Nullable
	public TextRessource findTextRessourceByLocale(Locale locale) {
		switch (locale.getLanguage()) {

		case "de":
			return deutsch;
		case "fr":
			return franzoesisch;
		default:
			return null;
		}
	}
}
