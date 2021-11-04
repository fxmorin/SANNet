/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2021 Simo Aaltonen
 */

package utils.procedure.expression;

import utils.configurable.DynamicParamException;
import utils.matrix.MatrixException;
import utils.procedure.node.Node;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * Abstract class that defines single computable expression including gradient expression.<br>
 * Assumes underlying class that implements specific expression.<br>
 *
 */
public abstract class AbstractExpression implements Expression, Serializable {

    @Serial
    private static final long serialVersionUID = -3692842009210981254L;

    /**
     * Name of expression;
     *
     */
    private final String expressionName;

    /**
     * Operation signature.
     *
     */
    private final String operationSignature;

    /**
     * Unique ID of expression.
     *
     */
    private final int expressionID;

    /**
     * Node for first argument.
     *
     */
    protected final Node argument1;

    /**
     * Node for result.
     *
     */
    protected final Node result;

    /**
     * Next expression for expression calculation.
     *
     */
    private Expression nextExpression;

    /**
     * Previous expression for gradient calculation.
     *
     */
    private Expression previousExpression;

    /**
     * Constructor for abstract expression.
     *
     * @param expressionName name of expression.
     * @param operationSignature operation signature of expression.
     * @param expressionID unique ID for expression.
     * @param argument1 first argument.
     * @param result result of expression.
     * @throws MatrixException throws exception if expression arguments are not defined.
     */
    public AbstractExpression(String expressionName, String operationSignature, int expressionID, Node argument1, Node result) throws MatrixException {
        if (argument1 == null) throw new MatrixException("First argument not defined.");
        this.expressionName = expressionName;
        this.operationSignature = operationSignature;
        this.expressionID = expressionID;
        this.argument1 = argument1;
        this.result = result;
    }

    /**
     * Returns name of expression.
     *
     * @return name of expression.
     */
    public String getExpressionName() {
        return expressionName;
    }

    /**
     * Returns signature of operation.
     *
     * @return signature of operation.
     */
    public String getOperationSignature() {
        return operationSignature;
    }

    /**
     * Returns expression ID
     *
     * @return expression ID
     */
    public int getExpressionID() {
        return expressionID;
    }

    /**
     * Returns first argument of expression.
     *
     * @return first argument of expression.
     */
    public Node getArgument1() {
        return argument1;
    }

    /**
     * Returns second argument of expression.
     *
     * @return returns null unless overloaded by abstract binary expression class.
     */
    public Node getArgument2() {
        return null;
    }

    /**
     * Returns result of expression.
     *
     * @return result of expression.
     */
    public Node getResult() {
        return result;
    }

    /**
     * Sets next expression for expression calculation chain.
     *
     * @param nextExpression next expression.
     */
    public void setNextExpression(Expression nextExpression) {
        this.nextExpression = nextExpression;
    }

    /**
     * Sets previous expression for gradient calculation chain.
     *
     * @param previousExpression previous expression.
     */
    public void setPreviousExpression (Expression previousExpression) {
        this.previousExpression = previousExpression;
    }

    /**
     * Returns true is expression is executed as single step otherwise false.
     *
     * @return true is expression is executed as single step otherwise false.
     */
    protected abstract boolean executeAsSingleStep();

    /**
     * Resets expression.
     *
     */
    public void reset() {
        if (nextExpression != null) nextExpression.reset();
    }

    /**
     * Calculates entire expression chain including normalization and regulation.
     *
     * @param sampleIndex sample index
     * @param firstSampleIndex first sample index
     * @param lastSampleIndex last key of inputs
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void calculateExpressionStep(int sampleIndex, int firstSampleIndex, int lastSampleIndex) throws MatrixException, DynamicParamException {
        calculateExpressionStep(sampleIndex, sampleIndex == firstSampleIndex, sampleIndex == lastSampleIndex);
        if (nextExpression != null) nextExpression.calculateExpressionStep(sampleIndex, firstSampleIndex, lastSampleIndex);
    }

    /**
     * Calculates entire expression chain including normalization and regulation.
     *
     * @param sampleIndices sample indices
     * @param firstSampleIndex first sample index
     * @param lastSampleIndex last sample index
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void calculateExpressionStep(Set<Integer> sampleIndices, int firstSampleIndex, int lastSampleIndex) throws MatrixException, DynamicParamException {
        for (Integer sampleIndex : sampleIndices) {
            calculateExpressionStep(sampleIndex, sampleIndex == firstSampleIndex, sampleIndex == lastSampleIndex);
        }
        if (nextExpression != null) nextExpression.calculateExpressionStep(sampleIndices, firstSampleIndex, lastSampleIndex);
    }

    /**
     * Calculates entire expression step including normalization and regulation.
     *
     * @param sampleIndex sample index
     * @param isFirstSampleIndex true if this is first sample index
     * @param isLastSampleIndex true if this is last sample index
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    private void calculateExpressionStep(int sampleIndex, boolean isFirstSampleIndex, boolean isLastSampleIndex) throws MatrixException, DynamicParamException {
        updateExpressionDependency(sampleIndex);
        if (isFirstSampleIndex) forwardRegularize();
        if (isFirstSampleIndex) forwardNormalize();
        forwardNormalize(sampleIndex);
        if (executeAsSingleStep()) {
            if (isFirstSampleIndex) calculateExpression();
        }
        else calculateExpression(sampleIndex);
        if (isLastSampleIndex) forwardNormalizeFinalize();
    }

    /**
     * Updates expression forward direction dependency.
     *
     * @param sampleIndex sample index
     * @throws MatrixException throws exception if scalar type of node and matrix are not matching or node is of type multi-index.
     */
    protected void updateExpressionDependency(int sampleIndex) throws MatrixException {
        argument1.updateMatrixDependency(sampleIndex);
    }

