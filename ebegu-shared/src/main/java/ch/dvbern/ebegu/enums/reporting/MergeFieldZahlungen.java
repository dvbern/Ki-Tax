/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.enums.reporting;

import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BIGDECIMAL_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BOOLEAN_X_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATETIME_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldZahlungen implements MergeFieldProvider {

    periodeParam(new SimpleMergeField<>("periodeParam", STRING_CONVERTER)),
    gemeindeParam(new SimpleMergeField<>("gemeindeParam", STRING_CONVERTER)),
    institutionParam(new SimpleMergeField<>("institutionParam", STRING_CONVERTER)),
    timestampParam(new SimpleMergeField<>("timestampParam", DATETIME_CONVERTER)),


    repeatRow(new RepeatRowMergeField("repeatRow")),
    zahlungslaufTitle(new SimpleMergeField<>("zahlungslaufTitle", STRING_CONVERTER)),
    faelligkeitsDatum(new SimpleMergeField<>("faelligkeitsDatum", DATE_CONVERTER)),
    gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
    institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
    timestampZahlungslauf(new SimpleMergeField<>("timestampZahlungslauf", DATETIME_CONVERTER)),
    kindVorname(new SimpleMergeField<>("kindVorname", STRING_CONVERTER)),
    kindNachname(new SimpleMergeField<>("kindNachname", STRING_CONVERTER)),
    referenzNummer(new SimpleMergeField<>("referenznummer", STRING_CONVERTER)),
    zeitabschnittVon(new SimpleMergeField<>("zeitabschnittVon", DATE_CONVERTER)),
    zeitabschnittBis(new SimpleMergeField<>("zeitabschnittBis", DATE_CONVERTER)),
    bgPensum(new SimpleMergeField<>("bgPensum", BIGDECIMAL_CONVERTER)),
    betrag(new SimpleMergeField<>("betrag", BIGDECIMAL_CONVERTER)),
    korrektur(new SimpleMergeField<>("korrektur", BOOLEAN_X_CONVERTER)),
    ignorieren(new SimpleMergeField<>("ignorieren", BOOLEAN_X_CONVERTER)),
    ibanEltern(new SimpleMergeField<>("ibanEltern", STRING_CONVERTER)),
    kontoEltern(new SimpleMergeField<>("kontoEltern", STRING_CONVERTER));

    @Nonnull
    private final MergeField<?> mergeField;

    <V> MergeFieldZahlungen(@Nonnull MergeField<V> mergeField) {
        this.mergeField = mergeField;
    }

    @Override
    @Nonnull
    public <V> MergeField<V> getMergeField() {
        //noinspection unchecked
        return (MergeField<V>) mergeField;
    }
}
