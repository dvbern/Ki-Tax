/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class InternePendenz extends AbstractEntity {

	private static final long serialVersionUID = 5441969456654012887L;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_interne_pendenz_gesuch_id"), nullable = false)
	private Gesuch gesuch;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private LocalDate termin;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	private String text;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private Boolean erledigt;

	@Nonnull
	public Gesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(@Nonnull Gesuch gesuch) {
		this.gesuch = gesuch;
	}

	@Nonnull
	public LocalDate getTermin() {
		return termin;
	}

	public void setTermin(@Nonnull LocalDate termin) {
		this.termin = termin;
	}

	@Nonnull
	public String getText() {
		return text;
	}

	public void setText(@Nonnull String text) {
		this.text = text;
	}

	@Nonnull
	public Boolean getErledigt() {
		return erledigt;
	}

	public void setErledigt(@Nonnull Boolean erledigt) {
		this.erledigt = erledigt;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof InternePendenz)){
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		InternePendenz internePendenz = (InternePendenz) other;
		return Objects.equals(this.getGesuch(), internePendenz.getGesuch())
			&& Objects.equals(this.getTermin(), internePendenz.getTermin())
			&& Objects.equals(this.getText(), internePendenz.getText())
			&& Objects.equals(this.getErledigt(), internePendenz.getErledigt());
	}
}
