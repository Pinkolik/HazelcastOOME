package com.github.pinkolik.hazelcast;

import com.github.pinkolik.hazelcast.policy.IncomeDocumentsEvictionPolicy;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Created by IntelliJ IDEA.
 * User: rsv
 * Date: 08.10.2019
 * Time: 13:44
 */
public final class Main {

    private static HazelcastInstance hazelcastInstance;

    private Main() {
    }

    public static void main(final String[] args) {

    }

    private static void initHazelcastInstance() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(getHazelcastConfig());
    }

    private static Config getHazelcastConfig() {
        Config config = new Config().setInstanceName("hazelcast-instance");
        MapConfig incomeDocumentsMapConfig =
                new MapConfig().setName("income-documents-map")
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
}
