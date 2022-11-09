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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

@Entity
@Table(indexes = {
	@Index(columnList = "gemeindeId", name = "IX_alle_faelle_view_gemeinde_id"),
	@Index(columnList = "gesuchsperiodeId", name = "IX_alle_faelle_view_gesuchsperiode_id"),
	@Index(columnList = "verantwortlicherBGId", name = "IX_alle_faelle_view_verantwortlicher_bg_id"),
	@Index(columnList = "verantwortlicherTSId", name = "IX_alle_faelle_view_verantwortlicher_ts_id"),
	@Index(columnList = "fallId", name = "IX_alle_faelle_view_fall_id"),
	@Index(columnList = "besitzerId", name = "IX_alle_faelle_view_besitzer_id"),
	@Index(columnList = "sozialdienstId", name = "IX_alle_faelle_view_sozialdienst_id")
}
)
public class AlleFaelleView {

	@Id
	@Column(unique = true, nullable = false, updatable = false, length = 16)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String antragId;

	@NotNull
	@Column()
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String mandantId;

	@NotNull
	@Column(nullable = false)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String dossierId;

	@NotNull
	@Column(nullable = false)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String fallId;

	@NotNull
	@Column(nullable = false)
	private String fallnummer;

	@Nullable
	@Column(nullable = true)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String besitzerId;

	@Nullable
	@Column(nullable = true)
	private String besitzerUsername;

	@NotNull
	@Column(nullable = false)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String gemeindeId;

	@NotNull
	@Column(nullable = false)
	private String gemeindeName;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragStatus antragStatus;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragTyp antragTyp;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Eingangsart eingangsart = Eingangsart.PAPIER;

	@NotNull
	@Min(0)
	@Column(nullable = false)
	private int laufnummer = 0;

	@Nullable
	@Column(nullable = true)
	private String familienName;

	@Nullable
	@Column(nullable = true)
	private String kinder;

	@ElementCollection(targetClass = BetreuungsangebotTyp.class, fetch = FetchType.EAGER)
	@CollectionTable(
		name = "alleFaelleViewBetreuungsangebotTypen",
		joinColumns = @JoinColumn(name = "alleFaelleViewBetreuungsangebotTypen")
	)
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	@NotNull
	private Set<BetreuungsangebotTyp> angebotTypen = EnumSet.noneOf(BetreuungsangebotTyp.class);

	@NotNull
	@Column(nullable = false)
	private LocalDateTime aenderungsdatum;

	@Nullable
	@Column(nullable = true)
	private LocalDate eingangsdatum;

	@Nullable
	@Column(nullable = true)
	private LocalDate eingangsdatumSTV;

	@NotNull
	@Column(nullable = false)
	private Boolean sozialdienst = false;

	@Nullable
	@Column()
	private String sozialdienstId;

	@NotNull
	@Column(nullable = false)
	private Boolean internePendenz = false;

	@NotNull
	@Column(nullable = false)
	private Boolean dokumenteHochgeladen = false;

	@NotNull
	@Column(nullable = false)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String gesuchsperiodeId;

	@NotNull
	@Column(nullable = false)
	private String gesuchsperiodeString;

	@Nullable
	@Column(nullable = true)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String verantwortlicherBGId;

	@Nullable
	@Column(nullable = false)
	private String verantwortlicherBG;

	@Nullable
	@Column(nullable = true)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type(type = "string-uuid-binary")
	private String verantwortlicherTSId;

	@Nullable
	@Column(nullable = true)
	private String verantwortlicherTS;

