package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.time.LocalDate;
import java.time.Month;

import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class TestfallDataProviderVisitor implements MandantVisitor<AbstractTestfallDataProvider> {

	private final Gesuchsperiode gesuchsperiode;
	private static final LocalDate START_FKJV = LocalDate.of(2022, Month.AUGUST, 1);

	public TestfallDataProviderVisitor(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public AbstractTestfallDataProvider getTestDataProvider(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractTestfallDataProvider visitBern() {
		if (gesuchsperiode.getGueltigkeit().getGueltigAb().isBefore(START_FKJV)) {
			return new AsivBernTestfallDataProvider(gesuchsperiode);
		}

		return new FkjvBernTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitLuzern() {
		return new LuzernTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitSolothurn() {
		return new SolothurnTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitAppenzellAusserrhoden() {
		return new AppenzellTestfallDataProvider(gesuchsperiode);
	}
}
