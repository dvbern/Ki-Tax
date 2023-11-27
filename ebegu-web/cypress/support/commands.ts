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

/// <reference types="cypress" />

type OnlyValidSelectors<T> = T extends string
    ? T extends `${string}${'[data-test='}${string}`
        ? 'Please specify the value given to data-test="", getByData automatically wraps the value in [data-test="..."]'
        : T
    : never;

type User =
    '[1-Superadmin] E-BEGU Superuser' |

    '[2-Admin-Kanton-Bern] Bernhard Röthlisberger' |
    '[2-SB-Kanton-Bern] Benno Röthlisberger' |

    '[3-Admin-Institution-Kita-Brünnen] Silvia Bergmann' |
    '[3-SB-Institution-Kita-Brünnen] Sophie Bergmann' |
    '[3-Admin-TS-Paris] Serge Gainsbourg' |
    '[3-SB-TS-Paris] Charlotte Gainsbourg' |
    '[3-Admin-Tägerschaft-Kitas-StadtBern] Bernhard Bern' |
    '[3-SB-Tägerschaft-Kitas-StadtBern] Agnes Krause' |

    '[4-Admin-Unterstützung-BernerSozialdienst] Patrick Melcher' |
    '[4-SB-Unterstützung-BernerSozialdienst] Max Palmer' |

    '[5-GS] Emma Gerber' |
    '[5-GS] Heinrich Mueller' |
    '[5-GS] Michael Berger' |
    '[5-GS] Hans Zimmermann' |
    '[5-GS] Jean Chambre' |

    '[6-Admin-BG] Kurt Blaser' |
    '[6-Admin-BG] Kurt Schmid' |
    '[6-Admin-BG] Kurt Kälin' |

    '[6-SB-BG] Jörg Becker' |
    '[6-SB-BG] Jörg Keller' |
    '[6-SB-BG] Jörg Aebischer' |

    '[6-Admin-TS] Adrian Schuler' |
    '[6-Admin-TS] Adrian Huber' |
    '[6-Admin-TS] Adrian Bernasconi' |

    '[6-SB-TS] Julien Schuler' |
    '[6-SB-TS] Julien Odermatt' |
    '[6-SB-TS] Julien Bucheli' |

    '[6-Admin-Gemeinde] Gerlinde Hofstetter' |
    '[6-Admin-Gemeinde] Gerlinde Bader' |
    '[6-Admin-Gemeinde] Gerlinde Mayer' |

    '[6-SB-Gemeinde] Stefan Wirth' |
    '[6-SB-Gemeinde] Stefan Weibel' |
    '[6-SB-Gemeinde] Stefan Marti' |

    '[6-SB-Ferienbetreuung-Gemeinde] Marlene Stöckli' |
    '[6-SB-Ferienbetreuung-Gemeinde] Jordan Hefti' |
    '[6-SB-Ferienbetreuung-Gemeinde] Valentin Burgener' |

    '[6-Admin-Ferienbetreuung-Gemeinde] Sarah Riesen' |
    '[6-Admin-Ferienbetreuung-Gemeinde] Jean-Pierre Kraeuchi' |
    '[6-Admin-Ferienbetreuung-Gemeinde] Christoph Hütter' |

    '[6-Steueramt] Rodolfo Geldmacher' |
    '[6-Steueramt] Rodolfo Iten' |
    '[6-Steueramt] Rodolfo Hermann' |

    '[Revisor] Reto Revisor' |
    '[Revisor] Reto Werlen' |
    '[Revisor] Reto Hug' |

    '[Jurist] Julia Jurist' |
    '[Jurist] Julia Adler' |
    '[Jurist] Julia Lory';

declare namespace Cypress {
    interface Chainable<Subject> {
        /**
         * Uses **`cy.session`** to create a new session with given user
         *
         * Please use the **`data-test`** values found at **`/#/locallogin`** except the _`test-user-`_ prefix
         *
         * @example
         * // ✅ ok
         * cy.login('E-BEGU-Superuser');
         *
         * // ⛔ not
         * cy.login('test-user-E-BEGU-Superuser');
         */
        login(user: User): void;

        /**
         * Used to change the used during the test, should be used with caution.
         *
         * @see login
         */
        changeLogin(user: User): void;

        /**
         * It is a shorthand for **`cy.get('[data-test="..."])`** and also allows to sub-select nested elements.
         *
         * @example
         *   cy.getByData('form-kind', 'vorname');
         *   // equals
         *   cy.get('[data-test="form-kind"] [data-test="vorname"]');
         *
         * @example
         *   cy.getByData('dv-radiobutton').find('label');
         *   // technically equals
         *   cy.get('[data-test="dv-radiobutton"] label');
         */
        getByData<T extends string>(name: OnlyValidSelectors<T>, ...nestedNames: OnlyValidSelectors<T>[]): Chainable<Subject>;
    }
}
Cypress.Commands.add('login', (user: User) => {
    console.log('/.*] (.*)/.exec(user)', /.*] (.*)/.exec(user));
    const userSelector = /.*] (.*)/.exec(user)[1].split(' ').join('-');
    cy.session(
        'login' + user,
        () => {
            cy.intercept({ pathname: '**/auth/authenticated-user', method: 'GET', times: 1 }).as('authCall');
            cy.visit('/#/locallogin');
            cy.get(`[data-test="test-user-${userSelector}"]`).click();
            cy.wait('@authCall');
        },
        {
            validate: () => {
                cy.intercept({ pathname: '**/auth/authenticated-user', method: 'GET', times: 1 }).as('authCallValidation');
                cy.visit('/#/');
                cy.reload();
                cy.wait('@authCallValidation')
                    .its('response.body')
                    .then((response) => {
                        expect(`${response.vorname}-${response.nachname}`).eq(userSelector);
                    });
            },
        },
    );
});
Cypress.Commands.add('getByData', (name, ...names) => {
    return cy.get([name, ...names].map((name) => `[data-test="${name}"]`).join(' '));
});
Cypress.Commands.add('changeLogin', (user: User) => {
    cy.clearAllSessionStorage();
    cy.clearAllCookies();

    cy.visit('/#/');
    cy.reload();

    cy.login(user);
});
