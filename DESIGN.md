---
name: FiadoYa
description: Consola sobria para operar crédito comercial con claridad.
colors:
  blue: "#1e3a8a"
  green: "#059669"
  text: "#1e293b"
  muted: "#58677d"
  canvas: "#f8fafc"
  surface: "#ffffff"
  line: "#e2e8f0"
  control-line: "#cbd5e1"
  positive: "#047857"
  positive-bg: "#ecfdf5"
  danger: "#b91c1c"
  danger-bg: "#fef2f2"
typography:
  headline:
    fontFamily: "Inter, Arial, sans-serif"
    fontSize: "1.75rem"
    fontWeight: 700
    lineHeight: 1.25
    letterSpacing: "-0.03em"
  title:
    fontFamily: "Inter, Arial, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 650
    letterSpacing: "-0.02em"
  body:
    fontFamily: "Inter, Arial, sans-serif"
    fontSize: "0.875rem"
    lineHeight: 1.65
  label:
    fontFamily: "Inter, Arial, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 500
rounded:
  tag: "4px"
  field: "6px"
  control: "7px"
  panel: "9px"
spacing:
  compact: "0.5rem"
  regular: "1rem"
  section: "1.5rem"
components:
  button-primary:
    backgroundColor: "{colors.blue}"
    textColor: "{colors.surface}"
    rounded: "{rounded.control}"
    padding: "0.65rem 1rem"
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.blue}"
    rounded: "{rounded.control}"
    padding: "0.65rem 1rem"
  field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    rounded: "{rounded.field}"
    padding: "0.7rem 0.8rem"
  panel:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.panel}"
    padding: "1.4rem"
  tag:
    backgroundColor: "{colors.positive-bg}"
    textColor: "{colors.positive}"
    rounded: "{rounded.tag}"
    padding: "0.25rem 0.5rem"
---

# Design System: FiadoYa

## Overview

**Creative North Star: "Consola operativa sobria"**

La dirección fijada por el usuario se materializa en superficies claras, tipografía Inter y jerarquía financiera. La densidad permite consultar importes, condiciones y movimientos sin perder el contexto del comercio activo.

Este registro describe el código implementado; la fuente visual normativa es `frontend/src/styles.css`, junto con los componentes Angular. No registra referencias visuales externas aprobadas.

**Key Characteristics:**
- Superficies blancas sobre fondo frío y separadores finos.
- Azul para acciones y énfasis; verde para identidad y estados positivos.
- Importes alineados y ayudas junto a los campos.

## Colors

La paleta combina azul profundo y verde con neutros fríos; los valores normativos están en el frontmatter.

### Primary
- **Azul operativo** (`blue`): acciones principales, identidad, navegación activa y métrica principal.

### Secondary
- **Verde de identidad** (`green`): parte del logotipo y acentos de presentación.
- **Verde positivo** (`positive`, `positive-bg`): importes recaudados y etiquetas favorables.
- **Rojo de deuda** (`danger`, `danger-bg`): cartera vencida y etiquetas de alerta.

### Neutral
- **Fondo frío** (`canvas`) y **superficie blanca** (`surface`): separan espacio de trabajo y contenido.
- **Texto principal** (`text`) y **texto secundario** (`muted`): sostienen la jerarquía de lectura.
- **Separador** (`line`) y **borde de control** (`control-line`): delimitan paneles, filas y campos.

### Named Rules
**The Estado con texto Rule.** Las etiquetas conservan nombres visibles de estado además del color.

## Typography

**Display Font:** Inter, con Arial y sans-serif de respaldo.
**Body Font:** Inter, con Arial y sans-serif de respaldo.

**Character:** Una sola familia mantiene la continuidad entre formularios y datos. Los títulos usan espaciado ligeramente cerrado; los párrafos priorizan lectura explicativa.

### Hierarchy
- **Headline:** título principal de página; en móvil pasa a (1.5rem).
- **Title:** cabeceras de panel; subtítulos menores usan (1rem) y peso (650).
- **Body:** párrafos y controles comparten tamaño base; los controles usan interlineado (1.4).
- **Label:** etiquetas persistentes de campos; botones usan peso (600).
- **Financial values:** métricas principales usan (1.6rem), peso (650) y cifras tabulares; tablas usan (0.8rem).

