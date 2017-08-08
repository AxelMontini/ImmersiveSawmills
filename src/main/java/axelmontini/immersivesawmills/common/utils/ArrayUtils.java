package axelmontini.immersivesawmills.common.utils;

public class ArrayUtils {
    /**@return the maximum length of the layers, in order, of this 3d array {firstMax, secondMax, thirdMax}*/
    public static <T> int[] getMaxLength(T[][][] t) {
        int firstMax=t.length, secondMax=0, thirdMax=0;
        for (T[][] t2: t) {  //Assign secondMax's value
            if (secondMax < t2.length)
                secondMax = t2.length;
            for(T[] t3 : t2) {  //Assign thirdMax's value
                if (thirdMax < t3.length)
                    thirdMax = t3.length;
            }
        }

        return new int[] {firstMax, secondMax, thirdMax};
    }
}
