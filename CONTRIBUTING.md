# Guía de contribución — BankCore

Gracias por contribuir al proyecto. Esta guía describe el flujo de trabajo, convenciones y requisitos para mantener el código consistente entre todos los miembros del equipo.

---

## Requisitos previos

Antes de comenzar asegúrate de tener instalado:

- Java 17
- Maven 3.9+
- Docker Desktop (incluye Docker Compose)
- Git
- Un IDE compatible con Java (IntelliJ IDEA recomendado, Eclipse o VS Code)

---

## Configuración del entorno local

### 1. Clonar el repositorio

```bash
git clone https://github.com/sebas679og/bankcore-system.git
cd bankcore-system
```

### 2. Configurar variables de entorno

Copia el archivo de plantilla y completa los valores:

```bash
cp .env.template .env
```

Edita el `.env` con tus valores locales. El archivo `.env` está en `.gitignore` y nunca debe ser commiteado.

### 3. Levantar el entorno local

```bash
docker compose up --build
```

Esto construye las imágenes localmente desde el código fuente y levanta los tres contenedores: PostgreSQL, ms-customers y ms-accounts.

---

## Flujo de trabajo con Git

El proyecto sigue el flujo `feat/ → dev → main`.

```
feat/mi-feature  →  dev  →  main
                               (CI/CD)
```

`main` es la rama de producción. Todo lo que llega a `main` se construye y publica automáticamente en el registry de Docker vía GitHub Actions.

### Ramas

Crea siempre tu rama desde `dev`:

```bash
git checkout dev
git pull origin dev
git checkout -b feat/nombre-descriptivo
```

Convenciones de nombre:

| Prefijo | Uso |
|---|---|
| `feat/` | Nueva funcionalidad |
| `fix/` | Corrección de bug |
| `refactor/` | Refactorización sin cambio de comportamiento |
| `docs/` | Cambios solo en documentación |
| `chore/` | Tareas de mantenimiento (deps, config, CI) |

### Commits

Usa mensajes descriptivos en español o inglés de forma consistente dentro de tu rama. Formato recomendado:

```
tipo: descripción corta en imperativo

Descripción opcional más detallada si el cambio
lo requiere.
```

Ejemplos:

```
feat: agregar endpoint de validación de PIN
fix: corregir cálculo de límite diario en retiros
refactor: extraer lógica de JWT a servicio dedicado
docs: actualizar variables de entorno en README
```

---

## Linters y pruebas

Antes de abrir cualquier PR es **obligatorio** ejecutar Spotless en local para garantizar que el código cumple con el estilo definido en el proyecto.

### Spotless (formateo de código)

Aplica el formatter automáticamente — **paso obligatorio antes de todo PR**:

```bash
./mvnw spotless:apply
```

Verifica que el código ya cumple con el formato sin modificarlo:

```bash
./mvnw spotless:check
```

> Si `spotless:check` falla en el pipeline, el PR será rechazado. Ejecuta siempre `spotless:apply` antes de hacer push para evitarlo.

### Ejecutar solo las pruebas

Corre las pruebas omitiendo la verificación de Spotless:

```bash
./mvnw -B clean verify "-Dspotless.check.skip=true"
```

### Linters + pruebas juntos

Ejecuta el formatter y las pruebas en un solo paso:

```bash
./mvnw -B clean verify
```

> Se recomienda correr este comando antes de cada PR para confirmar que tanto el estilo como las pruebas pasan correctamente.

---

## Pull Requests

### Desde `feat/` hacia `dev`

1. Asegúrate de que tu rama está actualizada con `dev`:

```bash
git fetch origin
git rebase origin/dev
```

2. Ejecuta linters y pruebas localmente (ver sección anterior) antes de abrir el PR.
3. Abre el PR en GitHub apuntando a `dev`.
4. El título del PR debe seguir el mismo formato que los commits.
5. Describe brevemente qué cambia y por qué.
6. Asigna al menos un reviewer del equipo.

### Desde `dev` hacia `main`

Solo los mantenedores del proyecto abren PRs de `dev` a `main`. Al mergearse, el workflow de GitHub Actions construye y publica las imágenes Docker automáticamente.

---

## Versionado

El proyecto sigue [Semantic Versioning](https://semver.org/). Antes de abrir un PR, actualiza la versión en el `pom.xml` del microservicio afectado de acuerdo con el tipo de cambio:

| Tipo de cambio | Bump | Ejemplo |
|---|---|---|
| Corrección de bug | Patch | `0.1.0 → 0.1.1` |
| Nueva funcionalidad | Minor | `0.1.0 → 0.2.0` |
| Cambio que rompe compatibilidad | Major | `0.1.0 → 1.0.0` |

Para actualizar la versión usa el plugin de Maven:

```bash
./mvnw versions:set "-DnewVersion=0.2.0" "-DgenerateBackupPoms=false"
git add pom.xml
git commit -m "chore: bump version to 0.2.0"
```

> Este paso asegura que las imágenes Docker publicadas en el registry reflejen siempre la versión correcta del servicio.

---

## CI/CD

El workflow `.github/workflows/docker-build-push.yml` se dispara con cada push a `main` y ejecuta en orden:

1. Build y push de `ms-customers` al registry
2. Build y push de `ms-accounts` al registry (requiere que el paso anterior haya terminado)

Las imágenes se publican con dos tags: `:latest` y `:<version> (e.g. 0.7.1)`.

---

## Estructura del proyecto

```
bankcore-system/
├── .github/
│   └── workflows/                  # Pipelines de CI/CD
│       ├── code-quality.yml   
│       ├── docker-build-push.yml   
│       └── run-tes.yml
├── docs/
│   └── Postman/
│       └── Bankcore-Collection.postman_collection.json
├── ms-customers/
│   ├── Dockerfile
│   └── src/
├── ms-accounts/
│   ├── Dockerfile
│   └── src/
├── docker-compose.yml
├── .env.template                   # Plantilla de variables (sí se commitea)
├── .env                            # Variables locales (NO se commitea)
├── CONTRIBUTING.md
└── README.md
```

---

## Convenciones de código

- Idioma del código y comentarios: inglés
- Idioma de commits y PRs: español o inglés (consistente en el equipo)
- Formato: usa el formatter del IDE configurado con las reglas del proyecto
- No dejes código comentado en los PRs
- Cada microservicio es independiente — no compartas clases entre `ms-customers` y `ms-accounts`

---

## Variables de entorno

Si agregas una nueva variable de entorno a cualquier microservicio debes:

1. Agregarla al `docker-compose.yml` en la sección del servicio correspondiente.
2. Agregarla al `.env.template` con un valor de ejemplo.
3. Documentarla en el `README.md` si afecta al despliegue.

> Nunca hardcodees credenciales o configuraciones sensibles en el código fuente.