### Named Rules
**The Cifra alineada Rule.** Las columnas numéricas se alinean a la derecha y emplean cifras tabulares; las métricas también usan cifras tabulares.

## Layout

La estructura de escritorio reserva una barra lateral fija de (232px), una cabecera de (82px) y contenido con ancho máximo de (1600px). El contenido principal usa separación lateral de (2rem). Los formularios y el punto de venta disponen dos columnas; los indicadores se distribuyen en cuatro.

A (1150px) o menos la barra lateral pasa a (205px), el contenido a (1.3rem) de relleno y la cuadrícula del dashboard a una columna. A (780px) o menos la navegación se oculta lateralmente y se abre mediante botón; la cabecera mide (70px), el contenido usa (1rem), los indicadores quedan en dos columnas y formularios, venta y liquidación se apilan. A partir de (1600px), la columna auxiliar del dashboard crece de (300px) a (340px).

Las tablas conservan columnas y permiten desplazamiento horizontal dentro de su contenedor. La autenticación pasa de dos columnas a una en móvil y reduce su relato a la marca. La impresión retira navegación, cabecera, botones y pie, y evita partir paneles.

## Elevation & Depth

Las superficies de trabajo son planas: fondo, borde y espacio construyen la jerarquía. Las sombras se reservan para ayuda contextual y navegación móvil superpuesta.

### Shadow Vocabulary
- **Ayuda contextual:** `0 6px 20px #0f172a26`.
- **Navegación móvil:** `5px 0 20px #0f172a15`.

### Named Rules
**The Panel plano Rule.** Los paneles de trabajo usan superficie blanca y borde fino; las capas superpuestas concentran las sombras observadas.

## Shapes

Los controles y paneles son rectángulos suavemente redondeados. Campos, botones, paneles y etiquetas tienen radios propios registrados arriba. Avatares e indicadores de ayuda son circulares. Las tablas separan filas mediante líneas horizontales; los artículos del catálogo mantienen bordes rectos entre filas.

## Components

### Buttons

Acciones compactas con texto claro. El botón principal usa azul y texto blanco, altura mínima (42px), tamaño (0.875rem) y peso (600). Al pasar el puntero oscurece a (#172f73). El secundario usa borde de control y fondo blanco; su hover es (#f1f5f9). La acción de texto usa fondo transparente y hover (#eef2ff). El estado deshabilitado usa opacidad (0.55) y cursor de espera.

### Chips

Etiquetas pequeñas con texto explícito, peso (600) y tamaño (0.675rem). Las variantes positiva, peligro e inactiva cambian fondo y texto sin eliminar la descripción de estado.

### Cards / Containers

Paneles blancos con borde (1px) y radio de panel; relleno general (1.4rem), reducido a (1rem) en móvil. Las métricas usan relleno (1.25rem); la principal invierte a azul con textos blancos y secundarios claros.

### Inputs / Fields

Campos blancos con borde de control (1px), altura mínima (44px) y tamaño (0.875rem). La etiqueta permanece encima; una ayuda desplegable junto a ella explica el dato. El foco visible compartido usa contorno azul claro (#60a5fa) de (3px), separado (3px). Los campos deshabilitados usan fondo (#f1f5f9). Los errores y éxitos aparecen en bloques de mensaje con texto, fondo y borde semánticos.

### Navigation

Botones alineados a la izquierda con texto secundario; hover gris claro (#f1f5f9), selección azul sobre fondo (#edf2fc) y peso (650). En móvil, un botón de cabecera abre la barra lateral por debajo de la cabecera.

### Financial tables

Cabeceras pequeñas (0.7rem), peso (500), fondo frío y separador inferior. Filas con relleno vertical (1rem), texto principal y detalle secundario. Los importes mantienen alineación derecha y cifras tabulares.

## Do's and Don'ts

### Do:
- **Do** conservar el contexto del comercio activo y las etiquetas persistentes de campos.
- **Do** usar cifras tabulares y alineación derecha en columnas monetarias.
- **Do** mantener foco visible y ayuda contextual accesible por teclado.
- **Do** conservar la adaptación a una columna y el desplazamiento interno de tablas en móvil.

### Don't:
- **Don't** sustituir el estado escrito por un indicador basado solo en color.
- **Don't** convertir los paneles planos de operación en tarjetas con sombras decorativas.
- **Don't** introducir animaciones innecesarias en esta interfaz sobria.
