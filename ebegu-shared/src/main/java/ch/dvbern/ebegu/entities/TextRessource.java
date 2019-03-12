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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class TextRessource extends AbstractMutableEntity{

	public static final long serialVersionUID = -3510401542520028556L;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private Sprache sprache;

	@Column(nullable = false, length = Constants.DB_TEXTAREA_LENGTH)
	@NotNull
	private String text;

	public Sprache getSprache() {
		return sprache;
	}

	public void setSprache(Sprache sprache) {
		this.sprache = sprache;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		// TODO KIBON-387
		return false;
	}
}
