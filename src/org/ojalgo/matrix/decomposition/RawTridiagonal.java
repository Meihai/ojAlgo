/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

class RawTridiagonal extends TridiagonalDecomposition<Double> {

    BasicArray<Double> myMain;
    BasicArray<Double> myOff;

    RawTridiagonal() {
        super(PrimitiveDenseStore.FACTORY);
    }

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        this.setInPlace(matrix);

        myMain = Primitive64Array.make(this.getMinDim());
        myOff = Primitive64Array.make(this.getMinDim());

        this.getInPlace().tred2(myMain, myOff, true);

        return true;
    }

    public MatrixStore<Double> getD() {

        final Array1D<Double> tmpMain = Array1D.PRIMITIVE64.wrap(myMain);
        final Array1D<Double> tmpOff = Array1D.PRIMITIVE64.wrap(myOff).subList(1, (int) myOff.count());

        final DiagonalAccess<Double> tmpAccess = new DiagonalAccess<>(tmpMain, tmpOff, tmpOff, PrimitiveMath.ZERO);

        return this.wrap(tmpAccess).get();
    }

    public MatrixStore<Double> getQ() {
        return this.getInPlace();
    }

    public boolean isFullSize() {
        return true;
    }

    public boolean isSolvable() {
        return false;
    }

    public MatrixStore<Double> solve(final Access2D<Double> rhs, final DecompositionStore<Double> preallocated) {
        throw new UnsupportedOperationException();
    }

}