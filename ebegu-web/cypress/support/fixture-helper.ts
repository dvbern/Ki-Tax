import type * as FamSit from '../fixtures/antrag/famsit.json';
import type * as FinSit from '../fixtures/antrag/finsit.json';
import type * as Papier from '../fixtures/antrag/papier.json';

const fromFixture = <T, FixturePart extends keyof T = keyof T>(fixture: string, fixturePart: FixturePart) => <R>(fn: (data: T[FixturePart]) => R) =>
    cy.fixture(fixture).then((data: T) => fn(data[fixturePart]));

export const FixtureFamSit = {
    withValid: fromFixture<typeof FamSit>('antrag/famsit.json', 'valid'),
}

export const FixtureFinSit = {
    // withValid: <T>(fn: (data: (typeof FinSit)['valid']) => T) => cy.fixture('antrag/finsit.json').then((data: typeof FinSit) => fn(data.valid)),
    withValid: fromFixture<typeof FinSit>('antrag/finsit.json', 'valid'),
};

export const FixturePapierAntrag = {
    // withValid: <T>(fn: (data: (typeof Papier)['valid']) => T) => cy.fixture('antrag/papier.json').then((data: typeof Papier) => fn(data.valid)),
    withValid: fromFixture<typeof Papier>('antrag/papier.json', 'valid'),
};

