package nuxeo.jenkisx.test;

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * Empty Unit Testing class.
 * <p/>
 *
 * @see <a href="https://doc.nuxeo.com/corg/unit-testing/">Unit Testing</a>
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("nuxeo.jenkisx.test.jenkinsx-quickstart-nuxeo-poc-core")
public class TestInitRepository {

    @Inject
    protected CoreSession session;

    @Test
    public void createSomeDocsTest() {
        DocumentModel doc = session.createDocumentModel("/", "folder", "Folder");
        doc.setProperty("dublincore", "title", "folder");
        doc = session.createDocument(doc);
        doc = session.saveDocument(doc);

        assertTrue(session.exists(new PathRef("/folder")));

    }
}
