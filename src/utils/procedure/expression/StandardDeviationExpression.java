/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package utils.procedure.expression;

import utils.configurable.DynamicParamException;
import utils.matrix.*;
import utils.procedure.node.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements expression for standard deviation operation.<br>
 *
 */
public class StandardDeviationExpression extends AbstractUnaryExpression {

    /**
     * True if calculation is done as single step otherwise false.
     *
     */
    private final boolean executeAsSingleStep;

    /**
     * Mean value as matrix.
     *
     */
    private Matrix mean;

    /**
     * Mean values as matrix for non-multi matrix case.
     *
     */
    private transient HashMap<Integer, Matrix> means = new HashMap<>();

    /**
     * Operation for square root.
     *
     */
    private final UnaryFunction sqrtFunction = new UnaryFunction(UnaryFunctionType.SQRT);

    /**
     * Constructor for standard deviation operation.
     *
     * @param expressionID unique ID for expression.
     * @param argument1 first argument.
     * @param result result of expression.
     * @param executeAsSingleStep true if calculation is done per index otherwise over all indices.
     * @throws MatrixException throws exception if expression arguments are not defined.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public StandardDeviationExpression(int expressionID, Node argument1, Node result, boolean executeAsSingleStep) throws MatrixException, DynamicParamException {
        super("STANDARD_DEVIATION", "STANDARD_DEVIATION", expressionID, argument1, result);

        this.executeAsSingleStep = executeAsSingleStep;
    }

    /**
     * Returns true is expression is executed as single step otherwise false.
     *
     * @return true is expression is executed as single step otherwise false.
     */
    protected boolean executeAsSingleStep() {
        return executeAsSingleStep;
    }

    /**
     * Resets expression.
     *
     */
    public void applyReset() {
        mean = null;
        means = new HashMap<>();
    }

    /**
     * Calculates expression.
     *
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void calculateExpression() throws MatrixException, DynamicParamException {
        if (!executeAsSingleStep()) return;
        if (argument1.getMatrices() == null) throw new MatrixException(getExpressionName() + ": Argument 1 for operation not defined");
        mean = AbstractMatrix.mean(argument1.getMatrices());
        result.setMatrix(AbstractMatrix.standardDeviation(argument1.getMatrices(), mean));
    }

    /**
     * Calculates expression.
     *
     * @param sampleIndex sample index
     * @throws MatrixException throws exception if calculation fails.
     */
    public void calculateExpression(int sampleIndex) throws MatrixException {
        if (executeAsSingleStep()) return;
        checkArgument(argument1, sampleIndex);
        Matrix mean = argument1.getMatrix(sampleIndex).meanAsMatrix();
        if (means == null) means = new HashMap<>();
        means.put(sampleIndex, mean);
        result.setMatrix(sampleIndex, argument1.getMatrix(sampleIndex).standardDeviationAsMatrix(mean));
    }

    /**
     * Calculates gradient of expression.
     *
     * @throws MatrixException throws exception if calculation of gradient fails.
     */
    public void calculateGradient() throws MatrixException {
        if (!executeAsSingleStep()) return;
        if (result.getGradient() == null) throw new MatrixException(getExpressionName() + ": Result gradient not defined");
        double argument1Size = argument1.size() - 1;
        for (Map.Entry<Integer, Matrix> entry : argument1.entrySet()) {
            int index = entry.getKey();
            Matrix argument1Matrix = entry.getValue();
            Matrix standardDeviationGradient = argument1Matrix.subtract(mean).multiply(2 / argument1Size).apply(new UnaryFunction(sqrtFunction.getDerivative()));
            argument1.cumulateGradient(index, result.getGradient().multiply(standardDeviationGradient), false);
        }
    }

    /**
     * Calculates gradient of expression.
     *
     * @param sampleIndex sample index
     * @throws MatrixException throws exception if calculation of gradient fails.
     */
    public void calculateGradient(int sampleIndex) throws MatrixException {
        if (executeAsSingleStep()) return;
        checkResultGradient(result, sampleIndex);
        if (!argument1.isStopGradient()) {
            Matrix standardDeviationGradient = argument1.getMatrix(sampleIndex).subtract(means.get(sampleIndex)).multiply(2 / (double)(result.getGradient(sampleIndex).size() - 1)).apply(new UnaryFunction(sqrtFunction.getDerivative()));
            argument1.cumulateGradient(sampleIndex, result.getGradient(sampleIndex).multiply(standardDeviationGradient), false);
        }
        means.remove(sampleIndex);
    }

    /**
     * Prints expression.
     *
     */
    public void printExpression() {
        print();
        System.out.println(getExpressionName() + "(" + argument1.getName() + ") = " + result.getName());
    }

    /**
     * Prints gradient.
     *
     */
    public void printGradient() {
        printArgument1Gradient(true, " * SQRT_GRADIENT((" + argument1.getName() + " - MEAN("  + argument1.getName() + ")) * 2 / SIZE(" + argument1.getName() + "))");
    }

}
