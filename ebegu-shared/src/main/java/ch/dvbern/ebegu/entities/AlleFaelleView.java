/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.annotations.Type;

@Entity
public class AlleFaelleView {

	@Id
	@Column(unique = true, nullable = false, updatable = false, length = 16)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type( type = "string-uuid-binary" )
	private String antragId;

	@NotNull
	@Column(nullable = false)
	private String dossierId;

	@NotNull
	@Column(nullable = false)
	private String fallNummer;

	@NotNull
	@Column(nullable = false)
	private String gemeinde;

	@NotNull
	@Column(nullable = false)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type( type = "string-uuid-binary" )
	private String gemeindeId;

	@NotNull
	@Column(nullable = false)
	private String familienName;

	@NotNull
	@Column(nullable = false)
	private String kinder;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragStatus status;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragTyp typ;

	@NotNull
	@Column(nullable = false)
	private String gesuchsperiodeString;

	@NotNull
	@Column(nullable = false)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type( type = "string-uuid-binary" )
	private String gesuchsperiodeId;

	@Nonnull
	@Column(nullable = false)
	private LocalDateTime aenderungsdatum;

	@NotNull
	@Column(nullable = false)
	private Boolean dokumenteHochgeladen = false;

	@NotNull
	@Column(nullable = false)
	private Boolean internePendenz = false;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Eingangsart eingangsart = Eingangsart.PAPIER;

	@ElementCollection(targetClass = BetreuungsangebotTyp.class, fetch = FetchType.EAGER)
	@CollectionTable(
		name = "alleFaelleViewBetreuungsangebotTypen",
		joinColumns = @JoinColumn(name = "alleFaelleViewBetreuungsangebotTypen")
	)
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	@Nonnull
	private Set<BetreuungsangebotTyp> betreuungsangebotTypen = EnumSet.noneOf(BetreuungsangebotTyp.class);

	@Nullable
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "antrag_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "institution_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_institution_alle_faelle_view_antrag_id"),
		inverseForeignKey = @ForeignKey(name = "FK_institution_alle_faelle_view_institution_id"),
		indexes = {
			@Index(name = "IX_institution_alle_faelle_view_antrag_id", columnList = "antrag_id"),
			@Index(name = "IX_institution_alle_faelle_view_institution_id", columnList = "institution_id"),
		}
	)
	private Set<Institution> institutionen = new TreeSet<>();

	@Nullable
	@Column(nullable = false)
	private String verantwortlicherBG;

	@Nullable
	@Column(nullable = true)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type( type = "string-uuid-binary" )
	private String verantwortlicherBGId;

	@Nullable
	@Column(nullable = true)
	private String verantwortlicherTS;

	@Nullable
	@Column(nullable = true)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type( type = "string-uuid-binary" )
	private String verantwortlicherTSId;


}
