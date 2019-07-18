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

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Superklasse fuer alle InstitutionsStammdaten (Stammdaten von Institutionen, Tagesschulen, Ferieninseln)
 */
@MappedSuperclass
@Audited
public abstract class AbstractInstitutionStammdaten extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -9037857320548372570L;

	@NotNull
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String mail;

	@NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_adresse_id"), nullable = false)
	private Adresse adresse;

	@Column(nullable = false)
	private boolean sendMailWennOffenePendenzen = true;


	protected AbstractInstitutionStammdaten() {
	}


	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}

	public boolean getSendMailWennOffenePendenzen() {
		return sendMailWennOffenePendenzen;
	}

	public void setSendMailWennOffenePendenzen(boolean sendMailWennOffenePendenzen) {
		this.sendMailWennOffenePendenzen = sendMailWennOffenePendenzen;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof AbstractInstitutionStammdaten)) {
			return false;
		}
		final AbstractInstitutionStammdaten otherInstStammdaten = (AbstractInstitutionStammdaten) other;
		return EbeguUtil.isSameObject(getAdresse(), otherInstStammdaten.getAdresse());
	}

	@Nonnull
	public abstract BetreuungsangebotTyp getBetreuungsangebotTyp();

}
