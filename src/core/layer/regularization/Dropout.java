/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2021 Simo Aaltonen
 */

package core.layer.regularization;

import core.network.NeuralNetworkException;
import utils.configurable.DynamicParam;
import utils.configurable.DynamicParamException;
import utils.matrix.Initialization;
import utils.matrix.Matrix;
import utils.matrix.MatrixException;
import utils.sampling.Sequence;

/**
 * Implements drop out regularization method for layer weights (parameters).<br>
 * Drop out is based on stochastic selection of layer nodes that are removed from training process at each training step.<br>
 * This forces other nodes to take over learning process reducing neural network's tendency to overfit.<br>
 * <br>
 * Reference: https://www.cs.toronto.edu/~hinton/absps/JMLRdropout.pdf<br>
 *
 */
public class Dropout extends AbstractRegularizationLayer {

    /**
     * Parameter name types for DropOut.
     *     - probability: probability of masking out a layer node. Default value 0.5.<br>
     *
     */
    private final static String paramNameTypes = "(probability:DOUBLE)";

    /**
     * Drop out probability of node.
     *
     */
    private double probability;

    /**
     * Constructor for drop out layer.
     *
     * @param layerIndex layer Index.
     * @param initialization initialization function for weight.
     * @param params parameters for feedforward layer.
     * @throws NeuralNetworkException throws exception if setting of activation function fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public Dropout(int layerIndex, Initialization initialization, String params) throws NeuralNetworkException, DynamicParamException {
        super (layerIndex, initialization, params);
    }

    /**
     * Initializes default params.
     *
     */
    public void initializeDefaultParams() {
        super.initializeDefaultParams();
        probability = 0.5;
    }

    /**
     * Returns parameters used for drop out layer.
     *
     * @return parameters used for drop out layer.
     */
    public String getParamDefs() {
        return super.getParamDefs() + ", " + Dropout.paramNameTypes;
    }

    /**
     * Sets parameters used for drop out.<br>
     * <br>
     * Supported parameters are:<br>
     *     - probability: probability of masking out a layer node. Default value 0.5.<br>
     *
     * @param params parameters used for drop out.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws NeuralNetworkException throws exception if minimum layer dimensions are not met.
     */
    public void setParams(DynamicParam params) throws DynamicParamException, NeuralNetworkException {
        super.setParams(params);
        if (params.hasParam("probability")) probability = 1 - params.getValueAsDouble("probability");
    }

    /**
     * Takes single forward processing step to process layer input(s).<br>
     *
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void forwardProcess() throws MatrixException {
        resetLayerOutputs();
        Sequence inputSequence = getPreviousLayerOutputs();

        if (isTraining()) {
            for (Integer sampleIndex : inputSequence.keySet()) {
                for (Matrix matrix : inputSequence.get(sampleIndex).values()) {
                    // Implements forward step for inverted drop out.<br>
                    // Function selectively masks out certain percentage of node governed by parameter probability during training phase.<br>
                    // During training phase it also compensates all remaining inputs by dividing by probability.<br>
                    matrix.multiply(1 / probability, matrix);
                    matrix.setMask();
                    matrix.getMask().setProbability(probability);
                    matrix.getMask().maskRowByProbability();
                }
            }
        }

        getLayerOutputs().putAll(inputSequence);
    }

    /**
     * Takes single backward processing step to process layer output gradient(s) towards input.<br>
     * Applies automated backward (automatic gradient) procedure when relevant to layer.<br>
     * Additionally applies any regularization defined for layer.<br>
     *
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void backwardProcess() throws MatrixException {
        resetLayerGradients();
        getLayerGradients().putAll(getNextLayerGradients());
    }

    /**
     * Returns layer details as string.
     *
     * @return layer details as string.
     */
    protected String getLayerDetailsByName() {
        return "Probability: " + probability;
    }

}