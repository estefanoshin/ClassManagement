private interface ClassManagement {
			Function<Object, List<Method>> getAllMethods = obj -> Arrays.asList(obj.getClass().getDeclaredMethods());
			Function<Object, List<String>> getAllAttributes = obj -> Arrays.asList(obj.getClass().getDeclaredFields()).stream().map(v -> v.getName()).collect(Collectors.toList());

			Function<List<Method>, List<Method>> getters = allMethods -> allMethods.stream().filter(v -> !v.getReturnType().getName().equals("void")).collect(Collectors.toList());

			Function<Object, List<Method>> getterMethods = obj -> getters.apply(getAllMethods.apply(obj));

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
			 * crea una lista de hash con los atributos y metodos correspondientes a la clase
			 */
			BiFunction<Object, String, List<HashMap<String, Method>>> filterMethodByObjectString = (obj, s) -> getterMethods.apply(obj).stream().map(m -> createMethodMap.apply(m, s))
					.collect(Collectors.toList());

			/**
			 * Elimina Todos los elementos nulos de la matriz
			 */
			Function<List<List<HashMap<String, Method>>>, List<List<HashMap<String, Method>>>> nullEraser = matrix -> matrix.parallelStream().filter(Objects::nonNull)
					.map(inner -> inner.parallelStream().filter(Objects::nonNull).collect(Collectors.toList())).collect(Collectors.toList());

			/**
			 * Crea una matriz con hashMaps del <atributo, metodo> de una instancia
			 */
			Function<Object, List<List<HashMap<String, Method>>>> hashByObject = obj -> getAllAttributes.apply(obj).stream().map(s -> filterMethodByObjectString.apply(obj, s))
					.collect(Collectors.toList());
			
			/**
			 * Obtengo todos los getters enviando solo la instancia como parametro
			 */
			Function<Object, List<List<HashMap<String, Method>>>> getEverything = obj -> nullEraser.apply(hashByObject.apply(obj));

			/**
			 * invoca a todos los metodos enviando parametros null
			 */
			BiFunction<List<Method>, Object, List<Object>> invokeAllGetters = (method, o) -> method.stream().map(m -> {
				try {
					return m.invoke(o);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					return null;
				}
			}).collect(Collectors.toList());
		}