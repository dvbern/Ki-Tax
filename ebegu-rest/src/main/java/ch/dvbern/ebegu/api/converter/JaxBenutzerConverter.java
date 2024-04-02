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

package ch.dvbern.ebegu.api.converter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerNoDetails;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigung;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigungHistory;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.SozialdienstService;
import ch.dvbern.ebegu.services.TraegerschaftService;
import ch.dvbern.ebegu.util.StreamsUtil;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@RequestScoped
public class JaxBenutzerConverter extends AbstractConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(JaxBenutzerConverter.class);
	@Inject
	private JaxSozialdienstConverter jaxSozialdienstConverter;
	@Inject
	private MandantService mandantService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private TraegerschaftService traegerschaftService;
	@Inject
	private SozialdienstService sozialdienstService;

	@Nonnull
	public Benutzer jaxBenutzerToBenutzer(
		@Nonnull JaxBenutzer jaxBenutzer,
		@Nonnull Benutzer benutzer) {

		benutzer.setUsername(jaxBenutzer.getUsername());
		benutzer.setExternalUUID(
			Strings.isNullOrEmpty(jaxBenutzer.getExternalUUID()) ? null : jaxBenutzer.getExternalUUID()
		);
		benutzer.setEmail(jaxBenutzer.getEmail());
		benutzer.setNachname(jaxBenutzer.getNachname());
		benutzer.setVorname(jaxBenutzer.getVorname());
		benutzer.setStatus(jaxBenutzer.getStatus());
		if (jaxBenutzer.getMandant() != null && jaxBenutzer.getMandant().getId() != null) {
			// Mandant darf nicht vom Client ueberschrieben werden
			Mandant mandantFromDB = mandantService.findMandant(jaxBenutzer.getMandant().getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"jaxBenutzerToBenutzer -> mandant",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					benutzer.getMandant().getId()));
			benutzer.setMandant(mandantFromDB);
		} else {
			throw new EbeguEntityNotFoundException(
				"jaxBenutzerToBenutzer -> mandant",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}
		// Berechtigungen
		final Set<Berechtigung> convertedBerechtigungen = berechtigungenListToEntity(
			jaxBenutzer.getBerechtigungen(),
			benutzer.getBerechtigungen(),
			benutzer
		);
		//change the existing collection to reflect changes
		// Already tested: All existing module of the list remain as they were, that means their data are updated
		// and the objects are not created again. ID and InsertTimeStamp are the same as before
		benutzer.getBerechtigungen().clear();
		benutzer.getBerechtigungen().addAll(convertedBerechtigungen);
		return benutzer;
	}

	@Nonnull
	private Set<Berechtigung> berechtigungenListToEntity(
		@Nonnull Set<JaxBerechtigung> jaxBerechtigungenList,
		@Nonnull Set<Berechtigung> berechtigungenList,
		@Nonnull Benutzer benutzer) {

		final Set<Berechtigung> convertedBerechtigungen = new TreeSet<>();
		for (final JaxBerechtigung jaxBerechtigung : jaxBerechtigungenList) {
			final Berechtigung berechtigungToMergeWith = berechtigungenList
				.stream()
				.filter(existingBerechtigung -> existingBerechtigung.getId().equals(jaxBerechtigung.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(Berechtigung::new);
			final Berechtigung berechtigungToAdd = berechtigungToEntity(jaxBerechtigung, berechtigungToMergeWith);
			berechtigungToAdd.setBenutzer(benutzer);
			final boolean added = convertedBerechtigungen.add(berechtigungToAdd);
			if (!added) {
				LOGGER.warn("dropped duplicate berechtigung {}", berechtigungToAdd);
			}
		}
		return convertedBerechtigungen;
	}

	public JaxBenutzerNoDetails benutzerToJaxBenutzerNoDetails(@Nonnull Benutzer benutzer) {
		JaxBenutzerNoDetails jaxLoginElement = new JaxBenutzerNoDetails();
		jaxLoginElement.setVorname(benutzer.getVorname());
		jaxLoginElement.setNachname(benutzer.getNachname());
		jaxLoginElement.setUsername(benutzer.getUsername());
		Set<String> gemeindeIds = benutzer.getBerechtigungen()
			.stream()
			.flatMap(berechtigung -> berechtigung.getGemeindeList()
				.stream())
			.map(AbstractEntity::getId)
			.collect(Collectors.toSet());
		jaxLoginElement.setGemeindeIds(gemeindeIds);
		return jaxLoginElement;
	}

	public Berechtigung berechtigungToEntity(JaxBerechtigung jaxBerechtigung, Berechtigung berechtigung) {
		convertAbstractDateRangedFieldsToEntity(jaxBerechtigung, berechtigung);
		berechtigung.setRole(jaxBerechtigung.getRole());

		// wir muessen Traegerschaft und Institution auch updaten wenn sie null sind. Es koennte auch so aus dem IAM
		// kommen
		if (jaxBerechtigung.getInstitution() != null && jaxBerechtigung.getInstitution().getId() != null) {
			final Optional<Institution> institutionFromDB =
				institutionService.findInstitution(jaxBerechtigung.getInstitution().getId(), false);
			if (institutionFromDB.isPresent()) {
				// Institution darf nicht vom Client ueberschrieben werden
				berechtigung.setInstitution(institutionFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"berechtigungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxBerechtigung.getInstitution().getId());
			}
		} else {
			berechtigung.setInstitution(null);
		}

		if (jaxBerechtigung.getTraegerschaft() != null && jaxBerechtigung.getTraegerschaft().getId() != null) {
			final Optional<Traegerschaft> traegerschaftFromDB =
				traegerschaftService.findTraegerschaft(jaxBerechtigung.getTraegerschaft().getId());
			if (traegerschaftFromDB.isPresent()) {
				// Traegerschaft darf nicht vom Client ueberschrieben werden
				berechtigung.setTraegerschaft(traegerschaftFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"berechtigungToEntity -> traegerschaft",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxBerechtigung.getTraegerschaft().getId());
			}
		} else {
			berechtigung.setTraegerschaft(null);
		}

		if (jaxBerechtigung.getSozialdienst() != null && jaxBerechtigung.getSozialdienst().getId() != null) {
			final Optional<Sozialdienst> sozialdienstFromDB =
				sozialdienstService.findSozialdienst(jaxBerechtigung.getSozialdienst().getId());
			if (sozialdienstFromDB.isPresent()) {
				// Traegerschaft darf nicht vom Client ueberschrieben werden
				berechtigung.setSozialdienst(sozialdienstFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"berechtigungToEntity -> sozialdienst",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxBerechtigung.getSozialdienst().getId());
			}
		} else {
			berechtigung.setSozialdienst(null);
		}

		// Gemeinden: Duerfen nicht vom Frontend übernommen werden, sondern müssen aus der DB gelesen werden!
		loadGemeindenFromJax(jaxBerechtigung, berechtigung);
		return berechtigung;
	}


	public JaxBenutzer benutzerToJaxBenutzer(@Nonnull Benutzer benutzer) {
		JaxBenutzer jaxLoginElement = new JaxBenutzer();
		jaxLoginElement.setVorname(benutzer.getVorname());
		jaxLoginElement.setNachname(benutzer.getNachname());
		jaxLoginElement.setEmail(benutzer.getEmail());
		jaxLoginElement.setMandant(mandantToJAX(benutzer.getMandant()));
		jaxLoginElement.setUsername(benutzer.getUsername());
		jaxLoginElement.setExternalUUID(benutzer.getExternalUUID());
		jaxLoginElement.setStatus(benutzer.getStatus());
		jaxLoginElement.setCurrentBerechtigung(berechtigungToJax(benutzer.getCurrentBerechtigung()));
		// Berechtigungen
		Set<JaxBerechtigung> jaxBerechtigungen = new TreeSet<>();
		if (benutzer.getBerechtigungen() != null) {
			jaxBerechtigungen = benutzer.getBerechtigungen().stream()
				.map(this::berechtigungToJax)
				.sorted()
				.collect(Collectors.toCollection(TreeSet::new));
		}
		jaxLoginElement.setBerechtigungen(jaxBerechtigungen);

		return jaxLoginElement;
	}

	public JaxBerechtigung berechtigungToJax(Berechtigung berechtigung) {
		JaxBerechtigung jaxBerechtigung = new JaxBerechtigung();
		convertAbstractDateRangedFieldsToJAX(berechtigung, jaxBerechtigung);
		jaxBerechtigung.setRole(berechtigung.getRole());
		if (berechtigung.getInstitution() != null) {
			jaxBerechtigung.setInstitution(institutionToJAX(berechtigung.getInstitution()));
		}
		if (berechtigung.getTraegerschaft() != null) {
			jaxBerechtigung.setTraegerschaft(traegerschaftLightToJAX(berechtigung.getTraegerschaft()));
		}
		if (berechtigung.getSozialdienst() != null) {
			jaxBerechtigung.setSozialdienst(jaxSozialdienstConverter.sozialdienstToJAX(berechtigung.getSozialdienst()));
		}
		// Gemeinden
		Set<JaxGemeinde> jaxGemeinden = berechtigung.getGemeindeList().stream()
			.map(this::gemeindeToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
		jaxBerechtigung.setGemeindeList(jaxGemeinden);

		return jaxBerechtigung;
	}

	public JaxBerechtigungHistory berechtigungHistoryToJax(BerechtigungHistory history) {
		JaxBerechtigungHistory jaxHistory = new JaxBerechtigungHistory();
		convertAbstractDateRangedFieldsToJAX(history, jaxHistory);
		requireNonNull(history.getUserErstellt());
		jaxHistory.setUserErstellt(history.getUserErstellt());
		jaxHistory.setUsername(history.getUsername());
		jaxHistory.setRole(history.getRole());
		if (history.getInstitution() != null) {
			jaxHistory.setInstitution(institutionToJAX(history.getInstitution()));
		}
		if (history.getTraegerschaft() != null) {
			jaxHistory.setTraegerschaft(traegerschaftLightToJAX(history.getTraegerschaft()));
		}
		if (history.getSozialdienst() != null) {
			jaxHistory.setSozialdienst(jaxSozialdienstConverter.sozialdienstToJAX(history.getSozialdienst()));
		}
		jaxHistory.setGemeinden(history.getGemeinden());
		jaxHistory.setStatus(history.getStatus());
		jaxHistory.setGeloescht(history.getGeloescht());
		return jaxHistory;
	}

	private void loadGemeindenFromJax(@Nonnull JaxBerechtigung jaxBerechtigung, @Nonnull Berechtigung berechtigung) {
		final Set<Gemeinde> gemeindeListe = new HashSet<>();
		for (JaxGemeinde jaxGemeinde : jaxBerechtigung.getGemeindeList()) {
			if (jaxGemeinde.getId() != null) {
				Gemeinde gemeinde = gemeindeService.findGemeinde(jaxGemeinde.getId())
					.orElseThrow(() -> new EbeguRuntimeException(
						"findGemeinde",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						jaxGemeinde.getId()));
				gemeindeListe.add(gemeinde);
			}
		}
		berechtigung.setGemeindeList(gemeindeListe);
	}
}
