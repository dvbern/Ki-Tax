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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.GesuchsperiodeEmailCandiateStatus;
import org.hibernate.envers.Audited;

/**
 * Beim Öffnen einer neuen Gesuchsperiode wird ein Email an jeden Antragsteller der vorherigen Gesuchsperiode
 * gesendet. Damit diese Emails gestaffelt versendet werden können, werden mit dieser Entität die Dossiers
 * und Gesuchsperiode gespeichert, an welche die Emails versendet werden müssen. Diese Liste wird in einem Batch Job
 * abgearbeitet.
 */

@Audited
@Entity
public class GesuchsperiodeEmailCandidate extends AbstractEntity {

	private static final long serialVersionUID = -4483436780916156841L;

	@Nonnull
	@NotNull
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchsperiode_massenversand_dossier_id"), nullable = false)
	private Dossier dossier;

	@Nonnull
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchsperiode_massenversand_gesuchsperiode_id"), nullable = false)
	private Gesuchsperiode lastGesuchsperiode;

	@Nonnull
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchsperiode_massenversand_gesuchsperiode_id"), nullable = false)
	private Gesuchsperiode nextGesuchsperiode;

	@Nullable
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GesuchsperiodeEmailCandiateStatus status;

	public GesuchsperiodeEmailCandidate() {}

	public GesuchsperiodeEmailCandidate(
		@Nonnull Dossier dossier,
		@Nonnull Gesuchsperiode lastGesuchsperiode,
		@Nonnull Gesuchsperiode nextGesuchsperiode
	) {
		this.dossier = dossier;
		this.lastGesuchsperiode = lastGesuchsperiode;
		this.nextGesuchsperiode = nextGesuchsperiode;
		this.status = GesuchsperiodeEmailCandiateStatus.OFFEN;
	}

	@Nonnull
	public Dossier getDossier() {
		return dossier;
	}

	public void setDossier(@Nonnull Dossier dossier) {
		this.dossier = dossier;
	}

	@Nonnull
	public Gesuchsperiode getLastGesuchsperiode() {
		return lastGesuchsperiode;
	}

	public void setLastGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.lastGesuchsperiode = gesuchsperiode;
	}

	@Nonnull
	public Gesuchsperiode getNextGesuchsperiode() {
		return nextGesuchsperiode;
	}

	public void setNextGesuchsperiode(@Nonnull Gesuchsperiode nextGesuchsperiode) {
		this.nextGesuchsperiode = nextGesuchsperiode;
	}

	@Nullable
	public GesuchsperiodeEmailCandiateStatus getStatus() {
		return status;
	}

	public void setStatus(@Nullable GesuchsperiodeEmailCandiateStatus status) {
		this.status = status;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
	}
}
