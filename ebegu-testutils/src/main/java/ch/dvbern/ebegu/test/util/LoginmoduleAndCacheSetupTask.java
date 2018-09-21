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

package ch.dvbern.ebegu.test.util;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is run at wildfly test startup and should install a infinispan cache into the container. it basically
 * runs the following cli conmmands
 * /subsystem=infinispan/cache-container=ebeguCache:add(default-cache="ebeguAuthorizationCache")
 * /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache:add()
 * /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/transaction=TRANSACTION:add
 * (mode=FULL_XA)
 * /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/eviction=EVICTION:add
 * (strategy=LRU, max-entries=10000)
 * /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/expiration=EXPIRATION:add
 * (lifespan=1800000)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class LoginmoduleAndCacheSetupTask implements ServerSetupTask {

	private static final Logger LOG = LoggerFactory.getLogger(LoginmoduleAndCacheSetupTask.class);

	private static final String JBOSSAS_CONTAINER = "wildfly-container";

	@Override
	public void setup(ManagementClient managementClient, String containerId) {
		if (!containerId.startsWith(JBOSSAS_CONTAINER)) {
			return;
		}

		//add login module
		addEbeguTestLoginModule(managementClient);

		//ad system properties
		addProperties(managementClient);

		// add infinispan cache subsystem
		addInfinispanCacheForTest(managementClient);

		setLogLevel(managementClient);

		//        // FIXME reload is not working due to https://bugzilla.redhat.com/show_bug.cgi?id=900065
		//         managementClient.getControllerClient().execute(ModelUtil.createOpNode(null, "reload"));
	}

	public void addProperties(ManagementClient managementClient) {
		//generate lucene index to jboss data dir
		String jbossDataDirPath = System.getProperty("jboss.server.data.dir", "target/lucene-index");
		if (jbossDataDirPath != null) {
			ModelNode stepa = WildflyCLIUtil.createOpNode("system-property=hibernate.search.default.indexBase", "add");
			stepa.get("value").set(jbossDataDirPath + "/lucene-index");
			ModelNode op = WildflyCLIUtil.createCompositeNode(stepa);

			boolean success = WildflyCLIUtil.execute(managementClient, op);
			LOG.info(
				"Installing ebegu system propertiesfor ebegu application {}",
				new Object[] { success ? "passed" : "failed" });
		}
	}

	private void addEbeguTestLoginModule(ManagementClient managementClient) {
		ModelNode step1 = WildflyCLIUtil.createOpNode("subsystem=security/security-domain=ebegu-test/", "add");
		step1.get("cache-type").set("default");
		ModelNode step2 =
			WildflyCLIUtil.createOpNode("subsystem=security/security-domain=ebegu-test/authentication=classic", "add");
		ModelNode step3 = WildflyCLIUtil.createOpNode(
			"subsystem=security/security-domain=ebegu-test/authentication=classic/login-module=UsersRoles",
			"add");
		step3.get("code").set("UsersRoles");
		step3.get("flag").set("sufficient");

		ModelNode op = WildflyCLIUtil.createCompositeNode(step1, step2, step3);

		boolean success = WildflyCLIUtil.execute(managementClient, op);
		LOG.info(
			"Installing ebegu dummy demo login module   for ebegu application {}",
			new Object[] { success ? "passed" : "failed" });
	}

	private void addInfinispanCacheForTest(ManagementClient managementClient) {
		ModelNode step1 = WildflyCLIUtil.createOpNode("subsystem=infinispan/cache-container=ebeguCache", "add");
		step1.get("default-cache").set("ebeguAuthorizationCache");
		ModelNode step2 = WildflyCLIUtil.createOpNode(
			"subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache",
			"add");
		ModelNode step3 = WildflyCLIUtil.createOpNode(
			"subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/transaction"
				+ "=TRANSACTION",
			"add");
		step3.get("mode").set("FULL_XA");
		ModelNode step4 = WildflyCLIUtil.createOpNode(
			"subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/eviction=EVICTION",
			"add");
		step4.get("strategy").set("LRU");
		step4.get("max-entries").set("100");

		ModelNode op = WildflyCLIUtil.createCompositeNode(step1, step2, step3, step4);

		// add infinispan cache subsystem
		boolean success = WildflyCLIUtil.execute(managementClient, op);
		LOG.info(
			"Installing infinispan cache for ebegu application {}",
			new Object[] { success ? "passed" : "failed" });
	}

	private void setLogLevel(ManagementClient managementClient) {
		ModelNode step1 = WildflyCLIUtil.createOpNode("subsystem=logging/console-handler=CONSOLE", "write-attribute");
		step1.get("name").set("level");
		step1.get("value").set("WARN");

		ModelNode step2 =
			WildflyCLIUtil.createOpNode("subsystem=logging/logger=org.dbunit.dataset.AbstractTableMetaData", "add");
		step2.get("level").set("ERROR");

		ModelNode op = WildflyCLIUtil.createCompositeNode(step1, step2);

		boolean success = WildflyCLIUtil.execute(managementClient, op);
		LOG.warn(
			"Setting log level for console handler to WARN {}",
			new Object[] { success ? "passed" : "failed" });
	}

	@Override
	public void tearDown(ManagementClient managementClient, String containerId) {

		if (!containerId.startsWith(JBOSSAS_CONTAINER)) {
			return;
		}

		LOG.info("starting removal of  Infinispan cache for ebegu testing");

		ModelNode op = WildflyCLIUtil.createOpNode("subsystem=infinispan/cache-container=ebeguCache", "remove");
		// remove picketlink subsystem
		boolean success = WildflyCLIUtil.execute(managementClient, op);
		LOG.info(
			"Uninstalling infinispan cache for ebegu application {}",
			new Object[] { success ? "passed" : "failed" });

		ModelNode op2 = WildflyCLIUtil.createOpNode("subsystem=security/security-domain=ebegu-test", "remove");
		boolean success2 = WildflyCLIUtil.execute(managementClient, op2);
		LOG.info(
			"Uninstalling demo login module  for ebegu application {}",
			new Object[] { success2 ? "passed" : "failed" });

		ModelNode op3 = WildflyCLIUtil.createOpNode("system-property=hibernate.search.default.indexBase", "remove");
		boolean success3 = WildflyCLIUtil.execute(managementClient, op3);
		LOG.info(
			"Uninstalling system properties {}",
			new Object[] { success3 ? "passed" : "failed" });

		// FIXME reload is not working due to https://bugzilla.redhat.com/show_bug.cgi?id=900065
		// managementClient.getControllerClient().execute(ModelUtil.createOpNode(null, "reload"));
	}
}
