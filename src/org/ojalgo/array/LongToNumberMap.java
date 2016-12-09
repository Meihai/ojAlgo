package org.ojalgo.array;

import java.math.BigDecimal;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.PlainArray.DenseFactory;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;

public final class LongToNumberMap<N extends Number> implements SortedMap<Long, N>, Access1D<N> {

    private static int INITIAL_CAPACITY = 16;

    public static LongToNumberMap<BigDecimal> makeBig() {
        return new LongToNumberMap<>(BigArray.FACTORY);
    }

    public static LongToNumberMap<ComplexNumber> makeComplexe() {
        return new LongToNumberMap<>(ComplexArray.FACTORY);
    }

    public static LongToNumberMap<Double> makePrimitive() {
        return new LongToNumberMap<>(PrimitiveArray.FACTORY);
    }

    public static LongToNumberMap<Quaternion> makeQuaternion() {
        return new LongToNumberMap<>(QuaternionArray.FACTORY);
    }

    public static LongToNumberMap<RationalNumber> makeRational() {
        return new LongToNumberMap<>(RationalArray.FACTORY);
    }

    private final DenseFactory<N> myArrayFactory;
    private final SparseArray<N> myStorage;

    public LongToNumberMap(final ArrayFactory<N> arrayFactory) {

        super();

        if (!(arrayFactory instanceof DenseFactory<?>)) {
            throw new IllegalArgumentException();
        }

        myArrayFactory = (DenseFactory<N>) arrayFactory;

        myStorage = new SparseArray<>(Long.MAX_VALUE, myArrayFactory, INITIAL_CAPACITY);
    }

    public void clear() {
        myStorage.clear();
    }

    public Comparator<? super Long> comparator() {
        return null;
    }

    public boolean containsKey(final long key) {
        return myStorage.index(key) >= 0;
    }

    public boolean containsKey(final Object key) {
        if (key instanceof Number) {
            return this.containsKey(((Number) key).longValue());
        } else {
            return false;
        }
    }

    public boolean containsValue(final double value) {
        for (final NonzeroView<N> tmpView : myStorage.nonzeros()) {
            if (tmpView.doubleValue() == value) {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(final Object value) {
        for (final NonzeroView<N> tmpView : myStorage.nonzeros()) {
            if (value.equals(tmpView.getNumber())) {
                return true;
            }
        }
        return false;
    }

    public long count() {
        return myStorage.getActualLength();
    }

    public double doubleValue(final long key) {
        final int tmpIndex = myStorage.index(key);
        if (tmpIndex >= 0) {
            return myStorage.doDoubleValue(tmpIndex);
        } else {
            return PrimitiveMath.NaN;
        }
    }

    public Set<Map.Entry<Long, N>> entrySet() {
        return new AbstractSet<Map.Entry<Long, N>>() {

            @Override
            public Iterator<Map.Entry<Long, N>> iterator() {
                return new Iterator<Map.Entry<Long, N>>() {

                    NonzeroView<N> tmpNonzeros = myStorage.nonzeros();

                    public boolean hasNext() {
                        return tmpNonzeros.hasNext();
                    }

                    public Map.Entry<Long, N> next() {

                        tmpNonzeros.next();

                        return new Map.Entry<Long, N>() {

                            public Long getKey() {
                                return tmpNonzeros.index();
                            }

                            public N getValue() {
                                return tmpNonzeros.getNumber();
                            }

                            public N setValue(final N value) {
                                throw new UnsupportedOperationException();
                            }

                        };
                    }

                };
            }

            @Override
            public int size() {
                return myStorage.getActualLength();
            }
        };
    }

    public Long firstKey() {
        return myStorage.firstIndex();
    }

    public N get(final long key) {
        final int tmpIndex = myStorage.index(key);
        if (tmpIndex >= 0) {
            return myStorage.doGet(tmpIndex);
        } else {
            return null;
        }
    }

    public N get(final Object key) {
        return key instanceof Number ? this.get(((Number) key).longValue()) : null;
    }

    public LongToNumberMap<N> headMap(final long toKey) {
        return this.subMap(myStorage.firstIndex(), toKey);
    }

    public LongToNumberMap<N> headMap(final Long toKey) {
        return this.headMap(toKey.longValue());
    }

    public boolean isEmpty() {
        return myStorage.getActualLength() == 0;
    }

    public Set<Long> keySet() {
        return new AbstractSet<Long>() {

            @Override
            public Iterator<Long> iterator() {
                return myStorage.indices().iterator();
            }

            @Override
            public int size() {
                return myStorage.getActualLength();
            }

        };
    }

    public Long lastKey() {
        return myStorage.lastIndex();
    }

    public double put(final long key, final double value) {
        final int tmpIndex = myStorage.index(key);
        final double tmpOldValue = myStorage.doDoubleValue(tmpIndex);
        myStorage.doSet(key, tmpIndex, value);
        return tmpOldValue;
    }

    public N put(final long key, final N value) {
        final int tmpIndex = myStorage.index(key);
        final N tmpOldValue = myStorage.doGet(tmpIndex);
        myStorage.doSet(key, tmpIndex, value);
        return tmpOldValue;
    }

    public N put(final Long key, final N value) {
        return this.put(key.longValue(), value);
    }

    public void putAll(final LongToNumberMap<N> m) {
        if (myStorage.isPrimitive()) {
            for (final NonzeroView<N> tmpView : m.getStorage().nonzeros()) {
                myStorage.set(tmpView.index(), tmpView.doubleValue());
            }
        } else {
            for (final NonzeroView<N> tmpView : m.getStorage().nonzeros()) {
                myStorage.set(tmpView.index(), tmpView.getNumber());
            }
        }
    }

    public void putAll(final Map<? extends Long, ? extends N> m) {
        for (final java.util.Map.Entry<? extends Long, ? extends N> tmpEntry : m.entrySet()) {
            myStorage.set(tmpEntry.getKey(), tmpEntry.getValue());
        }
    }

    public N remove(final long key) {
        final N tmpOldVal = myStorage.get(key);
        myStorage.set(key, 0.0);
        return tmpOldVal;
    }

    public N remove(final Object key) {
        if (key instanceof Number) {
            return this.remove(((Number) key).longValue());
        } else {
            return null;
        }
    }

    public int size() {
        return myStorage.getActualLength();
    }

    public LongToNumberMap<N> subMap(final long fromKey, final long toKey) {

        final LongToNumberMap<N> retVal = new LongToNumberMap<>(myArrayFactory);

        long tmpKey;
        for (final NonzeroView<N> tmpView : myStorage.nonzeros()) {
            tmpKey = tmpView.index();
            if ((fromKey <= tmpKey) && (tmpKey < toKey)) {
                final N tmpValue = tmpView.getNumber();
                retVal.put(tmpKey, tmpValue);
            }
        }

        return retVal;
    }

    public LongToNumberMap<N> subMap(final Long fromKey, final Long toKey) {
        return this.subMap(fromKey.longValue(), toKey.longValue());
    }

    public LongToNumberMap<N> tailMap(final long fromKey) {
        return this.subMap(fromKey, myStorage.lastIndex() + 1L);
    }

    public LongToNumberMap<N> tailMap(final Long fromKey) {
        return this.tailMap(fromKey.longValue());
    }

    public NumberList<N> values() {
        return new NumberList<>(myStorage.getValues(), myArrayFactory, myStorage.getActualLength());
    }

    SparseArray<N> getStorage() {
        return myStorage;
    }

}