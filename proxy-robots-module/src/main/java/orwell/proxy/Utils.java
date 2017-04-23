package orwell.proxy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class Utils {

    public static byte[] Concatenate(final byte[] a, final byte[] bytes) {
        final byte[] result = new byte[a.length + bytes.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(bytes, 0, result, a.length, bytes.length);
        return result;
    }

    /**
     * Split a byte array around a byte separator, within a given limit
     *
     * @param separator byte separator
     * @param input     bytes array to split
     * @param limit     max number of elements allowed in the returned list
     *                  If limit is < 0, then proceed as if there were no limit
     * @return list of bytes arrays, its size being of maximum {limit} elements
     *         if limit = 0: returns empty list
     *         if limit = 1: returns the first element result of the split (i.e.
     *         a list of one byte[] stopping at the first separator)
     */
    public static List<byte[]> split(final byte separator, final byte[] input, final int limit) {
        final List<byte[]> list = new LinkedList<>();
        if (0 == limit) {
            return list;
        }

        int blockStart = 0;
        int blockEnd = 0;
        final boolean limited = 0 < limit;
        while (blockEnd < input.length && (!limited || limit > list.size() + 1)) {
            if (separator == input[blockEnd]) {
                list.add(Arrays.copyOfRange(input, blockStart, blockEnd));
                blockStart = blockEnd + 1;
            }
            blockEnd++;
        }
        list.add(Arrays.copyOfRange(input, blockStart, input.length));
        return list;
    }
}