    /**
     * Calculates expression.
     *
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    protected abstract void calculateExpression() throws MatrixException, DynamicParamException;

    /**
     * Calculates expression.
     *
     * @param sampleIndex sample index.
     * @throws MatrixException throws exception if calculation fails.
     */
    protected abstract void calculateExpression(int sampleIndex) throws MatrixException;

    /**
     * Calculates entire gradient expression chain including normalization and regulation.
     *
     * @param sampleIndex sample index
     * @param lastSampleIndex last sample index
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void calculateGradientStep(int sampleIndex, int lastSampleIndex) throws MatrixException, DynamicParamException {
        calculateGradientStep (sampleIndex, sampleIndex == lastSampleIndex);
        if (previousExpression != null) previousExpression.calculateGradientStep(sampleIndex, lastSampleIndex);
    }

    /**
     * Calculates entire gradient expression chain including normalization and regulation.
     *
     * @param sampleIndices sample indices
     * @param lastSampleIndex last sample index
     * @param numberOfGradientSteps number of gradient steps taken
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void calculateGradientStep(Set<Integer> sampleIndices, int lastSampleIndex, int numberOfGradientSteps) throws MatrixException, DynamicParamException {
        int gradientStepCount = 0;
        for (Integer sampleIndex : sampleIndices) {
            boolean stopAtGradientStep = numberOfGradientSteps > 0 && ++gradientStepCount >= numberOfGradientSteps;
            calculateGradientStep (sampleIndex, sampleIndex == lastSampleIndex || stopAtGradientStep);
            if (stopAtGradientStep) break;
        }
        if (previousExpression != null) previousExpression.calculateGradientStep(sampleIndices, lastSampleIndex, numberOfGradientSteps);
    }

    /**
     * Calculates gradient step including normalization and regulation.
     *
     * @param sampleIndex sample index
     * @param isLastSampleIndex true if this is last sample index
     * @throws MatrixException throws exception if calculation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    private void calculateGradientStep(int sampleIndex, boolean isLastSampleIndex) throws MatrixException, DynamicParamException {
        updateGradientDependency(sampleIndex);
        if (executeAsSingleStep()) {
            if (isLastSampleIndex) calculateGradient();
        }
        else calculateGradient(sampleIndex);
        backwardNormalize(sampleIndex);
        if (isLastSampleIndex) backwardNormalize();
        if (isLastSampleIndex) backwardRegularize();
    }

    /**
     * Updates gradient dependency to backward direction.
     *
     * @param sampleIndex sample index
     * @throws MatrixException throws exception if scalar type of node and matrix are not matching or node is of type multi-index.
     */
    protected void updateGradientDependency(int sampleIndex) throws MatrixException {
        result.updateGradientDependency(sampleIndex);
    }

    /**
     * Calculates gradient of expression.
     *
     * @throws MatrixException throws exception if calculation of gradient fails.
     */
    protected abstract void calculateGradient() throws MatrixException;

    /**
     * Calculates gradient of expression.
     *
     * @param sampleIndex sample index
     * @throws MatrixException throws exception if calculation of gradient fails.
     */
    protected abstract void calculateGradient(int sampleIndex) throws MatrixException;

    /**
     * Execute forward normalization to constant node.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    protected void forwardNormalize() throws MatrixException, DynamicParamException {
        argument1.forwardNormalize();
    }

    /**
     * Execute forward normalization to specific entry (sample)
     *
     * @param sampleIndex sample index of specific entry.
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    protected void forwardNormalize(int sampleIndex) throws MatrixException, DynamicParamException {
        argument1.forwardNormalize(sampleIndex);
    }

    /**
     * Execute forward normalization finalize callback to constant node.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     */
    protected void forwardNormalizeFinalize() throws MatrixException {
        argument1.forwardNormalizeFinalize();
    }

    /**
     * Execute backward normalization to all entries of node.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    protected void backwardNormalize() throws MatrixException, DynamicParamException {
        argument1.backwardNormalize();
    }

    /**
     * Execute backward normalization to specific entry (sample)
     *
     * @param sampleIndex sample index of specific entry.
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    protected void backwardNormalize(int sampleIndex) throws MatrixException, DynamicParamException {
        argument1.backwardNormalize(sampleIndex);
    }

    /**
     * Executes forward regularization step.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     */
    protected void forwardRegularize() throws MatrixException {
        argument1.forwardRegularize();
    }

