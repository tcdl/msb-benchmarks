package com.thomascook.bus.utils;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to perform json transformations.
 * For more details about syntax please refer to https://github.com/bazaarvoice/jolt
 */
public class JsonTransformer {

    private static Map<Integer, Chainr> transformerCache = new HashMap<>();

    public static Map transform(Object jsonObject, String jsonTransformer) {
        Chainr transformer = getOrCreateTransformer(jsonTransformer);
        return (Map)transformer.transform(jsonObject);
    }

    private static Chainr getOrCreateTransformer(String jsonTransformer) {
        Integer key = jsonTransformer.hashCode();

        if (transformerCache.containsKey(key)) {
            transformerCache.get(key);
        } else {
            Chainr transformer = Chainr.fromSpec(JsonUtils.jsonToList(jsonTransformer));
            transformerCache.put(key, transformer);
        }

        return transformerCache.get(key);
    }
}