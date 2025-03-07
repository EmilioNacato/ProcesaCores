# Microservicio ProcesaCores

Este microservicio es parte del procesador de pagos y se encarga de la comunicación con el sistema CORE bancario para procesar transacciones.

## Requisitos

- Java 21
- Maven 3.8+

## Estructura del Proyecto

El proyecto sigue una estructura estándar de Spring Boot:

```
src/
├── main/
│   ├── java/
│   │   └── com/banquito/paymentprocessor/procesarcores/banquito/
│   │       ├── client/       # Clientes para comunicación con otros servicios
│   │       ├── controller/   # Controladores REST
│   │       ├── dto/          # Objetos de transferencia de datos
│   │       ├── exception/    # Excepciones personalizadas
│   │       ├── mapper/       # Mappers para conversión entre entidades y DTOs
│   │       ├── model/        # Modelos de dominio
│   │       └── service/      # Servicios de negocio
│   └── resources/            # Archivos de configuración
└── test/
    ├── java/
    │   └── com/banquito/paymentprocessor/procesarcores/banquito/
    │       ├── client/       # Pruebas para clientes
    │       ├── controller/   # Pruebas para controladores
    │       └── service/      # Pruebas para servicios
    └── resources/            # Recursos para pruebas
```

## Ejecución de Pruebas

Para ejecutar las pruebas unitarias y generar el informe de cobertura con JaCoCo:

```bash
mvn clean test
```

## Informe de Cobertura

Después de ejecutar las pruebas, el informe de cobertura de JaCoCo estará disponible en:

```
target/site/jacoco/index.html
```

Puedes abrir este archivo en un navegador para ver el informe detallado de cobertura.

## Verificación de Cobertura

El proyecto está configurado para verificar que la cobertura de código cumpla con los siguientes umbrales:

- Instrucciones: 70%
- Ramas: 70%

Si la cobertura está por debajo de estos umbrales, la compilación fallará.

## Ejecución del Microservicio

Para ejecutar el microservicio:

```bash
mvn spring-boot:run
```

## Construcción del Proyecto

Para construir el proyecto y generar el archivo JAR:

```bash
mvn clean package
```

El archivo JAR se generará en la carpeta `target/`.

## Docker

Para construir la imagen Docker:

```bash
docker build -t banquito/procesacores:latest .
```

Para ejecutar el contenedor:

```bash
docker run -p 8080:8080 banquito/procesacores:latest
``` 