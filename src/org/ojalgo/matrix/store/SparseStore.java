/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.matrix.store;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.AccessScalar;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.scalar.ComplexNumber;

public final class SparseStore<N extends Number> extends FactoryStore<N> implements Mutate2D {

    public static interface Factory<N extends Number> {

        SparseStore<N> make(long rowsCount, long columnsCount);

    }

    public static final class NonzeroElement<N extends Number> implements AccessScalar<N>, Iterator<NonzeroElement<N>>, Iterable<NonzeroElement<N>> {

        private final SparseArray.NonzeroElement<N> myDelegate;
        private final long myStructure;

        NonzeroElement(final SparseArray.NonzeroElement<N> delegate, final long structure) {

            super();

            myDelegate = delegate;
            myStructure = structure;
        }

        public long column() {
            return AccessUtils.column(myDelegate.index(), myStructure);
        }

        public double doubleValue() {
            return myDelegate.doubleValue();
        }

        public N getNumber() {
            return myDelegate.getNumber();
        }

        public boolean hasNext() {
            return myDelegate.hasNext();
        }

        public long index() {
            return myDelegate.index();
        }

        public Iterator<SparseStore.NonzeroElement<N>> iterator() {
            return this;
        }

        public SparseStore.NonzeroElement<N> next() {
            myDelegate.next();
            return this;
        }

        public long row() {
            return AccessUtils.row(myDelegate.index(), myStructure);
        }

    }

    public static final SparseStore.Factory<BigDecimal> BIG = new SparseStore.Factory<BigDecimal>() {

        public SparseStore<BigDecimal> make(final long rowsCount, final long columnsCount) {
            return SparseStore.makeBig((int) rowsCount, (int) columnsCount);
        }

    };

    public static final SparseStore.Factory<ComplexNumber> COMPLEX = new SparseStore.Factory<ComplexNumber>() {

        public SparseStore<ComplexNumber> make(final long rowsCount, final long columnsCount) {
            return SparseStore.makeComplex((int) rowsCount, (int) columnsCount);
        }

    };

    public static final SparseStore.Factory<Double> PRIMITIVE = new SparseStore.Factory<Double>() {

        public SparseStore<Double> make(final long rowsCount, final long columnsCount) {
            return SparseStore.makePrimitive((int) rowsCount, (int) columnsCount);
        }

    };

    public static SparseStore<BigDecimal> makeBig(final int rowsCount, final int columnsCount) {
        return new SparseStore<>(BigDenseStore.FACTORY, rowsCount, columnsCount, SparseArray.makeBig(rowsCount * columnsCount));
    }

    public static SparseStore<ComplexNumber> makeComplex(final int rowsCount, final int columnsCount) {
        return new SparseStore<>(ComplexDenseStore.FACTORY, rowsCount, columnsCount, SparseArray.makeComplex(rowsCount * columnsCount));
    }

    public static SparseStore<Double> makePrimitive(final int rowsCount, final int columnsCount) {
        return new SparseStore<>(PrimitiveDenseStore.FACTORY, rowsCount, columnsCount, SparseArray.makePrimitive(rowsCount * columnsCount));
    }

    private final SparseArray<N> myElements;
    private final int[] myFirsts;
    private final int[] myLimits;

    private SparseStore(final org.ojalgo.matrix.store.PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount) {
        super(factory, rowsCount, columnsCount);
        myElements = null;
        myFirsts = null;
        myLimits = null;
        ProgrammingError.throwForIllegalInvocation();
    }

    SparseStore(final PhysicalStore.Factory<N, ?> factory, final int rowsCount, final int columnsCount, final SparseArray<N> elements) {

        super(factory, rowsCount, columnsCount);

        myElements = elements;
        myFirsts = new int[rowsCount];
        myLimits = new int[rowsCount];
        Arrays.fill(myFirsts, columnsCount);
        // Arrays.fill(myLimits, 0); // Behövs inte, redan 0
    }

