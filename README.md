# Sistema de atención de emergencias — Java Collections Framework (Caso 25)

Actividad Unidad 3 — Protocolo individual — Estructuras de Datos — Universidad de Cartagena.

Aplicación de consola que administra incidentes, tipos de emergencia y recursos
(ambulancias, bomberos, patrullas, rescate) de una central de emergencias. Los
incidentes se atienden según su gravedad y no según su orden de llegada.

- **Java:** 17 o superior (probado con OpenJDK 21).
- **Sin estructuras propias:** solo colecciones del JDK.

## Colecciones utilizadas

| Necesidad | Colección | Por qué |
|---|---|---|
| Registro general de incidentes | `List<Incidente>` → `ArrayList` | Conserva el orden de llegada; se recorre completo y se accede por posición |
| Buscar incidente por código | `Map<String, Incidente>` → `HashMap` | El código es una clave única; `get` es O(1) promedio |
| Tipos de emergencia sin duplicados | `Set<TipoEmergencia>` → `HashSet` | `add` rechaza tipos con el mismo nombre (equals/hashCode) |
| Incidentes agrupados por zona, en orden alfabético | `TreeMap<String, List<Incidente>>` | Mantiene las zonas ordenadas y relaciona una clave con varios incidentes |
| Atender primero el más grave | `PriorityQueue<Incidente>` + `ComparadorPrioridad` | Sale primero CRÍTICA > ALTA > MEDIA > BAJA; empate por orden de llegada |
| Recursos ordenados por código | `TreeMap<String, Recurso>` | Lista ordenada y asignación determinista del primer recurso libre |
| Historial de atenciones | `Deque<Incidente>` → `ArrayDeque` | La atención más reciente queda al frente |

## Compilar y ejecutar

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out emergencias.app.Main
```

En Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out emergencias.app.Main
```

Al iniciar se pueden cargar datos de prueba: 6 tipos, 9 recursos y 16 incidentes (10 en cola,
enviados en un orden distinto a su gravedad), más intentos de duplicado.

## Estructura

```
src/emergencias/
├── modelo/     Incidente, TipoEmergencia, Recurso, Gravedad, EstadoIncidente,
│               ComparadorPrioridad, EmergenciaException
├── servicio/   CentralEmergencias (reglas y colecciones), DatosPrueba
└── app/        Main, MenuConsola (solo interacción con el usuario)
```
