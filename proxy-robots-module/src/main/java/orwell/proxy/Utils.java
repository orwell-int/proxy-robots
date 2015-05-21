package orwell.proxy;

public final class Utils {

    public static byte[] Concatenate(final byte[] a, final byte[] bytes) {
        final byte[] result = new byte[a.length + bytes.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(bytes, 0, result, a.length, bytes.length);
        return result;
    }

}
