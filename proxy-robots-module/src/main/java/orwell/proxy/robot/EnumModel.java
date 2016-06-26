package orwell.proxy.robot;

/**
 * Created by Michaël Ludmann on 26/06/16.
 */
public enum EnumModel {
    EV3,
    NXT;

    public static EnumModel getModelFromString(String model) {
        if (model == null)
        {
            return EnumModel.NXT;
        }
        switch (model.toLowerCase()){
            case "ev3":
                return EV3;
            default:
                return NXT;
        }
    }
}
