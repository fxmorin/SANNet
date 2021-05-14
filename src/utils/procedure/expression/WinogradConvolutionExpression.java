/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2021 Simo Aaltonen
 */

package utils.procedure.expression;

import utils.matrix.DMatrix;
import utils.matrix.Matrix;
import utils.matrix.MatrixException;
import utils.matrix.operation.CrosscorrelationFilterGradientMatrixOperation;
import utils.matrix.operation.CrosscorrelationInputGradientMatrixOperation;
import utils.matrix.operation.WinogradConvolutionMatrixOperation;
import utils.procedure.node.Node;

import java.io.Serializable;

/**
 * Class that describes expression for Winograd convolution operation.<br>
 *
 */
public class WinogradConvolutionExpression extends AbstractBinaryExpression implements Serializable {

    /**
     * G matrix for Winograd convolution.
     *
     */
    private final Matrix G;

    /**
     * G transposed matrix for Winograd convolution.
     *
     */
    private final Matrix GT;

    /**
     * Preprocessed filter.
     *
     */
    private Matrix preprocessedFilter;

    /**
     * Reference to crosscorrelation matrix operation.
     *
     */
    private final WinogradConvolutionMatrixOperation winogradConvolutionMatrixOperation;

    /**
     * Reference to crosscorrelation input gradient matrix operation.
     *
     */
    private final CrosscorrelationInputGradientMatrixOperation crosscorrelationInputGradientMatrixOperation;

    /**
     * Reference to crosscorrelation filter gradient matrix operation.
     *
     */
    private final CrosscorrelationFilterGradientMatrixOperation crosscorrelationFilterGradientMatrixOperation;

    /**
     * Constructor for Winograd convolution operation.
     *
     * @param expressionID unique ID for expression.
     * @param argument1 first argument.
     * @param argument2 second argument.
     * @param result result of expression.
     * @param stride stride of crosscorrelation operation.
     * @param dilation dilation step size for crosscorrelation operation.
     * @param filterRowSize filter row size.
     * @param filterColumnSize filter column size.
     * @throws MatrixException throws exception if expression arguments are not defined.
     */
    public WinogradConvolutionExpression(int expressionID, Node argument1, Node argument2, Node result, int stride, int dilation, int filterRowSize, int filterColumnSize) throws MatrixException {
        super("WINOGRAD_CONVOLUTION", "WINOGRAD_CONVOLUTION", expressionID, argument1, argument2, result);

        Matrix AT = new DMatrix(2, 4);
        AT.setValue(0, 0, 1);
        AT.setValue(0, 1, 1);
        AT.setValue(0, 2, 1);
        AT.setValue(0, 3, 0);
        AT.setValue(1, 0, 0);
        AT.setValue(1, 1, 1);
        AT.setValue(1, 2, -1);
        AT.setValue(1, 3, -1);
        maskZeros(AT);
        Matrix a = AT.transpose();

        Matrix c = new DMatrix(4, 4);
        c.setValue(0, 0, 1);
        c.setValue(0, 1, 0);
        c.setValue(0, 2, -1);
        c.setValue(0, 3, 0);
        c.setValue(1, 0, 0);
        c.setValue(1, 1, 1);
        c.setValue(1, 2, 1);
        c.setValue(1, 3, 0);
        c.setValue(2, 0, 0);
        c.setValue(2, 1, -1);
        c.setValue(2, 2, 1);
        c.setValue(2, 3, 0);
        c.setValue(3, 0, 0);
        c.setValue(3, 1, 1);
        c.setValue(3, 2, 0);
        c.setValue(3, 3, -1);
        maskZeros(c);
        Matrix CT = c.transpose();

        G = new DMatrix(4, 3);
        G.setValue(0, 0, 1);
        G.setValue(0, 1, 0);
        G.setValue(0, 2, 0);
        G.setValue(1, 0, 1/(double)2);
        G.setValue(1, 1, 1/(double)2);
        G.setValue(1, 2, 1/(double)2);
        G.setValue(2, 0, 1/(double)2);
        G.setValue(2, 1, -1/(double)2);
        G.setValue(2, 2, 1/(double)2);
        G.setValue(3, 0, 0);
        G.setValue(3, 1, 0);
        G.setValue(3, 2, 1);
        maskZeros(G);
        GT = G.transpose();

        winogradConvolutionMatrixOperation = new WinogradConvolutionMatrixOperation(result.getRows(), result.getColumns(), a, AT, c, CT);
        crosscorrelationInputGradientMatrixOperation = new CrosscorrelationInputGradientMatrixOperation(result.getRows(), result.getColumns(), argument2.getRows(), argument2.getColumns(), dilation, stride);
        crosscorrelationFilterGradientMatrixOperation = new CrosscorrelationFilterGradientMatrixOperation(result.getRows(), result.getColumns(), argument2.getRows(), argument2.getColumns(), dilation, stride);
    }

    /**
     * Masks matrix positions with zero value to avoid unnecessary calculations.
     *
     * @param matrix matrix to be masked.
     */
    private void maskZeros(Matrix matrix) {
        matrix.setMask();
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int column = 0; column < matrix.getColumns(); column++) {
                if (matrix.getValue(row, column) == 0) matrix.getMask().setMask(row, column, true);
            }
        }
    }

    /**
     * Calculates expression.
     *
     */
    public void calculateExpression() {
    }

    /**
     * Calculates expression.
     *
     * @param index data index.
     * @throws MatrixException throws exception if calculation fails.
     */
    public void calculateExpression(int index) throws MatrixException {
        if (argument1.getMatrix(index) == null || argument2.getMatrix(index) == null) throw new MatrixException(getExpressionName() + ": Arguments for operation not defined");
        if (preprocessedFilter == null) preprocessedFilter = G.dot(argument2.getMatrix(index)).dot(GT);
        winogradConvolutionMatrixOperation.apply(argument1.getMatrix(index), preprocessedFilter, result.getNewMatrix(index));
    }

    /**
     * Calculates gradient of expression.
     *
     */
    public void calculateGradient() {
    }

    /**
     * Calculates gradient of expression.
     *
     * @param index data index.
     * @throws MatrixException throws exception if calculation of gradient fails.
     */
    public void calculateGradient(int index) throws MatrixException {
        if (result.getGradient(index) == null) throw new MatrixException(getExpressionName() + ": Result gradient not defined.");
        argument1.cumulateGradient(index, crosscorrelationInputGradientMatrixOperation.apply(result.getGradient(index), argument2.getMatrix(index), argument1.getEmptyMatrix()), false);
        argument2.cumulateGradient(index, crosscorrelationFilterGradientMatrixOperation.apply(result.getGradient(index), argument1.getMatrix(index), argument2.getEmptyMatrix()), false);
        preprocessedFilter = null;
    }

    /**
     * Prints expression.
     *
     */
    public void printExpression() {
        printSpecificBinaryExpression();
    }

    /**
     * Prints gradient.
     *
     */
    public void printGradient() {
        printArgument1Gradient(false, getExpressionName() + "_GRADIENT(d" + result.getName() + ", " + argument2.getName() + ")");
        printArgument2Gradient(false, false, getExpressionName() + "_GRADIENT(d" + result.getName() + ", " + argument1.getName() + ")");
    }

}
