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

package ch.dvbern.ebegu.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Testet BetreuungResource
 */

public class DokumenteUtilTest {

	private Mandant mandant;

	@Before
	public void setUp() {
		mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
	}

	@Test
	public void testAllPersistedInNeeded() {
		Set<DokumentGrund> dokumentGrundsNeeded = new HashSet<>();

		Collection<DokumentGrund> persistedDokumentGrunds = new HashSet<>();

		createGrundNeeded(dokumentGrundsNeeded, DokumentGrundTyp.FAMILIENSITUATION, DokumentTyp.JAHRESLOHNAUSWEISE);
		createGrundNeeded(dokumentGrundsNeeded, DokumentGrundTyp.FAMILIENSITUATION, DokumentTyp.STEUERERKLAERUNG);
		createGrundNeeded(dokumentGrundsNeeded, DokumentGrundTyp.ERWERBSPENSUM, DokumentTyp.NACHWEIS_AUSBILDUNG);

		createGrundPersisted(persistedDokumentGrunds, DokumentGrundTyp.FAMILIENSITUATION, DokumentTyp.JAHRESLOHNAUSWEISE, 3);

		final Set<DokumentGrund> mergeNeededAndPersisted = DokumenteUtil.mergeNeededAndPersisted(dokumentGrundsNeeded, persistedDokumentGrunds);

		Set<DokumentGrund> mergedFamsit = getByGrundTyp(mergeNeededAndPersisted, DokumentGrundTyp.FAMILIENSITUATION);

		Assert.assertNotNull(mergedFamsit);
		Assert.assertEquals(2, mergedFamsit.size());

		Assert.assertEquals(3, getByDokumentType(mergedFamsit, DokumentTyp.JAHRESLOHNAUSWEISE).size());
		Assert.assertEquals(1, getByDokumentType(mergedFamsit, DokumentTyp.STEUERERKLAERUNG).size());

		Set<DokumentGrund> mergedERWERBSPENSUM = getByGrundTyp(mergeNeededAndPersisted, DokumentGrundTyp.ERWERBSPENSUM);
		Assert.assertNotNull(mergedERWERBSPENSUM);
		Assert.assertEquals(1, mergedERWERBSPENSUM.size());
		Assert.assertEquals(1, getByDokumentType(mergedERWERBSPENSUM, DokumentTyp.NACHWEIS_AUSBILDUNG).size());
	}

	@Test
	public void testGetFileNameForGeneratedDokumentTypBEGLEITSCHREIBEN() {
		Assert.assertEquals("Deckblatt_16.000001.pdf", DokumenteUtil
			.getFileNameForGeneratedDokumentTyp(
				GeneratedDokumentTyp.BEGLEITSCHREIBEN,
				"16.000001",
				Constants.DEFAULT_LOCALE,
				mandant
			)
		);
	}

	@Test
	public void testGetFileNameForGeneratedDokumentTypFINANZIELLE_SITUATION() {
		Assert.assertEquals("Finanzielle_Verhaeltnisse_16.000001.pdf", DokumenteUtil
			.getFileNameForGeneratedDokumentTyp(
				GeneratedDokumentTyp.FINANZIELLE_SITUATION,
				"16.000001",
				Constants.DEFAULT_LOCALE,
				mandant
			)
		);
	}

	@Test
	public void testGetFileNameForGeneratedDokumentTypVERFUEGUNG_KITA() {
		Assert.assertEquals("Verfuegung_16.000001.1.1.pdf", DokumenteUtil
			.getFileNameForGeneratedDokumentTyp(
				GeneratedDokumentTyp.VERFUEGUNG,
				"16.000001.1.1",
				Constants.DEFAULT_LOCALE,
				mandant
			)
		);
	}

	@Test
	public void testCompareDokumentGrundNewVersionSameDoks() {
		DokumentGrund persistedDok = new DokumentGrund(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, "tag",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentTyp.BESTAETIGUNG_ARZT);
		DokumentGrund neededDok = new DokumentGrund(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, "tag",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentTyp.BESTAETIGUNG_ARZT);
		int result = DokumenteUtil.compareDokumentGrunds(persistedDok, neededDok);
		Assert.assertEquals(0, result);
	}

