/*
 * SANNet Neural Network Framework
 * Copyright (C) 2018 - 2020 Simo Aaltonen
 */

package utils.matrix;

/**
 * Following functions are supported:
 *     ABS,
 *     COS,
 *     COSH,
 *     EXP,
 *     LOG,
 *     LOG10,
 *     SGN,
 *     SIN,
 *     SINH,
 *     SQRT,
 *     CBRT,
 *     TAN,
 *     TANH,
 *     LINEAR,
 *     SIGMOID,
 *     SWISH,
 *     HARDSIGMOID,
 *     BIPOLARSIGMOID,
 *     TANHSIG,
 *     TANHAPPR,
 *     HARDTANH,
 *     SOFTPLUS,
 *     SOFTSIGN,
 *     RELU,
 *     ELU,
 *     SELU,
 *     GELU,
 *     SOFTMAX,
 *     GAUSSIAN,
 *     SINACT,
 *     CUSTOM
 *
 */
public enum UnaryFunctionType {
    ABS,
    COS,
    COSH,
    EXP,
    LOG,
    LOG10,
    SGN,
    SIN,
    SINH,
    SQRT,
    CBRT,
    MULINV,
    TAN,
    TANH,
    LINEAR,
    SIGMOID,
    SWISH,
    HARDSIGMOID,
    BIPOLARSIGMOID,
    TANHSIG,
    TANHAPPR,
    HARDTANH,
    SOFTPLUS,
    SOFTSIGN,
    RELU,
    ELU,
    SELU,
    GELU,
    SOFTMAX,
    GAUSSIAN,
    SINACT,
    CUSTOM

}
