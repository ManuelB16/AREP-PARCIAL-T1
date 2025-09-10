*Parcial Primer Corte AREP*

* Manuel Felipe Barrera
---
* Calculadora Web para estimar la media y la desviación estándar de un conjunto de números

En el presente repositorio se presenta una calculadora web, que añade numeros a una lista, permite ver la lista, borra totalmente la lista, permita sacar la media de esa lista y obtener la desviación estandar de la lista.

Comandos para correr el proyecto:

```
git clone https://github.com/ManuelB16/AREP-PARCIAL-T1
cd AREP-PARCIAL-T1
mvn clean install
```

En una terminal se coloca:

```
java -cp target/classes com.mycompany.app.HttpServer
```

Y en otra terminal sin cerrar la que ya se abrió

```
java -cp target/classes com.mycompany.app.FacadeServer
```

Luego de esto, en un navegador, se coloca:

```
http://localhost:36000
```

Y se podra observar la calculadora, ya si se desean hacer peticiones especificas, se usa:
- Agregar numero:

```
localhost:36000/add?number=(valor)
```

- Ver la lista

```
localhost:36000/list
```

- Borrar la lista

```
localhost:36000/clear
```

- Obtener la media (Se debe tener mas de un numero agregado en la lista para que la media sea obtenida con exito

```
localhost:36000/mean
```

- Obtener la desviacion estandar

```
localhost:36000/stddev
```

Funcionalidad:
