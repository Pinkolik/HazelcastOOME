package com.github.pinkolik.hazelcast.policy;

import com.hazelcast.core.EntryView;
import com.hazelcast.map.eviction.MapEvictionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: rsv
 * Date: 08.10.2019
 * Time: 13:44
 */
public class IncomeDocumentsEvictionPolicy extends MapEvictionPolicy<String, byte[]> {

    private static final Logger LOG = LoggerFactory.getLogger(IncomeDocumentsEvictionPolicy.class);

    @Override
    public int compare(final EntryView<String, byte[]> entryView1, final EntryView<String, byte[]> entryView2) {
        LOG.info("Eviction policy executed");
        return Long.compare(entryView1.getCreationTime(), entryView2.getCreationTime());
    }
}
