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

package ch.dvbern.ebegu.api.converter.gemeindeantraege;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractConverter;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.gemeindekennzahlen.JaxGemeindeKennzahlen;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;

@RequestScoped
public class JaxGemeindeKennzahlenConverter extends AbstractConverter {

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	GesuchsperiodeService gesuchsperiodeService;

	@Nonnull
	public GemeindeKennzahlen gemeindeKennzahlenToEntity(
			@Nonnull final JaxGemeindeKennzahlen jaxGemeindeKennzahlen,
			@Nonnull final GemeindeKennzahlen gemeindeKennzahlen) {
		Objects.requireNonNull(jaxGemeindeKennzahlen.getGemeinde().getId());
		Objects.requireNonNull(jaxGemeindeKennzahlen.getGesuchsperiode().getId());

		convertAbstractFieldsToEntity(jaxGemeindeKennzahlen, gemeindeKennzahlen);

		gemeindeService.findGemeinde(jaxGemeindeKennzahlen.getGemeinde().getId()).ifPresent(gemeindeKennzahlen::setGemeinde);
		gesuchsperiodeService.findGesuchsperiode(jaxGemeindeKennzahlen.getGesuchsperiode().getId()).ifPresent(gemeindeKennzahlen::setGesuchsperiode);

		//don't set status from client

		gemeindeKennzahlen.setNachfrageErfuellt(jaxGemeindeKennzahlen.getNachfrageErfuellt());
		gemeindeKennzahlen.setNachfrageAnzahl(jaxGemeindeKennzahlen.getNachfrageAnzahl());
		gemeindeKennzahlen.setNachfrageDauer(jaxGemeindeKennzahlen.getNachfrageDauer());
		gemeindeKennzahlen.setKostenlenkungAndere(jaxGemeindeKennzahlen.getKostenlenkungAndere());
		gemeindeKennzahlen.setWelcheKostenlenkungsmassnahmen(jaxGemeindeKennzahlen.getWelcheKostenlenkungsmassnahmen());

		return gemeindeKennzahlen;
	}

	@Nonnull
	public JaxGemeindeKennzahlen gemeindeKennzahlenToJax(
			@Nonnull final GemeindeKennzahlen gemeindeKennzahlen
	) {
		JaxGemeindeKennzahlen jaxGemeindeKennzahlen = new JaxGemeindeKennzahlen();

		convertAbstractFieldsToJAX(gemeindeKennzahlen, jaxGemeindeKennzahlen);

		jaxGemeindeKennzahlen.setGemeinde(gemeindeToJAX(gemeindeKennzahlen.getGemeinde()));
		jaxGemeindeKennzahlen.setGesuchsperiode(gesuchsperiodeToJAX(gemeindeKennzahlen.getGesuchsperiode()));
		jaxGemeindeKennzahlen.setStatus(gemeindeKennzahlen.getStatus());

		jaxGemeindeKennzahlen.setNachfrageErfuellt(gemeindeKennzahlen.getNachfrageErfuellt());
		jaxGemeindeKennzahlen.setNachfrageAnzahl(gemeindeKennzahlen.getNachfrageAnzahl());
		jaxGemeindeKennzahlen.setNachfrageDauer(gemeindeKennzahlen.getNachfrageDauer());
		jaxGemeindeKennzahlen.setKostenlenkungAndere(gemeindeKennzahlen.getKostenlenkungAndere());
		jaxGemeindeKennzahlen.setWelcheKostenlenkungsmassnahmen(gemeindeKennzahlen.getWelcheKostenlenkungsmassnahmen());

		return jaxGemeindeKennzahlen;
	}
}
