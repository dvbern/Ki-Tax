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

package ch.dvbern.ebegu.outbox;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.AbholungTagesschule;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Regelwerk;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungStatus;
import ch.dvbern.kibon.exchange.commons.types.Intervall;
import ch.dvbern.kibon.exchange.commons.types.Mandant;
import ch.dvbern.kibon.exchange.commons.types.Wochentag;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class ApiCompatiblitityTest {

	static Stream<Arguments> enums() {
		return Stream.of(
			Arguments.of(ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp.class, BetreuungsangebotTyp.class),
			Arguments.of(ch.dvbern.kibon.exchange.commons.types.EinschulungTyp.class, EinschulungTyp.class),
			Arguments.of(ch.dvbern.kibon.exchange.commons.types.Geschlecht.class, Geschlecht.class),
			Arguments.of(Intervall.class, BelegungTagesschuleModulIntervall.class),
			Arguments.of(Mandant.class, MandantIdentifier.class),
			Arguments.of(ch.dvbern.kibon.exchange.commons.types.Regelwerk.class, Regelwerk.class),
			Arguments.of(Wochentag.class, DayOfWeek.class),
			Arguments.of(Zeiteinheit.class, PensumUnits.class),
			Arguments.of(ch.dvbern.kibon.exchange.commons.tagesschulen.AbholungTagesschule.class, AbholungTagesschule.class)
		);
	}

	@ParameterizedTest
	@MethodSource("enums")
	<E extends Enum<E>> void testSetupValidation(Class<E> exchangeEnum, Class<E> kiBonEnum) {
		assertThat(exchangeEnum, is(not(kiBonEnum)));
	}

	@ParameterizedTest
	@MethodSource("enums")
	<E extends Enum<E>> void testEnumMapping(Class<E> exchangeEnum, Class<E> kiBonEnum) {
		Set<String> exchangeNames = names(exchangeEnum);
		Set<String> kiBonNames = names(kiBonEnum);

		assertThat(exchangeNames, containsInAnyOrder(kiBonNames.toArray()));
	}

	@Test
	void tagesschuleAnmeldungStatus() {
		Set<String> exchangeNames = names(TagesschuleAnmeldungStatus.class);

		Set<String> kiBonNames = names(Betreuungsstatus.class).stream()
			.filter(name -> name.startsWith("SCHULAMT_"))
			.filter(name -> !name.equals(Betreuungsstatus.SCHULAMT_MUTATION_IGNORIERT.name()))
			.collect(Collectors.toSet());

		assertThat(exchangeNames, containsInAnyOrder(kiBonNames.toArray()));
	}

	@Nonnull
	private <E extends Enum<E>> Set<String> names(Class<E> enumClass) {
		return Arrays.stream(enumClass.getEnumConstants())
			.map(Enum::name)
			// we use UNKNOWN as default value in the AVRO schemas - UNKNOWN typically doesn't exist in kiBon
			.filter(name -> !name.equals("UNKNOWN"))
			.collect(Collectors.toSet());
	}
}
