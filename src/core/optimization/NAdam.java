/********************************************************
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2020 Simo Aaltonen
 *
 ********************************************************/

package core.optimization;

import utils.*;
import utils.matrix.DMatrix;
import utils.matrix.Matrix;
import utils.matrix.MatrixException;
import utils.matrix.UnaryFunctionType;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class that implements Nadam optimizer.<br>
 * <br>
 * Reference: http://ruder.io/optimizing-gradient-descent/<br>
 *
 */
public class NAdam implements Optimizer, Serializable {

    private static final long serialVersionUID = 6575858816658305979L;

    /**
     * Learning rate for Nadam. Default value 0.001.
     *
     */
    private double learningRate = 0.001;

    /**
     * Beta1 term for Nadam. Default value 0.9.
     *
     */
    private double beta1 = 0.9;

    /**
     * Beta2 term for Nadam. Default value 0.999.
     *
     */
    private double beta2 = 0.999;

    /**
     * Optimizer iteration count for Nadam.
     *
     */
    private transient int iter = 1;

    /**
     * Hash map to store first moments (means).
     *
     */
    private transient HashMap<Matrix, Matrix> m;

    /**
     * Hash map to store second moments (uncentered variances).
     *
     */
    private transient HashMap<Matrix, Matrix> v;

    /**
     * Default constructor for Nadam.
     *
     */
    public NAdam() {
    }

    /**
     * Constructor for Nadam.
     *
     * @param params parameters for Nadam.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public NAdam(String params) throws DynamicParamException {
        setParams(new DynamicParam(params, getParamDefs()));
    }

    /**
     * Returns parameters used for Nadam.
     *
     * @return parameters used for Nadam.
     */
    private HashMap<String, DynamicParam.ParamType> getParamDefs() {
        HashMap<String, DynamicParam.ParamType> paramDefs = new HashMap<>();
        paramDefs.put("learningRate", DynamicParam.ParamType.DOUBLE);
        paramDefs.put("beta1", DynamicParam.ParamType.DOUBLE);
        paramDefs.put("beta2", DynamicParam.ParamType.DOUBLE);
        return paramDefs;
    }

    /**
     * Sets parameters used for Nadam.<br>
     * <br>
     * Supported parameters are:<br>
     *     - learningRate: learning rate for optimizer. Default value 0.001.<br>
     *     - beta1: beta1 value for optimizer. Default value 0.9.<br>
     *     - beta2: beta2 value for optimizer. Default value 0.999.<br>
     *
     * @param params parameters used for Nadam.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     */
    public void setParams(DynamicParam params) throws DynamicParamException {
        if (params.hasParam("learningRate")) learningRate = params.getValueAsDouble("learningRate");
        if (params.hasParam("beta1")) beta1 = params.getValueAsDouble("beta1");
        if (params.hasParam("beta2")) beta2 = params.getValueAsDouble("beta2");
    }

    /**
     * Resets optimizer state.
     *
     */
    public void reset() {
        m = new HashMap<>();
        v = new HashMap<>();
        iter = 1;
    }

    /**
     * Set iteration count.
     *
     * @param iter iteration count.
     */
    public void setIteration(int iter) {
        this.iter = iter;
    }

    /**
     * Optimizes given weight (W) and bias (B) pair with given gradients respectively.
     *
     * @param W weight matrix to be optimized.
     * @param dW weight gradients for optimization step.
     * @param B bias matrix to be optimized.
     * @param dB bias gradients for optimization step.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void optimize(Matrix W, Matrix dW, Matrix B, Matrix dB) throws MatrixException {
        optimize(W, dW);
        optimize(B, dB);
    }

    /**
     * Optimizes single matrix (M) using calculated matrix gradient (dM).<br>
     * Matrix can be for example weight or bias matrix with gradient.<br>
     *
     * @param M matrix to be optimized.
     * @param dM matrix gradients for optimization step.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public void optimize(Matrix M, Matrix dM) throws MatrixException {
        if (m == null) m = new HashMap<>();
        if (v == null) v = new HashMap<>();
        if (iter == 0) iter = 1;
        Matrix mM;
        if (m.containsKey(M)) mM = m.get(M);
        else m.put(M, mM = new DMatrix(M.getRows(), M.getCols()));

        Matrix vM;
        if (v.containsKey(M)) vM = v.get(M);
        else v.put(M, vM = new DMatrix(M.getRows(), M.getCols()));

        // mt = β1*mt − 1 + (1 − β1)*gt
        mM.multiply(beta1).add(dM.multiply(1 - beta1), mM);

        // vt = β2*vt − 1 + (1 − β2)*g2t
        vM.multiply(beta2).add(dM.power(2).multiply(1 - beta2), vM);

        // mt = mt / (1 − βt1)
        Matrix mM_hat = mM.divide(1 - Math.pow(beta1, iter));

        // vt = vt / (1 − βt2)
        Matrix vM_hat = vM.divide(1 - Math.pow(beta2, iter));

        // θt+1 = θt − η / (√^vt+ϵ) * (β1 * mt + (1 − β1) * gt / (1 − βt1))
        double epsilon = 10E-8;
        M.subtract(vM_hat.add(epsilon).apply(UnaryFunctionType.SQRT).apply(UnaryFunctionType.MULINV).multiply(learningRate).multiply(mM_hat.multiply(beta1).add(dM.multiply((1 - beta1) / (1 - Math.pow(beta1, iter))))), M);

        iter++;
    }

}