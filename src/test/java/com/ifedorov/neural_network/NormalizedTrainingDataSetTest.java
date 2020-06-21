package com.ifedorov.neural_network;

import com.google.common.collect.Lists;
import com.ifedorov.neural_network.dataset.NormalizedTrainingDataSet;
import com.ifedorov.neural_network.dataset.TrainingDataSet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NormalizedTrainingDataSetTest {

    @Test
    void testNormalize() {
        ArrayList<TrainingDataSet> dataSets = Lists.newArrayList(
                new TrainingDataSet(Lists.newArrayList(new BigDecimalWrapper(20.0), new BigDecimalWrapper(20.0)), Lists.newArrayList(BigDecimalWrapper.ZERO)),
                new TrainingDataSet(Lists.newArrayList(new BigDecimalWrapper(40.0), new BigDecimalWrapper(80.0)), Lists.newArrayList(BigDecimalWrapper.ZERO)),
                new TrainingDataSet(Lists.newArrayList(new BigDecimalWrapper(60.0), new BigDecimalWrapper(40.0)), Lists.newArrayList(BigDecimalWrapper.ZERO))
        );
        List<NormalizedTrainingDataSet> normalized = NormalizedTrainingDataSet.asNormalized(dataSets);
        assertEquals(new BigDecimalWrapper(0), normalized.get(0).getInputValues().get(0));
        assertEquals(new BigDecimalWrapper(0.5), normalized.get(1).getInputValues().get(0));
        assertEquals(new BigDecimalWrapper(1), normalized.get(2).getInputValues().get(0));
        assertEquals(new BigDecimalWrapper(0), normalized.get(0).getInputValues().get(1));
        assertEquals(new BigDecimalWrapper(1), normalized.get(1).getInputValues().get(1));
        assertEquals(new BigDecimalWrapper(20).divide(new BigDecimalWrapper(60)), normalized.get(2).getInputValues().get(1));
    }
}
