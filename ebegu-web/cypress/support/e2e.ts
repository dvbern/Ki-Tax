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
    'dv-valueinput',
] as const;
const elementsWithSuffixSelector: [
    (typeof rootInteractionElements)[number],
    string
][] = [
    ['dv-checkbox-x', '[data-test="checkbox"]'],
    ['dv-datepicker', 'input'],
    ['dv-date-picker-x', 'input'],
    ['dv-valueinput', 'input'],
    ['md-autocomplete', 'input'],
    ['mat-radio-button', 'label'],
];


Cypress.SelectorPlayground.defaults({
    onElement: (el) => {
        const prefixSelector = el
            .parents('[data-test^="container."]')
            .toArray()
            .map((el) => `[data-test="${el.dataset.test}"]`)
            .join(' ');
        let suffixSelector = '';
        let workingEl = el.parents(
            rootInteractionElements.map((input) => `${input}[data-test]`).join(','),
        );
        if (workingEl.length === 0 || el.data('test') || workingEl.data('test')?.startsWith('container.')) {
            workingEl = el;
        }
        const [, suffix] =
        elementsWithSuffixSelector.find(
            ([sel]) => workingEl.prop('tagName') === sel.toUpperCase(),
        ) ?? [];
        if (suffix) {
            suffixSelector = suffix;
        }
        if (workingEl.length > 0 && workingEl.data('test') != null) {
            return [
                prefixSelector,
                `[data-test="${workingEl.data('test')}"]`,
                suffixSelector,
            ]
                .filter((s) => s.length > 0)
                .join(' ');
        }
        return undefined;
    },
});
