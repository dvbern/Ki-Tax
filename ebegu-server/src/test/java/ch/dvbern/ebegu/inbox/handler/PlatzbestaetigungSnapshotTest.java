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

package ch.dvbern.ebegu.inbox.handler;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.inbox.handler.pensum.PensumMapper;
import ch.dvbern.ebegu.inbox.handler.pensum.PensumMappingUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SnapshotExtension.class)
class PlatzbestaetigungSnapshotTest {

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	private Expect expect;

	/**
	 * This test will fail, when {@link BetreuungsmitteilungPensum} has new properties.
	 * <p>
	 * Verify, if these properties are relevant for improt via exchange service.
	 * <p>
	 * If no, simply update the snapshot (manually adjusting the reference file or by adjusting the `update-snapshot` property
	 * of `snapshot.properties`).
	 * <p>
	 * If yes, create a new {@link PensumMapper} for the property and add a comparator to {@link PensumMappingUtil#COMPARATOR}.
	 */
	@SuppressWarnings("TestMethodWithoutAssertion")
	@Test
	void mappingMustBeReconsideredWhenPropertiesChange() {
		DateRange dateRange = new DateRange(2024);

		EingewoehnungPauschale eingewoehnungPauschale = new EingewoehnungPauschale();
		eingewoehnungPauschale.setGueltigkeit(dateRange);

		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();
		pensum.setGueltigkeit(dateRange);
		pensum.setEingewoehnungPauschale(eingewoehnungPauschale);


		expect.serializer("json").toMatchSnapshot(pensum);
	}
}