    public void add(final long row, final long col, final double addend) {
        myElements.add(AccessUtils.index(myFirsts.length, row, col), addend);
        this.updateNonZeros(row, col);
    }

    public void add(final long row, final long col, final Number addend) {
        myElements.add(AccessUtils.index(myFirsts.length, row, col), addend);
        this.updateNonZeros(row, col);
    }

    public double doubleValue(final long row, final long col) {
        return myElements.doubleValue(AccessUtils.index(myFirsts.length, row, col));
    }

    public int firstInColumn(final int col) {

        final int tmpRowDim = myFirsts.length;

        final int tmpRangeFirst = tmpRowDim * col;
        final int tmpRangeLimit = tmpRowDim * (col + 1);

        final long tmpFirstInRange = myElements.firstInRange(tmpRangeFirst, tmpRangeLimit);

        if (tmpRangeFirst == tmpFirstInRange) {
            return 0;
        } else {
            return (int) (tmpFirstInRange % tmpRowDim);
        }
    }

    public int firstInRow(final int row) {
        return myFirsts[row];
    }

    public N get(final long row, final long col) {
        return myElements.get(AccessUtils.index(myFirsts.length, row, col));
    }

    @Override
    public int limitOfColumn(final int col) {

        final int tmpRowDim = myFirsts.length;

        final int tmpRangeFirst = tmpRowDim * col;
        final int tmpRangeLimit = tmpRangeFirst + tmpRowDim;

        final long tmpLimitOfRange = myElements.limitOfRange(tmpRangeFirst, tmpRangeLimit);

        if (tmpRangeLimit == tmpLimitOfRange) {
            return tmpRowDim;
        } else {
            return (int) tmpLimitOfRange % tmpRowDim;
        }
    }

    @Override
    public int limitOfRow(final int row) {
        return myLimits[row];
    }

    public PhysicalStore<N> multiply(final Access1D<N> right, final PhysicalStore<N> target) {

        if (this.isPrimitive()) {

            final long tmpTargetRows = target.countRows();
            final long tmpComplexity = this.countColumns();
            final long tmpTargetColumns = target.countColumns();

            target.fillAll(this.factory().scalar().zero().getNumber());

            for (final SparseArray.NonzeroElement<N> tmpNonzero : myElements.nonzeros()) {
                final long tmpIndex = tmpNonzero.index();
                final double tmpValue = tmpNonzero.doubleValue();

                final long tmpRow = AccessUtils.row(tmpIndex, tmpTargetRows);
                final long tmpCol = AccessUtils.column(tmpIndex, tmpTargetRows);

                for (long j = 0L; j < tmpTargetColumns; j++) {
                    target.add(tmpRow, j, tmpValue * right.doubleValue(AccessUtils.index(tmpComplexity, tmpCol, j)));
                }
            }

            return target;

        } else {

            return super.multiply(right, target);
        }

    }

    /**
     * Marked as deprecated because of it's preliminary design - not because it will be removed.
     *
     * @deprecated v40
     */
    @Deprecated
    public Iterable<NonzeroElement<N>> nonzeros() {
        return new NonzeroElement<N>((SparseArray.NonzeroElement<N>) myElements.nonzeros(), this.countRows());
    }

    public void set(final long row, final long col, final double value) {
        myElements.set(AccessUtils.index(myFirsts.length, row, col), value);
        this.updateNonZeros(row, col);
    }

    public void set(final long row, final long col, final Number value) {
        myElements.set(AccessUtils.index(myFirsts.length, row, col), value);
        this.updateNonZeros(row, col);
    }

    private void updateNonZeros(final long row, final long col) {
        this.updateNonZeros((int) row, (int) col);
    }

    @Override
    protected void addNonZerosTo(final ElementsConsumer<N> consumer) {
        myElements.supplyNonZerosTo(consumer);
    }

    void updateNonZeros(final int row, final int col) {
        myFirsts[row] = Math.min(col, myFirsts[row]);
        myLimits[row] = Math.max(col + 1, myLimits[row]);
    }

}
