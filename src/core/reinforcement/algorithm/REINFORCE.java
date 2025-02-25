/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2023 Simo Aaltonen
 */

package core.reinforcement.algorithm;

import core.reinforcement.agent.AgentException;
import core.reinforcement.agent.Environment;
import core.reinforcement.function.DirectFunctionEstimator;
import core.reinforcement.function.FunctionEstimator;
import core.reinforcement.policy.executablepolicy.ExecutablePolicyType;
import core.reinforcement.policy.updateablepolicy.UpdateableBasicPolicy;
import core.reinforcement.policy.updateablepolicy.UpdateableProximalPolicy;
import core.reinforcement.value.PlainValueFunction;
import utils.configurable.DynamicParamException;
import utils.matrix.MatrixException;

import java.io.IOException;

/**
 * Implements REINFORCE algorithm.<br>
 *
 */
public class REINFORCE extends AbstractPolicyGradient {

    /**
     * Constructor for REINFORCE
     *
     * @param environment reference to environment.
     * @param executablePolicyType executable policy type.
     * @param policyFunctionEstimator reference to policy function estimator.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     */
    public REINFORCE(Environment environment, ExecutablePolicyType executablePolicyType, FunctionEstimator policyFunctionEstimator) throws DynamicParamException, AgentException {
        super(environment, new UpdateableBasicPolicy(executablePolicyType, policyFunctionEstimator), new PlainValueFunction(policyFunctionEstimator.getNumberOfActions(), new DirectFunctionEstimator(policyFunctionEstimator.getMemory(), policyFunctionEstimator.getNumberOfStates(), policyFunctionEstimator.getNumberOfActions())));
    }

    /**
     * Constructor for REINFORCE
     *
     * @param environment reference to environment.
     * @param executablePolicyType executable policy type.
     * @param policyFunctionEstimator reference to policy function estimator.
     * @param params parameters for agent.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     */
    public REINFORCE(Environment environment, ExecutablePolicyType executablePolicyType, FunctionEstimator policyFunctionEstimator, String params) throws DynamicParamException, AgentException {
        super(environment, new UpdateableBasicPolicy(executablePolicyType, policyFunctionEstimator), new PlainValueFunction(policyFunctionEstimator.getNumberOfActions(), new DirectFunctionEstimator(policyFunctionEstimator.getMemory(), policyFunctionEstimator.getNumberOfStates(), policyFunctionEstimator.getNumberOfActions())), params);
    }

    /**
     * Constructor for REINFORCE
     *
     * @param environment reference to environment.
     * @param executablePolicyType executable policy type.
     * @param policyFunctionEstimator reference to policy function estimator.
     * @param asProximalPolicy if true proximal policy as applied otherwise basic policy gradient is applied.
     * @throws IOException throws exception if creation of target value function estimator fails.
     * @throws ClassNotFoundException throws exception if creation of target value function estimator fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public REINFORCE(Environment environment, ExecutablePolicyType executablePolicyType, FunctionEstimator policyFunctionEstimator, boolean asProximalPolicy) throws ClassNotFoundException, DynamicParamException, IOException, AgentException, MatrixException {
        super(environment, asProximalPolicy ? new UpdateableProximalPolicy(executablePolicyType, policyFunctionEstimator) : new UpdateableBasicPolicy(executablePolicyType, policyFunctionEstimator), new PlainValueFunction(policyFunctionEstimator.getNumberOfActions(), new DirectFunctionEstimator(policyFunctionEstimator.getMemory(), policyFunctionEstimator.getNumberOfStates(), policyFunctionEstimator.getNumberOfActions())));
    }

    /**
     * Constructor for REINFORCE
     *
     * @param environment reference to environment.
     * @param executablePolicyType executable policy type.
     * @param policyFunctionEstimator reference to policy function estimator.
     * @param params parameters for agent.
     * @param asProximalPolicy if true proximal policy as applied otherwise basic policy gradient is applied.
     * @throws IOException throws exception if creation of target value function estimator fails.
     * @throws ClassNotFoundException throws exception if creation of target value function estimator fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws AgentException throws exception if state action value function is applied to non-updateable policy.
     * @throws MatrixException throws exception if matrix operation fails.
     */
    public REINFORCE(Environment environment, ExecutablePolicyType executablePolicyType, FunctionEstimator policyFunctionEstimator, String params, boolean asProximalPolicy) throws DynamicParamException, IOException, ClassNotFoundException, AgentException, MatrixException {
        super(environment, asProximalPolicy ? new UpdateableProximalPolicy(executablePolicyType, policyFunctionEstimator) : new UpdateableBasicPolicy(executablePolicyType, policyFunctionEstimator), new PlainValueFunction(policyFunctionEstimator.getNumberOfActions(), new DirectFunctionEstimator(policyFunctionEstimator.getMemory(), policyFunctionEstimator.getNumberOfStates(), policyFunctionEstimator.getNumberOfActions())), params);
    }

    /**
     * Returns reference to algorithm.
     *
     * @return reference to algorithm.
     * @throws IOException throws exception if creation of target value function estimator fails.
     * @throws ClassNotFoundException throws exception if creation of target value function estimator fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if neural network has less output than actions.
     * @throws AgentException throws exception if soft Q alpha matrix is non-scalar matrix.
     */
    public REINFORCE reference() throws IOException, DynamicParamException, ClassNotFoundException, AgentException, MatrixException {
        return new REINFORCE(getEnvironment(), policy.getExecutablePolicy().getExecutablePolicyType(), policy.reference().getFunctionEstimator(), getParams());
    }

    /**
     * Returns reference to algorithm.
     *
     * @param sharedPolicyFunctionEstimator if true shared policy function estimator is used otherwise new policy function estimator is created.
     * @param sharedMemory if true shared memory is used between estimators.
     * @return reference to algorithm.
     * @throws IOException throws exception if creation of target value function estimator fails.
     * @throws ClassNotFoundException throws exception if creation of target value function estimator fails.
     * @throws DynamicParamException throws exception if parameter (params) setting fails.
     * @throws MatrixException throws exception if neural network has less output than actions.
     * @throws AgentException throws exception if soft Q alpha matrix is non-scalar matrix.
     */
    public REINFORCE reference(boolean sharedPolicyFunctionEstimator, boolean sharedMemory) throws IOException, DynamicParamException, ClassNotFoundException, AgentException, MatrixException {
        return new REINFORCE(getEnvironment(), policy.getExecutablePolicy().getExecutablePolicyType(), policy.reference(sharedPolicyFunctionEstimator, sharedMemory).getFunctionEstimator(), getParams());
    }

}
