/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2021 Simo Aaltonen
 */

package utils.procedure;

import utils.configurable.DynamicParamException;
import utils.matrix.*;
import utils.procedure.expression.*;
import utils.procedure.node.Node;
import utils.procedure.node.NodeRegister;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Class that builds computable procedures from chain of matrix operations including automated differentiation (automatic gradient) as backward operation.<br>
 * Procedure factory records matrix operations in matrix instances having attachment to procedure factory.<br>
 *
 */
public class ProcedureFactory implements Serializable {

    @Serial
    private static final long serialVersionUID = -4961334078305757207L;

    /**
     * Procedure data to construct single procedure.
     *
     */
    private static class ProcedureData {

        /**
         * List of expressions for forward calculation.
         *
         */
        private final LinkedList<Expression> expressions = new LinkedList<>();

        /**
         * List of expressions for backward gradient calculation.
         *
         */
        private final LinkedList<Expression> gradients = new LinkedList<>();

        /**
         * Map for expressions for backward (gradient) calculation.<br>
         * This temporary map is used to build list of backward gradient expressions.<br>
         *
         */
        private final HashMap<Node, Expression> reverseExpressionMap = new HashMap<>();

        /**
         * if true procedure has dependent nodes.
         *
         */
        private boolean hasDependentNodes = false;

        /**
         * Input matrices.
         *
         */
        private MMatrix inputMatrices;

        /**
         * Input nodes.
         *
         */
        private final HashMap<Integer, Node> inputNodes = new HashMap<>();

        /**
         * Output nodes.
         *
         */
        private final HashMap<Integer, Node> outputNodes = new HashMap<>();

        /**
         * Nodes of procedure.
         *
         */
        private final HashSet<Node> nodes = new HashSet<>();

    }

    /**
     * Reference to node register.
     *
     */
    private final NodeRegister nodeRegister = new NodeRegister();

    /**
     * Current expression ID.
     *
     */
    private transient int currentExpressionID = 0;

    /**
     * Current procedure data.
     *
     */
    private transient ProcedureData currentProcedureData = null;

    /**
     * Constant matrices.
     *
     */
    private final HashSet<Matrix> constantMatrices = new HashSet<>();

    /**
     * Unique expression lock to reserve procedure factory.
     *
     */
    private double expressionLock = 0;

    /**
     * If true silently continues creation of existing procedure even new one is attempted.
     *
     */
    private boolean silentlyContinue = false;

    /**
     * Random function.
     *
     */
    private final Random random = new Random();

    /**
     * Default constructor for procedure factory.
     *
     */
    public ProcedureFactory() {
    }

    /**
     * Returns procedure
     *
     * @param forwardProcedure reference to class that defines forward procedure.
     * @param parameterMatrices parameter matrices.
     * @param constantMatrices constant matrices to be registered.
     * @param stopGradientMatrices matrices for which gradient is not updated.
     * @param reversedInput if true input will be reversed for input sequence.
     * @return resulting procedure.
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public Procedure getProcedure(ForwardProcedure forwardProcedure, HashSet<Matrix> parameterMatrices, HashSet<Matrix> constantMatrices, HashSet<Matrix> stopGradientMatrices, boolean reversedInput) throws MatrixException, DynamicParamException {
        registerConstantMatrices(parameterMatrices);
        registerConstantMatrices(constantMatrices);

        ProcedureData previousProcedureData = new ProcedureData();
        newProcedure(previousProcedureData, forwardProcedure.getInputMatrices(true));
        endProcedure(previousProcedureData, forwardProcedure.getForwardProcedure());

        ProcedureData nextProcedureData = new ProcedureData();
        newProcedure(nextProcedureData, forwardProcedure.getInputMatrices(false));
        endProcedure(nextProcedureData, forwardProcedure.getForwardProcedure());

        updateDependencies(previousProcedureData, nextProcedureData);

        nodeRegister.removeProcedureFactory();

        Expression previousExpression = null;
        for (Expression expression : nextProcedureData.expressions) {
            if (previousExpression != null) previousExpression.setNextExpression(expression);
            previousExpression = expression;
        }
        previousExpression = null;
        for (Expression expression : nextProcedureData.gradients) {
            if (previousExpression != null) previousExpression.setPreviousExpression(expression);
            previousExpression = expression;
        }

        return new Procedure(nextProcedureData.inputNodes, nextProcedureData.outputNodes, nextProcedureData.nodes, nextProcedureData.expressions.get(0), nextProcedureData.gradients.get(0), nextProcedureData.hasDependentNodes, parameterMatrices, stopGradientMatrices, reversedInput);
    }

    /**
     * Registers set of constant matrices.
     *
     * @param constantMatrices constant matrices to be registered.
     */
    private void registerConstantMatrices(Set<Matrix> constantMatrices) {
        if (constantMatrices == null) return;
        for (Matrix matrix : constantMatrices) matrix.setProcedureFactory(this);
        this.constantMatrices.addAll(constantMatrices);
    }

