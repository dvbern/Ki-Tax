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

package ch.dvbern.ebegu.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

/**
 * DTO fuer Pendenzen
 */
@XmlRootElement(name = "pendenz")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxAntragDTO extends JaxAbstractAntragDTO {

	private static final long serialVersionUID = -1277026654764135397L;

	//probably unused
	public JaxAntragDTO(String antragId, LocalDate gesuchsperiodeGueltigAb, LocalDate gesuchsperiodeGueltigBis,
		@Nullable LocalDate eingangsdatum, @Nullable LocalDate eingangsdatumSTV, AntragTyp antragTyp,
		int laufnummer, Eingangsart eingangsart) {
		this();
		this.antragId = antragId;
		this.gesuchsperiodeGueltigAb = gesuchsperiodeGueltigAb;
		this.gesuchsperiodeGueltigBis = gesuchsperiodeGueltigBis;
		this.eingangsdatum = eingangsdatum;
		this.eingangsdatumSTV = eingangsdatumSTV;
		this.antragTyp = antragTyp;
		this.laufnummer = laufnummer;
		this.eingangsart = eingangsart;
	}

	//constructor fuer query
	public JaxAntragDTO(String antragId, LocalDate gesuchsperiodeGueltigAb, LocalDate gesuchsperiodeGueltigBis,
		@Nullable LocalDate eingangsdatum, @Nullable LocalDate eingangsdatumSTV, AntragTyp antragTyp,
		AntragStatus antragStatus, int laufnummer, Eingangsart eingangsart, @Nullable String besitzerUsername) {
		this();
		this.antragId = antragId;
		this.gesuchsperiodeGueltigAb = gesuchsperiodeGueltigAb;
		this.gesuchsperiodeGueltigBis = gesuchsperiodeGueltigBis;
		this.eingangsdatum = eingangsdatum;
		this.eingangsdatumSTV = eingangsdatumSTV;
		this.antragTyp = antragTyp;
		this.verfuegt = antragStatus.isAnyStatusOfVerfuegt();
		this.beschwerdeHaengig = antragStatus == AntragStatus.BESCHWERDE_HAENGIG;
		this.laufnummer = laufnummer;
		this.eingangsart = eingangsart;
		this.besitzerUsername = besitzerUsername;
	}

	public JaxAntragDTO() {
		super(JaxAntragDTO.class.getSimpleName());
	}

	@NotNull
	private String antragId = null;

	@NotNull
	private Eingangsart eingangsart;

	@Nullable
	private String besitzerUsername;

	@NotNull
	private AntragTyp antragTyp;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate gesuchsperiodeGueltigAb = null;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate gesuchsperiodeGueltigBis = null;

	@NotNull
	private String verantwortlicherBG; 	// Name Vorname

	@Nullable
	private String verantwortlicherTS; // Name Vorname

	@Nullable
	private String verantwortlicherUsernameBG; 	// Wird fuer Freigabe gebraucht

	@Nullable
	private String verantwortlicherUsernameTS; // Wird fuer Freigabe gebraucht

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate eingangsdatum = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate eingangsdatumSTV = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime aenderungsdatum = null;

	@NotNull
	private Set<BetreuungsangebotTyp> angebote;

	@NotNull
	private Set<String> kinder;

	@NotNull
	private Set<String> institutionen;

	@NotNull
	private AntragStatusDTO status;

	@NotNull
	private int laufnummer;

	private boolean verfuegt;

	private boolean beschwerdeHaengig;

	@NotNull
	private GesuchBetreuungenStatus gesuchBetreuungenStatus;

	private boolean dokumenteHochgeladen;

	@Nullable
	private FinSitStatus finSitStatus;


	public String getAntragId() {
		return antragId;
	}

	public void setAntragId(String antragId) {
		this.antragId = antragId;
	}

	public AntragTyp getAntragTyp() {
		return antragTyp;
	}

	public void setAntragTyp(AntragTyp antragTyp) {
		this.antragTyp = antragTyp;
	}

	public LocalDate getGesuchsperiodeGueltigAb() {
		return gesuchsperiodeGueltigAb;
	}

	public void setGesuchsperiodeGueltigAb(LocalDate gesuchsperiodeGueltigAb) {
		this.gesuchsperiodeGueltigAb = gesuchsperiodeGueltigAb;
	}

	public LocalDate getGesuchsperiodeGueltigBis() {
		return gesuchsperiodeGueltigBis;
	}

	public void setGesuchsperiodeGueltigBis(LocalDate gesuchsperiodeGueltigBis) {
		this.gesuchsperiodeGueltigBis = gesuchsperiodeGueltigBis;
	}

	public String getVerantwortlicherBG() {
		return verantwortlicherBG;
	}

	public void setVerantwortlicherBG(String verantwortlicherBG) {
		this.verantwortlicherBG = verantwortlicherBG;
	}

	@Nullable
	public String getVerantwortlicherTS() {
		return verantwortlicherTS;
	}

	public void setVerantwortlicherTS(@Nullable String verantwortlicherTS) {
		this.verantwortlicherTS = verantwortlicherTS;
	}

	@Nullable
	public String getVerantwortlicherUsernameBG() {
		return verantwortlicherUsernameBG;
	}

	public void setVerantwortlicherUsernameBG(@Nullable String verantwortlicherUsernameBG) {
		this.verantwortlicherUsernameBG = verantwortlicherUsernameBG;
	}

	@Nullable
	public String getVerantwortlicherUsernameTS() {
		return verantwortlicherUsernameTS;
	}

	public void setVerantwortlicherUsernameTS(@Nullable String verantwortlicherUsernameTS) {
		this.verantwortlicherUsernameTS = verantwortlicherUsernameTS;
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
	public LocalDateTime getAenderungsdatum() {
		return aenderungsdatum;
	}

	public void setAenderungsdatum(@Nullable LocalDateTime aenderungsdatum) {
		this.aenderungsdatum = aenderungsdatum;
	}

	public Set<BetreuungsangebotTyp> getAngebote() {
		return angebote;
	}

	public void setAngebote(Set<BetreuungsangebotTyp> angebote) {
		this.angebote = angebote;
	}

	public Set<String> getInstitutionen() {
		return institutionen;
	}

	public void setInstitutionen(Set<String> institutionen) {
		this.institutionen = institutionen;
	}

	public AntragStatusDTO getStatus() {
		return status;
	}

	public void setStatus(AntragStatusDTO status) {
		this.status = status;
	}

	public boolean isVerfuegt() {
		return verfuegt;
	}

	public void setVerfuegt(boolean verfuegt) {
		this.verfuegt = verfuegt;
	}

	public boolean isBeschwerdeHaengig() {
		return beschwerdeHaengig;
	}

	public void setBeschwerdeHaengig(boolean beschwerdeHaengig) {
		this.beschwerdeHaengig = beschwerdeHaengig;
	}

	public int getLaufnummer() {
		return laufnummer;
	}

	public void setLaufnummer(int laufnummer) {
		this.laufnummer = laufnummer;
	}

	public Eingangsart getEingangsart() {
		return eingangsart;
	}

	public void setEingangsart(Eingangsart eingangsart) {
		this.eingangsart = eingangsart;
	}

	@Nullable
	public String getBesitzerUsername() {
		return besitzerUsername;
	}

	public void setBesitzerUsername(@Nullable String besitzerUsername) {
		this.besitzerUsername = besitzerUsername;
	}

	public Set<String> getKinder() {
		return kinder;
	}

	public void setKinder(Set<String> kinder) {
		this.kinder = kinder;
	}

	public GesuchBetreuungenStatus getGesuchBetreuungenStatus() {
		return gesuchBetreuungenStatus;
	}

	public void setGesuchBetreuungenStatus(GesuchBetreuungenStatus gesuchBetreuungenStatus) {
		this.gesuchBetreuungenStatus = gesuchBetreuungenStatus;
	}

	public boolean isDokumenteHochgeladen() {
		return dokumenteHochgeladen;
	}

	public void setDokumenteHochgeladen(boolean dokumenteHochgeladen) {
		this.dokumenteHochgeladen = dokumenteHochgeladen;
	}

	@Nullable
	public FinSitStatus getFinSitStatus() {
		return finSitStatus;
	}

	public void setFinSitStatus(@Nullable FinSitStatus finSitStatus) {
		this.finSitStatus = finSitStatus;
	}
}
