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

package ch.dvbern.ebegu.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.InstitutionStammdatenBuilderBe;

public class TestDataInstitutionStammdatenBuilder extends InstitutionStammdatenBuilderBe {
	private final Gesuchsperiode gesuchsperiode;
	private final Collection<InstitutionStammdaten> stammdatenOverwrites;

	public TestDataInstitutionStammdatenBuilder(
			Gesuchsperiode gesuchsperiode){
		super(null);
		this.gesuchsperiode = gesuchsperiode;
		this.stammdatenOverwrites = List.of();
	}

	public TestDataInstitutionStammdatenBuilder(Gesuchsperiode gesuchsperiode, Collection<InstitutionStammdaten> stammdatenOverwrites){
		super(null);
		this.gesuchsperiode = gesuchsperiode;
		this.stammdatenOverwrites = stammdatenOverwrites;
	}

	@Override
	public List<InstitutionStammdaten> buildStammdaten() {
		if (!stammdatenOverwrites.isEmpty()) {
			return new ArrayList<>(stammdatenOverwrites);
		}
		List<InstitutionStammdaten> institutionStammdaten = new ArrayList<>();
		institutionStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		institutionStammdaten.add(TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		institutionStammdaten.add(TestDataUtil.createInstitutionStammdatenFerieninselGuarda());

		institutionStammdaten.forEach(stammdaten -> {
			stammdaten.getInstitution().setMandant(gesuchsperiode.getMandant());
		});
		return institutionStammdaten;
	}
}
