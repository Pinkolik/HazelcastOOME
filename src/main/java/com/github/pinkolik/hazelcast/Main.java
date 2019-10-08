package com.github.pinkolik.hazelcast;

import com.github.pinkolik.hazelcast.policy.IncomeDocumentsEvictionPolicy;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: rsv
 * Date: 08.10.2019
 * Time: 13:44
 */
public final class Main {

    private static final String INCOME_DOCUMENTS_MAP = "income-documents-map";

    private static final String HAZELCAST_INSTANCE = "hazelcast-instance";

    private static HazelcastInstance hazelcastInstance;

    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    private static IMap<String, byte[]> incomeDocumentsMap;

    private Main() {
    }

    /*
     * For faster results run with following JVM options -Xms500m -Xmx500m -XX:+HeapDumpOnOutOfMemoryError
     * */
    public static void main(final String[] args) throws IOException {
        initHazelcastInstance();
        runOOMETest();
    }

    private static void initHazelcastInstance() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(getHazelcastConfig());
        incomeDocumentsMap = hazelcastInstance.getMap(INCOME_DOCUMENTS_MAP);
    }

    private static void runOOMETest() throws IOException {
        int size = 100;
        LOG.info("First, we fill map with small documents (Press enter to continue)");
        System.in.read();
        putDocs(511, size);
        LOG.info("Now, if we keep adding documents of the same size, everything is going to work as usual." +
                         "\n Used heap size remains the same so does map size (Press enter to continue)");
        System.in.read();
        putDocs(10, size);
        LOG.info("But if we start putting documents of larger size, number of entries will stay same," +
                         "\n but used heap size will increase until we get OutOfMemoryException (Press enter to continue)");
        System.in.read();
        while (true) {
            size += size;
            putDocs(50, size);
        }
    }

    private static Config getHazelcastConfig() {
        Config config = new Config().setInstanceName(HAZELCAST_INSTANCE);
        MapConfig incomeDocumentsMapConfig =
                new MapConfig().setName(INCOME_DOCUMENTS_MAP) //This is the config we use on our prod application
                               .setMaxIdleSeconds(0)
                               .setTimeToLiveSeconds(0)
                               .setMaxSizeConfig(new MaxSizeConfig(50, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE))
                               .setMapEvictionPolicy(new IncomeDocumentsEvictionPolicy())
                               .setBackupCount(0)
                               .setAsyncBackupCount(1)
                               .setReadBackupData(true);
        config.addMapConfig(incomeDocumentsMapConfig);
        return config;
    }

    private static void putDocs(final int count, final int docSize) {
        for (int j = 0; j < count; j++) {
            int size = 1024 * docSize;
            byte[] doc = new byte[size];
            for (int i = 0; i < size; i++) {
                doc[i] = (byte) (Math.random() * 255);
            }
            addMessageToMapById(UUID.randomUUID().toString(), doc);
        }
    }

    private static void addMessageToMapById(final String documentId, final byte[] documentBody) {
        LOG.info("Put document to map with id {} and size {}", documentId, documentBody.length);
        incomeDocumentsMap.set(documentId, documentBody);
        LOG.info("{} takes {} MBytes, has {} entries", INCOME_DOCUMENTS_MAP,
                 incomeDocumentsMap.getLocalMapStats().getHeapCost() / 1024.0 / 1024.0, incomeDocumentsMap.size());
        LOG.info("USED HEAP PERCENTAGE {}", (double) incomeDocumentsMap.getLocalMapStats().getHeapCost() * 100.0D /
                (double) Math.max(maxMemoryInBytes(), 1L));
        LOG.info(incomeDocumentsMap.containsKey(documentId) ? "Entry has been added" : "Entry hasn't been added :(");
        System.gc(); //Calling gc after each putting for purity of the experiment
    }

    private static long maxMemoryInBytes() {
        return Runtime.getRuntime().maxMemory();
    }
}
