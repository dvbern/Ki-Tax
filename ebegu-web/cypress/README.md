# Cypress E2E Tests

[[_TOC_]]

## Install and preparations

**Local**

* `npm ci`
* Start the Frontend and Backend.
  > Make sure that `webpack` does not throw an error.
* `npm run cypress:open` (or `npm run cypress:run`/`npm run cypress:run:parallel`)

## Writing new Tests

Tests are located at `e2e/*.cy.ts`. All the common test Functions are also available with Cypress:

* `describe`, `describe.only`, `describe.skip`
* `it`, `it.only`, `it.skip`
* `expect` - should always run inside a `cy.{...}.then(() => expect...)`, due to the async nature of the cypress tests.

### TL;DR

* `npm run cypress:open` to start the Testsuite with UI
* Add or extend a test in `e2e/*.cy.ts`
* Keep the test plain sequential at the beginning and extract Page Objects (PO), fixtures, etc. at a later stage
  > i.e. just add plain `cy.visit(`, `cy.get(`, `).should` etc. with hardcoded values, expectations and selectors.
  > Only after reusable patterns emerge should we extract the relevant code into fixtures and POs.
* Use [`cy.getByData`](#dv-get-by-id) if possible.

### Page Objects (PO)

For short tests it is possible to just write some `cy.get` or `cy.getByData` and `.should`/`.type` tests.
But it is recommended to extract test code if a central Part of a test is too big or gets repeated too many times.

The current POs are just some functions that are scoped inside an Object and stateless.
They are located at `page-objects/*.po.ts`.

#### Test Specific PO

It is not recommended to share POs that are specific to a test case, for example to fill a new Kind view a series of PO commands
needs to be used:

```typescript
    // An example of creating a valid Kind
AntragKindPO.createNewKind();
AntragKindPO.fillKindForm();
AntragKindPO.fillPflegekind();

cy.getByData('show-fachstelle').click();
AntragKindPO.fillFachstelle();

cy.getByData('show-asylwesen').click();
cy.getByData('zemis-nummer').type('12345672.0');

cy.getByData('show-ausserordentlicher-anspruch').click();
AntragKindPO.fillAusserordentlicherAnspruch();
```

But as this code is very specific to the test itself it shouldn't be integrated in the PO. Also refrain from using save functions,
like triggering a save button on the page within an abstracted PO. Otherwise, the PO cannot be reused meaningfully.

### Anti-patterns (see [Cypress Best Practices](https://docs.cypress.io/guides/references/best-practices)):

* **Nesting POs** - Like for example a PO that calls another PO should not go deeper than 1 dependency, otherwise it gets
  confusing.
* **Using UI to login** - Usually it is not recommended to login via the UI if it is not specifically part of the E2E test, but
  the current implementation still needs to use the UI Login until a direct approach is implemented.
* **Complex Selectors** - Always try to use the `[data-test="..."]` selector and only fall back to _class_, _id_, etc. if the HTML
  code is unmodifiable.
* **`cy.` return values** - Do not try to save the return values of `cy.` commands like for example `cy.get` use `.as(...)` and
  `get('@...')`.
* **Coupling multiple tests together** - It only makes refactorings more complicated and also induces test dependencies issues.
* **Avoid `cy.wait`** - Each `cy.wait` will increase the test time drastically, use `cy.intercept` and other tools to find out if
  the test can continue.

There are many more recipes and ideas out there that should be sporadically be evaluated and potentially used as a refactoring or
improvement base for the E2E code, see https://docs.cypress.io/examples/recipes

### Custom commands

Cypress allows us to define additional `cy.` commands. Please look at `support/commands.ts` to see the currently implemented
helper commands that are exclusive to this project.

It is important to be able to differentiate between custom `cy.` commands and the ones given by cypress. Because of that, adding
more custom commands should only be done if it facilitates the writing of tests.

<span id="dv-get-by-id"></span>A central custom command usable in this project is the `cy.getByData` selector helper, it is a shorthand for
`cy.get('[data-test="..."])` and also allows to sub-select nested elements. For example `cy.getByData('form-kind', 'vorname')`
equals `cy.get('[data-test="form-kind"] [data-test="vorname"]')`.

### Context based selector

Sometimes components that define a `data-test` annotated element are being repeated on the same page, in order to select the
correct element it is recommended to mark the context on some parent element.

For example the save button used in Kibon is abstracted into a component, the `<button>` element is marked with an `data-test`
but the base component does not have any context information. For those cases it is important to add another `data-test` on a
parent element.

The current approach for such context descriptors is to use `container.` as a prefix for the `data-test="` value.

### Fixtures

Fixtures are a commonly used way to structure test data in a manner that, at least in theory, should easily be found and updated.
It is possible to define many different kind of fixture datasets, like simple JSON files, documents, pictures or archives. See
[cypress Fixtures](https://docs.cypress.io/api/commands/fixture#Examples) for more information.

Cypress integrates this concept by using `cy.fixture`, but the standard usage has the drawback that the data is not typed. To
ensure that the type information is present it is possible to use a custom helper located at `support/fixture-helper.ts`. This
tool loads the type information available in the Fixture data (for example if it is JSON) and delivers the fixture data with this
type information attached to it.

The currently prepared dataset structure is defined as followed:

1. Split the test data into groups, a `valid` and `invalid` dataset is anticipated but could change in the future.
2. Keep the data structure as clear and simple as possible and avoid too many nested levels.
3. The data structure should be consistent across the defined data-groups, create a new fixture set if it should deviate too much.

To create a new Fixture dataset, follow these steps:

1. Create or reuse a folder in `fixtures/`
2. Add the data as JSON.
   > For example (`fixtures/antrag/kind.json`):
   > ```json
   > {
   >     "valid": {
   >         "kind1": {
   >             "vorname": "kind1-vorname",
   >             "nachname": "kind1-nachname",
   >             "geschlecht": "MAENNLICH",
   >             "geburtsdatum":  "01.01.2020",
   >             "einschulungstyp": "VORSCHULALTER"
   >         }
   >     }
   > }
   > ```
3. Extend the `support/fixture-helper.ts` file with the new Fixture:
   > ```typescript
   > export const FixtureKind = {
   >     withValidBoy: fromFixture<typeof Kind>('antrag/kind.json', 'valid'),
   > }
   > ```
4. Use the new fixture by importing `import { FixtureKind } from '@dv-e2e/fixtures';`:
   > ```typescript
   > FixtureKind.withValid(({ kind1 }) => { // shorthand for `(data) => { ... data.kind1 ... }`
   >     cy.getByData(`geschlecht.radio-value.${kind1.geschlecht}`).click();
   >     cy.getByData('vorname').type(kind1.vorname);
   >     cy.getByData('nachname').type(kind1.nachname);
   >     cy.getByData('geburtsdatum').find('input').type(kind1.geburtsdatum);
   >     cy.getByData('einschulung-typ').select(kind1.einschulungstyp);
   > });
   > ```
