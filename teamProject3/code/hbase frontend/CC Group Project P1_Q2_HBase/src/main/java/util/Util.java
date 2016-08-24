package util;

import io.undertow.server.HttpServerExchange;

public class Util {
    public static boolean isParameterMissing(HttpServerExchange input,String[] parameters){
        for(String parameter:parameters){
            if(input==null||
                    input.getQueryParameters().get(parameter)==null
                    ||input.getQueryParameters().get(parameter).isEmpty()
                    ||input.getQueryParameters().get(parameter).peek().isEmpty()){
                return true;
            }
        }
        return false;
    }

}