    /**
     * Starts building new procedure.
     *
     * @param inputMatrices input matrices.
     */
    private void newProcedure(ProcedureData procedureData, MMatrix inputMatrices) {
        procedureData.inputMatrices = inputMatrices;
        inputMatrices.setProcedureFactory(this);
        for (Matrix matrix : inputMatrices.get().values()) matrix.setProcedureFactory(this);
        currentExpressionID = 0;
        currentProcedureData = procedureData;
    }

    /**
     * Finalizes building current procedure.
     *
     * @param outputMatrices output matrices.
     * @throws MatrixException throws exception if setting of output matrix and node fails.
     */
    private void endProcedure(ProcedureData procedureData, MMatrix outputMatrices) throws MatrixException {
        if (!nodeRegister.contains(outputMatrices)) {
            for (Integer index : outputMatrices.keySet()) {
                if (!nodeRegister.contains(outputMatrices.get(index))) throw new MatrixException("Setting of output node failed. No node corresponding output matrix is found.");
                procedureData.outputNodes.put(index, nodeRegister.getNode(outputMatrices.get(index)));
            }
        } else procedureData.outputNodes.put(0, nodeRegister.getNode(outputMatrices));
        defineGradientPath(procedureData);
        currentProcedureData = null;
    }

    /**
     * Defines backward gradient calculation path for expressions.<br>
     * Records gradient path to current procedure data.<br>
     *
     */
    private void defineGradientPath(ProcedureData procedureData) {
        Stack<Node> resultNodes = new Stack<>();
        for (Node outputNode : procedureData.outputNodes.values()) resultNodes.push(outputNode);
        while (!resultNodes.empty()) {
            Expression expression = procedureData.reverseExpressionMap.get(resultNodes.pop());
            if (expression != null && !procedureData.gradients.contains(expression)) {
                procedureData.gradients.add(expression);
                Node argument1 = expression.getArgument1();
                if (argument1 != null) resultNodes.push(argument1);
                Node argument2 = expression.getArgument2();
                if (argument2 != null) resultNodes.push(argument2);
            }
        }
    }

    /**
     * Analyzes and records dependencies between previous procedure and current procedure.
     *
     */
    private void updateDependencies(ProcedureData previousProcedureData, ProcedureData nextProcedureData) {
        int expressionIDSize = nextProcedureData.expressions.size() - 1;
        for (int expressionID = 0; expressionID < expressionIDSize; expressionID++) {
            updateNodeLink(previousProcedureData, nextProcedureData, previousProcedureData.expressions.get(expressionID).getArgument1(), nextProcedureData.expressions.get(expressionID).getArgument1());
            updateNodeLink(previousProcedureData, nextProcedureData, previousProcedureData.expressions.get(expressionID).getArgument2(), nextProcedureData.expressions.get(expressionID).getArgument2());
        }
    }

