# 🧊 AIFridge — Nevera inteligente con Firebase e IA

App Android en **Kotlin** para gestionar tu nevera o despensa: añade tus ingredientes, controla las fechas de caducidad y pide a **Gemini** una receta con lo que tienes.

Práctica del módulo optativo **A1-P · Backend al núvol** del CFGS **DAM** en el **INS Marianao**.

---

## ✨ Funcionalidades

- 🔐 **Login y registro** con email y contraseña (Firebase Authentication)
  - Validación de todos los campos a la vez, con errores en cada campo
  - Contraseña de 6 caracteres o más, y confirmación de contraseña
  - Errores de Firebase mostrados al usuario sin borrar lo que ha escrito
- 🥚 **CRUD de ingredientes** en Cloud Firestore
  - Añadir, listar, editar (pulsando un ingrediente) y borrar con confirmación
  - Lista en tiempo real con `addSnapshotListener`
  - Cada usuario solo ve **su propia nevera**
- 📅 **Fecha de caducidad** con selector de fecha
- 🍳 **Sugerencia de recetas con IA** (Firebase AI Logic · Gemini)
  - Diálogo de carga mientras se genera la receta
  - Receta en Markdown renderizada con **Markwon**
- 🚪 **Cerrar sesión** desde la barra superior

---

## 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje de la app |
| Material 3 | Componentes y tema visual |
| Firebase Authentication | Login y registro |
| Cloud Firestore | Base de datos de ingredientes |
| Firebase AI Logic (Gemini) | Generación de recetas |
| Markwon | Renderizado de Markdown |
| View Binding | Acceso a las vistas |

---

## 📱 Pantallas

| Pantalla | Activity | Layout |
|---|---|---|
| Login | `login_Activity` | `activity_login.xml` |
| Registro | `register_Activity` | `activity_register.xml` |
| Nevera | `fridge_Activity` | `activity_fridge.xml` + `item_ingredient.xml` |
| Receta | `Recipe_Activity` | `activity_recipe.xml` |

<!-- Añade aquí tus capturas:
| Login | Registro | Nevera | Receta |
|---|---|---|---|
| ![](docs/login.png) | ![](docs/register.png) | ![](docs/fridge.png) | ![](docs/recipe.png) |
-->

---

## 🗂️ Modelo de datos

Los ingredientes se guardan en una subcolección por usuario:

```
users/{uid}/ingredients/{ingredientId}
```

| Campo | Tipo | Obligatorio |
|---|---|---|
| `name` | String | ✅ |
| `quantity` | Int | ✅ |
| `unit` | String? | ❌ |
| `category` | String? | ❌ |
| `expiration` | Date? | ❌ |
| `addedAt` | Date | ✅ |

**Unidades:** units, grams, kg, liters, ml, packs
**Categorías:** Fruit & Vegs, Meat & Fish, Dairy & Eggs, Bakery & Bread, Grains & Pasta, Sauces & Condiments, Drinks, Leftovers, Other

---

## 🔒 Firestore Security Rules

Solo el usuario autenticado puede leer y escribir **sus** ingredientes:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/ingredients/{ingredientId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 🛡️ App Check

La app usa **Firebase App Check** con el proveedor *debug* (`AIFridgeApp.kt`): cada llamada a la IA lleva un token que demuestra que viene de esta app.

En la consola, App Check está en modo **No aplicado** para Firebase AI Logic, para que la práctica se pueda probar en cualquier ordenador sin registrar tokens. Para aplicarlo:

1. Ejecutar la app y copiar el token debug de Logcat (buscar `DebugAppCheckProvider`).
2. Registrarlo en *App Check → Apps → ⋮ → Administrar tokens de depuración*.
3. Cambiar Firebase AI Logic a **Aplicado** en *App Check → APIs*.

---

## 🚀 Cómo ejecutarlo

1. Clona el repositorio y entra en la carpeta de esta práctica:
   ```bash
   git clone https://github.com/marouan-sh/NOMBRE-DEL-REPO.git
   cd NOMBRE-DEL-REPO/AIFridge
   ```
   En Android Studio abre **solo la carpeta `AIFridge`** (*File → Open*), no la raíz del repositorio.
2. Crea un proyecto en la [consola de Firebase](https://console.firebase.google.com/) y añade una app Android con el mismo `applicationId`.
3. Descarga el archivo `google-services.json` y ponlo en `AIFridge/app/`.
4. En Firebase activa:
   - **Authentication** → proveedor *Email/Password*
   - **Firestore Database** (y pega las reglas de arriba)
   - **AI Logic** → *Gemini Developer API*
   - **App Check** → pestaña *APIs* → Firebase AI Logic en modo **No aplicado** (ver abajo)
5. Abre el proyecto en Android Studio, sincroniza Gradle y ejecuta.

> ⚠️ El archivo `google-services.json` no está en el repositorio por seguridad.

---

## ✅ Estado

- [x] Login con validación y errores de Firebase
- [x] Registro con validación y confirmación de contraseña
- [x] CRUD de ingredientes en Firestore
- [x] Borrado con diálogo de confirmación
- [x] Generación de receta con Gemini
- [x] Pantalla de receta con Markwon
- [x] Layout adaptado a horizontal

---

## 👤 Autor

**Marouan Sanhaji** — CFGS DAM · INS Marianao

[GitHub](https://github.com/marouan-sh) · [LinkedIn](https://www.linkedin.com/in/marouan-sanhaji-79922335b)
