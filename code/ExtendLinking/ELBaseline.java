package ExtendLinking;
import Baseline.*;
import Linking.LinkingViaEMNoContext;
import Linking.LinkingAddingContextEMLearnItera;

public class ELBaseline {
    public int max_n;

    public ELBaseline(int max_num) throws Exception{
        max_n = max_num;
        LinkingViaExtendContent LVEC = new LinkingViaExtendContent(max_n);
        LVEC.get_test_data_ready();
    }

    public void CosSimMethod() throws Exception{
        CosineSimilarity cosineSimilarity=new CosineSimilarity();
        cosineSimilarity.readVectorForWebpage();
        cosineSimilarity.readVectorForCandidate();
        cosineSimilarity.calculDocumentFreq();
        cosineSimilarity.readW2V();  // 要用w2v才使用 记得下个方法内部也要改
        cosineSimilarity.linkingViaCosine(); //tf 与tfidf 要修改内部方法 或者w2v
    }

    public void PopMethod() throws Exception{
        PopularityBasedMethod popularityBasedMethod=new PopularityBasedMethod();
        popularityBasedMethod.liningViaPopularity();
    }

    public void SHINE() throws Exception{
        LinkingViaEMNoContext linkingViaEM=new LinkingViaEMNoContext();
        linkingViaEM.for_ex_linking();
        linkingViaEM.calculAccu();
    }

    public void SHINEplus() throws Exception{
        LinkingAddingContextEMLearnItera linkingAddingContextEMLearnItera = new LinkingAddingContextEMLearnItera();
        linkingAddingContextEMLearnItera.for_ex_linking();
    }

    public static void main(String[] args) throws Exception{
        ELBaseline ELBL = new ELBaseline(5000);
//        ELBL.CosSimMethod();
//        ELBL.PopMethod();
        ELBL.SHINE();
//        ELBL.SHINEplus();
    }

}