    /**
     * Updates dependencies between previous (output) and current (input) node.<br>
     * Records dependencies to respective nodes.<br>
     *
     * @param previousArgumentNode previous node.
     * @param nextArgumentNode current node.
     */
    private void updateNodeLink(ProcedureData previousProcedureData, ProcedureData nextProcedureData, Node previousArgumentNode, Node nextArgumentNode) {
        int previousArgumentExpressionID = nodeRegister.getExpressionID(previousArgumentNode);
        int nextArgumentExpressionID = nodeRegister.getExpressionID(nextArgumentNode);
        if (previousArgumentExpressionID != nextArgumentExpressionID) {
            Node previousResultNode = previousProcedureData.expressions.get(previousArgumentExpressionID).getResult();
            nextProcedureData.hasDependentNodes = true;
            nextArgumentNode.setFromNode(previousResultNode);
            previousResultNode.setToNode(nextArgumentNode);
        }
    }

    /**
     * Defines node for procedure. Sets input and result nodes as non-constant nodes.
     *
     * @param matrix matrix for node.
     * @return defined node.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    private Node defineNode(Matrix matrix) throws MatrixException {
        return defineNode (matrix, false);
    }

    /**
     * Defines node for procedure. Sets input and result nodes as non-constant nodes.
     *
     * @param matrix matrix for node.
     * @return defined node.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    private Node defineSingleNode(Matrix matrix) throws MatrixException {
        return defineNode (matrix, true);
    }

    /**
     * Defines node for procedure. Sets input and result nodes as non-constant nodes.
     *
     * @param matrix matrix for node.
     * @return defined node.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    private Node defineNode(Matrix matrix, boolean asSingleNode) throws MatrixException {
        Node node = nodeRegister.defineNode(matrix, asSingleNode || constantMatrices.contains(matrix), currentExpressionID);
        for (Integer index : currentProcedureData.inputMatrices.keySet()) {
            if (currentProcedureData.inputMatrices.get(index) == matrix) currentProcedureData.inputNodes.put(index, node);
        }
        currentProcedureData.nodes.add(node);
        return node;
    }

    /**
     * Defines node for procedure. Sets input and result nodes as non-constant nodes.
     *
     * @param matrix matrix for node.
     * @return defined node.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    private Node defineNode(MMatrix matrix) throws MatrixException {
        boolean isSingleNode = false;
        for (Matrix singleMatrix : matrix.values()) {
            if (constantMatrices.contains(singleMatrix)) {
                isSingleNode = true;
                break;
            }
        }
        Node node = nodeRegister.defineNode(matrix, isSingleNode, currentExpressionID);
        for (Integer index : currentProcedureData.inputMatrices.keySet()) {
            if (currentProcedureData.inputMatrices == matrix) currentProcedureData.inputNodes.put(index, node);
        }
        currentProcedureData.nodes.add(node);
        return node;
    }

    /**
     * Starts new expression and reserves procedure factory with expression lock.
     *
     * @param originator originator of procedure request.
     * @throws MatrixException throws exception if procedure factory is already reserved by another request
     * @return unique expression lock key.
     */
    public double startExpression(Object originator) throws MatrixException {
        return startExpression(originator, true);
    }

    /**
     * Starts new expression and reserves procedure factory with expression lock.
     *
     * @param originator originator of procedure request.
     * @param silentlyContinue if true silently returns and continues creation of existing procedure without throwing exception.
     * @throws MatrixException throws exception if procedure factory is already reserved by another request
     * @return unique expression lock key.
     */
    public double startExpression(Object originator, boolean silentlyContinue) throws MatrixException {
        if (expressionLock != 0) {
            if (silentlyContinue) return 0;
            else throw new MatrixException("Procedure factory is reserved by: " + originator);
        }
        this.silentlyContinue = silentlyContinue;
        expressionLock = random.nextDouble();
        return expressionLock;
    }

    /**
     * Internally finishes creation of expression and frees expression lock.
     *
     */
    private void finishExpression() {
        expressionLock = 0;
    }

    /**
     * Checks if there is ongoing procedure. Silently continues with existing expression if flag is set otherwise throws exception.
     *
     * @param expressionLock unique expression lock key.
     * @param originator originator of procedure request.
     * @return returns true is existing expression creation is ongoing otherwise false.
     * @throws MatrixException throws exception if procedure factory is already reserved by another request
     */
    public boolean checkOngoingExpression(double expressionLock, Object originator) throws MatrixException {
        if (this.expressionLock != expressionLock) {
            if (silentlyContinue) return true;
            else throw new MatrixException("Procedure factory is reserved by: " + originator);
        }
        else return false;
    }

