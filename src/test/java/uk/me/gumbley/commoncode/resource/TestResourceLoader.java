package uk.me.gumbley.commoncode.resource;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import uk.me.gumbley.commoncode.CCTestCase;


/**
 * Test the resource loader
 * @author matt
 *
 */
public final class TestResourceLoader extends CCTestCase {
    private static final Logger LOGGER = Logger
            .getLogger(TestResourceLoader.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
    
    /**
     * 
     */
    @Test
    public void canGetResource() {
        final StringBuilder sb = new StringBuilder();
        ResourceLoader.readResource(sb, "resourceloader.txt");
        Assert.assertEquals("this is a test", sb.toString());
    }
    
    /**
     * 
     */
    @Test
    public void resourceExists() {
        Assert.assertTrue(ResourceLoader.resourceExists("resourceloader.txt"));
        Assert.assertFalse(ResourceLoader.resourceExists("lordlucan.txt"));
    }
}
