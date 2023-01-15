/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package core.layer;

import core.layer.normalization.BatchNormalization;
import core.layer.normalization.LayerNormalization;
import core.layer.normalization.WeightNormalization;
import core.layer.regularization.*;
import core.layer.reinforcement.DuelingLayer;
import core.network.NeuralNetworkException;
import core.activation.ActivationFunction;
import core.layer.convolutional.*;
import core.layer.feedforward.*;
import core.layer.recurrent.*;
import utils.configurable.DynamicParamException;
import utils.matrix.Initialization;
import utils.matrix.MatrixException;

/**
 * Factory class to construct neural network layers.<br>
 *
 */
public class LayerFactory {

    /**
     * Creates neural network execution layer.
     *
     * @param layerIndex layer index
     * @param layerType type of layer.
     * @param activationFunction activation function for layer.
     * @param initialization initialization function for layer.
     * @param params parameters for layer.
     * @return created neural network layer instance
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws NeuralNetworkException throws exception setting of activation function fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public static AbstractExecutionLayer create(int layerIndex, LayerType layerType, ActivationFunction activationFunction, Initialization initialization, String params) throws DynamicParamException, NeuralNetworkException, MatrixException {
        return switch (layerType) {
            case FEEDFORWARD -> new FeedforwardLayer(layerIndex, activationFunction, initialization, params);
            case DENSE -> new DenseLayer(layerIndex, initialization, params);
            case DUELING -> new DuelingLayer(layerIndex, params);
            case ACTIVATION -> new ActivationLayer(layerIndex, activationFunction, params);
            case FLATTEN -> new FlattenLayer(layerIndex, params);
            case RECURRENT -> new RecurrentLayer(layerIndex, activationFunction, initialization, params);
            case LSTM -> new LSTMLayer(layerIndex, activationFunction, initialization, params);
            case PEEPHOLELSTM -> new PeepholeLSTMLayer(layerIndex, activationFunction, initialization, params);
            case GRAVESLSTM -> new GravesLSTMLayer(layerIndex, activationFunction, initialization, params);
            case GRU -> new GRULayer(layerIndex, initialization, params);
            case MINGRU -> new MinGRULayer(layerIndex, initialization, params);
            case CONVOLUTION -> new ConvolutionLayer(layerIndex, activationFunction, initialization, params);
            case CROSSCORRELATION -> new CrosscorrelationLayer(layerIndex, activationFunction, initialization, params);
            case WINOGRAD_CONVOLUTION -> new WinogradConvolutionLayer(layerIndex, activationFunction, initialization, params);
            case DSCONVOLUTION -> new DSConvolutionLayer(layerIndex, activationFunction, initialization, params);
            case DSCROSSCORRELATION -> new DSCrosscorrelationLayer(layerIndex, activationFunction, initialization, params);
            case MAX_POOLING -> new MaxPoolingLayer(layerIndex, initialization, params);
            case RANDOM_POOLING -> new RandomPoolingLayer(layerIndex, initialization, params);
            case CYCLIC_POOLING -> new CyclicPoolingLayer(layerIndex, initialization, params);
            case AVERAGE_POOLING -> new AveragePoolingLayer(layerIndex, initialization, params);
            case BATCH_NORMALIZATION -> new BatchNormalization(layerIndex, initialization, params);
            case LAYER_NORMALIZATION -> new LayerNormalization(layerIndex, initialization, params);
            case WEIGHT_NORMALIZATION -> new WeightNormalization(layerIndex, initialization, params);
            case DROPOUT -> new Dropout(layerIndex, initialization, params);
            case GRADIENT_CLIPPING -> new GradientClipping(layerIndex, initialization, params);
            case L1_REGULARIZATION -> new L1_Regularization(layerIndex, initialization, params);
            case L2_REGULARIZATION -> new L2_Regularization(layerIndex, initialization, params);
            case Lp_REGULARIZATION -> new Lp_Regularization(layerIndex, initialization, params);
            case WEIGHT_NOISING -> new WeightNoising(layerIndex, initialization, params);
            case CONNECT -> new ConnectLayer(layerIndex, initialization, params);
            case JOIN -> new JoinLayer(layerIndex, initialization, params);
            case ADD -> new AddLayer(layerIndex, initialization, params);
            case MULTIPLY -> new MultiplyLayer(layerIndex, initialization, params);
            case ATTENTION -> new AttentionLayer(layerIndex, initialization, params);
        };
    }

    /**
     * Returns layer type.
     *
     * @param neuralNetworkLayer given layer.
     * @return layer type.
     * @throws NeuralNetworkException throws exception if layer is of an unknown type.
     */
    public static LayerType getLayerType(NeuralNetworkLayer neuralNetworkLayer) throws NeuralNetworkException {
        if (neuralNetworkLayer.getClass().equals(FeedforwardLayer.class)) return LayerType.FEEDFORWARD;
        if (neuralNetworkLayer.getClass().equals(DenseLayer.class)) return LayerType.DENSE;
        if (neuralNetworkLayer.getClass().equals(DuelingLayer.class)) return LayerType.DUELING;
        if (neuralNetworkLayer.getClass().equals(ActivationLayer.class)) return LayerType.ACTIVATION;
        if (neuralNetworkLayer.getClass().equals(FlattenLayer.class)) return LayerType.FLATTEN;
        if (neuralNetworkLayer.getClass().equals(RecurrentLayer.class)) return LayerType.RECURRENT;
        if (neuralNetworkLayer.getClass().equals(LSTMLayer.class)) return LayerType.LSTM;
        if (neuralNetworkLayer.getClass().equals(PeepholeLSTMLayer.class)) return LayerType.PEEPHOLELSTM;
        if (neuralNetworkLayer.getClass().equals(GravesLSTMLayer.class)) return LayerType.GRAVESLSTM;
        if (neuralNetworkLayer.getClass().equals(GRULayer.class)) return LayerType.GRU;
        if (neuralNetworkLayer.getClass().equals(MinGRULayer.class)) return LayerType.MINGRU;
        if (neuralNetworkLayer.getClass().equals(ConvolutionLayer.class)) return LayerType.CONVOLUTION;
        if (neuralNetworkLayer.getClass().equals(CrosscorrelationLayer.class)) return LayerType.CROSSCORRELATION;
        if (neuralNetworkLayer.getClass().equals(WinogradConvolutionLayer.class)) return LayerType.WINOGRAD_CONVOLUTION;
        if (neuralNetworkLayer.getClass().equals(DSConvolutionLayer.class)) return LayerType.DSCONVOLUTION;
        if (neuralNetworkLayer.getClass().equals(DSCrosscorrelationLayer.class)) return LayerType.DSCROSSCORRELATION;
        if (neuralNetworkLayer.getClass().equals(MaxPoolingLayer.class)) return LayerType.MAX_POOLING;
        if (neuralNetworkLayer.getClass().equals(RandomPoolingLayer.class)) return LayerType.RANDOM_POOLING;
        if (neuralNetworkLayer.getClass().equals(CyclicPoolingLayer.class)) return LayerType.CYCLIC_POOLING;
        if (neuralNetworkLayer.getClass().equals(AveragePoolingLayer.class)) return LayerType.AVERAGE_POOLING;
        if (neuralNetworkLayer.getClass().equals(BatchNormalization.class)) return LayerType.BATCH_NORMALIZATION;
        if (neuralNetworkLayer.getClass().equals(LayerNormalization.class)) return LayerType.LAYER_NORMALIZATION;
        if (neuralNetworkLayer.getClass().equals(WeightNormalization.class)) return LayerType.WEIGHT_NORMALIZATION;
        if (neuralNetworkLayer.getClass().equals(Dropout.class)) return LayerType.DROPOUT;
        if (neuralNetworkLayer.getClass().equals(GradientClipping.class)) return LayerType.GRADIENT_CLIPPING;
        if (neuralNetworkLayer.getClass().equals(L1_Regularization.class)) return LayerType.L1_REGULARIZATION;
        if (neuralNetworkLayer.getClass().equals(L2_Regularization.class)) return LayerType.L2_REGULARIZATION;
        if (neuralNetworkLayer.getClass().equals(Lp_Regularization.class)) return LayerType.Lp_REGULARIZATION;
        if (neuralNetworkLayer.getClass().equals(WeightNoising.class)) return LayerType.WEIGHT_NOISING;
        if (neuralNetworkLayer.getClass().equals(ConnectLayer.class)) return LayerType.CONNECT;
        if (neuralNetworkLayer.getClass().equals(JoinLayer.class)) return LayerType.JOIN;
        if (neuralNetworkLayer.getClass().equals(AddLayer.class)) return LayerType.ADD;
        if (neuralNetworkLayer.getClass().equals(MultiplyLayer.class)) return LayerType.MULTIPLY;
        if (neuralNetworkLayer.getClass().equals(AttentionLayer.class)) return LayerType.ATTENTION;
        throw new NeuralNetworkException("Unknown layer type");
    }

