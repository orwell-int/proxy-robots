package orwell.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by Michaël Ludmann on 6/2/15.
 */
@RunWith(JUnit4.class)
public class UtilsTest {
    private final static byte SEPARATOR = 0x77;

    @Test
    public void testSplit_bSEPb_limit3() throws Exception {
        final byte[] bytes = new byte[]{0x01, SEPARATOR, 0x02};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 3);
        assertEquals(2, list.size());
        assertArrayEquals(new byte[]{0x01}, list.get(0));
        assertArrayEquals(new byte[]{0x02}, list.get(1));
    }

    @Test
    public void testSplit_bSEPb_limit2() throws Exception {
        final byte[] bytes = new byte[]{0x01, SEPARATOR, 0x02};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 2);
        assertEquals(2, list.size());
        assertArrayEquals(new byte[]{0x01}, list.get(0));
        assertArrayEquals(new byte[]{0x02}, list.get(1));
    }

    @Test
    public void testSplit_bSEPb_limit1() throws Exception {
        final byte[] bytes = new byte[]{0x01, SEPARATOR, 0x02};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 1);
        assertEquals(1, list.size());
        assertArrayEquals(bytes, list.get(0));
    }

    @Test
    public void testSplit_bSEPb_limit0() throws Exception {
        final byte[] bytes = new byte[]{0x01, SEPARATOR, 0x02};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 0);
        assertEquals(0, list.size());
    }

    @Test
    public void testSplit_SEPb_limit2() throws Exception {
        final byte[] bytes = new byte[]{SEPARATOR, 0x02};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 2);
        assertEquals(2, list.size());
        assertArrayEquals(new byte[]{}, list.get(0));
        assertArrayEquals(new byte[]{0x02}, list.get(1));
    }

    @Test
    public void testSplit_bSEP_limit2() throws Exception {
        final byte[] bytes = new byte[]{0x01, SEPARATOR};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 2);
        assertEquals(2, list.size());
        assertArrayEquals(new byte[]{0x01}, list.get(0));
        assertArrayEquals(new byte[]{}, list.get(1));
    }

    @Test
    public void testSplit_SEP_limit2() throws Exception {
        final byte[] bytes = new byte[]{SEPARATOR};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 2);
        assertEquals(2, list.size());
        assertArrayEquals(new byte[]{}, list.get(0));
        assertArrayEquals(new byte[]{}, list.get(1));
    }

    @Test
    public void testSplit_bSEPSEP_limit3() throws Exception {
        final byte[] bytes = new byte[]{0x01, SEPARATOR, SEPARATOR};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 3);
        assertEquals(3, list.size());
        assertArrayEquals(new byte[]{0x01}, list.get(0));
        assertArrayEquals(new byte[]{}, list.get(1));
        assertArrayEquals(new byte[]{}, list.get(2));
    }

    @Test
    public void testSplit_SEPbSEP_limit3() throws Exception {
        final byte[] bytes = new byte[]{SEPARATOR, 0x01, SEPARATOR};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 3);
        assertEquals(3, list.size());
        assertArrayEquals(new byte[]{}, list.get(0));
        assertArrayEquals(new byte[]{0x01}, list.get(1));
        assertArrayEquals(new byte[]{}, list.get(2));
    }

    @Test
    public void testSplit_SEPSEP_limit3() throws Exception {
        final byte[] bytes = new byte[]{SEPARATOR, SEPARATOR};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 3);
        assertEquals(3, list.size());
        assertArrayEquals(new byte[]{}, list.get(0));
        assertArrayEquals(new byte[]{}, list.get(1));
        assertArrayEquals(new byte[]{}, list.get(2));
    }

    @Test
    public void testSplit_bSEPSEPb_limit3() throws Exception {
        final byte[] bytes = new byte[]{0x01, SEPARATOR, SEPARATOR, 0x02};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 3);
        assertEquals(3, list.size());
        assertArrayEquals(new byte[]{0x01}, list.get(0));
        assertArrayEquals(new byte[]{}, list.get(1));
        assertArrayEquals(new byte[]{0x02}, list.get(2));
    }

    @Test
    public void testSplit_bb_limit2() throws Exception {
        final byte[] bytes = new byte[]{0x01, 0x02};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 2);
        assertEquals(1, list.size());
        assertArrayEquals(bytes, list.get(0));
    }

    @Test
    public void testSplit_bbSEPbbbSEPbb_limit3() throws Exception {
        final byte[] bytes = new byte[]{0x01, 0x02, SEPARATOR,
                0x03, 0x04, 0x05, SEPARATOR,
                0x06, 0x07};
        final List<byte[]> list = Utils.split(SEPARATOR, bytes, 3);
        assertEquals(3, list.size());
        assertArrayEquals(new byte[]{0x01, 0x02}, list.get(0));
        assertArrayEquals(new byte[]{0x03, 0x04, 0x05}, list.get(1));
        assertArrayEquals(new byte[]{0x06, 0x07}, list.get(2));
    }
}
