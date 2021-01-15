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

package ch.dvbern.ebegu.api.converter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienst;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienstStammdaten;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstStammdaten;
import ch.dvbern.ebegu.services.SozialdienstService;

import static java.util.Objects.requireNonNull;

public class JaxSozialdienstConverter extends AbstractConverter {

	@Inject
	private JaxBConverter converter;

	@Inject
	private SozialdienstService sozialdienstService;

	@Nonnull
	public Sozialdienst sozialdienstToEntity(@Nonnull final JaxSozialdienst jaxSozialdienst, @Nonnull final Sozialdienst sozialdienst) {
		convertAbstractFieldsToEntity(jaxSozialdienst, sozialdienst);
		sozialdienst.setName(jaxSozialdienst.getName());
		sozialdienst.setStatus(jaxSozialdienst.getStatus());
		return sozialdienst;
	}

	public JaxSozialdienst sozialdienstToJAX(@Nonnull final Sozialdienst persistedSozialdienst) {
		final JaxSozialdienst jaxSozialdienst = new JaxSozialdienst();
		convertAbstractFieldsToJAX(persistedSozialdienst, jaxSozialdienst);
		jaxSozialdienst.setName(persistedSozialdienst.getName());
		jaxSozialdienst.setStatus(persistedSozialdienst.getStatus());
		return jaxSozialdienst;
	}

	@Nonnull
	public SozialdienstStammdaten sozialdienstStammdatenToEntity(
		@Nonnull final JaxSozialdienstStammdaten jaxStammdaten,
		@Nonnull final SozialdienstStammdaten stammdaten
	) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getAdresse());
		requireNonNull(jaxStammdaten);
		requireNonNull(jaxStammdaten.getAdresse());
		requireNonNull(jaxStammdaten.getSozialdienst());
		requireNonNull(jaxStammdaten.getSozialdienst().getId());
		convertAbstractFieldsToEntity(jaxStammdaten, stammdaten);

		// Die Gemeinde selbst ändert nicht, nur wieder von der DB lesen
		sozialdienstService.findSozialdienst(jaxStammdaten.getSozialdienst().getId())
			.ifPresent(stammdaten::setSozialdienst);

		converter.adresseToEntity(jaxStammdaten.getAdresse(), stammdaten.getAdresse());

		stammdaten.setMail(jaxStammdaten.getMail());
		stammdaten.setTelefon(jaxStammdaten.getTelefon());
		stammdaten.setWebseite(jaxStammdaten.getWebseite());

		return stammdaten;
	}

	public JaxSozialdienstStammdaten sozialdienstStammdatenToJAX(@Nonnull final SozialdienstStammdaten stammdaten) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getSozialdienst());
		requireNonNull(stammdaten.getAdresse());
		final JaxSozialdienstStammdaten jaxStammdaten = new JaxSozialdienstStammdaten();
		convertAbstractFieldsToJAX(stammdaten, jaxStammdaten);
		jaxStammdaten.setSozialdienst(sozialdienstToJAX(stammdaten.getSozialdienst()));
		jaxStammdaten.setMail(stammdaten.getMail());
		jaxStammdaten.setTelefon(stammdaten.getTelefon());
		jaxStammdaten.setWebseite(stammdaten.getWebseite());
		jaxStammdaten.setAdresse(converter.adresseToJAX(stammdaten.getAdresse()));

		return jaxStammdaten;
	}
}
