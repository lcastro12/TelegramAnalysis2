package com.google.android.gms.common.internal.safeparcel;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.internal.view.SupportMenu;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class C0141a {

    public static class C0140a extends RuntimeException {
        public C0140a(String str, Parcel parcel) {
            super(str + " Parcel: pos=" + parcel.dataPosition() + " size=" + parcel.dataSize());
        }
    }

    public static int m74a(Parcel parcel, int i) {
        return (i & SupportMenu.CATEGORY_MASK) != SupportMenu.CATEGORY_MASK ? (i >> 16) & SupportMenu.USER_MASK : parcel.readInt();
    }

    public static <T extends Parcelable> T m75a(Parcel parcel, int i, Creator<T> creator) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        Parcelable parcelable = (Parcelable) creator.createFromParcel(parcel);
        parcel.setDataPosition(a + dataPosition);
        return parcelable;
    }

    private static void m76a(Parcel parcel, int i, int i2) {
        int a = C0141a.m74a(parcel, i);
        if (a != i2) {
            throw new C0140a("Expected size " + i2 + " got " + a + " (0x" + Integer.toHexString(a) + ")", parcel);
        }
    }

    public static void m77a(Parcel parcel, int i, List list, ClassLoader classLoader) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a != 0) {
            parcel.readList(list, classLoader);
            parcel.setDataPosition(a + dataPosition);
        }
    }

    public static int m78b(Parcel parcel) {
        return parcel.readInt();
    }

    public static void m79b(Parcel parcel, int i) {
        parcel.setDataPosition(C0141a.m74a(parcel, i) + parcel.dataPosition());
    }

    public static <T> T[] m80b(Parcel parcel, int i, Creator<T> creator) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        T[] createTypedArray = parcel.createTypedArray(creator);
        parcel.setDataPosition(a + dataPosition);
        return createTypedArray;
    }

    public static int m81c(Parcel parcel) {
        int b = C0141a.m78b(parcel);
        int a = C0141a.m74a(parcel, b);
        int dataPosition = parcel.dataPosition();
        if (C0141a.m93m(b) != 20293) {
            throw new C0140a("Expected object header. Got 0x" + Integer.toHexString(b), parcel);
        }
        b = dataPosition + a;
        if (b >= dataPosition && b <= parcel.dataSize()) {
            return b;
        }
        throw new C0140a("Size read is invalid start=" + dataPosition + " end=" + b, parcel);
    }

    public static <T> ArrayList<T> m82c(Parcel parcel, int i, Creator<T> creator) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        ArrayList<T> createTypedArrayList = parcel.createTypedArrayList(creator);
        parcel.setDataPosition(a + dataPosition);
        return createTypedArrayList;
    }

    public static boolean m83c(Parcel parcel, int i) {
        C0141a.m76a(parcel, i, 4);
        return parcel.readInt() != 0;
    }

    public static byte m84d(Parcel parcel, int i) {
        C0141a.m76a(parcel, i, 4);
        return (byte) parcel.readInt();
    }

    public static short m85e(Parcel parcel, int i) {
        C0141a.m76a(parcel, i, 4);
        return (short) parcel.readInt();
    }

    public static int m86f(Parcel parcel, int i) {
        C0141a.m76a(parcel, i, 4);
        return parcel.readInt();
    }

    public static long m87g(Parcel parcel, int i) {
        C0141a.m76a(parcel, i, 8);
        return parcel.readLong();
    }

    public static BigInteger m88h(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        byte[] createByteArray = parcel.createByteArray();
        parcel.setDataPosition(a + dataPosition);
        return new BigInteger(createByteArray);
    }

    public static float m89i(Parcel parcel, int i) {
        C0141a.m76a(parcel, i, 4);
        return parcel.readFloat();
    }

    public static double m90j(Parcel parcel, int i) {
        C0141a.m76a(parcel, i, 8);
        return parcel.readDouble();
    }

    public static BigDecimal m91k(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        byte[] createByteArray = parcel.createByteArray();
        int readInt = parcel.readInt();
        parcel.setDataPosition(a + dataPosition);
        return new BigDecimal(new BigInteger(createByteArray), readInt);
    }

    public static String m92l(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        String readString = parcel.readString();
        parcel.setDataPosition(a + dataPosition);
        return readString;
    }

    public static int m93m(int i) {
        return SupportMenu.USER_MASK & i;
    }

    public static IBinder m94m(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        IBinder readStrongBinder = parcel.readStrongBinder();
        parcel.setDataPosition(a + dataPosition);
        return readStrongBinder;
    }

    public static Bundle m95n(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        Bundle readBundle = parcel.readBundle();
        parcel.setDataPosition(a + dataPosition);
        return readBundle;
    }

    public static byte[] m96o(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        byte[] createByteArray = parcel.createByteArray();
        parcel.setDataPosition(a + dataPosition);
        return createByteArray;
    }

    public static boolean[] m97p(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        boolean[] createBooleanArray = parcel.createBooleanArray();
        parcel.setDataPosition(a + dataPosition);
        return createBooleanArray;
    }

    public static int[] m98q(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        int[] createIntArray = parcel.createIntArray();
        parcel.setDataPosition(a + dataPosition);
        return createIntArray;
    }

    public static long[] m99r(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        long[] createLongArray = parcel.createLongArray();
        parcel.setDataPosition(a + dataPosition);
        return createLongArray;
    }

    public static BigInteger[] m100s(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        int readInt = parcel.readInt();
        BigInteger[] bigIntegerArr = new BigInteger[readInt];
        for (int i2 = 0; i2 < readInt; i2++) {
            bigIntegerArr[i2] = new BigInteger(parcel.createByteArray());
        }
        parcel.setDataPosition(dataPosition + a);
        return bigIntegerArr;
    }

    public static float[] m101t(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        float[] createFloatArray = parcel.createFloatArray();
        parcel.setDataPosition(a + dataPosition);
        return createFloatArray;
    }

    public static double[] m102u(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        double[] createDoubleArray = parcel.createDoubleArray();
        parcel.setDataPosition(a + dataPosition);
        return createDoubleArray;
    }

    public static BigDecimal[] m103v(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        int readInt = parcel.readInt();
        BigDecimal[] bigDecimalArr = new BigDecimal[readInt];
        for (int i2 = 0; i2 < readInt; i2++) {
            byte[] createByteArray = parcel.createByteArray();
            bigDecimalArr[i2] = new BigDecimal(new BigInteger(createByteArray), parcel.readInt());
        }
        parcel.setDataPosition(dataPosition + a);
        return bigDecimalArr;
    }

    public static String[] m104w(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        String[] createStringArray = parcel.createStringArray();
        parcel.setDataPosition(a + dataPosition);
        return createStringArray;
    }

    public static ArrayList<String> m105x(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        ArrayList<String> createStringArrayList = parcel.createStringArrayList();
        parcel.setDataPosition(a + dataPosition);
        return createStringArrayList;
    }

    public static Parcel m106y(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        Parcel obtain = Parcel.obtain();
        obtain.appendFrom(parcel, dataPosition, a);
        parcel.setDataPosition(a + dataPosition);
        return obtain;
    }

    public static Parcel[] m107z(Parcel parcel, int i) {
        int a = C0141a.m74a(parcel, i);
        int dataPosition = parcel.dataPosition();
        if (a == 0) {
            return null;
        }
        int readInt = parcel.readInt();
        Parcel[] parcelArr = new Parcel[readInt];
        for (int i2 = 0; i2 < readInt; i2++) {
            int readInt2 = parcel.readInt();
            if (readInt2 != 0) {
                int dataPosition2 = parcel.dataPosition();
                Parcel obtain = Parcel.obtain();
                obtain.appendFrom(parcel, dataPosition2, readInt2);
                parcelArr[i2] = obtain;
                parcel.setDataPosition(readInt2 + dataPosition2);
            } else {
                parcelArr[i2] = null;
            }
        }
        parcel.setDataPosition(dataPosition + a);
        return parcelArr;
    }
}
