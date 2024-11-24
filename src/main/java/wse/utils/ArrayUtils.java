package wse.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

/**
 * 
 * @author CarlCaesar
 *
 */

public final class ArrayUtils {
	private ArrayUtils() {
	}

	private static Random random = new Random();

	// ///////////////////////////////////////////////////
	// Misc
	// ///////////////////////////////////////////////////

	public static <T> int indexOf(T[] source, T element) {
		for (int i = 0; i < source.length; i++)
			if (source[i] == element)
				return i;
		return -1;
	}

	public static <E, T> HashMap<T, E> invert(HashMap<E, T> map) {
		HashMap<T, E> result = new HashMap<T, E>();

		Set<E> keys = map.keySet();

		for (E key : keys) {
			T value = map.get(key);
			result.put(value, key);
		}

		return result;
	}

	public static <T> boolean containsCopies(Collection<T> e) {
		Set<T> set = new HashSet<T>(e);
		return (set.size() < e.size());
	}

	public static <T> boolean containsMultiple(T value, @SuppressWarnings("unchecked") T... array) {
		int copies = 0;
		for (T t : array)
			if (t == value)
				if (copies == 1)
					return true;
				else
					copies = 1;
		return false;
	}

	public static <T extends Comparable<? super T>> boolean removeEquals(ArrayList<T> array) {
		for (int i = 0; i < array.size(); i++) {
			for (int j = 0; j < array.size(); j++) {
				if (i == j)
					continue;
				if (array.get(i).compareTo(array.get(j)) == 0) {
					array.remove(array.get(j));
					j--;
				}
			}
		}
		return false;
	}

	public static <T> boolean containsCopies(T[] e) {
		return containsCopies(Arrays.asList(e));
	}

	public static <E, T extends Comparable<T>> ArrayList<E> sort(HashMap<E, T> map) {

		ArrayList<Comp<E, T>> sorted = new ArrayList<Comp<E, T>>();
		ArrayList<E> result = new ArrayList<E>();

		for (E key : map.keySet()) {
			T val = map.get(key);
			sorted.add(new Comp<E, T>(key, val));
		}

		Collections.sort(sorted);

		for (Comp<E, T> co : sorted) {
			result.add(co.key);
		}
		sorted.clear();

		return result;
	}

	private static class Comp<E, T extends Comparable<T>> implements Comparable<Comp<E, T>> {

		private E key;
		private T value;

		public Comp(E key, T value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public int compareTo(Comp<E, T> other) {

			return value.compareTo(other.value);
		}
	}

	@SafeVarargs
	public static <T> boolean containsNull(T... objects) {
		for (T t : objects) {
			if (t == null)
				return true;
		}

		return false;

	}

	// ///////////////////////////////////////////////////
	// invoke
	// ///////////////////////////////////////////////////

