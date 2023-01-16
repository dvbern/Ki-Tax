/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Audited
@Entity
public class NeueVeranlagungsMitteilung extends Mitteilung {

	private static final long serialVersionUID = -8523411439182708718L;

	@NotNull
	@Nonnull
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_mitteilung_steuerdaten_response_id"))
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private SteuerdatenResponse steuerdatenResponse;

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
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
		final NeueVeranlagungsMitteilung otherBetreuungsmitteilung = (NeueVeranlagungsMitteilung) other;
		return getSteuerdatenResponse().isSame(otherBetreuungsmitteilung.getSteuerdatenResponse());
	}

	@Nonnull
	public SteuerdatenResponse getSteuerdatenResponse() {
		return steuerdatenResponse;
	}

	public void setSteuerdatenResponse(@Nonnull SteuerdatenResponse steuerdatenResponse) {
		this.steuerdatenResponse = steuerdatenResponse;
	}
}
