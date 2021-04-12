/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validators.CheckGemeindeAtLeastOneAngebot;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.END_OF_TIME;

@Audited
@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "name", name = "UK_gemeinde_name"),
		@UniqueConstraint(columnNames = "bfsNummer", name = "UK_gemeinde_bfsnummer"),
		@UniqueConstraint(columnNames = {"gemeindeNummer", "mandant_id"}, name = "UK_gemeinde_gemeindeNummer_mandant")
	}
)
@CheckGemeindeAtLeastOneAngebot
public class Gemeinde extends AbstractEntity implements Comparable<Gemeinde>, Displayable, HasMandant {

	private static final long serialVersionUID = -6976259296646006855L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeinde_mandant_id"))
	private Mandant mandant;

	@NotNull
	@Column(nullable = false)
	@Field(bridge = @FieldBridge(impl = LongBridge.class))
	private long gemeindeNummer = 0;

	@NotNull
	@Column(nullable = false)
	@Field(bridge = @FieldBridge(impl = LongBridge.class))
	private Long bfsNummer;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	private String name;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GemeindeStatus status = GemeindeStatus.EINGELADEN;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private LocalDate betreuungsgutscheineStartdatum;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private LocalDate tagesschulanmeldungenStartdatum;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private LocalDate ferieninselanmeldungenStartdatum;

	@Column(nullable = false)
	private boolean angebotBG = false;

	@Column(nullable = false)
	private boolean angebotTS = false;

	@Column(nullable = false)
	private boolean angebotFI = false;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private LocalDate gueltigBis = END_OF_TIME;


	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
	}

	public long getGemeindeNummer() {
		return gemeindeNummer;
	}

	public void setGemeindeNummer(long gemeindeNummer) {
		this.gemeindeNummer = gemeindeNummer;
	}

	@Override
	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	public GemeindeStatus getStatus() {
		return status;
	}

	public void setStatus(GemeindeStatus status) {
		this.status = status;
	}

	@Nonnull
	public Long getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(@Nonnull Long bfsNummer) {
		this.bfsNummer = bfsNummer;
	}

	@Nonnull
	public LocalDate getBetreuungsgutscheineStartdatum() {
		return betreuungsgutscheineStartdatum;
	}

	public void setBetreuungsgutscheineStartdatum(@Nonnull LocalDate betreuungsgutscheineStartdatum) {
		this.betreuungsgutscheineStartdatum = betreuungsgutscheineStartdatum;
	}

	public boolean isAngebotBG() {
		return angebotBG;
	}

	public void setAngebotBG(boolean angebotBG) {
		this.angebotBG = angebotBG;
	}

	public boolean isAngebotTS() {
		return angebotTS;
	}

	public void setAngebotTS(boolean angebotTS) {
		this.angebotTS = angebotTS;
	}

	public boolean isAngebotFI() {
		return angebotFI;
	}

	public void setAngebotFI(boolean angebotFI) {
		this.angebotFI = angebotFI;
	}

	@Nonnull
	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(@Nonnull LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof Gemeinde)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		Gemeinde gemeinde = (Gemeinde) other;
		return Objects.equals(this.getName(), gemeinde.getName())
			&& Objects.equals(this.getGemeindeNummer(), gemeinde.getGemeindeNummer())
			&& Objects.equals(this.getMandant(), gemeinde.getMandant());
	}

	@Override
	public int compareTo(Gemeinde o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getName(), o.getName());
		builder.append(this.getGemeindeNummer(), o.getGemeindeNummer());
		builder.append(this.getMandant(), o.getMandant());
		builder.append(this.getStatus(), o.getStatus());
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	@Transient
	public String getPaddedGemeindeNummer() {
		return Strings.padStart(Long.toString(getGemeindeNummer()), Constants.GEMEINDENUMMER_LENGTH, '0');
	}

	@Nonnull
	public LocalDate getTagesschulanmeldungenStartdatum() {
		return tagesschulanmeldungenStartdatum;
	}

	public void setTagesschulanmeldungenStartdatum(@Nonnull LocalDate tagesschulanmeldungenStartdatum) {
		this.tagesschulanmeldungenStartdatum = tagesschulanmeldungenStartdatum;
	}

	@Nonnull
	public LocalDate getFerieninselanmeldungenStartdatum() {
		return ferieninselanmeldungenStartdatum;
	}

	public void setFerieninselanmeldungenStartdatum(@Nonnull LocalDate ferieninselanmeldungenStartdatum) {
		this.ferieninselanmeldungenStartdatum = ferieninselanmeldungenStartdatum;
	}

	public boolean isGesuchsperiodeRelevantForGemeinde(@Nonnull Gesuchsperiode gesuchsperiode) {
		// Pruefen, ob irgendein Angebot waehrend dieser Gesuchsperiode vorhanden war
		LocalDate endeGesuchperiode = gesuchsperiode.getGueltigkeit().getGueltigBis();
		LocalDate startGesuchperiode = gesuchsperiode.getGueltigkeit().getGueltigAb();
		if (getGueltigBis().isBefore(startGesuchperiode)) {
			return false;
		}
		if (angebotBG && betreuungsgutscheineStartdatum.isBefore(endeGesuchperiode)) {
			return true;
		}
		if (angebotTS && tagesschulanmeldungenStartdatum.isBefore(endeGesuchperiode)) {
			return true;
		}
		if (angebotFI && ferieninselanmeldungenStartdatum.isBefore(endeGesuchperiode)) {
			return true;
		}
		return false;
	}

	public boolean isTagesschuleActiveForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		return this.angebotTS
			&& this.tagesschulanmeldungenStartdatum.isBefore(gesuchsperiode.getGueltigkeit().getGueltigBis())
			&& gesuchsperiode.getGueltigkeit().getGueltigAb().isBefore(this.gueltigBis);
	}
}