    /**
     * Cumulates error from regularization.
     *
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if matrix operation fails.
     * @return updated error value.
     */
    public double cumulateRegularizationError() throws MatrixException, DynamicParamException {
        return argument1.cumulateRegularizationError() + (nextExpression != null ? nextExpression.cumulateRegularizationError() : 0);
    }

    /**
     * Executes backward regularization.
     *
     * @throws MatrixException throws exception if matrix operation fails.
     */
    protected void backwardRegularize() throws MatrixException {
        argument1.backwardRegularize();
    }

    /**
     * Prints expression chain.
     *
     */
    public void printExpressionChain() {
        System.out.println("Chain of expressions for procedure: ");
        invokePrintExpressionChain();
    }

    /**
     * Prints expression chain.
     *
     */
    public void invokePrintExpressionChain() {
        printExpression();
        if (nextExpression != null) nextExpression.invokePrintExpressionChain();
    }

    /**
     * Prints gradient chain.
     *
     */
    public void printGradientChain() {
        System.out.println("Chain of gradients for procedure: ");
        invokePrintGradientChain();
    }

    /**
     * Prints gradient chain.
     *
     */
    public void invokePrintGradientChain() {
        printGradient();
        if (previousExpression != null) previousExpression.invokePrintGradientChain();
    }

    /**
     * Prints expression.
     *
     */
    public abstract void printExpression();

    /**
     * Prints gradient.
     *
     */
    public abstract void printGradient();

    /**
     * Prints expression.
     *
     */
    protected void print() {
        System.out.print("Expression " +getExpressionID() + ": ");
    }

    /**
     * Returns gradient identifier name.
     *
     * @return gradient identifier name.
     */
    protected String getGradientIdentifierName() {
        return "d";
    }

    /**
     * Return gradient name of argument1.
     *
     * @return gradient name of argument1.
     */
    protected String getArgument1GradientName() {
        return getGradientIdentifierName() + argument1.getName();
    }

    /**
     * Return gradient name of result.
     *
     * @return gradient name of result.
     */
    protected String getResultGradientName() {
        return getGradientIdentifierName() + result.getName();
    }

    /**
     * Returns gradient prefix for argument1.
     *
     * @return gradient prefix for argument1.
     */
    protected String getArgument1PrefixName() {
        return getExpressionName() + ": " + getArgument1GradientName() + " = " + "" + getArgument1SumPrefix();
    }

    /**
     * Returns argument1 prefix.
     *
     * @return argument1 prefix.
     */
    protected String getArgument1SumPrefix() {
        return !argument1.isMultiIndex() ? "sum(" : "";
    }

    /**
     * Returns argument1 postfix.
     *
     * @return argument1 postfix.
     */
    protected String getArgument1SumPostfix() {
        return !argument1.isMultiIndex() ? ")" : "";
    }

    /**
     * Returns node gradient name.
     *
     * @param node node
     * @return node gradient name.
     */
    protected String getNodeGradientName(Node node) {
        return getGradientIdentifierName() + node.getName();
    }

    /**
     * Returns node gradient prefix name.
     *
     * @param node node
     * @param negateResult if true result will be negated.
     * @return node gradient prefix name.
     */
    protected String getNodeGradientPrefixName(Node node, boolean negateResult) {
        return getExpressionName() + ": " + getNodeGradientName(node) + " = " + (negateResult ? "-" : "") + getNodeSumPrefix(node);
    }

    /**
     * Returns node gradient prefix name with result.
     *
     * @param node node
     * @param negateResult if true result will be negated.
     * @return node gradient prefix name with result.
     */
    protected String getNodeGradientWithResultPrefixName(Node node, boolean negateResult) {
        return getNodeGradientPrefixName(node, negateResult) + getResultGradientName();
    }

    /**
     * Returns node sum prefix.
     *
     * @param node node.
     * @return node sum prefix.
     */
    protected String getNodeSumPrefix(Node node) {
        return !node.isMultiIndex() ? "sum(" : "";
    }

    /**
     * Returns node sum postfix.
     *
     * @param node node.
     * @return node sum postfix.
     */
    protected String getNodeSumPostfix(Node node) {
        return !node.isMultiIndex() ? ")" : "";
    }

    /**
     * Prints gradient for argument1
     *
     * @param withResultPrefix true if result prefix is added.
     * @param suffix suffix part for gradient expression.
     */
    protected void printArgument1Gradient(boolean withResultPrefix, String suffix) {
        print();
        System.out.println((withResultPrefix ? getNodeGradientWithResultPrefixName(argument1, false) : getNodeGradientPrefixName(argument1, false)) + (suffix != null ? suffix : "") + getNodeSumPostfix(argument1));
    }

}