	@Nullable
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "antrag_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "institution_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_alle_faelle_view_antrag_id"),
		inverseForeignKey = @ForeignKey(name = "FK_alle_faelle_view_institution_id"),
		indexes = {
			@Index(name = "IX_alle_faelle_view_antrag_id", columnList = "antrag_id"),
			@Index(name = "IX_alle_faelle_view_institution_id", columnList = "institution_id"),
		}
	)
	private List<Institution> institutionen = new ArrayList<>();

	public AlleFaelleView() {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AlleFaelleView that = (AlleFaelleView) o;
		return getLaufnummer() == that.getLaufnummer()
			&& getAntragId().equals(that.getAntragId())
			&& getDossierId().equals(that.getDossierId())
			&& getFallId().equals(that.getFallId())
			&& getFallnummer() == that.getFallnummer()
			&& Objects.equals(getBesitzerId(), that.getBesitzerId())
			&& Objects.equals(getBesitzerUsername(), that.getBesitzerUsername())
			&& getGemeindeId().equals(that.getGemeindeId())
			&& getGemeindeName().equals(that.getGemeindeName())
			&& getAntragStatus() == that.getAntragStatus()
			&& getAntragTyp() == that.getAntragTyp()
			&& getEingangsart() == that.getEingangsart()
			&& Objects.equals(getEingangsdatum(), that.getEingangsdatum())
			&& Objects.equals(getEingangsdatumSTV(), that.getEingangsdatumSTV())
			&& Objects.equals(getFamilienName(), that.getFamilienName())
			&& Objects.equals(getKinder(), that.getKinder())
			&& Objects.equals(getAngebotTypen(), that.getAngebotTypen())
			&& getAenderungsdatum().equals(that.getAenderungsdatum())
			&& getSozialdienst().equals(that.getSozialdienst())
			&& Objects.equals(getSozialdienstId(), that.getSozialdienstId())
			&& getInternePendenz().equals(that.getInternePendenz())
			&& getDokumenteHochgeladen().equals(that.getDokumenteHochgeladen())
			&& getGesuchsperiodeId().equals(that.getGesuchsperiodeId())
			&& getGesuchsperiodeString().equals(that.getGesuchsperiodeString())
			&& Objects.equals(getVerantwortlicherBGId(), that.getVerantwortlicherBGId())
			&& Objects.equals(getVerantwortlicherBG(), that.getVerantwortlicherBG())
			&& Objects.equals(getVerantwortlicherTSId(), that.getVerantwortlicherTSId())
			&& Objects.equals(getVerantwortlicherTS(), that.getVerantwortlicherTS())
			&& Objects.equals(getInstitutionen(), that.getInstitutionen());
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			getAntragId(),
			getDossierId(),
			getFallId(),
			getFallnummer(),
			getBesitzerId(),
			getBesitzerUsername(),
			getGemeindeId(),
			getGemeindeName(),
			getAntragStatus(),
			getAntragTyp(),
			getEingangsart(),
			getLaufnummer(),
			getFamilienName(),
			getKinder(),
			getAngebotTypen(),
			getAenderungsdatum(),
			getSozialdienst(),
			getInternePendenz(),
			getDokumenteHochgeladen(),
			getGesuchsperiodeId(),
			getGesuchsperiodeString(),
			getVerantwortlicherBGId(),
			getVerantwortlicherBG(),
			getVerantwortlicherTSId(),
			getVerantwortlicherTS(),
			getInstitutionen());
	}

	public String getAntragId() {
		return antragId;
	}

	public void setAntragId(String antragId) {
		this.antragId = antragId;
	}

	public String getMandantId() {
		return mandantId;
	}

	public void setMandantId(String mandantId) {
		this.mandantId = mandantId;
	}

	public String getDossierId() {
		return dossierId;
	}

	public void setDossierId(String dossierId) {
		this.dossierId = dossierId;
	}

	public String getFallId() {
		return fallId;
	}

	public void setFallId(String fallId) {
		this.fallId = fallId;
	}

	public String getFallnummer() {
		return fallnummer;
	}

	public void setFallnummer(String fallnummer) {
		this.fallnummer = fallnummer;
	}

	@Nullable
	public String getBesitzerId() {
		return besitzerId;
	}

	public void setBesitzerId(@Nullable String besitzerId) {
		this.besitzerId = besitzerId;
	}

	@Nullable
	public String getBesitzerUsername() {
		return besitzerUsername;
	}

	public void setBesitzerUsername(@Nullable String besitzerUsername) {
		this.besitzerUsername = besitzerUsername;
	}

	public String getGemeindeId() {
		return gemeindeId;
	}

	public void setGemeindeId(String gemeindeId) {
		this.gemeindeId = gemeindeId;
	}

	public String getGemeindeName() {
		return gemeindeName;
	}

	public void setGemeindeName(String gemeindeName) {
		this.gemeindeName = gemeindeName;
	}

	public AntragStatus getAntragStatus() {
		return antragStatus;
	}

	public void setAntragStatus(AntragStatus antragStatus) {
		this.antragStatus = antragStatus;
	}

	public AntragTyp getAntragTyp() {
		return antragTyp;
	}

	public void setAntragTyp(AntragTyp antragTyp) {
		this.antragTyp = antragTyp;
	}

	public Eingangsart getEingangsart() {
		return eingangsart;
	}

	public void setEingangsart(Eingangsart eingangsart) {
		this.eingangsart = eingangsart;
	}

	public int getLaufnummer() {
		return laufnummer;
	}

	public void setLaufnummer(int laufnummer) {
		this.laufnummer = laufnummer;
	}

	@Nullable
	public String getFamilienName() {
		return familienName;
	}

	public void setFamilienName(@Nullable String familienName) {
		this.familienName = familienName;
	}

	@Nullable
	public String getKinder() {
		return kinder;
	}

	public void setKinder(@Nullable String kinder) {
		this.kinder = kinder;
	}

	@Nonnull
	public Set<BetreuungsangebotTyp> getAngebotTypen() {
		return angebotTypen;
	}

	public void setAngebotTypen(@Nonnull Set<BetreuungsangebotTyp> angebotTypen) {
		this.angebotTypen = angebotTypen;
	}

	@Nonnull
	public LocalDateTime getAenderungsdatum() {
		return aenderungsdatum;
	}

	public void setAenderungsdatum(@Nonnull LocalDateTime aenderungsdatum) {
		this.aenderungsdatum = aenderungsdatum;
	}

	public Boolean getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(Boolean sozialdienst) {
		this.sozialdienst = sozialdienst;
	}

	public Boolean getInternePendenz() {
		return internePendenz;
	}

	public void setInternePendenz(Boolean internePendenz) {
		this.internePendenz = internePendenz;
	}

	public Boolean getDokumenteHochgeladen() {
		return dokumenteHochgeladen;
	}

	public void setDokumenteHochgeladen(Boolean dokumenteHochgeladen) {
		this.dokumenteHochgeladen = dokumenteHochgeladen;
	}

	public String getGesuchsperiodeId() {
		return gesuchsperiodeId;
	}

	public void setGesuchsperiodeId(String gesuchsperiodeId) {
		this.gesuchsperiodeId = gesuchsperiodeId;
	}

	public String getGesuchsperiodeString() {
		return gesuchsperiodeString;
	}

	public void setGesuchsperiodeString(String gesuchsperiodeString) {
		this.gesuchsperiodeString = gesuchsperiodeString;
	}

	@Nullable
	public String getVerantwortlicherBGId() {
		return verantwortlicherBGId;
	}

	public void setVerantwortlicherBGId(@Nullable String verantwortlicherBGId) {
		this.verantwortlicherBGId = verantwortlicherBGId;
	}

	@Nullable
	public String getVerantwortlicherBG() {
		return verantwortlicherBG;
	}

	public void setVerantwortlicherBG(@Nullable String verantwortlicherBG) {
		this.verantwortlicherBG = verantwortlicherBG;
	}

	@Nullable
	public String getVerantwortlicherTSId() {
		return verantwortlicherTSId;
	}

	public void setVerantwortlicherTSId(@Nullable String verantwortlicherTSId) {
		this.verantwortlicherTSId = verantwortlicherTSId;
	}

	@Nullable
	public String getVerantwortlicherTS() {
		return verantwortlicherTS;
	}

	public void setVerantwortlicherTS(@Nullable String verantwortlicherTS) {
		this.verantwortlicherTS = verantwortlicherTS;
	}

	@Nullable
	public List<Institution> getInstitutionen() {
		return institutionen;
	}

	public void setInstitutionen(@Nullable List<Institution> institutionen) {
		this.institutionen = institutionen;
	}

	@Nullable
	public LocalDate getEingangsdatum() {
		return eingangsdatum;
	}

	public void setEingangsdatum(@Nullable LocalDate eingangsdatum) {
		this.eingangsdatum = eingangsdatum;
	}

	@Nullable
	public LocalDate getEingangsdatumSTV() {
		return eingangsdatumSTV;
	}

	public void setEingangsdatumSTV(@Nullable LocalDate eingangsdatumSTV) {
		this.eingangsdatumSTV = eingangsdatumSTV;
	}

	@Nullable
	public String getSozialdienstId() {
		return sozialdienstId;
	}

	public void setSozialdienstId(@Nullable String sozialdienstId) {
		this.sozialdienstId = sozialdienstId;
	}
}
