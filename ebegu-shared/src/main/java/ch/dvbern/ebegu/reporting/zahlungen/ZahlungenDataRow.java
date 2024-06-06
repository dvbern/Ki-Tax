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

package ch.dvbern.ebegu.reporting.zahlungen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO für den Lastenausgleich von KiBon
 */
@Getter
@Setter
public class ZahlungenDataRow {
    @Setter
    private String zahlungslaufTitle;
    @Getter
    @Setter
    private LocalDate zahlungsFaelligkeitsDatum;
    @Getter
    @Setter
    private String gemeinde;
    @Getter
    @Setter
    private String institution;
    @Getter
    @Setter
    private LocalDateTime timestampZahlungslauf;
    @Getter
    @Setter
    private String kindVorname;
    @Getter
    @Setter
    private String kindNachname;
    @Getter
    @Setter
    private String referenzNummer;
    @Getter
    @Setter
    private LocalDate zeitabschnittVon;
    @Getter
    @Setter
    private LocalDate zeitabschnittBis;
    @Getter
    @Setter
    private BigDecimal bgPensum;
    @Getter
    @Setter
    private BigDecimal betrag;
    @Getter
    @Setter
    private Boolean korrektur;
    @Getter
    @Setter
    private Boolean ignorieren;
    @Getter
    @Setter
    private String ibanEltern;
    @Getter
    @Setter
    private String kontoEltern;

}
