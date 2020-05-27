package output;

import datastructure.ImpactData;

import java.util.List;

public interface IOutput {

    void saveImpactData(List<ImpactData> impactDataList);
    void saveImpactData(ImpactData impactData);
}
