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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// ***********************************************************
// This example support/e2e.ts is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands';

const rootInteractionElements = [
    'a',
    'button',
    'mat-select',
    'mat-option',
    'mat-row',
    'tr',
    'md-checkbox',
    'md-autocomplete',
    'mat-radio-button',
    'md-radio-button',
    'dv-accordion-tab',
    'dv-checkbox-x',
    'dv-datepicker',
    'dv-date-picker-x',
    'dv-input-label-field',
    'dv-valueinput',
    'dv-valueinput-x'
] as const;
const elementsWithSuffixSelector: [
    (typeof rootInteractionElements)[number],
    string
][] = [
    ['dv-checkbox-x', '[data-test="checkbox"]'],
    ['dv-datepicker', 'input'],
    ['dv-date-picker-x', 'input'],
    ['dv-input-label-field', 'input'],
    ['dv-valueinput', 'input'],
    ['dv-valueinput-x', 'input'],
    ['md-autocomplete', 'input'],
    ['mat-radio-button', 'label']
];

Cypress.SelectorPlayground.defaults({
    onElement: el => {
        const prefixSelector = el
            .parents('[data-test^="container."]')
            .toArray()
            .map(el => `[data-test="${el.dataset.test}"]`)
            .join(' ');
        let suffixSelector = '';
        let workingEl = el.parents(
            rootInteractionElements
                .map(input => `${input}[data-test]`)
                .join(',')
        );
        if (
            workingEl.length === 0 ||
            el.data('test') ||
            workingEl.data('test')?.startsWith('container.')
        ) {
            workingEl = el;
        }
        const [, suffix] =
            elementsWithSuffixSelector.find(
                ([sel]) => workingEl.prop('tagName') === sel.toUpperCase()
            ) ?? [];
        if (suffix) {
            suffixSelector = suffix;
        }
        if (workingEl.length > 0 && workingEl.data('test') != null) {
            return [
                prefixSelector,
                `[data-test="${workingEl.data('test')}"]`,
                suffixSelector
            ]
                .filter(s => s.length > 0)
                .join(' ');
        }
        return undefined;
    }
});
