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

import EbeguUtil from '../../../utils/EbeguUtil';

// Es wird empfohlen, Filters als normale Funktionen zu implementieren, denn es bringt nichts, dafuer eine Klasse zu
// implementieren.
quicksearchFilter.$inject = ['$filter'];

// Zuerst pruefen wir welcher Wert kommt, d.h. aus welcher Column. Je nach Column wird danach dem entsprechenden
// Comparator aufgerufen. Fuer mehrere Columns reicht es mit dem standard Comparator, der auch hier einfach
// implementiert wird.
export function quicksearchFilter($filter: any): (array: any, expression: any) => any {
    const filterFilter = $filter('filter');

    return (array, expression) => {
        function customComparator(actual: any, expected: any): boolean {
            if (expression.eingangsdatum && expression.eingangsdatum === expected) {
                return EbeguUtil.compareDates(actual, expected);
            }
            if (expression.fallNummer && expression.fallNummer === expected) {
                const actualString = EbeguUtil.addZerosToFallNummer(actual);
                return actualString.indexOf(expected) >= 0;
            }
            if (expression.gesuchsperiodeGueltigAb && expression.gesuchsperiodeGueltigAb === expected) {
                return EbeguUtil.compareDates(actual, expected);
            }
            if (expression.gesuchsperiodeGueltigBis && expression.gesuchsperiodeGueltigBis === expected) {
                return EbeguUtil.compareDates(actual, expected);
            }

            return EbeguUtil.hasTextCaseInsensitive(actual, expected);
        }

        return filterFilter(array, expression, customComparator);
    };
}