	public static Object[] invoke(Method m, Object performBy, Object[] data) {
		if (m == null)
			return null;
		Object[] returnedValues = new Object[data.length];
		for (int i = 0; i < data.length; i++) {
			Object obj = data[i];
			try {
				returnedValues[i] = m.invoke(performBy, obj);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return returnedValues;
	}

	/**
	 * Returns an array of type Class containing the class types of each Object
	 * 
	 * @param objects
	 * @return
	 */
	public static Class<?>[] getClassTypes(Object... objects) {
		Class<?>[] array = new Class<?>[objects.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = objects[i].getClass();
		}
		return array;
	}

	// ///////////////////////////////////////////////////
	// Mix
	// ///////////////////////////////////////////////////

	public static void mix(int[] data) {
		int index1;
		int index2;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			int value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	public static void mix(double[] data) {
		int index1;
		int index2;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			double value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	public static void mix(float[] data) {
		int index1;
		int index2;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			float value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	public static void mix(long[] data) {
		int index1;
		int index2;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			long value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	public static void mix(boolean[] data) {
		int index1;
		int index2;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			boolean value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	public static void mix(char[] data) {
		int index1;
		int index2;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			char value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	public static void mix(byte[] data) {
		int index1;
		int index2;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			byte value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	public static <T> void mix(T[] data) {
		int index1;
		int index2;
		T value1;
		for (int i = 0; i < data.length * 2; i++) {
			// Random positions
			index1 = random.nextInt(data.length);
			index2 = random.nextInt(data.length);
			if (index1 == index2) {
				continue;
			}
			// Switch
			value1 = data[index1];
			data[index1] = data[index2];
			data[index2] = value1;
		}
	}

	// ///////////////////////////////////////////////////
	// Array of
	// ///////////////////////////////////////////////////

	public static byte[] arrayOf(byte value, int size) {
		byte[] result = new byte[size];
		Arrays.fill(result, value);
		return result;
	}

	// ///////////////////////////////////////////////////
	// Split
	// ///////////////////////////////////////////////////

	public static byte[][] split(byte[] array, byte split) {
		return split(array, split, -1);
	}

	public static byte[][] split(byte[] array, byte split, int times) {
		return split(array, split, times, true);
	}

	public static byte[][] split(byte[] source, byte splitByte, int times, boolean skipEmpty) {

		if (times == 0)
			return new byte[0][0];
		if (times == 1)
			return new byte[][] { source };
		times--;

		List<byte[]> result = new LinkedList<>();

		int start = 0;
		int len, c;
		for (c = 0; c < source.length; c++) {
			if (source[c] != splitByte)
				continue;
			len = c - start;
			if (len > 0 || !skipEmpty) {
				result.add(Arrays.copyOfRange(source, start, c));
				if (times > 0 && times == result.size()) {
					start = c + 1;
					break;
				}
			}
			start = c + 1;

		}
		if (result.size() == 0) {
			return new byte[][] { source };
		}

		if (source.length - start > 0 || !skipEmpty)
			result.add(Arrays.copyOfRange(source, start, source.length));
		return result.toArray(new byte[result.size()][]);
	}

	public static byte[][] split(byte[] source, byte[] regex) {
		return split(source, regex, -1);
	}

	public static byte[][] split(byte[] source, byte[] regex, int times) {
		return split(source, regex, times, false);
	}

	public static byte[][] split(byte[] source, byte[] regex, int times, boolean skipEmpty) {
		if (times == 0)
			return new byte[0][0];
		if (times == 1)
			return new byte[][] { source };
		times--;

		List<byte[]> result = new LinkedList<>();
		int start = 0;
		int len, c, i;

		outer: for (c = 0; c < source.length - regex.length + 1; c++) {
			for (i = 0; i < regex.length; i++)
				if (source[c + i] != regex[i])
					continue outer;

			len = c - start;
			if (len > 0 || !skipEmpty) {
				result.add(Arrays.copyOfRange(source, start, c));
				if (times > 0 && times == result.size()) {
					start = c + regex.length;
					break;
				}
			}
			start = c + regex.length;
			c += regex.length - 1;
		}

		if (result.size() == 0) {
			return new byte[][] { source };
		}

		if (source.length - start > 0 || !skipEmpty)
			result.add(Arrays.copyOfRange(source, start, source.length));

		return result.toArray(new byte[result.size()][]);
	}

	// ///////////////////////////////////////////////////
	// Put
	// ///////////////////////////////////////////////////

	public static int[] put(int[] data, int index, int... value) {
		int[] part1 = Arrays.copyOfRange(data, 0, index);
		int[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static double[] put(double[] data, int index, double... value) {
		double[] part1 = Arrays.copyOfRange(data, 0, index);
		double[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static float[] put(float[] data, int index, float... value) {
		float[] part1 = Arrays.copyOfRange(data, 0, index);
		float[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static long[] put(long[] data, int index, long... value) {
		long[] part1 = Arrays.copyOfRange(data, 0, index);
		long[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static boolean[] put(boolean[] data, int index, boolean... value) {
		boolean[] part1 = Arrays.copyOfRange(data, 0, index);
		boolean[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static char[] put(char[] data, int index, char... value) {
		char[] part1 = Arrays.copyOfRange(data, 0, index);
		char[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static byte[] put(byte[] data, int index, byte... value) {
		byte[] part1 = Arrays.copyOfRange(data, 0, index);
		byte[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static String[] put(String[] data, int index, String... value) {
		String[] part1 = Arrays.copyOfRange(data, 0, index);
		String[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static Object[] put(Object[] data, int index, Object... value) {
		Object[] part1 = Arrays.copyOfRange(data, 0, index);
		Object[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	public static Class<?>[] put(Class<?>[] data, int index, Class<?>... value) {
		Class<?>[] part1 = Arrays.copyOfRange(data, 0, index);
		Class<?>[] part2 = Arrays.copyOfRange(data, index, data.length);
		return combine(part1, value, part2);
	}

	// ///////////////////////////////////////////////////
	// Flip
	// ///////////////////////////////////////////////////

	public static int[] flip(int[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			int p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static double[] flip(double[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			double p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static float[] flip(float[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			float p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static long[] flip(long[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			long p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static boolean[] flip(boolean[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			boolean p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static char[] flip(char[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			char p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static byte[] flip(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			byte p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static String[] flip(String[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			String p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static Object[] flip(Object[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			Object p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	public static Class<?>[] flip(Class<?>[] data) {
		for (int i = 0; i < data.length; i++) {
			int oposite = data.length - 1 - i;
			if (oposite == i || i > oposite)
				return data;
			Class<?> p = data[oposite];
			data[oposite] = data[i];
			data[i] = p;
		}
		return data;
	}

	// ///////////////////////////////////////////////////
	// Convert
	// ///////////////////////////////////////////////////

	public static int[] convert(Integer[] data) {
		int[] result = new int[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static double[] convert(Double[] data) {
		double[] result = new double[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static float[] convert(Float[] data) {
		float[] result = new float[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static long[] convert(Long[] data) {
		long[] result = new long[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static boolean[] convert(Boolean[] data) {
		boolean[] result = new boolean[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static char[] convert(Character[] data) {
		char[] result = new char[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static byte[] convert(Byte[] data) {
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	// ///////////////////////////////////////////////////
	// To Array
	// ///////////////////////////////////////////////////

	public static int[] array(int... data) {
		return data;
	}

	public static double[] array(double... data) {
		return data;
	}

	public static float[] array(float... data) {
		return data;
	}

	public static long[] array(long... data) {
		return data;
	}

	public static boolean[] array(boolean... data) {
		return data;
	}

	public static char[] array(char... data) {
		return data;
	}

	public static byte[] array(byte... data) {
		return data;
	}

	public static String[] array(String... data) {
		return data;
	}

	public static Object[] array(Object... data) {
		return data;
	}

	public static Class<?>[] array(Class<?>... data) {
		return data;
	}

	// ///////////////////////////////////////////////////
	// Make Array
	// ///////////////////////////////////////////////////

	public static int[] makeArray(int value, int length) {
		int[] array = new int[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static double[] makeArray(double value, int length) {
		double[] array = new double[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static float[] makeArray(float value, int length) {
		float[] array = new float[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static long[] makeArray(long value, int length) {
		long[] array = new long[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static boolean[] makeArray(boolean value, int length) {
		boolean[] array = new boolean[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static char[] makeArray(char value, int length) {
		char[] array = new char[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static byte[] makeArray(byte value, int length) {
		byte[] array = new byte[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static String[] makeArray(String value, int length) {
		String[] array = new String[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static Object[] makeArray(Object value, int length) {
		Object[] array = new Object[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	public static Class<?>[] makeArray(Class<?> value, int length) {
		Class<?>[] array = new Class<?>[length];
		for (int i = 0; i < length; i++) {
			array[i] = value;
		}

		return array;
	}

	// ///////////////////////////////////////////////////
	// Convert back
	// ///////////////////////////////////////////////////

	public static Integer[] convert(int[] data) {
		Integer[] result = new Integer[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static char[] convertToChar(int[] data) {
		char[] result = new char[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = (char) data[i];
		return result;
	}

	public static int[] convertToInt(char[] data) {
		int[] result = new int[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = (int) data[i];
		return result;
	}

	public static Double[] convert(double[] data) {
		Double[] result = new Double[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static Float[] convert(float[] data) {
		Float[] result = new Float[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static Long[] convert(long[] data) {
		Long[] result = new Long[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static Boolean[] convert(boolean[] data) {
		Boolean[] result = new Boolean[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static Character[] convert(char[] data) {
		Character[] result = new Character[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static Byte[] convert(byte[] data) {
		Byte[] result = new Byte[data.length];
		for (int i = 0; i < data.length; i++)
			result[i] = data[i];
		return result;
	}

	public static Object[] convert(Class<? extends Object>[] data, Object cast) {
		if (cast == null)
			return null;
		Object[] result = new Object[data.length];
		for (int i = 0; i < data.length; i++) {
			if (data[i].isAssignableFrom(cast.getClass()))
				result[i] = data[i].cast(cast);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked" })
	public <T, R extends T> void convert(T[] array, R[] to) {
		if (array == null || to == null || array.length == 0 || to.length == 0 || to.length != array.length)
			return;

		for (int i = 0; i < array.length; i++) {
			T obj = array[i];
			if (obj.getClass().isAssignableFrom(to.getClass())) {
				to[i] = (R) obj;
			}
		}
		return;
	}

	// ///////////////////////////////////////////////////
	// Contains
	// ///////////////////////////////////////////////////

	public static <E> boolean contains(E[] array, E i) {
		for (E l : array)
			if (i == l)
				return true;
		return false;
	}

	public static boolean contains(int[] data, int v) {
		for (int i : data)
			if (v == i)
				return true;
		return false;
	}

	public static boolean contains(double[] data, double v) {
		for (double i : data)
			if (v == i)
				return true;
		return false;
	}

	public static boolean contains(float[] data, float v) {
		for (float i : data)
			if (v == i)
				return true;
		return false;
	}

	public static boolean contains(long[] data, long v) {
		for (long i : data)
			if (v == i)
				return true;
		return false;
	}

	public static boolean contains(boolean[] data, boolean v) {
		for (boolean i : data)
			if (v == i)
				return true;
		return false;
	}

	public static boolean contains(char[] data, char v) {
		for (char i : data)
			if (v == i)
				return true;
		return false;
	}

	public static boolean contains(byte[] data, byte v) {
		for (byte i : data)
			if (v == i)
				return true;
		return false;
	}

	// ///////////////////////////////////////////////////
	// Sub
	// ///////////////////////////////////////////////////

	private static int[] _sub(int arrayLength, int start) {
		if (start >= 0) {
			return new int[] { start, arrayLength };
		} else {
			return new int[] { arrayLength - start, arrayLength };
		}
	}

	private static int[] _sub(int arrayLength, int start, int length) {
		if (start >= 0) {
			return new int[] { start, start + length };
		} else {
			return new int[] { arrayLength - start, arrayLength - start + length };
		}
	}

	public static int[] sub(int[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static double[] sub(double[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static float[] sub(float[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static long[] sub(long[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static boolean[] sub(boolean[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static char[] sub(char[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static byte[] sub(byte[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static <T> T[] sub(T[] data, int start, int length) {
		int[] s = _sub(data.length, start, length);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	/* No length */

	public static int[] sub(int[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static double[] sub(double[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static float[] sub(float[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static long[] sub(long[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static boolean[] sub(boolean[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static char[] sub(char[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static byte[] sub(byte[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	public static <T> T[] sub(T[] data, int start) {
		int[] s = _sub(data.length, start);
		return Arrays.copyOfRange(data, s[0], s[1]);
	}

	// ///////////////////////////////////////////////////
	// Fill from -> to
	// ///////////////////////////////////////////////////

	public static int[] from_toInt(int from, int to) {
		int size = (to - from);
		if (size <= 0)
			return null;
		size++;
		int[] result = new int[size];
		for (int i = 0; i < size; i++)
			result[i] = i + from;
		return result;
	}

	public static float[] from_toFloat(int from, int to) {
		int size = (to - from);
		if (size <= 0)
			return null;
		size++;
		float[] result = new float[size];
		for (int i = 0; i < size; i++)
			result[i] = i + from;
		return result;
	}

	public static char[] from_toChar(int from, int to) {
		int size = (to - from);
		if (size <= 0)
			return null;
		size++;
		char[] result = new char[size];
		for (int i = 0; i < size; i++)
			result[i] = (char) (i + from);
		return result;
	}

	public static double[] from_toDouble(int from, int to) {
		int size = (to - from);
		if (size <= 0)
			return null;
		size++;
		double[] result = new double[size];
		for (int i = 0; i < size; i++)
			result[i] = (double) (i + from);
		return result;
	}

	// ///////////////////////////////////////////////////
	// Replace
	// ///////////////////////////////////////////////////

	public static int[] replace(int[] data, int from, int to) {
		for (int i = 0; i < data.length; i++)
			if (data[i] == from)
				data[i] = to;
		return data;
	}

	public static float[] replace(float[] data, float from, float to) {
		for (int i = 0; i < data.length; i++)
			if (data[i] == from)
				data[i] = to;
		return data;
	}

	public static double[] replace(double[] data, double from, double to) {
		for (int i = 0; i < data.length; i++)
			if (data[i] == from)
				data[i] = to;
		return data;
	}

	public static long[] replace(long[] data, long from, long to) {
		for (int i = 0; i < data.length; i++)
			if (data[i] == from)
				data[i] = to;
		return data;
	}

	public static char[] replace(char[] data, char from, char to) {
		for (int i = 0; i < data.length; i++)
			if (data[i] == from)
				data[i] = to;
		return data;
	}

	public static byte[] replace(byte[] data, byte from, byte to) {
		for (int i = 0; i < data.length; i++)
			if (data[i] == from)
				data[i] = to;
		return data;
	}

	// ///////////////////////////////////////////////////
	// Combine
	// ///////////////////////////////////////////////////
	public static int[] combine(int[]... datas) {
		int size = 0;
		for (int[] d : datas)
			size += d.length;
		int[] result = new int[size];

		int n = 0;
		for (int[] data : datas)
			for (int i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static double[] combine(double[]... datas) {
		int size = 0;
		for (double[] d : datas)
			size += d.length;
		double[] result = new double[size];

		int n = 0;
		for (double[] data : datas)
			for (double i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static float[] combine(float[]... datas) {
		int size = 0;
		for (float[] d : datas)
			size += d.length;
		float[] result = new float[size];

		int n = 0;
		for (float[] data : datas)
			for (float i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static long[] combine(long[]... datas) {
		int size = 0;
		for (long[] d : datas)
			size += d.length;
		long[] result = new long[size];

		int n = 0;
		for (long[] data : datas)
			for (long i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static boolean[] combine(boolean[]... datas) {
		int size = 0;
		for (boolean[] d : datas)
			size += d.length;
		boolean[] result = new boolean[size];

		int n = 0;
		for (boolean[] data : datas)
			for (boolean i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static char[] combine(char[]... datas) {
		int size = 0;
		for (char[] d : datas)
			size += d.length;
		char[] result = new char[size];

		int n = 0;
		for (char[] data : datas)
			for (char i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static byte[] combine(byte[]... datas) {
		int size = 0;
		for (byte[] d : datas)
			size += d.length;
		byte[] result = new byte[size];

		int n = 0;
		for (byte[] data : datas)
			for (byte i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static String[] combine(String[]... datas) {
		int size = 0;
		for (String[] d : datas)
			size += d.length;
		String[] result = new String[size];

		int n = 0;
		for (String[] data : datas)
			for (String i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static Object[] combine(Object[]... datas) {
		int size = 0;
		for (Object[] d : datas)
			size += d.length;

		Object[] result = new Object[size];

		int n = 0;
		for (Object[] data : datas)
			for (Object i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	@SafeVarargs
	public static <T> void fill(T[] dest, T[]... datas) {
		int size = 0;
		for (T[] d : datas)
			size += d.length;

		if (dest.length != size)
			return;

		int n = 0;
		for (T[] data : datas) {
			for (T i : data) {
				dest[n++] = i;
			}
		}
	}

	public static Class<?>[] combine(Class<?>[]... datas) {
		int size = 0;
		for (Class<?>[] d : datas)
			size += d.length;
		Class<?>[] result = new Class<?>[size];

		int n = 0;
		for (Class<?>[] data : datas)
			for (Class<?> i : data) {
				result[n] = i;
				n++;
			}
		return result;
	}

	public static int[] combineS(int[] first, int... datas) {
		return combine(first, datas);
	}

	public static double[] combineS(double[] first, double... datas) {
		return combine(first, datas);
	}

	public static float[] combineS(float[] first, float... datas) {
		return combine(first, datas);
	}

	public static long[] combineS(long[] first, long... datas) {
		return combine(first, datas);
	}

	public static boolean[] combineS(boolean[] first, boolean... datas) {
		return combine(first, datas);
	}

	public static char[] combineS(char[] first, char... datas) {
		return combine(first, datas);
	}

	public static byte[] combineS(byte[] first, byte... datas) {
		return combine(first, datas);
	}

	public static String[] combineS(String[] first, String... datas) {
		return combine(first, datas);
	}

	public static Object[] combineS(Object[] first, Object... datas) {
		return combine(first, datas);
	}

	public static Class<?>[] combineS(Class<?>[] first, Class<?>... datas) {
		return combine(first, datas);
	}

	// ///////////////////////////////////////////////////
	// To String
	// ///////////////////////////////////////////////////

	public static String toString(int[] src) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + src[0];
		for (int i = 1; i < src.length; i++)
			result += "," + src[i];
		return result + "]";
	}

	public static String toString(double[] src) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + src[0];
		for (int i = 1; i < src.length; i++)
			result += "," + src[i];
		return result + "]";
	}

	public static String toString(float[] src) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + src[0];
		for (int i = 1; i < src.length; i++)
			result += "," + src[i];
		return result + "]";
	}

	public static String toString(long[] src) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + src[0];
		for (int i = 1; i < src.length; i++)
			result += "," + src[i];
		return result + "]";
	}

	public static String toString(boolean[] src) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + src[0];
		for (int i = 1; i < src.length; i++)
			result += "," + src[i];
		return result + "]";
	}

	public static String toString(char[] src, boolean asNumbers) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + (asNumbers ? (int) src[0] : src[0]);
		for (int i = 1; i < src.length; i++)
			result += "," + (asNumbers ? (int) src[i] : src[i]);
		return result + "]";
	}

	public static String toString(byte[] src) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + src[0];
		for (int i = 1; i < src.length; i++)
			result += "," + src[i];
		return result + "]";
	}

	public static String toString(String[] src) {
		return toString(src, true);
	}

	public static String toString(Object[] src) {
		if (src.length == 0) {
			return "[]";
		}
		String result = "[" + src[0].toString();
		for (int i = 1; i < src.length; i++)
			result += "," + src[i].toString();
		return result + "]";
	}

	public static String toString(String[] src, boolean array) {
		if (!array) {
			if (src.length == 0) {
				return "";
			}
			String result = "";
			for (String s : src)
				result += s;
			return result;
		} else {
			if (src.length == 0) {
				return "[]";
			}
			String result = "[" + src[0];
			for (int i = 1; i < src.length; i++)
				result += "," + src[i];
			return result + "]";
		}
	}

	public static String join(String[] src, String between) {
		if (src == null || src.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();

		builder.append(src[0]);
		for (int i = 1; i < src.length; i++) {
			builder.append(between);
			builder.append(src[i]);
		}
		return builder.toString();
	}

	public static String joinFilterEmpty(String between, String... src) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < src.length; i++) {
			if (src[i] == null || src[i].isEmpty())
				continue;
			if (builder.length() != 0)
				builder.append(between);
			builder.append(src[i]);
		}
		return builder.toString();
	}

	public static String join(Object[] src, String between) {
		return join(stringValues(src), between);
	}

	public static <F> String join(Collection<F> src, String between) {
		return join(src, between, new Transformer<F, String>() {
			@Override
			public String transform(F value) {
				return String.valueOf(value);
			}
		});
	}

	public static <F, T> String join(Collection<F> src, String between, Transformer<F, T> transformer) {
		return join(stringValues(src, transformer), between);
	}

	public static <F, T> String join(F[] src, String between, Transformer<F, T> transformer) {
		return join(stringValues(src, transformer), between);
	}

	public static <K, V> String join(final Map<K, V> map, final String betweenEntries, final String between) {
		return join(map.entrySet(), betweenEntries, new Transformer<Entry<K, V>, String>() {
			@Override
			public String transform(Entry<K, V> value) {
				return value.getKey() + between + value.getValue();
			}
		});
	}

	public static String join(int[] src, String between) {
		return join(stringValues(src), between);
	}

	public static String join(long[] src, String between) {
		return join(stringValues(src), between);
	}

	public static String join(double[] src, String between) {
		return join(stringValues(src), between);
	}

	public static String join(float[] src, String between) {
		return join(stringValues(src), between);
	}

	public static String join(boolean[] src, String between) {
		return join(stringValues(src), between);
	}

	public static String join(char[] src, String between) {
		return join(stringValues(src), between);
	}

	public static String join(byte[] src, String between) {
		return join(stringValues(src), between);
	}

	public static String[] stringValues(Object[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	public static <F, T> String[] stringValues(F[] obj, Transformer<F, T> transformer) {
		if (obj == null || obj.length == 0)
			return new String[0];
		String[] res = new String[obj.length];

		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(transformer.transform(obj[i]));
		return res;
	}

	public static <F, T> String[] stringValues(Collection<F> set, Transformer<F, T> transformer) {
		if (set == null || set.size() == 0)
			return new String[0];
		String[] res = new String[set.size()];

		int i = 0;
		for (F f : set) {
			res[i] = String.valueOf(transformer.transform(f));
			i++;
		}
		return res;
	}

	public static <F> String[] stringValues(Collection<F> set) {
		if (set == null || set.size() == 0)
			return new String[0];
		String[] res = new String[set.size()];

		int i = 0;
		for (F f : set) {
			res[i] = String.valueOf(f);
			i++;
		}
		return res;
	}

	public static String[] stringValues(int[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	public static String[] stringValues(long[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	public static String[] stringValues(double[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	public static String[] stringValues(float[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	public static String[] stringValues(boolean[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	public static String[] stringValues(char[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	public static String[] stringValues(byte[] obj) {
		if (obj == null)
			return new String[0];
		String[] res = new String[obj.length];
		for (int i = 0; i < obj.length; i++)
			res[i] = String.valueOf(obj[i]);
		return res;
	}

	// ///////////////////////////////////////////////////
	// Remove
	// ///////////////////////////////////////////////////

	public static int[] remove(int[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static double[] remove(double[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static float[] remove(float[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static long[] remove(long[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static boolean[] remove(boolean[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static char[] remove(char[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static byte[] remove(byte[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static String[] remove(String[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}

	public static Object[] remove(Object[] src, int index) {
		return combine(Arrays.copyOfRange(src, 0, index), Arrays.copyOfRange(src, index + 1, src.length));
	}
}
