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

package ch.dvbern.ebegu.rest.test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.api.resource.GesuchsperiodeResource;
import ch.dvbern.ebegu.api.resource.authentication.AuthResource;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.test.util.LoginmoduleAndCacheSetupTask;
import ch.dvbern.lib.cdipersistence.ISessionContextService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.ArrayUtils;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.resteasy.core.ResteasyHttpServletResponseWrapper;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.exporter.zip.ZipExporterImpl;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.RejectDependenciesStrategy;
import org.junit.Assert;

import static org.junit.Assert.assertNotNull;

/**
 * Diese Klasse implementiert die Methode "Deployment" fuer die Arquillian Tests und muss
 * von allen Testklassen in REST modul erweitert werden. Es verhaelt sich leicht anders als die Basisklasse in
 * AbstractEbeguTest
 */
@ArquillianSuiteDeployment
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
@ServerSetup(LoginmoduleAndCacheSetupTask.class)
public abstract class AbstractEbeguRestTest {

	@Inject
	private GesuchsperiodeResource gesuchsperiodeResource;

	public HttpServletResponse DUMMY_RESPONSE;
	public UriInfo DUMMY_URIINFO;

	public AbstractEbeguRestTest() {
		// it is required to instantiate HttpServletResponse to create the wrapper.
		HttpServletResponse response = new HttpServletResponse() {
			@Override
			public void addCookie(Cookie cookie) {

			}

			@Override
			public boolean containsHeader(String name) {
				return false;
			}

			@Override
			public String encodeURL(String url) {
				return "";
			}

			@Override
			public String encodeRedirectURL(String url) {
				return "";
			}

			@Override
			public String encodeUrl(String url) {
				return "";
			}

			@Override
			public String encodeRedirectUrl(String url) {
				return "";
			}

			@Override
			public void sendError(int sc, String msg) {

			}

			@Override
			public void sendError(int sc) {

			}

			@Override
			public void sendRedirect(String location) {

			}

			@Override
			public void setDateHeader(String name, long date) {

			}

			@Override
			public void addDateHeader(String name, long date) {

			}

			@Override
			public void setHeader(String name, String value) {

			}

			@Override
			public void addHeader(String name, String value) {

			}

			@Override
			public void setIntHeader(String name, int value) {

			}

			@Override
			public void addIntHeader(String name, int value) {

			}

			@Override
			public void setStatus(int sc) {

			}

			@Override
			public void setStatus(int sc, String sm) {

			}

			@Override
			public int getStatus() {
				return 0;
			}

			@Override
			public String getHeader(String name) {
				return "";
			}

			@Override
			public Collection<String> getHeaders(String name) {
				return Collections.emptyList();
			}

			@Override
			public Collection<String> getHeaderNames() {
				return Collections.emptyList();
			}

			@Override
			public String getCharacterEncoding() {
				return "";
			}

			@Override
			public String getContentType() {
				return "";
			}

			@Override
			public ServletOutputStream getOutputStream() {
				return null;
			}

			@Override
			public PrintWriter getWriter() {
				return null;
			}

			@Override
			public void setCharacterEncoding(String charset) {

			}

			@Override
			public void setContentLength(int len) {

			}

			@Override
			public void setContentLengthLong(long len) {

			}

			@Override
			public void setContentType(String type) {

			}

			@Override
			public void setBufferSize(int size) {

			}

			@Override
			public int getBufferSize() {
				return 0;
			}

			@Override
			public void flushBuffer() {

			}

			@Override
			public void resetBuffer() {

			}

			@Override
			public boolean isCommitted() {
				return false;
			}

			@Override
			public void reset() {

			}

			@Override
			public void setLocale(Locale loc) {

			}

			@Override
			public Locale getLocale() {
				return Locale.GERMAN;
			}
		};
		DUMMY_RESPONSE = new ResteasyHttpServletResponseWrapper(response, null) {};
		DUMMY_URIINFO = new ResteasyUriInfo("test", "test", "test");
	}

	@Deployment
	@OverProtocol("Servlet 3.0")
	public static Archive<?> createTestArchive() {
		//noinspection ConstantConditions
		return createTestArchive(null);
	}

	public static Archive<?> createTestArchive(@Nullable Class[] classesToAdd) {

		PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile("pom.xml");
		File[] runtimeDepsBefore = pom.importRuntimeDependencies().resolve()
			.using(new RejectDependenciesStrategy(false, "ch.dvbern.ebegu:ebegu-dbschema")) //wir wollen flyway nicht im test
			.asFile();
		File[] testDeps = pom.importTestDependencies().resolve().withoutTransitivity().asFile();

		File serverFile = findEbeguServerJarFile(runtimeDepsBefore);
		JavaArchive serverJar = createEbeguServerJar(serverFile);
		File[] runtimeDeps = removeEbeguServerJar(runtimeDepsBefore, serverFile);

		// wir fuegen die packages einzeln hinzu weil sonst klassen die im shared sind und das gleiche package haben doppelt eingefuegt werden
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "rest-test.war")

