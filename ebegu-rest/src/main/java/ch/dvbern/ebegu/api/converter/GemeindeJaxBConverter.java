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

package ch.dvbern.ebegu.api.converter;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;

import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.util.StreamsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.api.converter.JaxBConverter.DROPPED_DUPLICATE_CONTAINER;
import static java.util.Objects.requireNonNull;

@RequestScoped
public class GemeindeJaxBConverter extends AbstractConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(GemeindeJaxBConverter.class);

	@Nonnull
	public Gemeinde gemeindeToEntity(@Nonnull final JaxGemeinde jaxGemeinde, @Nonnull final Gemeinde gemeinde) {
		requireNonNull(gemeinde);
		requireNonNull(jaxGemeinde);
		requireNonNull(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		convertAbstractFieldsToEntity(jaxGemeinde, gemeinde);
		gemeinde.setName(jaxGemeinde.getName());
		gemeinde.setStatus(jaxGemeinde.getStatus());
		gemeinde.setGemeindeNummer(jaxGemeinde.getGemeindeNummer());
		gemeinde.setBfsNummer(jaxGemeinde.getBfsNummer());
		if (jaxGemeinde.getBetreuungsgutscheineStartdatum() != null) {
			gemeinde.setBetreuungsgutscheineStartdatum(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		}

		return gemeinde;
	}

	public JaxGemeinde gemeindeToJAX(@Nonnull final Gemeinde persistedGemeinde) {
		final JaxGemeinde jaxGemeinde = new JaxGemeinde();
		convertAbstractFieldsToJAX(persistedGemeinde, jaxGemeinde);
		jaxGemeinde.setName(persistedGemeinde.getName());
		jaxGemeinde.setStatus(persistedGemeinde.getStatus());
		jaxGemeinde.setGemeindeNummer(persistedGemeinde.getGemeindeNummer());
		jaxGemeinde.setBfsNummer(persistedGemeinde.getBfsNummer());
		jaxGemeinde.setBetreuungsgutscheineStartdatum(persistedGemeinde.getBetreuungsgutscheineStartdatum());

		return jaxGemeinde;
	}

	@Nonnull
	public Set<Gemeinde> gemeindeListToEntity(
		@Nonnull Set<JaxGemeinde> jaxGemeindeList,
		@Nonnull Set<Gemeinde> gemeindeList) {

		final Set<Gemeinde> transformedGemeindeList = new TreeSet<>();
		for (final JaxGemeinde jaxGemeinde : jaxGemeindeList) {
			final Gemeinde gemeindeToMergeWith = gemeindeList
				.stream()
				.filter(existingGemeinde -> existingGemeinde.getId().equalsIgnoreCase(jaxGemeinde.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new Gemeinde());
			final Gemeinde gemeindeToAdd = gemeindeToEntity(jaxGemeinde, gemeindeToMergeWith);
			final boolean added = transformedGemeindeList.add(gemeindeToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", gemeindeToAdd);
			}
		}

		return transformedGemeindeList;
	}
}
