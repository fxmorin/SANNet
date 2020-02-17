/********************************************************
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2020 Simo Aaltonen
 *
 ********************************************************/

package core.optimization;

import core.NeuralNetworkException;
import utils.DynamicParamException;

/**
 * Defines factory class to construct optimizers.
 *
 */
public class OptimizerFactory {

    /**
     * Constructs optimizer.
     *
     * @param optimization optimizer type.
     * @param params parameters for specific optimizer.
     * @return constructed optimizer.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public static Optimizer create(OptimizationType optimization, String params) throws DynamicParamException {
        switch (optimization) {
            case GRADIENT_DESCENT:
                return (params == null) ? new GradientDescent() : new GradientDescent(params);
            case MOMENTUM_GRADIENT_DESCENT:
                return (params == null) ? new MomentumGradientDescent() : new MomentumGradientDescent(params);
            case NESTEROV_ACCELERATED_GRADIENT:
                return (params == null) ? new NesterovAcceleratedGradient() : new NesterovAcceleratedGradient(params);
            case ADAGRAD:
                return (params == null) ? new Adagrad() : new Adagrad(params);
            case ADADELTA:
                return (params == null) ? new Adadelta() : new Adadelta(params);
            case RMSPROP:
                return (params == null) ? new RMSProp() : new RMSProp(params);
            case ADAM:
                return (params == null) ? new Adam() : new Adam(params);
            case ADAMAX:
                return (params == null) ? new Adamax() : new Adamax(params);
            case NADAM:
                return (params == null) ? new NAdam() : new NAdam(params);
            case AMSGRAD:
                return (params == null) ? new AMSGrad() : new AMSGrad(params);
            case RESILIENT_PROPAGATION:
                return (params == null) ? new ResilientPropagation() : new ResilientPropagation(params);
        }
        return null;
    }

    /**
     * Constructs optimizer with default parameters.
     *
     * @param optimization optimizer type.
     * @return constructed optimizer.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public static Optimizer create(OptimizationType optimization) throws DynamicParamException {
        return create(optimization, null);
    }

    /**
     * Returns type of optimizer.
     *
     * @param optimizer given optimizer.
     * @return type of optimizer.
     * @throws NeuralNetworkException throws exception if optimizer is of an unknown type.
     */
    public static OptimizationType getOptimizationType(Optimizer optimizer) throws NeuralNetworkException {
        if (optimizer instanceof GradientDescent) return OptimizationType.GRADIENT_DESCENT;
        if (optimizer instanceof MomentumGradientDescent) return OptimizationType.MOMENTUM_GRADIENT_DESCENT;
        if (optimizer instanceof NesterovAcceleratedGradient) return OptimizationType.NESTEROV_ACCELERATED_GRADIENT;
        if (optimizer instanceof Adagrad) return OptimizationType.ADAGRAD;
        if (optimizer instanceof Adadelta) return OptimizationType.ADADELTA;
        if (optimizer instanceof RMSProp) return OptimizationType.RMSPROP;
        if (optimizer instanceof Adam) return OptimizationType.ADAM;
        if (optimizer instanceof Adamax) return OptimizationType.ADAMAX;
        if (optimizer instanceof NAdam) return OptimizationType.NADAM;
        if (optimizer instanceof AMSGrad) return OptimizationType.AMSGRAD;
        if (optimizer instanceof ResilientPropagation) return OptimizationType.RESILIENT_PROPAGATION;
        throw new NeuralNetworkException("Unknown optimizer type");
    }

}

