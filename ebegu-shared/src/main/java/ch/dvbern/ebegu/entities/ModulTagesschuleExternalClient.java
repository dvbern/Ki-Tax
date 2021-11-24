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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

@Audited
@Entity
public class ModulTagesschuleExternalClient extends AbstractEntity {

	private static final long serialVersionUID = 8289018484808022362L;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_modul_tagesschule_external_client_modul_tagesschule_group_id"), nullable = false)
	private ModulTagesschuleGroup modulTagesschuleGroup;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_modul_tagesschule_external_client_external_client_id"), nullable = false)
	private ExternalClient externalClient;


	@Nonnull
	private String identifier;

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final ModulTagesschuleExternalClient otherModulTagesschuleExternalClient = (ModulTagesschuleExternalClient) other;
		return otherModulTagesschuleExternalClient.getExternalClient().isSame(externalClient) &&
				otherModulTagesschuleExternalClient.getModulTagesschuleGroup().isSame(modulTagesschuleGroup) &&
				Objects.equals(otherModulTagesschuleExternalClient.getIdentifier(), identifier);
	}

	@Nonnull @NotNull
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(@Nonnull String identifier) {
		this.identifier = identifier;
	}

	@Nonnull
	public ModulTagesschuleGroup getModulTagesschuleGroup() {
		return modulTagesschuleGroup;
	}

	public void setModulTagesschuleGroup(@Nonnull ModulTagesschuleGroup modulTagesschuleGroup) {
		this.modulTagesschuleGroup = modulTagesschuleGroup;
	}

	@Nonnull
	public ExternalClient getExternalClient() {
		return externalClient;
	}

	public void setExternalClient(@Nonnull ExternalClient externalClient) {
		this.externalClient = externalClient;
	}

	public ModulTagesschuleExternalClient copy() {
		ModulTagesschuleExternalClient copy = new ModulTagesschuleExternalClient();
		copy.setModulTagesschuleGroup(this.modulTagesschuleGroup);
		copy.setIdentifier(this.identifier);
		copy.setExternalClient(this.externalClient);

		return copy;
	}
}
