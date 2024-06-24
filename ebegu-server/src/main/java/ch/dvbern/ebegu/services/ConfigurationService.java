/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.pdfgenerator.verfuegung.VerfuegungPdfGeneratorKonfiguration;

@Stateless
public class ConfigurationService {

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	public VerfuegungPdfGeneratorKonfiguration getVerfuegungPdfGeneratorKonfiguration(
		@Nonnull Betreuung betreuung, boolean writeProtected) {
		// Falls die Gemeinde Kontingentierung eingeschaltet hat *und* es sich um einen Entwurf handelt
		// wird auf der Verfügung ein Vermerk zur Kontingentierung gedruckt
		boolean showInfoKontingentierung = false;
		if (!writeProtected) {
			Einstellung einstellungKontingentierung = einstellungService.findEinstellung(
				EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
				betreuung.extractGesuch().extractGemeinde(),
				betreuung.extractGesuchsperiode());
			showInfoKontingentierung = einstellungKontingentierung.getValueAsBoolean();
		}

		return VerfuegungPdfGeneratorKonfiguration.builder()
			.kontingentierungEnabledAndEntwurf(showInfoKontingentierung)
			.stadtBernAsivConfigured(applicationPropertyService.isStadtBernAsivConfigured(betreuung.extractGesuch().extractGemeinde().getMandant()))
			.FKJVTexte(getEinstellungFKJVTexte(betreuung))
			.betreuungspensumAnzeigeTyp(getEinstellungBetreuungspensumAnzeigeTyp(betreuung))
			.build();
	}


	public VerfuegungPdfGeneratorKonfiguration getVerfuegungPdfGeneratorKonfigurationNichtEintretten(
		@Nonnull Betreuung betreuung) {
		return VerfuegungPdfGeneratorKonfiguration.builder()
			.kontingentierungEnabledAndEntwurf(false)
			.stadtBernAsivConfigured(false)
			.FKJVTexte(getEinstellungFKJVTexte(betreuung))
			.betreuungspensumAnzeigeTyp(getEinstellungBetreuungspensumAnzeigeTyp(betreuung))
			.build();
	}

	private boolean getEinstellungFKJVTexte(@Nonnull Betreuung betreuung) {
		return einstellungService.findEinstellung(
			EinstellungKey.FKJV_TEXTE,
			betreuung.extractGesuch().extractGemeinde(),
			betreuung.extractGesuchsperiode()
		).getValueAsBoolean();
	}

	private BetreuungspensumAnzeigeTyp getEinstellungBetreuungspensumAnzeigeTyp(@Nonnull Betreuung betreuung) {
		return BetreuungspensumAnzeigeTyp.valueOf(einstellungService.findEinstellung(
			EinstellungKey.PENSUM_ANZEIGE_TYP,
			betreuung.extractGesuch().extractGemeinde(),
			betreuung.extractGesuchsperiode()
		).getValue());
	}
}
