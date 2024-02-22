package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.ModulTagesschuleTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(EasyMockExtension.class)
public class InstitutionStammdatenInitalizerServiceTest extends EasyMockSupport {

	@TestSubject
	private final InstitutionStammdatenInitializerServiceBean institutionStammdatenInitializerServiceBean = new InstitutionStammdatenInitializerServiceBean();

	@Mock
	private GesuchsperiodeService gesuchsperiodeService;

	@Mock
	private GemeindeService gemeindeService;

	private static final String GEMEINDE_ID = "123";

	@Test
	public void initInstitutionStammdatenBetreuungsgutschein() {
		InstitutionStammdaten institutionStammdaten =
			institutionStammdatenInitializerServiceBean.initInstitutionStammdatenBetreuungsgutschein();

		assertThat(institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine(), notNullValue());
		assertThat(institutionStammdaten.getInstitutionStammdatenTagesschule(), nullValue());
		assertThat(institutionStammdaten.getInstitutionStammdatenFerieninsel(), nullValue());
	}

	@Test
	public void initTagesschule_gemeindNull() {
		EbeguRuntimeException e = assertThrows(EbeguRuntimeException.class, () -> {
			institutionStammdatenInitializerServiceBean.initInstitutionStammdatenTagesschule(null);
		});

		assertThat(e.getMessage(), is("missing gemeindeId"));
	}

	@Test
	public void initFerieninsel_gemeindNull() {
		EbeguRuntimeException e = assertThrows(EbeguRuntimeException.class, () -> {
			institutionStammdatenInitializerServiceBean.initInstitutionStammdatenTagesschule(null);
		});

		assertThat(e.getMessage(), is("missing gemeindeId"));
	}

	@Test
	public void initTagesschule_gemeindNotFound() {
		expect(gemeindeService.findGemeinde(GEMEINDE_ID)).andReturn(Optional.empty());
		replayAll();

		EbeguRuntimeException e = assertThrows(EbeguEntityNotFoundException.class, () -> {
			institutionStammdatenInitializerServiceBean.initInstitutionStammdatenTagesschule(GEMEINDE_ID);
		});

		assertThat(e.getErrorCodeEnum(), is(ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}

	@Test
	public void initFerieninsel_gemeindNotFound() {
		expect(gemeindeService.findGemeinde(GEMEINDE_ID)).andReturn(Optional.empty());
		replayAll();

		EbeguRuntimeException e = assertThrows(EbeguEntityNotFoundException.class, () -> {
			institutionStammdatenInitializerServiceBean.initInstitutionStammdatenFerieninsel(GEMEINDE_ID);
		});

		assertThat(e.getErrorCodeEnum(), is(ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}
	@Test
	public void initTagesschule() {
		expect(gemeindeService.findGemeinde(GEMEINDE_ID)).andReturn(Optional.of(new Gemeinde()));
		expect(gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden()).andReturn(List.of(new Gesuchsperiode()));
		replayAll();

		InstitutionStammdaten stammdaten =
			institutionStammdatenInitializerServiceBean.initInstitutionStammdatenTagesschule(GEMEINDE_ID);

		assertThat(stammdaten.getInstitutionStammdatenTagesschule(), notNullValue());
		assertThat(stammdaten.getInstitutionStammdatenTagesschule().getGemeinde(), notNullValue());
		assertThat(stammdaten.getInstitutionStammdatenTagesschule().getEinstellungenTagesschule(), notNullValue());
		stammdaten.getInstitutionStammdatenTagesschule().getEinstellungenTagesschule().forEach(einstellung -> {
			assertThat(einstellung.getInstitutionStammdatenTagesschule(), notNullValue());
			assertThat(einstellung.getGesuchsperiode(), notNullValue());
			assertThat(einstellung.getModulTagesschuleTyp(), is(ModulTagesschuleTyp.DYNAMISCH));
		});
		assertThat(stammdaten.getInstitutionStammdatenBetreuungsgutscheine(), nullValue());
		assertThat(stammdaten.getInstitutionStammdatenFerieninsel(), nullValue());
	}

	@Test
	public void initFerieninsel() {
		expect(gemeindeService.findGemeinde(GEMEINDE_ID)).andReturn(Optional.of(new Gemeinde()));
		expect(gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden()).andReturn(List.of(new Gesuchsperiode()));
		replayAll();

		InstitutionStammdaten stammdaten =
			institutionStammdatenInitializerServiceBean.initInstitutionStammdatenFerieninsel(GEMEINDE_ID);
		assertThat(stammdaten.getInstitutionStammdatenFerieninsel(), notNullValue());
		assertThat(stammdaten.getInstitutionStammdatenFerieninsel().getGemeinde(), notNullValue());
		assertThat(stammdaten.getInstitutionStammdatenFerieninsel().getEinstellungenFerieninsel(), notNullValue());
		stammdaten.getInstitutionStammdatenFerieninsel().getEinstellungenFerieninsel().forEach(einstellung -> {
			assertThat(einstellung.getInstitutionStammdatenFerieninsel(), notNullValue());
			assertThat(einstellung.getGesuchsperiode(), notNullValue());
		});
		assertThat(stammdaten.getInstitutionStammdatenBetreuungsgutscheine(), nullValue());
		assertThat(stammdaten.getInstitutionStammdatenTagesschule(), nullValue());
	}
}
