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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.ApplicationProperty_;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer ApplicationProperty
 */
@Stateless
@Local(ApplicationPropertyService.class)
public class ApplicationPropertyServiceBean extends AbstractBaseService implements ApplicationPropertyService {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertyServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	private static final String NAME_MISSING_MSG = "name muss gesetzt sein";

	@Nonnull
	@Override
	public ApplicationProperty saveOrUpdateApplicationProperty(
		@Nonnull final ApplicationPropertyKey key,
		@Nonnull final String value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		Optional<ApplicationProperty> property = readApplicationProperty(key);
		if (property.isPresent()) {
			property.get().setValue(value);
			return persistence.merge(property.get());
		} else {
			return persistence.persist(new ApplicationProperty(key, value));
		}
	}

	@Nonnull
	@Override
	public Optional<ApplicationProperty> readApplicationProperty(@Nonnull final ApplicationPropertyKey key) {
		return criteriaQueryHelper.getEntityByUniqueAttribute(
			ApplicationProperty.class,
			key,
			ApplicationProperty_.name);
	}

	@Nonnull
	@Override
	public Collection<String> readMimeTypeWhitelist() {
		//note this is a candidate for caching
		Set<String> allowedTypes = Collections.emptySet();
		final Optional<ApplicationProperty> whitelistVal =
			this.readApplicationProperty(ApplicationPropertyKey.UPLOAD_FILETYPES_WHITELIST);
		if (whitelistVal.isPresent() && StringUtils.isNotEmpty(whitelistVal.get().getValue())) {
			final String[] values = whitelistVal.get().getValue().split(",");
			allowedTypes = Arrays.stream(values)
				.map(StringUtils::trimToNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		}
		return allowedTypes;
	}

	@Override
	public Optional<ApplicationProperty> readApplicationProperty(String keyParam) {
		try {
			ApplicationPropertyKey keyToSearch = Enum.valueOf(ApplicationPropertyKey.class, keyParam);
			return readApplicationProperty(keyToSearch);
		} catch (IllegalArgumentException e) {
			LOG.warn("Property not found {}", keyParam, e);
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public List<ApplicationProperty> getAllApplicationProperties() {
		return new ArrayList<>(criteriaQueryHelper.getAll(ApplicationProperty.class));
	}

	@Override
	public void removeApplicationProperty(@Nonnull ApplicationPropertyKey key) {
		Objects.requireNonNull(key);
		ApplicationProperty toRemove =
			readApplicationProperty(key).orElseThrow(() -> new EbeguEntityNotFoundException(
				"removeApplicationProperty",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, key));
		persistence.remove(toRemove);
	}

	@Override
	@Nullable
	public String findApplicationPropertyAsString(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		Optional<ApplicationProperty> property =
			criteriaQueryHelper.getEntityByUniqueAttribute(ApplicationProperty.class, name, ApplicationProperty_.name);
		return property.map(ApplicationProperty::getValue).orElse(null);
	}

	@Override
	@Nullable
	public BigDecimal findApplicationPropertyAsBigDecimal(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name);
		if (valueAsString != null) {
			return new BigDecimal(valueAsString);
		}
		return null;
	}

	@Override
	@Nullable
	public Integer findApplicationPropertyAsInteger(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name);
		if (valueAsString != null) {
			return Integer.valueOf(valueAsString);
		}
		return null;
	}

	@Override
	@Nullable
	public Boolean findApplicationPropertyAsBoolean(@Nonnull ApplicationPropertyKey name) {
		Objects.requireNonNull(name, NAME_MISSING_MSG);
		String valueAsString = findApplicationPropertyAsString(name);
		if (valueAsString != null) {
			return Boolean.valueOf(valueAsString);
		}
		return null;
	}

	@Override
	@Nonnull
	public Boolean findApplicationPropertyAsBoolean(@Nonnull ApplicationPropertyKey name, boolean defaultValue) {
		Boolean property = findApplicationPropertyAsBoolean(name);
		if (property == null) {
			return defaultValue;
		}
		return property;
	}

	@Override
	@Nonnull
	public LocalDate getStadtBernAsivStartDatum() {
		String valueAsString = findApplicationPropertyAsString(ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM);
		if (valueAsString != null) {
			return LocalDate.parse(valueAsString, Constants.DATE_FORMATTER);
		}
		// Default ist 1.1.2021
		return LocalDate.of(2021, Month.JANUARY, 1);
	}

	@Override
	@Nonnull
	public Boolean isStadtBernAsivConfigured() {
		return findApplicationPropertyAsBoolean(ApplicationPropertyKey.STADT_BERN_ASIV_CONFIGURED, false);
	}

	@Override
	@Nonnull
	public Boolean isKantonNotverordnungPhase2Aktiviert() {
		return findApplicationPropertyAsBoolean(ApplicationPropertyKey.KANTON_NOTVERORDNUNG_PHASE_2_AKTIV, false);
	}
}
