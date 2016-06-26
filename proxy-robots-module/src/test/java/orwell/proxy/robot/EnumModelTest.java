package orwell.proxy.robot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Michaël Ludmann on 26/06/16.
 */
public class EnumModelTest {
    @Test
    public void getModelFromStringEv3LowerCase() throws Exception {
        assertEquals(EnumModel.EV3, EnumModel.getModelFromString("ev3"));
    }

    @Test
    public void getModelFromStringEv3UpperCase() throws Exception {
        assertEquals(EnumModel.EV3, EnumModel.getModelFromString("EV3"));
    }

    @Test
    public void getModelFromStringNxt() throws Exception {
        assertEquals(EnumModel.NXT, EnumModel.getModelFromString("nxt"));
    }

    @Test
    public void getModelFromRandomString() throws Exception {
        assertEquals(EnumModel.NXT, EnumModel.getModelFromString("jambon"));
    }

    @Test
    public void getModelFromNull() throws Exception {
        assertEquals(EnumModel.NXT, EnumModel.getModelFromString(null));
    }
}