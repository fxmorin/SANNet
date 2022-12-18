/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2022 Simo Aaltonen
 */

package core.network;

import core.activation.ActivationFunction;
import core.layer.*;
import core.loss.LossFunction;
import utils.configurable.DynamicParamException;
import utils.matrix.BinaryFunctionType;
import utils.matrix.Initialization;
import utils.matrix.MatrixException;

import java.util.TreeMap;

/**
 * Defines configuration (layers and how they are connected to each other) for neural network.<br>
 *
 */
public class NeuralNetworkConfiguration {

    /**
     * Reference to input layer of neural network.
     *
     */
    private final TreeMap<Integer, InputLayer> inputLayers = new TreeMap<>();

    /**
     * List containing hidden layers for neural network.
     *
     */
    private final TreeMap<Integer, AbstractLayer> hiddenLayers = new TreeMap<>();

    /**
     * Reference to output layer of neural network.
     *
     */
    private final TreeMap<Integer, OutputLayer> outputLayers = new TreeMap<>();

    /**
     * List of neural network layers.
     *
     */
    private final TreeMap<Integer, NeuralNetworkLayer> neuralNetworkLayers = new TreeMap<>();

    /**
     * Cumulating neural network layer index count.
     *
     */
    private int neuralNetworkLayerIndexCount = 0;

    /**
     * Default constructor for neural network configuration.
     *
     */
    public NeuralNetworkConfiguration() {
    }