    /**
     * Returns type of layer as string.
     *
     * @param neuralNetworkLayer given layer.
     * @return layer type as string.
     * @throws NeuralNetworkException throws exception if layer is of an unknown type.
     */
    public static String getLayerTypeByName(NeuralNetworkLayer neuralNetworkLayer) throws NeuralNetworkException {
        if (neuralNetworkLayer.getClass().equals(FeedforwardLayer.class)) return "FEEDFORWARD";
        if (neuralNetworkLayer.getClass().equals(DenseLayer.class)) return "DENSE";
        if (neuralNetworkLayer.getClass().equals(DuelingLayer.class)) return "DUELING";
        if (neuralNetworkLayer.getClass().equals(ActivationLayer.class)) return "ACTIVATION";
        if (neuralNetworkLayer.getClass().equals(FlattenLayer.class)) return "FLATTEN";
        if (neuralNetworkLayer.getClass().equals(RecurrentLayer.class)) return "RECURRENT";
        if (neuralNetworkLayer.getClass().equals(LSTMLayer.class)) return "LSTM";
        if (neuralNetworkLayer.getClass().equals(PeepholeLSTMLayer.class)) return "PEEPHOLELSTM";
        if (neuralNetworkLayer.getClass().equals(GravesLSTMLayer.class)) return "GRAVESLSTM";
        if (neuralNetworkLayer.getClass().equals(GRULayer.class)) return "GRU";
        if (neuralNetworkLayer.getClass().equals(MinGRULayer.class)) return "MINGRU";
        if (neuralNetworkLayer.getClass().equals(ConvolutionLayer.class)) return "CONVOLUTION";
        if (neuralNetworkLayer.getClass().equals(CrosscorrelationLayer.class)) return "CROSSCORRELATION";
        if (neuralNetworkLayer.getClass().equals(WinogradConvolutionLayer.class)) return "WINOGRAD_CONVOLUTION";
        if (neuralNetworkLayer.getClass().equals(DSConvolutionLayer.class)) return "DSCONVOLUTION";
        if (neuralNetworkLayer.getClass().equals(DSCrosscorrelationLayer.class)) return "DSCROSSCORRELATION";
        if (neuralNetworkLayer.getClass().equals(MaxPoolingLayer.class)) return "MAX_POOLING";
        if (neuralNetworkLayer.getClass().equals(RandomPoolingLayer.class)) return "RANDOM_POOLING";
        if (neuralNetworkLayer.getClass().equals(CyclicPoolingLayer.class)) return "CYCLIC_POOLING";
        if (neuralNetworkLayer.getClass().equals(AveragePoolingLayer.class)) return "AVERAGE_POOLING";
        if (neuralNetworkLayer.getClass().equals(BatchNormalization.class)) return "BATCH_NORMALIZATION";
        if (neuralNetworkLayer.getClass().equals(LayerNormalization.class)) return "LAYER_NORMALIZATION";
        if (neuralNetworkLayer.getClass().equals(WeightNormalization.class)) return "WEIGHT_NORMALIZATION";
        if (neuralNetworkLayer.getClass().equals(Dropout.class)) return "DROPOUT";
        if (neuralNetworkLayer.getClass().equals(GradientClipping.class)) return "GRADIENT_CLIPPING";
        if (neuralNetworkLayer.getClass().equals(L1_Regularization.class)) return "L1_REGULARIZATION";
        if (neuralNetworkLayer.getClass().equals(L2_Regularization.class)) return "L2_REGULARIZATION";
        if (neuralNetworkLayer.getClass().equals(Lp_Regularization.class)) return "Lp_REGULARIZATION";
        if (neuralNetworkLayer.getClass().equals(WeightNoising.class)) return "WEIGHT_NOISING";
        if (neuralNetworkLayer.getClass().equals(ConnectLayer.class)) return "CONNECTOR";
        if (neuralNetworkLayer.getClass().equals(JoinLayer.class)) return "JOIN";
        if (neuralNetworkLayer.getClass().equals(AddLayer.class)) return "ADD";
        if (neuralNetworkLayer.getClass().equals(MultiplyLayer.class)) return "MULTIPLY";
        if (neuralNetworkLayer.getClass().equals(AttentionLayer.class)) return "ATTENTION";
        throw new NeuralNetworkException("Unknown layer type");
    }

}