	@Test
	public void testCompareDokumentGrundNewVersionDifferentPersonNumber() {
		DokumentGrund persistedDok = new DokumentGrund(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, "tag",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentTyp.BESTAETIGUNG_ARZT);
		DokumentGrund neededDok = new DokumentGrund(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, "tag",
			DokumentGrundPersonType.GESUCHSTELLER, 2, DokumentTyp.BESTAETIGUNG_ARZT);
		int result = DokumenteUtil.compareDokumentGrunds(persistedDok, neededDok);
		Assert.assertNotEquals(0, result);
	}

	@Test
	public void testCompareDokumentGrundNewVersionDifferentPersonTypes() {
		DokumentGrund persistedDok = new DokumentGrund(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, "tag",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentTyp.BESTAETIGUNG_ARZT);
		DokumentGrund neededDok = new DokumentGrund(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, "tag",
			DokumentGrundPersonType.KIND, 1, DokumentTyp.BESTAETIGUNG_ARZT);
		int result = DokumenteUtil.compareDokumentGrunds(persistedDok, neededDok);
		Assert.assertNotEquals(0, result);
	}

	@Test
	public void testCompareDokumentGrundNewVersionDifferentDokumentGrundTypes() {
		DokumentGrund persistedDok = new DokumentGrund(DokumentGrundTyp.FINANZIELLESITUATION, "tag",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentTyp.BESTAETIGUNG_ARZT);
		DokumentGrund neededDok = new DokumentGrund(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG, "tag",
			DokumentGrundPersonType.GESUCHSTELLER, 1, DokumentTyp.BESTAETIGUNG_ARZT);
		int result = DokumenteUtil.compareDokumentGrunds(persistedDok, neededDok);
		Assert.assertNotEquals(0, result);
	}

	private Set<Dokument> getByDokumentType(Set<DokumentGrund> dokumentGrunds, DokumentTyp dokumentTyp) {
		Set<Dokument> dokumente = new HashSet<>();

		for (DokumentGrund dokumentGrund : dokumentGrunds) {
			if (dokumentGrund.getDokumentTyp() == dokumentTyp) {
				dokumente.addAll(dokumentGrund.getDokumente());
			}
		}
		return dokumente;

	}

	private Set<DokumentGrund> getByGrundTyp(Set<DokumentGrund> dokumentGrundsNeeded, DokumentGrundTyp dokumentGrundTyp) {

		Set<DokumentGrund> dokumentGrundsNeededMerged = new HashSet<>();
		for (DokumentGrund dokumentGrund : dokumentGrundsNeeded) {
			if (dokumentGrund.getDokumentGrundTyp() == dokumentGrundTyp) {
				dokumentGrundsNeededMerged.add(dokumentGrund);
			}
		}
		return dokumentGrundsNeededMerged;
	}

	private void createGrundNeeded(Set<DokumentGrund> dokumentGrundsNeeded, DokumentGrundTyp dokumentGrundTyp, DokumentTyp dokumentTyp) {

		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(dokumentGrundTyp);
		dokumentGrund.setDokumentTyp(dokumentTyp);

		Dokument dokument = new Dokument();
		Assert.assertNotNull(dokumentGrund.getDokumente());
		dokumentGrund.getDokumente().add(dokument);

		dokumentGrundsNeeded.add(dokumentGrund);
	}

	private void createGrundPersisted(
		Collection<DokumentGrund> dokumentGrunds,
		DokumentGrundTyp dokumentGrundTyp,
		DokumentTyp dokumentTyp,
		int number
	) {

		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(dokumentGrundTyp);
		dokumentGrund.setDokumentTyp(dokumentTyp);

		for (int i = 1; i < number; i++) {
			Dokument dokument = new Dokument();
			dokument.setFilename(i + " ");
			Assert.assertNotNull(dokumentGrund.getDokumente());
			dokumentGrund.getDokumente().add(dokument);
		}
		Dokument dokument = new Dokument();
		Assert.assertNotNull(dokumentGrund.getDokumente());
		dokumentGrund.getDokumente().add(dokument);

		dokumentGrunds.add(dokumentGrund);
	}
}
