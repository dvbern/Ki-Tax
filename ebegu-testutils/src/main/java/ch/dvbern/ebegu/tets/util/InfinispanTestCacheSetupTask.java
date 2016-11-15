package ch.dvbern.ebegu.tets.util;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is run at wildfly test startup and should install a infinispan cache into the container. it basically runs the
 * following cli conmmands
 /subsystem=infinispan/cache-container=ebeguCache:add(default-cache="ebeguAuthorizationCache")
 /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache:add()
 /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/transaction=TRANSACTION:add(mode=FULL_XA)
 /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/eviction=EVICTION:add(strategy=LRU, max-entries=10000)
 /subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/expiration=EXPIRATION:add(lifespan=1800000)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class InfinispanTestCacheSetupTask implements ServerSetupTask {


	private static final Logger LOG = LoggerFactory.getLogger(InfinispanTestCacheSetupTask.class);

    private static final String JBOSSAS_CONTAINER = "wildfly-container";

    @Override
	public void setup(ManagementClient managementClient, String containerId){
        if (!containerId.startsWith(JBOSSAS_CONTAINER)) {
            return;
        }


        ModelNode step1 = WildflyCLIUtil.createOpNode("subsystem=infinispan/cache-container=ebeguCache", "add");
		step1.get("default-cache").set("ebeguAuthorizationCache");
		ModelNode step2 = WildflyCLIUtil.createOpNode("subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache", "add");
		ModelNode step3 = WildflyCLIUtil.createOpNode("subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/transaction=TRANSACTION", "add");
		step3.get("mode").set("FULL_XA");
		ModelNode step4 = WildflyCLIUtil.createOpNode("subsystem=infinispan/cache-container=ebeguCache/local-cache=ebeguAuthorizationCache/eviction=EVICTION", "add");
		step4.get("strategy").set("LRU");
		step4.get("max-entries").set("100");

        ModelNode op = WildflyCLIUtil.createCompositeNode(step1, step2, step3, step4);

        // add infinispan cache subsystem
        boolean success = WildflyCLIUtil.execute(managementClient, op);
        LOG.info("Installing infinispan cache for ebegu application {}",
                new Object[] { success ? "passed" : "failed" });

//        // FIXME reload is not working due to https://bugzilla.redhat.com/show_bug.cgi?id=900065
//         managementClient.getControllerClient().execute(ModelUtil.createOpNode(null, "reload"));
    }

    @Override
    public void tearDown(ManagementClient managementClient, String containerId){

        if (!containerId.startsWith(JBOSSAS_CONTAINER)) {
            return;
        }

        LOG.info( "starting removal of  Infinispan cache for ebegu testing");

        ModelNode op = WildflyCLIUtil.createOpNode("subsystem=infinispan/cache-container=ebeguCache", "remove");

        // remove picketlink subsystem
        boolean success = WildflyCLIUtil.execute(managementClient, op);
		LOG.info("Uninstalling infinispan cache for ebegu application {}",
                new Object[] { success ? "passed" : "failed" });

        // FIXME reload is not working due to https://bugzilla.redhat.com/show_bug.cgi?id=900065
        // managementClient.getControllerClient().execute(ModelUtil.createOpNode(null, "reload"));
    }
}