    /**
     * Adds input layer to neural network.
     *
     * @param params parameters for input layer.
     * @throws NeuralNetworkException throws neural network exception if adding of input layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void addInputLayer(String params) throws NeuralNetworkException, DynamicParamException {
        InputLayer inputLayer = new InputLayer(neuralNetworkLayerIndexCount++, params);
        inputLayers.put(inputLayers.size(), inputLayer);
        neuralNetworkLayers.put(neuralNetworkLayers.size(), inputLayer);
    }

    /**
     * Returns inputs layers.
     *
     * @return input layers.
     */
    public TreeMap<Integer, InputLayer> getInputLayers() {
        return new TreeMap<>() {{ putAll(inputLayers); }};
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addHiddenLayer(layerType, null, null, null);
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @param params parameters for layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType, String params) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addHiddenLayer(layerType, null, null, params);
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @param activationFunction activation function for layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType, ActivationFunction activationFunction) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addHiddenLayer(layerType, activationFunction, null, null);
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @param activationFunction activation function for layer.
     * @param params parameters for layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType, ActivationFunction activationFunction, String params) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addHiddenLayer(layerType, activationFunction, null, params);
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @param activationFunction activation function for layer.
     * @param initialization layer parameter initialization function for layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType, ActivationFunction activationFunction, Initialization initialization) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addHiddenLayer(layerType, activationFunction, initialization, null);
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @param initialization layer parameter initialization function for layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType, Initialization initialization) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addHiddenLayer(layerType, initialization, null);
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @param initialization layer parameter initialization function for layer.
     * @param params parameters for layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType, Initialization initialization, String params) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addHiddenLayer(layerType, null, initialization, params);
    }

    /**
     * Adds hidden layer to neural network. Layers are executed in order which they are added.
     *
     * @param layerType type of layer.
     * @param activationFunction activation function for layer.
     * @param initialization layer parameter initialization function for layer.
     * @param params parameters for layer.
     * @throws NeuralNetworkException throws neural network exception if adding of layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addHiddenLayer(LayerType layerType, ActivationFunction activationFunction, Initialization initialization, String params) throws NeuralNetworkException, DynamicParamException, MatrixException {
        AbstractLayer hiddenLayer = LayerFactory.create(neuralNetworkLayerIndexCount++, layerType, activationFunction, initialization, params);
        hiddenLayers.put(hiddenLayers.size(), hiddenLayer);
        neuralNetworkLayers.put(neuralNetworkLayers.size(), hiddenLayer);
    }

    /**
     * Returns hidden layers.
     *
     * @return hidden layers.
     */
    public TreeMap<Integer, AbstractLayer> getHiddenLayers() {
        return new TreeMap<>() {{ putAll(hiddenLayers); }};
    }

    /**
     * Adds output layer to neural network.
     *
     * @param lossFunctionType loss function type for output layer.
     * @throws NeuralNetworkException throws neural network exception if adding of output layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addOutputLayer(BinaryFunctionType lossFunctionType) throws NeuralNetworkException, DynamicParamException, MatrixException {
        addOutputLayer(lossFunctionType, null);
    }

    /**
     * Adds output layer to neural network.
     *
     * @param lossFunctionType loss function type for output layer.
     * @param params parameters for loss function.
     * @throws NeuralNetworkException throws neural network exception if adding of output layer fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if custom function is attempted to be created with this constructor.
     */
    public void addOutputLayer(BinaryFunctionType lossFunctionType, String params) throws NeuralNetworkException, DynamicParamException, MatrixException {
        OutputLayer outputLayer = new OutputLayer(neuralNetworkLayerIndexCount++, new LossFunction(lossFunctionType, params));
        outputLayers.put(outputLayers.size(), outputLayer);
        neuralNetworkLayers.put(neuralNetworkLayers.size(), outputLayer);
    }

    /**
     * Returns output layers of neural network.
     *
     * @return output layers of neural network.
     */
    public TreeMap<Integer, OutputLayer> getOutputLayers() {
        return new TreeMap<>() {{ putAll(outputLayers); }};
    }

    /**
     * Returns list of neural network layers.
     *
     * @return list of neural network layers.
     */
    public TreeMap<Integer, NeuralNetworkLayer> getNeuralNetworkLayers() {
        return new TreeMap<>() {{ putAll(neuralNetworkLayers); }};
    }

    /**
     * Connects previous and next layers.
     *
     * @param previousLayer previous layer.
     * @param nextLayer next layer.
     * @throws NeuralNetworkException throws exception if next layer has already previous layer and cannot have multiple previous layers.
     */
    public void connectLayers(NeuralNetworkLayer previousLayer, NeuralNetworkLayer nextLayer) throws NeuralNetworkException {
        if (nextLayer.hasPreviousLayers() && !nextLayer.canHaveMultiplePreviousLayers()) throw new NeuralNetworkException("Next layer has already previous layer and cannot have multiple previous layers.");
        previousLayer.addNextLayer(nextLayer);
        nextLayer.addPreviousLayer(previousLayer);
    }

    /**
     * Connects previous and next layers.
     *
     * @param previousLayerIndex previous layer index.
     * @param nextLayerIndex next layer index.
     * @throws NeuralNetworkException throws exception if next layer has already previous layer and cannot have multiple previous layers.
     */
    public void connectLayers(int previousLayerIndex, int nextLayerIndex) throws NeuralNetworkException {
        connectLayers(neuralNetworkLayers.get(previousLayerIndex), neuralNetworkLayers.get(nextLayerIndex));
    }

    /**
     * Connects neural network layers serially in order.
     *
     * @throws NeuralNetworkException throws exception if next layer has already previous layer and cannot have multiple previous layers.
     */
    public void connectLayersSerially() throws NeuralNetworkException {
        NeuralNetworkLayer previousNeuralNetworkLayer = null;
        for (NeuralNetworkLayer nextNeuralNetworkLayer : getNeuralNetworkLayers().values()) {
            if (previousNeuralNetworkLayer != null) connectLayers(previousNeuralNetworkLayer, nextNeuralNetworkLayer);
            previousNeuralNetworkLayer = nextNeuralNetworkLayer;
        }
    }

    /**
     * Validates neural network configuration.<br>
     * Checks that all layer are connected properly.<br>
     *
     * @throws NeuralNetworkException thrown if validation of neural network configuration fails.
     */
    public void validate() throws NeuralNetworkException {
        for (InputLayer inputLayer : inputLayers.values()) if (!inputLayer.hasNextLayers()) throw new NeuralNetworkException("Input layer #" + inputLayer.getLayerIndex() + " does not have next layer.");
        for (AbstractLayer hiddenLayer : hiddenLayers.values()) {
            if (!hiddenLayer.hasPreviousLayers()) throw new NeuralNetworkException("Hidden layer #" + hiddenLayer.getLayerIndex() + " does not have previous layer.");
            if (!hiddenLayer.hasNextLayers()) throw new NeuralNetworkException("Hidden layer #" + hiddenLayer.getLayerIndex() + " does not have next layer.");
        }
        for (OutputLayer outputLayer : outputLayers.values()) if (!outputLayer.hasPreviousLayers()) throw new NeuralNetworkException("Input layer #" + outputLayer.getLayerIndex() + " does not have previous layer.");

        checkLayerCompatibility(neuralNetworkLayers);

        for (NeuralNetworkLayer neuralNetworkLayer : neuralNetworkLayers.values()) neuralNetworkLayer.initializeDimensions();
    }

    /**
     * Checks that neural network layers are compatible with each other.
     *
     * @param neuralNetworkLayers neural network layers
     * @throws NeuralNetworkException throws exception if neural network layers are not compatible with each other
     */
    private void checkLayerCompatibility(TreeMap<Integer, NeuralNetworkLayer> neuralNetworkLayers) throws NeuralNetworkException {
        boolean hasRecurrentLayers = false;
        for (NeuralNetworkLayer neuralNetworkLayer : neuralNetworkLayers.values()) {
            if (neuralNetworkLayer.isRecurrentLayer()) {
                hasRecurrentLayers = true;
                break;
            }
        }
        if (hasRecurrentLayers) {
            for (NeuralNetworkLayer neuralNetworkLayer : neuralNetworkLayers.values()) {
                if (!neuralNetworkLayer.worksWithRecurrentLayer()) {
                    throw new NeuralNetworkException(LayerFactory.getLayerTypeByName(neuralNetworkLayer) + " layer does not work with recurrent layers.");
                }
            }
        }
    }

}