    /**
     * Records add expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createAddExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new AddExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records add expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createAddExpression(double expressionLock, MMatrix argument1, Matrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new AddExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records add expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createAddExpression(double expressionLock, MMatrix argument1, MMatrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new AddExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records subtract expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createSubtractExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new SubtractExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records subtract expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createSubtractExpression(double expressionLock, MMatrix argument1, Matrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new SubtractExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records subtract expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createSubtractExpression(double expressionLock, MMatrix argument1, MMatrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new SubtractExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records dot expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createDotExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new DotExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records dot expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createDotExpression(double expressionLock, MMatrix argument1, Matrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new DotExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records dot expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createDotExpression(double expressionLock, MMatrix argument1, MMatrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new DotExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records multiply expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createMultiplyExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new MultiplyExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records multiply expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createMultiplyExpression(double expressionLock, MMatrix argument1, Matrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new MultiplyExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records multiply expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createMultiplyExpression(double expressionLock, MMatrix argument1, MMatrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new MultiplyExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records divide expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createDivideExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new DivideExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records divide expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createDivideExpression(double expressionLock, MMatrix argument1, Matrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new DivideExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records divide expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createDivideExpression(double expressionLock, MMatrix argument1, MMatrix argument2, MMatrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new DivideExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result)));
    }

    /**
     * Records convolve expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @param stride stride of convolution operation.
     * @param dilation dilation step size.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createConvolveExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result, int stride, int dilation) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new ConvolveExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result), stride, dilation));
    }

    /**
     * Records crosscorrelate expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @param stride stride for operation.
     * @param dilation dilation step size.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createCrosscorrelateExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result, int stride, int dilation) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new CrosscorrelateExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result), stride, dilation));
    }

    /**
     * Records crosscorrelate expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @param stride stride for operation.
     * @param dilation dilation step size.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createWinogradConvolveExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result, int stride, int dilation) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new WinogradConvolutionExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result), stride, dilation));
    }

    /**
     * Records max pool expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @param stride stride for operation.
     * @param filterRowSize filter row size for operation.
     * @param filterColumnSize filter column size for operation.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createMaxPoolExpression(double expressionLock, Matrix argument1, Matrix result, int stride, int filterRowSize, int filterColumnSize) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new MaxPoolExpression(currentExpressionID++, defineNode(argument1), defineNode(result), stride, filterRowSize, filterColumnSize));
    }

    /**
     * Records random pool expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @param stride stride for operation.
     * @param filterRowSize filter row size for operation.
     * @param filterColumnSize filter column size for operation.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createRandomPoolExpression(double expressionLock, Matrix argument1, Matrix result, int stride, int filterRowSize, int filterColumnSize) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new RandomPoolExpression(currentExpressionID++, defineNode(argument1), defineNode(result), stride, filterRowSize, filterColumnSize));
    }

    /**
     * Records cyclic pool expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @param stride stride for operation.
     * @param filterRowSize filter row size for operation.
     * @param filterColumnSize filter column size for operation.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createCyclicPoolExpression(double expressionLock, Matrix argument1, Matrix result, int stride, int filterRowSize, int filterColumnSize) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new CyclicPoolExpression(currentExpressionID++, defineNode(argument1), defineNode(result), stride, filterRowSize, filterColumnSize));
    }

    /**
     * Records average pool expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @param stride stride for operation.
     * @param filterRowSize filter row size for operation.
     * @param filterColumnSize filter column size for operation.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createAveragePoolExpression(double expressionLock, Matrix argument1, Matrix result, int stride, int filterRowSize, int filterColumnSize) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new AveragePoolExpression(currentExpressionID++, defineNode(argument1), defineNode(result), stride, filterRowSize, filterColumnSize));
    }

    /**
     * Records sum expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createSumExpression(double expressionLock, Matrix argument1, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new SumExpression(currentExpressionID++, defineNode(argument1), defineNode(result), false));
    }

    /**
     * Records sum expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createSumExpression(double expressionLock, MMatrix argument1, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new SumExpression(currentExpressionID++, defineNode(argument1), defineSingleNode(result), true));
    }

    /**
     * Records mean expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createMeanExpression(double expressionLock, Matrix argument1, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new MeanExpression(currentExpressionID++, defineNode(argument1), defineNode(result), false));
    }

    /**
     * Records mean expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createMeanExpression(double expressionLock, MMatrix argument1, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new MeanExpression(currentExpressionID++, defineNode(argument1), defineSingleNode(result), true));
    }

    /**
     * Records variance expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createVarianceExpression(double expressionLock, Matrix argument1, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new VarianceExpression(currentExpressionID++, defineNode(argument1), defineNode(result), false));
    }

    /**
     * Records variance expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createVarianceExpression(double expressionLock, MMatrix argument1, Matrix result) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new VarianceExpression(currentExpressionID++, defineNode(argument1), defineSingleNode(result), true));
    }

    /**
     * Records standard deviation expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void createStandardDeviationExpression(double expressionLock, Matrix argument1, Matrix result) throws MatrixException, DynamicParamException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new StandardDeviationExpression(currentExpressionID++, defineNode(argument1), defineNode(result), false));
    }

    /**
     * Records standard deviation expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void createStandardDeviationExpression(double expressionLock, MMatrix argument1, Matrix result) throws MatrixException, DynamicParamException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new StandardDeviationExpression(currentExpressionID++, defineNode(argument1), defineSingleNode(result), true));
    }

    /**
     * Records norm expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @param p power of norm.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createNormExpression(double expressionLock, Matrix argument1, Matrix result, int p) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new NormExpression(currentExpressionID++, defineNode(argument1), defineNode(result), p));
    }

    /**
     * Records unary (single argument) expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @param unaryFunction UnaryFunction of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createUnaryFunctionExpression(double expressionLock, Matrix argument1, Matrix result, UnaryFunction unaryFunction) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new UnaryFunctionExpression(currentExpressionID++, defineNode(argument1), defineNode(result), unaryFunction));
    }

    /**
     * Records unary (single argument) expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param result result of expression.
     * @param unaryFunction UnaryFunction of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createUnaryFunctionExpression(double expressionLock, MMatrix argument1, MMatrix result, UnaryFunction unaryFunction) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new UnaryFunctionExpression(currentExpressionID++, defineNode(argument1), defineNode(result), unaryFunction));
    }

    /**
     * Records binary (two argument) expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @param binaryFunction BinaryFunction of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createBinaryFunctionExpression(double expressionLock, Matrix argument1, Matrix argument2, Matrix result, BinaryFunction binaryFunction) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new BinaryFunctionExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result), binaryFunction));
    }

    /**
     * Records binary (two argument) expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @param binaryFunction BinaryFunction of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createBinaryFunctionExpression(double expressionLock, MMatrix argument1, Matrix argument2, MMatrix result, BinaryFunction binaryFunction) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new BinaryFunctionExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result), binaryFunction));
    }

    /**
     * Records binary (two argument) expression to procedure factory.
     *
     * @param expressionLock unique expression lock key.
     * @param argument1 first argument of expression.
     * @param argument2 second argument of expression.
     * @param result result of expression.
     * @param binaryFunction BinaryFunction of expression.
     * @throws MatrixException throws exception if adding of expression fails.
     */
    public void createBinaryFunctionExpression(double expressionLock, MMatrix argument1, MMatrix argument2, MMatrix result, BinaryFunction binaryFunction) throws MatrixException {
        if (checkOngoingExpression(expressionLock, argument1)) return;
        storeExpression(new BinaryFunctionExpression(currentExpressionID++, defineNode(argument1), defineNode(argument2), defineNode(result), binaryFunction));
    }

    /**
     * Stores expression into procedure chain
     *
     * @param expression expression.
     */
    private void storeExpression(Expression expression) {
        currentProcedureData.expressions.add(expression);
        currentProcedureData.reverseExpressionMap.put(expression.getResult(), expression);
        finishExpression();
    }

}
