package jp.ats.util.exif;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import jp.ats.util.exif.IFDUtilities.Bytes;
import jp.ats.util.exif.IFDUtilities.Endian;

/**
 * @author 千葉 哲嗣
 */
public class IFD {

	private static final int TYPE_BYTE = 1;

	private static final int TYPE_ASCII = 2;

	private static final int TYPE_SHORT = 3;

	private static final int TYPE_LONG = 4;

	private static final int TYPE_RATIONAL = 5;

	private static final int TYPE_UNDEFINED = 7;

	private static final int TYPE_SLONG = 9;

	private static final int TYPE_SRATIONAL = 10;

	private final boolean littleEndian;

	int tag;

	int type;

	int count;

	byte[][] values;

	IFD(boolean littleEndian) {
		this.littleEndian = littleEndian;
	}

	public boolean isLittleEndian() {
		return littleEndian;
	}

	public String getName() {
		return IFDUtilities.getName(tag);
	}

	public int getTag() {
		return tag;
	}

	public int getType() {
		return type;
	}

	public int getCount() {
		return count;
	}

	public byte[][] getValues() {
		return values;
	}

	public byte[] getValuesAsByteType() {
		int length = 0;
		for (int i = 0; i < values.length; i++) {
			length += values[i].length;
		}

		byte[] result = new byte[length];

		for (int i = 0; i < values.length; i++) {
			for (int ii = 0; ii < values[i].length; ii++) {
				result[i * ii] = values[i][ii];
			}
		}

		return result;
	}

	public String getValueAsASCIIType() {
		return new String(getValuesAsByteType()).trim();
	}

	public int[] getValuesAsShortType() {
		int[] result = new int[count];

		for (int i = 0; i < values.length; i++) {
			result[i] = new Endian(littleEndian, new Bytes(values[i])).readWord();
		}

		return result;
	}

	public long[] getValuesAsLongType() {
		long[] result = new long[count];

		for (int i = 0; i < values.length; i++) {
			result[i] = new Endian(littleEndian, new Bytes(values[i])).readDWord();
		}

		return result;
	}

	public Rational[] getValuesAsRationalType() {
		Rational[] result = new Rational[count];

		for (int i = 0; i < values.length; i++) {
			Endian endian = new Endian(littleEndian, new Bytes(values[i]));
			result[i] = new Rational(endian.readDWord(), endian.readDWord());
		}

		return result;
	}

	public byte[] getValuesAsUndefinedType() {
		return getValuesAsByteType();
	}

	public long[] getValuesAsSLongType() {
		long[] result = new long[count];

		for (int i = 0; i < values.length; i++) {
			result[i] = new Endian(littleEndian, new Bytes(values[i])).readSignedDWord();
		}

		return result;
	}

	public Rational[] getValuesAsSRationalType() {
		Rational[] result = new Rational[count];

		for (int i = 0; i < values.length; i++) {
			Endian endian = new Endian(littleEndian, new Bytes(values[i]));
			result[i] = new Rational(endian.readSignedDWord(), endian.readSignedDWord());
		}

		return result;
	}

	public String getValueAsString() {
		long[] longValues = {};
		switch (type) {
		case TYPE_BYTE:
		case TYPE_ASCII:
		case TYPE_UNDEFINED:
			return getValueAsASCIIType();

		case TYPE_SHORT:
			int[] shortValues = getValuesAsShortType();
			List<Integer> shortList = new LinkedList<>();
			for (int shortValue : shortValues) {
				shortList.add(shortValue);
			}

			return join(shortList, " ");

		case TYPE_LONG:
			longValues = getValuesAsLongType();
		case TYPE_SLONG:
			longValues = getValuesAsSLongType();

			List<Long> longList = new LinkedList<>();
			for (long longValue : longValues) {
				longList.add(longValue);
			}
			return join(longList, " ");

		case TYPE_RATIONAL:
			return join(getValuesAsRationalType(), " ");
		case TYPE_SRATIONAL:
			return join(getValuesAsSRationalType(), " ");
		default:
			throw new IllegalStateException("不正な type " + type + " です");
		}
	}

	@Override
	public String toString() {
		return tag + " " + getName() + " " + getValueAsString();
	}

	int estimateSize() {
		return count * getSize();
	}

	int getSize() {
		switch (type) {
		case TYPE_BYTE:
		case TYPE_ASCII:
		case TYPE_UNDEFINED:
			return 1;

		case TYPE_SHORT:
			return 2;

		case TYPE_LONG:
		case TYPE_SLONG:
			return 4;

		case TYPE_RATIONAL:
		case TYPE_SRATIONAL:
			return 8;
		default:
			throw new IllegalStateException("不正な type " + type + " です");
		}
	}

	public static class Rational {

		public final long numerator;

		public final long denominator;

		private Rational(long numerator, long denominator) {
			this.numerator = numerator;
			this.denominator = denominator;
		}

		@Override
		public String toString() {
			return numerator + "/" + denominator;
		}
	}

	private static String join(Object[] array, String delimiter) {
		return join(Arrays.asList(array), delimiter);
	}

	private static String join(List<?> list, String delimiter) {
		return String.join(delimiter, list.stream().map(e -> e.toString()).collect(Collectors.toList()));
	}
}
