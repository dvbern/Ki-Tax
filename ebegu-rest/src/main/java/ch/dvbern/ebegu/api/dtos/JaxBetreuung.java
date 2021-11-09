/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.AnmeldungMutationZustand;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

/**
 * DTO fuer Daten der Betreuungen,
 */
@XmlRootElement(name = "betreuung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuung extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297022381674937397L;

	@NotNull
	private JaxInstitutionStammdatenSummary institutionStammdaten;

	@NotNull
	private Betreuungsstatus betreuungsstatus;

	@Nullable
	private JaxBelegungTagesschule belegungTagesschule;

	@Nullable
	private JaxBelegungFerieninsel belegungFerieninsel;

	@NotNull
	private List<JaxBetreuungspensumContainer> betreuungspensumContainers = new ArrayList<>();

	@NotNull
	private List<JaxAbwesenheitContainer> abwesenheitContainers = new ArrayList<>();

	@NotNull
	private JaxErweiterteBetreuungContainer erweiterteBetreuungContainer;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private String grundAblehnung;

	@Min(1)
	private Integer betreuungNummer = 1;

	@Nullable
	private JaxVerfuegung verfuegung;

	@NotNull
	private Boolean vertrag;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumAblehnung = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumBestaetigung = null;

	@Nullable
	private String kindFullname;

	@Nullable
	private Integer kindNummer;

	@Nullable
	private String kindId;

	@Nullable
	private String gesuchId;

	@Nullable
	private JaxGesuchsperiode gesuchsperiode;

	@Nullable
	private Boolean betreuungMutiert;

	@Nullable
	private Boolean abwesenheitMutiert;

	@Column(nullable = false)
	private boolean gueltig = false;

	@Nullable
	private AnmeldungMutationZustand anmeldungMutationZustand;

	@NotNull
	private List<JaxBetreuungspensumAbweichung> betreuungspensumAbweichungen = new ArrayList<>();

	private boolean keineDetailinformationen = false;

	@NotNull
	private List<JaxAnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts = new ArrayList<>();

	private boolean eingewoehnung = false;

	// transient (Not stored on server, just an information for client)
	private String bgNummer;

	public JaxInstitutionStammdatenSummary getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(JaxInstitutionStammdatenSummary institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	public Betreuungsstatus getBetreuungsstatus() {
		return betreuungsstatus;
	}

	public void setBetreuungsstatus(Betreuungsstatus betreuungsstatus) {
		this.betreuungsstatus = betreuungsstatus;
	}

	public List<JaxBetreuungspensumContainer> getBetreuungspensumContainers() {
		return betreuungspensumContainers;
	}

	public void setBetreuungspensumContainers(List<JaxBetreuungspensumContainer> betreuungspensumContainers) {
		this.betreuungspensumContainers = betreuungspensumContainers;
	}

	public List<JaxAbwesenheitContainer> getAbwesenheitContainers() {
		return abwesenheitContainers;
	}

	public void setAbwesenheitContainers(List<JaxAbwesenheitContainer> abwesenheiten) {
		this.abwesenheitContainers = abwesenheiten;
	}

	@Nonnull
	public JaxErweiterteBetreuungContainer getErweiterteBetreuungContainer() {
		return erweiterteBetreuungContainer;
	}

	public void setErweiterteBetreuungContainer(
		@Nonnull JaxErweiterteBetreuungContainer erweiterteBetreuungContainer) {
		this.erweiterteBetreuungContainer = erweiterteBetreuungContainer;
	}

	@Nullable
	public String getGrundAblehnung() {
		return grundAblehnung;
	}

	public void setGrundAblehnung(@Nullable String grundAblehnung) {
		this.grundAblehnung = grundAblehnung;
	}

	public Integer getBetreuungNummer() {
		return betreuungNummer;
	}

	public void setBetreuungNummer(Integer betreuungNummer) {
		this.betreuungNummer = betreuungNummer;
	}

	@Nullable
	public JaxVerfuegung getVerfuegung() {
		return verfuegung;
	}

	public void setVerfuegung(@Nullable JaxVerfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	public Boolean getVertrag() {
		return vertrag;
	}

	public void setVertrag(Boolean vertrag) {
		this.vertrag = vertrag;
	}

	@Nullable
	public LocalDate getDatumAblehnung() {
		return datumAblehnung;
	}

	public void setDatumAblehnung(@Nullable LocalDate datumAblehnung) {
		this.datumAblehnung = datumAblehnung;
	}

	@Nullable
	public LocalDate getDatumBestaetigung() {
		return datumBestaetigung;
	}

	public void setDatumBestaetigung(@Nullable LocalDate datumBestaetigung) {
		this.datumBestaetigung = datumBestaetigung;
	}

	@Nullable
	public String getKindFullname() {
		return kindFullname;
	}

	public void setKindFullname(String kindFullname) {
		this.kindFullname = kindFullname;
	}

	@Nullable
	public Integer getKindNummer() {
		return kindNummer;
	}

	public void setKindNummer(@Nullable Integer kindNummer) {
		this.kindNummer = kindNummer;
	}

	@Nullable
	public String getKindId() {
		return kindId;
	}

	public void setKindId(@Nullable String kindId) {
		this.kindId = kindId;
	}

	@Nullable
	public String getGesuchId() {
		return gesuchId;
	}

	public void setGesuchId(@Nullable String gesuchId) {
		this.gesuchId = gesuchId;
	}

	@Nullable
	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nullable JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public Boolean getBetreuungMutiert() {
		return betreuungMutiert;
	}

	public void setBetreuungMutiert(@Nullable Boolean betreuungMutiert) {
		this.betreuungMutiert = betreuungMutiert;
	}

	@Nullable
	public Boolean getAbwesenheitMutiert() {
		return abwesenheitMutiert;
	}

	public void setAbwesenheitMutiert(@Nullable Boolean abwesenheitMutiert) {
		this.abwesenheitMutiert = abwesenheitMutiert;
	}

	public boolean isGueltig() {
		return gueltig;
	}

	public void setGueltig(boolean gueltig) {
		this.gueltig = gueltig;
	}

	@Nullable
	public JaxBelegungTagesschule getBelegungTagesschule() {
		return belegungTagesschule;
	}

	public void setBelegungTagesschule(@Nullable JaxBelegungTagesschule belegungTagesschule) {
		this.belegungTagesschule = belegungTagesschule;
	}

	@Nullable
	public JaxBelegungFerieninsel getBelegungFerieninsel() {
		return belegungFerieninsel;
	}

	public void setBelegungFerieninsel(@Nullable JaxBelegungFerieninsel belegungFerieninsel) {
		this.belegungFerieninsel = belegungFerieninsel;
	}

	@Nullable
	public AnmeldungMutationZustand getAnmeldungMutationZustand() {
		return anmeldungMutationZustand;
	}

	public void setAnmeldungMutationZustand(@Nullable AnmeldungMutationZustand anmeldungMutationZustand) {
		this.anmeldungMutationZustand = anmeldungMutationZustand;
	}

	public String getBgNummer() {
		return bgNummer;
	}

	public void setBgNummer(String bgNummer) {
		this.bgNummer = bgNummer;
	}

	public boolean isKeineDetailinformationen() {
		return keineDetailinformationen;
	}

	public void setKeineDetailinformationen(boolean keineDetailinformationen) {
		this.keineDetailinformationen = keineDetailinformationen;
	}

	@Nonnull
	public List<JaxBetreuungspensumAbweichung> getBetreuungspensumAbweichungen() {
		return betreuungspensumAbweichungen;
	}

	public void setBetreuungspensumAbweichungen(@Nonnull List<JaxBetreuungspensumAbweichung> betreuungspensumAbweichungen) {
		this.betreuungspensumAbweichungen = betreuungspensumAbweichungen;
	}

	@Override
	public int compareTo(@Nonnull JaxAbstractDTO o) {
		if (o instanceof JaxBetreuung) {
			final JaxBetreuung other = (JaxBetreuung) o;
			return getBetreuungNummer().compareTo(other.getBetreuungNummer());
		}
		return super.compareTo(o);
	}

	public List<JaxAnmeldungTagesschuleZeitabschnitt> getAnmeldungTagesschuleZeitabschnitts() {
		return anmeldungTagesschuleZeitabschnitts;
	}

	public void setAnmeldungTagesschuleZeitabschnitts(List<JaxAnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts) {
		this.anmeldungTagesschuleZeitabschnitts = anmeldungTagesschuleZeitabschnitts;
	}

	public boolean isEingewoehnung() {
		return eingewoehnung;
	}

	public void setEingewoehnung(boolean eingewoehnung) {
		this.eingewoehnung = eingewoehnung;
	}
}
