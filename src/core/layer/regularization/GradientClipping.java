/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package core.layer.regularization;

import core.layer.NeuralNetworkLayer;
import core.network.NeuralNetworkException;
import utils.configurable.DynamicParam;
import utils.configurable.DynamicParamException;
import utils.matrix.Initialization;
import utils.matrix.Matrix;
import utils.matrix.MatrixException;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Implements gradient clipping.<br>
 * Gradient clipping cuts gradient when certain threshold is reached to prevent then from growing too big i.e. exploding.<br>
 * <br>
 * Reference: <a href="https://hackernoon.com/gradient-clipping-57f04f0adae">...</a><br>
 *
 */
public class GradientClipping extends AbstractRegularizationLayer {

    /**
     * Parameter name types for gradient clipping.
     *     - threshold: threshold for clipping gradients. Default value 0.1.<br>
     *
     */
    private final static String paramNameTypes = "(threshold:DOUBLE)";

    /**
     * Threshold for gradient clipping.
     *
     */
    private double threshold;

    /**
     * Regularized weight of next layer.
     *
     */
    private HashSet<Matrix> nextLayerRegularizedWeights;

    /**
     * Constructor for gradient clipping layer.
     *
     * @param layerIndex layer index
     * @param initialization initialization function for weight.
     * @param params parameters for feedforward layer.
     * @throws NeuralNetworkException throws exception if setting of activation function fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public GradientClipping(int layerIndex, Initialization initialization, String params) throws NeuralNetworkException, DynamicParamException {
        super (layerIndex, initialization, params);
    }

    /**
     * Initializes default params.
     *
     */
    public void initializeDefaultParams() {
        super.initializeDefaultParams();
        threshold = 0.1;
    }

    /**
     * Returns parameters used for gradient clipping layer.
     *
     * @return parameters used for gradient clipping layer.
     */
    public String getParamDefs() {
        return super.getParamDefs() + ", " + GradientClipping.paramNameTypes;
    }

    /**
     * Sets parameters used for gradient clipping.<br>
     * <br>
     * Supported parameters are:<br>
     *     - threshold: threshold for clipping gradients. Default value 0.1.<br>
     *
     * @param params parameters used for gradient clipping.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws NeuralNetworkException throws exception if minimum layer dimensions are not met.
     */
    public void setParams(DynamicParam params) throws DynamicParamException, NeuralNetworkException {
        super.setParams(params);
        if (params.hasParam("threshold")) threshold = params.getValueAsDouble("threshold");
    }

    /**
     * Defines layer procedure for forward and backward calculation (automatic gradient) by applying procedure factory.<br>
     *
     * @throws NeuralNetworkException thrown if initialization of layer fails.
     */
    protected void defineProcedure() throws NeuralNetworkException {
        for (NeuralNetworkLayer nextLayer : getNextLayers().values()) if (nextLayer.getWeightsMap().isEmpty()) throw new NeuralNetworkException("Unable initialize weight normalization. Next layer #" + nextLayer.getLayerIndex() + " does not contain any weights.");

        nextLayerRegularizedWeights = new HashSet<>();
        for (NeuralNetworkLayer nextLayer : getNextLayers().values()) nextLayerRegularizedWeights.addAll(nextLayer.getRegularizedWeights());
    }

    /**
     * Takes single backward processing step to process layer output gradient(s) towards input.<br>
     * Applies automated backward (automatic gradient) procedure when relevant to layer.<br>
     * Applies additionally any regularization defined for layer.<br>
     *
     * @throws MatrixException throws exception if matrix operation fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void backwardProcess() throws MatrixException, DynamicParamException {
        super.backwardProcess();

        for (NeuralNetworkLayer nextLayer : getNextLayers().values()) {
            HashMap<Matrix, Matrix> nextLayerWeightGradients = nextLayer.getLayerWeightGradients();
            for (Matrix weight : nextLayerRegularizedWeights) {
                Matrix weightGradientSum = nextLayerWeightGradients.get(weight);
                if (weightGradientSum != null) {
                    double weightGradientSumL2norm = Math.sqrt(weightGradientSum.norm(2));
                    if (weightGradientSumL2norm > threshold) weightGradientSum.multiplyBy(threshold / weightGradientSumL2norm);
                }
            }
        }
    }

    /**
     * Returns layer details as string.
     *
     * @return layer details as string.
     */
    protected String getLayerDetailsByName() {
        return "Threshold: " + threshold;
    }

}