			.addClasses(AbstractEbeguRestLoginTest.class, Persistence.class,
				ISessionContextService.class, AbstractEntity.class)

			.addPackages(true, "ch/dvbern/ebegu/api")
			.addPackages(true, "ch/dvbern/ebegu/rest/test")
			.addAsLibraries(runtimeDeps)
			.addAsLibraries(serverJar) // import the created serverJar without persistence.xml again
			.addAsLibraries(testDeps)
			.addAsManifestResource("META-INF/TEST-MANIFEST.MF", "MANIFEST.MF")

			// entfernt unnoetige Klassen, die vielleicht Dependency-Konflikten ergeben wuerden, login erfolgt im test nicht ueber openam
			.deleteClass(AuthResource.class)

			.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
			.addAsWebInfResource("META-INF/test-beans.xml", "beans.xml")
			.addAsResource("META-INF/test-orm.xml", "META-INF/orm.xml")
			//deploy our test loginmodule
			.addAsResource("testlogin-users.properties", "users.properties")
			.addAsResource("testlogin-roles.properties", "roles.properties")
			.addAsWebInfResource("META-INF/test-jboss-web.xml", "jboss-web.xml")
			// Deploy our test datasource
			.addAsWebInfResource("test-ds.xml");

		if (classesToAdd != null) {
			webArchive.addClasses(classesToAdd);
		}
		//Folgende Zeile gibt im /tmp dir das archiv aus zum debuggen nuetzlich
		new ZipExporterImpl(webArchive).exportTo(new File(System.getProperty("java.io.tmpdir"), "myWebRestArchive.war"), true);
		return webArchive;
	}

	/**
	 * Takes the ebegu-server.jar out of the runtimeDependencies.
	 */
	@Nonnull
	private static File findEbeguServerJarFile(File[] runtimeDeps) {
		List<File> serverFile = Arrays.stream(runtimeDeps).filter(file -> file.getName().contains("ebegu-server-"))
			.collect(Collectors.toList());

		Assert.assertEquals(1, serverFile.size());
		return serverFile.get(0);
	}

	/**
	 *
	 */
	private static File[] removeEbeguServerJar(File[] runtimeDepsBefore, File serverFile) {
		return ArrayUtils.removeElement(runtimeDepsBefore, serverFile);
	}

	/**
	 * Removes the persistence.xml from the given (jar) file
	 * We need the test-persistence.xml for testing but the jar ebegu-server.jar already has the production persistence.xml. So we
	 * need to remove the last one because there can only be one persistenceUnit defined.
	 */
	@Nonnull
	private static JavaArchive createEbeguServerJar(@Nonnull File serverJarFile) {
		JavaArchive serverJar = ShrinkWrap
			.create(ZipImporter.class, serverJarFile.getName())
			.importFrom(serverJarFile)
			.as(JavaArchive.class);
		serverJar.delete("/META-INF/persistence.xml");

		return serverJar;
	}

	public JaxGesuchsperiode saveGesuchsperiodeInStatusEntwurf(JaxGesuchsperiode gesuchsperiode) {
		gesuchsperiode.setStatus(GesuchsperiodeStatus.ENTWURF);
		return gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
	}

	public JaxGesuchsperiode saveGesuchsperiodeInStatusAktiv(JaxGesuchsperiode gesuchsperiode) {
		gesuchsperiode.setStatus(GesuchsperiodeStatus.ENTWURF);
		gesuchsperiode = gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
		assertNotNull(gesuchsperiode);
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		return gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
	}

	public JaxGesuchsperiode saveGesuchsperiodeInStatusInaktiv(JaxGesuchsperiode gesuchsperiode) {
		gesuchsperiode.setStatus(GesuchsperiodeStatus.ENTWURF);
		gesuchsperiode = gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
		assertNotNull(gesuchsperiode);
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode = gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
		assertNotNull(gesuchsperiode);
		gesuchsperiode.setStatus(GesuchsperiodeStatus.INAKTIV);
		return gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
	}

	public JaxGesuchsperiode saveGesuchsperiodeInStatusGesperrt(JaxGesuchsperiode gesuchsperiode) {
		gesuchsperiode.setStatus(GesuchsperiodeStatus.ENTWURF);
		gesuchsperiode = gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
		assertNotNull(gesuchsperiode);
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode = gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
		assertNotNull(gesuchsperiode);
		gesuchsperiode.setStatus(GesuchsperiodeStatus.INAKTIV);
		gesuchsperiode = gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
		assertNotNull(gesuchsperiode);
		gesuchsperiode.setStatus(GesuchsperiodeStatus.GESCHLOSSEN);
		return gesuchsperiodeResource.saveGesuchsperiode(gesuchsperiode, DUMMY_URIINFO, DUMMY_RESPONSE);
	}
}
