# Proyecto: Control y Reportes de Casos por Provincia

Este proyecto implementa la generación de reportes para una fecha y país específicos, asegurando que no se dupliquen los datos y que los resultados estén ordenados.

---

## Requerimiento 1: Controlar ejecución del thread para un país y una fecha

**Objetivo:** Evitar que el proceso de generación de reportes se ejecute dos veces para el mismo país en la misma fecha.

### 1.1 Validar si ya se ejecutó

**Fragmento de código:**

```java
boolean alreadyExecuted = executionRegistry.isExecuted("2022-04-16", "ARG");

if (alreadyExecuted) {
    System.out.println("Ya se ejecutó para esta fecha y país.");
    return;
}
```

**¿Qué hace este fragmento?**

- Verifica si ya existe un registro para esa combinación de fecha y país.
- Si ya se ejecutó, imprime un mensaje y detiene el proceso.

### 1.2 Registrar nueva ejecución

**Fragmento de código:**

```java
executionRegistry.registerExecution("2022-04-16", "ARG");
```

**¿Qué hace este fragmento?**

- Guarda la ejecución actual en el registro para futuras validaciones.

---

## Requerimiento 2: Mostrar los reportes

**Objetivo:** Imprimir la lista de provincias de manera ordenada alfabéticamente y mostrar la cantidad de casos.

### 2.1 Obtener los reportes

**Fragmento de código:**

```java
List<Report> reports = reportService.getReportsByDateAndCountry("2022-04-16", "ARG");
```

**¿Qué hace este fragmento?**

- Obtiene los reportes de casos para una fecha y país especificados.

### 2.2 Ordenar y eliminar duplicados

**Fragmento de código:**

```java
Map<String, Integer> provinceCases = new TreeMap<>();

for (Report report : reports) {
    provinceCases.put(report.getProvinceName(), report.getCases());
}
```

**¿Qué hace este fragmento?**

- Usa un `TreeMap` para:
  - Eliminar duplicados: si una provincia aparece varias veces, solo queda una.
  - Ordenar automáticamente los nombres de provincias de forma alfabética.

### 2.3 Mostrar los reportes

**Fragmento de código:**

```java
for (Map.Entry<String, Integer> entry : provinceCases.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
}
```

**¿Qué hace este fragmento?**

- Recorre el `TreeMap` y muestra el nombre de cada provincia seguido de la cantidad de casos.

**Salida esperada:**

```bash
Buenos Aires -> 1500
Cordoba -> 700
Santa Fe -> 600
```

---

## Clase adicional: SerieI - Encontrar Caminos en un Árbol Binario

Esta clase busca todos los caminos en un árbol binario donde la suma de los nodos sea igual a un valor objetivo.

### Fragmento principal:

```java
public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
    List<List<Integer>> result = new ArrayList<>();
    List<Integer> currentPath = new ArrayList<>();
    findPaths(root, targetSum, currentPath, result);
    return result;
}
```

**¿Qué hace este fragmento?**

- Inicializa listas para almacenar los caminos encontrados.
- Llama al método auxiliar `findPaths` para buscar todos los caminos válidos.

### Lógica de búsqueda:

```java
private void findPaths(TreeNode node, int targetSum, List<Integer> currentPath, List<List<Integer>> result) {
    if (node == null) return;

    currentPath.add(node.val);

    if (node.left == null && node.right == null && targetSum == node.val) {
        result.add(new ArrayList<>(currentPath));
    } else {
        findPaths(node.left, targetSum - node.val, currentPath, result);
        findPaths(node.right, targetSum - node.val, currentPath, result);
    }

    currentPath.remove(currentPath.size() - 1);
}
```

**¿Qué hace este fragmento?**

- Si el nodo actual es nulo, termina.
- Agrega el valor del nodo actual al camino en curso.
- Si es una hoja y el valor coincide con `targetSum`, guarda el camino.
- Si no, sigue buscando por la izquierda y derecha.
- Al regresar, elimina el nodo actual del camino para explorar nuevas rutas.

### Árbol de prueba:

```java
TreeNode root = buildSampleTree();
int targetSum = 22;

List<List<Integer>> paths = finder.pathSum(root, targetSum);
System.out.println(paths);
```

**Ejemplo de salida:**

```bash
[[5, 4, 11, 2], [5, 8, 4, 5]]
```

---

## Clases utilizadas:

- `ExecutionRegistry`: Maneja el registro de ejecuciones.
- `ReportService`: Proporciona los reportes de casos.
- `Report`: Representa el reporte de casos para una provincia.
- `SerieI`: Encuentra caminos en un árbol binario cuya suma coincida con un valor objetivo.
- `TreeNode`: Nodo básico para representar el árbol binario.

---

## Resumen

- **Requerimiento 1:** Se controla la ejecución del proceso para evitar duplicados.
- **Requerimiento 2:** Se obtienen los reportes, se ordenan alfabéticamente y se muestran en pantalla.
- **Clase adicional SerieI:** Busca caminos con suma objetivo en un árbol binario.

Este proyecto asegura la correcta gestión y presentación de los datos, garantizando eficiencia y claridad.

