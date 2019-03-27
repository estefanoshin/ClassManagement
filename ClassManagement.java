package com.galeno.prepaga.requerimientosReclamos.ui.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ClassInterface {
	Function<Object, Map<String, Method>> extraerGetters = obj -> {
		Function<Object, List<Method>> getAllMethods = o -> Arrays.asList(o.getClass().getDeclaredMethods());
		Function<Object, List<String>> getAllAttributes = o -> Arrays.asList(o.getClass().getDeclaredFields()).stream().map(v -> v.getName()).collect(Collectors.toList());

		Function<List<Method>, List<Method>> getterFilter = allMethods -> allMethods.stream().filter(v -> !v.getReturnType().getName().equals("void")).collect(Collectors.toList());

		Function<Object, List<Method>> getterMethods = o -> getterFilter.apply(getAllMethods.apply(o));

		Function<String, String> filterGetFromMethod = name -> name.replace("get", "");
		Function<String, String> firstLetterToLowerCase = name -> name.substring(0, 1).toLowerCase() + name.substring(1);
		Function<String, String> methodNameMapper = name -> firstLetterToLowerCase.apply(filterGetFromMethod.apply(name));

		BiFunction<Method, String, HashMap<String, Method>> saveInHash = (m, s) -> {
			HashMap<String, Method> map = new HashMap<>();
			map.put(methodNameMapper.apply(s), m);
			return map;
		};

		/**
		 * crea un hashmap del metodo y el atributo
		 */
		BiFunction<Method, String, HashMap<String, Method>> createMethodMap = (m, s) -> methodNameMapper.apply(m.getName()).equals(s) ? saveInHash.apply(m, s) : null;

		/**
		 * crea una lista de hash con los atributos y metodos correspondientes a
		 * la clase
		 */
		BiFunction<Object, String, List<HashMap<String, Method>>> filterMethodByObjectString = (o, s) -> getterMethods.apply(o).stream().map(m -> createMethodMap.apply(m, s))
				.collect(Collectors.toList());

		/**
		 * Elimina Todos los elementos nulos de la matriz
		 */
		Function<List<List<HashMap<String, Method>>>, List<List<HashMap<String, Method>>>> nullEraser = matrix -> matrix.parallelStream().filter(Objects::nonNull)
				.map(inner -> inner.parallelStream().filter(Objects::nonNull).collect(Collectors.toList())).collect(Collectors.toList());

		/**
		 * Crea una matriz con hashMaps del <atributo, metodo> de una instancia
		 */
		Function<Object, List<List<HashMap<String, Method>>>> hashByObject = o -> getAllAttributes.apply(o).stream().map(s -> filterMethodByObjectString.apply(obj, s))
				.collect(Collectors.toList());

		/**
		 * Obtengo todos los getters enviando solo la instancia como parametro
		 */
		Function<Object, List<List<HashMap<String, Method>>>> getEverything = o -> nullEraser.apply(hashByObject.apply(o));

		Function<Object, Map<String, Method>> extractHashMap = o -> getEverything.apply(o).parallelStream().filter(iv -> iv != null && !iv.isEmpty()).map(iv -> iv.get(0))
				.collect(Collectors.toMap(a -> (String) a.keySet().iterator().next(), a -> (Method) a.values().iterator().next()));

		return extractHashMap.apply(obj);
	};

	BiFunction<Object, Object, Object> invokeMethod = (obj, attr) -> {
		try {
			return extraerGetters.apply(obj).get(attr).invoke(obj);
		} catch (Exception e) {
			return null;
		}
	};
}
