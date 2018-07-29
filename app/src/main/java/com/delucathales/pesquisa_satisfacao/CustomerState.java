package com.delucathales.pesquisa_satisfacao;

public class CustomerState {
    public static final int NOT_REGISTERED = 0;
    public static final int REGISTERED = 1;
    public static final int REGISTERED_EVALUATED = 2;
    public static final String PROMOTER = "Promotor";
    public static final String NEUTRAL = "Neutro";
    public static final String DETRACTOR = "Detrator";

    public String getFlag(int evaluation){
        if(evaluation >= 9){
            return PROMOTER;
        }
        else if(evaluation >= 7){
            return NEUTRAL;
        }
        return DETRACTOR;
    }

}
