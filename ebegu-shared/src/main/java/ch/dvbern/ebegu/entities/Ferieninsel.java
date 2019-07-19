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

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;

import org.hibernate.envers.Audited;

/**
 * Entität für Ferieninseln
 */
@Entity
@Audited
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverrides({
	@AssociationOverride(name = "traegerschaft", joinColumns = @JoinColumn(name = "traegerschaft_id"), foreignKey = @ForeignKey(name = "FK_ferieninsel_traegerschaft_id")),
	@AssociationOverride(name = "mandant", joinColumns = @JoinColumn(name = "mandant_id"), foreignKey = @ForeignKey(name = "FK_ferieninsel_mandant_id"))
})
public class Ferieninsel extends AbstractInstitution {

	private static final long serialVersionUID = -9037857320548372570L;

	public Ferieninsel() {
	}
}
