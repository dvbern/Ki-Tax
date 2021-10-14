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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Entity
public class BfsGemeinde  implements Serializable, HasMandant {

	private static final long serialVersionUID = -6976259296646006855L;

	@Id
	@Column(unique = true, nullable = false, updatable = false, length = Constants.UUID_LENGTH)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	private String id;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_bfs_gemeinde_mandant_id"))
	private Mandant mandant;

	@NotNull
	@Column(nullable = false)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String kanton;

	@NotNull
	@Column(nullable = false)
	private Long bfsNummer;

	@NotNull
	@Column(nullable = false)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String name;

	@NotNull
	@Column(nullable = false)
	private LocalDate gueltigAb;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_bfsgemeinde_verbund_id"), nullable = true)
	private BfsGemeinde verbund;


	public BfsGemeinde() {
		id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
	}

	public String getKanton() {
		return kanton;
	}

	public void setKanton(String kanton) {
		this.kanton = kanton;
	}

	public Long getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(Long bfsNummer) {
		this.bfsNummer = bfsNummer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getGueltigAb() {
		return gueltigAb;
	}

	public void setGueltigAb(LocalDate gueltigAb) {
		this.gueltigAb = gueltigAb;
	}

	@Nullable
	public BfsGemeinde getVerbund() {
		return verbund;
	}

	public void setVerbund(@Nullable BfsGemeinde verbund) {
		this.verbund = verbund;
	}


	// Alle Schulverbund Gemeinden haben einen BFS Nummer höher als 10'000 aber kleiner als 11'000. Die neue Range für die "Spezielles Gemeinde" wie École
	// cantonale de langue française sind ab 100'000 definiert.

	public static boolean isBfsNummerVerbund(@Nullable Long bfsNummer) {
		if (bfsNummer == null) {
			return false;
		}
		return !isBfsNummerGemeinde(bfsNummer) && !isBfsNummerSpezialgemeinde(bfsNummer);
	}

	public static boolean isBfsNummerGemeinde(@Nullable Long bfsNummer) {
		if (bfsNummer == null) {
			return false;
		}
		return bfsNummer < 10000;
	}

	public static boolean isBfsNummerSpezialgemeinde(@Nullable Long bfsNummer) {
		if (bfsNummer == null) {
			return false;
		}
		return bfsNummer >= 100000;
	}
}
