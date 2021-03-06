package com.cloudera.labs.envelope.plan;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.avro.generic.GenericRecord;

import com.cloudera.labs.envelope.RecordModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A planner implementation for appending the stream to the storage table. Only plans insert mutations.
 */
public class AppendPlanner extends Planner {
    
    public AppendPlanner(Properties props) {
        super(props);
    }
    
    @Override
    public List<PlannedRecord> planMutations(List<GenericRecord> arrivingRecords,
            List<GenericRecord> existingRecords, RecordModel recordModel)
    {
        boolean setKeyToUUID = Boolean.parseBoolean(props.getProperty("uuid.key.enabled", "false"));
        
        List<PlannedRecord> planned = Lists.newArrayList();
        
        for (GenericRecord arriving : arrivingRecords) {
            if (setKeyToUUID) {
                if (!recordModel.hasKeyFields()) {
                    throw new RuntimeException("Key columns must be specified to provide UUID keys.");
                }
                
                arriving.put(recordModel.getKeyFieldNames().get(0), UUID.randomUUID().toString());
            }
            
            if (recordModel.hasLastUpdatedField()) {
                arriving.put(recordModel.getLastUpdatedFieldName(), currentTimestampString());
            }
            
            planned.add(new PlannedRecord(arriving, MutationType.INSERT));
        }
        
        return planned;
    }

    @Override
    public boolean requiresExistingRecords() {
        return false;
    }

    @Override
    public boolean requiresKeyColocation() {
        return false;
    }

    @Override
    public Set<MutationType> getEmittedMutationTypes() {
        return Sets.newHashSet(MutationType.INSERT);
    }
    
}
