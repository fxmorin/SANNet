/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2020 Simo Aaltonen
 */

package utils.procedure.expression;

import utils.DynamicParamException;
import utils.matrix.MatrixException;
import utils.procedure.node.Node;

import java.io.Serializable;

/**
 * Class that describes expression for division operation.
 *
 */
public class DivideExpression extends AbstractBinaryExpression implements Serializable {

    /**
     * Name of expression.
     *
     */
    private static final String expressionName = "DIVIDE";

    /**
     * Constructor for division operation.
     *
     * @param expressionID unique ID for expression.
     * @param argument1 first argument.
     * @param argument2 second argument.
     * @param result result of expression.
     * @throws MatrixException throws exception if expression arguments are not defined.
     */
    public DivideExpression(int expressionID, Node argument1, Node argument2, Node result) throws MatrixException {
        super(expressionName, "/", expressionID, argument1, argument2, result);
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
        if (argument1.getMatrix(index) == null || argument2.getMatrix(index) == null) throw new MatrixException(expressionName + ": Arguments for operation not defined");
        result.setMatrix(index, argument1.getMatrix(index).divide(argument2.getMatrix(index)));
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
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void calculateGradient(int index) throws MatrixException, DynamicParamException {
        if (result.getGradient(index) == null) throw new MatrixException(expressionName + ": Result gradient not defined.");
        argument1.updateGradient(index, result.getGradient(index).divide(argument2.getMatrix(index)), true);
        argument2.updateGradient(index, result.getGradient(index).multiply(argument1.getMatrix(index)).divide(argument2.getMatrix(index).power(2)), false);
    }

    /**
     * Prints expression.
     *
     */
    public void printExpression() {
        printBasicBinaryExpression();
    }

    /**
     * Prints gradient.
     *
     */
    public void printGradient() {
        printArgument1Gradient(true, " / " + argument2.getName());
        printArgument2Gradient(true, false, " * " + argument1.getName() + " / " + argument2.getName() + "^2");
    }

}